package com.huatu.tiku.interview.service.impl;

import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.constant.UserStatusConstant;
import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.entity.po.Area;
import com.huatu.tiku.interview.entity.po.User;
import com.huatu.tiku.interview.entity.po.UserClassRelation;
import com.huatu.tiku.interview.exception.ReqException;
import com.huatu.tiku.interview.repository.AreaRepository;
import com.huatu.tiku.interview.repository.ClassInfoRepository;
import com.huatu.tiku.interview.repository.UserClassRelationRepository;
import com.huatu.tiku.interview.repository.UserRepository;
import com.huatu.tiku.interview.repository.impl.UserRepositoryImpl;
import com.huatu.tiku.interview.service.UserService;
import com.huatu.tiku.interview.util.common.PageUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-05 下午4:32
 **/
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    ClassInfoRepository classInfoRepository;

    @Autowired
    UserClassRelationRepository userClassRelationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryImpl userRepositoryImpl;
    @Autowired
    AreaRepository areaRepository;

    @Override
    public Boolean updateUser(User user, HttpServletRequest request) {

        User user_ = userRepository.findByOpenId(user.getOpenId());
        if (user_ != null) {
            user_.setSex(user.getSex());
            user_.setName(user.getName());
            user_.setIdCard(user.getIdCard());
            user_.setPregnancy(user.getPregnancy());
            user_.setNation(user.getNation());
            user_.setKeyContact(user.getKeyContact());
            user_.setAgreement(user.getAgreement());
            user_.setBizStatus(UserStatusConstant.BizStatus.COMPLETED.getBizSatus());
            user = userRepository.save(user_);

            //默认都是一班,保存用户和班级关联关系
            //查询用户班级关系
            List<UserClassRelation> relationList = userClassRelationRepository.findByOpenIdAndStatus
                    (user.getOpenId(), WXStatusEnum.Status.NORMAL.getStatus());
            if(CollectionUtils.isNotEmpty(relationList)){
                //已经绑定过班级信息

            }else{
                UserClassRelation relation = new UserClassRelation().builder()
                        .classId(1)
                        .boundType(1)
                        .openId(user.getOpenId())
                        .build();
                relation.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
                userClassRelationRepository.save(relation);
            }

        }


        return user == null ? false : true;
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public void createUser(String openId) {
        User user = new User();
        user.setOpenId(openId);
        user.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
        user.setBizStatus(UserStatusConstant.BizStatus.UN_BIND.getBizSatus());
        userRepository.save(user);
    }

    @Override
    public User getUser(String openId) {
        User user = null;
        List<User> userList = userRepository.findByOpenIdAndStatus(openId, WXStatusEnum.Status.NORMAL.getStatus());
        if(CollectionUtils.isNotEmpty(userList)){
            user = userList.get(0);
        }
        return user;
    }


    @Override
    public User getUserByOpenId(String openId) {
        return userRepository.findByOpenId(openId);
    }

    @Override
    public PageUtil<Map> findUserByConditions(int page,int pageSize,String content,long classId,long areaId) {
        long count = userRepositoryImpl.count(content, classId, areaId);
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (count != 0) {
            mapList.addAll(userRepositoryImpl.listForLimit(page, pageSize, content, classId, areaId));
        }
        PageUtil result = PageUtil.builder()
                .result(mapList)
                .next(count > page * pageSize ? 1 : 0)
                .total(count)
                .totalPage((int) (Math.ceil((double) count / (double) pageSize)))
                .build();
        return result;
    }

    @Override
    public List<User> findAllUser() {
        return userRepository.findByStatusAndBizStatus(WXStatusEnum.Status.NORMAL.getStatus(),UserStatusConstant.BizStatus.BIND.getBizSatus());
    }

    @Override
    public Long getCluss(String openId) {
        List<UserClassRelation> list = userClassRelationRepository.findByOpenIdAndStatus(openId, 1);
        System.out.println("进入获取班级ID");
        // 如果学员所在班级大于1，报错
        if (list.size() > 1) {
            throw new ReqException(ResultEnum.CLASS_UNIQUE_ERROR);
        }
        if (list.isEmpty()) {
//            throw new ReqException(ResultEnum.CLASS_NULL_ERROR);
            return 0L;
        }

        return list.get(0).getClassId();
    }

    @Override
    public List<User> findByClass(Long classId) {
        return userRepository.findByClassIdMy(classId);
    }

    @Override
    public List<User> findByPhone(String phone) {
        return userRepository.findByPhoneAndStatus(phone,1);
    }

    @Override
    public List<Area> findAreaList() {
         return  areaRepository.findByPIdAndStatusOrderBySortAsc(0, WXStatusEnum.Status.NORMAL.getStatus());
    }

}
