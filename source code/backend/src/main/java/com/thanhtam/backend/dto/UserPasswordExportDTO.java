// dto/UserPasswordExportDTO.java
package com.thanhtam.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserPasswordExportDTO {
    private String username;
    private String password;
    private String intakeName;
}
