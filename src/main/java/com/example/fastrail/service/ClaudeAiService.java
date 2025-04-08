package com.example.fastrail.service;


import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ClaudeAiService {

    private final AnthropicChatModel chatModel;

    @Autowired
    public ClaudeAiService(AnthropicChatModel anthropicChatModel){
        this.chatModel = anthropicChatModel;
    }

    public String generateResponse(String userInput){
        UserMessage userMessage = new UserMessage(userInput);
        ChatResponse response = chatModel.call(new Prompt(List.of(userMessage)));
        return response.getResult().getOutput().getContent();
    }

    public String generateResponseWithSystemPrompt(String userInput){
        SystemMessage systemMessage = new SystemMessage("你是一位專業的AI助手，請用繁體中文回答問題。");
        UserMessage userMessage = new UserMessage(userInput);
        ChatResponse response = chatModel.call(new Prompt(List.of(systemMessage, userMessage)));
        return response.getResult().getOutput().getContent();
    }

    public String generateResponseWithTemplatedPrompt(String userInput, String language) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(
                "你是一位專業的AI助手，請用{language}回答問題。"
        );

        Message systemMessage = systemPromptTemplate.createMessage(Map.of("language", language));
        UserMessage userMessage = new UserMessage(userInput);
        ChatResponse response = chatModel.call(new Prompt(List.of(systemMessage, userMessage)));
        return response.getResult().getOutput().getContent();
    }
}
