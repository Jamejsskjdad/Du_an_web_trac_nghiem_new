package com.thanhtam.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.thanhtam.backend.dto.CourseDto;
import com.thanhtam.backend.dto.IntakeDto;
import com.thanhtam.backend.entity.Course;

public class CourseMapper {
    public static CourseDto toDto(Course course) {
        List<IntakeDto> intakeDtos = course.getIntakes() != null
            ? course.getIntakes().stream()
                .map(i -> new IntakeDto(i.getId(), i.getName()))
                .collect(Collectors.toList())
            : null;
        return new CourseDto(
            course.getId(),
            course.getCourseCode(),
            course.getName(),
            intakeDtos
        );
    }
}
