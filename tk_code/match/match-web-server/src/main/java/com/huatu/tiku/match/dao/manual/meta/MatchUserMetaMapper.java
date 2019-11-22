package com.huatu.tiku.match.dao.manual.meta;


import com.huatu.tiku.match.bean.entity.MatchQuestionMeta;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.dao.manual.provider.MatchQuestionMetaProvider;
import com.huatu.tiku.match.dao.manual.provider.MatchUserMetaProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqingpeng on 2018/10/17.
 */
@Repository
public interface MatchUserMetaMapper extends Mapper<MatchUserMeta> {

    /**
     * 用户分数和答题卡ID查询
     * @param paperId
     * @return
     */
    @SelectProvider(type = MatchUserMetaProvider.class, method = "findMatchUserScoreByPaperId")
    List<Map<String,Object>> findMatchUserScoreByPaperId(@Param("paperId") int paperId);


    /**
     * 用户最大分数
     * @param paperId
     * @return
     */
    @SelectProvider(type = MatchUserMetaProvider.class,method = "findMatchUserMaxScore")
    Double findMatchUserMaxScore(@Param("paperId") int paperId);

    /**
     * 模考大赛分数累加值
     * @param paperId
     * @return
     */
    @SelectProvider(type = MatchUserMetaProvider.class,method = "findMatchUserSumScore")
    Double findMatchUserSumScore(@Param("paperId") int paperId);

    /**
     * 测试专用---获取参加过考试的用户Id
     * @return
     */
    @SelectProvider(type = MatchUserMetaProvider.class,method = "findUserIds")
    List<Integer> findUserIds();


    @SelectProvider(type = MatchUserMetaProvider.class, method = "findByCursor")
    List<HashMap> findByCursor(int index, int limit);

    @Select("select match_id as matchId from match_user_meta where `status` = 1 group by match_id ")
    List<HashMap> findMatchIds();

}
