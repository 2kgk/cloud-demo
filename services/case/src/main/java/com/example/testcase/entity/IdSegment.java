package com.example.testcase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 主键号段表实体
 */
@Data
@TableName("id_segment")
public class IdSegment {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 业务类型
     */
    private String bizType;
    
    /**
     * 当前最大ID
     */
    private Long maxId;
    
    /**
     * 步长
     */
    private Integer step;
    
    /**
     * 版本号(乐观锁)
     */
    private Integer version;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}