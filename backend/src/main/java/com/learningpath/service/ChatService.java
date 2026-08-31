package com.learningpath.service;

import com.learningpath.dto.ChatMessageDto;
import com.learningpath.dto.ChatRequest;
import com.learningpath.dto.ChatResponse;
import com.learningpath.dto.RecalculateTimeRequest;
import com.learningpath.entity.ChatMessage;
import com.learningpath.entity.LearnerProfile;
import com.learningpath.entity.MessageSender;
import com.learningpath.entity.User;
import com.learningpath.repository.ChatMessageRepository;
import com.learningpath.repository.LearnerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private LearnerProfileRepository profileRepository;

    @Autowired
    private AiServiceClient aiServiceClient;

    @Autowired
    private RoadmapService roadmapService;

    @Autowired
    private AuthService authService;

    public List<ChatMessageDto> getChatHistory() {
        User user = authService.getCurrentAuthenticatedUser();
        List<ChatMessage> messages = chatMessageRepository.findByUserIdOrderByCreatedAtAsc(user.getId());
        return messages.stream().map(m -> new ChatMessageDto(
                m.getId(),
                m.getSender().name(),
                m.getMessage(),
                m.getMetadataJson(),
                m.getCreatedAt()
        )).collect(Collectors.toList());
    }

    @Transactional
    public ChatResponse sendMessage(ChatRequest request) {
        User user = authService.getCurrentAuthenticatedUser();
        LearnerProfile profile = profileRepository.findByUser(user).orElse(null);

        // Save User Message
        ChatMessage userMsg = new ChatMessage(user, MessageSender.USER, request.getMessage(), null);
        chatMessageRepository.save(userMsg);

        // Prepare context for AI Service
        Map<String, Object> context = new HashMap<>();
        if (profile != null) {
            context.put("target_role", profile.getTargetRole());
            context.put("experience_level", profile.getExperienceLevel() != null ? profile.getExperienceLevel().name() : "BEGINNER");
            context.put("weekly_hours", profile.getWeeklyHours());
            context.put("preferred_style", profile.getPreferredStyle() != null ? profile.getPreferredStyle().name() : "PRACTICAL");
        }

        Map<String, Object> aiResult = aiServiceClient.generateChatResponse(request.getMessage(), context);
        String replyText = (String) aiResult.getOrDefault("reply", "I'm analyzing your learning path...");
        String suggestedAction = (String) aiResult.getOrDefault("suggested_action", aiResult.getOrDefault("suggestedAction", "GENERAL"));
        String actionType = (String) aiResult.getOrDefault("action_type", "NONE");
        Object actionPayload = aiResult.getOrDefault("action_payload", null);
        List<String> quickReplies = (List<String>) aiResult.getOrDefault("quick_replies", aiResult.getOrDefault("quickReplies", List.of("What should I learn next?", "Show my skill gaps")));

        // Automatic Adaptive Action execution
        if ("PACE_ADAPTED".equals(actionType) && actionPayload instanceof Map) {
            Map<?, ?> pMap = (Map<?, ?>) actionPayload;
            Object wH = pMap.get("weekly_hours");
            if (wH instanceof Number) {
                int newHours = ((Number) wH).intValue();
                if (profile != null) {
                    profile.setWeeklyHours(newHours);
                    profileRepository.save(profile);
                }
                try {
                    roadmapService.recalculateRoadmapTimeline(new RecalculateTimeRequest(newHours));
                } catch (Exception ignored) {}
            }
        }

        // Save Assistant Message
        ChatMessage assistantMsg = new ChatMessage(user, MessageSender.ASSISTANT, replyText, suggestedAction);
        chatMessageRepository.save(assistantMsg);

        return new ChatResponse(replyText, suggestedAction, actionType, actionPayload, quickReplies);
    }
}
