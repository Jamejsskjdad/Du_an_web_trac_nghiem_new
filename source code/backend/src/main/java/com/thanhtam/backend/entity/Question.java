package com.thanhtam.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thanhtam.backend.audit.Auditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question")
public class Question extends Auditable<Long> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "question_text", columnDefinition = "text")
    private String questionText;

    @Column(name="point")
    private int point;

    @Column(name = "deleted")
    private boolean deleted = false;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "question_type_id")
    private QuestionType questionType;

    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "question_id")
    private List<Choice> choices;

    @ManyToOne()
    @JoinColumn(name = "part_id")
    private Part part;
}
