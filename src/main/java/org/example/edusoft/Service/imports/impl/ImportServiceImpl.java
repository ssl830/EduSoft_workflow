package org.example.edusoft.service.imports.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.edusoft.entity.classroom.ClassUser;
import org.example.edusoft.entity.imports.ImportRecord;
import org.example.edusoft.mapper.classroom.ClassUserMapper;
import org.example.edusoft.mapper.imports.ImportRecordMapper;
import org.example.edusoft.service.imports.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportServiceImpl implements ImportService {

    @Autowired
    private ImportRecordMapper importRecordMapper;

    @Autowired
    private ClassUserMapper classUserMapper;

    @Override
    @Transactional
    public ImportRecord importStudentsByFile(Long classId, Long operatorId, MultipartFile file) {
        ImportRecord record = new ImportRecord();
        record.setClassId(classId);
        record.setOperatorId(operatorId);
        record.setFileName(file.getOriginalFilename());
        record.setImportTime(LocalDateTime.now());
        record.setImportType("FILE");
        
        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;
        List<String> failReasons = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim());
            
            for (CSVRecord csvRecord : csvParser) {
                totalCount++;
                try {
                    // 假设CSV文件中有一列是student_id
                    String studentIdStr = csvRecord.get("student_id");
                    Long studentId = Long.parseLong(studentIdStr);
                    
                    // 检查学生是否已在班级中
                    if (classUserMapper.checkUserInClass(classId, studentId) == 0) {
                        ClassUser classUser = new ClassUser();
                        classUser.setClassId(classId);
                        classUser.setUserId(studentId);
                        classUser.setJoinedAt(LocalDateTime.now());
                        classUserMapper.insert(classUser);
                        successCount++;
                    } else {
                        failCount++;
                        failReasons.add("学生ID " + studentId + " 已在班级中");
                    }
                } catch (Exception e) {
                    failCount++;
                    failReasons.add("第 " + totalCount + " 行数据格式错误: " + e.getMessage());
                }
            }
            
            csvParser.close();
            reader.close();
            
        } catch (Exception e) {
            failCount = totalCount;
            failReasons.add("文件处理错误: " + e.getMessage());
        }

        record.setTotalCount(totalCount);
        record.setSuccessCount(successCount);
        record.setFailCount(failCount);
        record.setFailReason(String.join("; ", failReasons));
        
        importRecordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public ImportRecord importStudentsManually(Long classId, Long operatorId, List<Long> studentIds) {
        ImportRecord record = new ImportRecord();
        record.setClassId(classId);
        record.setOperatorId(operatorId);
        record.setImportTime(LocalDateTime.now());
        record.setImportType("MANUAL");
        
        int totalCount = studentIds.size();
        int successCount = 0;
        int failCount = 0;
        List<String> failReasons = new ArrayList<>();

        for (Long studentId : studentIds) {
            try {
                if (classUserMapper.checkUserInClass(classId, studentId) == 0) {
                    ClassUser classUser = new ClassUser();
                    classUser.setClassId(classId);
                    classUser.setUserId(studentId);
                    classUser.setJoinedAt(LocalDateTime.now());
                    classUserMapper.insert(classUser);
                    successCount++;
                } else {
                    failCount++;
                    failReasons.add("学生ID " + studentId + " 已在班级中");
                }
            } catch (Exception e) {
                failCount++;
                failReasons.add("学生ID " + studentId + " 处理失败: " + e.getMessage());
            }
        }

        record.setTotalCount(totalCount);
        record.setSuccessCount(successCount);
        record.setFailCount(failCount);
        record.setFailReason(String.join("; ", failReasons));
        
        importRecordMapper.insert(record);
        return record;
    }

    @Override
    public List<ImportRecord> getImportRecords(Long classId) {
        return importRecordMapper.getImportRecordsByClassId(classId);
    }

    @Override
    public ImportRecord getImportRecord(Long id) {
        return importRecordMapper.selectById(id);
    }
} 