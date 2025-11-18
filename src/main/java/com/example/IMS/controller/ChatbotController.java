package com.example.IMS.controller;

import com.example.IMS.chatbot.service.GeminiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    @Autowired
    private GeminiChatService geminiChatService;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String response = geminiChatService.chat(request.getMessage());
        return new ChatResponse(response);
    }

    @GetMapping("/test")
    public String test() {
        return "✅ Chatbot API is working!";
    }

    public static class ChatRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ChatResponse {
        private String response;

        public ChatResponse(String response) {
            this.response = response;
        }

        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }
    }
}
