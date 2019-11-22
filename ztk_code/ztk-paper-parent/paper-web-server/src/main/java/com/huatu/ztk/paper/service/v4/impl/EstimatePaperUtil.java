package com.huatu.ztk.paper.service.v4.impl;

import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.PaperUserMeta;
import com.huatu.ztk.paper.bo.SmallEstimateHeaderBo;
import com.huatu.ztk.paper.common.EstimateStatus;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 处理试卷对象数据
 * Created by huangqingpeng on 2019/2/20.
 */
final class EstimatePaperUtil {


    /**
     * 试卷信息转换成小模考首页返回值
     *
     * @param estimatePaper
     * @return
     */
    public static SmallEstimateHeaderBo transSmallEstimateHeaderBo(EstimatePaper estimatePaper) {
        SmallEstimateHeaderBo smallEstimateHeaderBo = new SmallEstimateHeaderBo();
        smallEstimateHeaderBo.setPaperId(estimatePaper.getId());
        smallEstimateHeaderBo.setName(estimatePaper.getName());
        smallEstimateHeaderBo.setStartTime(estimatePaper.getStartTime());
        smallEstimateHeaderBo.setEndTime(estimatePaper.getEndTime());
        smallEstimateHeaderBo.setPointsName(estimatePaper.getPointsName());
        smallEstimateHeaderBo.setQcount(estimatePaper.getQcount());
        smallEstimateHeaderBo.setLimitTime(estimatePaper.getTime());
        smallEstimateHeaderBo.setDescription(estimatePaper.getDescrp());
        smallEstimateHeaderBo.setCourseId(estimatePaper.getCourseId());
        smallEstimateHeaderBo.setCourseName(estimatePaper.getCourseName());
        smallEstimateHeaderBo.setCourseInfo(estimatePaper.getCourseInfo());
        smallEstimateHeaderBo.setStatus(estimatePaper.getStatus());
        long practiceId = getUserPracticeId(estimatePaper.getUserMeta(), estimatePaper.getStatus());
        smallEstimateHeaderBo.setPracticeId(practiceId);
        smallEstimateHeaderBo.setIdStr(practiceId + "");
        smallEstimateHeaderBo.setJoinCount(estimatePaper.getPaperMeta().getCardCounts());
        return smallEstimateHeaderBo;
    }

    /**
     * 获取用户ID
     *
     * @param userMeta
     * @param status
     * @return
     */
    public static long getUserPracticeId(PaperUserMeta userMeta, int status) {
        switch (status) {
            case EstimateStatus.ONLINE:             //进行中-开始考试
                return -1;
            case EstimateStatus.CONTINUE_AVAILABLE:     //继续考试
                return userMeta.getCurrentPracticeId();
            case EstimateStatus.REPORT_AVAILABLE: {     //查看报告
                long currentPracticeId = userMeta.getCurrentPracticeId();
                if (currentPracticeId > 0) {
                    return currentPracticeId;
                }
                List<Long> practiceIds = userMeta.getPracticeIds();
                if (CollectionUtils.isNotEmpty(practiceIds)) {
                    return practiceIds.get(0);
                }
            }
        }
        return -1;

    }

}
