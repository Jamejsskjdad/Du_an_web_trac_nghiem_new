package com.thanhtam.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thanhtam.backend.service.CourseIntakeService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseIntakeController {

    @Autowired
    private CourseIntakeService courseIntakeService;

    @PostMapping("/course-intake")
    public void createCourseIntake(@RequestParam Long courseId, @RequestParam Long intakeId) {
        courseIntakeService.saveCourseIntake(courseId, intakeId);
    }
}
