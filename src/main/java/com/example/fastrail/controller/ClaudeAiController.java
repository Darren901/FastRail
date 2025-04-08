package com.example.fastrail.controller;

import com.example.fastrail.service.ClaudeAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claude")
public class ClaudeAiController {

    private final ClaudeAiService claudeAiService;

    @Autowired
    public ClaudeAiController(ClaudeAiService claudeAiService){
        this.claudeAiService = claudeAiService;
    }

    @GetMapping("/chat")
    public String chat(@RequestBody String message){
        return claudeAiService.generateResponse(message);
    }

    @GetMapping("/chat/system-prompt")
    public String chatWithSystemPrompt(@RequestBody String message){
        return claudeAiService.generateResponseWithSystemPrompt(message);
    }

    @GetMapping("/chat/{language}")
    public String chatWithLanguage(@RequestBody String message, @PathVariable String language){
        return claudeAiService.generateResponseWithTemplatedPrompt(message, language);
    }
}
