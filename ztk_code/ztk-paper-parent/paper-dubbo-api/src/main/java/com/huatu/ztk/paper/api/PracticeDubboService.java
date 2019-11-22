package com.huatu.ztk.paper.api;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.PracticePaper;

/**
 * 练习dubboservice
 * Created by shaojieyue
 * Created time 2016-07-05 16:37
 */
public interface PracticeDubboService {

    /**
     * 根据知识点组装练习试卷
     *
     * @param point
     * @param qcount
     * @return
     */
    public PracticePaper create(long uid, int subject, int point, int qcount);

    /**
     * 随机组卷,该组卷不依赖用户做题信息
     *
     * @param subject 科目
     * @param point   知识点
     * @param qcount  试题数目
     * @return
     */
    public PracticePaper create(int subject, int point, int qcount);

    /**
     * 根据知识点组装微信练习试卷
     *
     * @param point
     * @param qcount
     * @return
     */
    public PracticePaper createWeixinPaper(long uid, int subject, int point, int qcount) throws BizException;
}
