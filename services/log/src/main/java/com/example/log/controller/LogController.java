package com.example.log.controller;

import com.example.log.entity.LogEntry;
import com.example.log.repository.LogEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日志查询Controller
 */
@RestController
@RequestMapping("/api/logs")
public class LogController {
    
    private static final Logger log = LoggerFactory.getLogger(LogController.class);
    
    @Autowired
    private LogEntryRepository logEntryRepository;
    
    /**
     * 根据caseId查询日志
     * @param caseId 用例ID
     * @return 日志列表
     */
    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<LogEntry>> getLogsByCaseId(@PathVariable String caseId) {
        try {
            log.info("查询日志 - CaseId: {}", caseId);
            List<LogEntry> logs = logEntryRepository.findByCaseId(caseId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("查询日志失败 - CaseId: {}, Error: {}", caseId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 根据caseId和时间范围查询日志
     * @param caseId 用例ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    @GetMapping("/case/{caseId}/time-range")
    public ResponseEntity<List<LogEntry>> getLogsByCaseIdAndTimeRange(
            @PathVariable String caseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            log.info("查询日志 - CaseId: {}, StartTime: {}, EndTime: {}", caseId, startTime, endTime);
            List<LogEntry> logs = logEntryRepository.findByCaseId(caseId).stream()
                    .filter(logEntry -> logEntry.getTimestamp().isAfter(startTime) && logEntry.getTimestamp().isBefore(endTime))
                    .toList();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("查询日志失败 - CaseId: {}, Error: {}", caseId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}