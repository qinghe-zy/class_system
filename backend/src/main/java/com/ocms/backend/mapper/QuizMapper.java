package com.ocms.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ocms.backend.model.entity.Quiz;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuizMapper extends BaseMapper<Quiz> {
}
