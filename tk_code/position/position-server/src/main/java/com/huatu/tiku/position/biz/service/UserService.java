package com.huatu.tiku.position.biz.service;

import com.huatu.tiku.position.base.service.BaseService;
import com.huatu.tiku.position.biz.domain.Area;
import com.huatu.tiku.position.biz.domain.User;
import com.huatu.tiku.position.biz.dto.*;

import java.util.List;
import java.util.Set;

/**
 * @author wangjian
 **/
public interface UserService extends BaseService <User,Long>{

    void updateUserInfo(UpdateUserDto dto, User user);

    List computeAreaUserInfo(Set<Area> areas,User user );

    User findByOpenId(String openId);

    void updateUserAreas(UpdateAreaDto dto, User user);

    void updateUserSpecialty(UpdateSpecialtyDto dto, User user);

    void updateUserEducation(UpdateEducationDto dto, User user);

    void updateUserExp(UpdateExpDto dto, User user);

    void updateUnionidByOpenId(String unionid, String openId);
}
