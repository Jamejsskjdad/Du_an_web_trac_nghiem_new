package com.thanhtam.backend.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profiles")
public class Profile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String phone;

    private String address;

    // Thêm các field bị thiếu đang được gọi
    private String firstName;
    private String lastName;
    private String image;
}
