package com.huatu.ztk.paper.service.v4.impl;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.api.QuestionStrategyDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.huatu.ztk.paper.common.PracticeErrors;
import com.huatu.ztk.paper.enums.CustomizeEnum;
import com.huatu.ztk.paper.service.PracticeService;
import com.huatu.ztk.paper.service.v4.CustomizeService;
import com.huatu.ztk.paper.service.v4.QuestionUserMetaService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class CustomizeServiceImpl implements CustomizeService {

    private static final Logger logger = LoggerFactory.getLogger(CustomizeServiceImpl.class);

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private QuestionStrategyDubboService questionStrategyDubboService;

    @Override
    public PracticePaper createPracticePaper(Integer pointId, int size, long userId, int subject, CustomizeEnum.ModeEnum modeEnum) throws BizException {
        switch (modeEnum) {
            case Look:
                return practiceService.createPracticePaper(pointId, size, userId, subject);
            case Write:
                QuestionStrategy questionStrategy = questionStrategyDubboService.randomCustomizeStrategy(userId, subject, pointId, size);
                if (CollectionUtils.isEmpty(questionStrategy.getQuestions())) {
                    throw new BizException(PracticeErrors.QUESTION_COUNT_NOT_ENOUGH);
                }
                final PracticePaper practicePaper = practiceService.toPracticePaper(questionStrategy, subject);
                QuestionPoint point = questionPointDubboService.findById(pointId);
                String name = "";
                if (null != point && pointId > 0) {
                    name = point.getName();
                } else {
                    name = questionStrategy.getModules().get(0).getName();
                }
                practicePaper.setName(practiceService.getPracticeName(PracticeService.CUSTOMIZE_PRACTICE, name));
                return practicePaper;
            case Default:
                return null;

        }
        return null;
    }


}
