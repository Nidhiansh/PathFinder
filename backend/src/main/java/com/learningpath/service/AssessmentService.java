package com.learningpath.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.dto.*;
import com.learningpath.entity.*;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentQuestionRepository questionRepository;

    @Autowired
    private AssessmentSubmissionRepository submissionRepository;

    @Autowired
    private LearnerProfileRepository profileRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private RoadmapService roadmapService;

    @Autowired
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<AssessmentDto> getAllAssessments() {
        return assessmentRepository.findAll().stream().map(a -> {
            AssessmentDto dto = new AssessmentDto();
            dto.setId(a.getId());
            dto.setTitle(a.getTitle());
            dto.setDescription(a.getDescription());
            dto.setSkillName(a.getSkill() != null ? a.getSkill().getName() : "Software Engineering");
            dto.setDifficulty(a.getDifficulty().name());
            dto.setPassingScore(a.getPassingScore());
            dto.setTimeLimitMinutes(a.getTimeLimitMinutes());
            dto.setTotalQuestions(questionRepository.findByAssessmentId(a.getId()).size());
            return dto;
        }).collect(Collectors.toList());
    }

    public AssessmentDto getAssessmentById(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + id));

        List<AssessmentQuestion> questions = questionRepository.findByAssessmentId(assessment.getId());
        List<QuestionDto> questionDtos = questions.stream().map(q -> {
            QuestionDto dto = new QuestionDto();
            dto.setId(q.getId());
            dto.setQuestionText(q.getQuestionText());
            try {
                List<String> options = objectMapper.readValue(q.getOptionsJson(), new TypeReference<List<String>>() {});
                dto.setOptions(options);
            } catch (Exception e) {
                dto.setOptions(List.of("Option A", "Option B", "Option C", "Option D"));
            }
            // Mask correct answer index for test takers
            dto.setCorrectAnswerIndex(null);
            dto.setExplanation(null);
            return dto;
        }).collect(Collectors.toList());

        AssessmentDto dto = new AssessmentDto();
        dto.setId(assessment.getId());
        dto.setTitle(assessment.getTitle());
        dto.setDescription(assessment.getDescription());
        dto.setSkillName(assessment.getSkill() != null ? assessment.getSkill().getName() : "Software Engineering");
        dto.setDifficulty(assessment.getDifficulty().name());
        dto.setPassingScore(assessment.getPassingScore());
        dto.setTimeLimitMinutes(assessment.getTimeLimitMinutes());
        dto.setTotalQuestions(questions.size());
        dto.setQuestions(questionDtos);

        return dto;
    }

    @Transactional
    public AssessmentResultDto submitAssessment(Long assessmentId, AssessmentSubmissionRequest request) {
        User user = authService.getCurrentAuthenticatedUser();
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        List<AssessmentQuestion> questions = questionRepository.findByAssessmentId(assessment.getId());
        int totalQuestions = questions.size();
        int correctCount = 0;

        List<QuestionDto> reviewedQuestions = new ArrayList<>();

        for (AssessmentQuestion q : questions) {
            Integer selectedAnswer = request.getAnswers() != null ? request.getAnswers().get(q.getId()) : null;
            boolean isCorrect = selectedAnswer != null && selectedAnswer.equals(q.getCorrectAnswerIndex());
            if (isCorrect) {
                correctCount++;
            }

            QuestionDto reviewDto = new QuestionDto();
            reviewDto.setId(q.getId());
            reviewDto.setQuestionText(q.getQuestionText());
            try {
                reviewDto.setOptions(objectMapper.readValue(q.getOptionsJson(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                reviewDto.setOptions(List.of());
            }
            reviewDto.setCorrectAnswerIndex(q.getCorrectAnswerIndex());
            reviewDto.setExplanation(q.getExplanation());
            reviewedQuestions.add(reviewDto);
        }

        int scorePercentage = totalQuestions > 0 ? (int) Math.round(((double) correctCount / totalQuestions) * 100.0) : 0;
        boolean passed = scorePercentage >= assessment.getPassingScore();

        // Adaptive Engine Rules:
        String adaptiveActionTaken;
        String feedbackSummary;

        LearnerProfile profile = profileRepository.findByUser(user).orElse(null);
        Skill skill = assessment.getSkill();

        if (scorePercentage >= 90) {
            adaptiveActionTaken = "High Mastery Detected (>90%). Verified " + skill.getName() + " proficiency at " + scorePercentage + "%. Fast-tracked downstream prerequisites and recommended advanced capstone project.";
            feedbackSummary = "Outstanding performance! You demonstrated comprehensive mastery of core and edge-case principles.";
            if (profile != null && skill != null) {
                updateOrVerifySkill(profile, skill, Math.max(90, scorePercentage), true);
            }
        } else if (passed) {
            adaptiveActionTaken = "Passed (" + scorePercentage + "%). Unlocked next sequential learning roadmap phase.";
            feedbackSummary = "Great job! You have satisfied the verification criteria for " + skill.getName() + ".";
            if (profile != null && skill != null) {
                updateOrVerifySkill(profile, skill, scorePercentage, true);
            }
        } else {
            adaptiveActionTaken = "Needs Remediation (<" + assessment.getPassingScore() + "%). Added targeted foundational review resources before proceeding to advanced modules.";
            feedbackSummary = "Review recommended: Strengthen core concepts in " + skill.getName() + " and retry the checkpoint.";
            if (profile != null && skill != null) {
                updateOrVerifySkill(profile, skill, Math.min(45, scorePercentage), false);
            }
        }

        AssessmentSubmission submission = new AssessmentSubmission(
                user,
                assessment,
                scorePercentage,
                passed,
                adaptiveActionTaken
        );
        submission = submissionRepository.save(submission);

        AssessmentResultDto result = new AssessmentResultDto();
        result.setSubmissionId(submission.getId());
        result.setAssessmentId(assessment.getId());
        result.setAssessmentTitle(assessment.getTitle());
        result.setSkillName(skill.getName());
        result.setScorePercentage(scorePercentage);
        result.setPassed(passed);
        result.setCorrectCount(correctCount);
        result.setTotalQuestions(totalQuestions);
        result.setAdaptiveActionTaken(adaptiveActionTaken);
        result.setFeedbackSummary(feedbackSummary);
        result.setReviewedQuestions(reviewedQuestions);

        return result;
    }

    private void updateOrVerifySkill(LearnerProfile profile, Skill skill, int proficiency, boolean verified) {
        UserSkill userSkill = userSkillRepository.findByProfileIdAndSkillId(profile.getId(), skill.getId())
                .orElseGet(() -> new UserSkill(profile, skill, proficiency, verified));
        userSkill.setProficiencyLevel(proficiency);
        userSkill.setIsVerified(verified);
        userSkillRepository.save(userSkill);
    }
}
