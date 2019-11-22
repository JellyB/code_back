package com.huatu.tiku.teacher.service.activity;


import com.huatu.tiku.entity.activity.Estimate;
import com.huatu.tiku.service.BaseService;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/10/19
 * @描述
 */
public interface EstimateService extends BaseService<Estimate> {


     Estimate getEstimateInfo(Long activityId) ;


}
