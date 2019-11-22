package com.huatu.tiku.banckend.service;

import com.huatu.tiku.dto.request.BatchDealAdoption;
import com.huatu.tiku.entity.AdviceBean;
import com.huatu.tiku.entity.advice.QuestionAdvice;
import com.huatu.tiku.service.BaseService;

/**
 * @author zhengyi
 * @date 2018/9/13 1:25 PM
 **/
public interface QuestionAdviceService extends BaseService<QuestionAdvice> {
    Object list(AdviceBean advice,int page,int size);

    Object batchUpdateUserAdvice(BatchDealAdoption batchDealAdoption);

}
