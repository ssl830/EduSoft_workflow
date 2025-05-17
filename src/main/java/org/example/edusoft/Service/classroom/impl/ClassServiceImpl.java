package org.example.edusoft.service.classroom.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.edusoft.entity.classroom.Class;
import org.example.edusoft.entity.classroom.ClassUser;
import org.example.edusoft.entity.imports.ImportRecord;
import org.example.edusoft.mapper.classroom.ClassMapper;
import org.example.edusoft.mapper.classroom.ClassUserMapper;
import org.example.edusoft.mapper.imports.ImportRecordMapper;
import org.example.edusoft.service.classroom.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private ClassUserMapper classUserMapper;

    @Autowired
    private ImportRecordMapper importRecordMapper;

    @Override
    @Transactional
    public Class createClass(Class clazz) {
        classMapper.insert(clazz);
        return clazz;
    }

    @Override
    public List<Class> getClassesByTeacherId(Long teacherId) {
        return classMapper.getClassesByTeacherId(teacherId);
    }

    @Override
    public List<Class> getClassesByStudentId(Long studentId) {
        return classMapper.getClassesByStudentId(studentId);
    }

    @Override
    public Class getClassById(Long id) {
        return classMapper.selectById(id);
    }

    @Override
    @Transactional
    public Class updateClass(Class clazz) {
        classMapper.updateById(clazz);
        return clazz;
    }

    @Override
    @Transactional
    public boolean deleteClass(Long id) {
        // 先删除班级成员
        QueryWrapper<ClassUser> wrapper = new QueryWrapper<>();
        wrapper.eq("class_id", id);
        classUserMapper.delete(wrapper);
        // 再删除班级
        return classMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean joinClass(Long classId, Long userId) {
        // 检查是否已经在班级中
        if (classUserMapper.checkUserInClass(classId, userId) > 0) {
            return false;
        }
        
        ClassUser classUser = new ClassUser();
        classUser.setClassId(classId);
        classUser.setUserId(userId);
        classUser.setJoinedAt(LocalDateTime.now());
        
        return classUserMapper.insert(classUser) > 0;
    }

    @Override
    @Transactional
    public boolean leaveClass(Long classId, Long userId) {
        QueryWrapper<ClassUser> wrapper = new QueryWrapper<>();
        wrapper.eq("class_id", classId)
               .eq("user_id", userId);
        return classUserMapper.delete(wrapper) > 0;
    }

    @Override
    public List<ClassUser> getClassUsers(Long classId) {
        return classUserMapper.getClassUsersByClassId(classId);
    }

    @Override
    @Transactional
    public boolean importStudents(Long classId, List<Long> studentIds) {
        for (Long studentId : studentIds) {
            if (classUserMapper.checkUserInClass(classId, studentId) == 0) {
                ClassUser classUser = new ClassUser();
                classUser.setClassId(classId);
                classUser.setUserId(studentId);
                classUser.setJoinedAt(LocalDateTime.now());
                classUserMapper.insert(classUser);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public ImportRecord addStudent(Long classId, Long studentId) {
        // 检查学生是否已在班级中
        if (classUserMapper.checkUserInClass(classId, studentId) > 0) {
            throw new RuntimeException("学生已在班级中");
        }

        // 添加学生到班级
        ClassUser classUser = new ClassUser();
        classUser.setClassId(classId);
        classUser.setUserId(studentId);
        classUser.setJoinedAt(LocalDateTime.now());
        classUserMapper.insert(classUser);

        // 创建导入记录
        ImportRecord record = new ImportRecord();
        record.setClassId(classId);
        record.setOperatorId(studentId); // 这里使用学生ID作为操作人ID
        record.setImportTime(LocalDateTime.now());
        record.setImportType("MANUAL_ADD");
        record.setTotalCount(1);
        record.setSuccessCount(1);
        record.setFailCount(0);
        importRecordMapper.insert(record);

        return record;
    }

    @Override
    @Transactional
    public void removeStudent(Long classId, Long studentId) {
        // 检查学生是否在班级中
        if (classUserMapper.checkUserInClass(classId, studentId) == 0) {
            throw new RuntimeException("学生不在班级中");
        }

        // 从班级中删除学生
        QueryWrapper<ClassUser> wrapper = new QueryWrapper<>();
        wrapper.eq("class_id", classId)
               .eq("user_id", studentId);
        classUserMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public ImportRecord joinClassByCode(String classCode, Long studentId) {
        // 通过班级代码查找班级
        QueryWrapper<Class> wrapper = new QueryWrapper<>();
        wrapper.eq("class_code", classCode);
        Class clazz = classMapper.selectOne(wrapper);
        
        if (clazz == null) {
            throw new RuntimeException("班级代码无效");
        }

        // 检查学生是否已在班级中
        if (classUserMapper.checkUserInClass(clazz.getId(), studentId) > 0) {
            throw new RuntimeException("学生已在班级中");
        }

        // 添加学生到班级
        ClassUser classUser = new ClassUser();
        classUser.setClassId(clazz.getId());
        classUser.setUserId(studentId);
        classUser.setJoinedAt(LocalDateTime.now());
        classUserMapper.insert(classUser);

        // 创建导入记录
        ImportRecord record = new ImportRecord();
        record.setClassId(clazz.getId());
        record.setOperatorId(studentId);
        record.setImportTime(LocalDateTime.now());
        record.setImportType("CODE_JOIN");
        record.setTotalCount(1);
        record.setSuccessCount(1);
        record.setFailCount(0);
        importRecordMapper.insert(record);

        return record;
    }
} 