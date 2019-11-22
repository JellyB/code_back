package com.huatu.tiku.banckend.dao.manual;

import com.huatu.tiku.banckend.dao.provider.AdviceSearchProvider;
import com.huatu.tiku.dto.vo.QuestionAdviceBaseIds;
import com.huatu.tiku.entity.AdviceBean;
import com.huatu.tiku.entity.advice.QuestionAdvice;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * @author zhengyi
 * @date 2018/9/13 1:38 PM
 **/
@Repository
public interface QuestionAdviceMapper extends Mapper<QuestionAdvice> {

    @SelectProvider(type = AdviceSearchProvider.class, method = "getList")
    List<QuestionAdviceBaseIds> getList(AdviceBean questionAdvice);

    @Select("select id,checker,question_area,user_id,error_descrp from v_question_correction_log where question_id=#{questionId}")
    List<QuestionAdvice> getUserInfo(Integer questionId);

    @SelectProvider(type = AdviceSearchProvider.class, method = "getQuestionSourceInfo")
    List<HashMap<String, Object>> getQuestionTagInfo(String questionIds);
}
