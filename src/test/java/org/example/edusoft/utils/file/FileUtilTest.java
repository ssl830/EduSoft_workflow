package org.example.edusoft.utils.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

class FileUtilTest {

    @Test
    void testIsFileAllowed_ValidFile() {
        // 准备测试数据 - 有效的文件后缀
        String fileName = "test.pdf";
        
        // 执行测试
        boolean result = FileUtil.isFileAllowed(fileName);
        
        // 验证结果
        assertTrue(result);
    }

    @Test
    void testIsFileAllowed_InvalidFile() {
        // 准备测试数据 - 无效的文件后缀
        String fileName = "test.invalid";
        
        // 执行测试
        boolean result = FileUtil.isFileAllowed(fileName);
        
        // 验证结果
        assertFalse(result);
    }

    @Test
    void testIsFileAllowed_WithCustomArray_ValidFile() {
        // 准备测试数据 - 自定义文件类型数组，包含有效文件
        String fileName = "test.txt";
        String[] allowedTypes = {"txt", "pdf", "doc"};
        
        // 执行测试
        boolean result = FileUtil.isFileAllowed(fileName, allowedTypes);
        
        // 验证结果
        assertTrue(result);
    }

    @Test
    void testIsFileAllowed_WithCustomArray_InvalidFile() {
        // 准备测试数据 - 自定义文件类型数组，不包含该文件
        String fileName = "test.jpg";
        String[] allowedTypes = {"txt", "pdf", "doc"};
        
        // 执行测试
        boolean result = FileUtil.isFileAllowed(fileName, allowedTypes);
        
        // 验证结果
        assertFalse(result);
    }

    @Test
    void testIsImg_ValidImage() {
        // 准备测试数据 - 有效的图片文件
        String suffix = "png";
        
        // 执行测试
        boolean result = FileUtil.isImg(suffix);
        
        // 验证结果
        assertTrue(result);
    }

    @Test
    void testIsImg_InvalidImage() {
        // 准备测试数据 - 无效的图片文件
        String suffix = "txt";
        
        // 执行测试
        boolean result = FileUtil.isImg(suffix);
        
        // 验证结果
        assertFalse(result);
    }

    @Test
    void testIsCode_ValidCodeFile() {
        // 准备测试数据 - 有效的代码文件
        String suffix = "java";
        
        // 执行测试
        boolean result = FileUtil.isCode(suffix);
        
        // 验证结果
        assertTrue(result);
    }

    @Test
    void testIsCode_InvalidCodeFile() {
        // 准备测试数据 - 无效的代码文件
        String suffix = "pdf";
        
        // 执行测试
        boolean result = FileUtil.isCode(suffix);
        
        // 验证结果
        assertFalse(result);
    }

    @Test
    void testGetContentType_PdfFile() {
        // 准备测试数据 - PDF文件
        String filenameExtension = ".pdf";
        
        // 执行测试
        String result = FileUtil.getContentType(filenameExtension);
        
        // 验证结果
        assertEquals("application/pdf", result);
    }

    @Test
    void testGetContentType_UnknownFile() {
        // 准备测试数据 - 未知文件类型
        String filenameExtension = ".unknown";
        
        // 执行测试
        String result = FileUtil.getContentType(filenameExtension);
        
        // 验证结果 - 应该返回默认值
        assertEquals("image/jpg", result);
    }

    @Test
    void testGetFileName_ValidFileName() {
        // 准备测试数据 - 有效的文件名
        String fileName = "test.pdf";
        
        // 执行测试
        String result = FileUtil.getFileName(fileName);
        
        // 验证结果
        assertEquals("test", result);
    }

    @Test
    void testGetFileName_FileNameWithoutExtension() {
        // 准备测试数据 - 没有扩展名的文件名
        String fileName = "test";
        
        // 执行测试 - 这里会抛出异常
        try {
            FileUtil.getFileName(fileName);
            fail("应该抛出StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException e) {
            // 预期抛出异常
        }
    }

    @Test
    void testGetFileSuffix_ValidFileName() {
        // 准备测试数据 - 有效的文件名
        String fileName = "test.PDF";
        
        // 执行测试
        String result = FileUtil.getFileSuffix(fileName);
        
        // 验证结果 - 应该转换为小写
        assertEquals("pdf", result);
    }

    @Test
    void testGetFileSuffix_FileNameWithoutExtension() {
        // 准备测试数据 - 没有扩展名的文件名
        String fileName = "test";
        
        // 执行测试 - 这里会抛出异常
        try {
            FileUtil.getFileSuffix(fileName);
            fail("应该抛出StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException e) {
            // 预期抛出异常
        }
    }

    @Test
    void testGetFileBaseName_FileNameWithNumber() {
        // 准备测试数据 - 带数字的文件名
        String fileName = "test(1).pdf";
        
        // 执行测试
        String result = FileUtil.getFileBaseName(fileName);
        
        // 验证结果
        assertEquals("test.pdf", result);
    }

    @Test
    void testGetFileBaseName_FileNameWithoutNumber() {
        // 准备测试数据 - 不带数字的文件名
        String fileName = "test.pdf";
        
        // 执行测试
        String result = FileUtil.getFileBaseName(fileName);
        
        // 验证结果
        assertEquals("test.pdf", result);
    }

    @Test
    void testGetFileBaseName_NullFileName() {
        // 准备测试数据 - null文件名
        String fileName = null;
        
        // 执行测试
        String result = FileUtil.getFileBaseName(fileName);
        
        // 验证结果
        assertNull(result);
    }

    @Test
    void testGetFileBaseName_EmptyFileName() {
        // 准备测试数据 - 空文件名
        String fileName = "";
        
        // 执行测试
        String result = FileUtil.getFileBaseName(fileName);
        
        // 验证结果
        assertEquals("", result);
    }
}

