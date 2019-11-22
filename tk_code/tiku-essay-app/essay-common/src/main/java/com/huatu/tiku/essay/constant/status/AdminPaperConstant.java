package com.huatu.tiku.essay.constant.status;

/**
 * Created by x6 on 2017/12/10.
 *
 * 后台试卷相关常量
 */
public class AdminPaperConstant {

    //试卷(考试)类型    1真题 0模考题
    public final static  int TRUE_PAPER = 1;//真題
    public final static  int MOCK_PAPER = 0;//模考題

    //试卷操作类型 : 1提交审核  2上线  3下线   4审核通过 5审核未通过  6删除
    public static final int  UP_TO_CHECK = 1;//提交审核
    public static final int  UP_TO_ONLINE = 2;//上线
    public static final int  UP_TO_OFFLINE = 3;//下线
    public static final int  UP_TO_CHECK_PASS = 4;//审核通过
    public static final int  UP_TO_CHECK_FAILURE = 5;//审核不通过
    public static final int  UP_TO_DELETE = 6;//删除

}
