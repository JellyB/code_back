package com.huatu.ztk.user.task;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.huatu.ztk.user.bean.UcenterBind;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dao.UcenterDao;
import com.huatu.ztk.user.dao.UserDao;
import com.huatu.ztk.user.service.UcenterService;
import com.huatu.ztk.user.service.UserService;

/**
 * 
 * @author zhangchong
 *
 */
//@Component
public class UCenterCorrectTask {
	private static final Logger logger = LoggerFactory.getLogger(UCenterCorrectTask.class);

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private UcenterService ucenterService;

	@Autowired
	private UcenterDao ucenterDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private UserService userService;

	// 开始刷的用户id//12504275
	public static final int startUserId = 13283170;
	public static boolean flag = true;

	public static final String CORRECTUSERID = "ucenter.correct.uid";

	// 30秒钟执行一次
	//@Scheduled(cron = "0/20 * * * * ?")
//	public void ucenterTask() {
//		if (!flag) {
//			logger.info("ucenterTask no complate.");
//			return;
//		}
//		try {
//			flag = false;
//			logger.info("ucenterTask task start.");
//			String uidStr = (String) redisTemplate.opsForValue().get(CORRECTUSERID);
//			Integer uid = 0;
//			if (uidStr == null) {
//				uid = startUserId;
//			} else {
//				uid = Integer.valueOf(uidStr);
//			}
//
//			List<Map<String, Object>> findBindList = ucenterService.findCorrectBindList(uid);
//			if (findBindList != null) {
//				findBindList.stream().filter(map -> map.get("phone").toString().length() == 11).forEach(bindInfo -> {
//					if (bindInfo != null) {
//						logger.info("开始处理bindInfo:{}", bindInfo.toString());
//						String phone = (String) bindInfo.get("phone");
//						List<UcenterBind> bindList = ucenterDao.findBindList(phone);
//						Optional<UcenterBind> ztkBind = bindList.stream()
//								.filter(bind -> bind.getUsername().contains("app_ztk")).findFirst();
//						Optional<UcenterBind> xueBind = bindList.stream()
//								.filter(bind -> bind.getUsername().contains("xue")).findFirst();
//
//						if (ztkBind.isPresent()) {
//							UcenterBind ucenterBind = ztkBind.get();
//							// 修改本地用户名称
//							UserDto localUser = userService.findByMobile(ucenterBind.getPhone());
//							if (localUser != null && xueBind.isPresent()) {
//								userDao.modifyUname(localUser.getId(), xueBind.get().getUsername());
//								logger.info("本地用户:{} 修改用户名为:{}", localUser.getName(), xueBind.get().getUsername());
//								// 删除uc用户
//								ucenterDao.delUserById(ucenterBind.getId());
//							}
//						}
//					}
//				});
//			}
//			Integer start = uid - 1000;
//			redisTemplate.opsForValue().set(CORRECTUSERID, String.valueOf(start));
//		} catch (Exception ex) {
//			logger.error("ex", ex);
//		} finally {
//			flag = true;
//		}
//	}
	@Scheduled(cron = "0/20 * * * * ?")
	public void updateUserInfo() {

		if (!flag) {
			logger.info("ucenterTask no complate.");
			return;
		}
		try {
			flag = false;
			logger.info("ucenterTask task start.");

			List<Map<String, Object>> findBindList = ucenterService.findCorrectBindList(1);
			if (findBindList != null) {
				findBindList.stream().filter(map -> map.get("phone").toString().length() == 11).forEach(bindInfo -> {
					if (bindInfo != null) {
						logger.info("开始处理bindInfo:{}", bindInfo.toString());
						String phone = (String) bindInfo.get("phone");
						List<UcenterBind> bindList = ucenterDao.findBindList(phone);
						UcenterBind local = new UcenterBind();
						UcenterBind other = new UcenterBind();
						for (UcenterBind bind : bindList) {
							if (bind.getUsername().contains("app_ztk")) {
								local = bind;
							} else {
								other = bind;
							}
						}
						if (local != null) {
							// 存在需要判断是否为在线后插入数据
							if (local.getId() > other.getId()) {
								// 处理数据
								UserDto localUser = userService.findByMobile(other.getPhone());
								if (localUser != null) {
									// 查询本地是否有此用户名
									UserDto findByName = userDao.findByName(other.getUsername());
									if (findByName == null) {
										userDao.modifyUname(localUser.getId(), other.getUsername());
										logger.info("本地用户:{} 修改用户名为:{}", localUser.getName(), other.getUsername());
										// 删除uc用户
										ucenterDao.delUserById(local.getId());
										logger.info("删除ucenter用户:{}", local.getUsername());
									} else {
										logger.info("本地存在用户:{},不修改", findByName.getName());
									}
								}

							}

						}

					}
				});
			}
		} catch (Exception ex) {
			logger.error("ex", ex);
		} finally {
			flag = true;
		}

	}
}
