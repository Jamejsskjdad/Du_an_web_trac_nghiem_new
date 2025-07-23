package com.thanhtam.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.thanhtam.backend.entity.Course;

public interface CourseService {
    Optional<Course> getCourseById(Long id);

    List<Course> getCourseList();

    Page<Course> getCourseListByPage(Pageable pageable);

    void saveCourse(Course course);

    void delete(Long id);

    boolean existsByCode(String code);

    boolean existsById(Long id);

    List<Course> findAllByIntakeId(Long intakeId);
    Course findCourseByPartId(Long partId);
    void deleteManyByIds(List<Long> courseIds);

}
