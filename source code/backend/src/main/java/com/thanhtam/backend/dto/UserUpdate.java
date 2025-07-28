package com.thanhtam.backend.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thanhtam.backend.entity.Profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdate {

    private String username;
    private String password;
    private Profile profile;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date birthDate;
}
