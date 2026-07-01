package com.my.lostfound.controller;

import com.my.lostfound.dto.LoginRequestDto;
import com.my.lostfound.dto.UserRequestDto;
import com.my.lostfound.dto.UserResponseDto;
import com.my.lostfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto dto) {
        log.info("Registering user: {}", dto.getEmail());
        UserResponseDto response = userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        log.info("User login attempt: {}", dto.getEmail());
        UserResponseDto response = userService.login(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        UserResponseDto response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
}
