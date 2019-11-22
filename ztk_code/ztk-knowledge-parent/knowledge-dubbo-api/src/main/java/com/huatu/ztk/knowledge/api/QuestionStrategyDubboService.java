package com.huatu.ztk.knowledge.api;


import com.huatu.ztk.knowledge.bean.QuestionStrategy;

/**
 * 抽题策略dubbo服务
 * Created by shaojieyue
 * Created time 2016-05-18 18:22
 */
public interface QuestionStrategyDubboService {

//    /**
//     * 随机知识点获取试题
//     *
//     * @param uid
//     * @param subject
//     *@param size  @return
//     */
//    public QuestionStrategy randomStrategy(long uid, int subject, int size);

    /**
     * 智能获取试题
     *
     * @param uid
     * @param subject
     * @param size    @return
     */
    public QuestionStrategy smartStrategy(long uid, int subject, int size);

    /**
     * 随机组卷
     *
     * @param uid
     * @param subject
     * @param pointId 知识点id
     * @param size    返回试题大小   @return
     */
    public QuestionStrategy randomStrategy(long uid, int subject, int pointId, int size);

    /**
     * 完全随机组卷
     * 该组卷不根据用户做题信息进行组卷
     *
     * @param subject 科目
     * @param pointId 知识点id
     * @param size    试题个数
     * @return
     */
    public QuestionStrategy randomStrategyNoUser(int subject, int pointId, int size);

    /**
     * 错题随机组卷接口
     *
     * @param pointId 知识点id
     * @param size    返回试题大小
     * @return
     */
    public QuestionStrategy randomErrorStrategy(long uid, int pointId, int subject, int size);

    /**
     * 错题背题模式加做题模式组卷接口兼容
     *
     * @param uid
     * @param pointId
     * @param subject
     * @param size
     * @param flag      1是做题模式2是背题模式
     * @return
     */
    public QuestionStrategy randomErrorStrategyWithFlag(long uid, int pointId, int subject, int size, int flag);

    /**
     * 收藏题目随机抽题
     *
     * @param uid
     * @param pointId
     * @param subject
     * @param size
     * @return
     */
    public QuestionStrategy randomCollectStrategy(long uid, int pointId, int subject, int size);

    /**
     * 专项训练专用抽题策略
     * @param userId
     * @param subject
     * @param pointId
     * @param size
     * @return
     */
    public QuestionStrategy randomCustomizeStrategy(long userId, int subject, Integer pointId, int size);
}
