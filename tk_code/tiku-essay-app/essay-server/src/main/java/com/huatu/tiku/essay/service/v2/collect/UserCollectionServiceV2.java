package com.huatu.tiku.essay.service.v2.collect;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;

/**
 * Created by x6 on 2018/1/30.
 */
public interface UserCollectionServiceV2 {

    Object list(UserSession userSession, int type, int page, int pageSize, EssayAnswerCardEnum.ModeTypeEnum normal);

}
