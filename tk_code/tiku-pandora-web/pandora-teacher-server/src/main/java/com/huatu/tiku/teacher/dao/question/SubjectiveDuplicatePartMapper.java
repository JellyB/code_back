package com.huatu.tiku.teacher.dao.question;

import com.huatu.tiku.entity.duplicate.SubjectiveDuplicatePart;
import com.huatu.tiku.teacher.dao.provider.question.SubjectiveDuplicatePartProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;

/**
 * Created by huangqp on 2018\6\29 0029.
 */
@Repository
public interface SubjectiveDuplicatePartMapper extends Mapper<SubjectiveDuplicatePart> {

    @SelectProvider(type = SubjectiveDuplicatePartProvider.class,method = "findByQuestionId")
    HashMap<String,Object> findByQuestionId(@Param("id") long id);
}

