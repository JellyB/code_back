package com.huatu.ztk.knowledge.service;

import com.huatu.ztk.knowledge.api.PointSummaryDubboService;
import com.huatu.ztk.knowledge.bean.PointSummary;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识点汇总dubbo服务
 * Created by shaojieyue
 * Created time 2016-06-16 14:20
 */
@Service
public class PointSummaryDubboServiceImpl implements PointSummaryDubboService {
    private static final Logger logger = LoggerFactory.getLogger(PointSummaryDubboServiceImpl.class);

    @Autowired
    private PoxyUtilService poxyUtilService;

    /**
     * 查询用户指定科目下的知识点汇总
     *
     * @param uid     用户id
     * @param subject 考试科目
     * @return
     */
    public List<PointSummary> findUserPointSummary(long uid, int subject) {
        throw new RuntimeException("不支持的方法");
    }

    /**
     * 查找用户知识点汇总
     * 如果该知识点还不存在汇总,则创建一个
     * @param uid
     * @param subject
     * @param point
     * @return
     */
    public PointSummary find(long uid, int subject, int point) {
        final QuestionPoint questionPoint = poxyUtilService.getQuestionPointService().findById(point);

        int wrongCount = poxyUtilService.getQuestionErrorService().count(uid,questionPoint);
        int finishCount = poxyUtilService.getQuestionFinishService().count(uid,questionPoint);

        PointSummary pointSummary = PointSummary.builder()
                .name(questionPoint.getName())
                .pointId(point)
                .uid(uid)
                .subject(subject)
                .build();
        pointSummary.setWcount(wrongCount);
        pointSummary.setRcount(Math.max(finishCount-wrongCount,0));//防止负数出现
        //这一句一定这么写,保证 wcount+rcount=acount
        pointSummary.setAcount(pointSummary.getRcount()+pointSummary.getWcount());
        if (pointSummary.getAcount()>0) {
            pointSummary.setAccuracy(pointSummary.getRcount()*100/pointSummary.getAcount());
        }
        return pointSummary;
    }

}
