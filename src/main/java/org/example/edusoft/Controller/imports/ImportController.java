package org.example.edusoft.controller.imports;

import org.example.edusoft.entity.imports.ImportRecord;
import org.example.edusoft.service.imports.ImportService;
import org.example.edusoft.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    @Autowired
    private ImportService importService;

    @PostMapping("/students/file")
    public Result<ImportRecord> importStudentsByFile(
            @RequestParam("classId") Long classId,
            @RequestParam("operatorId") Long operatorId,
            @RequestParam("file") MultipartFile file) {
        try {
            ImportRecord record = importService.importStudentsByFile(classId, operatorId, file);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @PostMapping("/students/manual")
    public Result<ImportRecord> importStudentsManually(
            @RequestParam("classId") Long classId,
            @RequestParam("operatorId") Long operatorId,
            @RequestBody List<Long> studentIds) {
        try {
            ImportRecord record = importService.importStudentsManually(classId, operatorId, studentIds);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/records/{classId}")
    public Result<List<ImportRecord>> getImportRecords(@PathVariable Long classId) {
        try {
            List<ImportRecord> records = importService.getImportRecords(classId);
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("获取导入记录失败：" + e.getMessage());
        }
    }

    @GetMapping("/record/{id}")
    public Result<ImportRecord> getImportRecord(@PathVariable Long id) {
        try {
            ImportRecord record = importService.getImportRecord(id);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error("获取导入记录详情失败：" + e.getMessage());
        }
    }
} 