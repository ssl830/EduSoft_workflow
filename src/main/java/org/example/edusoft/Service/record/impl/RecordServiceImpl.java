package org.example.edusoft.service.record.impl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import org.example.edusoft.service.record.RecordService;
import org.example.edusoft.mapper.record.*;
import org.example.edusoft.entity.record.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import java.time.LocalDateTime;
import com.itextpdf.io.font.PdfEncodings;  // 添加这个导入


@Service
@Transactional(readOnly = true)
public class RecordServiceImpl implements RecordService {
    
    @Autowired
    private StudyRecordMapper studyRecordMapper;
    
    @Autowired
    private PracticeRecordMapper practiceRecordMapper;
    
    @Override
    public List<StudyRecord> getStudyRecords(Long studentId) {
        return studyRecordMapper.findStudyRecords(studentId);
    }
    
    @Override
    public List<PracticeRecord> getPracticeRecords(Long studentId) {
        List<PracticeRecord> records = practiceRecordMapper.findPracticeRecords(studentId);
        for (PracticeRecord record : records) {
            record.setAnswers(practiceRecordMapper.findAnswerDetails(record.getId()));
        }
        return records;
    }
    
    @Override
    public byte[] exportRecordsToExcel(Long studentId) {
        List<StudyRecord> studyRecords = getStudyRecords(studentId);
        List<PracticeRecord> practiceRecords = getPracticeRecords(studentId);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建学习记录sheet
            Sheet studySheet = workbook.createSheet("学习记录");
            Row headerRow = studySheet.createRow(0);
            headerRow.createCell(0).setCellValue("课程");
            headerRow.createCell(1).setCellValue("章节");
            headerRow.createCell(2).setCellValue("完成状态");
            headerRow.createCell(3).setCellValue("完成时间");
            
            int rowNum = 1;
            for (StudyRecord record : studyRecords) {
                Row row = studySheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getCourseName());
                row.createCell(1).setCellValue(record.getSectionTitle());
                row.createCell(2).setCellValue(record.getCompleted() ? "已完成" : "未完成");
                row.createCell(3).setCellValue(
                    record.getCompletedAt() != null ? 
                    record.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : 
                    ""
                );
            }
            
            // 创建练习记录sheet
            Sheet practiceSheet = workbook.createSheet("练习记录");
            // ... 类似的创建练习记录sheet的代码 ...
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    @Override
    public Map<String, Object> getPracticeReport(Long practiceId, Long studentId) {
        Map<String, Object> report = new HashMap<>();
        
        // 获取练习基本信息
        PracticeRecord record = practiceRecordMapper.findPracticeRecord(practiceId, studentId);
        report.put("practiceInfo", record);
        
        // 获取班级排名
        int rank = practiceRecordMapper.getPracticeRank(practiceId, studentId);
        int totalStudents = practiceRecordMapper.getTotalStudentsInPractice(practiceId);
        
        report.put("rank", rank);
        report.put("totalStudents", totalStudents);
        report.put("percentile", ((totalStudents - rank) * 100.0) / totalStudents);
        
        // 获取得分分布
        List<Map<String, Object>> scoreDistribution = practiceRecordMapper.getScoreDistribution(practiceId);
        report.put("scoreDistribution", scoreDistribution);
        
        return report;
    }

    @Override
    public byte[] generatePracticeReportPdf(Map<String, Object> reportData) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // 使用系统字体，确保中文能显示
           String fontPath = "C:/Windows/Fonts/simsun.ttc,0";
           PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H);
            // 标题
            Paragraph title = new Paragraph("练习报告")
                .setFont(font)
                .setFontSize(20);
            document.add(title);
            
            // 基本信息
            PracticeRecord practiceInfo = (PracticeRecord) reportData.get("practiceInfo");
            document.add(new Paragraph("\n练习信息").setFont(font).setFontSize(16));
            document.add(new Paragraph("练习标题：" + practiceInfo.getPracticeTitle()).setFont(font));
            document.add(new Paragraph("课程名称：" + practiceInfo.getCourseName()).setFont(font));
            document.add(new Paragraph("得分：" + practiceInfo.getScore() + "分").setFont(font));
            
            // 排名信息
            document.add(new Paragraph("\n排名信息").setFont(font).setFontSize(16));
            document.add(new Paragraph("班级排名：第" + reportData.get("rank") + "名").setFont(font));
            document.add(new Paragraph("总人数：" + reportData.get("totalStudents") + "人").setFont(font));
            document.add(new Paragraph("超过：" + String.format("%.1f", reportData.get("percentile")) + "%的同学").setFont(font));
            
            // 分数分布表格
            document.add(new Paragraph("\n分数分布").setFont(font).setFontSize(16));
            Table table = new Table(new float[]{150f, 150f, 150f});
            table.addCell(new Cell().add(new Paragraph("分数段").setFont(font)));
            table.addCell(new Cell().add(new Paragraph("人数").setFont(font)));
            table.addCell(new Cell().add(new Paragraph("占比").setFont(font)));
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> distribution = (List<Map<String, Object>>) reportData.get("scoreDistribution");
            for (Map<String, Object> item : distribution) {
                table.addCell(new Cell().add(new Paragraph(item.get("score_range").toString()).setFont(font)));
                table.addCell(new Cell().add(new Paragraph(item.get("count").toString()).setFont(font)));
                table.addCell(new Cell().add(new Paragraph(item.get("percentage") + "%").setFont(font)));
            }
            document.add(table);
            
            // 生成时间
            document.add(new Paragraph("\n\n生成时间：" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .setFont(font));
            
            document.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成PDF报告失败", e);
        }
    }
}