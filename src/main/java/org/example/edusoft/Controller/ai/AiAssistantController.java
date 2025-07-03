package org.example.edusoft.controller.ai;

import org.example.edusoft.service.ai.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {
    private static final Logger logger = LoggerFactory.getLogger(AiAssistantController.class);

    @Autowired
    private AiAssistantService aiAssistantService;

    @PostMapping("/embedding/upload")
    public Map<String, Object> uploadEmbeddingFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "course_id", required = false) String courseId
    ) {
        logger.info("收到上传知识库文件请求: file={}, course_id={}", file.getOriginalFilename(), courseId);
        return aiAssistantService.uploadEmbeddingFile(file, courseId);
    }

    @PostMapping("/rag/generate")
    public Map<String, Object> generateTeachingContent(@RequestBody Map<String, Object> req) {
        logger.info("收到生成教案请求: {}", req);
        return aiAssistantService.generateTeachingContent(req);
    }

    @PostMapping("/rag/detail")
    public Map<String, Object> generateTeachingContentDetail(@RequestBody Map<String, Object> req) {
        logger.info("收到生成教案细节请求: {}", req);
        return aiAssistantService.generateTeachingContentDetail(req);
    }

    @PostMapping("/rag/regenerate")
        public Map<String, Object> regenerateTeachingContent(@RequestBody Map<String, Object> req) {
            logger.info("收到重新生成教案请求: {}", req);
            return aiAssistantService.regenerateTeachingContent(req);
        }

    @PostMapping("/rag/generate_exercise")
    public Map<String, Object> generateExercises(@RequestBody Map<String, Object> req) {
        logger.info("收到生成题目请求: {}", req);
        return aiAssistantService.generateExercises(req);
    }

    @PostMapping("/rag/assistant")
    public Map<String, Object> onlineAssistant(@RequestBody Map<String, Object> req) {
        logger.info("收到在线学习助手请求: {}", req);
        return aiAssistantService.onlineAssistant(req);
    }

}
