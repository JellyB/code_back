package com.huatu.tiku.essay.constant.cache;

import com.google.common.base.Joiner;

/**
 * @author zhaoxi
 * @Description: 试卷相关缓存
 * @date 2018/12/101:38 PM
 */
public class PaperRedisKey {

    /**
     * 根据试卷id查询试题信息列表
     * 失效时间 5分钟
     * @param paperId
     * @return
     */
    public static String getQuestionInfoListKey(Long paperId) {
        return  Joiner.on("_").join("essay_question_info_list",paperId);
    }
}
