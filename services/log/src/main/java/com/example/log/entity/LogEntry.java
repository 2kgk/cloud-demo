package com.example.log.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * 日志实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "execution-logs")
public class LogEntry {
    
    @Id
    private String id;
    
    /**
     * 日志时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime timestamp;
    
    /**
     * 日志级别
     */
    @Field(type = FieldType.Keyword)
    private String level;
    
    /**
     * 日志记录器名称
     */
    @Field(type = FieldType.Keyword)
    private String loggerName;
    
    /**
     * 线程名称
     */
    @Field(type = FieldType.Keyword)
    private String threadName;
    
    /**
     * 日志消息
     */
    @Field(type = FieldType.Text)
    private String message;
    
    /**
     * 服务名称
     */
    @Field(type = FieldType.Keyword)
    private String serviceName;
    
    /**
     * 用例ID
     */
    @Field(type = FieldType.Keyword)
    private String caseId;
    
    /**
     * 路径ID
     */
    @Field(type = FieldType.Keyword)
    private String pathId;
    
    /**
     * 用例名称
     */
    @Field(type = FieldType.Keyword)
    private String caseName;
    
    /**
     * 异常信息
     */
    @Field(type = FieldType.Text)
    private String exception;
}