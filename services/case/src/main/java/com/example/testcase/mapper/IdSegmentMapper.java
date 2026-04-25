package com.example.testcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.testcase.entity.IdSegment;
import com.example.testcase.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 主键号段Mapper接口
 */
@Mapper
public interface IdSegmentMapper extends BaseMapper<IdSegment> {
    
    /**
     * 更新号段（乐观锁）
     * @param bizType 业务类型
     * @param oldMaxId 旧的最大ID
     * @param newMaxId 新的最大ID
     * @param version 版本号
     * @return 影响行数
     */
    int updateSegment(@Param("bizType") String bizType, 
                      @Param("oldMaxId") Long oldMaxId, 
                      @Param("newMaxId") Long newMaxId, 
                      @Param("version") Integer version);
}