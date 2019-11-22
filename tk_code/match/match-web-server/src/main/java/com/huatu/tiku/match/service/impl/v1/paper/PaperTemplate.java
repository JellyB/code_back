package com.huatu.tiku.match.service.impl.v1.paper;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.ztk.paper.bean.AnswerCard;

/**
 * Created by lijun on 2018/10/31
 */
public abstract class PaperTemplate {

    public abstract AnswerCard createAnswerCard(UserSession userSession, Integer paperId, int terminal);
}
