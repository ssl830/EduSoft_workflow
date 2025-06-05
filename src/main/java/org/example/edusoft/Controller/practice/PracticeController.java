package org.example.edusoft.controller.practice;

import org.example.edusoft.common.Result;
import org.example.edusoft.entity.practice.Practice;
import org.example.edusoft.entity.practice.Question;
import org.example.edusoft.service.practice.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import cn.dev33.satoken.stp.StpUtil;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    @Autowired
    private PracticeService practiceService;

    @PostMapping("/create")
    public Result<Practice> createPractice(@RequestBody Practice practice) {
        try {
            Practice createdPractice = practiceService.createPractice(practice);
            return Result.success(createdPractice, "练习创建成功");
        } catch (Exception e) {
            return Result.error(500, "创建练习失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Practice> updatePractice(@PathVariable Long id, @RequestBody Practice practice) {
        try {
            practice.setId(id);
            Practice updatedPractice = practiceService.updatePractice(practice);
            return Result.success(updatedPractice, "练习更新成功");
        } catch (Exception e) {
            return Result.error(500, "更新练习失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Practice>> getPracticeList(@RequestParam Long classId) {
        try {
            List<Practice> practices = practiceService.getPracticeList(classId);
            return Result.success(practices, "获取练习列表成功");
        } catch (Exception e) {
            return Result.error(500, "获取练习列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Practice> getPracticeDetail(@PathVariable Long id) {
        try {
            Practice practice = practiceService.getPracticeDetail(id);
            return Result.success(practice, "获取练习详情成功");
        } catch (Exception e) {
            return Result.error(500, "获取练习详情失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePractice(@PathVariable Long id) {
        try {
            practiceService.deletePractice(id);
            return Result.success(null, "练习删除成功");
        } catch (Exception e) {
            return Result.error(500, "删除练习失败：" + e.getMessage());
        }
    }

    @PostMapping("/{practiceId}/questions/{questionId}")
    public Result<Void> addQuestionToPractice(
            @PathVariable Long practiceId,
            @PathVariable Long questionId,
            @RequestParam Integer score) {
        try {
            practiceService.addQuestionToPractice(practiceId, questionId, score);
            return Result.success(null, "添加题目成功");
        } catch (Exception e) {
            return Result.error(500, "添加题目失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{practiceId}/questions/{questionId}")
    public Result<Void> removeQuestionFromPractice(
            @PathVariable Long practiceId,
            @PathVariable Long questionId) {
        try {
            practiceService.removeQuestionFromPractice(practiceId, questionId);
            return Result.success(null, "移除题目成功");
        } catch (Exception e) {
            return Result.error(500, "移除题目失败：" + e.getMessage());
        }
    }

    @GetMapping("/{practiceId}/questions")
    public Result<List<Question>> getPracticeQuestions(@PathVariable Long practiceId) {
        try {
            List<Question> questions = practiceService.getPracticeQuestions(practiceId);
            return Result.success(questions, "获取练习题目列表成功");
        } catch (Exception e) {
            return Result.error(500, "获取练习题目列表失败：" + e.getMessage());
        }
    }

    // 收藏题目
    @PostMapping("/questions/{questionId}/favorite")
    public Result<Boolean> favoriteQuestion(@PathVariable Long questionId) {
        if (!StpUtil.isLogin()) {
            return Result.error("请先登录");
        }
        Long studentId = StpUtil.getLoginIdAsLong();
        practiceService.favoriteQuestion(studentId, questionId);
        return Result.success(true);
    }

    // 取消收藏题目
    @DeleteMapping("/questions/{questionId}/favorite")
    public Result<Boolean> unfavoriteQuestion(@PathVariable Long questionId) {
        if (!StpUtil.isLogin()) {
            return Result.error("请先登录");
        }
        Long studentId = StpUtil.getLoginIdAsLong();
        practiceService.unfavoriteQuestion(studentId, questionId);
        return Result.success(true);
    }

    // 获取收藏的题目列表
    @GetMapping("/questions/favorites")
    public Result<List<Map<String, Object>>> getFavoriteQuestions() {
        if (!StpUtil.isLogin()) {
            return Result.error("请先登录");
        }
        Long studentId = StpUtil.getLoginIdAsLong();
        List<Map<String, Object>> questions = practiceService.getFavoriteQuestions(studentId);
        return Result.success(questions);
    }

    // 添加错题
    @PostMapping("/questions/{questionId}/wrong")
    public Result<Boolean> addWrongQuestion(
            @PathVariable Long questionId,
            @RequestBody Map<String, String> data) {
        if (!StpUtil.isLogin()) {
            return Result.error("请先登录");
        }
        Long studentId = StpUtil.getLoginIdAsLong();
        String wrongAnswer = data.get("wrongAnswer");
        practiceService.addWrongQuestion(studentId, questionId, wrongAnswer);
        return Result.success(true);
    }

    // 获取错题列表
    @GetMapping("/questions/wrong")
    public Result<List<Map<String, Object>>> getWrongQuestions() {
        if (!StpUtil.isLogin()) {
            return Result.error("请先登录");
        }
        Long studentId = StpUtil.getLoginIdAsLong();
        List<Map<String, Object>> questions = practiceService.getWrongQuestions(studentId);
        return Result.success(questions);
    }

    // 获取某个课程的错题列表
    @GetMapping("/questions/wrong/course/{courseId}")
    public Result<List<Map<String, Object>>> getWrongQuestionsByCourse(@PathVariable Long courseId) {
        if (!StpUtil.isLogin()) {
            return Result.error("请先登录");
        }
        Long studentId = StpUtil.getLoginIdAsLong();
        List<Map<String, Object>> questions = practiceService.getWrongQuestionsByCourse(studentId, courseId);
        return Result.success(questions);
    }

    // 删除错题
    @DeleteMapping("/questions/{questionId}/wrong")
    public Result<Boolean> removeWrongQuestion(@PathVariable Long questionId) {
        if (!StpUtil.isLogin()) {
            return Result.error("请先登录");
        }
        Long studentId = StpUtil.getLoginIdAsLong();
        practiceService.removeWrongQuestion(studentId, questionId);
        return Result.success(true);
    }

    // 获取课程的所有练习
    @GetMapping("/course/{courseId}")
    public Result<List<Map<String, Object>>> getCoursePractices(@PathVariable Long courseId) {
        if (!StpUtil.isLogin()) {
            return Result.error("请先登录");
        }
        Long studentId = StpUtil.getLoginIdAsLong();
        List<Map<String, Object>> practices = practiceService.getCoursePractices(studentId, courseId);
        return Result.success(practices);
    }
}