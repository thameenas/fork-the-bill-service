package com.forkthebill.service.services;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private static final String PROMPT = """
            Analyze this restaurant bill image and extract the following information in JSON format:
            {
                "subtotal": "Subtotal amount before tax and tip/serviceCharge",
                "tax": "Tax amount",
                "serviceCharge": "Tip amount or service charge (if any)",
                "totalAmount": "Total amount paid",
                "restaurantName": "Name of the restaurant (if visible)",
                "date": "Date of the bill (if visible)",
                "items": [
                    {
                        "name": "Item name",
                        "price": "Total Item amount for all quantity",
                        "quantity": "Quantity (if visible, otherwise 1)"
                    }
                ]
            }
            
            Important guidelines:
            - Extract only numerical values for amounts (no currency symbols)
            - If any amount is not visible, use 0.00
            - For items, try to extract individual line items with their prices
            - Ensure all amounts are in decimal format (e.g., 12.50 not 12,50)
            - Return only valid JSON, no additional text
            - Some bills might call tip as service charge
            """;

    public String getGeminiResponse(byte[] imageData) {
        // The client automatically uses the GOOGLE_API_KEY environment variable.
        try {
            Client client = new Client();
            Part imagePart = Part.fromBytes(imageData, "image/jpeg");
            Part textPart = Part.fromText(PROMPT);

            // Specify the model and the prompt.
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    Content.fromParts(textPart, imagePart),
                    null); // The last parameter is for custom options, null for default.

            // Get the generated text from the response.
            String text = Objects.requireNonNull(response.text()).trim()
                    .replaceFirst("```json", "")
                    .replaceFirst("```$", "");

            System.out.println("Gemini response: " + text);
            return text;
        } catch (Exception e) {
            System.out.println("Error calling gemini: " + e.getMessage());
            return "";
        }
    }

}
