package com.huatu.ztk.user.controller.channel;

import com.google.common.base.Stopwatch;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.common.SourceType;
import com.huatu.ztk.user.service.ChannelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouwei
 * @Description:
 * @create 2018-06-07 上午11:49
 **/
@RestController
@RequestMapping(value = "/v1/channel", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ChannelController {

    private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);

    @Resource
    ChannelService channelService;


    /**
     * 上报idfa值
     *
     * @param deviceToken 手机的唯一标识
     * @param ip          用户IP
     * @param gmtCreate   时间戳
     * @param version     系统版本
     * @param model       手机型号
     * @param request
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Object channel(@RequestParam String deviceToken,
                          @RequestParam String ip,
                          @RequestParam long gmtCreate,
                          @RequestParam(defaultValue = "") String version,
                          @RequestParam(defaultValue = "") String model,
                          @RequestParam(defaultValue = "1") int isBreakPrison,
                          HttpServletRequest request) throws BizException {
        String cv = request.getHeader("cv");
        logger.info("channel method:{}", deviceToken);
        String agent = request.getHeader("User-Agent");
        if (StringUtils.isBlank(agent)) {
            agent = request.getHeader("user-agent");
        }
        try {
            if (isLegalAgent(agent)) {
                channelService.addChannel(deviceToken, ip, gmtCreate, version, model, cv,isBreakPrison);
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 排重接口
     *
     * @param idfa
     * @param appid
     * @param source
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object getChannel(@RequestParam String idfa,
                             @RequestParam String appid,
                             int source,
                             @RequestParam String ip,
                             @RequestParam long gmtCreate) throws BizException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<String, Integer> resultMap = new HashMap();
        if (SourceType.isContains(source)) {
            try {
                //this.dataForwarding(idfa, source);//蝉大师后台不再使用,不再转发
                resultMap = channelService.checkChannel(idfa, "", gmtCreate, source, false);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        logger.info("getChannel spentTime:{}", String.valueOf(stopwatch.stop()));
        return resultMap;
    }

    /**
     * 点击，激活回调
     *
     * @param idfa
     * @param
     * @return
     */
    @RequestMapping(value = "/updateChannelState", method = RequestMethod.GET)
    public Object updateChannelState(@RequestParam String idfa,
                                     @RequestParam int source,
                                     @RequestParam String callBack) throws Exception {
        if (SourceType.isContains(source)) {
            return channelService.saveCallBack(idfa, source, callBack);
        }
        return 0;
    }


    /**
     * 数据转发禅大师
     *
     * @param idfa
     * @param source
     */
    public void dataForwarding(String idfa, int source) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        //访问渠道的连接
        StringBuffer strURL = new StringBuffer();
        switch (source) {
            case SourceType.XIAO_MAO_ID:
                strURL.append(SourceType.XIAO_MAO_CALLBACK);
                break;
            case SourceType.QI_MAI_ID:
                strURL.append(SourceType.QI_MAI_CALLBACK);
                break;
            case SourceType.LAN_MAO_ID:
                strURL.append(SourceType.LAN_MAO_CALLBACK);
                break;
            case SourceType.DA_YOU_ID:
                strURL.append(SourceType.DA_YOU_CALLBACK);
                break;
            case SourceType.TIAN_TIAN_ID:
                strURL.append(SourceType.TIAN_TIAN_CALLBACK);
                break;
            case SourceType.CHAN_ID:
                strURL.append(SourceType.CHAN_CALLBACK);
                break;
            case SourceType.AI_YING_LI_ID:
                strURL.append(SourceType.AI_YING_LI_CALLBACK);
                break;
            case SourceType.BACKUP_NUMBER1_ID:
                strURL.append(SourceType.BACKUP_NUMBER1_CALLBACK);
                break;
            case SourceType.BACKUP_NUMBER2_ID:
                strURL.append(SourceType.BACKUP_NUMBER2_CALLBACK);
                break;
            case SourceType.BACKUP_NUMBER3_ID:
                strURL.append(SourceType.BACKUP_NUMBER3_CALLBACK);
                break;
            default:
                strURL.append(SourceType.DEFAULT_CALLBACK);
                break;
        }
        strURL.append("?idfa=")
                .append(idfa)
                .append("&callback= ");

        try {
            channelService.callBackUrl(strURL.toString());
            logger.info("dataForwarding success：{}", String.valueOf(stopwatch.stop()));
        } catch (Exception e) {
            e.printStackTrace();
        }

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
}

