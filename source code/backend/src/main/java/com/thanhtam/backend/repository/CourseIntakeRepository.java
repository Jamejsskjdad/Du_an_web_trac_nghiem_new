package com.thanhtam.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thanhtam.backend.entity.CourseIntake;

public interface CourseIntakeRepository extends JpaRepository<CourseIntake, Long> {
    @Modifying
    @Query("UPDATE CourseIntake ci SET ci.intake.id = :intakeId WHERE ci.course.id = :courseId")
    void updateIntakeByCourseId(@Param("courseId") Long courseId, @Param("intakeId") Long intakeId);

}
