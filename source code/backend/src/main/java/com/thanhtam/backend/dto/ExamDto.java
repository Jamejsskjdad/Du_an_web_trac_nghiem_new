package com.thanhtam.backend.dto;

import java.util.List;

import com.thanhtam.backend.entity.Exam;

import lombok.Data;

@Data
public class ExamDto {
    private Exam exam;
    private List<ExamQuestionPoint> examQuestionPoints;
    private int maxPoint;
}
