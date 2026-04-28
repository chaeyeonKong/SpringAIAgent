package com.example.ai.agentwebhook.agent;

import com.example.ai.agentwebhook.tools.ScoreTools;
import org.apache.catalina.User;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

//엄격한 기준표에 따라 채점하고, ScoreTools를 사용하여 DB에 저장
public class GradingAgent {

    private final ChatClient chatClient; //Spring AI 라이브러리의 ChatClient
    private final ScoreTools scoreTools;

    // 외부 파일에서 프롬프트 로딩
    @Value("classpath:prompts/grading-rubric.txt")
    private Resource rubricResource;


    @Value("classpath:prompts/system-message.st")
    private Resource systemPromptResource;

    private static final String USER_PROMPT_TEXT = """
            [메타 정보]
            - 학생: {studentName}
            - 레포지토리: {repoName}
            - PR 번호: {prNumber}
            
            [모범 답안 (Reference Code)]
            - 아래 코드는 교수님이 작성한 정답입니다. 학생의 코드가 이 로직과 일치하는지 확인하세요.
            {referenceCode}
            
            [학생의 제출 코드 변경사항 (Diff)]
            주의: Diff 형식에서 `-`로 시작하는 줄은 삭제된 코드(과거)이고, `+`로 시작하는 줄이 학생이 작성한 코드(현재)입니다.
            **반드시 `-` 라인은 무시하고, `+` 라인만 보고 평가하세요.**
            
            {diff}
            """;


    public GradingAgent(ChatClient.Builder chatClient, ScoreTools scoreTools) {
        this.chatClient = chatClient.build();
        this.scoreTools = scoreTools;
    }

    public String gradeAndSave(String diff, String solutionCode, int prNumber, String studentName, String repoName) {
        // 리소스 파일 읽어오기(채점기준표)
        String detailedRubric = loadResourceToString(rubricResource);

        //시스템 메시지 생성(채점 기준표 map에 주입)
        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(systemPromptResource);
        Message systemMessage = systemTemplate.createMessage(Map.of("detailedRubric",detailedRubric));

        //유저 메시지 생성
        PromptTemplate userTemplate = new PromptTemplate(USER_PROMPT_TEXT);
        Message userMessage = userTemplate.createMessage(Map.of(
                "studentName",studentName,
                "repoName",repoName,
                "prNumber",prNumber,
                "diff",diff,
                "referenceCode",solutionCode
        ));

        // AI 호출 (TOOL 활성화 - > 점수 DB 저장 -> 결과 반환)
        return chatClient.prompt()
                .messages(systemMessage, userMessage)
                .tools(scoreTools)
                .call()
                .content();
    }

    // 리소스를 string으로 변경하는 헬퍼 메소드
    private String loadResourceToString(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("프롬프트 파일을 읽을 수 없습니다: " + resource.getFilename(), e);
        }
    }


}
