package com.example.ai.agentwebhook.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class GithubService {
    private final RestClient restClient;

    //Github Api 전용 RestClient 설정
    public GithubService(@Value("${github.token}") String token) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    // PR의 변경된 코드(Diff) 가져오기
    public String getPrDiff(String owner, String repo, int prNumber) {
        return restClient.get().uri("/repos/{owner}/{repo}/pulls/{prNumber}", owner, repo, prNumber)
                .header("Accept", "application/vnd.github.v3.diff") //diff 내역을 받을 것을 명시
                .retrieve()
                .body(String.class);
    }

    // PR에 댓글달기 (이슈로 다는 것. github는 풀리퀘를 코드가 포함된 이슈라고 생각한다.)
    public void commentOnPr(String owner, String repo, int prNumber, String comment){
        restClient.post()
                .uri("/repos/{owner}/{repo}/issues/{prNumber}/comments",owner, repo, prNumber)
                .header("Accept","application/vnd.github+json")
                .body(Map.of("body",comment)) //github가 원하는 json 포맷으로 -> body: "내용"
                .retrieve()
                .toBodilessEntity(); //결과 내용은 버리고 성공여부만 확인
    }


}
