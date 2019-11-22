package com.huatu.one.biz.mapper;

import com.huatu.one.base.mapper.BaseMapper;
import com.huatu.one.biz.model.User;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户
 *
 * @author geek-s
 * @date 2019-08-26
 */
@Repository
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据openid获取数据权限
     *
     * @param openid 微信ID
     * @return 数据权限IDs
     */
    @Select("select c.`value` from `user` u join user_data_category dc on u.id = dc.user_id join data_category c on dc.data_category_id = c.id where u.openid = #{0}")
    Long[] selectDataCategoryByOpenid(String openid);

    /**
     * 根据opened获取角色
     *
     * @param openid 微信ID
     * @return 权限IDs
     */
    @Select("select m.`index` from `user` u join user_menu ur on u.id = ur.user_id join menu m on ur.menu_id = m.id where u.openid = #{0}")
    Integer[] getMenus(String openid);

    /**
     * 根据openid获取排名选项
     *
     * @param openid 微信ID
     * @return 数据权限IDs
     */
    @Select("select exam_type_id from user_course_ranking where openid = #{0} and status = 1")
    List<Long> selectCourseRankingByOpenid(String openid);
}