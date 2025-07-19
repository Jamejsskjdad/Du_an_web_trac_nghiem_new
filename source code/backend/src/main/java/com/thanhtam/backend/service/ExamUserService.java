package com.thanhtam.backend.service;

import java.util.List;
import java.util.Optional;

import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.User;

public interface ExamUserService {
    void create(Exam exam, List<User> userSet);
    List<ExamUser> getExamListByUsername(String username);
    ExamUser findByExamAndUser(Long examId, String username);
    void update(ExamUser examUser);
    Optional<ExamUser> findExamUserById(Long id);

    List<ExamUser> getCompleteExams(Long courseId, String username);
    List<ExamUser> findAllByExam_Id(Long examId);
    List<ExamUser> findExamUsersByIsFinishedIsTrueAndExam_Id(Long examId);
    void save(ExamUser examUser);

}
