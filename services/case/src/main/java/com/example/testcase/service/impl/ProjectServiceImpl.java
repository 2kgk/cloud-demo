package com.example.testcase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.testcase.entity.Project;
import com.example.testcase.mapper.ProjectMapper;
import com.example.testcase.service.ProjectService;
import org.springframework.stereotype.Service;

/**
 * 项目服务实现类
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {
}