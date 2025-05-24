package org.example.edusoft.controller.practice;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.example.edusoft.common.domain.Result;
import org.example.edusoft.entity.practice.*;
import org.example.edusoft.mapper.practice.*;
import org.springframework.beans.factory.annotation.*;
import org.example.edusoft.service.practice.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submission")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @PostMapping("/submit")
    public Result<Integer> submitPractice(
        @RequestParam Long practiceId,
        @RequestParam Long studentId,
        @RequestBody Map<Long, String> answers) {
        return submissionService.autoJudgeAnswers(practiceId, studentId, answers);
    }
}