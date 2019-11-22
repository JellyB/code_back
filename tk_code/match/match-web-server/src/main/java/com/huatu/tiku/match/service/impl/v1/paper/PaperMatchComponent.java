package com.huatu.tiku.match.service.impl.v1.paper;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.common.PaperErrorInfo;
import com.huatu.tiku.match.enums.AnswerCardInfoEnum;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.meta.MetaHandlerService;
import com.huatu.tiku.match.service.v1.paper.AnswerCardDBService;
import com.huatu.tiku.match.service.v1.paper.PaperService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Created by lijun on 2018/10/31
 */
@RequiredArgsConstructor
@Component
public class PaperMatchComponent extends PaperTemplate {

    final AnswerCardDBService answerCardDBService;

    final PaperService paperService;

    final MatchUserMetaService matchUserMetaService;
    final MetaHandlerService metaHandlerService;

    @Override
    public AnswerCard createAnswerCard(UserSession userSession, Integer paperId, int terminal) {
        //TODO: 正式业务 需要打开校验
        //1、获取报名信息、报名成功一般试卷信息都会存在
        MatchUserMeta matchUserEnrollInfo = matchUserMetaService.findMatchUserEnrollInfo(userSession.getId(), paperId);
        if (null == matchUserEnrollInfo) {
            PaperErrorInfo.AnswerCard.USER_NOT_ENROLL.exception();
        }
        //2、获取试卷信息
        Paper paperInfo = paperService.findPaperCacheById(paperId);
        if (!AnswerCardUtil.isEnableCreateCard(paperInfo)) {
            PaperErrorInfo.AnswerCard.CREATE_ERROR.exception();
        }
        StandardCard standardCard = AnswerCardFactory.createStandardCard(paperInfo, userSession, terminal, AnswerCardInfoEnum.TypeEnum.MATCH);
        //入库
        answerCardDBService.saveToDB(standardCard);
        metaHandlerService.savePracticeId(standardCard.getId());
        return standardCard;
    }

}
