package org.example.edusoft.controller.practice;

import org.example.edusoft.common.Result;
import org.example.edusoft.service.practice.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import cn.dev33.satoken.stp.StpUtil;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practices")
public class practiceController {

    @Autowired
    private PracticeService practiceService;

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

}