package org.example.edusoft.utils.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class RequestHolderTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Test
    void testGetHttpServletRequest_Success() {
        // 准备测试数据
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        
        // 使用MockedStatic来模拟静态方法
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
            
            // 执行测试
            HttpServletRequest result = RequestHolder.getHttpServletRequest();
            
            // 验证结果
            assertNotNull(result);
            assertEquals(request, result);
        }
    }

    @Test
    void testGetHttpServletRequest_NullAttributes() {
        // 准备测试数据 - null的请求属性
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);
            
            // 执行测试 - 这里会抛出NullPointerException
            try {
                RequestHolder.getHttpServletRequest();
                fail("应该抛出NullPointerException");
            } catch (NullPointerException e) {
                // 预期抛出异常
            }
        }
    }

    @Test
    void testGetHttpServletRequestIpAddress_Success() {
        // 准备测试数据
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "192.168.1.1");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        
        // 使用MockedStatic来模拟静态方法
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
            
            // 执行测试
            String result = RequestHolder.getHttpServletRequestIpAddress();
            
            // 验证结果
            assertEquals("192.168.1.1", result);
        }
    }

    @Test
    void testGetHttpServletRequestIpAddress_NoForwardedHeader() {
        // 准备测试数据 - 没有x-forwarded-for头
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.2");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        
        // 使用MockedStatic来模拟静态方法
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
            
            // 执行测试
            String result = RequestHolder.getHttpServletRequestIpAddress();
            
            // 验证结果
            assertEquals("192.168.1.2", result);
        }
    }

    @Test
    void testGetHttpServletRequestIpAddress_WithRequestParameter_Success() {
        // 准备测试数据
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "192.168.1.3");
        
        // 执行测试
        String result = RequestHolder.getHttpServletRequestIpAddress(request);
        
        // 验证结果
        assertEquals("192.168.1.3", result);
    }

    @Test
    void testGetHttpServletRequestIpAddress_WithRequestParameter_UnknownHeader() {
        // 准备测试数据 - 未知的代理头
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "unknown");
        request.addHeader("Proxy-Client-IP", "192.168.1.4");
        
        // 执行测试
        String result = RequestHolder.getHttpServletRequestIpAddress(request);
        
        // 验证结果
        assertEquals("192.168.1.4", result);
    }

    @Test
    void testGetHttpServletRequestIpAddress_WithRequestParameter_RemoteAddr() {
        // 准备测试数据 - 使用远程地址
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.5");
        
        // 执行测试
        String result = RequestHolder.getHttpServletRequestIpAddress(request);
        
        // 验证结果
        assertEquals("192.168.1.5", result);
    }

    @Test
    void testGetHttpServletRequestIpAddress_WithRequestParameter_Localhost() {
        // 准备测试数据 - 本地地址
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("0:0:0:0:0:0:0:1");
        
        // 执行测试
        String result = RequestHolder.getHttpServletRequestIpAddress(request);
        
        // 验证结果 - 应该转换为127.0.0.1
        assertEquals("127.0.0.1", result);
    }

    @Test
    void testGetHttpServletRequestIpAddress_WithRequestParameter_MultipleIPs() {
        // 准备测试数据 - 多个IP地址
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "192.168.1.6,192.168.1.7,192.168.1.8");
        
        // 执行测试
        String result = RequestHolder.getHttpServletRequestIpAddress(request);
        
        // 验证结果 - 应该返回第一个IP
        assertEquals("192.168.1.6", result);
    }
}

