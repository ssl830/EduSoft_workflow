package org.example.edusoft.controller.practice;

import org.example.edusoft.entity.practice.Practice;
import org.example.edusoft.entity.practice.Question;
import org.example.edusoft.service.practice.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    @Autowired
    private PracticeService practiceService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPractice(@RequestBody Practice practice) {
        Practice createdPractice = practiceService.createPractice(practice);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "练习创建成功");
        response.put("data", createdPractice);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePractice(@PathVariable Long id, @RequestBody Practice practice) {
        practice.setId(id);
        Practice updatedPractice = practiceService.updatePractice(practice);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "练习更新成功");
        response.put("data", updatedPractice);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getPracticeList(
            @RequestParam Long courseId,
            @RequestParam Long classId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        List<Practice> practices = practiceService.getPracticeList(courseId, classId, page, size);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取练习列表成功");
        response.put("data", practices);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPracticeDetail(@PathVariable Long id) {
        Practice practice = practiceService.getPracticeDetail(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取练习详情成功");
        response.put("data", practice);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePractice(@PathVariable Long id) {
        practiceService.deletePractice(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "练习删除成功");
        response.put("data", Map.of("practiceId", id));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<Map<String, Object>> addQuestionToPractice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Long questionId = Long.valueOf(request.get("questionId").toString());
        Integer score = Integer.valueOf(request.get("score").toString());
        practiceService.addQuestionToPractice(id, questionId, score);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "题目添加成功");
        response.put("data", Map.of(
            "practiceId", id,
            "questionId", questionId,
            "score", score
        ));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    public ResponseEntity<Map<String, Object>> removeQuestionFromPractice(
            @PathVariable Long id,
            @PathVariable Long questionId) {
        practiceService.removeQuestionFromPractice(id, questionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "题目移除成功");
        response.put("data", Map.of(
            "practiceId", id,
            "questionId", questionId
        ));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<Map<String, Object>> getPracticeQuestions(@PathVariable Long id) {
        List<Question> questions = practiceService.getPracticeQuestions(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取练习题目列表成功");
        response.put("data", questions);
        return ResponseEntity.ok(response);
    }
} 