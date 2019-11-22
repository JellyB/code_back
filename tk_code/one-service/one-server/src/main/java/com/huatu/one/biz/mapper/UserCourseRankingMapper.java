package com.huatu.one.biz.mapper;

import com.huatu.one.base.mapper.BaseMapper;
import com.huatu.one.biz.model.UserCourseRanking;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * 课程排名考试类型
 *
 * @author geek-s
 * @date 2019-09-19
 */
@Repository
public interface UserCourseRankingMapper extends BaseMapper<UserCourseRanking> {

    @Select("select count(*) from user_course_ranking where openid = #{openid} and exam_type_id = #{examTypeId} and status = 1")
    Integer selectCountByOpenidAndExamTypeId(@Param("openid") String openid, @Param("examTypeId") Long examTypeId);

    @Update("update user_course_ranking set status = 0 where openid = #{openid} and exam_type_id = #{examTypeId}")
    void deleteByExamTypeIdAndOpenid(@Param("examTypeId") Long examTypeId, @Param("openid") String openid);
}
