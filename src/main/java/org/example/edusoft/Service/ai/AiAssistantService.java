package org.example.edusoft.service.ai;

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
import java.util.Map;

@Service
public class AiAssistantService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String aiServiceUrl = "http://localhost:8000"; // Python 微服务地址

    public Map<String, Object> uploadEmbeddingFile(MultipartFile file, String courseId) {
        try {
            // 保存到临时文件
            Path tempFile = Files.createTempFile("upload-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile.toFile());

            // 构造请求
            String url = aiServiceUrl + "/embedding/upload";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile.toFile()));
            if (courseId != null) {
                body.add("course_id", courseId);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 调用 Python 微服务
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            // 删除临时文件
            Files.deleteIfExists(tempFile);

            return response.getBody();
        } catch (Exception e) {
            return Map.of(
                "status", "fail",
                "message", "AI服务调用失败: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> generateTeachingContent(Map<String, Object> req) {
        try {
            String url = aiServiceUrl + "/rag/generate";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            return response.getBody();
        } catch (Exception e) {
            return Map.of(
                "status", "fail",
                "message", "AI教案生成服务调用失败: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> generateExercises(Map<String, Object> req) {
        try {
            String url = aiServiceUrl + "/rag/generate_exercise";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            return response.getBody();
        } catch (Exception e) {
            return Map.of(
                "status", "fail",
                "message", "AI题目生成服务调用失败: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> onlineAssistant(Map<String, Object> req) {
        try {
            String url = aiServiceUrl + "/rag/assistant";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            return Map.of(
                "status", "fail",
                "message", "AI在线助手服务调用失败: " + e.getMessage()
            );
        }
    }
    
    public Map<String, Object> evaluateSubjective(Map<String, Object> req) {
        try {
            String url = aiServiceUrl + "/rag/evaluate_subjective";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(req, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            return Map.of(
                "status", "fail",
                "message", "AI主观题评估服务调用失败: " + e.getMessage()
            );
        }
    }
}
