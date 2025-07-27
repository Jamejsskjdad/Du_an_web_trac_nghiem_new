package com.thanhtam.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanhtam.backend.dto.ServiceResult;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.service.ProfileService;
import com.thanhtam.backend.service.UserService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/api")
public class ProfileController {
    private ProfileService profileService;
    private UserService userService;

    @Autowired
    public ProfileController(ProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllProfiles() {

        List<Profile> profiles = profileService.getAllProfiles();
        return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), "Get list of profile successfully!", profiles));

    }

    @PatchMapping("/profiles/{id}/icon")
    public ResponseEntity<?> updateIcon(@PathVariable Long id, @RequestBody String icon) {
        try {
            // Bỏ dấu ngoặc kép nếu có
            icon = icon.replaceAll("^\"|\"$", "");
    
            profileService.updateIcon(id, icon);
            return ResponseEntity.ok(new ServiceResult(200, "Cập nhật icon thành công!", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ServiceResult(500, "Lỗi khi cập nhật icon!", e.getMessage()));
        }
    }
    

}
