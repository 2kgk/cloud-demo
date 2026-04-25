package com.example.testcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.testcase.entity.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目Mapper接口
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}