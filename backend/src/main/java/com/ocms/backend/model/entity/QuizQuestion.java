package com.ocms.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("quiz_question")
public class QuizQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long quizId;
    private String questionTitle;
    private String questionType;
    private String optionsJson;
    private String correctAnswer;
    private Integer score;
    private Integer sortNo;
}
