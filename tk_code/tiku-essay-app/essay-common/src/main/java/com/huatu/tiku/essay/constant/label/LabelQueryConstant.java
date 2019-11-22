package com.huatu.tiku.essay.constant.label;

/**
 * Created by x6 on 2018/7/5.
 */
public class LabelQueryConstant {

    //分数
    public static final int  SCORE_LESS_THAN_10 = 1;//10分以下
    public static final int  SCORE_BETWEEN_10_AND_20 = 2;//（10,20]
    public static final int  SCORE_BETWEEN_20_AND_30 = 3;//（20,30]
    public static final int  SCORE_GREATER_THAN_30= 4;//大于30


    //字数
    public static final int  WORD_LESS_THAN_200 = 1;//200字以下
    public static final int  WORD_BETWEEN_200_AND_500 = 2;//（200,500]
    public static final int  WORD_BETWEEN_500_AND_1000 = 3;//（500,1000]
    public static final int  WORD_GREATER_THAN_1000= 4;//大于1000


    //分差
    public static final int  SUB_LESS_EQUAL_THAN_10 = 1;//不超过10%
    public static final int  SUB_GREATER_THAN_10= 2;//大于10%

    //状态
    public static final int  INIT_STATUS = 0;//未批注
    public static final int  LABEL_ONE_TIMES= 1;//已批注一次
    public static final int  LABEL_TWO_TIMES= 2;//已批注两次
    public static final int  FINAL_LABEL= 3;//终审完成


    //是否是终审
    public static final int  NOT_FINAL= 0;//不是终审
    public static final int  IS_FINAL= 1;//是终审





}
