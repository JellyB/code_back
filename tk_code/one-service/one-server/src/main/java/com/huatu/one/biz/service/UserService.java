package com.huatu.one.biz.service;

import com.alibaba.fastjson.JSONObject;
import com.huatu.one.base.config.WeChatConfig;
import com.huatu.one.base.util.WeChatUtil;
import com.huatu.one.biz.mapper.UserMapper;
import com.huatu.one.biz.model.User;
import com.huatu.one.biz.vo.UserCheckVo;
import com.huatu.one.biz.vo.WxIdVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/**
 * 课表
 *
 * @author geek-s
 * @date 2019-08-26
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatConfig weChatConfig;

    @Autowired
    private VersionService versionService;

    /**
     * 根据wxId查询
     *
     * @param openid 微信ID
     * @return 用户信息
     */
    public User selectByOpenid(String openid) {
        Example example = new Example(User.class);
        example.and()
                .andEqualTo("openid", openid);

        List<User> users = userMapper.selectByExample(example);

        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * 检测用户状态
     *
     * @param openid 微信ID
     * @return 用户状态
     */
    public UserCheckVo check(String openid, String version) {
        User user = selectByOpenid(openid);

        // 小程序审核中匿名/未审核逻辑
        String auditVersion = versionService.getVersion();

        if (auditVersion.equals(version) && (user == null || user.getStatus().equals(1))) {
            return UserCheckVo.builder()
                    .status(2)
                    .menuIndexes(getScheduleMenu())
                    .build();
        }

        return UserCheckVo.builder()
                .status(user == null ? 0 : user.getStatus())
                .menuIndexes(getMenus(openid))
                .build();
    }

    /**
     * 获取微信ID
     *
     * @param code
     * @return
     */
    public Object getOpenid(String code) {
        // 发送请求，返回Json字符串
        String requestUrl = weChatConfig.requestUrl + "appid=" + weChatConfig.appId + "&secret=" + weChatConfig.secret + "&js_code=" + code + "&grant_type=" + weChatConfig.grantType;
        String str = WeChatUtil.httpRequest(requestUrl, "GET", null);

        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        return WxIdVo.builder().openid(jsonObject.get("openid").toString()).build();
    }

    /**
     * 注册用户
     *
     * @param user 用户信息
     * @return
     */
    public void register(User user) {
        User userCheck = selectByOpenid(user.getOpenid());

        if (userCheck == null) {
            user.setStatus(1);
            user.setGmtCreate(new Date());

            userMapper.insertSelective(user);
        }
    }

    /**
     * 更改显示状态
     *
     * @param wxId
     * @return
     */
    public Object updateStatus(String wxId) {
        Example example = new Example(User.class);
        example.and().andEqualTo("wxId", wxId).andEqualTo("status", 1);
        User user = userMapper.selectOneByExample(example);
        if (user != null) {
            user.setStatus(2);
            user.setGmtModified(new Date());
            userMapper.updateByPrimaryKeySelective(user);
        }
        return user != null ? user.getStatus() : 0;
    }

    /**
     * 根据微信ID获取数据权限
     *
     * @param openid 微信ID
     * @return 数据权限IDs
     */
    public Long[] selectDataCategoryByOpenid(String openid) {
        return userMapper.selectDataCategoryByOpenid(openid);
    }

    /**
     * 根据微信ID获取角色
     *
     * @param openid 微信ID
     * @return 权限IDs
     */
    public Integer[] getMenus(String openid) {
        return userMapper.getMenus(openid);
    }

    /**
     * 根据微信ID获取数据权限
     *
     * @param openid 微信ID
     * @return 数据权限IDs
     */
    public List<Long> selectCourseRankingByOpenid(String openid) {
        return userMapper.selectCourseRankingByOpenid(openid);
    }

    /**
     * 获取审核课表菜单
     *
     * @return
     */
    public Integer[] getScheduleMenu() {
        Integer[] menus = {1};
        return menus;
    }

}
