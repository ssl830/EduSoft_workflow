package org.example.edusoft.controller.file;

import org.example.edusoft.common.domain.Result;
import org.example.edusoft.common.dto.file.FileQueryRequest;
import org.example.edusoft.common.dto.file.FileResponseDTO;
import org.example.edusoft.entity.file.FileAccessDTO;
import org.example.edusoft.exception.BusinessException;
import org.example.edusoft.service.file.FileDownloadService;
import org.example.edusoft.service.file.FileUpload;
import org.example.edusoft.service.file.FilePreviewService;
import org.example.edusoft.service.file.FileQueryService;
import org.example.edusoft.service.file.FolderService;
import org.example.edusoft.service.file.FileAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileDownloadService fileDownloadService;

    @Mock
    private FileUpload fileUploadService;

    @Mock
    private FileQueryService fileQueryService;

    @Mock
    private FolderService folderService;

    @Mock
    private FilePreviewService filePreviewService;

    @Mock
    private FileAccessService fileAccessService;

    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setUp() {
        // 重置所有mock对象
        reset(fileDownloadService, fileUploadService, fileQueryService, 
              folderService, filePreviewService, fileAccessService);
    }

    // ==================== getUserRootFolders 方法测试 ====================

    @Test
    void testGetUserRootFolders_Success() {
        // 准备测试数据 - 正常情况
        Map<String, Long> request = new HashMap<>();
        request.put("userId", 1L);
        
        List<FileResponseDTO> expectedFolders = Arrays.asList(
            new FileResponseDTO(), new FileResponseDTO()
        );
        
        when(fileQueryService.getAllFilesByUserId(1L)).thenReturn(expectedFolders);
        
        // 执行测试
        Result<List<FileResponseDTO>> result = fileController.getUserRootFolders(request);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("获取用户文件成功", result.getMsg());
        assertEquals(expectedFolders, result.getData());
        verify(fileQueryService).getAllFilesByUserId(1L);
    }

    @Test
    void testGetUserRootFolders_NullUserId() {
        // 准备测试数据 - 异常情况：null用户ID
        Map<String, Long> request = new HashMap<>();
        request.put("userId", null);
        
        // 执行测试
        Result<List<FileResponseDTO>> result = fileController.getUserRootFolders(request);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("用户ID不能为空", result.getMsg());
        verify(fileQueryService, never()).getAllFilesByUserId(any());
    }

    // ==================== getFilesByUserAndCourse 方法测试 ====================

    @Test
    void testGetFilesByUserAndCourse_Success() {
        // 准备测试数据 - 正常情况
        Long courseId = 1L;
        FileQueryRequest request = new FileQueryRequest();
        request.setUserId(1L);
        request.setCourseId(1L);
        request.setChapter(1L);
        request.setType("PDF");
        request.setTitle("测试文件");
        request.setIsTeacher(false);
        
        List<FileResponseDTO> expectedFiles = Arrays.asList(new FileResponseDTO());
        
        when(fileQueryService.getFilesByUserandCourseWithFilter(1L, 1L, "测试文件", "PDF", 1L, false))
            .thenReturn(expectedFiles);
        
        // 执行测试
        Result<List<FileResponseDTO>> result = fileController.getFilesByUserAndCourse(courseId, request);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("获取用户课程文件成功", result.getMsg());
        assertEquals(expectedFiles, result.getData());
        verify(fileQueryService).getFilesByUserandCourseWithFilter(1L, 1L, "测试文件", "PDF", 1L, false);
    }

    @Test
    void testGetFilesByUserAndCourse_NullUserId() {
        // 准备测试数据 - 异常情况：null用户ID
        Long courseId = 1L;
        FileQueryRequest request = new FileQueryRequest();
        request.setUserId(null);
        request.setCourseId(1L);
        
        // 执行测试
        Result<List<FileResponseDTO>> result = fileController.getFilesByUserAndCourse(courseId, request);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("用户ID和课程ID不能为空", result.getMsg());
        verify(fileQueryService, never()).getFilesByUserandCourseWithFilter(any(), any(), any(), any(), any(), any());
    }

    // ==================== uploadFile 方法测试 ====================

    @Test
    void testUploadFile_Success() {
        // 准备测试数据 - 正常情况
        Long courseId = 1L;
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());
        String title = "测试文件";
        Long sectionId = 1L;
        Long uploaderId = 1L;
        String visibility = "PUBLIC";
        String type = "PDF";
        Boolean uploadToKnowledgeBase = false;
        
        when(fileUploadService.uploadFile(file, title, courseId, sectionId, visibility, uploaderId, type, uploadToKnowledgeBase))
            .thenReturn(Result.ok("上传成功"));
        
        // 执行测试
        Result<?> result = fileController.uploadFile(courseId, file, title, sectionId, uploaderId, visibility, type, uploadToKnowledgeBase);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("上传成功", result.getMsg());
        verify(fileUploadService).uploadFile(file, title, courseId, sectionId, visibility, uploaderId, type, uploadToKnowledgeBase);
    }

    @Test
    void testUploadFile_NullFile() {
        // 准备测试数据 - 异常情况：null文件
        Long courseId = 1L;
        MultipartFile file = null;
        String title = "测试文件";
        
        // 执行测试
        Result<?> result = fileController.uploadFile(courseId, file, title, null, null, "PUBLIC", null, false);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("上传文件不能为空", result.getMsg());
        verify(fileUploadService, never()).uploadFile(any(MultipartFile.class), any(String.class), any(Long.class), any(Long.class), any(String.class), any(Long.class), any(String.class), any(Boolean.class));
    }

    @Test
    void testUploadFile_EmptyTitle() {
        // 准备测试数据 - 异常情况：空标题
        Long courseId = 1L;
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());
        String title = "";
        
        // 执行测试
        Result<?> result = fileController.uploadFile(courseId, file, title, null, null, "PUBLIC", null, false);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("文件标题不能为空", result.getMsg());
        verify(fileUploadService, never()).uploadFile(any(MultipartFile.class), any(String.class), any(Long.class), any(Long.class), any(String.class), any(Long.class), any(String.class), any(Boolean.class));
    }

    @Test
    void testUploadFile_InvalidVisibility() {
        // 准备测试数据 - 异常情况：无效的可见性
        Long courseId = 1L;
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());
        String title = "测试文件";
        String visibility = "INVALID";
        
        // 执行测试
        Result<?> result = fileController.uploadFile(courseId, file, title, null, null, visibility, null, false);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("visibility 参数非法", result.getMsg());
        verify(fileUploadService, never()).uploadFile(any(MultipartFile.class), any(String.class), any(Long.class), any(Long.class), any(String.class), any(Long.class), any(String.class), any(Boolean.class));
    }

    // ==================== downloadResource 方法测试 ====================

    @Test
    void testDownloadResource_Success() {
        // 准备测试数据 - 正常情况
        String resourceId = "123";
        FileAccessDTO expectedAccess = FileAccessDTO.builder()
            .url("https://example.com/test.pdf")
            .fileName("test.pdf")
            .fileType("pdf")
            .expiresIn(3600)
            .build();
        
        when(fileAccessService.getDownloadUrl(123L)).thenReturn(expectedAccess);
        
        // 执行测试
        Result<FileAccessDTO> result = fileController.downloadResource(resourceId);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("获取下载链接成功", result.getMsg());
        assertEquals(expectedAccess, result.getData());
        verify(fileAccessService).getDownloadUrl(123L);
    }

    @Test
    void testDownloadResource_InvalidFormat() {
        // 准备测试数据 - 异常情况：无效的resourceId格式
        String resourceId = "invalid";
        
        // 执行测试
        Result<FileAccessDTO> result = fileController.downloadResource(resourceId);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("无效的 resourceId 格式", result.getMsg());
        verify(fileAccessService, never()).getDownloadUrl(any());
    }

    @Test
    void testDownloadResource_BusinessException() {
        // 准备测试数据 - 异常情况：业务异常
        String resourceId = "123";
        
        when(fileAccessService.getDownloadUrl(123L)).thenThrow(new BusinessException("文件不存在"));
        
        // 执行测试
        Result<FileAccessDTO> result = fileController.downloadResource(resourceId);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("文件不存在", result.getMsg());
        verify(fileAccessService).getDownloadUrl(123L);
    }

    // ==================== previewResource 方法测试 ====================

    @Test
    void testPreviewResource_Success() {
        // 准备测试数据 - 正常情况
        String resourceId = "123";
        FileAccessDTO expectedAccess = FileAccessDTO.builder()
            .url("https://example.com/test.pdf")
            .fileName("test.pdf")
            .fileType("pdf")
            .expiresIn(3600)
            .build();
        
        when(fileAccessService.getPreviewUrl(123L)).thenReturn(expectedAccess);
        
        // 执行测试
        Result<FileAccessDTO> result = fileController.previewResource(resourceId);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("获取预览链接成功", result.getMsg());
        assertEquals(expectedAccess, result.getData());
        verify(fileAccessService).getPreviewUrl(123L);
    }

    @Test
    void testPreviewResource_InvalidFormat() {
        // 准备测试数据 - 异常情况：无效的resourceId格式
        String resourceId = "invalid";
        
        // 执行测试
        Result<FileAccessDTO> result = fileController.previewResource(resourceId);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("无效的 resourceId 格式", result.getMsg());
        verify(fileAccessService, never()).getPreviewUrl(any());
    }

    // ==================== getPreviewUrl 方法测试 ====================

    @Test
    void testGetPreviewUrl_Success() {
        // 准备测试数据 - 正常情况
        String resourceId = "123";
        FileAccessDTO expectedAccess = FileAccessDTO.builder()
            .url("https://example.com/test.pdf")
            .fileName("test.pdf")
            .fileType("pdf")
            .expiresIn(3600)
            .build();
        
        when(fileAccessService.getPreviewUrl(123L)).thenReturn(expectedAccess);
        
        // 执行测试
        Result<FileAccessDTO> result = fileController.getPreviewUrl(resourceId);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("获取预览链接成功", result.getMsg());
        assertEquals(expectedAccess, result.getData());
        verify(fileAccessService).getPreviewUrl(123L);
    }

    @Test
    void testGetPreviewUrl_InvalidFormat() {
        // 准备测试数据 - 异常情况：无效的resourceId格式
        String resourceId = "invalid";
        
        // 执行测试
        Result<FileAccessDTO> result = fileController.getPreviewUrl(resourceId);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("无效的 resourceId 格式", result.getMsg());
        verify(fileAccessService, never()).getPreviewUrl(any());
    }
}
