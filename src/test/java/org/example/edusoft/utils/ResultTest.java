package org.example.edusoft.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void testSuccess_WithData() {
        // 准备测试数据
        String testData = "测试数据";
        
        // 执行测试
        Result<String> result = Result.success(testData);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals(testData, result.getData());
    }

    @Test
    void testSuccess_WithNullData() {
        // 准备测试数据 - null数据
        String testData = null;
        
        // 执行测试
        Result<String> result = Result.success(testData);
        
        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testError_WithMessage() {
        // 准备测试数据
        String errorMessage = "测试错误信息";
        
        // 执行测试
        Result<String> result = Result.error(errorMessage);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals(errorMessage, result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testError_WithEmptyMessage() {
        // 准备测试数据 - 空消息
        String errorMessage = "";
        
        // 执行测试
        Result<String> result = Result.error(errorMessage);
        
        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals(errorMessage, result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testSettersAndGetters() {
        // 准备测试数据
        Result<String> result = new Result<>();
        Integer code = 404;
        String message = "自定义消息";
        String data = "自定义数据";
        
        // 执行测试
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        
        // 验证结果
        assertEquals(code, result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    void testSettersAndGetters_WithNullValues() {
        // 准备测试数据 - null值
        Result<String> result = new Result<>();
        
        // 执行测试
        result.setCode(null);
        result.setMessage(null);
        result.setData(null);
        
        // 验证结果
        assertNull(result.getCode());
        assertNull(result.getMessage());
        assertNull(result.getData());
    }
}

