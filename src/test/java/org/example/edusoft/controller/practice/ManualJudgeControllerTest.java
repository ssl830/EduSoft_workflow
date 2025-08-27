package org.example.edusoft.controller.practice;

import org.example.edusoft.common.domain.Result;
import org.example.edusoft.dto.practice.*;
import org.example.edusoft.service.practice.ManualJudgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManualJudgeControllerTest {

    @Mock
    private ManualJudgeService manualJudgeService;

    @InjectMocks
    private ManualJudgeController manualJudgeController;

    @BeforeEach
    void setUp() {
        // 重置所有mock对象
        reset(manualJudgeService);
    }

    // getPendingList
    @Test
    void testGetPendingList_Success() {
        PendingListRequest request = new PendingListRequest();
        request.setPracticeId(1L);
        request.setClassId(2L);

        List<PendingSubmissionDTO> list = Collections.singletonList(new PendingSubmissionDTO());
        Result<List<PendingSubmissionDTO>> mockResult = Result.ok(list, "获取待批改列表成功");

        when(manualJudgeService.getPendingSubmissionList(1L, 2L)).thenReturn(mockResult);

        Result<List<PendingSubmissionDTO>> result = manualJudgeController.getPendingList(request);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    @Test
    void testGetPendingList_Failure() {
        PendingListRequest request = new PendingListRequest();
        request.setPracticeId(1L);
        request.setClassId(2L);

        Result<List<PendingSubmissionDTO>> mockResult = Result.error("查询失败");
        when(manualJudgeService.getPendingSubmissionList(1L, 2L)).thenReturn(mockResult);

        Result<List<PendingSubmissionDTO>> result = manualJudgeController.getPendingList(request);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("查询失败"));
    }

    // getSubmissionDetail
    @Test
    void testGetSubmissionDetail_Success() {
        SubmissionDetailRequest request = new SubmissionDetailRequest();
        request.setSubmissionId(5L);

        List<SubmissionDetailDTO> details = Collections.singletonList(new SubmissionDetailDTO());
        Result<List<SubmissionDetailDTO>> mockResult = Result.ok(details, "获取提交详情成功");

        when(manualJudgeService.getSubmissionDetail(5L)).thenReturn(mockResult);

        Result<List<SubmissionDetailDTO>> result = manualJudgeController.getSubmissionDetail(request);
        assertEquals(200, result.getCode());
        assertEquals(details, result.getData());
    }

    @Test
    void testGetSubmissionDetail_Failure() {
        SubmissionDetailRequest request = new SubmissionDetailRequest();
        request.setSubmissionId(5L);

        Result<List<SubmissionDetailDTO>> mockResult = Result.error("获取详情失败");
        when(manualJudgeService.getSubmissionDetail(5L)).thenReturn(mockResult);

        Result<List<SubmissionDetailDTO>> result = manualJudgeController.getSubmissionDetail(request);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("获取详情失败"));
    }

    // judgeAnswers
    @Test
    void testJudgeAnswers_Success() {
        JudgeSubmissionRequest request = new JudgeSubmissionRequest();
        Result<Void> mockResult = Result.ok(null, "批改成功");

        when(manualJudgeService.judgeSubmission(request)).thenReturn(mockResult);

        Result<Void> result = manualJudgeController.judgeAnswers(request);
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void testJudgeAnswers_Failure() {
        JudgeSubmissionRequest request = new JudgeSubmissionRequest();
        Result<Void> mockResult = Result.error("批改失败");

        when(manualJudgeService.judgeSubmission(request)).thenReturn(mockResult);

        Result<Void> result = manualJudgeController.judgeAnswers(request);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("批改失败"));
    }
}