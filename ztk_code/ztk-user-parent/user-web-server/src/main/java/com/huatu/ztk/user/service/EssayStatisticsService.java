package com.huatu.ztk.user.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dao.UserDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author jbzm
 * @Date Create on 2018/1/6 20:12
 */
@Service
public class EssayStatisticsService {

    @Autowired
    private UserDao userDao;
    public List<UserDto> findUserById(List<UserDto> userList) {
        List<UserDto> userDtos= Lists.newLinkedList();
        for(UserDto userDto:userList){
            UserDto userDtoResult = null;
            if(userDto.getId() > 0){
                userDtoResult = userDao.findById(userDto.getId());
            }else if(StringUtils.isNotBlank(userDto.getMobile())){
                userDtoResult = userDao.findByMobile(userDto.getMobile());
            }
            if(null!=userDtoResult){
                userDtos.add(userDtoResult);
            }
        }
        return userDtos;
    }
}
