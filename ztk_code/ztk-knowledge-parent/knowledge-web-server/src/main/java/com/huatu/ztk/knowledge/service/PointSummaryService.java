package com.huatu.ztk.knowledge.service;

import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.PointSummary;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.dao.PointSummaryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by shaojieyue
 * Created time 2016-06-16 09:40
 */

@Service
public class PointSummaryService {
    private static final Logger logger = LoggerFactory.getLogger(PointSummaryService.class);

    @Autowired
    private PointSummaryDao pointSummaryDao;



    /**
     * 更新知识点汇总
     * @param pointSummary
     */
    public void update(PointSummary pointSummary) {
        //计算做题速度
        pointSummary.setSpeed(pointSummary.getTimes()/pointSummary.getAcount());
        //正确率,保留1位小数
        final double accuracy =pointSummary.getRcount() * 100/pointSummary.getAcount();
        //百分比
        pointSummary.setAccuracy(accuracy);
        pointSummaryDao.save(pointSummary);
    }
}
