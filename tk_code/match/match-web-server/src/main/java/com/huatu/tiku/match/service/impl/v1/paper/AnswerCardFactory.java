package com.huatu.tiku.match.service.impl.v1.paper;

import com.huatu.common.utils.code.IdCenter;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.match.common.PaperErrorInfo;
import com.huatu.tiku.match.enums.AnswerCardInfoEnum;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import org.apache.commons.collections.CollectionUtils;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lijun on 2018/10/31
 */
public class AnswerCardFactory {

    /**
     * 创建整套 卷子的答题卡
     * 未设置 catgory/subject/userId/meta/type
     *
     * @param paperInfo 试卷信息
     * @return 整套卷子答题卡
     */
    public static StandardCard createStandardCard(final Paper paperInfo) {
        if (null == paperInfo) {
            PaperErrorInfo.AnswerCard.PAPER_INFO_NOT_EXIT.exception();
        }
        if (CollectionUtils.isEmpty(paperInfo.getQuestions())) {
            PaperErrorInfo.AnswerCard.QUESTION_INFO_NOR_EXIT.exception();
        }
        //试题数量
        final int questionCount = paperInfo.getQuestions().size();
        StandardCard standardCard = StandardCard.builder()
                .paper(paperInfo)
                .build();
        //ID 生成
        final long id = Long.valueOf(String.valueOf(System.nanoTime()) + String.valueOf(System.currentTimeMillis()).substring(11));
        standardCard.setId(id);
        //主要解决web 对于 long 数据精度丢失的问题
        standardCard.setIdStr(String.valueOf(standardCard.getId()));
        //难度
        standardCard.setDifficulty(paperInfo.getDifficulty());
        //提交时间
        standardCard.setCreateTime(System.currentTimeMillis());
        //创建时间
        standardCard.setCardCreateTime(System.currentTimeMillis());
        //初始化耗时
        standardCard.setExpendTime(0);
        //名称
        standardCard.setName(paperInfo.getName());
        //正确数量
        standardCard.setRcount(0);
        //错误数量
        standardCard.setWcount(0);
        //未做数量
        standardCard.setUcount(questionCount);
        //设置剩余时间
        standardCard.setRemainingTime(paperInfo.getTime());
        //当前做到哪一题
        standardCard.setLastIndex(0);
        //状态
        standardCard.setStatus(AnswerCardInfoEnum.Status.CREATE.getCode());
        // --- 用户答题 相关信息 ---
        //答案
        standardCard.setAnswers(IntStream.range(0, questionCount).boxed().map(i -> "0").toArray(String[]::new));
        //答题结果 0 未答，1 正确，2 错误
        standardCard.setCorrects(new int[questionCount]);
        //答题时间
        standardCard.setTimes(new int[questionCount]);
        //是否有疑问
        standardCard.setDoubts(new int[questionCount]);
        return standardCard;
    }

    /**
     * 创建 包含用户信息的答题卡
     *
     * @param paperInfo      试卷信息
     * @param userSession    用户信息
     * @param terminal       设备类型
     * @param answerTypeEnum 答题卡类型
     * @return 新建的答题卡
     */
    public static StandardCard createStandardCard(final Paper paperInfo, final UserSession userSession, int terminal, AnswerCardInfoEnum.TypeEnum answerTypeEnum) {
        //创建答题卡
        StandardCard standardCard = createStandardCard(paperInfo);
        standardCard.setType(answerTypeEnum.getCode());
        if (null == userSession) {
            return standardCard;
        }
        /**
         *使用试卷的科目ID比较靠谱
         */
        standardCard.setSubject(paperInfo.getCatgory());
        standardCard.setCatgory(userSession.getCategory());
        standardCard.setUserId(userSession.getId());
        standardCard.setTerminal(terminal);
        return standardCard;
    }
}
