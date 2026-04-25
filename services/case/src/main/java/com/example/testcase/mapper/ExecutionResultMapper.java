package com.example.testcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.testcase.entity.ExecutionResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * 执行结果Mapper接口
 */
@Mapper
public interface ExecutionResultMapper extends BaseMapper<ExecutionResult> {
}