package com.huatu.tiku.banckend.dao.manual;

import com.huatu.tiku.banckend.dao.provider.OldQuestionSearchProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by huangqingpeng on 2018/10/29.
 */
@Repository
public interface OldQuestionMapper {

    @SelectProvider(type = OldQuestionSearchProvider.class, method = "findQuestionMaterial")
    public List<HashMap<String,Object>> findQuestionMaterial(@Param("questionIds") List<Integer> questionIds);

    @SelectProvider(type = OldQuestionSearchProvider.class, method = "findQuestionByMultiIds")
    List<HashMap<String,Object>> findQuestionByMultiIds(@Param("ids") Set<Long> oldMultiIds);
}
