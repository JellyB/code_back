package com.huatu.ztk.user.controller.userAdvert;

import com.huatu.tiku.common.AdvertEnum;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.bean.MMessage;
import com.huatu.ztk.user.bean.Message;
import com.huatu.ztk.user.dao.AdvertMessageDao;
import com.huatu.ztk.user.service.AdvertMessageService;
import com.huatu.ztk.user.service.UserService;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.utils.MessageUtil;
import com.huatu.ztk.user.utils.VersionUtil;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/15
 * @描述 广告走CDN优化
 * 广告接口修改内容:客户端传递category,方便走CDN;ios审核状态不再判断,交给客户端;广告统一做本地缓存
 * 补充： /u/v5/users/bc/notice,广告功能已经去掉,不再提供此接口
 */
@RestController
@RequestMapping(value = "/v5/users/bc", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserAdvertControllerV5 {

    private static final Logger logger = LoggerFactory.getLogger(UserAdvertControllerV5.class);


    @Autowired
    UserSessionService userSessionService;

    @Autowired
    AdvertMessageService advertMessageService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    AdvertMessageDao advertMessageDao;

    @Autowired
    UserService userService;

    /**
     * 查询首页广告轮播图列表
     *
     * @param category 考试类别
     * @param fur      是否是白名单用户标识 0 非白名单;1 白名单
     * @param terminal 终端类型
     * @param cv       版本号
     * @return
     */
    @RequestMapping(value = "/list")
    public Object list(@RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN + "") int category,
                       @RequestParam(defaultValue = "0") int fur,
                       @RequestHeader(defaultValue = "1") int terminal,
                       @RequestHeader(defaultValue = "7.1.150") String cv) {

        List<Message> result = advertMessageService.findBannerListV3(category, terminal, cv, 0L, AdvertEnum.AppType.HTZX.getCode());
        //轮播图白名单
        //logger.info("非白名单用户广告图:{}", JsonUtil.toJson(result));
        if (fur == 1) {
            //logger.info("白名单逻辑");
            List<Message> filterWhiteUserName = MessageUtil.filterWhiteUserName(redisTemplate, advertMessageDao, advertMessageService, result);
            return filterWhiteUserName;
        }
        return result;
    }

    /**
     * m 站轮播图
     * @return
     */
    @RequestMapping(value = "/mList")
    public Object mList(){

        List<MMessage> result = advertMessageService.findMBannerList();
        return result;
    }



    /**
     * 查询首页弹出广告图列表
     *
     * @return
     */
    @RequestMapping(value = "/popup", method = RequestMethod.GET)
    public Object popup(@RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN + "") int category) throws BizException {
        List<Message> result = advertMessageService.findNewPopupList(category, 0L, AdvertEnum.AppType.HTZX.getCode());
        return result;
    }

    /**
     * 查询启动广告图列表
     *
     * @return
     */
	@RequestMapping(value = "/launch", method = RequestMethod.GET)
	public Object launch(@RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN + "") int category) throws BizException {
//		if (TerminalType.IPHONE == terminal && system.startsWith("13") && device.contains("iPad")
//				&& VersionUtil.compareVersion(cv, "7.2.110") == -1) {
//			logger.info("ipad问题版本,启动图返回空terminal:{},system:{},device:{},cv:{}", terminal, system, device, cv);
//			return new ArrayList<>();
//		}
		List<Message> result = advertMessageService.findLaunchList(category, 0L, AdvertEnum.AppType.HTZX.getCode());
		return result;
	}

}
