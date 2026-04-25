package com.example.testcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.testcase.entity.TestCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用例Mapper接口
 */
@Mapper
public interface TestCaseMapper extends BaseMapper<TestCase> {
    
    /**
     * 根据项目ID查询用例
     */
    List<TestCase> selectByProjectId(@Param("projectId") Long projectId);
}