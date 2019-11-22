package com.huatu.ztk.pc.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.spring.annotation.RequestToken;
import com.huatu.ztk.pc.bean.Share;
import com.huatu.ztk.pc.service.HuatuShareService;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dubbo.UserDubboService;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Created by lijun on 2018/9/28
 */
@CrossOrigin
@Controller
@RequestMapping(value = "/v1/shareRedPackage")
public class ShareRedPackageController {

    private static final Logger logger = LoggerFactory.getLogger(ShareRedPackageController.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private HuatuShareService huatuShareService;
    @Autowired
    private UserDubboService userDubboService;

    /**
     * 获取分享详情
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object getShareDetail(
            @RequestToken String token,
            @RequestParam long redPackageId,
            @RequestParam String moneyNum,
            @RequestParam String param
    ) throws BizException {
        //WxChatShareUtil.assertWeiXinInfo 解决微信二次分享问题 参考文件 match.ftl
        userSessionService.assertSession(token);
        long uid = userSessionService.getUid(token);
        String nick = userSessionService.getNick(token);
        UserDto userDto = userDubboService.findById(uid);
        if (StringUtils.isBlank(nick)) {
            String mobileNo = userSessionService.getMobileNo(token);
            if (StringUtils.isNotBlank(mobileNo)) {
                nick = transPhoneToNick(mobileNo);
            } else {
                nick = "您的好友";
            }
        }
        //头像信息
        String avatar;
        if (userDto != null && userDto.getAvatar() != null) {
            avatar = userDto.getAvatar();
        } else {
            avatar = "http://tiku.huatu.com/cdn/images/vhuatu/avatars/default.png";
        }
        Share share = huatuShareService.getRedPackageShare(uid, nick, avatar, param, redPackageId, moneyNum);
        //返回前端的 url 需要处理 添加参数
        share.setUrl(share.getUrl() + "?p=" + share.getId());
        return share;
    }

    /**
     * 根据ID 获取详情
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object getShareDetail(@PathVariable("id") String id) {
        return huatuShareService.getRedPackageShareById(id);
    }


    /**
     * 压缩参数
     */
    public static String gzip(String primStr) {
        if (StringUtils.isBlank(primStr)) {
            return StringUtils.EMPTY;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
            gzipOutputStream.write(primStr.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String encode = new BASE64Encoder().encode(outputStream.toByteArray());
        if (StringUtils.isNotBlank(encode)) {
            encode = encode.replaceAll("\n", "");
        }

        return encode;
    }

    public static String transPhoneToNick(String phoneNo) {
        final String prefix = "****";
        if (StringUtils.isBlank(phoneNo)) {
            return prefix;
        }
        int prefixNo = phoneNo.startsWith("0") ? 1 : 0;
        try {
            return phoneNo.substring(0 + prefixNo, 3) + prefix + phoneNo.substring(7 + prefixNo, phoneNo.length());
        } catch (Exception e) {
            //如果手机号长度出现问题
            return prefix;
        }
    }
}
