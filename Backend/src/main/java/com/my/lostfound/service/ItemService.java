package com.my.lostfound.service;

import com.my.lostfound.dto.ItemRequestDto;
import com.my.lostfound.dto.ItemResponseDto;
import com.my.lostfound.dto.MarkFoundRequestDto;
import com.my.lostfound.entity.Item;
import com.my.lostfound.exception.ResourceNotFoundException;
import com.my.lostfound.exception.UnauthorizedException;
import com.my.lostfound.exception.ForbiddenException;
import com.my.lostfound.exception.BadRequestException;
import com.my.lostfound.repository.ItemRepository;
import com.my.lostfound.util.FileUploadUtil;
import com.my.lostfound.entity.User;
import com.my.lostfound.repository.UserRepository;

import org.springframework.ai.chat.client.ChatClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final FileUploadUtil fileUploadUtil;
    private final ChatClient chatClient;
    private final NotificationService notificationService;

    public ItemService(ItemRepository itemRepository,
                       UserRepository userRepository,
                       FileUploadUtil fileUploadUtil,
                       ChatClient.Builder chatClientBuilder,
                       NotificationService notificationService) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.fileUploadUtil = fileUploadUtil;
        this.chatClient = chatClientBuilder.build();
        this.notificationService = notificationService;
    }

    private record MatchResponse(List<Long> matchedIds) {}

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("User not authenticated");
        }
        return ((User) auth.getPrincipal()).getId();
    }

    @Transactional
    public ItemResponseDto createItem(ItemRequestDto dto) {

        log.info("Creating item: {}", dto.getTitle());

        Long currentUserId = getCurrentUserId();
        User reporter = userRepository.findById(currentUserId)
                .orElseThrow(() -> {
                    log.error("Reporter not found with id: {}", currentUserId);
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

    public Page<ItemResponseDto> getAllItems(Pageable pageable) {

        log.info("Fetching all items");

        Page<Item> items =
                itemRepository.findAll(pageable);

        return items.map(this::mapToDTO);
    }

    public Page<ItemResponseDto> getItemsByUser(Long userId, Pageable pageable) {
        log.info("Fetching items for user id {}", userId);

        Page<Item> items =
                itemRepository.findByReporterId(userId, pageable);

        return items.map(this::mapToDTO);
    }

    public List<ItemResponseDto> getSmartMatches(Long itemId) {
        log.info("Finding smart matches for item id {}", itemId);

        Item sourceItem = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", itemId);
                    return new ResourceNotFoundException("Item not found");
                });
        return getSmartMatches(sourceItem);
    }

    private List<ItemResponseDto> getSmartMatches(Item sourceItem) {
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
                "Based on the title, description, and location, determine which %s items are highly probable matches.",
                sourceType,
                sourceItem.getTitle(),
                sourceItem.getDescription(),
                sourceItem.getLocation(),
                candidateType,
                candidatesContext,
                candidateType
        );

        try {
            MatchResponse response = chatClient.prompt()
                    .user(promptText)
                    .call()
                    .entity(MatchResponse.class);

            if (response == null || response.matchedIds() == null || response.matchedIds().isEmpty()) {
                return List.of();
            }

            log.info("LLM Response matched IDs: {}", response.matchedIds());

            List<Item> matchedItems = itemRepository.findAllById(response.matchedIds());
            return matchedItems.stream()
                    .map(this::mapToDTO)
                    .toList();

        } catch (Exception e) {
            log.error("Failed to get smart matches", e);
            return List.of();
        }
    }

    @Transactional
    public void confirmMatch(Long sourceId, Long matchId) {
        log.info("User confirmed match between source {} and match {}", sourceId, matchId);
        Item sourceItem = itemRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source item not found"));
        Item matchItem = itemRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Matched item not found"));

        Long currentUserId = getCurrentUserId();
        if (!sourceItem.getReporter().getId().equals(currentUserId)) {
            throw new ForbiddenException("Only the original reporter can confirm a match for this item");
        }

        if (sourceItem.isFound()) {
            User lostItemReporter = matchItem.getReporter();
            if (lostItemReporter != null) {
                String message = String.format("Great news! Your lost item '%s' has been confirmed as a match with a found item by %s at '%s'. Contact number: %s | Email: %s", 
                        matchItem.getTitle(), sourceItem.getReporter().getName(), sourceItem.getLocation(), sourceItem.getContactInfo(), sourceItem.getReporter().getEmail());
                notificationService.createNotification(lostItemReporter, message);
            }
        } else {
            User lostItemReporter = sourceItem.getReporter();
            User founder = matchItem.getReporter();
            String founderEmail = (founder != null) ? founder.getEmail() : "Not available";
            String founderName = (founder != null) ? founder.getName() : "Someone";
            
            String message = String.format("Great news! Your lost item '%s' has been confirmed as a match with a found item by %s at '%s'. Contact number: %s | Email: %s", 
                    sourceItem.getTitle(), founderName, matchItem.getLocation(), matchItem.getContactInfo(), founderEmail);
            notificationService.createNotification(lostItemReporter, message);
        }
        
        itemRepository.deleteById(sourceItem.getId());
        itemRepository.deleteById(matchItem.getId());
        log.info("Confirmed match successful. Deleted source item {} and matched item {}", sourceId, matchId);
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

        Long currentUserId = getCurrentUserId();
        if (!item.getReporter().getId().equals(currentUserId)) {
            throw new ForbiddenException("Only the original reporter can edit this item");
        }

        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setLocation(dto.getLocation());
        item.setContactInfo(dto.getContactInfo());
        item.setFound(dto.isFound());

        Item updated = itemRepository.save(item);

        return mapToDTO(updated);
    }

    @Transactional
    public ItemResponseDto markAsFound(Long id, MarkFoundRequestDto dto) {

        Long currentUserId = getCurrentUserId();
        log.info("Marking item id {} as found by user {}", id, currentUserId);

        Item item = itemRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", id);
                    return new ResourceNotFoundException("Item not found");
                });

        if (item.getReporter().getId().equals(currentUserId)) {
            throw new BadRequestException("You cannot mark your own item as found");
        }

        if (item.isFound()) {
            throw new BadRequestException("This item is already marked as found");
        }

        item.setFound(true);
        item.setFoundLocation(dto.getFoundLocation());
        item.setFounderName(dto.getFounderName());
        item.setFounderContactInfo(dto.getFounderContactInfo());

        Item updated = itemRepository.save(item);

        User finder = userRepository.findById(currentUserId).orElse(null);
        String finderEmail = (finder != null) ? finder.getEmail() : "Not available";

        String message = String.format(
                "Great news! Your lost item \"%s\" has been found by %s at \"%s\". Contact number: %s | Email: %s",
                item.getTitle(), dto.getFounderName(), dto.getFoundLocation(), dto.getFounderContactInfo(), finderEmail);
        notificationService.createNotification(item.getReporter(), message);

        ItemResponseDto responseDto = mapToDTO(updated);
        log.info("Item {} marked as found, awaiting owner verification", id);

        return responseDto;
    }

    private void deleteItemInternal(Long id, String logMessage, String errorMessage) {
        log.info(logMessage, id);
        Item item = itemRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", id);
                    return new ResourceNotFoundException("Item not found");
                });

        Long currentUserId = getCurrentUserId();
        if (!item.getReporter().getId().equals(currentUserId)) {
            throw new ForbiddenException(errorMessage);
        }
        itemRepository.delete(item);
    }

    public void resolveItem(Long id) {
        deleteItemInternal(id, "Owner accepted item id {} as resolved, deleting it", "Only the original reporter can resolve this item");
    }

    public void deleteItem(Long id) {
        deleteItemInternal(id, "Hard deleting item id {}", "Only the original reporter can delete this item");
    }

    public Page<ItemResponseDto> searchByTitle(String keyword, Pageable pageable) {

        log.info("Searching items: {}", keyword);

        return itemRepository
                .findByTitleContainingIgnoreCase(keyword, pageable)
                .map(this::mapToDTO);
    }

    public Page<ItemResponseDto> filterItems(String location, boolean found, Pageable pageable) {

        log.info("Filtering items by location '{}' and found={}",
                location, found);

        return itemRepository
                .findByLocationAndFound(location, found, pageable)
                .map(this::mapToDTO);
    }

    public String uploadImage(Long id, MultipartFile file) {

        log.info("Uploading file for item {}", id);

        if (file.getContentType() == null ||
                !file.getContentType().startsWith("image/")) {

            throw new BadRequestException("Only image files are allowed");
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

    public void performAutoMatching(Item sourceItem) {
        log.info("Starting auto-matching for item id {}", sourceItem.getId());
        try {
            List<ItemResponseDto> matches = getSmartMatches(sourceItem);
            if (matches != null && !matches.isEmpty()) {
                User userToNotify = sourceItem.getReporter();
                if (userToNotify != null) {
                    String message = String.format("AI has found a potential match for your item '%s'. Check the item details to verify!", sourceItem.getTitle());
                    notificationService.createNotification(userToNotify, message);
                }
            }
        } catch (Exception e) {
            log.error("Error during auto matching for item id {}", sourceItem.getId(), e);
        }
    }
}