package org.example.edusoft.controller.practice;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.example.edusoft.common.domain.Result;
import org.example.edusoft.dto.practice.SubmissionAnswersDTO;
import org.example.edusoft.entity.practice.*;
import org.example.edusoft.mapper.practice.*;
import org.springframework.beans.factory.annotation.*;
import org.example.edusoft.service.practice.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/judge")
public class ManualJudgeController {
 
    @Autowired
    private ManualJudgeService manualJudgeService;

    @GetMapping("/pending")
    public Result<List<SubmissionAnswersDTO>> getPendingAnswers(
        @RequestParam Long practiceId,
        @RequestParam(required = false) Long submissionId) {
        return manualJudgeService.getPendingAnswers(practiceId, submissionId);
    }

    @PostMapping("/judge")
    public Result<Void> judgeAnswers(@RequestBody List<Answer> answers) {
        return manualJudgeService.manualJudge(answers);
    }
}