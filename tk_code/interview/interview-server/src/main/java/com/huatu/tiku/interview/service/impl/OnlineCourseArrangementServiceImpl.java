package com.huatu.tiku.interview.service.impl;

import com.huatu.tiku.interview.constant.NotificationTypeConstant;
import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.entity.po.ClassInfo;
import com.huatu.tiku.interview.entity.po.NotificationType;
import com.huatu.tiku.interview.entity.vo.response.NotificationTypeVO;
import com.huatu.tiku.interview.exception.ReqException;
import com.huatu.tiku.interview.repository.ClassInfoRepository;
import com.huatu.tiku.interview.repository.NotificationTypeRepository;
import com.huatu.tiku.interview.service.OnlineCourseArrangementService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author jbzm
 * @Date Create on 2018/1/17 17:21
 */
@Service
public class OnlineCourseArrangementServiceImpl implements OnlineCourseArrangementService {

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;
    @Autowired
    private ClassInfoRepository classInfoRepository;
    @Override
    public Boolean add(NotificationType notificationType) {
        notificationType.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
        notificationType.setType(1);
        notificationType.setBizStatus(NotificationTypeConstant.BizStatus.UN_PUSHED.getBizSatus());
        List<NotificationType> byClassId = notificationTypeRepository.findByClassIdAndTypeAndStatus(notificationType.getClassId(),1,1);
        System.out.println("是不是Null："+byClassId);
        if(byClassId.isEmpty()){
            notificationTypeRepository.save(notificationType);
        }else{
            if(byClassId.get(0).getId() == notificationType.getId()){
                notificationTypeRepository.save(notificationType);
            }else{
                throw new ReqException(ResultEnum.CLASS_ARRANGEMENT_UNIQUE);
            }
        }
        return true;
    }

    @Override
    public Object findById(Long id) {
        NotificationType one = notificationTypeRepository.findOne(id);
        NotificationTypeVO vo = new NotificationTypeVO();
        BeanUtils.copyProperties(one,vo);
        Long classId = one.getClassId();
        ClassInfo classInfo = classInfoRepository.findOne(classId);
        vo.setAreaId(classInfo.getAreaId());
        return vo;
    }
}
