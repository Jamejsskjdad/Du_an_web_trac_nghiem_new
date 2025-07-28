package com.thanhtam.backend.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thanhtam.backend.entity.Profile;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String username;
    private String password;
    private Long roleId;
    private Profile profile;
    private Long intakeId;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date birthDate;

}
