package com.huatu.tiku.position.biz.service.impl;

import com.huatu.tiku.position.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.position.biz.domain.BrowseRecord;
import com.huatu.tiku.position.biz.domain.Enroll;
import com.huatu.tiku.position.biz.respository.EnrollRepository;
import com.huatu.tiku.position.biz.service.EnrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wangjian
 **/
@Service
public class EnrollServiceImpl extends BaseServiceImpl<Enroll,Long> implements EnrollService {

    private EnrollRepository enrollRepository;

    @Autowired
    public EnrollServiceImpl(EnrollRepository enrollRepository) {
        this.enrollRepository = enrollRepository;
    }

    @Override
    public void addEnroll(Long userId, Long positionId) {
        Enroll enroll = new Enroll();
        enroll.setUserId(userId);
        enroll.setPositionId(positionId);
        enroll.setStatus((byte) 1);
        enrollRepository.save(enroll);
    }

    public Boolean findByUserIdAndPositionIdAndStatus(Long userId,Long positionId, Byte status){
        return null!=enrollRepository.findByUserIdAndPositionIdAndStatus(userId, positionId, status);
    }

    @Override
    public Integer getEnrollCount(Long id) {
        return enrollRepository.getEnrollCount(id);
    }

    @Transactional
    @Override
    public void removeEnroll(Long userId, Long positionId) {
        enrollRepository.removeEnroll(userId, positionId);
    }
}
