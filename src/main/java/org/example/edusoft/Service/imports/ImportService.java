package org.example.edusoft.service.imports;

import org.example.edusoft.entity.imports.ImportRecord;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ImportService {
    // 通过文件导入学生
    ImportRecord importStudentsByFile(Long classId, Long operatorId, MultipartFile file);
    
    // 手动导入学生
    ImportRecord importStudentsManually(Long classId, Long operatorId, List<Long> studentIds);
    
    // 获取班级的导入记录
    List<ImportRecord> getImportRecords(Long classId);
    
    // 获取导入记录详情
    ImportRecord getImportRecord(Long id);
} 