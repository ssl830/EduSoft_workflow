package org.example.edusoft.service.ai;

import org.example.edusoft.entity.ai.AiServiceCallLog;
import org.example.edusoft.mapper.ai.AiServiceCallLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Map;

import cn.dev33.satoken.stp.StpUtil;

@Service
public class AiAssistantService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String aiServiceUrl = "http://localhost:8000"; // Python 微服务地址

    @Autowired
    private AiServiceCallLogMapper aiServiceCallLogMapper;

    // Helper method to log AI service calls
    private void logAiServiceCall(Long userId, String endpoint, long durationMs, String status, String errorMessage) {
        AiServiceCallLog log = new AiServiceCallLog();
        log.setUserId(userId);
        log.setEndpoint(endpoint);
        log.setDurationMs(durationMs);
        log.setCallTime(LocalDateTime.now());
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        aiServiceCallLogMapper.insertLog(log);
    }

    public Map<String, Object> uploadEmbeddingFile(MultipartFile file, String courseId) {
        long startTime = System.currentTimeMillis();
        String endpoint = "/embedding/upload";
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
            Path tempFile = Files.createTempFile("upload-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile.toFile());

            String url = aiServiceUrl + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile.toFile()));
            if (courseId != null) {
                body.add("course_id", courseId);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            Files.deleteIfExists(tempFile);

            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "success", null);
            return response.getBody();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "fail", e.getMessage());
            return Map.of(
                "status", "fail",
                "message", "AI服务调用失败: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> generateTeachingContent(Map<String, Object> req) {
        long startTime = System.currentTimeMillis();
        String endpoint = "/rag/generate";
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
            String url = aiServiceUrl + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "success", null);
            return response.getBody();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "fail", e.getMessage());
            return Map.of(
                "status", "fail",
                "message", "AI教案生成服务调用失败: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> generateTeachingContentDetail(Map<String, Object> req) {
        long startTime = System.currentTimeMillis();
        String endpoint = "/rag/detail";
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
            String url = aiServiceUrl + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "success", null);
            return response.getBody();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "fail", e.getMessage());
            return Map.of(
                "status", "fail",
                "message", "AI教案细节生成服务调用失败: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> regenerateTeachingContent(Map<String, Object> req) {
        long startTime = System.currentTimeMillis();
        String endpoint = "/rag/regenerate";
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
            String url = aiServiceUrl + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "success", null);
            return response.getBody();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "fail", e.getMessage());
            return Map.of(
                "status", "fail",
                "message", "AI教案重新生成服务调用失败: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> generateExercises(Map<String, Object> req) {
        long startTime = System.currentTimeMillis();
        String endpoint = "/rag/generate_exercise";
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
            String url = aiServiceUrl + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "success", null);
            return response.getBody();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "fail", e.getMessage());
            return Map.of(
                "status", "fail",
                "message", "AI题目生成服务调用失败: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> onlineAssistant(Map<String, Object> req) {
        long startTime = System.currentTimeMillis();
        String endpoint = "/rag/assistant";
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
            String url = aiServiceUrl + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "success", null);
            return response.getBody();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "fail", e.getMessage());
            return Map.of(
                "status", "fail",
                "message", "AI在线助手服务调用失败: " + e.getMessage()
            );
        }
    }
    
    public Map<String, Object> generateStudentExercise(Map<String, Object> req) {
        long startTime = System.currentTimeMillis();
        String endpoint = "/rag/generate_student_exercise";
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
            String url = aiServiceUrl + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, requestEntity, (Class<Map<String, Object>>)(Class<?>)Map.class);
            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "success", null);
            return response.getBody();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "fail", e.getMessage());
            return Map.of(
                "status", "fail",
                "message", "AI学生自测练习生成服务调用失败: " + e.getMessage()
            );
        }
    }
    public Map<String, Object> evaluateSubjectiveAnswer(String question, String studentAnswer, String referenceAnswer, Double maxScore) {
        long startTime = System.currentTimeMillis();
        String endpoint = "/rag/evaluate_subjective";
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
            String url = aiServiceUrl + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                "question", question,
                "student_answer", studentAnswer,
                "reference_answer", referenceAnswer,
                "max_score", maxScore
            );
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, requestEntity, (Class<Map<String, Object>>)(Class<?>)Map.class);
            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "success", null);
            return response.getBody();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logAiServiceCall(userId, endpoint, duration, "fail", e.getMessage());
            return Map.of(
                "status", "fail",
                "message", "AI主观题评测调用失败: " + e.getMessage()
            );
        }
    }

    // 兼容 Map<String, Object> 参数的主观题评测方法
    public Map<String, Object> evaluateSubjective(Map<String, Object> req) {
        String question = (String) req.getOrDefault("question", "");
        String studentAnswer = (String) req.getOrDefault("student_answer", "");
        String referenceAnswer = (String) req.getOrDefault("reference_answer", "");
        Double maxScore = req.get("max_score") instanceof Number ? ((Number) req.get("max_score")).doubleValue() : 5.0;
        return evaluateSubjectiveAnswer(question, studentAnswer, referenceAnswer, maxScore);
    }
}
