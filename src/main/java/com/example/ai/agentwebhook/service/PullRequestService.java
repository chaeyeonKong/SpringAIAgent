package com.example.ai.agentwebhook.service;

import com.example.ai.agentwebhook.agent.CodeReviewParallelWorkflow;
import org.springframework.stereotype.Service;

//Github에서 정보를 가져와 워크플로우를 실행하고, 결과를 다시 github에 리뷰 등록합니다.
@Service
public class PullRequestService {

    private final GithubService githubService;
    private final CodeReviewParallelWorkflow workflow;


    public PullRequestService(GithubService githubService, CodeReviewParallelWorkflow workflow) {
        this.githubService = githubService;
        this.workflow = workflow;
    }
    // 레포주인(교수님 id), 과제명, pr번호, 학생id, 정답코드 (controller or db에서 받아옴)
    public void processPullRequest(String repoOwner, String repoName, int prNumber, String studentName, String solutionCode){
        //github api 호출(변경된 코드 가져오기)
        String diff = githubService.getPrDiff(repoOwner, repoName, prNumber);
        // 병렬 워크플로우 실행(AI 에이전트들에게 일 시키기)
        String finalComment = workflow.execute(diff, solutionCode, prNumber, studentName, repoName);
        // github에게 댓글 달기(최종 리뷰 등록)
        githubService.commentOnPr(repoOwner, repoName, prNumber, finalComment);
        System.out.println("Service 모든 처리가 완료되었습니다.");

    }
}
