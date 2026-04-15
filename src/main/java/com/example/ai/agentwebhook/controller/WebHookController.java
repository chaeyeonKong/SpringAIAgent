package com.example.ai.agentwebhook.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WebHookController {

    @PostMapping("/webhook")
    public void handleGithubEvent(@RequestBody Map<String, Object> payload) {
        System.out.println("payload = " + payload);

    }

}
