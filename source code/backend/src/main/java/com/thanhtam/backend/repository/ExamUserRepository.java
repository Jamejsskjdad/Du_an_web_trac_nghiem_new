package com.thanhtam.backend.repository;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.thanhtam.backend.entity.ExamUser;
@Repository
public interface ExamUserRepository extends JpaRepository<ExamUser, Long> {
    List<ExamUser> findAllByUser_Username(String username);
    List<ExamUser> findAllByUser_UsernameAndExam_Canceled(String username, boolean canceled);
    ExamUser findByExam_IdAndUser_Username(Long examId, String username);
    List<ExamUser> findAllByExam_Part_Course_IdAndUser_UsernameAndTotalPointIsGreaterThan(Long courseId, String username, Double point);
    List<ExamUser> findAllByExam_Id(Long examId);
    List<ExamUser> findExamUsersByOrderByTimeFinish();
    List<ExamUser> findExamUsersByIsFinishedIsTrueAndExam_Id(Long examId);
    List<ExamUser> findByExam_Part_IdIn(Set<Long> partIds);
    @Transactional
    @Modifying
    @Query("DELETE FROM ExamUser eu WHERE eu.user.id IN :userIds")
    void deleteAllByUserIds(@Param("userIds") List<Long> userIds);
    @Query("SELECT eu FROM ExamUser eu WHERE eu.exam.id = :examId AND eu.answerSheet LIKE %:questionIdStr%")
    List<ExamUser> findExamUsersContainingQuestionId(@Param("examId") Long examId, @Param("questionIdStr") String questionIdStr);
}
