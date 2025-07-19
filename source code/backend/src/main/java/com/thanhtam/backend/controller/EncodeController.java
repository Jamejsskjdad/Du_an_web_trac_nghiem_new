package com.thanhtam.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class EncodeController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/encode")
    public String encodePassword(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }
}
