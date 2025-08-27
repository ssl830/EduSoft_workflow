package org.example.edusoft.controller.classroom;

import java.util.List;
import java.util.Map;

import org.example.edusoft.common.Result;
import org.example.edusoft.entity.classroom.Class;
import org.example.edusoft.entity.classroom.ClassDetailDTO;
import org.example.edusoft.entity.classroom.ClassUser;
import org.example.edusoft.entity.classroom.TeacherClassDTO;
import org.example.edusoft.entity.imports.ImportRecord;
import org.example.edusoft.service.classroom.ClassService;
import org.example.edusoft.service.classroom.TeacherClassService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

class ClassControllerTest {

    @InjectMocks
    private ClassController classController;

    @Mock
    private ClassService classService;
    @Mock
    private TeacherClassService teacherClassService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // createClass 正例
    @Test
    void testCreateClass_success() {
        Class clazz = new Class();
        when(classService.createClass(clazz)).thenReturn(clazz);

        Result<Class> result = classController.createClass(clazz);
        assertEquals(200, result.getCode());
        assertEquals(clazz, result.getData());
    }

    // createClass 反例
    @Test
    void testCreateClass_fail() {
        Class clazz = new Class();
        when(classService.createClass(clazz)).thenThrow(new IllegalArgumentException("fail"));
        Result<Class> result = classController.createClass(clazz);
        assertEquals(500, result.getCode());
    }

    // getClassById 正例
    @Test
    void testGetClassById_success() {
        ClassDetailDTO dto = new ClassDetailDTO();
        when(classService.getClassDetailById(1L)).thenReturn(dto);
        Result<ClassDetailDTO> result = classController.getClassById(1L);
        assertEquals(200, result.getCode());
        assertEquals(dto, result.getData());
    }

    // getClassById 反例
    @Test
    void testGetClassById_fail() {
        when(classService.getClassDetailById(1L)).thenThrow(new IllegalArgumentException("fail"));
        assertThrows(IllegalArgumentException.class, () -> classController.getClassById(1L));
    }

    // updateClass 正例
    @Test
    void testUpdateClass_success() {
        Class clazz = new Class();
        when(classService.updateClass(clazz)).thenReturn(clazz);
        Result<Class> result = classController.updateClass(1L, clazz);
        assertEquals(200, result.getCode());
        assertEquals(clazz, result.getData());
    }

    // updateClass 反例
    @Test
    void testUpdateClass_fail() {
        Class clazz = new Class();
        when(classService.updateClass(clazz)).thenThrow(new IllegalArgumentException("fail"));
        Result<Class> result = classController.updateClass(1L, clazz);
        assertEquals(500, result.getCode());
    }

    // deleteClass 正例
    @Test
    void testDeleteClass_success() {
        when(classService.deleteClass(1L)).thenReturn(true);
        Result<Boolean> result = classController.deleteClass(1L);
        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }

    // deleteClass 反例
    @Test
    void testDeleteClass_fail() {
        when(classService.deleteClass(1L)).thenThrow(new IllegalArgumentException("fail"));
        Result<Boolean> result = classController.deleteClass(1L);
        assertEquals(500, result.getCode());
    }

    // joinClass 正例
    @Test
    void testJoinClass_success() {
        when(classService.joinClass(1L, 2L)).thenReturn(true);
        Result<Boolean> result = classController.joinClass(1L, 2L);
        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }

    // joinClass 反例
    @Test
    void testJoinClass_fail() {
        when(classService.joinClass(1L, 2L)).thenThrow(new IllegalArgumentException("fail"));
        Result<Boolean> result = classController.joinClass(1L, 2L);
        assertEquals(500, result.getCode());
    }

    // leaveClass 正例
    @Test
    void testLeaveClass_success() {
        when(classService.leaveClass(1L, 2L)).thenReturn(true);
        Result<Boolean> result = classController.leaveClass(1L, 2L);
        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }

    // leaveClass 反例
    @Test
    void testLeaveClass_fail() {
        when(classService.leaveClass(1L, 2L)).thenThrow(new IllegalArgumentException("fail"));
        Result<Boolean> result = classController.leaveClass(1L, 2L);
        assertEquals(500, result.getCode());
    }

    // getClassUsers 正例
    @Test
    void testGetClassUsers_success() {
        List<ClassUser> users = List.of(new ClassUser());
        when(classService.getClassUsers(1L)).thenReturn(users);
        Result<List<ClassUser>> result = classController.getClassUsers(1L);
        assertEquals(200, result.getCode());
        assertEquals(users, result.getData());
    }

    // getClassUsers 反例
    @Test
    void testGetClassUsers_fail() {
        when(classService.getClassUsers(1L)).thenThrow(new IllegalArgumentException("fail"));
        Result<List<ClassUser>> result = classController.getClassUsers(1L);
        assertEquals(500, result.getCode());
    }

    // importStudents 正例
    @Test
    void testImportStudents_success() {
        List<Long> studentIds = List.of(2L, 3L);
        when(classService.importStudents(1L, studentIds)).thenReturn(true);
        Result<Boolean> result = classController.importStudents(1L, studentIds);
        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }

    // importStudents 反例
    @Test
    void testImportStudents_fail() {
        List<Long> studentIds = List.of(2L, 3L);
        when(classService.importStudents(1L, studentIds)).thenThrow(new IllegalArgumentException("fail"));
        Result<Boolean> result = classController.importStudents(1L, studentIds);
        assertEquals(500, result.getCode());
    }

    // addStudent 正例
    @Test
    void testAddStudent_success() {
        ImportRecord record = new ImportRecord();
        when(classService.addStudent(1L, 2L)).thenReturn(record);
        Result<ImportRecord> result = classController.addStudent(1L, 2L);
        assertEquals(200, result.getCode());
        assertEquals(record, result.getData());
    }

    // addStudent 反例
    @Test
    void testAddStudent_fail() {
        when(classService.addStudent(1L, 2L)).thenThrow(new IllegalArgumentException("fail"));
        Result<ImportRecord> result = classController.addStudent(1L, 2L);
        assertEquals(500, result.getCode());
    }

    // removeStudent 正例
    @Test
    void testRemoveStudent_success() {
        Result<Void> result = classController.removeStudent(1L, 2L);
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    // removeStudent 反例
    @Test
    void testRemoveStudent_fail() {
        doThrow(new IllegalArgumentException("fail")).when(classService).removeStudent(1L, 2L);
        Result<Void> result = classController.removeStudent(1L, 2L);
        assertEquals(500, result.getCode());
    }

    // joinClassByCode 正例
    @Test
    void testJoinClassByCode_success() {
        ImportRecord record = new ImportRecord();
        when(classService.joinClassByCode("code", 2L)).thenReturn(record);
        Result<ImportRecord> result = classController.joinClassByCode("code", 2L);
        assertEquals(200, result.getCode());
        assertEquals(record, result.getData());
    }

    // joinClassByCode 反例
    @Test
    void testJoinClassByCode_fail() {
        when(classService.joinClassByCode("code", 2L)).thenThrow(new IllegalArgumentException("fail"));
        Result<ImportRecord> result = classController.joinClassByCode("code", 2L);
        assertEquals(500, result.getCode());
    }

    // getClassesByUserId 正例
    @Test
    void testGetClassesByUserId_success() {
        List<ClassDetailDTO> list = List.of(new ClassDetailDTO());
        when(classService.getClassesByUserId(1L)).thenReturn(list);
        Result<List<ClassDetailDTO>> result = classController.getClassesByUserId(1L);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    // getClassesByUserId 反例
    @Test
    void testGetClassesByUserId_fail() {
        when(classService.getClassesByUserId(1L)).thenThrow(new IllegalArgumentException("fail"));
        assertThrows(IllegalArgumentException.class, () -> classController.getClassesByUserId(1L));
    }

    // getSimpleClassesByTeacherId 正例
    @Test
    void testGetSimpleClassesByTeacherId_success() {
        List<TeacherClassDTO> list = List.of(new TeacherClassDTO());
        when(teacherClassService.getClassesByTeacherId(1L)).thenReturn(list);
        Result<List<TeacherClassDTO>> result = classController.getSimpleClassesByTeacherId(1L);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    // getSimpleClassesByTeacherId 反例
    @Test
    void testGetSimpleClassesByTeacherId_fail() {
        when(teacherClassService.getClassesByTeacherId(1L)).thenThrow(new IllegalArgumentException("fail"));
        assertThrows(IllegalArgumentException.class, () -> classController.getSimpleClassesByTeacherId(1L));
    }

    // getScheduleByUserId 正例
    @Test
    void testGetScheduleByUserId_success() {
        List<ClassDetailDTO> list = List.of(new ClassDetailDTO());
        when(classService.getClassesByUserId(1L)).thenReturn(list);
        Result<List<ClassDetailDTO>> result = classController.getScheduleByUserId(1L);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    // getScheduleByUserId 反例
    @Test
    void testGetScheduleByUserId_fail() {
        when(classService.getClassesByUserId(1L)).thenThrow(new IllegalArgumentException("fail"));
        assertThrows(IllegalArgumentException.class, () -> classController.getScheduleByUserId(1L));
    }

    // getClassStudentCount 正例
    @Test
    void testGetClassStudentCount_success() {
        when(classService.getClassStudentCount(1L)).thenReturn(10);
        Result<Object> result = classController.getClassStudentCount(1L);
        assertEquals(200, result.getCode());
        assertEquals(10, ((Map<?, ?>)result.getData()).get("total"));
    }

    // getClassStudentCount 反例
    @Test
    void testGetClassStudentCount_fail() {
        when(classService.getClassStudentCount(1L)).thenThrow(new IllegalArgumentException("fail"));
        assertThrows(IllegalArgumentException.class, () -> classController.getClassStudentCount(1L));
    }
}