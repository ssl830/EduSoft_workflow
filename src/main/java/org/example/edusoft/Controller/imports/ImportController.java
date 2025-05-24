package org.example.edusoft.controller.imports;

import org.example.edusoft.entity.imports.ImportRecord;
import org.example.edusoft.service.imports.ImportService;
import org.example.edusoft.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    @Autowired
    private ImportService importService;

    @PostMapping("/students")
    public Result<ImportRecord> importStudents(
            @RequestParam("classId") Long classId,
            @RequestParam("operatorId") Long operatorId,
            @RequestParam("importType") String importType,
            @RequestBody List<Map<String, Object>> studentData) {
        try {
            ImportRecord record = importService.importStudents(classId, operatorId, importType, studentData);
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