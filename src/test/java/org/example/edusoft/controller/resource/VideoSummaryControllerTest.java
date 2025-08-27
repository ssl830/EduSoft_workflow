package org.example.edusoft.controller.resource;

import org.example.edusoft.common.Result;
import org.example.edusoft.dto.resource.VideoSummaryDetailDTO;
import org.example.edusoft.service.resource.VideoSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoSummaryControllerTest {

    @Mock
    private VideoSummaryService videoSummaryService;

    @InjectMocks
    private VideoSummaryController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // generateSummary
    @Test
    void testGenerateSummary_Success() {
        VideoSummaryDetailDTO summary = new VideoSummaryDetailDTO();
        when(videoSummaryService.generateSummaryForResource(1L)).thenReturn(summary);

        Result<VideoSummaryDetailDTO> result = controller.generateSummary(1L);
        assertEquals(200, result.getCode());
        assertEquals(summary, result.getData());
    }

    @Test
    void testGenerateSummary_Failure() {
        when(videoSummaryService.generateSummaryForResource(1L)).thenThrow(new RuntimeException("fail"));
        Result<VideoSummaryDetailDTO> result = controller.generateSummary(1L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("生成视频摘要失败"));
    }

    // getSummaryByResourceId
    @Test
    void testGetSummaryByResourceId_Success() {
        VideoSummaryDetailDTO summary = new VideoSummaryDetailDTO();
        when(videoSummaryService.getSummaryByResourceId(2L)).thenReturn(summary);

        Result<VideoSummaryDetailDTO> result = controller.getSummaryByResourceId(2L);
        assertEquals(200, result.getCode());
        assertEquals(summary, result.getData());
    }

    @Test
    void testGetSummaryByResourceId_NotFound() {
        when(videoSummaryService.getSummaryByResourceId(2L)).thenReturn(null);
        Result<VideoSummaryDetailDTO> result = controller.getSummaryByResourceId(2L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("不存在"));
    }

    @Test
    void testGetSummaryByResourceId_Failure() {
        when(videoSummaryService.getSummaryByResourceId(2L)).thenThrow(new RuntimeException("fail"));
        Result<VideoSummaryDetailDTO> result = controller.getSummaryByResourceId(2L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("获取视频摘要失败"));
    }

    // getSummaryById
    @Test
    void testGetSummaryById_Success() {
        VideoSummaryDetailDTO summary = new VideoSummaryDetailDTO();
        when(videoSummaryService.getSummaryById(3L)).thenReturn(summary);

        Result<VideoSummaryDetailDTO> result = controller.getSummaryById(3L);
        assertEquals(200, result.getCode());
        assertEquals(summary, result.getData());
    }

    @Test
    void testGetSummaryById_NotFound() {
        when(videoSummaryService.getSummaryById(3L)).thenReturn(null);
        Result<VideoSummaryDetailDTO> result = controller.getSummaryById(3L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("不存在"));
    }

    @Test
    void testGetSummaryById_Failure() {
        when(videoSummaryService.getSummaryById(3L)).thenThrow(new RuntimeException("fail"));
        Result<VideoSummaryDetailDTO> result = controller.getSummaryById(3L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("获取视频摘要失败"));
    }

    // regenerateSummary
    @Test
    void testRegenerateSummary_Success() {
        VideoSummaryDetailDTO summary = new VideoSummaryDetailDTO();
        when(videoSummaryService.regenerateSummary(4L)).thenReturn(summary);

        Result<VideoSummaryDetailDTO> result = controller.regenerateSummary(4L);
        assertEquals(200, result.getCode());
        assertEquals(summary, result.getData());
    }

    @Test
    void testRegenerateSummary_Failure() {
        when(videoSummaryService.regenerateSummary(4L)).thenThrow(new RuntimeException("fail"));
        Result<VideoSummaryDetailDTO> result = controller.regenerateSummary(4L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("重新生成视频摘要失败"));
    }

    // deleteSummary
    @Test
    void testDeleteSummary_Success() {
        when(videoSummaryService.deleteSummary(5L)).thenReturn(true);
        Result<String> result = controller.deleteSummary(5L);
        assertEquals(200, result.getCode());
        assertEquals("删除成功", result.getData());
    }

    @Test
    void testDeleteSummary_Failure() {
        when(videoSummaryService.deleteSummary(5L)).thenReturn(false);
        Result<String> result = controller.deleteSummary(5L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("删除失败"));
    }

    @Test
    void testDeleteSummary_Exception() {
        when(videoSummaryService.deleteSummary(5L)).thenThrow(new RuntimeException("fail"));
        Result<String> result = controller.deleteSummary(5L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("删除视频摘要失败"));
    }

    // deleteSummaryByResourceId
    @Test
    void testDeleteSummaryByResourceId_Success() {
        when(videoSummaryService.deleteSummaryByResourceId(6L)).thenReturn(true);
        Result<String> result = controller.deleteSummaryByResourceId(6L);
        assertEquals(200, result.getCode());
        assertEquals("删除成功", result.getData());
    }

    @Test
    void testDeleteSummaryByResourceId_Failure() {
        when(videoSummaryService.deleteSummaryByResourceId(6L)).thenReturn(false);
        Result<String> result = controller.deleteSummaryByResourceId(6L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("删除失败"));
    }

    @Test
    void testDeleteSummaryByResourceId_Exception() {
        when(videoSummaryService.deleteSummaryByResourceId(6L)).thenThrow(new RuntimeException("fail"));
        Result<String> result = controller.deleteSummaryByResourceId(6L);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("删除视频摘要失败"));
    }
}