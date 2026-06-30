package com.my.lostfound.service;

import com.my.lostfound.dto.ItemRequestDto;
import com.my.lostfound.dto.ItemResponseDto;
import com.my.lostfound.dto.MarkFoundRequestDto;
import com.my.lostfound.entity.Item;
import com.my.lostfound.exception.ResourceNotFoundException;
import com.my.lostfound.repository.ItemRepository;
import com.my.lostfound.util.FileUploadUtil;
import com.my.lostfound.entity.User;
import com.my.lostfound.repository.UserRepository;

import org.springframework.ai.chat.client.ChatClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final FileUploadUtil fileUploadUtil;
    private final ChatClient.Builder chatClientBuilder;
    private final NotificationService notificationService;


    public ItemResponseDto createItem(ItemRequestDto dto) {

        log.info("Creating item: {}", dto.getTitle());

        User reporter = userRepository.findById(dto.getReporterId())
                .orElseThrow(() -> {
                    log.error("Reporter not found with id: {}", dto.getReporterId());
                    return new ResourceNotFoundException("Reporter not found");
                });

        Item item = Item.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .found(dto.isFound())
                .contactInfo(dto.getContactInfo())
                .reporter(reporter)
                .build();

        Item saved = itemRepository.save(item);

        CompletableFuture.runAsync(() -> performAutoMatching(saved));

        return mapToDTO(saved);
    }


    public List<ItemResponseDto> getAllItems() {

        log.info("Fetching all items");

        List<Item> items =
                itemRepository.findAll();

        return items.stream().map(this::mapToDTO).collect(Collectors.toList());
    }


    public List<ItemResponseDto> getItemsByUser(Long userId) {
        log.info("Fetching items for user id {}", userId);

        List<Item> items =
                itemRepository.findByReporterId(userId);

        return items.stream().map(this::mapToDTO).collect(Collectors.toList());
    }


    public List<ItemResponseDto> getSmartMatches(Long itemId) {
        log.info("Finding smart matches for item id {}", itemId);

        Item sourceItem = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", itemId);
                    return new ResourceNotFoundException("Item not found");
                });

        List<Item> candidateItems;
        String sourceType;
        String candidateType;

        if (sourceItem.isFound()) {
            candidateItems = itemRepository.findByFoundFalse();
            sourceType = "FOUND";
            candidateType = "LOST";
        } else {
            candidateItems = itemRepository.findByFoundTrue();
            sourceType = "LOST";
            candidateType = "FOUND";
        }

        if (candidateItems.isEmpty()) {
            return List.of();
        }

        String candidatesContext = candidateItems.stream()
                .map(i -> String.format("ID: %d | Title: %s | Desc: %s | Location: %s",
                        i.getId(), i.getTitle(), i.getDescription(), i.getLocation()))
                .collect(Collectors.joining("\n"));

        String promptText = String.format(
                "You are an intelligent Lost and Found matching assistant. " +
                "I have a %s item: Title: '%s', Description: '%s', Location: '%s'. " +
                "Here is a list of all %s items:\n%s\n" +
                "Based on the title, description, and location, determine which %s items are highly probable matches. " +
                "Return ONLY valid JSON in the following format:\n" +
                "{\"matchedIds\":[2,5,7]}\n" +
                "If there are no matches, return:\n" +
                "{\"matchedIds\":[]}\n" +
                "Do not include explanations, markdown, labels, or any extra text.",
                sourceType,
                sourceItem.getTitle(),
                sourceItem.getDescription(),
                sourceItem.getLocation(),
                candidateType,
                candidatesContext,
                candidateType
        );

        String response = chatClientBuilder.build().prompt()
                .user(promptText)
                .call()
                .content();

        log.info("LLM Response for smart matches: {}", response);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode idsNode = root.get("matchedIds");

            if (idsNode == null || !idsNode.isArray()) {
                return List.of();
            }

            List<Long> matchedIds = new ArrayList<>();
            for (JsonNode id : idsNode) {
                matchedIds.add(id.asLong());
            }

            return itemRepository.findAllById(matchedIds)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", response, e);
            return List.of();
        }
    }


    public ItemResponseDto getItemById(Long id) {

        log.info("Getting item id {}", id);

        Item item = itemRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", id);
                    return new ResourceNotFoundException("Item not found");
                });

        return mapToDTO(item);
    }


    public ItemResponseDto updateItem(Long id, ItemRequestDto dto) {

        log.info("Updating item id {}", id);

        Item item = itemRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", id);
                    return new ResourceNotFoundException("Item not found");
                });

        if (!item.getReporter().getId().equals(dto.getReporterId())) {
            throw new RuntimeException("Only the original reporter can edit this item");
        }

        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setLocation(dto.getLocation());
        item.setContactInfo(dto.getContactInfo());
        item.setFound(dto.isFound());

        Item updated = itemRepository.save(item);

        return mapToDTO(updated);
    }


    public ItemResponseDto markAsFound(Long id, MarkFoundRequestDto dto) {

        log.info("Marking item id {} as found by user {}", id, dto.getFinderUserId());

        Item item = itemRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", id);
                    return new ResourceNotFoundException("Item not found");
                });

        if (item.isFound()) {
            throw new RuntimeException("This item is already marked as found");
        }

        item.setFound(true);
        item.setFoundLocation(dto.getFoundLocation());
        item.setFounderName(dto.getFounderName());
        item.setFounderContactInfo(dto.getFounderContactInfo());

        Item updated = itemRepository.save(item);

        User finder = userRepository.findById(dto.getFinderUserId()).orElse(null);
        String finderEmail = (finder != null) ? finder.getEmail() : "Not available";

        String message = String.format(
                "Great news! Your lost item \"%s\" has been found by %s at \"%s\". Contact number: %s | Email: %s",
                item.getTitle(), dto.getFounderName(), dto.getFoundLocation(), dto.getFounderContactInfo(), finderEmail);
        notificationService.createNotification(item.getReporter(), message);

        ItemResponseDto responseDto = mapToDTO(updated);
        itemRepository.delete(updated);
        log.info("Item {} deleted after being manually marked as found", id);

        return responseDto;
    }


    public void deleteItem(Long id) {

        log.info("Hard deleting item id {}", id);

        Item item = itemRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", id);
                    return new ResourceNotFoundException("Item not found");
                });

        itemRepository.delete(item);
    }


    public List<ItemResponseDto> searchByTitle(String keyword) {

        log.info("Searching items: {}", keyword);

        return itemRepository
                .findByTitleContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    public List<ItemResponseDto> filterItems(String location, boolean found) {

        log.info("Filtering items by location '{}' and found={}",
                location, found);

        return itemRepository
                .findByLocationAndFound(location, found)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    public String uploadImage(Long id, MultipartFile file) {

        log.info("Uploading file for item {}", id);

        if (file.getContentType() == null ||
                !file.getContentType().startsWith("image/")) {

            throw new RuntimeException("Only image files are allowed");
        }

        Item item = itemRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", id);
                    return new ResourceNotFoundException("Item not found");
                });

        String path = fileUploadUtil.saveFile(file);

        item.setImagePath(path);

        itemRepository.save(item);

        return path;
    }


    private ItemResponseDto mapToDTO(Item item) {

        return ItemResponseDto.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .location(item.getLocation())
                .found(item.isFound())
                .imagePath(item.getImagePath())
                .contactInfo(item.getContactInfo())
                .foundLocation(item.getFoundLocation())
                .founderName(item.getFounderName())
                .founderContactInfo(item.getFounderContactInfo())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .reporterId(item.getReporter() != null ? item.getReporter().getId() : null)
                .reporterName(item.getReporter() != null ? item.getReporter().getName() : null)
                .build();
    }


    private void performAutoMatching(Item sourceItem) {
        log.info("Starting auto-matching for item id {}", sourceItem.getId());
        try {
            List<ItemResponseDto> matches = getSmartMatches(sourceItem.getId());
            if (matches != null && !matches.isEmpty()) {
                ItemResponseDto match = matches.get(0);

                if (sourceItem.isFound()) {
                    User lostItemReporter = userRepository.findById(match.getReporterId()).orElse(null);
                    if (lostItemReporter != null) {
                        String message = String.format("Great news! Your lost item '%s' has been found by %s at '%s'. Contact number: %s | Email: %s", 
                                match.getTitle(), sourceItem.getReporter().getName(), sourceItem.getLocation(), sourceItem.getContactInfo(), sourceItem.getReporter().getEmail());
                        notificationService.createNotification(lostItemReporter, message);
                    }
                } else {
                    User lostItemReporter = sourceItem.getReporter();
                    User founder = userRepository.findById(match.getReporterId()).orElse(null);
                    String founderEmail = (founder != null) ? founder.getEmail() : "Not available";
                    
                    String message = String.format("Great news! Your lost item '%s' has been found by %s at '%s'. Contact number: %s | Email: %s", 
                            sourceItem.getTitle(), match.getReporterName(), match.getLocation(), match.getContactInfo(), founderEmail);
                    notificationService.createNotification(lostItemReporter, message);
                }

                itemRepository.deleteById(sourceItem.getId());
                itemRepository.deleteById(match.getId());
                log.info("Auto-match successful. Deleted source item {} and matched item {}", sourceItem.getId(), match.getId());
            }
        } catch (Exception e) {
            log.error("Error during auto matching for item id {}", sourceItem.getId(), e);
        }
    }
}