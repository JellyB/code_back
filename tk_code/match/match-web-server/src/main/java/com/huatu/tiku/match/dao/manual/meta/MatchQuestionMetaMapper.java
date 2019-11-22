package com.huatu.tiku.match.dao.manual.meta;

import com.huatu.tiku.match.bean.entity.MatchQuestionMeta;
import com.huatu.tiku.match.dao.manual.provider.MatchQuestionMetaProvider;
import com.huatu.tiku.match.dao.manual.provider.MatchUserMetaProvider;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * Created by huangqingpeng on 2018/10/17.
 */
@Repository
public interface MatchQuestionMetaMapper extends Mapper<MatchQuestionMeta> {

    @SelectProvider(type = MatchQuestionMetaProvider.class, method = "findByCursor")
    List<HashMap> findByCursor(int index, int limit);
}
