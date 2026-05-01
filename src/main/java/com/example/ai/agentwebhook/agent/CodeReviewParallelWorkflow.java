package com.example.ai.agentwebhook.agent;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class CodeReviewParallelWorkflow {

    private final ReviewAgent reviewAgent;
    private final GradingAgent gradingAgent;

    public CodeReviewParallelWorkflow(ReviewAgent reviewAgent, GradingAgent gradingAgent) {
        this.reviewAgent = reviewAgent;
        this.gradingAgent = gradingAgent;
    }

    public String execute(String diff, String solutionCode, int prNumber, String studentName, String repoName){
        long startTime = System.currentTimeMillis(); //성능 측정 시작
        //thread 만들 것. 비동기 - 리뷰 에이전트 실행
        CompletableFuture<String> reviewFuture = CompletableFuture.supplyAsync(()->{
            System.out.println("Async 리뷰 에이전트가 분석을 시작했습니다...");
            return reviewAgent.generateFeedback(diff, solutionCode);
        });
        // 비동기 - 채점 에이전트 실행 - 또 다른 스레드에서 수행
        CompletableFuture<String> gradingFuture = CompletableFuture.supplyAsync(()->{
            System.out.println("Async 채점 에이전트가 채점중입니다 ....");
            return gradingAgent.gradeAndSave(diff, solutionCode, prNumber, studentName, repoName);
        });

        CompletableFuture.allOf(reviewFuture, gradingFuture).join();

        String reviewResult = reviewFuture.join();
        String gradingLog = gradingFuture.join();

        long endTime = System.currentTimeMillis();
        System.out.println("전체 처리 시간" + (endTime - startTime)+"ms");
        System.out.println("system log" + gradingLog);

        return String.format("""
                ## AI 코드 리뷰!
                %s
                """,reviewResult);

    }



}
