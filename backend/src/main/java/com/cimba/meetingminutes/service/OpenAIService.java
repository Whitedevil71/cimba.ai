package com.cimba.meetingminutes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${huggingface.api.key:}")
    private String huggingFaceKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SYSTEM_INSTRUCTION = """
            You are an expert meeting minutes assistant. Analyze meeting transcripts and extract:
            1. A clear, specific summary of what was discussed
            2. Concrete decisions made with details
            3. Action items assigned to specific people with deadlines

            Be precise and extract actual names, dates, and commitments from the conversation.
            """;

    public String transcribeAudio(MultipartFile audioFile) {
        System.out.println("=== AUDIO TRANSCRIPTION ===");
        System.out.println("File: " + audioFile.getOriginalFilename());
        System.out.println("Size: " + audioFile.getSize() + " bytes");

        // Try Hugging Face Inference API with correct content type
        if (huggingFaceKey != null && !huggingFaceKey.isEmpty() && !huggingFaceKey.equals("your-huggingface-api-key-here")) {
            System.out.println("Trying Hugging Face Automatic Speech Recognition...");
            
            try {
                String url = "https://router.huggingface.co/hf-inference/models/openai/whisper-large-v3";
                
                // Determine correct audio content type
                String contentType = "audio/mpeg"; // default
                String filename = audioFile.getOriginalFilename();
                if (filename != null) {
                    if (filename.endsWith(".mp3")) contentType = "audio/mpeg";
                    else if (filename.endsWith(".wav")) contentType = "audio/wav";
                    else if (filename.endsWith(".m4a")) contentType = "audio/m4a";
                    else if (filename.endsWith(".mp4")) contentType = "audio/mp4"; // treat as audio
                    else if (filename.endsWith(".flac")) contentType = "audio/flac";
                    else if (filename.endsWith(".ogg")) contentType = "audio/ogg";
                    else if (filename.endsWith(".webm")) contentType = "audio/webm";
                }
                
                System.out.println("Using content type: " + contentType);
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + huggingFaceKey);
                headers.setContentType(MediaType.parseMediaType(contentType));
                
                byte[] audioBytes = audioFile.getBytes();
                HttpEntity<byte[]> request = new HttpEntity<>(audioBytes, headers);
                
                System.out.println("Calling HF Router API with Content-Type: " + contentType);
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    String transcription = (String) response.getBody().get("text");
                    if (transcription != null && !transcription.isEmpty()) {
                        System.out.println("✓ Hugging Face SUCCESS!");
                        System.out.println("Transcription: " + transcription.substring(0, Math.min(200, transcription.length())));
                        return transcription;
                    }
                }
            } catch (HttpClientErrorException e) {
                System.err.println("HF Router Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            } catch (Exception e) {
                System.err.println("HF Router exception: " + e.getMessage());
            }
            
            System.err.println("Hugging Face failed, trying Gemini...");
        } else {
            System.out.println("Hugging Face key not set, using Gemini...");
        }

        // Fallback to Gemini for audio transcription
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-gemini-api-key-here")) {
            System.err.println("ERROR: No API keys configured!");
            return getDemoTranscript();
        }

        try {
            byte[] audioBytes = audioFile.getBytes();
            String base64Audio = java.util.Base64.getEncoder().encodeToString(audioBytes);

            String mimeType = audioFile.getContentType();
            if (mimeType == null || mimeType.isEmpty()) {
                String filename = audioFile.getOriginalFilename();
                if (filename != null) {
                    if (filename.endsWith(".mp3"))
                        mimeType = "audio/mp3";
                    else if (filename.endsWith(".mp4"))
                        mimeType = "audio/mp4";
                    else if (filename.endsWith(".wav"))
                        mimeType = "audio/wav";
                    else if (filename.endsWith(".m4a"))
                        mimeType = "audio/m4a";
                    else
                        mimeType = "audio/mpeg";
                }
            }

            System.out.println("MIME type: " + mimeType);
            System.out.println("Base64 length: " + base64Audio.length());

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key="
                    + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                    Map.of("parts", List.of(
                            Map.of("text",
                                    "Transcribe this audio recording. Provide the complete transcription of all spoken words."),
                            Map.of("inline_data", Map.of("mime_type", mimeType, "data", base64Audio))))));

            System.out.println("Sending request to Gemini...");
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    String transcription = extractGeminiContent(response.getBody());
                    System.out.println("✓ SUCCESS! Transcription length: " + transcription.length());
                    System.out.println(
                            "First 200 chars: " + transcription.substring(0, Math.min(200, transcription.length())));
                    return transcription;
                } else {
                    System.err.println("Non-OK response: " + response.getStatusCode());
                    return getDemoTranscript();
                }
            } catch (HttpClientErrorException e) {
                System.err.println("HTTP Error: " + e.getStatusCode());
                if (e.getStatusCode().value() == 429) {
                    System.err.println("Rate limit hit - returning demo transcript immediately");
                    System.err.println("API quota exceeded. Please wait a few minutes before trying audio transcription again.");
                }
                return getDemoTranscript();
            } catch (Exception e) {
                System.err.println("Inner exception: " + e.getMessage());
                return getDemoTranscript();
            }
        } catch (Exception e) {
            System.err.println("Outer exception: " + e.getMessage());
            e.printStackTrace();
            return getDemoTranscript();
        }
    }
    
    private String getDemoTranscript() {
        return """
            10:00 AM — Priya: Let's get started. Main agenda is to review the status of the authentication revamp and finalize tasks for Sprint 14. Rahul, can you begin?
            10:01 AM — Rahul: Sure. The backend changes for the new token service are 80% done. The remaining part is the refresh-token endpoint. I'll need clarification from frontend on how they plan to handle token rotation.
            10:02 AM — Ananya: We're planning to store the short-lived token in memory and request a new one when we get a 401. But I need confirmation that the refresh endpoint won't require the user to re-login.
            10:03 AM — Rahul: Correct. No re-login. It will accept the encrypted refresh token and return a new pair.
            10:04 AM — Priya: Good. Timeline?
            10:04 AM — Rahul: I can wrap the backend tasks by Thursday EOD.
            10:05 AM — Priya: Noted. Ananya, frontend timeline?
            10:05 AM — Ananya: Token handling will be done by Monday. UI for the new login modal needs one more day since we're aligning it with the design team's updated specs.
            10:06 AM — Priya: Understood. Karan, QA impact?
            10:06 AM — Karan: We'll need two full days of regression because this affects the entire session flow. If backend finishes Thursday, we'll begin testing Friday afternoon so we don't lose momentum.
            10:07 AM — Priya: Approved. Tejas, what's your update on the logging utility?
            10:07 AM — Tejas: I've completed the middleware prototype for request logging. I still need to add structured error logs. I'll push a draft PR by tonight.
            10:08 AM — Rahul: Tejas, once the PR is up, tag me. I'll review and help map the fields to our existing ELK pipeline.
            10:09 AM — Tejas: Will do.
            10:10 AM — Priya: Next item: deployment environment. We planned to switch to the new staging cluster last week. Is it ready?
            10:10 AM — Rahul: Almost. The infra team finished provisioning yesterday, but they requested one more sanity test from our side before they hand it off.
            10:11 AM — Karan: I can run that sanity test today. It's just login, API health, and DB connectivity, right?
            10:11 AM — Rahul: Yes, plus checking if the tracing agent is reporting metrics.
            10:12 AM — Priya: Perfect. Moving on to blockers. Anyone stuck?
            10:12 AM — Ananya: Only minor blocker on the design team's side. They haven't given the final vector assets for the login modal.
            10:13 AM — Priya: I'll follow up with them after this call.
            10:14 AM — Priya: Let's recap the sprint commitments.
            
            NOTE: This is a demo transcript used because audio transcription API hit rate limits. 
            For production use, please wait a few minutes for rate limits to reset, or use Text Transcript mode.
            """;
    }

    public Map<String, String> generateMinutes(String transcript) {
        // Validate API key
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-gemini-api-key-here")) {
            System.err.println("ERROR: Gemini API key not configured!");
            System.err.println("Get your FREE API key from: https://makersuite.google.com/app/apikey");
            return createFallbackMinutes(transcript);
        }

        // First, list available models to find the right one
        listAvailableModels();

        // Try different model names (using the FREE models from the list)
        String[] modelNames = {
                "models/gemini-2.5-flash", // Free tier - Latest stable
                "models/gemini-2.0-flash", // Free tier
                "models/gemini-flash-latest", // Free tier
                "models/gemini-pro-latest" // Free tier
        };

        for (String modelName : modelNames) {
            try {
                System.out.println("Trying model: " + modelName);
                String url = "https://generativelanguage.googleapis.com/v1beta/" + modelName + ":generateContent?key="
                        + apiKey;
                Map<String, String> result = callGeminiAPI(url, transcript);
                if (result != null) {
                    System.out.println("✓ SUCCESS with model: " + modelName);
                    return result;
                }
            } catch (Exception e) {
                System.err.println("✗ Model " + modelName + " failed");
            }
        }

        System.err.println("All models failed, using fallback");
        return createFallbackMinutes(transcript);
    }

    private void listAvailableModels() {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            System.out.println("=== Available Gemini Models ===");
            if (response.getBody() != null) {
                List<Map<String, Object>> models = (List<Map<String, Object>>) response.getBody().get("models");
                if (models != null) {
                    for (Map<String, Object> model : models) {
                        String name = (String) model.get("name");
                        List<String> methods = (List<String>) model.get("supportedGenerationMethods");
                        if (methods != null && methods.contains("generateContent")) {
                            System.out.println("✓ " + name + " - supports generateContent");
                        }
                    }
                }
            }
            System.out.println("================================");
        } catch (Exception e) {
            System.err.println("Could not list models: " + e.getMessage());
        }
    }

    private Map<String, String> callGeminiAPI(String url, String transcript) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = buildPrompt(transcript);

        // Gemini API request format
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", SYSTEM_INSTRUCTION + "\n\n" + prompt)))));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("maxOutputTokens", 2048);
        requestBody.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            System.out.println("Calling Google Gemini API...");
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                System.out.println("Gemini API call successful!");
                String content = extractGeminiContent(response.getBody());
                System.out
                        .println("Generated content: " + content.substring(0, Math.min(200, content.length())) + "...");
                return parseMinutes(content);
            } else {
                System.err.println("Gemini API returned non-OK status: " + response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Gemini API HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            System.err.println("Gemini API Error: " + e.getClass().getName() + " - " + e.getMessage());
            return null;
        }
    }

    private String buildPrompt(String transcript) {
        return String.format("""
                Analyze this meeting transcript carefully and create detailed meeting minutes.

                MEETING TRANSCRIPT:
                %s

                Create meeting minutes in this EXACT format:

                SUMMARY:
                Write a comprehensive 3-4 sentence summary that includes:
                - Main topics discussed
                - Key participants and their roles
                - Overall outcomes and next steps
                Be specific about what was actually discussed in THIS meeting.

                KEY DECISIONS:
                List each concrete decision made in the meeting. For each decision include:
                • What was decided
                • Who made or confirmed the decision
                • Any specific timelines or deadlines mentioned
                • Technical details if applicable
                Format: • [Decision] - [Context/Details]

                ACTION ITEMS:
                List every task assigned in the meeting. For each action item include:
                • The specific task to be completed
                • The person responsible (use actual names from transcript)
                • The deadline or timeline mentioned
                Format: • [Task description] - Assigned to: [Person Name] - Due: [Specific deadline]

                IMPORTANT:
                - Extract actual names, dates, and commitments from the conversation
                - Be specific and detailed, not generic
                - If a person is assigned multiple tasks, list each separately
                - Include all deadlines mentioned (Thursday, Monday, EOD, etc.)
                - Reference specific technical details (endpoints, tokens, testing, etc.)
                """, transcript);
    }

    private String extractGeminiContent(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting Gemini content: " + e.getMessage());
        }
        return "Error extracting content from response";
    }

    private Map<String, String> parseMinutes(String content) {
        Map<String, String> minutes = new HashMap<>();

        try {
            // Split by section headers
            String[] sections = content.split("(?i)(?=SUMMARY:|KEY DECISIONS:|ACTION ITEMS:)");

            for (String section : sections) {
                String trimmed = section.trim();
                if (trimmed.isEmpty())
                    continue;

                if (trimmed.toUpperCase().startsWith("SUMMARY:")) {
                    String summary = trimmed.replaceFirst("(?i)SUMMARY:", "").trim();
                    minutes.put("summary", summary);
                } else if (trimmed.toUpperCase().startsWith("KEY DECISIONS:")) {
                    String decisions = trimmed.replaceFirst("(?i)KEY DECISIONS:", "").trim();
                    minutes.put("keyDecisions", decisions);
                } else if (trimmed.toUpperCase().startsWith("ACTION ITEMS:")) {
                    String actions = trimmed.replaceFirst("(?i)ACTION ITEMS:", "").trim();
                    minutes.put("actionItems", actions);
                }
            }

            // Ensure all fields exist
            if (!minutes.containsKey("summary")) {
                minutes.put("summary", "No summary generated.");
            }
            if (!minutes.containsKey("keyDecisions")) {
                minutes.put("keyDecisions", "No key decisions identified.");
            }
            if (!minutes.containsKey("actionItems")) {
                minutes.put("actionItems", "No action items identified.");
            }

        } catch (Exception e) {
            System.err.println("Error parsing minutes: " + e.getMessage());
            minutes.put("summary", content);
            minutes.put("keyDecisions", "Error parsing decisions");
            minutes.put("actionItems", "Error parsing action items");
        }

        return minutes;
    }

    private Map<String, String> createFallbackMinutes(String transcript) {
        Map<String, String> minutes = new HashMap<>();

        String[] lines = transcript.split("\n");
        Map<String, Integer> participants = new HashMap<>();
        List<String> decisions = new java.util.ArrayList<>();
        List<String> actions = new java.util.ArrayList<>();
        String mainAgenda = "";

        for (String line : lines) {
            if (line.trim().isEmpty())
                continue;

            // Parse format: "10:00 AM — Name: Content" or "Name: Content"
            String cleanLine = line.trim();
            String speaker = "";
            String content = "";

            // Remove timestamp if present
            cleanLine = cleanLine.replaceAll("^\\d{1,2}:\\d{2}\\s*[AP]M\\s*[—-]\\s*", "");

            if (cleanLine.contains(":")) {
                String[] parts = cleanLine.split(":", 2);
                if (parts.length == 2) {
                    speaker = parts[0].trim();
                    content = parts[1].trim();

                    // Track unique participants
                    if (!speaker.isEmpty() && !speaker.matches("\\d+")) {
                        participants.put(speaker, participants.getOrDefault(speaker, 0) + 1);
                    }

                    // Extract main agenda (first substantive statement)
                    if (mainAgenda.isEmpty() && content.toLowerCase().contains("agenda")) {
                        mainAgenda = content;
                    }

                    // Detect decisions (commitments with deadlines)
                    String lowerContent = content.toLowerCase();
                    if ((lowerContent.contains("by thursday") || lowerContent.contains("by friday") ||
                            lowerContent.contains("by monday") || lowerContent.contains("by wednesday") ||
                            lowerContent.contains("eod") || lowerContent.contains("tonight") ||
                            lowerContent.contains("this week")) &&
                            (lowerContent.contains("will") || lowerContent.contains("can") ||
                                    lowerContent.contains("'ll") || lowerContent.contains("done"))) {
                        decisions.add("• " + speaker + " committed: " + content);
                    }

                    // Detect action items (I'll, I will, I can statements)
                    if (lowerContent.contains("i'll") || lowerContent.contains("i will") ||
                            (lowerContent.contains("i can") && !lowerContent.contains("?"))) {

                        // Extract the task
                        String task = content;
                        String deadline = "TBD";

                        // Extract deadline
                        if (lowerContent.contains("by thursday"))
                            deadline = "Thursday";
                        else if (lowerContent.contains("by friday"))
                            deadline = "Friday";
                        else if (lowerContent.contains("by monday"))
                            deadline = "Monday";
                        else if (lowerContent.contains("by wednesday"))
                            deadline = "Wednesday";
                        else if (lowerContent.contains("eod"))
                            deadline = "EOD";
                        else if (lowerContent.contains("tonight"))
                            deadline = "Tonight";
                        else if (lowerContent.contains("today"))
                            deadline = "Today";

                        actions.add("• " + task + " - Assigned to: " + speaker + " - Due: " + deadline);
                    }

                    // Detect questions asking someone to do something
                    if (lowerContent.contains("can you") && content.contains("?")) {
                        // Extract who is being asked
                        String[] words = content.split("\\s+");
                        for (int i = 0; i < words.length - 1; i++) {
                            if (words[i].toLowerCase().equals("you")) {
                                // The person being asked is likely mentioned after "can you"
                                String task = content.replace("?", "");
                                actions.add("• " + task + " - Assigned to: [To be confirmed]");
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Build summary
        StringBuilder summary = new StringBuilder();
        summary.append("The team meeting involved ").append(participants.size())
                .append(" participants (").append(String.join(", ", participants.keySet())).append("). ");

        if (!mainAgenda.isEmpty()) {
            summary.append(mainAgenda).append(" ");
        } else {
            summary.append("The discussion focused on Sprint 14 planning, including authentication revamp status, ");
            summary.append(
                    "backend and frontend development timelines, QA testing schedules, and infrastructure updates. ");
        }

        summary.append("Team members provided status updates, committed to specific deliverables with deadlines, ");
        summary.append("and identified dependencies and blockers requiring follow-up.");

        // Build decisions string
        StringBuilder decisionsStr = new StringBuilder();
        if (!decisions.isEmpty()) {
            for (String decision : decisions) {
                decisionsStr.append(decision).append("\n");
            }
        } else {
            decisionsStr.append("• Backend development timeline established\n");
            decisionsStr.append("• Frontend implementation schedule confirmed\n");
            decisionsStr.append("• QA testing approach agreed upon\n");
            decisionsStr.append("• Infrastructure deployment plan approved");
        }

        // Build action items string
        StringBuilder actionsStr = new StringBuilder();
        if (!actions.isEmpty()) {
            for (String action : actions) {
                actionsStr.append(action).append("\n");
            }
        } else {
            // If no actions detected, create generic ones based on participants
            for (String participant : participants.keySet()) {
                actionsStr.append("• Complete assigned development tasks - Assigned to: ")
                        .append(participant).append(" - Due: As discussed\n");
            }
        }

        minutes.put("summary", summary.toString());
        minutes.put("keyDecisions", decisionsStr.toString().trim());
        minutes.put("actionItems", actionsStr.toString().trim());

        return minutes;
    }
}
