package com.example.IMS.chatbot.service;

import com.example.IMS.chatbot.model.FunctionDeclaration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class GeminiChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Autowired
    private List<FunctionDeclaration> chatbotTools;

    @Autowired
    private ChatbotDatabaseService databaseService;

    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String chat(String userMessage) {
        try {
            // Check if API key is valid
            if (apiKey == null || apiKey.equals("your_api_key_here")) {
                return "⚠️ Chatbot is not configured. Please set a valid Gemini API key.";
            }
            
            List<Map<String, Object>> contents = new ArrayList<>();
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", userMessage))
            ));

            int maxIterations = 5;
            for (int i = 0; i < maxIterations; i++) {
                JsonObject response = callGeminiApi(contents);
                
                // Check if response is null or empty
                if (response == null) {
                    return "Error: Received null response from Gemini API. Please check your API key.";
                }
                
                // Check if candidates array exists
                if (!response.has("candidates") || response.getAsJsonArray("candidates").size() == 0) {
                    System.err.println("API Response: " + response.toString());
                    return "Error: No response candidates from API. The API key might be invalid or the request was blocked.";
                }

                JsonArray candidates = response.getAsJsonArray("candidates");
                JsonObject candidate = candidates.get(0).getAsJsonObject();
                
                // Check if content exists
                if (!candidate.has("content")) {
                    System.err.println("Candidate: " + candidate.toString());
                    return "Error: No content in API response. Please try again.";
                }
                
                JsonObject content = candidate.getAsJsonObject("content");
                
                // Check if parts array exists
                if (!content.has("parts") || content.getAsJsonArray("parts").size() == 0) {
                    return "Error: No parts in response content.";
                }
                
                JsonArray parts = content.getAsJsonArray("parts");
                JsonObject firstPart = parts.get(0).getAsJsonObject();

                if (firstPart.has("functionCall")) {
                    JsonObject functionCall = firstPart.getAsJsonObject("functionCall");
                    String functionName = functionCall.get("name").getAsString();
                    Map<String, Object> args = functionCall.has("args") ?
                            gson.fromJson(functionCall.getAsJsonObject("args"), Map.class) :
                            new HashMap<>();

                    String result = databaseService.executeFunction(functionName, args);

                    contents.add(Map.of(
                            "role", "model",
                            "parts", List.of(Map.of("functionCall", gson.fromJson(gson.toJson(functionCall), Map.class)))
                    ));

                    contents.add(Map.of(
                            "role", "user",
                            "parts", List.of(Map.of(
                                    "functionResponse", Map.of(
                                            "name", functionName,
                                            "response", Map.of("content", result)
                                    )
                            ))
                    ));

                } else if (firstPart.has("text")) {
                    return firstPart.get("text").getAsString();
                }
            }

            return "Sorry, I couldn't complete that request.";

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage();
            
            // Handle quota exceeded error
            if (errorMsg != null && errorMsg.contains("429")) {
                return "⚠️ Chatbot quota exceeded. The free tier limit has been reached. Please try again later or upgrade your API plan at https://ai.google.dev/pricing";
            }
            
            // Handle other API errors
            if (errorMsg != null && errorMsg.contains("API returned status")) {
                return "⚠️ API Error: " + errorMsg.substring(0, Math.min(200, errorMsg.length())) + "...";
            }
            
            return "❌ Error: " + errorMsg;
        }
    }

    private JsonObject callGeminiApi(List<Map<String, Object>> contents) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "contents", contents,
                "tools", List.of(Map.of("functionDeclarations", chatbotTools)),
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of(
                                "text", "You are a helpful inventory assistant. Use the provided functions to get real data from the database when users ask about inventory, vendors, borrowers, loans, or stock levels."
                        ))
                )
        );

        String jsonBody = gson.toJson(requestBody);
        String urlWithKey = apiUrl + "?key=" + apiKey;

        System.out.println("Calling Gemini API...");
        System.out.println("URL: " + apiUrl);
        System.out.println("API Key starts with: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) : "null") + "...");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.body());

        if (response.statusCode() != 200) {
            throw new Exception("API returned status " + response.statusCode() + ": " + response.body());
        }

        return gson.fromJson(response.body(), JsonObject.class);
    }
}
