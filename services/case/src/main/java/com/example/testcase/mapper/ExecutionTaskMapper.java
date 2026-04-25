package com.example.testcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.testcase.entity.ExecutionTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 执行任务Mapper接口
 */
@Mapper
public interface ExecutionTaskMapper extends BaseMapper<ExecutionTask> {
}