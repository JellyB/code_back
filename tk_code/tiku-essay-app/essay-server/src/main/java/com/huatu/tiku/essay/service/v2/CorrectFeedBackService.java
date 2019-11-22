package com.huatu.tiku.essay.service.v2;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.entity.correct.CorrectFeedBack;
import com.huatu.tiku.essay.vo.admin.correct.CorrectFeedBackVo;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/9
 * @描述 学员人工批改意见反馈
 */
public interface CorrectFeedBackService {

	Object save(CorrectFeedBack correctFeedBack, UserSession userSession);

    List<CorrectFeedBackVo> findByAnswerId(long answerId,int answerType);

}
