package com.huatu.ztk.paper.service.v4;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.huatu.ztk.paper.enums.CustomizeEnum;

/**
 * 专项训练抽题逻辑整理
 */
public interface CustomizeService {


    PracticePaper createPracticePaper(Integer pointId, int size, long userId, int subject, CustomizeEnum.ModeEnum modeEnum) throws BizException;


}
