package org.example.edusoft.ai;

import org.example.edusoft.config.AiServiceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

/**
 * AIServiceClient 封装SpringBoot调用Python AI微服务的HTTP工具类。
 * 支持资料上传、RAG问答、自动生成练习等功能。
 */
@Component
public class AIServiceClient {

    @Autowired
    private AiServiceProperties aiServiceProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 上传资料并入库（调用/embedding/upload）
     */
    public String uploadMaterial(MultipartFile file) throws IOException {
        String url = aiServiceProperties.getBaseUrl() + "/embedding/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        return response.getBody();
    }

    /**
     * RAG智能问答（调用/rag/answer）
     */
    public String ragAnswer(String question) {
        String url = aiServiceProperties.getBaseUrl() + "/rag/answer";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("question", question);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
        return (String) response.getBody().get("answer");
    }

    /**
     * 自动生成练习题与答案（调用/rag/generate_exercise）
     */
    public String generateExercise(String courseOutline) {
        String url = aiServiceProperties.getBaseUrl() + "/rag/generate_exercise";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("course_outline", courseOutline);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
        return (String) response.getBody().get("exercise");
    }

    /**
     * 根据课程大纲自动生成教学内容（调用/rag/generate_teaching_content）
     */
    public Map<String, Object> generateTeachingContent(String courseOutline, String courseName, Integer expectedHours) {
        String url = aiServiceProperties.getBaseUrl() + "/rag/generate_teaching_content";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
            "course_outline", courseOutline,
            "course_name", courseName,
            "expected_hours", expectedHours
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
        return response.getBody();
    }
} 