package com.huatu.tiku.essay.constant.error;


import com.huatu.common.ErrorResult;

/**
 * 模考大赛错误
 */
public class EssayMockErrors {

    public static final ErrorResult MOCK_PAPER_UNCHECK = ErrorResult.create(10031006, "模考试卷尚未通过审核");

    /**
     * 查找不到当前的考试科目下面的模考大赛
     */
    public static final ErrorResult NO_MATCH = ErrorResult.create(10031007, "尚无模考大赛");

    /**
     * 考试开始30分钟后，无法报名，无法创建答题卡
     */
    public static final ErrorResult MISSING_MATCH = ErrorResult.create(10031008, "已错过模考大赛");

    /**
     * 未报名的模考大赛不能参加
     */
    public static final ErrorResult NOT_ENROLL = ErrorResult.create(10031009, "模考大赛未报名");

    /**
     * 模考大赛未开始
     */
    public static final ErrorResult NOT_START = ErrorResult.create(10031010, "模考大赛考试未开始");

    /**
     * 模考大赛已经报名，不可重复报名
     */
    public static final ErrorResult EXIST_PAPER = ErrorResult.create(10031011, "模考大赛已报名");


   /*
   * 申论模考尚未开始
   */
    public static final ErrorResult  NOT_READY_YET = ErrorResult.create(1000501, "未到开考时间，暂不可查看试题信息");


    /*
    *  试卷未上线
    */
    public static final ErrorResult  MOCK_PAPER_OFFLINE = ErrorResult.create(1000502, "申论模考试卷尚未上线");


    /*
       *  试卷未上线
       */
    public static final ErrorResult  MOCK_OFFLINE = ErrorResult.create(1000503, "申论模考尚未上线");


    /*
      *  试卷未上线
      */
    public static final ErrorResult  NO_REPORT = ErrorResult.create(1000504, "申论报告暂未生成");




    /*
    *  试卷关联模考已上线，不可操作
    */
    public static final ErrorResult  MOCK_CONNECTED = ErrorResult.create(1000505, "该申论模考已上线或结束，不可操作");
    /**
     * 模考id不存在
     */
    public static final ErrorResult  MOCK_ID_NOT_EXIST = ErrorResult.create(1000506, "模考ID查询失败");



    /**
     * 模考已绑定
     */
    public static final ErrorResult  MOCK_ALREADY_CONNECTED = ErrorResult.create(1000507, "该申论模考已被其他模考关联，不可重复绑定");


    /*
    *  模考试卷缺失
    */
    public static final ErrorResult  MOCK_PAPER_LOST = ErrorResult.create(1000508, "模考试卷缺失");



    /*
   *  试卷未上线
   */
    public static final ErrorResult  NOT_MOCK_CORRECT_TIME = ErrorResult.create(1000509, "申论模考未到作答时间");


    /**
     * id为空异常
     */
    public static final ErrorResult  AREA_ID_NULL = ErrorResult.create(1000510, "地区id为空");

    /**
     * id为空异常
     */
    public static final ErrorResult  MOCK_ID_NULL = ErrorResult.create(1000511, "模考id为空");

    /**
     * 异常
     */
    public static final ErrorResult  EXCEL_ID_NULL = ErrorResult.create(1000512, "参数格式异常");


    /**
     * 异常
     */
    public static final ErrorResult  UNCONNECTED_FIRST = ErrorResult.create(1000513, "该申论模考已被其他模考关联，请先解除绑定再操作");


    /**
     * 异常
     */
    public static final ErrorResult  MQ_MSG_ERROR = ErrorResult.create(1000514, "批改完成，消息队列消息异常");


    /**
     * 模考试卷材料为空
     */
    public static final ErrorResult  MOCK_MATERIAL_NOT_EXIST = ErrorResult.create(1000515, "模考试卷材料列表为空");


    /**
     * 模考试卷材料为空
     */
    public static final ErrorResult  MOCK_ANSWERCARD_NOT_EXIST = ErrorResult.create(1000516, "用户模考答题卡数据有误");

    /**
     * 未到交卷时间暂不可交卷
     */
    public static final ErrorResult  NOT_COMMIT_YET = ErrorResult.create(1000517, "未到交卷时间暂不可交卷");



}
