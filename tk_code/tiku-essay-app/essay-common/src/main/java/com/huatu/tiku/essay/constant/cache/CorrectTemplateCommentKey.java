package com.huatu.tiku.essay.constant.cache;

import com.google.common.base.Joiner;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/13
 * @描述
 */
public class CorrectTemplateCommentKey {


    /**
     * 模版评语缓存key
     *
     * @param questionType
     * @param labelType
     * @return
     */
    public static String getCommentKey(int questionType, int labelType) {
        return Joiner.on("_").join("template_comment_", questionType, labelType);

    }
}
