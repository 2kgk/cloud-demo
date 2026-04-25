package com.example.log.repository;

import com.example.log.entity.LogEntry;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 日志Repository
 */
@Repository
public interface LogEntryRepository extends ElasticsearchRepository<LogEntry, String> {
    
    /**
     * 根据caseId查询日志
     * @param caseId 用例ID
     * @return 日志列表
     */
    List<LogEntry> findByCaseId(String caseId);
}