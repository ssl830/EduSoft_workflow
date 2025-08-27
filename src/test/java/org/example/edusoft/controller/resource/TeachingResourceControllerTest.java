package org.example.edusoft.controller.resource;

import org.example.edusoft.common.domain.Result;
import org.example.edusoft.entity.resource.TeachingResource;
import org.example.edusoft.entity.resource.LearningProgress;
import org.example.edusoft.entity.resource.ResourceProgressDTO;
import org.example.edusoft.entity.resource.ChapterResourceRequest;
import org.example.edusoft.service.resource.TeachingResourceService;
import org.example.edusoft.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeachingResourceControllerTest {

    @Mock
    private TeachingResourceService resourceService;

    @InjectMocks
    private TeachingResourceController controller;

    @BeforeEach
    void setUp() {
        // 重置所有mock对象
        reset(resourceService);
    }

    // uploadResource
    @Test
    void testUploadResource_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", new byte[0]);
        Result<TeachingResource> result = controller.uploadResource(file, 1L, 2L, "chapter", "title", "desc", 3L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("不能为空"));
    }

    @Test
    void testUploadResource_TooLarge() {
        byte[] largeContent = new byte[(int)(600L * 1024 * 1024)];
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", largeContent);
        Result<TeachingResource> result = controller.uploadResource(file, 1L, 2L, "chapter", "title", "desc", 3L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("不能超过500MB"));
    }

    @Test
    void testUploadResource_NotVideo() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test content".getBytes());
        Result<TeachingResource> result = controller.uploadResource(file, 1L, 2L, "chapter", "title", "desc", 3L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("请上传视频文件"));
    }

    @Test
    void testUploadResource_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "test content".getBytes());
        TeachingResource resource = new TeachingResource();
        resource.setId(1L);
        when(resourceService.uploadResource(any(), any(), any(), any(), any(), any(), any())).thenReturn(resource);
        Result<TeachingResource> result = controller.uploadResource(file, 1L, 2L, "chapter", "title", "desc", 3L);
        assertEquals(200, result.getCode());
        assertEquals(resource, result.getData());
    }

    @Test
    void testUploadResource_Exception() {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "test content".getBytes());
        when(resourceService.uploadResource(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("fail"));
        Result<TeachingResource> result = controller.uploadResource(file, 1L, 2L, "chapter", "title", "desc", 3L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("资源上传失败"));
    }

    // getResource
    @Test
    void testGetResource_Success() {
        TeachingResource resource = new TeachingResource();
        when(resourceService.getResource(1L)).thenReturn(resource);
        Result<TeachingResource> result = controller.getResource(1L);
        assertEquals(200, result.getCode());
        assertEquals(resource, result.getData());
    }

    @Test
    void testGetResource_NotFound() {
        when(resourceService.getResource(1L)).thenReturn(null);
        Result<TeachingResource> result = controller.getResource(1L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("资源不存在"));
    }

    @Test
    void testGetResource_Exception() {
        when(resourceService.getResource(1L)).thenThrow(new RuntimeException("fail"));
        Result<TeachingResource> result = controller.getResource(1L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("获取资源失败"));
    }

    // getResourcesByCourse
    @Test
    void testGetResourcesByCourse_Success() {
        Map<Long, List<TeachingResource>> map = new HashMap<>();
        when(resourceService.getResourcesByCourse(1L)).thenReturn(map);
        Result<Map<Long, List<TeachingResource>>> result = controller.getResourcesByCourse(1L);
        assertEquals(200, result.getCode());
        assertEquals(map, result.getData());
    }

    @Test
    void testGetResourcesByCourse_Exception() {
        when(resourceService.getResourcesByCourse(1L)).thenThrow(new RuntimeException("fail"));
        Result<Map<Long, List<TeachingResource>>> result = controller.getResourcesByCourse(1L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("获取课程资源列表失败"));
    }

    // getResourcesByChapter
    @Test
    void testGetResourcesByChapter_Success() {
        List<TeachingResource> list = new ArrayList<>();
        when(resourceService.getResourcesByChapter(1L, 2L)).thenReturn(list);
        Result<List<TeachingResource>> result = controller.getResourcesByChapter(1L, 2L);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    @Test
    void testGetResourcesByChapter_Exception() {
        when(resourceService.getResourcesByChapter(1L, 2L)).thenThrow(new RuntimeException("fail"));
        Result<List<TeachingResource>> result = controller.getResourcesByChapter(1L, 2L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("获取章节资源列表失败"));
    }

    // deleteResource
    @Test
    void testDeleteResource_Success() {
        when(resourceService.deleteResource(1L, 2L)).thenReturn(true);
        Result<Void> result = controller.deleteResource(1L, 2L);
        assertEquals(200, result.getCode());
    }

    @Test
    void testDeleteResource_NotFound() {
        when(resourceService.deleteResource(1L, 2L)).thenReturn(false);
        Result<Void> result = controller.deleteResource(1L, 2L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("不存在"));
    }

    @Test
    void testDeleteResource_Exception() {
        when(resourceService.deleteResource(1L, 2L)).thenThrow(new RuntimeException("fail"));
        Result<Void> result = controller.deleteResource(1L, 2L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("删除资源失败"));
    }

    // updateProgress
    @Test
    void testUpdateProgress_Success() {
        ProgressUpdateRequest req = new ProgressUpdateRequest();
        req.setResourceId(1L);
        req.setStudentId(2L);
        req.setProgress(50);
        req.setPosition(200);

        TeachingResource resource = new TeachingResource();
        when(resourceService.getResource(1L)).thenReturn(resource);
        LearningProgress lp = new LearningProgress();
        when(resourceService.updateProgress(1L, 2L, 50, 200)).thenReturn(lp);

        Result<LearningProgress> result = controller.updateProgress(req);
        assertEquals(200, result.getCode());
        assertEquals(lp, result.getData());
    }

    @Test
    void testUpdateProgress_ResourceNotFound() {
        ProgressUpdateRequest req = new ProgressUpdateRequest();
        req.setResourceId(1L);
        req.setStudentId(2L);
        req.setProgress(50);
        req.setPosition(200);

        when(resourceService.getResource(1L)).thenReturn(null);
        Result<LearningProgress> result = controller.updateProgress(req);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("教学资源不存在"));
    }

    @Test
    void testUpdateProgress_BusinessException() {
        ProgressUpdateRequest req = new ProgressUpdateRequest();
        req.setResourceId(1L);
        req.setStudentId(2L);
        req.setProgress(50);
        req.setPosition(200);

        TeachingResource resource = new TeachingResource();
        when(resourceService.getResource(1L)).thenReturn(resource);
        when(resourceService.updateProgress(1L, 2L, 50, 200)).thenThrow(new BusinessException("业务异常"));
        Result<LearningProgress> result = controller.updateProgress(req);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("业务异常"));
    }

    @Test
    void testUpdateProgress_Exception() {
        ProgressUpdateRequest req = new ProgressUpdateRequest();
        req.setResourceId(1L);
        req.setStudentId(2L);
        req.setProgress(50);
        req.setPosition(200);

        TeachingResource resource = new TeachingResource();
        when(resourceService.getResource(1L)).thenReturn(resource);
        when(resourceService.updateProgress(1L, 2L, 50, 200)).thenThrow(new RuntimeException("fail"));
        Result<LearningProgress> result = controller.updateProgress(req);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("更新学习进度失败"));
    }

    // getProgress
    @Test
    void testGetProgress_Success() {
        LearningProgress lp = new LearningProgress();
        when(resourceService.getProgress(1L, 2L)).thenReturn(lp);

        Result<LearningProgress> result = controller.getProgress(1L, 2L);
        assertEquals(200, result.getCode());
        assertEquals(lp, result.getData());
    }

    @Test
    void testGetProgress_NotFound() {
        when(resourceService.getProgress(1L, 2L)).thenReturn(null);

        Result<LearningProgress> result = controller.getProgress(1L, 2L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("未找到学习进度记录"));
    }

    @Test
    void testGetProgress_Exception() {
        when(resourceService.getProgress(1L, 2L)).thenThrow(new RuntimeException("fail"));
        Result<LearningProgress> result = controller.getProgress(1L, 2L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("获取学习进度失败"));
    }

    // getResourceUrl
    @Test
    void testGetResourceUrl_Success() {
        TeachingResource resource = new TeachingResource();
        when(resourceService.getResource(1L)).thenReturn(resource);
        when(resourceService.getSignedResourceUrl(1L)).thenReturn("url");
        Result<String> result = controller.getResourceUrl(1L);
        assertEquals(200, result.getCode());
        assertEquals("url", result.getData());
    }

    @Test
    void testGetResourceUrl_ResourceNotFound() {
        when(resourceService.getResource(1L)).thenReturn(null);
        Result<String> result = controller.getResourceUrl(1L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("资源不存在"));
    }

    @Test
    void testGetResourceUrl_UrlNull() {
        TeachingResource resource = new TeachingResource();
        when(resourceService.getResource(1L)).thenReturn(resource);
        when(resourceService.getSignedResourceUrl(1L)).thenReturn(null);

        Result<String> result = controller.getResourceUrl(1L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("获取资源访问链接失败"));
    }

    @Test
    void testGetResourceUrl_BusinessException() {
        TeachingResource resource = new TeachingResource();
        when(resourceService.getResource(1L)).thenReturn(resource);
        when(resourceService.getSignedResourceUrl(1L)).thenThrow(new BusinessException("业务异常"));
        Result<String> result = controller.getResourceUrl(1L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("业务异常"));
    }

    @Test
    void testGetResourceUrl_Exception() {
        TeachingResource resource = new TeachingResource();
        when(resourceService.getResource(1L)).thenReturn(resource);
        when(resourceService.getSignedResourceUrl(1L)).thenThrow(new RuntimeException("fail"));

        Result<String> result = controller.getResourceUrl(1L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("获取资源访问链接失败"));
    }

    // getCourseResourcesWithProgress
    @Test
    void testGetCourseResourcesWithProgress_Success() {
        ChapterResourceRequest req = new ChapterResourceRequest();
        List<ResourceProgressDTO> list = new ArrayList<>();
        when(resourceService.getCourseResourcesWithProgress(anyLong(), any(), any())).thenReturn(list);
        Result<List<ResourceProgressDTO>> result = controller.getCourseResourcesWithProgress(1L, req).getBody();
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    @Test
    void testGetCourseResourcesWithProgress_Exception() {
        ChapterResourceRequest req = new ChapterResourceRequest();
        when(resourceService.getCourseResourcesWithProgress(anyLong(), any(), any()))
                .thenThrow(new RuntimeException("fail"));
        Result<List<ResourceProgressDTO>> result = controller.getCourseResourcesWithProgress(1L, req).getBody();
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("获取课程资源及进度信息失败"));
    }

    // updateResourceDuration
    @Test
    void testUpdateResourceDuration_Success() {
        TeachingResource resource = new TeachingResource();
        when(resourceService.updateResourceDuration(1L, 100)).thenReturn(resource);
        Map<String, Integer> req = new HashMap<>();
        req.put("duration", 100);
        Result<TeachingResource> result = controller.updateResourceDuration(1L, req);
        assertEquals(200, result.getCode());
        assertEquals(resource, result.getData());
    }

    @Test
    void testUpdateResourceDuration_DurationNull() {
        Map<String, Integer> req = new HashMap<>();
        Result<TeachingResource> result = controller.updateResourceDuration(1L, req);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("不能为空"));
    }

    @Test
    void testUpdateResourceDuration_Exception() {
        when(resourceService.updateResourceDuration(1L, 100)).thenThrow(new RuntimeException("fail"));
        Map<String, Integer> req = new HashMap<>();
        req.put("duration", 100);
        Result<TeachingResource> result = controller.updateResourceDuration(1L, req);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("更新视频时长失败"));
    }

}