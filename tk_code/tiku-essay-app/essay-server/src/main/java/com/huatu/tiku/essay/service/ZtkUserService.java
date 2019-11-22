package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.vo.user.ZtkUserVO;

import java.util.List;

/**
 * 砖题库用户
 *
 * @author geek-s
 * @date 2019-07-08
 */
public interface ZtkUserService {

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户集合
     */
    ZtkUserVO getById(Integer id);

    /**
     * 根据ID获取用户信息
     *
     * @param ids 用户ID集合
     * @return 用户集合
     */
    List<ZtkUserVO> getByIds(List<Integer> ids);

    /**
     * 根据用户名/手机号查询
     *
     * @param params 用户名/手机号
     * @return 用户列表
     */
    ZtkUserVO getByUsernameOrderMobile(String params);

    /**
     * 根据用户名/手机号查询
     *
     * @param params 用户名/手机号
     * @return 用户列表
     */
    List<ZtkUserVO> getByUsernameOrderMobiles(List<String> params);
}
