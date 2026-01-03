package com.always.service;

import com.always.config.OpenAIConfig;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenAIService {

    private final OpenAIConfig openAIConfig;
    private OpenAiService openAiService;

    @Autowired
    public OpenAIService(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
        initializeService();
    }

    private void initializeService() {
        String apiKey = openAIConfig.getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            this.openAiService = new OpenAiService(apiKey);
        }
    }

    public String generateCompletion(String prompt) {
        if (openAiService == null) {
            throw new IllegalStateException("OpenAI API key가 설정되지 않았습니다. application.properties에 openai.api.key를 설정해주세요.");
        }

        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("gpt-3.5-turbo-instruct")
                .prompt(prompt)
                .maxTokens(500)
                .temperature(0.7)
                .build();

        CompletionResult completion = openAiService.createCompletion(completionRequest);
        return completion.getChoices().get(0).getText().trim();
    }

    public String generateCompletion(String prompt, Integer maxTokens, Double temperature) {
        if (openAiService == null) {
            throw new IllegalStateException("OpenAI API key가 설정되지 않았습니다. application.properties에 openai.api.key를 설정해주세요.");
        }

        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("gpt-3.5-turbo-instruct")
                .prompt(prompt)
                .maxTokens(maxTokens != null ? maxTokens : 500)
                .temperature(temperature != null ? temperature : 0.7)
                .build();

        CompletionResult completion = openAiService.createCompletion(completionRequest);
        return completion.getChoices().get(0).getText().trim();
    }
}
