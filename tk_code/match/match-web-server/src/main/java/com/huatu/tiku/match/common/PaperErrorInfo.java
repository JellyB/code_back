package com.huatu.tiku.match.common;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.match.enums.util.EnumCommon;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lijun on 2018/11/1
 */
public final class PaperErrorInfo {
    private static final Integer default_code = 5000000;
    private static final Integer user_not_enroll = 5000001;
    private static final Integer user_not_exit_in_paper = 5000002;
    private static final Integer paper_info_not_exit = 5000003;
    private static final Integer question_info_nor_exit = 5000004;
    private static final Integer create_error = 5000005;
    private static final Integer save_answer_card_error = 5000006;
    private static final Integer submit_answer_card_error = 5000007;
    private static final Integer answer_card_has_finished = 5000008;
    private static final Integer answer_card_not_finished = 5000009;

    @AllArgsConstructor
    @Getter
    public enum AnswerCard implements EnumCommon {
        USER_NOT_ENROLL(user_not_enroll, "用户暂未报名"),
        USER_NOT_EXIT_IN_PAPER(user_not_exit_in_paper, "当前用户信息与试卷用户信息不匹配"),
        PAPER_INFO_NOT_EXIT(paper_info_not_exit, "试卷信息不存在"),
        QUESTION_INFO_NOR_EXIT(question_info_nor_exit, "试题信息不存在"),
        CREATE_ERROR(create_error, "答题卡创建失败,试卷信息不符合"),
        SAVE_ANSWER_CARD_ERROR(save_answer_card_error, "试卷保存条件校验失败"),
        SUBMIT_ANSWER_CARD_ERROR(submit_answer_card_error, "试卷提交条件校验失败"),
        ANSWER_CARD_HAS_FINISHED(answer_card_has_finished, "该答题卡已经交卷"),
        ANSWER_CARD_NOT_FINISHED(answer_card_not_finished, "答题卡还未完成"),
        DEFAULT_CODE(default_code, "未知模考错误"),;
        private Integer code;
        private String message;

        public static AnswerCard create(int key) {
            AnswerCard[] values = AnswerCard.values();
            for (AnswerCard value : values) {
                if (value.getCode().intValue() == key) {
                    return value;
                }
            }
            return DEFAULT_CODE;
        }

        @Override
        public int getKey() {
            return code;
        }

        @Override
        public String getValue() {
            return message;
        }

        public void exception() {
            throw new BizException(ErrorResult.create(code, message));
        }
    }
}
