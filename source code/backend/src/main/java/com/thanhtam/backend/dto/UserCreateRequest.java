package com.thanhtam.backend.dto;

import com.thanhtam.backend.entity.Profile;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String username;
    private String email;
    private String password;
    private Long roleId;
    private Profile profile;
    private Long intakeId; // Nếu cần
}
