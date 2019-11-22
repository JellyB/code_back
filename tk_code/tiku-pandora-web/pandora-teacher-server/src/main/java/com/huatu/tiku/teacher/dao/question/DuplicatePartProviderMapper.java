package com.huatu.tiku.teacher.dao.question;


import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.teacher.dao.provider.question.DuplicatePartProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/12/2
 * @描述
 */
@Repository
public interface DuplicatePartProviderMapper {

    @SelectProvider(type = DuplicatePartProvider.class, method = "buildObjective")
    List<HashMap<String, Object>> buildObjective(String questionIds, int questionType);


    @SelectProvider(type = DuplicatePartProvider.class, method = "buildSubjective")
    List<HashMap<String, Object>> buildSubjective(String questionIds, int questionType);


    @SelectProvider(type = DuplicatePartProvider.class, method = "buildObjective")
    List<DuplicatePartResp> buildObjectiveInfo(String questionIds, int questionType);

    @SelectProvider(type = DuplicatePartProvider.class, method = "buildSubjective")
    List<DuplicatePartResp> buildSubjectiveInfo(String questionIds, int questionType);


}
