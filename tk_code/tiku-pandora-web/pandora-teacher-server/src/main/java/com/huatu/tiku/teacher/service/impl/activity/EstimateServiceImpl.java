package com.huatu.tiku.teacher.service.impl.activity;

import com.huatu.tiku.entity.activity.Estimate;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.activity.EstimateMapper;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.activity.EstimateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/10/19
 * @描述
 */
@Slf4j
@Service
public class EstimateServiceImpl extends BaseServiceImpl<Estimate> implements EstimateService {

    public EstimateServiceImpl() {
        super(Estimate.class);
    }

    @Autowired
    PaperActivityService paperActivityService;


    @Override
    public Estimate getEstimateInfo(Long activityId) {
        PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(activityId);
        if (null == paperActivity) {
            throwBizException("此活动不存在");
        }
        //根据活动ID查询礼包配置信息
        Example example = new Example(Estimate.class);
        example.and().andEqualTo("activityId", activityId);
        List<Estimate> estimateList = selectByExample(example);
        if (CollectionUtils.isNotEmpty(estimateList)) {
            return estimateList.get(0);
        }
        return null;
    }

}

