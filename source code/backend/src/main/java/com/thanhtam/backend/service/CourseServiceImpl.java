package com.thanhtam.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thanhtam.backend.entity.Course;
import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.Part;
import com.thanhtam.backend.entity.Question;
import com.thanhtam.backend.repository.CourseRepository;
import com.thanhtam.backend.repository.PartRepository;
import com.thanhtam.backend.repository.QuestionRepository;

@Service
public class CourseServiceImpl implements CourseService {
    private CourseRepository courseRepository;

    @Autowired
    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }


    @Override
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public List<Course> getCourseList() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> getCourseListByPage(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    public void saveCourse(Course course) {
        courseRepository.save(course);
    }

    @Override
    public void delete(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return courseRepository.existsByCourseCode(code);
    }

    @Override
    public boolean existsById(Long id) {
        return courseRepository.existsById(id);
    }
    @Override
    @Transactional(readOnly = true)
    public List<Course> findAllByIntakeId(Long intakeId) {
        return courseRepository.findAllByIntakes_Id(intakeId);
    }

    @Override
    public Course findCourseByPartId(Long partId) {
        return courseRepository.findCourseByPartId(partId);
    }
    @Autowired
    private PartRepository partRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ExamService examService; // đã có sẵn

    @Override
    @Transactional // nhớ thêm để đảm bảo atomic
    public void deleteManyByIds(List<Long> courseIds) {
        for (Long courseId : courseIds) {
            // Lấy tất cả part thuộc course
            List<Part> parts = partRepository.findAllByCourseId(courseId);

            for (Part part : parts) {
                Long partId = part.getId();

                // 1. Lấy tất cả exam thuộc part, xóa qua service đã có
                List<Exam> exams = examService.findByPartId(partId);
                if (!exams.isEmpty()) {
                    List<Long> examIds = exams.stream().map(Exam::getId).collect(Collectors.toList());
                    examService.deleteManyByIds(examIds);
                }

                // 2. Xóa toàn bộ question theo part
                List<Question> questions = questionRepository.findAllByPartId(partId);

                if (!questions.isEmpty()) {
                    questionRepository.deleteAll(questions);
                }

                // 3. Xóa part
                partRepository.deleteById(partId);
            }
            // 4. Xóa course
            courseRepository.deleteById(courseId);
        }
    }

}
