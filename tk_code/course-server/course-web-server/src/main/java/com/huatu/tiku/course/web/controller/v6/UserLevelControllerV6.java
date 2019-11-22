package com.huatu.tiku.course.web.controller.v6;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v3.UserLevelServiceV3;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.ztk.api.v4.user.UserServiceV4;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户等级接口-paper阶段测试使用
 * 
 * @author zhangchong
 *
 */
@Slf4j
@RestController
@RequestMapping("/user")
@ApiVersion("v6")
public class UserLevelControllerV6 {
	@Autowired
	private UserLevelServiceV3 userLevelServiceV3;

	@Autowired
	private UserServiceV4 userServiceV4;

	@RequestMapping("/getLevelBatch")
	public Object getUserLevel(@RequestParam String userIds) {
		List<String> userIdList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(userIds);
		Map<Integer, String> userLevelMap = Maps.newHashMap();
		Map<String, String> userInfoMap = Maps.newHashMap();
		NetSchoolResponse response = userServiceV4.getUserLevelBatch(userIdList);
		if (ResponseUtil.isSuccess(response)) {
			List<Map<String, String>> userInfoList = (List<Map<String, String>>) response.getData();
			userInfoList.forEach(user -> {
				userInfoMap.put(user.get("id"), user.get("name"));
			});
			userIdList.forEach(userId -> {
				if (StringUtils.isNotEmpty(userInfoMap.get(userId))) {
					// 获取用户name
					Map<String, Object> params = HashMapBuilder.newBuilder().put("username", userInfoMap.get(userId))
							.put("action", 0).buildUnsafe();
					Map<String, Object> ret = (Map<String, Object>) ResponseUtil
							.build(userLevelServiceV3.getUserLevel(RequestUtil.encryptParams(params)), true);
					String level = (String) ret.get("level");
					userLevelMap.put(Ints.tryParse(userId), level);
				}

			});
		}

		return userLevelMap;
	}

}
