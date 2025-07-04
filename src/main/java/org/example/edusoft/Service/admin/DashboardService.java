package org.example.edusoft.service.admin;

import org.example.edusoft.mapper.admin.DashboardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    /**
     * 获取教师 & 学生在给定时间区间内的关键统计数据
     *
     * @param start 开始日期（含）
     * @param end   结束日期（含）
     * @return Map 结构，包含 teacherStats / studentStats
     */
    public Map<String, Object> getBasicStatistics(LocalDate start, LocalDate end) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Integer> teacherStats = new HashMap<>();
        teacherStats.put("uploadResource", dashboardMapper.countTeacherUploadResource(start, end));
        teacherStats.put("createHomework", dashboardMapper.countTeacherCreateHomework(start, end));
        teacherStats.put("createPractice", dashboardMapper.countTeacherCreatePractice(start, end));
        teacherStats.put("correctPractice", dashboardMapper.countTeacherCorrectPractice(start, end));
        teacherStats.put("createQuestion", dashboardMapper.countTeacherCreateQuestion(start, end));

        Map<String, Integer> studentStats = new HashMap<>();
        studentStats.put("downloadResource", dashboardMapper.countStudentDownloadResource(start, end));
        studentStats.put("submitHomework", dashboardMapper.countStudentSubmitHomework(start, end));
        studentStats.put("submitPractice", dashboardMapper.countStudentSubmitPractice(start, end));
        studentStats.put("assistantQuestions", dashboardMapper.countStudentAssistantQuestions(start, end));

        result.put("teacher", teacherStats);
        result.put("student", studentStats);
        return result;
    }

    /**
     * 获取教学效率指数
     *
     * @param start 开始日期
     * @param end 结束日期
     * @return 包含备课耗时、练习设计耗时等统计的Map
     */
    public Map<String, Object> getTeachingEfficiencyMetrics(LocalDate start, LocalDate end) {
        Map<String, Object> metrics = new HashMap<>();

        // 备课与修正耗时：调用AI接口 /rag/generate, /rag/detail, /rag/regenerate 的次数与平均耗时
        Double prepAvgDuration = dashboardMapper.getAiServiceAverageDuration("/rag/generate", start, end) +
                                 dashboardMapper.getAiServiceAverageDuration("/rag/detail", start, end) +
                                 dashboardMapper.getAiServiceAverageDuration("/rag/regenerate", start, end);
        int prepCallCount = dashboardMapper.getAiServiceCallCount("/rag/generate", start, end) +
                            dashboardMapper.getAiServiceCallCount("/rag/detail", start, end) +
                            dashboardMapper.getAiServiceCallCount("/rag/regenerate", start, end);
        metrics.put("lessonPrepAvgDuration", String.format("%.2f", prepAvgDuration));
        metrics.put("lessonPrepCallCount", prepCallCount);

        // 课后练习设计与修正耗时：调用AI接口 /rag/generate_exercise 的次数与平均耗时
        Double exerciseDesignAvgDuration = dashboardMapper.getAiServiceAverageDuration("/rag/generate_exercise", start, end);
        int exerciseDesignCallCount = dashboardMapper.getAiServiceCallCount("/rag/generate_exercise", start, end);
        metrics.put("exerciseDesignAvgDuration", String.format("%.2f", exerciseDesignAvgDuration));
        metrics.put("exerciseDesignCallCount", exerciseDesignCallCount);

        // TODO: 课程优化方向 (如：某学科通过率持续偏低) - 需AI服务提供更具体分析
        metrics.put("courseOptimizationDirection", "AI自动分析功能待完善");

        return metrics;
    }

    /**
     * 获取学生学习效果数据
     *
     * @param start 开始日期
     * @param end 结束日期
     * @return 包含平均正确率趋势、知识点掌握情况、高频错误知识点等统计的Map
     */
    public Map<String, Object> getStudentLearningEffect(LocalDate start, LocalDate end) {
        Map<String, Object> effect = new HashMap<>();

        // 平均正确率趋势 & 知识点掌握情况 (按课程-章节)
        List<Map<String, Object>> correctnessBySection = dashboardMapper.getAverageCorrectnessBySection(start, end);
        effect.put("correctnessBySection", correctnessBySection);

        // 高频错误知识点
        List<Map<String, Object>> topWrongKnowledgePoints = dashboardMapper.getTopWrongKnowledgePoints(start, end);
        effect.put("topWrongKnowledgePoints", topWrongKnowledgePoints);

        // 获取所有课程和章节信息，用于前端名称映射
        List<Map<String, Object>> allCoursesAndSections = dashboardMapper.getAllCoursesAndSections();
        Map<String, String> courseSectionNames = allCoursesAndSections.stream()
                .collect(Collectors.toMap(
                        map -> map.get("course_id") + "-" + map.get("section_id"),
                        map -> map.get("course_name") + " - " + map.get("section_name")
                ));
        effect.put("courseSectionNames", courseSectionNames);

        return effect;
    }

    /**
     * 组合今日 / 本周两套数据，用于大屏概览。
     */
    public Map<String, Object> getDashboardOverview() {
        Map<String, Object> overview = new HashMap<>();

        LocalDate today = LocalDate.now();
        // 今天
        overview.put("today", getBasicStatistics(today, today));
        overview.put("todayEfficiency", getTeachingEfficiencyMetrics(today, today));
        overview.put("todayLearningEffect", getStudentLearningEffect(today, today));

        // 本周（过去 7 天含今日）
        LocalDate weekStart = today.minusDays(6);
        overview.put("week", getBasicStatistics(weekStart, today));
        overview.put("weekEfficiency", getTeachingEfficiencyMetrics(weekStart, today));
        overview.put("weekLearningEffect", getStudentLearningEffect(weekStart, today));

        return overview;
    }
} 