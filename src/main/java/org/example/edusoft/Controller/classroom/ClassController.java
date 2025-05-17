package org.example.edusoft.controller.classroom;

import org.example.edusoft.common.Result;
import org.example.edusoft.entity.classroom.Class;
import org.example.edusoft.entity.classroom.ClassUser;
import org.example.edusoft.entity.imports.ImportRecord;
import org.example.edusoft.service.classroom.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    @Autowired
    private ClassService classService;

    @PostMapping
    public Result<Class> createClass(@RequestBody Class clazz) {
        return Result.success(classService.createClass(clazz));
    }

    @GetMapping("/teacher/{teacherId}")
    public Result<List<Class>> getClassesByTeacherId(@PathVariable Long teacherId) {
        return Result.success(classService.getClassesByTeacherId(teacherId));
    }

    @GetMapping("/student/{studentId}")
    public Result<List<Class>> getClassesByStudentId(@PathVariable Long studentId) {
        return Result.success(classService.getClassesByStudentId(studentId));
    }

    @GetMapping("/{id}")
    public Result<Class> getClassById(@PathVariable Long id) {
        return Result.success(classService.getClassById(id));
    }

    @PutMapping("/{id}")
    public Result<Class> updateClass(@PathVariable Long id, @RequestBody Class clazz) {
        clazz.setId(id);
        return Result.success(classService.updateClass(clazz));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteClass(@PathVariable Long id) {
        return Result.success(classService.deleteClass(id));
    }

    @PostMapping("/{classId}/join/{userId}")
    public Result<Boolean> joinClass(@PathVariable Long classId, @PathVariable Long userId) {
        return Result.success(classService.joinClass(classId, userId));
    }

    @DeleteMapping("/{classId}/leave/{userId}")
    public Result<Boolean> leaveClass(@PathVariable Long classId, @PathVariable Long userId) {
        return Result.success(classService.leaveClass(classId, userId));
    }

    @GetMapping("/{classId}/users")
    public Result<List<ClassUser>> getClassUsers(@PathVariable Long classId) {
        return Result.success(classService.getClassUsers(classId));
    }

    @PostMapping("/{classId}/import")
    public Result<Boolean> importStudents(@PathVariable Long classId, @RequestBody List<Long> studentIds) {
        return Result.success(classService.importStudents(classId, studentIds));
    }

    @PostMapping("/{classId}/students")
    public Result<ImportRecord> addStudent(
            @PathVariable Long classId,
            @RequestParam Long studentId) {
        try {
            ImportRecord record = classService.addStudent(classId, studentId);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error("添加学生失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    public Result<Void> removeStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        try {
            classService.removeStudent(classId, studentId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除学生失败：" + e.getMessage());
        }
    }

    @PostMapping("/join")
    public Result<ImportRecord> joinClassByCode(
            @RequestParam String classCode,
            @RequestParam Long studentId) {
        try {
            ImportRecord record = classService.joinClassByCode(classCode, studentId);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error("加入班级失败：" + e.getMessage());
        }
    }
} 