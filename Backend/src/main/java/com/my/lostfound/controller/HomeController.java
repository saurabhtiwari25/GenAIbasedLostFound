package com.my.lostfound.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
            "name", "Lost and Found System API",
            "status", "UP",
            "version", "1.0",
            "message", "Welcome to the A.I. Powered Lost and Found System. All systems are operational."
        ));
    }
}
