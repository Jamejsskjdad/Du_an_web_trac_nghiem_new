package com.thanhtam.backend.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thanhtam.backend.dto.CourseDto;
import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.dto.ServiceResult;
import com.thanhtam.backend.dto.pagination.PaginationDetails;
import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.mapper.CourseMapper;
import com.thanhtam.backend.service.CourseService;
import com.thanhtam.backend.service.S3Services;

import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/api")
@Slf4j
public class CourseController {
    private CourseService courseService;
    private S3Services s3Services;

    @Autowired
    public CourseController(CourseService courseService, S3Services s3Services) {
        this.courseService = courseService;
        this.s3Services = s3Services;
    }


    @GetMapping(value = "/course-list")
    public List<CourseDto> getAllCourse() {
        List<Course> courseList = courseService.getCourseList();
        return courseList.stream().map(CourseMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping(value = "/courses")
    public PageResult getCourseListByPage(@PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        Page<Course> page = courseService.getCourseListByPage(pageable);
        List<CourseDto> dtos = page.getContent().stream().map(CourseMapper::toDto).collect(Collectors.toList());
        return new PageResult<>(dtos, new PaginationDetails(page));
    }

    @GetMapping(value = "/courses/{id}/check-course-code")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    public boolean checkCourseCode(@RequestParam String value, @PathVariable Long id) {
        if (courseService.existsByCode(value)) {
            if (courseService.getCourseById(id).get().getCourseCode().equals(value)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @GetMapping(value = "/courses/check-course-code")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    public boolean checkCode(@RequestParam String value) {
        return courseService.existsByCode(value);
    }

    @GetMapping(value = "/courses/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseService.getCourseById(id);
        if (!course.isPresent()) {
            throw new EntityNotFoundException("Not found with course id: " + id);

        }
        return ResponseEntity.ok().body(course);
    }

    @PostMapping(value = "/courses")
    public ResponseEntity<Object> createCourse(@Valid @RequestBody Course course) {
        try {
            if (!courseService.existsByCode(course.getCourseCode())) {
                courseService.saveCourse(course);
                return ResponseEntity.ok().body(new ServiceResult(HttpStatus.CREATED.value(), "Created course successfully!", course));

            } else {
                return ResponseEntity.badRequest().body(new ServiceResult(HttpStatus.CONFLICT.value(), "Duplicate Course!", course.getCourseCode()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.toString());
        }
    }

    @PatchMapping(value = "/courses/{id}")
    public ResponseEntity<?> updateCourse(@Valid @RequestBody Course course, @PathVariable Long id) {
        Optional<Course> updateCourse = courseService.getCourseById(id);
        if (!updateCourse.isPresent()) {
            throw new EntityNotFoundException("Not found with course id: " + id + " successfully!");
        }
        course.setId(id);
        if(course.getImgUrl().isEmpty()){
            course.setImgUrl(updateCourse.get().getImgUrl());
        }
        else{
            course.setImgUrl(course.getImgUrl());

        }
        course.setIntakes(updateCourse.get().getIntakes());
        courseService.saveCourse(course);
        log.error(course.toString());
        return ResponseEntity.ok().body(new ServiceResult(HttpStatus.OK.value(), "Update course with id: " + id, course));
    }

    @DeleteMapping(value = "/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        Optional<Course> courseOptional = courseService.getCourseById(id);
        if (!courseOptional.isPresent()) {
            throw new EntityNotFoundException("Not found with course id:" + id + " successfully!");
        }
        courseService.delete(id);
        return ResponseEntity.ok().body(new ServiceResult(HttpStatus.NO_CONTENT.value(), "Deleted course with id: " + id + " successfully!", null));
    }

    @GetMapping(value = "/courses/part/{partId}")
    public Course getCourseByPart(@PathVariable Long partId) {
        return courseService.findCourseByPartId(partId);
    }

    @GetMapping(value = "/intakes/{intakeId}/courses")
    public List<Course> findAllByIntakeId(@PathVariable Long intakeId) {
        return courseService.findAllByIntakeId(intakeId);
    }


}
