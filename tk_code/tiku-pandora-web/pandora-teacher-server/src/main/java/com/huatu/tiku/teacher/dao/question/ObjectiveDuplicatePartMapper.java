package com.huatu.tiku.teacher.dao.question;


import com.huatu.tiku.entity.duplicate.ObjectiveDuplicatePart;
import com.huatu.tiku.teacher.dao.provider.question.ObjectiveDuplicatePartProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;


/**
 * Created by huangqp on 2018\5\16 0016.
 */
@Repository
public interface ObjectiveDuplicatePartMapper extends Mapper<ObjectiveDuplicatePart> {

    /**
     * 通过试题ID查询复用数据
     * @param questionId
     * @return
     */
    @SelectProvider(type = ObjectiveDuplicatePartProvider.class , method = "findByQuestionId")
    HashMap<String,Object> findByQuestionId(@Param("questionId") long questionId);
}
