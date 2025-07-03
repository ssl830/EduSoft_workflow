package org.example.edusoft.controller.ai;

import org.example.edusoft.service.ai.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.context.ContextLoader;
import org.example.edusoft.service.selfpractice.SelfPracticeService;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {
    private static final Logger logger = LoggerFactory.getLogger(AiAssistantController.class);

    @Autowired
    private AiAssistantService aiAssistantService;

    @Autowired
    private SelfPracticeService selfPracticeService;

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

    @PostMapping("/rag/generate_student_exercise")
    public Map<String, Object> generateStudentExercise(@RequestBody Map<String, Object> req) {
        logger.info("收到学生自测练习生成请求: {}", req);
        Map<String,Object> body = aiAssistantService.generateStudentExercise(req);
        if (StpUtil.isLogin()) {
            try {
                Long studentId = StpUtil.getLoginIdAsLong();
                Long pid = selfPracticeService.saveGeneratedPractice(studentId, body);
                body.put("practiceId", pid);
            } catch (Exception e) {
                logger.error("保存自测练习失败", e);
            }
        }
        return body;
    }

}
