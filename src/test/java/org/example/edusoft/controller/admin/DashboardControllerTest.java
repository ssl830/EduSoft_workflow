package org.example.edusoft.controller.admin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.example.edusoft.service.admin.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    // 正例：service 正常返回数据
    @Test
    void testGetOverview_success() throws Exception {
        Map<String, Object> overview = new HashMap<>();
        overview.put("teacherToday", 5);
        overview.put("studentToday", 50);
        overview.put("teacherWeek", 30);
        overview.put("studentWeek", 200);

        when(dashboardService.getDashboardOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/admin/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)) // 你Result.success()的code是200，修改为你的实际值
                .andExpect(jsonPath("$.data.teacherToday").value(5))
                .andExpect(jsonPath("$.data.studentToday").value(50))
                .andExpect(jsonPath("$.data.teacherWeek").value(30))
                .andExpect(jsonPath("$.data.studentWeek").value(200));
    }

    // 反例：service 返回空，接口依然正常
    @Test
    void testGetOverview_empty() throws Exception {
        when(dashboardService.getDashboardOverview()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/api/admin/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)) // 你Result.success()的code是200，修改为你的实际值
                .andExpect(jsonPath("$.data").isEmpty());
    }
}