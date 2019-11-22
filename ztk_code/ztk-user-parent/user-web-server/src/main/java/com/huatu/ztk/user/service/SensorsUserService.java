package com.huatu.ztk.user.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.huatu.ztk.user.common.RegFromEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.huatu.common.consts.SensorsEventEnum;
import com.huatu.common.consts.TerminalType;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.user.bean.UserSession;
import com.huatu.ztk.user.utils.SensorsUtils;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

/**
 * 神策分析相关逻辑
 * 
 * @author zhangchong
 *
 */
@Service
public class SensorsUserService {

	private static final Logger logger = LoggerFactory.getLogger(SensorsUserService.class);

	@Autowired
	private SensorsAnalytics sensorsAnalytics;

	@Autowired
	private SubjectDubboService subjectDubboService;

	/**
	 * 登录神策埋点
	 * 
	 * @param createTime
	 * @param terminal
	 * @param loginWay
	 * @param loginFirst
	 * @param
	 * 
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param terminal
	 */
	@Async
	public void loginAnalytics(UserSession session, Boolean loginFirst, String loginWay, int terminal, Long createTime,
			String anonymousId, String from, Boolean fromUc, String source) {
		try {
			logger.info("loginAnalytics start...loginFirst is:{}", loginFirst);
			HashMap<String, Object> saProperties = Maps.newHashMap();
			boolean flag = false;
			// 排除pc首次登录标识
			if (loginFirst != null && loginFirst == true && terminal != TerminalType.PC && "验证码".equals(loginWay)) {
				flag = true;
			}
			saProperties.put("login_first", flag);
			saProperties.put("user_phone", session.getMobile());
			saProperties.put("login_way", loginWay);
			saProperties.put("platform", SensorsUtils.getPlatform(terminal));
			logger.info("loginAnalytics param terminal is:{} anonymousId is:{}", terminal, anonymousId);
			if (terminal == TerminalType.PC && StringUtils.isNotBlank(anonymousId)) {// pc并且不是题库
				logger.info("pc login_first is:{}", saProperties.get("login_first"));
				String sourceStr = "其他";
				if(StringUtils.isNotBlank(source) && "1".equals(source)) {
					sourceStr = "中石油";
				}
				saProperties.put("sign_up_from", sourceStr);
				sensorsAnalytics.trackSignUp(session.getUcId(), anonymousId);
				sensorsAnalytics.track(session.getUcId() + "", true, SensorsEventEnum.PC_LOGIN_SUCCEED.getCode(),
						saProperties);
				sensorsAnalytics.flush();
				logger.info("pc loginAnalytics SensorsEventEnum.LOGIN_SUCCEED prop is:{}", saProperties.toString());
			} else if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD
					|| terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
				int regFrom = Integer.valueOf(from);
				if (terminal == regFrom) {// 如果不等来自非app端
					if (fromUc == null || fromUc == false) {
						// 过滤uc同步用户
						logger.info("app loginAnalytics login type:{} loginFirst:{} regFrom:{} ", loginWay, flag,
								regFrom);
						sensorsAnalytics.track(session.getUcId() + "", true, SensorsEventEnum.LOGIN_SUCCEED.getCode(),
								saProperties);
						sensorsAnalytics.flush();
						logger.info("app loginAnalytics SensorsEventEnum.LOGIN_SUCCEED prop is:{}",
								saProperties.toString());
					}
				}
			} else if (terminal == TerminalType.MOBILE && StringUtils.isNotBlank(anonymousId)) {
				// m站登录埋点 排除题库登录
				saProperties.put("login_first", flag);
				int regFrom = Integer.valueOf(from);
				logger.info("mobile loginAnalytics login type:{} loginFirst:{} regFrom:{} ", loginWay, flag, regFrom);
				sensorsAnalytics.track(session.getUcId() + "", true, SensorsEventEnum.M_LOGIN_SUCCEED.getCode(),
						saProperties);
				sensorsAnalytics.trackSignUp(session.getUcId(), anonymousId);
				sensorsAnalytics.flush();
				logger.info("mobile loginAnalytics SensorsEventEnum.LOGIN_SUCCEED prop is:{}", saProperties.toString());
			}
			saProperties.clear();
			// 上报用户名
			saProperties.put(SensorsEventEnum.LOGIN_NAME.getCode(), session.getUname());
			// 报考城市
			saProperties.put(SensorsEventEnum.EXAM_AREA.getCode(), session.getAreaName());
			String categoryName = subjectDubboService.getCategoryNameById(session.getCatgory());
			logger.info("用户:{}对应的考试类型为:{}", session.getUname(), categoryName);
			saProperties.put(SensorsEventEnum.EXAM_CATEGORY_AREA.getCode(), categoryName);
			if (createTime != null) {
				// 上报注册时间
				saProperties.put(SensorsEventEnum.REGISTER_TIME.getCode(),
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(createTime)));
			}
            logger.info("为null注册来源是:{}", session.getRegFrom());
            RegFromEnum regFromEnum = RegFromEnum.create(session.getRegFrom());
			saProperties.put(SensorsEventEnum.REGISTER_TYPE.getCode(), regFromEnum.getValue());
			sensorsAnalytics.profileSet(session.getUcId() + "", true, saProperties);
			logger.info("loginAnalytics profileSet prop is:{}", saProperties.toString());
		} catch (InvalidArgumentException e) {
			logger.error("sa track error:" + e);
		}
	}

	@Async
	public void registerAnalytics(String mobile, int terminal, String from, String ucId, String source) {
		try {
			logger.info("registerAnalytics start-----------");
			Map<String, Object> properties = Maps.newHashMap();
			properties.put("platform", SensorsUtils.getPlatform(terminal));
			properties.put("user_phone", mobile);
			properties.put("sign_up_type", from);
			String sourceStr = "其他";
			if(StringUtils.isNotBlank(source) && "1".equals(source)) {
				sourceStr = "中石油";
			}
			properties.put("sign_up_from", sourceStr);
			logger.info("registerAnalytics properties---------" + properties.toString());
			sensorsAnalytics.track(ucId, true, SensorsEventEnum.SIGNUP_SUCCEED.getCode(), properties);
			properties.clear();
			String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			properties.put(SensorsEventEnum.REGISTER_TIME.getCode(), date);
			properties.put("sign_up_from", sourceStr);
			logger.info("registerAnalytics profileSet properties---------" + properties.toString());
			sensorsAnalytics.profileSet(ucId, true, properties);
		} catch (InvalidArgumentException e) {
			logger.error("registerAnalytics error :{}", e);
		}
	}

	/**
	 * 更新注册时间
	 * 
	 * @param ucId
	 * @param sigTime
	 */
	@Async
	public void profileSetSigUpTime(String ucId, Long sigTime) {
		try {
			HashMap<String, Object> saProperties = Maps.newHashMap();
			if (sigTime != null) {
				saProperties.put(SensorsEventEnum.REGISTER_TIME.getCode(),
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(sigTime)));
				sensorsAnalytics.profileSet(ucId, true, saProperties);
			}
		} catch (InvalidArgumentException e) {
			logger.error("profileSetSigUpTime error :{}", e);
		}
	}

}
