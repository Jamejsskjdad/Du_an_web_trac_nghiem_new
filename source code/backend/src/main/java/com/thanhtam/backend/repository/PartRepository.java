package com.thanhtam.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Part;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {
    Page<Part> findAllByCourseId(Long courseId, Pageable pageable);

    List<Part> findAllByCourse(Course course);

    // Thêm dòng này để dùng cho chức năng xóa theo courseId!
    List<Part> findAllByCourseId(Long courseId);
}
