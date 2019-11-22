package com.huatu.tiku.essay.constant.error;

import com.huatu.common.ErrorResult;

/**
 * 议论文 批注相关异常
 * code格式：10007XX
 */
public class EssayLabelErrors {

    public static final ErrorResult ALREADY_MAX_LABEL_TIMES = ErrorResult.create(1000700, "该题批注次数已达最大，不可批注，请刷新页面后重试");

    public static final ErrorResult LABEL_INSERT_ERROR = ErrorResult.create(1000701, "插入批注数据异常");

    public static final ErrorResult LABEL_TOTAL_ID_ERROR = ErrorResult.create(1000702, "批注ID异常");

    public static final ErrorResult LABEL_UPDATE_ERROR = ErrorResult.create(1000703, "更新批注数据异常");

    public static final ErrorResult NOT_READY_FOR_FINAL_LABEL = ErrorResult.create(1000704, "当前题目状态，不可进行终审批注，请刷新页面后重试");

    public static final ErrorResult EXIST_UNFINISHEDLABEL = ErrorResult.create(1000705, "您还有未完成批注，请先保存或取消未完成的批注");

    public static final ErrorResult LABEL_FINAL_ID_ERROR = ErrorResult.create(1000706, "终审ID异常");

    public static final ErrorResult NOT_GET_NEXT = ErrorResult.create(1000707, "没有拿到题目，请稍后重试");

    public static final ErrorResult NOT_FINISH_LAST_LABEL = ErrorResult.create(1000708, "是否继续上次未完成的批注");

    public static final ErrorResult LABEL_DETAIL_ID_ERROR = ErrorResult.create(1000709, "详细批注ID异常");

    public static final ErrorResult ALREADY_LABEL_THIS = ErrorResult.create(1000710, "您已经批注过此答题卡，不可重复批注。请重新选择批注对象");

    public static final ErrorResult NO_THESIS_YET = ErrorResult.create(1000711, "暂无论点批注，论据批注不可保存");


    public static final ErrorResult UPDATE_LABELED_CONTENT_ERROR = ErrorResult.create(1000712, "更新批注后的内容失败");

    public static final ErrorResult TITLE_LABEL_EXIST = ErrorResult.create(1000713, "标题批注已存在，不可重复批注");


    public static final ErrorResult STRUCTURE_LABEL_EXIST = ErrorResult.create(1000714, "结构批注已存在，不可重复批注");


    public static final ErrorResult THESIS_EVIDENCE_NOT_EXIST_TOGETHER = ErrorResult.create(1000716, "论点批注，论据批注不可同时出现，请修改后保存");

    public static final ErrorResult ERROR_TOTAL_SCORE = ErrorResult.create(1000715, "综合批注分数不能为空");
    public static final ErrorResult ERROR_WORD_NUM_SCORE = ErrorResult.create(1000717, "综合批注字数得分不能为空");
    public static final ErrorResult ERROR_PARAGRAPH_SCORE = ErrorResult.create(1000718, "综合批注分段得分不能为空");
    public static final ErrorResult ERROR_TITLE_SCORE = ErrorResult.create(1000719, "综合批注标题得分不能为空");
    public static final ErrorResult ERROR_SENTENCE_SCORE = ErrorResult.create(1000720, "综合语言得分不能为空");
    public static final ErrorResult ERROR_EVIDENCE_SCORE = ErrorResult.create(1000721, "综合批注论据得分不能为空");
    public static final ErrorResult ERROR_LITERARY_SCORE = ErrorResult.create(1000722, "综合批注文采得分不能为空");
    public static final ErrorResult ERROR_THOUGHTFUL_SCORE = ErrorResult.create(1000723, "综合批注思想性得分不能为空");
    public static final ErrorResult ERROR_THESIS_SCORE = ErrorResult.create(1000724, "综合批注论点得分不能为空");
    public static final ErrorResult ERROR_STRUCTURE_SCORE = ErrorResult.create(1000725, "综合批注结构得分不能为空");
    public static final ErrorResult ERROR_THAN_LIMIT_TIME = ErrorResult.create(5000001, "页面停留超过两个小时");

    public static final ErrorResult LABEL_DELETED = ErrorResult.create(1000733, "该批注已被删除不可继续操作");

    public static final ErrorResult EVIDENCE_SCORE_ERROR = ErrorResult.create(1000726, "论据得分必须关联相应论点");

    public static final ErrorResult CANOT_GIVE_UP_LABEL = ErrorResult.create(1000727, "当前答题卡已完成一审，暂不可放弃批注^_^");

    public static final ErrorResult THESIS_LABEL_CONNECT_ERROR = ErrorResult.create(1000728, "论点批注必须关联标答论点");

    public static final ErrorResult NOTHING_LABEL_CONTENT = ErrorResult.create(1000729, "请至少输入一项批注内容");

    public static final ErrorResult EMPTY_GIVEUP_TYPE = ErrorResult.create(1000730, "请明确放弃或过滤当前批注的原因");

    public static final ErrorResult VIP_ONLY_LABEL_EMPTY = ErrorResult.create(1000731, "终审2只能批注，未被批注的答题卡");

    public static final ErrorResult VIP_LABELING = ErrorResult.create(1000732, "该题已被VIP批注，不可批注，请刷新页面后重试");

    public static final ErrorResult SINGLE_SCORE_SUM_MORE_THAN_TOTAL_SCORE = ErrorResult.create(1000733, "单个批注分数必须小于试卷总分!");


}
