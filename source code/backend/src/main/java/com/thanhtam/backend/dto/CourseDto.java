package com.thanhtam.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String courseCode;
    private String name;
    //private String imgUrl;
    private List<IntakeDto> intakes;
}
