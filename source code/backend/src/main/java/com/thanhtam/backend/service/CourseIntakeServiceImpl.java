package com.thanhtam.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.CourseIntake;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.repository.CourseIntakeRepository;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.repository.IntakeRepository;

@Service
public class CourseIntakeServiceImpl implements CourseIntakeService {

    @Autowired
    private CourseIntakeRepository courseIntakeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private IntakeRepository intakeRepository;

    @Override
    public void saveCourseIntake(Long courseId, Long intakeId) {
        Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new RuntimeException("Not found courseId=" + courseId));
        Intake intake = intakeRepository.findById(intakeId)
        .orElseThrow(() -> new RuntimeException("Not found intakeId=" + intakeId));
    
        CourseIntake courseIntake = new CourseIntake();
        courseIntake.setCourse(course);
        courseIntake.setIntake(intake);
        courseIntakeRepository.save(courseIntake);
    }
}
