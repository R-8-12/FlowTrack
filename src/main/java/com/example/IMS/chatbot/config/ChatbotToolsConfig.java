package com.example.IMS.chatbot.config;

import com.example.IMS.chatbot.model.FunctionDeclaration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.*;

@Configuration
public class ChatbotToolsConfig {

    @Bean
    public List<FunctionDeclaration> chatbotTools() {
        List<FunctionDeclaration> tools = new ArrayList<>();

        // Tool: Get all inventory items
        tools.add(new FunctionDeclaration(
                "getAllInventoryItems",
                "Retrieves all inventory items in the system. Use when user asks about inventory, items, stock, or products.",
                Map.of(
                        "type", "object",
                        "properties", Map.of()
                )
        ));

        // Tool: Get all vendors
        tools.add(new FunctionDeclaration(
                "getAllVendors",
                "Retrieves all vendors. Use when user asks about vendors or suppliers.",
                Map.of(
                        "type", "object",
                        "properties", Map.of()
                )
        ));

        // Tool: Get all borrowers
        tools.add(new FunctionDeclaration(
                "getAllBorrowers",
                "Retrieves all borrowers. Use when user asks about borrowers or users.",
                Map.of(
                        "type", "object",
                        "properties", Map.of()
                )
        ));

        // Tool: Get all loans
        tools.add(new FunctionDeclaration(
                "getAllLoans",
                "Retrieves all loans. Use when user asks about loans or borrowed items.",
                Map.of(
                        "type", "object",
                        "properties", Map.of()
                )
        ));

        // Tool: Get item by ID
        tools.add(new FunctionDeclaration(
                "getItemById",
                "Retrieves a specific item by its ID. Use when user asks about a specific item.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "itemId", Map.of(
                                        "type", "number",
                                        "description", "The ID of the item to retrieve"
                                )
                        ),
                        "required", List.of("itemId")
                )
        ));

        // Tool: Get low stock items
        tools.add(new FunctionDeclaration(
                "getLowStockItems",
                "Retrieves items with low stock levels. Use when user asks about low stock, items running out, or inventory alerts.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "threshold", Map.of(
                                        "type", "number",
                                        "description", "The quantity threshold below which items are considered low stock (default: 10)"
                                )
                        )
                )
        ));

        return tools;
    }
}
