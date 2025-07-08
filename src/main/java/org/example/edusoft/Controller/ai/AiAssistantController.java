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
    @PostMapping("/evaluate-subjective")
    public Map<String, Object> evaluateSubjective(@RequestBody Map<String, Object> req) {
        logger.info("收到主观题AI评估请求: {}", req);
        return aiAssistantService.evaluateSubjective(req);
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

    @PostMapping("/rag/feedback")
    public Map<String, Object> reviseTeachingContent(@RequestBody Map<String, Object> req) {
        logger.info("收到教案反馈修改请求: {}", req);
        return aiAssistantService.reviseTeachingContent(req);
    }

    @PostMapping("/rag/step_detail")
    public Map<String, Object> generateStepDetail(@RequestBody Map<String, Object> req) {
        logger.info("收到课时环节细节生成请求: {}", req);
        return aiAssistantService.generateStepDetail(req);
    }

    /**
     * 练习学情分析：统计每题得分率，调用AI微服务分析
     * 请求体需包含 practiceId
     */
    @PostMapping("/analyze-exercise")
    public Map<String, Object> analyzeExercise(@RequestBody Map<String, Object> req) {
        logger.info("收到学情分析请求: {}", req);
        Long practiceId = req.get("practiceId") instanceof Number ? ((Number) req.get("practiceId")).longValue() : null;
        if (practiceId == null) {
            return Map.of("status", "fail", "message", "缺少practiceId");
        }
        // 统计并写入得分率，组装题目内容
        Map<String, Object> result = aiAssistantService.analyzeExercise(practiceId);
        return result;
    }

    @PostMapping(value = "/rag/generate_section", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> generateSectionTeachingContent(
            @RequestPart("file") MultipartFile file,
            @RequestParam("course_name") String courseName,
            @RequestParam("section_title") String sectionTitle,
            @RequestParam("expected_hours") Integer expectedHours,
            @RequestParam(value = "constraints", required = false) String constraints
    ) {
        logger.info("收到章节教案生成请求: course={}, section={}", courseName, sectionTitle);
        return aiAssistantService.generateSectionTeachingContent(file, courseName, sectionTitle, expectedHours, constraints);
    }

    // -------------------- 私密知识库目录 --------------------
    @PostMapping("/embedding/base_path")
    public Map<String, Object> setBasePath(@RequestBody Map<String, Object> body) {
        String basePath = (String) body.get("base_path");
        return aiAssistantService.setBasePath(basePath);
    }

    @PostMapping("/embedding/base_path/reset")
    public Map<String, Object> resetBasePath() {
        return aiAssistantService.resetBasePath();
    }
}
