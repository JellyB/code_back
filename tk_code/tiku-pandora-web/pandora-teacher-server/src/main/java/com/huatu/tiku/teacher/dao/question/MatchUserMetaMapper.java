package com.huatu.tiku.teacher.dao.question;

import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqingpeng on 2018/10/17.
 */
@Repository
public interface MatchUserMetaMapper extends Mapper<MatchUserMeta> {

    @Select("select * from match_user_meta where match_id = #{paperId} and practice_id <> -1 ORDER BY score desc limit #{size}")
    List<Map> findOrderByScore(@Param("paperId") int paperId, @Param("size") int size);

    @Select("select avg(score) from match_user_meta where match_id = #{paperId} and score > 0")
    Integer average(@Param("paperId") int paperId);

}
