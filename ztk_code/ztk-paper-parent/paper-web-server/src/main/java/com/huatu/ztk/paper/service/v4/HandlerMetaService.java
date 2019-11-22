package com.huatu.ztk.paper.service.v4;

import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.CardUserMeta;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.dto.ScoreRankDto;

import java.util.List;

/**
 * Created by huangqingpeng on 2019/2/20.
 */
public interface HandlerMetaService {

    /**
     * 查询试卷创建的答题卡数量
     * @param paperId
     * @return
     */
    int getJoinCount(int paperId);

    /**
     * 创建答题卡数量，自增1
     * @param paperId
     */
    void incrementJoinCount(int paperId);

    /**
     * 补充小模考报告数据
     * @param answerCard
     */
    void fillSmallEstimateReportInfo(AnswerCard answerCard);

    /**
     * 计算排名、平均分
     * @param standardCard
     * @return
     */
    CardUserMeta getCardUserMeta(final StandardCard standardCard);

    /**
     * 交卷统计数据写入
     * @param answerCard
     */
    void handlerSubmitInfo(AnswerCard answerCard);

    /**
     * 报告写入缓存
     * @param answerCard
     */
    void putCache(StandardCard answerCard);

    /**
     * 查询报告缓存
     * @param practiceId
     * @return
     */
    StandardCard getReportCache(Long practiceId);

    /**
     * 查询阶段测试缓存报告
     *
     * @return
     */
    public List<ScoreRankDto> getPeriodReportCache(StandardCard standardCard);

    /**
     * 缓存阶段测试报告一分钟
     *
     * @param scoreRankDtoList
     */
    public void putPeriodReportCache(List<ScoreRankDto> scoreRankDtoList,StandardCard standardCard);

    /**
     * 获取答题人数（有超过20的查询20以上，没有超过查询所有）
     * @param standardCard
     * @return
     */
    public long getCardUserMetaNum(final StandardCard standardCard);
}
