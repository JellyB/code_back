package com.huatu.tiku.essay.service;

/**
 * 微信相关
 *
 *
 * @author zhangchong
 */
public interface EssayWeiXinService {

    Object compareRedisAndMysqlMockInfo(Long paperId);

    void bindPaper2Mock(Long paperId, Long mockId);
}
