package com.arj.monitor.service;

import com.arj.monitor.entity.User;
import com.arj.monitor.entity.UserSession;
import com.arj.monitor.exception.BizException;

/**
 * @author zhouwei
 * @create 2018-10-15 上午11:05
 **/
public interface TokenService {
  UserSession assertSession(UserSession userSession) throws BizException;
   UserSession assertAdminSession(UserSession userSession);
    // UserSession findByUserId(Long userId);
 // void updateSession(UserSession userSession);

  /**
   * 退出登陆
   * @param userSession
   */
 // void  outLogin(UserSession userSession);

  /**
   * 用户登录后创建新的session信息
   * @param user
   * @return
   */
  UserSession newUserSession(User user);

  /**
   * userSession 放入redis中如果不存在
   * @param userSession
   */
  void putUserSession2RedisIfAbsent(UserSession userSession);

  /**
   * 根据token去缓存中获取新的session信息
   * @param token
   * @return
   */
  UserSession getUserSessionFromRedis(String token);

  /**
   * redis 缓存时间重新续约
   * @param token
   */
  void updateSessionExpireTime(String token);
}
