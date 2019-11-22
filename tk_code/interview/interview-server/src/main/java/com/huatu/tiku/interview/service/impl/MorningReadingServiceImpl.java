package com.huatu.tiku.interview.service.impl;

import com.huatu.tiku.interview.constant.NotificationTypeConstant;
import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.entity.po.NotificationType;
import com.huatu.tiku.interview.exception.ReqException;
import com.huatu.tiku.interview.repository.NotificationTypeRepository;
import com.huatu.tiku.interview.service.MorningReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/13 17:17
 * @Description
 */
@Service
public class MorningReadingServiceImpl implements MorningReadingService {

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    @Override
    public Long add(NotificationType data) {
        data.setBizStatus(NotificationTypeConstant.BizStatus.UN_PUSHED.getBizSatus());
        NotificationType nt = notificationTypeRepository.save(data);
        if(nt == null){
            throw new ReqException(ResultEnum.INSERT_FAIL);
        }
        return nt.getId();
    }

    @Override
    public Boolean update(NotificationType reading) {
        return null;
    }

    @Override
    public void del(Long id) {
    }

    @Override
    public List<NotificationType> findAll() {
        Sort sort = new Sort("pushTime","desc");
        return notificationTypeRepository.findAll(sort);
    }

    @Override
    public NotificationType get(Long id) {

        NotificationType one = notificationTypeRepository.findOne(id);
        if(one == null){
            throw new ReqException(ResultEnum.FIND_FAIL);
        }
        return one;
    }


}
