package org.example.edusoft.controller.notification;

import org.example.edusoft.common.Result;
import org.example.edusoft.entity.notification.TaskReminder;
import org.example.edusoft.service.notification.TaskReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskReminderControllerTest {

    @Mock
    private TaskReminderService taskReminderService;

    @InjectMocks
    private TaskReminderController taskReminderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // createTaskReminder
    @Test
    void testCreateTaskReminder_Success() {
        TaskReminder tr = new TaskReminder();
        when(taskReminderService.createTaskReminder(any(TaskReminder.class))).thenReturn(tr);

        Result<TaskReminder> result = taskReminderController.createTaskReminder(tr);
        assertEquals(200, result.getCode());
        assertEquals(tr, result.getData());
    }

    @Test
    void testCreateTaskReminder_Failure() {
        TaskReminder tr = new TaskReminder();
        when(taskReminderService.createTaskReminder(any(TaskReminder.class))).thenThrow(new RuntimeException("fail"));

        try {
            taskReminderController.createTaskReminder(tr);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // updateTaskReminder
    @Test
    void testUpdateTaskReminder_Success() {
        TaskReminder tr = new TaskReminder();
        when(taskReminderService.updateTaskReminder(any(TaskReminder.class))).thenReturn(tr);

        Result<TaskReminder> result = taskReminderController.updateTaskReminder(1L, tr);
        assertEquals(200, result.getCode());
        assertEquals(tr, result.getData());
        assertEquals(1L, tr.getId());
    }

    @Test
    void testUpdateTaskReminder_Failure() {
        TaskReminder tr = new TaskReminder();
        when(taskReminderService.updateTaskReminder(any(TaskReminder.class))).thenThrow(new RuntimeException("fail"));

        try {
            taskReminderController.updateTaskReminder(1L, tr);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // deleteTaskReminder
    @Test
    void testDeleteTaskReminder_Success() {
        doNothing().when(taskReminderService).deleteTaskReminder(1L);
        Result<Boolean> result = taskReminderController.deleteTaskReminder(1L);
        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }

    @Test
    void testDeleteTaskReminder_Failure() {
        doThrow(new RuntimeException("fail")).when(taskReminderService).deleteTaskReminder(1L);
        try {
            taskReminderController.deleteTaskReminder(1L);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // getTaskReminder
    @Test
    void testGetTaskReminder_Success() {
        TaskReminder tr = new TaskReminder();
        when(taskReminderService.getTaskReminderById(1L)).thenReturn(tr);

        Result<TaskReminder> result = taskReminderController.getTaskReminder(1L);
        assertEquals(200, result.getCode());
        assertEquals(tr, result.getData());
    }

    @Test
    void testGetTaskReminder_Failure() {
        when(taskReminderService.getTaskReminderById(1L)).thenThrow(new RuntimeException("fail"));
        try {
            taskReminderController.getTaskReminder(1L);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // getUserTaskReminders
    @Test
    void testGetUserTaskReminders_Success() {
        List<TaskReminder> list = Collections.singletonList(new TaskReminder());
        when(taskReminderService.getTaskRemindersByUserId(1L)).thenReturn(list);

        Result<List<TaskReminder>> result = taskReminderController.getUserTaskReminders(1L);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    @Test
    void testGetUserTaskReminders_Failure() {
        when(taskReminderService.getTaskRemindersByUserId(1L)).thenThrow(new RuntimeException("fail"));
        try {
            taskReminderController.getUserTaskReminders(1L);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // getUncompletedTaskReminders
    @Test
    void testGetUncompletedTaskReminders_Success() {
        List<TaskReminder> list = Collections.singletonList(new TaskReminder());
        when(taskReminderService.getUncompletedTaskReminders(1L)).thenReturn(list);

        Result<List<TaskReminder>> result = taskReminderController.getUncompletedTaskReminders(1L);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    @Test
    void testGetUncompletedTaskReminders_Failure() {
        when(taskReminderService.getUncompletedTaskReminders(1L)).thenThrow(new RuntimeException("fail"));
        try {
            taskReminderController.getUncompletedTaskReminders(1L);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // getCompletedTaskReminders
    @Test
    void testGetCompletedTaskReminders_Success() {
        List<TaskReminder> list = Collections.singletonList(new TaskReminder());
        when(taskReminderService.getCompletedTaskReminders(1L)).thenReturn(list);

        Result<List<TaskReminder>> result = taskReminderController.getCompletedTaskReminders(1L);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    @Test
    void testGetCompletedTaskReminders_Failure() {
        when(taskReminderService.getCompletedTaskReminders(1L)).thenThrow(new RuntimeException("fail"));
        try {
            taskReminderController.getCompletedTaskReminders(1L);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // markTaskAsCompleted
    @Test
    void testMarkTaskAsCompleted_Success() {
        doNothing().when(taskReminderService).markTaskAsCompleted(1L);
        Result<Boolean> result = taskReminderController.markTaskAsCompleted(1L);
        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }

    @Test
    void testMarkTaskAsCompleted_Failure() {
        doThrow(new RuntimeException("fail")).when(taskReminderService).markTaskAsCompleted(1L);
        try {
            taskReminderController.markTaskAsCompleted(1L);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // markTaskAsUncompleted
    @Test
    void testMarkTaskAsUncompleted_Success() {
        doNothing().when(taskReminderService).markTaskAsUncompleted(1L);
        Result<Boolean> result = taskReminderController.markTaskAsUncompleted(1L);
        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }

    @Test
    void testMarkTaskAsUncompleted_Failure() {
        doThrow(new RuntimeException("fail")).when(taskReminderService).markTaskAsUncompleted(1L);
        try {
            taskReminderController.markTaskAsUncompleted(1L);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }
}