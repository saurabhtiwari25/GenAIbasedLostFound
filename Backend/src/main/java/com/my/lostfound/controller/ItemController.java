package com.my.lostfound.controller;

import com.my.lostfound.dto.ItemRequestDto;
import com.my.lostfound.dto.ItemResponseDto;
import com.my.lostfound.dto.MarkFoundRequestDto;
import com.my.lostfound.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;


    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {

        log.info("Fetching all items");

        List<ItemResponseDto> items =
                itemService.getAllItems();

        return ResponseEntity.ok(items);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getItemById(@PathVariable Long id) {
        log.info("Fetching item with id: {}", id);
        ItemResponseDto item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ItemResponseDto>> getItemsByUser(
            @PathVariable Long userId) {

        log.info("Fetching items for user id: {}", userId);

        List<ItemResponseDto> items =
                itemService.getItemsByUser(userId);

        return ResponseEntity.ok(items);
    }


    @GetMapping("/{itemId}/smart-matches")
    public ResponseEntity<List<ItemResponseDto>> getSmartMatches(@PathVariable Long itemId) {
        log.info("Requesting smart matches for item id: {}", itemId);
        List<ItemResponseDto> matches = itemService.getSmartMatches(itemId);
        return ResponseEntity.ok(matches);
    }


    @GetMapping("/search")
    public ResponseEntity<List<ItemResponseDto>> searchByTitle(
            @RequestParam String keyword) {

        log.info("Searching items: {}", keyword);

        List<ItemResponseDto> result =
                itemService.searchByTitle(keyword);

        return ResponseEntity.ok(result);
    }


    @GetMapping("/filter")
    public ResponseEntity<List<ItemResponseDto>> filterItems(
            @RequestParam String location,
            @RequestParam boolean found) {

        log.info("Filtering items");

        List<ItemResponseDto> result =
                itemService.filterItems(location, found);

        return ResponseEntity.ok(result);
    }


    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(@Valid @RequestBody ItemRequestDto dto) {
        log.info("Creating new item");
        ItemResponseDto response = itemService.createItem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDto> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequestDto dto) {

        log.info("Updating item id: {}", id);
        ItemResponseDto updated = itemService.updateItem(id, dto);
        return ResponseEntity.ok(updated);
    }


    @PostMapping("/{id}/found")
    public ResponseEntity<ItemResponseDto> markAsFound(
            @PathVariable Long id,
            @Valid @RequestBody MarkFoundRequestDto dto) {

        log.info("Marking item {} as found", id);
        ItemResponseDto updated = itemService.markAsFound(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }


    @PostMapping("/{id}/upload")
    public ResponseEntity<String> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        log.info("Uploading image for item {}", id);
        String path = itemService.uploadImage(id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(path);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {
        log.info("Deleting item id: {}", id);
        itemService.deleteItem(id);
        return ResponseEntity.ok("Item deleted successfully");
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<String> resolveItem(@PathVariable Long id) {
        log.info("Resolving item id: {}", id);
        itemService.resolveItem(id);
        return ResponseEntity.ok("Item resolved and deleted successfully");
    }

}
