package com.thanhtam.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ChoiceCorrect;
import com.thanhtam.backend.dto.ChoiceList;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
import com.thanhtam.backend.repository.IntakeRepository;
@Service

public class ExamServiceImpl implements ExamService {

    private ExamRepository examRepository;
    private IntakeRepository intakeRepository;
    private PartService partService;
    private UserService userService;
    private QuestionService questionService;
    private ChoiceService choiceService;
    private ExamUserRepository examUserRepository; // Thêm biến này
    @Autowired
    public ExamServiceImpl(ExamRepository examRepository, IntakeRepository intakeRepository, PartService partService, UserService userService, QuestionService questionService, ChoiceService choiceService, ExamUserRepository examUserRepository ) {
        this.examRepository = examRepository;
        this.intakeRepository = intakeRepository;
        this.partService = partService;
        this.userService = userService;
        this.questionService = questionService;
        this.choiceService = choiceService;
        this.examUserRepository = examUserRepository; // Gán giá trị
    }

    @Override
    public Exam saveExam(Exam exam) {
        return examRepository.save(exam);
    }

    @Override
    public Page<Exam> findAll(Pageable pageable) {
        return examRepository.findAll(pageable);
    }

    @Override
    public void cancelExam(Long id) {
        examRepository.cancelExam(id);
    }

    @Override
    public List<Exam> getAll() {
        return examRepository.findAll();
    }

    @Override
    public Optional<Exam> getExamById(Long id) {
        return examRepository.findById(id);
    }


    @Override
    public Page<Exam> findAllByCreatedBy_Username(Pageable pageable, String username) {
        return examRepository.findAllByCreatedBy_Username(pageable, username);
    }


    @Override
public List<ChoiceList> getChoiceList(List<AnswerSheet> userChoices, List<ExamQuestionPoint> examQuestionPoints) {
    List<ChoiceList> choiceLists = new ArrayList<>();
    userChoices.forEach(userChoice -> {
        ChoiceList choiceList = new ChoiceList();
        Question question = questionService.getQuestionById(userChoice.getQuestionId()).get();
        choiceList.setQuestion(question);
        choiceList.setPoint(userChoice.getPoint());

        List<ChoiceCorrect> choiceCorrects = new ArrayList<>();
        switch (question.getQuestionType().getTypeCode()) {
            case TF: {
                String selectedText = userChoice.getChoices().get(0).getChoiceText();
                int userSelected = userChoice.getChoices().get(0).getIsCorrected();
                List<String> correctChoiceTexts = new ArrayList<>();
                question.getChoices().forEach(c -> {
                    if (c.getIsCorrected() == 1) {
                        correctChoiceTexts.add(c.getChoiceText());
                    }
                });
                boolean isCorrect = false;
                if (userSelected == 1 && selectedText != null && !selectedText.trim().isEmpty()) {
                    isCorrect = correctChoiceTexts.contains(selectedText);
                }
                choiceList.setIsSelectedCorrected(isCorrect);
                userChoice.getChoices().forEach(choice -> {
                    ChoiceCorrect choiceCorrect = new ChoiceCorrect();
                    choiceCorrect.setChoice(choice);
                    choiceCorrect.setIsRealCorrect(correctChoiceTexts.contains(choice.getChoiceText()) ? 1 : 0);
                    choiceCorrects.add(choiceCorrect);
                });
                break;
            }
            case MC: {
                choiceList.setIsSelectedCorrected(false);
                userChoice.getChoices().forEach(choice -> {
                    ChoiceCorrect choiceCorrect = new ChoiceCorrect();
                    choiceCorrect.setChoice(choice);
                    Integer isRealCorrect = choiceService.findIsCorrectedById(choice.getId());
                    choiceCorrect.setIsRealCorrect(isRealCorrect);
                    if (choice.getIsCorrected() == isRealCorrect && isRealCorrect == 1) {
                        choiceList.setIsSelectedCorrected(true);
                    }
                    choiceCorrects.add(choiceCorrect);
                });
                break;
            }
            case MS: {
                choiceList.setIsSelectedCorrected(true);
                userChoice.getChoices().forEach(choice -> {
                    ChoiceCorrect choiceCorrect = new ChoiceCorrect();
                    choiceCorrect.setChoice(choice);
                    Integer isRealCorrect = choiceService.findIsCorrectedById(choice.getId());
                    choiceCorrect.setIsRealCorrect(isRealCorrect);
                    if (choice.getIsCorrected() == 0 && isRealCorrect == 1) {
                        choiceList.setIsSelectedCorrected(false);
                    }
                    choiceCorrects.add(choiceCorrect);
                });
                break;
            }
            case SA: {
                String userAnswer = userChoice.getChoices().get(0).getChoiceText();
                String correctAnswer = question.getChoices().get(0).getChoiceText();
                boolean isCorrect = userAnswer != null && userAnswer.trim().equals(correctAnswer.trim());
                choiceList.setIsSelectedCorrected(isCorrect);
                // Đáp án thực tế cho review kết quả
                ChoiceCorrect choiceCorrect = new ChoiceCorrect();
                choiceCorrect.setChoice(userChoice.getChoices().get(0));
                choiceCorrect.setIsRealCorrect(isCorrect ? 1 : 0);
                choiceCorrects.add(choiceCorrect);
                break;
            }
        }
        choiceList.setChoices(choiceCorrects);
        choiceLists.add(choiceList);
    });
    return choiceLists;
}


    @Override
    public List<Exam> findByIntakeId(Long intakeId) {
        return examRepository.findByIntake_Id(intakeId);
    }
    @Override
    public void deleteManyByIds(List<Long> examIds) {
        // 1. Xóa tất cả ExamUser có exam_id nằm trong danh sách
        for (Long examId : examIds) {
            List<ExamUser> relatedExamUsers = examUserRepository.findAllByExam_Id(examId);
            if (!relatedExamUsers.isEmpty()) {
                examUserRepository.deleteAll(relatedExamUsers);
            }
        }
        // 2. Xóa luôn các Exam tương ứng
        examRepository.deleteAllById(examIds); // hoặc .deleteAllByIdInBatch(examIds) nếu JPA 2.4+
    }
    
    @Override
    public List<Exam> findByPartId(Long partId) {
        return examRepository.findByPart_Id(partId);
    }

}
