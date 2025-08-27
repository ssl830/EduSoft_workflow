package org.example.edusoft.controller.homework;

import org.example.edusoft.common.domain.Result;
import org.example.edusoft.dto.homework.HomeworkDTO;
import org.example.edusoft.dto.homework.HomeworkSubmissionDTO;
import org.example.edusoft.service.homework.HomeworkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeworkControllerTest {

    @Mock
    private HomeworkService homeworkService;

    @InjectMocks
    private HomeworkController homeworkController;

    @BeforeEach
    void setUp() {
        reset(homeworkService);
    }

    // ==================== createHomework 方法测试 ====================

    @Test
    void testCreateHomework_Success() {
        // 准备测试数据 - 正常情况
        Long classId = 1L;
        String title = "测试作业";
        String description = "作业描述";
        String endTime = "2025-12-31";
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());
        
        when(homeworkService.createHomework(classId, title, description, endTime, file))
            .thenReturn(123L);
        
        // 执行测试
        Result<Long> result = homeworkController.createHomework(classId, title, description, endTime, file);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("作业创建成功", result.getMsg());
        assertEquals(123L, result.getData());
        verify(homeworkService).createHomework(classId, title, description, endTime, file);
    }

    @Test
    void testCreateHomework_Exception() {
        // 准备测试数据 - 异常情况
        Long classId = 1L;
        String title = "测试作业";
        String description = "作业描述";
        String endTime = "2025-12-31";
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());
        
        when(homeworkService.createHomework(classId, title, description, endTime, file))
            .thenThrow(new RuntimeException("创建失败"));
        
        // 执行测试
        Result<Long> result = homeworkController.createHomework(classId, title, description, endTime, file);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("作业创建失败：创建失败", result.getMsg());
        verify(homeworkService).createHomework(classId, title, description, endTime, file);
    }

    // ==================== getHomework 方法测试 ====================

    @Test
    void testGetHomework_Success() {
        // 准备测试数据 - 正常情况
        Long homeworkId = 1L;
        HomeworkDTO expectedHomework = HomeworkDTO.builder()
            .homeworkId(homeworkId)
            .title("测试作业")
            .build();
        
        when(homeworkService.getHomework(homeworkId)).thenReturn(expectedHomework);
        
        // 执行测试
        Result<HomeworkDTO> result = homeworkController.getHomework(homeworkId);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("获取作业详情成功", result.getMsg());
        assertEquals(expectedHomework, result.getData());
        verify(homeworkService).getHomework(homeworkId);
    }

    @Test
    void testGetHomework_NotFound() {
        // 准备测试数据 - 异常情况：作业不存在
        Long homeworkId = 1L;
        
        when(homeworkService.getHomework(homeworkId)).thenReturn(null);
        
        // 执行测试
        Result<HomeworkDTO> result = homeworkController.getHomework(homeworkId);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("作业不存在", result.getMsg());
        verify(homeworkService).getHomework(homeworkId);
    }

    // ==================== getHomeworkList 方法测试 ====================

    @Test
    void testGetHomeworkList_Success() {
        // 准备测试数据 - 正常情况
        Long classId = 1L;
        List<HomeworkDTO> expectedList = Arrays.asList(
            HomeworkDTO.builder().homeworkId(1L).title("作业1").build(),
            HomeworkDTO.builder().homeworkId(2L).title("作业2").build()
        );
        
        when(homeworkService.getHomeworkList(classId)).thenReturn(expectedList);
        
        // 执行测试
        Result<List<HomeworkDTO>> result = homeworkController.getHomeworkList(classId);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("获取作业列表成功", result.getMsg());
        assertEquals(expectedList, result.getData());
        verify(homeworkService).getHomeworkList(classId);
    }

    @Test
    void testGetHomeworkList_Exception() {
        // 准备测试数据 - 异常情况
        Long classId = 1L;
        
        when(homeworkService.getHomeworkList(classId)).thenThrow(new RuntimeException("查询失败"));
        
        // 执行测试
        Result<List<HomeworkDTO>> result = homeworkController.getHomeworkList(classId);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("获取作业列表失败：查询失败", result.getMsg());
        verify(homeworkService).getHomeworkList(classId);
    }

    // ==================== submitHomework 方法测试 ====================

    @Test
    void testSubmitHomework_Success() {
        // 准备测试数据 - 正常情况
        Long homeworkId = 1L;
        Long studentId = 1L;
        MultipartFile file = new MockMultipartFile("file", "submission.pdf", "application/pdf", "submission content".getBytes());
        
        when(homeworkService.submitHomework(homeworkId, studentId, file)).thenReturn(456L);
        
        // 执行测试
        Result<Long> result = homeworkController.submitHomework(homeworkId, studentId, file);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("作业提交成功", result.getMsg());
        assertEquals(456L, result.getData());
        verify(homeworkService).submitHomework(homeworkId, studentId, file);
    }

    @Test
    void testSubmitHomework_Exception() {
        // 准备测试数据 - 异常情况
        Long homeworkId = 1L;
        Long studentId = 1L;
        MultipartFile file = new MockMultipartFile("file", "submission.pdf", "application/pdf", "submission content".getBytes());
        
        when(homeworkService.submitHomework(homeworkId, studentId, file)).thenThrow(new RuntimeException("提交失败"));
        
        // 执行测试
        Result<Long> result = homeworkController.submitHomework(homeworkId, studentId, file);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("作业提交失败：提交失败", result.getMsg());
        verify(homeworkService).submitHomework(homeworkId, studentId, file);
    }

    // ==================== getSubmissionList 方法测试 ====================

    @Test
    void testGetSubmissionList_Success() {
        // 准备测试数据 - 正常情况
        Long homeworkId = 1L;
        List<HomeworkSubmissionDTO> expectedList = Arrays.asList(
            HomeworkSubmissionDTO.builder().submissionId(1L).studentId("1").studentName("学生1").build(),
            HomeworkSubmissionDTO.builder().submissionId(2L).studentId("2").studentName("学生2").build()
        );
        
        when(homeworkService.getSubmissionList(homeworkId)).thenReturn(expectedList);
        
        // 执行测试
        Result<List<HomeworkSubmissionDTO>> result = homeworkController.getSubmissionList(homeworkId);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("获取提交列表成功", result.getMsg());
        assertEquals(expectedList, result.getData());
        verify(homeworkService).getSubmissionList(homeworkId);
    }

    @Test
    void testGetSubmissionList_Exception() {
        // 准备测试数据 - 异常情况
        Long homeworkId = 1L;
        
        when(homeworkService.getSubmissionList(homeworkId)).thenThrow(new RuntimeException("查询失败"));
        
        // 执行测试
        Result<List<HomeworkSubmissionDTO>> result = homeworkController.getSubmissionList(homeworkId);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("获取提交列表失败：查询失败", result.getMsg());
        verify(homeworkService).getSubmissionList(homeworkId);
    }

    // ==================== getStudentSubmission 方法测试 ====================

    @Test
    void testGetStudentSubmission_Success() {
        // 准备测试数据 - 正常情况
        Long homeworkId = 1L;
        Long studentId = 1L;
        HomeworkSubmissionDTO expectedSubmission = HomeworkSubmissionDTO.builder()
            .submissionId(789L)
            .studentId("1")
            .studentName("测试学生")
            .build();
        
        when(homeworkService.getStudentSubmission(homeworkId, studentId)).thenReturn(expectedSubmission);
        
        // 执行测试
        Result<HomeworkSubmissionDTO> result = homeworkController.getStudentSubmission(homeworkId, studentId);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("获取提交记录成功", result.getMsg());
        assertEquals(expectedSubmission, result.getData());
        verify(homeworkService).getStudentSubmission(homeworkId, studentId);
    }

    @Test
    void testGetStudentSubmission_NotFound() {
        // 准备测试数据 - 异常情况：提交记录不存在
        Long homeworkId = 1L;
        Long studentId = 1L;
        
        when(homeworkService.getStudentSubmission(homeworkId, studentId)).thenReturn(null);
        
        // 执行测试
        Result<HomeworkSubmissionDTO> result = homeworkController.getStudentSubmission(homeworkId, studentId);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("未找到提交记录", result.getMsg());
        verify(homeworkService).getStudentSubmission(homeworkId, studentId);
    }

    // ==================== downloadHomeworkFile 方法测试 ====================

    @Test
    void testDownloadHomeworkFile_Success() {
        // 准备测试数据 - 正常情况
        Long homeworkId = 1L;
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // 执行测试
        homeworkController.downloadHomeworkFile(homeworkId, response);
        
        // 验证结果
        verify(homeworkService).downloadHomeworkFile(homeworkId, response);
    }

    @Test
    void testDownloadHomeworkFile_Exception() {
        // 准备测试数据 - 异常情况
        Long homeworkId = 1L;
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        doThrow(new RuntimeException("下载失败")).when(homeworkService).downloadHomeworkFile(homeworkId, response);
        
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            homeworkController.downloadHomeworkFile(homeworkId, response);
        });
        
        verify(homeworkService).downloadHomeworkFile(homeworkId, response);
    }

    // ==================== downloadSubmissionFile 方法测试 ====================

    @Test
    void testDownloadSubmissionFile_Success() {
        // 准备测试数据 - 正常情况
        Long submissionId = 1L;
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // 执行测试
        homeworkController.downloadSubmissionFile(submissionId, response);
        
        // 验证结果
        verify(homeworkService).downloadSubmissionFile(submissionId, response);
    }

    @Test
    void testDownloadSubmissionFile_Exception() {
        // 准备测试数据 - 异常情况
        Long submissionId = 1L;
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        doThrow(new RuntimeException("下载失败")).when(homeworkService).downloadSubmissionFile(submissionId, response);
        
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            homeworkController.downloadSubmissionFile(submissionId, response);
        });
        
        verify(homeworkService).downloadSubmissionFile(submissionId, response);
    }

    // ==================== deleteHomework 方法测试 ====================

    @Test
    void testDeleteHomework_Success() {
        // 准备测试数据 - 正常情况
        Long homeworkId = 1L;
        
        // 执行测试
        Result<Void> result = homeworkController.deleteHomework(homeworkId);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("作业删除成功", result.getMsg());
        assertNull(result.getData());
        verify(homeworkService).deleteHomework(homeworkId);
    }

    @Test
    void testDeleteHomework_Exception() {
        // 准备测试数据 - 异常情况
        Long homeworkId = 1L;
        
        doThrow(new RuntimeException("删除失败")).when(homeworkService).deleteHomework(homeworkId);
        
        // 执行测试
        Result<Void> result = homeworkController.deleteHomework(homeworkId);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("作业删除失败：删除失败", result.getMsg());
        verify(homeworkService).deleteHomework(homeworkId);
    }
}
