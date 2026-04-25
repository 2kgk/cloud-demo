package com.example.testcase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用例实体类
 */
@Data
@TableName("test_case")
public class TestCase {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String pathId;
    private String caseId;
    private Long projectId;
    private String caseName;
    private String caseDesc;
    private String requestData;
    private String expectedResult;
    private Integer caseType;
    private Integer priority;
    private Integer deleted;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
