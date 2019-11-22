package com.huatu.ztk.user.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.Result;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.user.bean.PostAddress;
import com.huatu.ztk.user.service.UserPostAddressService;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-10-11 15:52
 */
@RestController
@RequestMapping(value = "/v1/users/address", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserPostAddressController {
    private static final Logger logger = LoggerFactory.getLogger(UserPostAddressController.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserPostAddressService userPostAddressService;

    /**
     * 查询用户所有地址
     * @param token
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PostAddress> list(@RequestHeader(required = false) String token) throws BizException{
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);

        List<PostAddress> addresses =  userPostAddressService.findAddress(userId);

        return addresses;
    }

    /**
     * 查询用户默认地址
     *
     * @return
     */
    @RequestMapping(value = "/default", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PostAddress defaultAddress(@RequestHeader(required = false) String token) throws BizException {
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);

        PostAddress address = userPostAddressService.findDefault(userId);

        if (address == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        return address;
    }

    /**
     * 设置用户默认地址
     *
     * @param token
     * @param id    用户地址id
     * @return
     */
    @RequestMapping(value = "/default", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result setDefaultAddress(@RequestHeader(required = false) String token, @RequestParam long id) throws BizException{
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);

        userPostAddressService.setDefaultAddress(userId, id);

        return SuccessMessage.create("设置默认地址成功");
    }

    /**
     * 更新用户地址
     *
     * @param token
     * @param postAddress 要更新的地址
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object update(@RequestHeader(required = false) String token,
                         @RequestBody PostAddress postAddress) throws BizException {

        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);

        postAddress.setUid(userId);
        userPostAddressService.update(postAddress);

        return SuccessMessage.create("更新地址成功");
    }

    /**
     * 新建用户地址
     * @param token
     * @param postAddress
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object create(@RequestHeader(required = false) String token,
                         @RequestBody PostAddress postAddress) throws BizException{
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);

        postAddress.setUid(userId);
        return userPostAddressService.create(postAddress);
    }

    /**
     * 删除用户地址
     * @param token
     * @param id 待删除的地址
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object delete(@RequestHeader(required = false) String token,@RequestParam long id) throws BizException{
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);

        userPostAddressService.deleteAddress(userId, id);

        return SuccessMessage.create("删除地址成功");
    }

}
