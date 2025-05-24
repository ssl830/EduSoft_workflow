package org.example.edusoft.controller.record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.example.edusoft.service.record.RecordService;
import org.example.edusoft.common.Result;
import org.example.edusoft.entity.record.*;
import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/record")
public class RecordController {
    
    @Autowired
    private RecordService recordService;
    
    @GetMapping("/study")
    public Result<List<StudyRecord>> getStudyRecords() {
        Long studentId = StpUtil.getLoginIdAsLong();
        return Result.success(recordService.getStudyRecords(studentId));
    }
    
    @GetMapping("/practice")
    public Result<List<PracticeRecord>> getPracticeRecords() {
        Long studentId = StpUtil.getLoginIdAsLong();
        return Result.success(recordService.getPracticeRecords(studentId));
    }
    
    @GetMapping("/export")
    public void exportRecords(HttpServletResponse response) {
        try {
            Long studentId = StpUtil.getLoginIdAsLong();
            byte[] data = recordService.exportRecordsToExcel(studentId);
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=learning_records.xlsx");
            response.getOutputStream().write(data);
        } catch (IOException e) {
            throw new RuntimeException("导出文件失败", e);
        }
    }

    @GetMapping("/practice/{practiceId}/report")
    public Result<Map<String, Object>> getPracticeReport(@PathVariable Long practiceId) {
        Long studentId = StpUtil.getLoginIdAsLong();
        return Result.success(recordService.getPracticeReport(practiceId, studentId));
    }
    
    @GetMapping("/practice/export-report/{practiceId}")
    public void exportPracticeReport(@PathVariable Long practiceId, HttpServletResponse response) {
        try {
            Long studentId = StpUtil.getLoginIdAsLong();
            Map<String, Object> reportData = recordService.getPracticeReport(practiceId, studentId);
            
            // 生成PDF报告
            byte[] pdfData = recordService.generatePracticeReportPdf(reportData);
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=practice_report.pdf");
            response.getOutputStream().write(pdfData);
        } catch (IOException e) {
            throw new RuntimeException("导出报告失败", e);
        }
    }
}
