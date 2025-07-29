package com.thanhtam.backend.repository;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.thanhtam.backend.entity.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findAllByPart_Course_Id(Long courseId);
List<Exam> findByCanceledIsTrueOrderByCreatedDateDesc();
    public Page<Exam> findAll(Pageable pageable);
    public Page<Exam> findAllByCreatedBy_Username(Pageable pageable, String username);
    @Transactional
    @Modifying
    @Query(value = "UPDATE exam set exam.canceled=true where exam.id=?" , nativeQuery = true)
    void cancelExam(Long id);
    List<Exam> findByIntake_Id(Long intakeId);
    @Modifying
    @Transactional
    @Query("UPDATE Exam e SET e.createdBy.id = :adminId WHERE e.createdBy.id = :oldUserId")
    void updateCreatedByForExams(@Param("oldUserId") Long oldUserId, @Param("adminId") Long adminId);
    List<Exam> findByPart_Id(Long partId);
    @Query("SELECT e FROM Exam e WHERE e.part.id IN :partIds")
    List<Exam> findExamsByPartIds(@Param("partIds") Set<Long> partIds);
    
    List<Exam> findAllByPartIdIn(Set<Long> partIds);
    @Query("SELECT e FROM Exam e WHERE e.questionData LIKE %:questionIdStr%")
    List<Exam> findExamsContainingQuestionId(@Param("questionIdStr") String questionIdStr);
    
}
