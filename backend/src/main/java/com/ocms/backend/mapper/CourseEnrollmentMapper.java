package com.ocms.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ocms.backend.model.entity.CourseEnrollment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment> {
}
