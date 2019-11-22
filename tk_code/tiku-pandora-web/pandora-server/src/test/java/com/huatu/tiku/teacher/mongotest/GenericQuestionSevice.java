package com.huatu.tiku.teacher.mongotest;

import com.huatu.ztk.question.bean.GenericQuestion;
import org.springframework.data.domain.Page;


public interface GenericQuestionSevice {
    Page<GenericQuestion> paginationQuery(Integer pageNum);
}
