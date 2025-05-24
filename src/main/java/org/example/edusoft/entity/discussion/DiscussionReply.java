package org.example.edusoft.entity.discussion;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@TableName("DiscussionReply")
public class DiscussionReply {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @NotNull(message = "讨论ID不能为空")
    private Long discussionId;
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotBlank(message = "回复内容不能为空")
    private String content;
    
    private Long parentReplyId;
    
    private Boolean isTeacherReply = false;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 