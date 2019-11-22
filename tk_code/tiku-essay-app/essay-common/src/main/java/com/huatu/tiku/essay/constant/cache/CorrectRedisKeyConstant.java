package com.huatu.tiku.essay.constant.cache;

import com.google.common.base.Joiner;

/**
 *  智能批改中用到的缓存
 * Created by huangqp on 2018\4\21 0021.
 */
public class CorrectRedisKeyConstant {

    /**
     * 试题下关联的材料
     * @param questionBaseId
     * @return
     */
    public static String getMaterialQuestionKey(Long questionBaseId) {
        return  Joiner.on("_").join("essay_material_question",questionBaseId);
    }

    /**
     * 试卷下的材料
     * @param paperId
     * @return
     */
    public static String getMaterialPaperKey(Long paperId) {
        return  Joiner.on("_").join("essay_material_paper",paperId);
    }
}
