package com.huatu.ztk.user.controller.channel;

import com.huatu.ztk.user.service.ChannelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/8/27
 * @描述
 */
@RestController
@RequestMapping(value = "/v2/channel", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ChannelControllerV2 {


    private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);

    @Resource
    ChannelService channelService;

    /**
     * 上报idfa值(安卓专用)
     *
     * @param deviceToken android设备唯一标识
     * @param ip          用户ip地址
     * @param gmtCreate   时间戳
     * @param source      下载渠道号
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Object channel(@RequestParam String deviceToken,
                          @RequestParam String ip,
                          @RequestParam long gmtCreate,
                          @RequestParam int source,
                          @RequestHeader(defaultValue = "2") int terminal,
                          HttpServletRequest request) {

        logger.info("android channel method:{}", deviceToken);
        String agent = request.getHeader("User-Agent");
        if (StringUtils.isBlank(agent)) {
            agent = request.getHeader("user-agent");
        }
        try {
            if (isLegalAgent(agent)) {
                channelService.insertChannel(deviceToken, ip, gmtCreate, source, false, "", "", "", terminal);
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 简单判断agent是否合法
     *
     * @param agent
     * @return
     */
    private boolean isLegalAgent(String agent) {
        //安卓,ios,pc理论上都带agent,不带agent视为非法请求
        if (StringUtils.isBlank(agent)) {
            return false;
        }
        return agent.contains("okhttp") || agent.contains("netschool");

    }

    /**
     * 上报idfa值(面库专用)
     *
     * @param deviceToken android设备唯一标识
     * @param ip          用户ip地址
     * @param gmtCreate   时间戳
     * @param source      下载渠道号
     * @return
     */
    @RequestMapping(value = "cool", method = RequestMethod.POST)
    public Object coolChannel(@RequestParam String deviceToken,
                              @RequestParam String ip,
                              @RequestParam long gmtCreate,
                              @RequestParam String version,
                              @RequestParam int source,
                              @RequestHeader(required = false) String pm,
                              @RequestHeader(required = false) String cv,
                              @RequestHeader(required = false) int terminal,
                              @RequestHeader(required = false) String token,
                              HttpServletRequest request) {

        logger.info("cool channel method:{},手机型号:{},设备ID:{}", deviceToken, pm, terminal);
        String agent = request.getHeader("User-Agent");
        if (StringUtils.isBlank(agent)) {
            agent = request.getHeader("user-agent");
        }
        try {
            if (isLegalAgent(agent)) {
                channelService.insertChannel(deviceToken, ip, gmtCreate, source, true, version + "", pm, cv, terminal);
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


}
