package com.huatu.tiku.interview.service.impl;

import com.alibaba.fastjson.JSON;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.env.IpUtils;
import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.constant.UserStatusConstant;
import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.entity.po.Area;
import com.huatu.tiku.interview.entity.po.User;
import com.huatu.tiku.interview.entity.po.WhiteList;
import com.huatu.tiku.interview.entity.result.PhpResult;
import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.exception.ReqException;
import com.huatu.tiku.interview.repository.AreaRepository;
import com.huatu.tiku.interview.repository.UserClassRelationRepository;
import com.huatu.tiku.interview.repository.UserRepository;
import com.huatu.tiku.interview.service.MobileService;
import com.huatu.tiku.interview.service.WhiteListSerive;
import com.huatu.tiku.interview.util.Crypt3Des;
import com.huatu.tiku.interview.util.MdSmsUtil;
import com.huatu.tiku.interview.util.common.RegexConfig;
import com.huatu.tiku.interview.util.common.UserRedisKeys;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/10 21:55
 * @Modefied By:
 */
@Service
@Slf4j
public class MobileServiceImpl implements MobileService {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AreaRepository areaRepository;
    @Autowired
    WhiteListSerive whiteListSerive;
    @Autowired
    UserClassRelationRepository userClassRelationRepository;

    @Value("${get_php_user_info_by_phone}")
    private String getPhpUserInfoByPhoneUrl;

    @Override
    public User checkPHP(String mobile, String openId, HttpServletRequest request) {

        User user = userRepository.findByOpenId(openId);
        if (user == null) {
            throw new ReqException(ResultEnum.OPENID_ERROR);
        }
        //参数加密
        String token = Crypt3Des.encryptMode("phone=" + mobile );
        // 请求php接口，查询用户信息
        String result = restTemplate.getForObject(getPhpUserInfoByPhoneUrl + token, String.class, token);
        log.debug("php接口返回数据:" + result);
        JSONObject jsonobject = JSONObject.fromObject(result);
        PhpResult phpResult = (PhpResult) JSONObject.toBean(jsonobject, PhpResult.class);
        //白名单验证
        Boolean isWhiteUser = false;
        List<WhiteList> whiteLists = whiteListSerive.getList();
        if (null != whiteLists && whiteLists.size() > 0) {
            for (WhiteList wl : whiteLists) {
                if (wl.getPhone().equals(mobile)) {
                    isWhiteUser = true;
                    break;
                }
            }
        }
        //白名单验证,不是白名单用户校验以下信息
        if(!isWhiteUser){
            //php验证（code 10000成功）
            if (!phpResult.getCode().equals("10000")) {
                throw new ReqException(ResultEnum.PHP_MOBILE_ERROR);
            }else{
                String userInfo = Crypt3Des.decryptMode(phpResult.getData());
                JSONObject jsonObject2 = JSONObject.fromObject(userInfo);

                String phone = jsonObject2.get("phone").toString();
                String areaName = jsonObject2.get("areaName").toString();
                String userName = jsonObject2.get("username").toString();
                String classTitle = jsonObject2.get("classTitle").toString();
                //处理地区
                if(StringUtils.isNotEmpty(areaName)){
                    log.debug("用户所属地区为：{}。",areaName);
                    areaName =  areaName.substring(0,2);
                }else{
                    log.error("用户所属地区为空，phone：{}。userName：{}",phone,userName);
                }
                List<Area> areaList = areaRepository.findByNameLikeAndBizStatusAndStatus
                        (areaName, WXStatusEnum.BizStatus.ONLINE.getBizSatus(), WXStatusEnum.Status.NORMAL.getStatus());
                if(CollectionUtils.isNotEmpty(areaList)){
                    user.setAreaId(areaList.get(0).getId());
                }
                user.setUserName(userName);
                user.setClassTitle(classTitle);
                user.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
            }
        }
        user.setPhone(mobile);
        //更新用户数据
        userRepository.save(user);

        String clientIp = null;
        try {
            final String real = request.getHeader("X-Real-IP");
            final String forwardedFor = request.getHeader("X-Forwarded-For");
            String agent = request.getHeader("User-Agent");
            if (StringUtils.isBlank(agent)) {
                agent = request.getHeader("user-agent");
            }
            final UserAgent userAgent = UserAgent.parseUserAgentString(agent);
            if (userAgent.getOperatingSystem().getDeviceType() == DeviceType.COMPUTER
                    || !isLegalAgent(agent)) {
                log.debug("filter mobile={},agent={},forwardedFor={}", mobile, agent, forwardedFor);
            }

            clientIp = Optional.ofNullable(IpUtils.getIpFromRequest(request)).orElse("unknow");
            log.debug("client={} send sms,mobile={}", clientIp, mobile);
            log.debug("forwardedFor={},mobile={}, agent={},realip={}", forwardedFor, mobile, agent, real);
        } catch (Exception e) {
            log.error("ex", e);
        }

        //这里开始发送验证码
        sendCaptcha(user,mobile, clientIp);
        return user;
    }


    @Override
    public Result userCaptcha(String mobile, String captcha, HttpServletRequest request) {
        //验证码对应的key
        String captchaKey = String.format(UserRedisKeys.CAPTCHA_MOBILE, mobile);
        //实际验证码
        final Object actualCaptcha = redisTemplate.opsForValue().get(captchaKey);

        if (actualCaptcha == null) {
            throw new ReqException(ResultEnum.CAPTCHA_EXPIRE);
        }

        //验证码错误
        if (!captcha.equals(actualCaptcha.toString())) {
            throw new ReqException(ResultEnum.CAPTCHA_ERROR);
        }
        String json = redisTemplate.opsForValue().get(mobile+captcha);
        User user = JSON.parseObject(json, User.class);
        if (null != user) {
            user.setPhone(mobile);
            user.setBizStatus(UserStatusConstant.BizStatus.BIND.getBizSatus());
            user = userRepository.save(user);
        }
        return Result.ok(ResultEnum.CAPTCHA_PASS);
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
     * 发送短信
     * @param mobile   手机号
     * @param clientIp
     * @throws BizException
     */
    @Override
    public void sendCaptcha(User user,String mobile, String clientIp) throws BizException {
        mobile = StringUtils.trimToEmpty(mobile);
        final SetOperations operations = redisTemplate.opsForSet();

        /**
         * 验证请求合法性
         */
        if (operations.isMember(UserRedisKeys.REJECT_MOBILES, mobile)) {
            log.info("it is robot,reject mobile={}", mobile);
            return;
        }

        if (StringUtils.isNoneBlank(clientIp) && operations.isMember(UserRedisKeys.REJECT_IPS, clientIp)) {
            log.info("it is robot,reject clientIp={}", clientIp);
            return;
        }


        final boolean isMobile = Pattern.matches(RegexConfig.MOBILE_PHONE_REGEX, mobile);
        if (!isMobile) {//非法的手机号
            //暂时先这样
            throw new ReqException(ResultEnum.PHONE_ILLEGAL);
        }

        String captchaKey = String.format(UserRedisKeys.CAPTCHA_MOBILE, mobile);
        String captchaMarkKey = String.format(UserRedisKeys.USER_CAPTCHA_MARK, mobile);

        final ValueOperations valueOperations = redisTemplate.opsForValue();
        final Object markObj = redisTemplate.opsForValue().get(captchaMarkKey);

        //markObj存在，说明距离上次发送验证码的时间未超过1分钟
        if (markObj != null) {
            throw new ReqException(ResultEnum.WAIT_TO_SEND);
        }

        //随机生成验证码
        String captcha = RandomStringUtils.randomNumeric(6);
        while (true) {
            if (!captcha.startsWith("0")) {//验证码不能以0开头
                break;
            }
            captcha = RandomStringUtils.randomNumeric(6);
        }

        //将验证码设置到redis里面,有效期设置为3分钟
        valueOperations.set(captchaKey, captcha, 1, TimeUnit.MINUTES);
        valueOperations.set(captchaMarkKey, mobile, 1, TimeUnit.MINUTES);

        redisTemplate.opsForValue().set(user.getPhone()+captcha, JSON.toJSONString(user));
        redisTemplate.expire(user.getPhone()+captcha, 60 * 1000, TimeUnit.SECONDS);
        //发送验证码
        MdSmsUtil.sendCaptcha(mobile, captcha);
    }


}
