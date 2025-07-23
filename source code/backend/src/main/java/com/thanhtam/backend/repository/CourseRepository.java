package com.thanhtam.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.thanhtam.backend.entity.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCourseCode(String s);

    boolean existsById(Long id);

    // KHÔNG dùng @EntityGraph ở đây!
    Page<Course> findAll(Pageable pageable);

    // Dùng khi không phân trang, vẫn có thể dùng @EntityGraph
    @EntityGraph(attributePaths = "intakes")
    List<Course> findAll();

    // Lấy course theo intakeId, có thể dùng khi không cần phân trang
    @EntityGraph(attributePaths = "intakes")
    List<Course> findAllByIntakes_Id(Long intakeId);

    // Lấy course theo partId (native)
    @Query(value="select * from course c join part p on c.id = p.course_id where p.id=:partId", nativeQuery=true)
    Course findCourseByPartId(Long partId);

}

