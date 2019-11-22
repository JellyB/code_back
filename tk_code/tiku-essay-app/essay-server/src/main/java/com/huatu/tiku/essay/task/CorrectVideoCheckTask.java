package com.huatu.tiku.essay.task;

import com.huatu.tiku.essay.constant.status.EssayLabelStatusConstant;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.correct.EssayPaperLabelTotal;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.repository.EssayLabelTotalRepository;
import com.huatu.tiku.essay.repository.v2.EssayPaperLabelTotalRepository;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author huangqingpeng
 * @title: CorrectVideoCheckTask
 * @description: TODO
 * @date 2019-08-0100:10
 */
@Component
@Slf4j
public class CorrectVideoCheckTask extends TaskService {


    private static final long expireTime = 1;
    private static final String cacheKey = "correct_video_status_check_task_lock";

    @Autowired
    EssayLabelTotalRepository essayLabelTotalRepository;

    @Autowired
    EssayPaperLabelTotalRepository essayPaperLabelTotalRepository;

    @Autowired
    CorrectOrderService correctOrderService;

    @Scheduled(fixedRate = 60000 * 2)       //一分钟执行一次派单
    public void checkVideoStatus() {
        task();
    }

    @Override
    public void run() {
        log.info("开始检查labelVideoStatus!!!!");
        Pageable pageable  = new PageRequest(0, 50, Sort.Direction.ASC, "id");
        List<EssayLabelTotal> labelTotals = essayLabelTotalRepository.findByStatusAndBizStatusAndLabelFlag(pageable, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(),
                EssayLabelStatusConstant.EssayLabelBizStatusEnum.ONLINE.getBizStatus(),
                LabelFlagEnum.STUDENT_LOOK.getCode());
        if (CollectionUtils.isNotEmpty(labelTotals)) {
            for (EssayLabelTotal labelTotal : labelTotals) {
                if (labelTotal.getLabelFlag() == LabelFlagEnum.STUDENT_LOOK.getCode()) {
                    correctOrderService.sendManualCorrectMessage(labelTotal.getAnswerId(), EssayAnswerCardEnum.TypeEnum.QUESTION);
                }
            }
            log.info("完成questionLabel检查：{}", labelTotals.size());
        }
        List<EssayPaperLabelTotal> paperLabelTotals = essayPaperLabelTotalRepository.findByStatusAndBizStatusAndLabelFlag(
                pageable,
                EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(),
                EssayLabelStatusConstant.EssayLabelBizStatusEnum.ONLINE.getBizStatus(),
                LabelFlagEnum.STUDENT_LOOK.getCode());
        if (CollectionUtils.isNotEmpty(paperLabelTotals)) {
            for (EssayPaperLabelTotal paperLabelTotal : paperLabelTotals) {
                if (paperLabelTotal.getLabelFlag() == LabelFlagEnum.STUDENT_LOOK.getCode()) {
                    correctOrderService.sendManualCorrectMessage(paperLabelTotal.getAnswerId(), EssayAnswerCardEnum.TypeEnum.PAPER);
                }
            }
            log.info("完成paperLabel检查:{}", paperLabelTotals.size());
        }
        log.info("完成检查labelVideoStatus!!!!");
    }

    @Override
    protected long getExpireTime() {
        return expireTime;
    }

    @Override
    public String getCacheKey() {
        return cacheKey;
    }
}
