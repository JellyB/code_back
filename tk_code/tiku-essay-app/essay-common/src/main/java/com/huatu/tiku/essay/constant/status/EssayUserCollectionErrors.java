package com.huatu.tiku.essay.constant.status;


import com.huatu.common.ErrorResult;

/**
 * 试题收藏相关接口异常
 * Created by x6 on 2017/10/02 下午3:39
 */
public class EssayUserCollectionErrors {

    //试题已删除或已下线，请刷新试题列表后再进行操作
    public static final ErrorResult QUESTION_NOT_EXIST = ErrorResult.create(2018001, "试题已删除或已下线，请刷新试题列表后再进行操作");

    //试卷已删除或已下线，请刷新试卷列表后再进行操作
    public static final ErrorResult PAPER_NOT_EXIST = ErrorResult.create(2018002, "试卷已删除或已下线，请刷新试卷列表后再进行操作");


    public static final ErrorResult SIMILAR_QUESTION_REPETITION_ERROR = ErrorResult.create(2018004, "试题重复，请联系管理员");

    public static final ErrorResult COLLECT_FAIL = ErrorResult.create(2018006, "收藏失败");


    public static final ErrorResult COLLECT_UPDATE_FAIL = ErrorResult.create(2018008, "题目收藏，修改状态失败");

    public static final ErrorResult ERROR_SAVE_TYPE = ErrorResult.create(2018009, "操作类型异常");

    public static final ErrorResult GUFEN_CANOT_COLLECT = ErrorResult.create(2018010, "估分试卷暂不支持收藏!");

}
