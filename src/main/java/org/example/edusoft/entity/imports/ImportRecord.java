package org.example.edusoft.entity.imports;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("import_record")
public class ImportRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long classId;
    private Long operatorId;  // 操作人ID
    private String fileName;  // 导入文件名
    private Integer totalCount;  // 总记录数
    private Integer successCount;  // 成功导入数
    private Integer failCount;  // 失败数
    private String failReason;  // 失败原因
    private LocalDateTime importTime;  // 导入时间
    private String importType;  // 导入类型：FILE（文件导入）, MANUAL（手动导入）
} 