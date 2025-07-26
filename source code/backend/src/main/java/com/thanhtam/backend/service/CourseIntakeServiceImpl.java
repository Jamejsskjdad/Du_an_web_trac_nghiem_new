package com.thanhtam.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void saveCourseIntake(Long courseId, Long intakeId) {
        // Kiểm tra có tồn tại không
        courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Not found courseId=" + courseId));
        intakeRepository.findById(intakeId)
            .orElseThrow(() -> new RuntimeException("Not found intakeId=" + intakeId));

        // Gọi update
        courseIntakeRepository.updateIntakeByCourseId(courseId, intakeId);
    }
}
