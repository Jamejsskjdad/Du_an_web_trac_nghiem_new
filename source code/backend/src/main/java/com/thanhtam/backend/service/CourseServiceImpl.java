package com.thanhtam.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.repository.CourseRepository;

@Service
public class CourseServiceImpl implements CourseService {
    private CourseRepository courseRepository;

    @Autowired
    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }


    @Override
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public List<Course> getCourseList() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> getCourseListByPage(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    public void saveCourse(Course course) {
        courseRepository.save(course);
    }

    @Override
    public void delete(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return courseRepository.existsByCourseCode(code);
    }

    @Override
    public boolean existsById(Long id) {
        return courseRepository.existsById(id);
    }
    @Override
    @Transactional(readOnly = true)
    public List<Course> findAllByIntakeId(Long intakeId) {
        return courseRepository.findAllByIntakes_Id(intakeId);
    }

    @Override
    public Course findCourseByPartId(Long partId) {
        return courseRepository.findCourseByPartId(partId);
    }

}
