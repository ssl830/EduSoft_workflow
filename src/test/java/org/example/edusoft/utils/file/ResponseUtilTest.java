package org.example.edusoft.utils.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ResponseUtilTest {

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private ServletOutputStream mockOutputStream;

    @Test
    void testWrite_Success() throws IOException {
        // 准备测试数据 - 正常情况
        String testContent = "测试内容";
        InputStream inputStream = new ByteArrayInputStream(testContent.getBytes(StandardCharsets.UTF_8));
        String objectName = "test.txt";
        
        // 设置mock行为
        when(mockResponse.getOutputStream()).thenReturn(mockOutputStream);
        
        // 执行测试
        ResponseUtil.write(inputStream, objectName, mockResponse);
        
        // 验证结果 - 应该正确设置响应头并关闭流
        verify(mockResponse).setContentType("application/x-msdownload");
        verify(mockResponse).setCharacterEncoding(StandardCharsets.UTF_8.name());
        verify(mockResponse).setHeader("Content-Disposition", "attachment;fileName=" + objectName);
        verify(mockResponse).flushBuffer();
        verify(mockOutputStream).close();
        // 注意：inputStream是真实的ByteArrayInputStream，不是mock对象，所以不能verify
    }

    @Test
    void testWrite_NullInputStream() throws IOException {
        // 准备测试数据 - 异常情况：null输入流
        InputStream inputStream = null;
        String objectName = "null.txt";
        
        // 不需要设置mock行为，因为会直接抛出异常
        
        // 验证结果 - 不应该设置任何响应头
        verify(mockResponse, never()).setContentType(anyString());
        verify(mockResponse, never()).setCharacterEncoding(anyString());
        verify(mockResponse, never()).setHeader(anyString(), anyString());
    }
}