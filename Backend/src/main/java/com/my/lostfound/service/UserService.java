package com.my.lostfound.service;

import com.my.lostfound.dto.LoginRequestDto;
import com.my.lostfound.dto.UserRequestDto;
import com.my.lostfound.dto.UserResponseDto;
import com.my.lostfound.entity.User;
import com.my.lostfound.exception.BadRequestException;
import com.my.lostfound.exception.ResourceNotFoundException;
import com.my.lostfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;


    @Transactional
    public UserResponseDto register(UserRequestDto dto) {
        log.info("Attempting to register user with email: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Registration failed: Email {} already exists", dto.getEmail());
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        user.setPassword(dto.getPassword());

        User savedUser = userRepository.save(user);

        log.info("User registered successfully with id: {}", savedUser.getId());

        return mapToResponseDto(savedUser);
    }


    public UserResponseDto login(LoginRequestDto dto) {
        log.info("User login attempt for email: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with email {}", dto.getEmail());
                    return new ResourceNotFoundException("User not found");
                });

        if (!dto.getPassword().equals(user.getPassword())) {
            log.warn("Login failed: Invalid password for email {}", dto.getEmail());
            throw new BadRequestException("Invalid password");
        }

        log.info("User logged in successfully: {}", user.getId());

        return mapToResponseDto(user);
    }


    public UserResponseDto getUserById(Long id) {
        log.info("Fetching user by id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });

        return mapToResponseDto(user);
    }


    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto response = new UserResponseDto();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return response;
    }
}