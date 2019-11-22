package com.huatu.tiku.essay.service;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssayUpdateVO;

/**
 * Created by x6 on 2018/1/30.
 */
public interface UserCollectionService {
    Object saveCollection(int userId, int type, long baseId, long similarId);

    Object delCollection(int userId, long baseId, int type, long similarId);

    Object listV1(UserSession userSession, int type, int page, int pageSize, EssayAnswerCardEnum.ModeTypeEnum normal);

    Object listV2(UserSession userSession, int type, int page, int pageSize, EssayAnswerCardEnum.ModeTypeEnum normal);

    EssayUpdateVO check(UserSession userSession, long baseId, int type, long similarId);

    EssayUpdateVO status(UserSession userSession, long baseId, int type, long similarId);

}
