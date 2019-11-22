package com.huatu.tiku.schedule.biz.service.imple;

import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.FeedbackUpdateLog;
import com.huatu.tiku.schedule.biz.repository.FeedbackUpdateLogRepository;
import com.huatu.tiku.schedule.biz.service.FeedbackUpdateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FeedbackUpdateLogServiceImpl extends BaseServiceImpl<FeedbackUpdateLog, Long> implements FeedbackUpdateLogService {

    @Autowired
    private FeedbackUpdateLogRepository feedbackUpdateLogRepository;

    @Override
    public Page<FeedbackUpdateLog> findByTypeAndFeedbackId(Integer type, Long feedbackId, Pageable page) {
        return feedbackUpdateLogRepository.findByTypeAndFeedbackId(type, feedbackId, page);
    }
}
