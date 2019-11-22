package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.entity.po.Area;
import com.huatu.tiku.interview.entity.po.User;
import com.huatu.tiku.interview.util.common.PageUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-05 下午4:31
 **/
public interface UserService {
    Boolean updateUser(User user, HttpServletRequest request);

    void save(User user);

    //关注时添加用户
    void createUser(String openId);

    User getUser(String openId);

    User getUserByOpenId(String openId);

    PageUtil<Map> findUserByConditions(int page, int pageSize, String content, long classId, long areaId);
    List<User> findAllUser();

    Long getCluss(String openId);

    List<User> findByClass(Long classId);

    List<User> findByPhone(String phone);

    List<Area> findAreaList();
}
