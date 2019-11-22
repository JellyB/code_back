package com.huatu.ztk.question.common;

/**
 * 试题 redis key 列表
 * Created by shaojieyue
 * Created time 2016-05-19 09:36
 */
public class QuestionReidsKeys {

    /**
     * 查询question meta2 redis key
     * @param qid
     * @return
     */
    public static final String getQuestionMetaKey(int qid){
        return new StringBuilder("qmeta_").append(qid).toString();
    }
}
