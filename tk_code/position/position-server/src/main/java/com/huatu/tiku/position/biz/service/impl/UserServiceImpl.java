package com.huatu.tiku.position.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.tiku.position.base.exception.NoLoginException;
import com.huatu.tiku.position.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.position.biz.domain.Area;
import com.huatu.tiku.position.biz.domain.Specialty;
import com.huatu.tiku.position.biz.domain.User;
import com.huatu.tiku.position.biz.dto.*;
import com.huatu.tiku.position.biz.enums.Education;
import com.huatu.tiku.position.biz.respository.AreaRepository;
import com.huatu.tiku.position.biz.respository.UserRepository;
import com.huatu.tiku.position.biz.service.UserService;
import com.huatu.tiku.position.biz.util.RedisUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangjian
 **/
@Service
public class UserServiceImpl extends BaseServiceImpl<User,Long> implements UserService {

    private final UserRepository userRepository;

    private final AreaRepository areaRepository;

    private static final String POSITION_USER="positionUser:";

    private RedisUtil redisUtil;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, AreaRepository areaRepository, RedisUtil redisUtil) {
        this.userRepository = userRepository;
        this.areaRepository = areaRepository;
        this.redisUtil = redisUtil;
    }

    @Override
    @Transactional
    public void updateUserInfo(UpdateUserDto dto, User user) {
        BeanUtils.copyProperties(dto, user);
        List<Long> areaIds = dto.getAreaIds();
        Set<Area> areas = Sets.newHashSet();
        if(null!=areaIds&&!areaIds.isEmpty()){
            areaIds.forEach(id->{
                Area area=new Area();
                    area.setId(id);
                    areas.add(area);
            });
        }
        user.setAreas(areas);
        if(null!=dto.getEnglishTypes()&&!dto.getEnglishTypes().isEmpty()) {
            user.setEnglishType(dto.getEnglishTypes().stream().collect(Collectors.joining(",")));//英语
        }else{
            user.setEnglishType(null);
        }
        if(null!=dto.getCertificates()&&!dto.getCertificates().isEmpty()) {
            user.setCertificate(dto.getCertificates().stream().collect(Collectors.joining(",")));//证书
        }else{
            user.setCertificate(null);//证书
        }
        userRepository.save(user);
        deleteRedisUser(user.getOpenId());//修改时 手动清除缓存
    }

    /**
     * 指定地区相同条件相同人数
     * @param areas 地区集合
     */
    @Override
    public List computeAreaUserInfo(Set<Area> areas,User user ) {
        List<Area> areasList= Lists.newArrayList(areas);
        areasList.sort((o1,o2)->(o2.getId().compareTo(o1.getId())));
        Specialty specialty = user.getSpecialty();//用户专业
        Long parentSpecialtyId=null;
        if(null!=specialty&&null!=specialty.getParentId())
            parentSpecialtyId = specialty.getParentId();//用户上级专业id
        Education education = user.getEducation();//用户学历
        Map<Long,Map> areaMap= Maps.newHashMap();
        List resultList=new ArrayList();//封装结果
        for(Area area:areasList){
            List areaBean=new ArrayList();
            Map areaBeanMap = areaMap.get(area.getId());
            if(null==areaBeanMap){
                Integer countByArea = userRepository.findCountByArea(area.getId());//指定地区人数
                Integer userCount = userRepository.getCountByAreaUserInfo(area.getId(),
                        parentSpecialtyId,
                        null==education?null:education.ordinal());//指定地区人数
                areaBeanMap = ImmutableMap.of("name", area.getName(), "count", countByArea, "type", area.getType(), "userCount", userCount);
                areaMap.put(area.getId(),areaBeanMap);
            }else{
                continue;
            }
            areaBean.add(areaBeanMap);
            if(null!=area.getParentId()&&
                    !(area.getParentArea().getName().endsWith("市")&&area.getType()==2)){
                Area parentArea = area.getParentArea();
                Map parentBeanMap = areaMap.get(parentArea.getId());
                if(null==parentBeanMap){
                    Integer countByParentId = userRepository.findCountByParentId(parentArea.getId());
                    Integer userCountByParentId = userRepository.getCountByParentIdUserInfo(parentArea.getId(),
                            parentSpecialtyId,
                            null==education?null:education.ordinal());//指定地区人数
                    parentBeanMap = ImmutableMap.of("name",parentArea.getName(),"count",countByParentId,"type",parentArea.getType(),"userCount",userCountByParentId);
                    areaMap.put(parentArea.getId(),parentBeanMap);
                }
                areaBean.add(parentBeanMap);
                if(null!=parentArea.getParentId()&&
                        !parentArea.getParentArea().getName().endsWith("市")){ //有父级城市并且不是不是直辖市
                    Area sourceArea = parentArea.getParentArea();//省级城市
                    Map sourceBeanMap = areaMap.get(sourceArea.getId());
                    if(null==sourceBeanMap){
                        List<Long> idsBySourceId = areaRepository.findIdsByParentIdIn(sourceArea.getId());//下属市ids
                        Integer countBySourceId = userRepository.findCountByParentIds(idsBySourceId);
                        Integer userCountBySourceId = userRepository.getCountByParentIdsUserInfo(idsBySourceId,
                                parentSpecialtyId,
                                null==education?null:education.ordinal());//指定地区人数
                        sourceBeanMap = ImmutableMap.of("name",sourceArea.getName(),"count",countBySourceId,"type",sourceArea.getType(),"userCount",userCountBySourceId);
                        areaMap.put(sourceArea.getId(),sourceBeanMap);
                    }
                    areaBean.add(sourceBeanMap);
                }
            }
            resultList.add(areaBean);
        }
        return resultList;
    }

    @Override
    @Transactional
    public User findByOpenId(String openId) {
        User user;
        String userString = redisUtil.get(POSITION_USER+ openId);
        if(StringUtils.isNotBlank(userString)){
            user = JSON.parseObject(userString, User.class);
        }else{
            user=userRepository.findByOpenId(openId);
            if(null!=user) { //没有用户
                redisUtil.set(POSITION_USER + openId, JSON.toJSONString(user),60L);
//                redisUtil.set("positionUser:" + openId, JSON.toJSONString(user),1800L);
            }else{
                throw new NoLoginException("请登录");
            }
        }
        return user;
    }

    @Override
    public void updateUserAreas(UpdateAreaDto dto, User user) {
        List<Long> areaIds = dto.getAreaIds();
        Set<Area> areas = Sets.newHashSet();
        if(null!=areaIds&&!areaIds.isEmpty()){
            areaIds.forEach(id->{
                Area area=new Area();
                area.setId(id);
                areas.add(area);
            });
        }
        user.setAreas(areas);//取值
        userRepository.save(user);//存值
        deleteRedisUser(user.getOpenId());//清缓存
    }

    @Override
    public void updateUserSpecialty(UpdateSpecialtyDto dto, User user) {
        user.setSpecialtyId(dto.getSpecialtyId());//取值
        userRepository.save(user);//存值
        deleteRedisUser(user.getOpenId());//清缓存
    }

    @Override
    public void updateUserEducation(UpdateEducationDto dto, User user) {
        user.setEducation(dto.getEducation());//取值
        userRepository.save(user);//存值
        deleteRedisUser(user.getOpenId());//清缓存
    }

    @Override
    public void updateUserExp(UpdateExpDto dto, User user) {
        user.setExp(dto.getExp());//取值
        userRepository.save(user);//存值
        deleteRedisUser(user.getOpenId());//清缓存
    }

    @Override
    public void updateUnionidByOpenId(String unionid, String openId) {
        Integer result = userRepository.updateUnionidByOpenId(unionid, openId);
        if(result!=null&&result>=0){//如果成功
            deleteRedisUser(openId);//删除缓存
        }
    }

    //手动清除缓存
    private void deleteRedisUser(String openId){
        redisUtil.remove(POSITION_USER + openId);//修改时 手动清除缓存
    }
}
