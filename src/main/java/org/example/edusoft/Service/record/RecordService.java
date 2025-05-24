package org.example.edusoft.service.record;
import java.util.List;
import java.util.Map;
import org.example.edusoft.entity.record.*;

public interface RecordService {
    List<StudyRecord> getStudyRecords(Long studentId);
    List<PracticeRecord> getPracticeRecords(Long studentId);
    byte[] exportRecordsToExcel(Long studentId);
    byte[] exportPracticeRecordsToExcel(Long studentId);
    Map<String, Object> getPracticeReport(Long practiceId, Long studentId);
    byte[] generatePracticeReportPdf(Map<String, Object> reportData);
}