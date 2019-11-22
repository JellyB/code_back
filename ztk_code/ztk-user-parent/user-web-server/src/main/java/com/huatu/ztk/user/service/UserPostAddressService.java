package com.huatu.ztk.user.service;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.user.bean.PostAddress;
import com.huatu.ztk.user.common.RegexConfig;
import com.huatu.ztk.user.dao.UserPostAddressDao;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 收货地址管理Service
 * Created by linkang on 10/11/16.
 */

@Service
public class UserPostAddressService {

    private static final Logger logger = LoggerFactory.getLogger(UserPostAddressService.class);

    @Autowired
    private UserPostAddressDao userPostAddressDao;

    //默认地址
    private final static int DEFAULT_VALUE = 1;
    //非默认地址
    private final static int NOT_DEFAULT_VALUE = 2;
    //最短详细地址
    private final static int MIN_ADDRESS_LENGTH = 5;
    //最长详细地址
    private final static int MAX_ADDRESS_LENGTH = 60;

    /**
     * 查询所有地址
     * @param userId
     * @return
     */
    public List<PostAddress> findAddress(long userId) {
        return userPostAddressDao.findAll(userId);
    }

    /**
     * 查询默认地址
     * @param userId
     * @return
     */
    public PostAddress findDefault(long userId) throws BizException{
        PostAddress defaultAddress = userPostAddressDao.findDefault(userId);
        return defaultAddress;
    }

    /**
     * 设置默认地址
     * @param userId
     * @param id
     * @throws BizException
     */
    public void setDefaultAddress(long userId, long id) throws BizException{
        //查询当前默认地址
        PostAddress currentDefault = findDefault(userId);

        if (currentDefault != null && currentDefault.getId() != id) {
            //将当前默认地址设置为非默认
            userPostAddressDao.setDefaultValue(currentDefault.getId(), NOT_DEFAULT_VALUE);
            userPostAddressDao.setDefaultValue(id, DEFAULT_VALUE);
        }

    }

    /**
     * 删除地址
     * @param userId
     * @param id
     */
    public void deleteAddress(long userId, long id) throws BizException{
        PostAddress currentDefault = findDefault(userId);

        //如果删除的是默认地址，将列表的第2个设置为默认
        if (currentDefault != null && currentDefault.getId() == id) {
            List<PostAddress> addresses = userPostAddressDao.findAll(userId);
            if (addresses.size() > 1) {
                userPostAddressDao.setDefaultValue(addresses.get(1).getId(), DEFAULT_VALUE);
            }
        }

        userPostAddressDao.deleteOne(userId, id);
    }

    /**
     *新建地址
     * @param postAddress
     */
    public PostAddress create(PostAddress postAddress) throws BizException{
        checkAddress(postAddress);

        PostAddress defaultAddress = findDefault(postAddress.getUid());

        //如果不存在默认地址，设置为默认
        if (defaultAddress == null) {
            postAddress.setDefalut(DEFAULT_VALUE);
        } else {
            postAddress.setDefalut(NOT_DEFAULT_VALUE);
        }

        //创建时间
        postAddress.setCreateTime(new Date());

        userPostAddressDao.insert(postAddress);
        return postAddress;
    }

    /**
     * 更新地址
     * @param postAddress
     */
    public void update(PostAddress postAddress) throws BizException{
        checkAddress(postAddress);

        //防止传递错误的id
        if (postAddress.getId() <= 0) {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        userPostAddressDao.update(postAddress);
    }

    /**
     * 检查参数
     * @param postAddress
     * @throws BizException
     */
    private void checkAddress(PostAddress postAddress) throws BizException{

        if (StringUtils.isBlank(postAddress.getConsignee())) {
            throw new BizException(ErrorResult.create(1000101,"请填写收货人信息"));
        }

        if (!postAddress.getConsignee().matches(RegexConfig.CONSIGNEE_NAME_REGEX)) {
            throw new BizException(ErrorResult.create(1000101,"收货人：2-15个字符限制，只支持中英文"));
        }

        //手机为空
        if (StringUtils.isBlank(postAddress.getPhone())) {
            throw new BizException(ErrorResult.create(1000101,"请填写手机号"));
        }

        //检查手机号格式
        if (!postAddress.getPhone().matches(RegexConfig.MOBILE_PHONE_REGEX)) {
            throw new BizException(ErrorResult.create(1000101,"手机号码无效"));
        }

        if (StringUtils.isBlank(postAddress.getCity()) || StringUtils.isBlank(postAddress.getProvince())) {
            throw new BizException(ErrorResult.create(1000101,"请选择地区信息"));
        }

        if (StringUtils.isBlank(postAddress.getAddress())) {
            throw new BizException(ErrorResult.create(1000101,"请填写详细地址"));
        }

        if (postAddress.getAddress().length() < MIN_ADDRESS_LENGTH
                || postAddress.getAddress().length() > MAX_ADDRESS_LENGTH) {
            throw new BizException(ErrorResult.create(1000101,"详细地址：5-60个字符限制"));
        }

    }

}
