package com.huatu.tiku.course.web.controller.v3;

import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.common.utils.reflect.ClassUtils;
import com.huatu.tiku.course.bean.AddressFormDTO;
import com.huatu.tiku.course.netschool.api.v3.AddressServiceV3;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/19 13:57
 */
@RestController
@RequestMapping("/v3/address")
public class AddressControllerV3 {
    @Autowired
    private AddressServiceV3 addressServiceV3;

    /**
     * 用户地址列表
     * @param usersession
     * @return
     */
    @GetMapping
    public Object findAddressList(@Token UserSession usersession) {
        Map<String,Object> params = HashMapBuilder.<String,Object>newBuilder()
                .put("action","getAddress")
                .put("username",usersession.getUname())
                .build();
        return ResponseUtil.build(addressServiceV3.findAddressList(RequestUtil.encrypt(params)),true);
    }

    /**
     * 保存地址
     * requestbody参数验证抛MethodArgumentNotValidException
     * @param address
     * @return
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createAddress(@RequestBody @Valid AddressFormDTO address,
                                @Token UserSession userSession) {
        Map<String, Object> params = ClassUtils.getBeanProperties(address);
        params.put("action","addAddress");
        params.put("username",userSession.getUname());
        return ResponseUtil.build(addressServiceV3.createAddress(RequestUtil.encryptParams(params)));
    }

    /**
     * 修改地址
     * @param address
     * @return
     */
    @PatchMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object updateAddress(@RequestBody AddressFormDTO address,
                                @PathVariable int id,
                                @Token UserSession userSession) {
        Map<String, Object> params = ClassUtils.getBeanProperties(address);
        params.put("action","modifyAddress");
        params.put("username",userSession.getUname());
        params.put("id",id);
        return ResponseUtil.build(addressServiceV3.updateAddress(RequestUtil.encryptParams(params)));
    }

    /**
     * 删除地址
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object deleteAddress(@PathVariable int id,
                                @Token UserSession userSession) {
        Map<String,Object> params = HashMapBuilder.<String,Object>newBuilder()
                .put("action","delAddress")
                .put("username",userSession.getUname())
                .put("id",id)
                .build();
        return ResponseUtil.build(addressServiceV3.deleteAddress(RequestUtil.encrypt(params)));
    }

}
