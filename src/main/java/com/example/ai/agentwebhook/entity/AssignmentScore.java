package com.example.ai.agentwebhook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class AssignmentScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentName; //학생의 github id

    private String repoName; // 과제 레포지토리 이름

    private int prNumber; //pr번호

    private int score; //과제 점수

    @Column(length=1000)
    private String feedback; //LLM이 만드는 피드백이 들어갈 것.

    private LocalDateTime gradeAt; //생성자에서 now()로 초기화 함.

    public AssignmentScore(String studentName, String repoName, int prNumber, int score, String feedback) {
        this.studentName = studentName;
        this.repoName = repoName;
        this.prNumber = prNumber;
        this.score = score;
        this.feedback = feedback;
        this.gradeAt = LocalDateTime.now();
    }
}
