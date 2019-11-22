package com.huatu.tiku.essay.service.correct;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/9
 * @描述
 */
public interface IntelligenceConvertManualRecordService {

    List<Long> getConvertOrderIds(Long answerId, int answerType);


}
