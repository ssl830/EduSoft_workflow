package org.example.edusoft.entity.discussion;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@TableName("Discussion")
public class Discussion {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    @NotNull(message = "班级ID不能为空")
    private Long classId;
    
    @NotNull(message = "创建者ID不能为空")
    private Long creatorId;
    
    @NotNull(message = "用户编号不能为空")
    @TableField("creator_num")
    private String creatorNum;
    
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 200, message = "标题长度必须在2-200个字符之间")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    private Boolean isPinned = false;
    
    private Boolean isClosed = false;
    
    private Integer viewCount = 0;
    
    private Integer replyCount = 0;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 