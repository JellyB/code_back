package com.huatu.ztk.user.controller;

import com.huatu.ztk.user.bean.UcenterBind;
import com.huatu.ztk.user.bean.UcenterMember;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dao.UserDao;
import com.huatu.ztk.user.service.UcenterService;
import com.huatu.ztk.user.utils.UcenterUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@Deprecated
@RequestMapping(value = "phpUpdate")
public class DoUserInfoUpdateController {

    private static final Logger logger = LoggerFactory.getLogger(DoUserInfoUpdateController.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private UcenterService ucenterService;//中心用户

    /**
     * 此处从外部触发用户信息更改任务
     *
     * @return
     */
    @RequestMapping(value = "updateTask", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object updateTask(
            @RequestParam("page") int page
    ) {
        long l = System.currentTimeMillis();
        logger.info("开始处理用户信息<<<<<<<<<<<<<<<<<<<<<<<,");
        /**
         * 查询该数据是否需要处理
         */
        String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";
        Pattern pattern = Pattern.compile(regex);

        Predicate<String> phoneExit = (userName) -> {
            if (StringUtils.isBlank(userName)) {
                logger.info("账号为空 {}", userName);
                //账号为空
                return false;
            }
            if (!userName.startsWith("0")) {
                //不是以 0 开头的手机号
                logger.info("账号不是以0开头 {}", userName);
                return false;
            }
            if (userName.length() != 12) {
                logger.info("账号长度不符合 {}", userName);
                return false;
            }
            String trueMobile = userName.substring(1, userName.length());
            if (!pattern.matcher(trueMobile).matches()) {
                logger.info("非法手机号码 {}", trueMobile);
            }

            UcenterBind ucenterBind = ucenterService.findBind(trueMobile);
            if (ucenterBind != null) {
                //绑定表已经存在
                logger.info("绑定表已经存在，{}", trueMobile);
                return false;
            }
            UcenterMember member = ucenterService.findMemberByUsername(trueMobile);
            if (member != null) {
                //去掉0的手机号已经存在
                logger.info("去掉0的手机号已经在中心库存在 {}", trueMobile);
                return false;
            }
            return true;
        };
        /**
         * 获取待处理信息
         */
        List<UserDto> phpBadData = userDao.getPHPBadData(page);
        logger.info("待处理数据长度 {}", phpBadData.size());
        /**
         * 开始处理数据
         */
        HashMap<Object, Object> data = null;
        try {
            List<String> collect = phpBadData.parallelStream()
                    .filter(userDto -> phoneExit.test(userDto.getName()))
                    .map(userDto -> {
                        UcenterMember ucenterMember = ucenterService.findMemberByUsername(userDto.getName());
                        //带0的手机号存在，处理
                        logger.info("需要处理的账户 {},在中心库是存在：{}", userDto.getName(), null != ucenterMember);
                        if (null != ucenterMember) {
                            String username = UcenterUtils.getUsername();
                            //添加绑定信息
                            String trueMobile = userDto.getName().substring(1, userDto.getName().length());
                            ucenterService.ucBind(ucenterMember.getUid(), username, trueMobile, "");
                            //添加用户手机号
                            userDao.updateMobile(userDto.getName(), trueMobile);
                            logger.info("处理的手机号码 = {}", userDto.getName());
                            //更换手机绑定信息
                            return userDto.getName();
                        }
                        return "";
                    })
                    .filter(StringUtils::isNoneBlank)
                    .collect(Collectors.toList());
            logger.info("处理用户信息结束 = {}", System.currentTimeMillis() - l);
            data = new HashMap<>();
            data.put("data", collect);
        } catch (Exception e) {
            logger.info("处理失败信息 =  {}", e);
        } finally {
            return data;
        }
    }

}
