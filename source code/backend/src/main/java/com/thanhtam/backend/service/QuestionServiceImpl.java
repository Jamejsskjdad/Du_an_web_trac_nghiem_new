package com.thanhtam.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.AnswerSheet;
import com.thanhtam.backend.dto.ExamQuestionPoint;
import com.thanhtam.backend.entity.Choice;
import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.ExamUser;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.entity.QuestionType;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
import com.thanhtam.backend.repository.QuestionRepository;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QuestionServiceImpl implements QuestionService {
    Logger logger = LoggerFactory.getLogger(QuestionServiceImpl.class);
    private QuestionRepository questionRepository;

    @Autowired
    public QuestionServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    @Override
    public List<Question> getQuestionByPart(Part part) {
        return questionRepository.findByPart(part);
    }

    @Override
    public List<Question> getQuestionByQuestionType(QuestionType questionType) {
        return questionRepository.findByQuestionType(questionType);
    }

    @Override
    public List<Question> getQuestionPointList(List<ExamQuestionPoint> examQuestionPoints) {
        List<Question> questions = new ArrayList<>();
        examQuestionPoints.forEach(examQuestionPoint -> {
            Optional<Question> question = questionRepository.findById(examQuestionPoint.getQuestionId());
            questions.add(question.get());
        });
        return questions;
    }

    @Override
    public List<AnswerSheet> convertFromQuestionList(List<Question> questionList) {
        List<AnswerSheet> answerSheets = new ArrayList<>();
        questionList.forEach(question -> {
            List<Choice> choices = question.getChoices();
            choices.stream().map(choice -> {
                choice.setIsCorrected(0);
                return choice;
            }).collect(Collectors.toList());
            AnswerSheet answerSheet = new AnswerSheet(question.getId(), question.getChoices(), question.getPoint());
            answerSheets.add(answerSheet);
        });
        return answerSheets;
    }

    @Override
    public List<Question> getQuestionList() {
        return questionRepository.findAll();
    }

    @Override
    public Page<Question> findQuestionsByPart(Pageable pageable, Part part) {
        return questionRepository.findQuestionsByPart(pageable, part);
    }

    @Override
    public Page<Question> findQuestionsByPartAndDeletedFalse(Pageable pageable, Part part) {
        return questionRepository.findQuestionsByPartAndDeletedFalse(pageable, part);
    }

    @Override
    public Page<Question> findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(Pageable pageable, Long partId, String username) {
        return questionRepository.findQuestionsByPart_IdAndCreatedBy_UsernameAndDeletedFalse(pageable, partId, username);
    }

    @Override
    public Page<Question> findAllQuestions(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    @Override
    public String findQuestionTextById(Long questionId) {
        return questionRepository.findQuestionTextById(questionId);
    }

    @Override
    public Page<Question> findQuestionsByPart_IdAndCreatedBy_Username(Pageable pageable, Long partId, String username) {
        return questionRepository.findQuestionsByPart_IdAndCreatedBy_Username(pageable, partId, username);
    }

    @Override
    public Page<Question> findQuestionsByCreatedBy_Username(Pageable pageable, String username) {
        return questionRepository.findQuestionsByCreatedBy_Username(pageable, username);
    }

    @Override
    public void save(Question question) {
        questionRepository.save(question);
    }

    @Override
    public void update(Question question) {
        Optional<Question> oldQuestionOpt = questionRepository.findById(question.getId());
        if (!oldQuestionOpt.isPresent()) return;
    
        Question oldQuestion = oldQuestionOpt.get();
    
        oldQuestion.setQuestionText(question.getQuestionText());
        oldQuestion.setQuestionType(question.getQuestionType());
        oldQuestion.setPart(question.getPart());
        oldQuestion.setPoint(question.getPoint());
    
        // Kiểm tra nếu là câu hỏi SA (Short Answer)
        if ("SA".equals(question.getQuestionType().getTypeCode().name())) {
            // Xóa toàn bộ đáp án cũ, chỉ giữ lại 1 đáp án mới
            oldQuestion.getChoices().clear();
            if (question.getChoices() != null && !question.getChoices().isEmpty()) {
                // Nếu entity Choice có setQuestion thì gán lại ở đây!
                question.getChoices().forEach(c -> {
                    c.setId(null); // để force insert mới, tránh update nhầm choice cũ
                    // Nếu Choice có setQuestion thì thêm: c.setQuestion(oldQuestion);
                });
                oldQuestion.setChoices(question.getChoices());
            }
        } else {
            // Các loại khác (MC, MS, TF)
            oldQuestion.setChoices(question.getChoices());
        }
    
        questionRepository.save(oldQuestion);
    }
    @Override
    public void updateQuestion(Long id, Question updatedQuestion) {
        Optional<Question> questionOpt = questionRepository.findById(id);
        if (!questionOpt.isPresent()) return;
        Question q = questionOpt.get();

        // Update các trường đơn giản
        q.setQuestionText(updatedQuestion.getQuestionText());
        q.setQuestionType(updatedQuestion.getQuestionType());
        q.setPart(updatedQuestion.getPart());
        q.setPoint(updatedQuestion.getPoint());

        // ---- XỬ LÝ CHOICE: XÓA DANH SÁCH CŨ, GÁN DANH SÁCH MỚI ----
        q.getChoices().clear(); // Xóa hết choice cũ (sẽ được orphanRemoval=true xóa ở DB)
        // Gán lại choices mới (nên tạo mới Choice entity nếu là thêm)
        for (Choice c : updatedQuestion.getChoices()) {
            // reset id = null để Hibernate biết đây là choice mới, hoặc giữ id cũ để update
            if (c.getId() == null || c.getId() == 0) c.setId(null);
            // Nếu có logic gì nữa thì xử lý ở đây
            q.getChoices().add(c);
        }

        questionRepository.save(q);
        updateExamPointWhenQuestionChanged(q.getId(), q.getPart().getId(), q.getPoint());
    }

    @Override
    public void delete(Long id) {
        questionRepository.deleteById(id);
    }
    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamUserRepository examUserRepository;

    @Override
    public void deleteQuestionsAndUpdateRelatedData(List<Long> questionIds) {
    List<Question> questionsToDelete = questionRepository.findAllById(questionIds);

        // Lấy tất cả partId liên quan
        Set<Long> partIds = questionsToDelete.stream()
                .map(q -> q.getPart().getId())
                .collect(Collectors.toSet());

        // 1. Xóa question
        questionRepository.deleteAll(questionsToDelete);

        // 2. Update exam.question_data
        List<Exam> affectedExams = examRepository.findExamsByPartIds(partIds);
        for (Exam exam : affectedExams) {
            List<Map<String, Object>> questionData = parseJsonArray(exam.getQuestionData());
            questionData.removeIf(q -> questionIds.contains(((Number) q.get("questionId")).longValue()));
            exam.setQuestionData(toJson(questionData));
            examRepository.save(exam);
        }

        // 3. Update exam_user.answer_sheet
        List<ExamUser> affectedExamUsers = examUserRepository.findByExam_Part_IdIn(partIds);
        for (ExamUser examUser : affectedExamUsers) {
            List<Map<String, Object>> sheet = parseJsonArray(examUser.getAnswerSheet());
            sheet.removeIf(q -> questionIds.contains(((Number) q.get("questionId")).longValue()));
            examUser.setAnswerSheet(toJson(sheet));
            examUserRepository.save(examUser);
        }
    }
    private List<Map<String, Object>> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String toJson(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }
    @Transactional
    public void updateExamPointWhenQuestionChanged(Long questionId, Long partId, int newPoint) {
        System.out.println("▶ [SYNC] Đang đồng bộ câu hỏi ID: " + questionId + ", partId: " + partId + ", point = " + newPoint);
        String qidStr = "\"questionId\":" + questionId;
        List<Exam> exams = examRepository.findExamsContainingQuestionId(qidStr);
    
        for (Exam exam : exams) {
            List<Map<String, Object>> questionData = parseJsonArray(exam.getQuestionData());
            boolean updated = false;
            for (Map<String, Object> qd : questionData) {
                Long qId = ((Number) qd.get("questionId")).longValue();
                if (Objects.equals(qId, questionId)) {
                    qd.put("point", newPoint);
                    updated = true;
                }
            }
            if (updated) {
                exam.setQuestionData(toJson(questionData));
                examRepository.saveAndFlush(exam);
            }
    
            // Cập nhật answer_sheet
            List<ExamUser> examUsers = examUserRepository.findAllByExam_Id(exam.getId());
            for (ExamUser eu : examUsers) {
                List<Map<String, Object>> answerSheet = parseJsonArray(eu.getAnswerSheet());
                boolean changed = false;
                for (Map<String, Object> ans : answerSheet) {
                    Long qId = ((Number) ans.get("questionId")).longValue();
                    if (Objects.equals(qId, questionId)) {
                        ans.put("point", newPoint);
                        changed = true;
                    }
                }
                if (changed) {
                    eu.setAnswerSheet(toJson(answerSheet));
                    examUserRepository.saveAndFlush(eu);
                }
            }
        }
    }
}
