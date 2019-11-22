package com.huatu.tiku.essay.constant.error;

import com.huatu.common.ErrorResult;

/**
 * 申论相关业务报错
 * Created by huangqp on 2017\12\3 0003.
 */
public class EssayErrors {

//    @Value("${max_correct_time}")
//    private static Integer maxCorrectTime;

    public static final ErrorResult NO_EXISTED_PAPER = ErrorResult.create(1000501, "试卷不存在");
    public static final ErrorResult NO_EXISTED_PAPER_DETAIL = ErrorResult.create(1000502, "试卷详情不存在");
    public static final ErrorResult NO_EXISTED_MATERIAL_IN_PAPER = ErrorResult.create(1000503, "试卷材料不存在");
    public static final ErrorResult NO_EXISTED_QUESTION_IN_PAPER = ErrorResult.create(1000504, "试卷题目不存在");
    public static final ErrorResult NO_EXISTED_QUESTION_DETAIL = ErrorResult.create(1000505, "题目详情不存在");
    public static final ErrorResult NO_EXISTED_SIMILAR_QUESTION = ErrorResult.create(1000506, "单题组单题不存在");
    public static final ErrorResult NO_EXISTED_ENROLL_POSITION = ErrorResult.create(1000507, "无报名地区");

    public static final ErrorResult NO_EXISTED_QUESTION_BASE = ErrorResult.create(1000508, "试卷不存在");

    public static final ErrorResult LOW_INVENTORY = ErrorResult.create(1000509, "库存不足，请修改购买数量");
    public static final ErrorResult PAYMENT_ERROR = ErrorResult.create(1000512, "支付金额异常");
    public static final ErrorResult ORDER_ERROR = ErrorResult.create(1000513, "订单生成失败");
    //    public static final ErrorResult CANNOT_CORRECT = ErrorResult.create(1000509, "已批改"+maxCorrectTime+"次，无法交卷");
    public static final ErrorResult UNFINISHED_PAPER = ErrorResult.create(1000510, "该题存在未完成的答题卡，答题卡创建失败");
    public static final ErrorResult LOW_CORRECT_TIMES = ErrorResult.create(1000511, "批改次数不足");
    public static final ErrorResult LOW_COIN = ErrorResult.create(1000513, "账户图币余额不足");

    //试卷状态修改异常状态
    public static final ErrorResult PAPER_STATUS_ERROR = ErrorResult.create(1000514, "试卷状态修改异常");
    public static final ErrorResult COMMON_DATA_STYLE_ERROR = ErrorResult.create(1000515, "数据结构错误");

    //试卷下试题状态
    public static final ErrorResult QUESTION_SORT_EXISTED_IN_PAPER = ErrorResult.create(1000516, "该题序已被其他试题占用");
    public static final ErrorResult QUESTION_CREATE_NO_SORT = ErrorResult.create(1000517, "首次保存试题属性时，请添加题序以便试题创建");
    public static final ErrorResult QUESTION_CREATE_NO_SCORE = ErrorResult.create(1000518, "首次保存试题属性时，请添加分数以便试题创建");
    public static final ErrorResult QUESTION_CREATE_NO_TYPE = ErrorResult.create(1000519, "试题无类型");
    public static final ErrorResult SCORE_PAPER_NOT_MATCH_QUESTIONS = ErrorResult.create(1000520, "试卷分数和试题分数的和不匹配");
    //非法

    public static final ErrorResult URL_NOT_USE = ErrorResult.create(1000521, "url图片打开失败");

    public static final ErrorResult ESSAY_PARAM_ILLEGAL = ErrorResult.create(1000522, "请求有误，请重试");
    public static final ErrorResult PDF_OBJ_NULL = ErrorResult.create(1000523, "pdf有空值");
    public static final ErrorResult PAGE_QUERY_ERROR = ErrorResult.create(1000524, "分页查询失败");
    public static final ErrorResult UPDATE_MATERIAL_ERROR = ErrorResult.create(1000525, "材料维护失败");
    public static final ErrorResult MATERIAL_NO_CONTENT_ERROR = ErrorResult.create(1000525, "材料中存在无内容的材料，请检查后再提交");
    public static final ErrorResult UPDATE_PAPER_ERROR_FOR_STATUS = ErrorResult.create(1000526, "试卷状态不允许修改");
    public static final ErrorResult PARAM_ILLEGAL_IN_QUESTION_NUM_LIMIT = ErrorResult.create(1000527, "试题字数限制规则不合法");
    public static final ErrorResult NO_EXISTED_QUESTION_NUM_LIMIT = ErrorResult.create(1000528, "试题字数限制规则必须存在");
    public static final ErrorResult NO_EXISTED_REAL_DATA = ErrorResult.create(1000529, "无真实数据录入");


    public static final ErrorResult ESSAY_QUESTION_BASE_ILLEGAL = ErrorResult.create(1000532, "试题属性不合法");
    public static final ErrorResult ESSAY_QUESTION_BASE_ID_NULL = ErrorResult.create(1000533, "问题id不存在");
    public static final ErrorResult NO_EXSITED_ESSAY_SIMILAR_QUESTION = ErrorResult.create(1000534, "单题组不存在");
    public static final ErrorResult TYPE_NO_MATCH_SIMILAR_QUESTION = ErrorResult.create(1000535, "单题组题型与题目类型不匹配");
    public static final ErrorResult INSERT_SAME_QUESTION_IN_SIMILAR_GROUP = ErrorResult.create(1000536, "单题组中重复添加同一题目");
    public static final ErrorResult NO_EXSITED_SIMILAR_GROUP_TYPE = ErrorResult.create(1000537, "单题组题型参数未指定，不能添加题目");
    public static final ErrorResult NO_DOWN_SERVER = ErrorResult.create(1000538, "单题组没有下线,不能更改题目");
    public static final ErrorResult NO_EXISTED_QUESTION_ANALYZE = ErrorResult.create(1000578, "不存在试题分析内容");
    public static final ErrorResult CORRECT_ERROR = ErrorResult.create(1000539, "批改异常");
    public static final ErrorResult PAPER_ANSWER_CARD_ERROR = ErrorResult.create(1000540, "批改异常");


    public static final ErrorResult NO_CHOOSE_DIFFICULTGRADE = ErrorResult.create(1000541, "请选择试题难度");


    public static final ErrorResult ANSWER_CARD_ID_ERROR = ErrorResult.create(1000542, "答题卡id错误");
    public static final ErrorResult ANSWER_CARD_COMMIT = ErrorResult.create(1000543, "试卷已提交或已批改");
    public static final ErrorResult ANSWER_CARD_SAVE_TYPE_ERROR = ErrorResult.create(1000544, "答题卡保存类型错误");

    public static final ErrorResult GOODS_SAVE_TYPE_ERROR = ErrorResult.create(1000545, "商品保存类型错误");
    public static final ErrorResult SIMILAR_QUESTION_GROUP_ID_ERROR = ErrorResult.create(1000546, "单题组id错误");
    public static final ErrorResult SIMILAR_QUESTION_RELATION_ID_ERROR = ErrorResult.create(1000547, "单题组关联id错误");
    public static final ErrorResult SIMILAR_QUESTION_SAVE_TYPE_ERROR = ErrorResult.create(1000548, "单题组保存类型错误");


    public static final ErrorResult SIMILAR_QUESTION_SAVE_ARGUMENT_ERROR = ErrorResult.create(1000549, "单题组保存参数错误");


    public static final ErrorResult NO_ANSWER_MSG_IN_REDIS = ErrorResult.create(1000550, "用户答题卡信息异常");
    public static final ErrorResult ANSWER_CARD_CORRECTED = ErrorResult.create(1000550, "用户答题卡已批改");

    public static final ErrorResult NO_AREA_LIST = ErrorResult.create(1000551, "地区id列表为空");

    public static final ErrorResult USER_ID_ERROR = ErrorResult.create(1000552, "用户id错误");
    public static final ErrorResult USER_NAME_ERROR = ErrorResult.create(1000557, "用户名称错误");
    public static final ErrorResult FREE_USER_NOT_EXIST = ErrorResult.create(1000553, "用户不在白名单中,删除失败。");
    public static final ErrorResult FREE_USER_EXIST = ErrorResult.create(1000554, "用户已存在在白名单中,请勿重复添加。");

    public static final ErrorResult SAVE_TYPE_ERROR = ErrorResult.create(1000555, "操作类型异常");
    public static final ErrorResult COURSE_LIST_NOT_EXIST = ErrorResult.create(1000556, "课程列表为空");
    public static final ErrorResult ORDER_ID_ERROR = ErrorResult.create(1000558, "订单ID错误");
    public static final ErrorResult SAVE_LIST_NOT_EXIST = ErrorResult.create(1000559, "课程列表为空");

    public static final ErrorResult CENTER_THESIS_NOT_EXIST = ErrorResult.create(1000560, "中心论点不存在，中心论点ID错误");


    public static final ErrorResult SAME_PAPER_EXIST = ErrorResult.create(1000561, "存在同名试卷，请修改试卷名称。");

    public static final ErrorResult REPLY_ERROR = ErrorResult.create(1000562, "意见反馈回复失败");
    public static final ErrorResult GET_REPLY_ERROR = ErrorResult.create(1000563, "查询意见反馈回复失败");

    public static final ErrorResult PAPER_NOT_EXIST = ErrorResult.create(1000564, "试卷id错误，不存在对应试卷");


    public static final ErrorResult PAPER_SHOW_NORMAL = ErrorResult.create(1000565, "该试卷已在app正常展示，无需加入白名单");
    public static final ErrorResult WHITE_PAPER_EXIST = ErrorResult.create(1000566, "该试卷已加至白名单，请勿重复添加");


    public static final ErrorResult REDIS_REFRESH_ERROR = ErrorResult.create(1000567, "缓存刷新失败");

    public static final ErrorResult PAPER_MATERIAL_NOT_EXIST = ErrorResult.create(1000568, "试卷尚未关联材料");
    public static final ErrorResult QUESTION_MATERIAL_NOT_EXIST = ErrorResult.create(1000569, "存在尚未关联材料的试题");


    /*  勿动 ，code请勿重复  */
    public static final ErrorResult ESSAY_PAPER_OFFLINE = ErrorResult.create(1000570, "试卷已下线或已删除");
    public static final ErrorResult ESSAY_QUESTION_OFFLINE = ErrorResult.create(1000571, "试题已下线或已删除");
    public static final ErrorResult ESSAY_QUESTION_GROUP_OFFLINE = ErrorResult.create(1000572, "题组已下线或已删除");
    public static final ErrorResult ESSAY_QUESTION_GROUP_UNCONNECT = ErrorResult.create(1000573, "试题已从题组中已下线或删除");
    /*  勿动 ，code请勿重复  */


    public static final ErrorResult ERROR_FILE_TYPE = ErrorResult.create(1000574, "文件类型错误");
    public static final ErrorResult ERROR_PAPER_TYPE = ErrorResult.create(1000575, "试卷类型错误");

    public static final ErrorResult ERROR_PIC_TYPE = ErrorResult.create(1000576, "文件识别类型错误");

    public static final ErrorResult FILE_READ_ERROR = ErrorResult.create(1000577, "文件读取错误");


    public static final ErrorResult ANSWER_LIST_EMPTY = ErrorResult.create(1000579, "答案数据不能为空");


    public static final ErrorResult ERROR_QUESTION_ID = ErrorResult.create(1000580, "题目id错误");

    public static final ErrorResult QUESTION_DEL_ERRROR_PAPER_ONLINE = ErrorResult.create(1000581, "试题删除失败。试题所属试卷处于上线状态，请先将试卷下线再进行操作");


    public static final ErrorResult QUESTION_DEL_ERRROR_QUESTION_GROUP_ONLINE = ErrorResult.create(1000582, "试题删除失败。试题所属题组处于上线状态，请先将题组下线再进行操作");


    public static final ErrorResult MOCK_NOT_FINISH_CANOT_ANSWER = ErrorResult.create(1000583, "当前模考暂未结束，暂时不可答题");

    public static final ErrorResult ERROR_VIDEO_ID = ErrorResult.create(1000584, "视频ID有误，请检查视频ID后重试");

    public static final ErrorResult ERROR_BJY_SERVER = ErrorResult.create(1000585, "调用百家云服务异常");

    public static final ErrorResult ERROR_BJY_VIDEO_ID = ErrorResult.create(1000586, "百家云视频ID有误或视频已被删除。请检查视频ID后重试");

    public static final ErrorResult ERROR_CORRECT_TYPE = ErrorResult.create(1000587, "批改类型指定错误，请指定正确的批改类型");
    public static final ErrorResult EMPTY_CORRECT_TYPE = ErrorResult.create(1000588, "批改类型暂未指定，请指定正确的批改类型");
    public static final ErrorResult ERROR_DOC_STYLE = ErrorResult.create(1000579, "文档格式错误,请校正后再尝试导入");

    public static final ErrorResult ERROR_CELL_TYPE = ErrorResult.create(1000580, "文档数据格式错误,请校正后再尝试导入");

    public static final ErrorResult GUFEN_CANOT_ANSWER = ErrorResult.create(1000589, "估分试卷暂不支持答题");

    public static final ErrorResult INVALID_TOTAL_ID = ErrorResult.create(1000590, "无效的批注ID");

    public static final ErrorResult HAVE_FEEDBACK_STATUS = ErrorResult.create(1000591, "您已反馈过");
    public static final ErrorResult ONlY_SUPPORT_MANUAL_CORRECT_MODE = ErrorResult.create(1000591, "不支持非人工批改模式");
    public static final ErrorResult ERROR_CORRECT_MODE_NULL = ErrorResult.create(1000592, "批改模式不能为空");

    public static final ErrorResult ERROR_TEACHER_INFO_NOT_EXIST = ErrorResult.create(1000593, "老师信息错误");

    public static final ErrorResult ERROR_CORRECT_ORDER_BIND_ERROR = ErrorResult.create(1000594, "接单失败");

    public static final ErrorResult CORRECT_ORRDER_ERROR = ErrorResult.create(1000595, "批改订单信息错误");

    public static final ErrorResult LOGIN_INVALID_ERROR = ErrorResult.create(1000596, "登录信息无效");
    public static final ErrorResult ERROR_TEACHER_INFO_ORDERLIMIT_ERROR = ErrorResult.create(1000597, "个人接单数量不能大于管理员设置最大数");
    public static final ErrorResult RE_CORRECT_CAN_NOT_SAME_TEACHER = ErrorResult.create(1000598, "再次批改不能是同一老师");
    public static final ErrorResult KFX_CAN_NOT_MORE_THAN_TOTAL_SCORE = ErrorResult.create(1000598, "扣分项总分不能超过试题总分!");
    public static final ErrorResult BTYJ_SCORE_CAN_NOT_MORE_THAN_TOTAL_SCORE = ErrorResult.create(1000598, "本题得分不能大于总分减去扣分项得分!");
    public static final ErrorResult ANSWER_CARD_NOT_CORRECTED = ErrorResult.create(1000599, "答案卡暂未批改完成!");
    public static final ErrorResult CAN_NOT_RETURN_USER = ErrorResult.create(1000600, "接单老师非本人禁止退回!");
    public static final ErrorResult ANSWER_USERINFO_ILLEGAL = ErrorResult.create(1000601, "答题卡用户信息为空");
    public static final ErrorResult PAPER_TOTAL_NOT_EXIST = ErrorResult.create(1000602, "套卷综合评价不存在!");
    public static final ErrorResult ERROR_EXIST_RECORRECT_ORDER = ErrorResult.create(1000603, "已经存在再次批改的订单");
    public static final ErrorResult REFUSE_ORDER_REASON_CANNOT_EMPTY = ErrorResult.create(1000604, "拒绝接单原因不能为空!");
    public static final ErrorResult PAPER_CORRECT_CAN_NOT_DELETE = ErrorResult.create(1000605, "批改中的记录不可删除!");
    public static final ErrorResult MANUAL_CANNOT_RETURN_USER = ErrorResult.create(1000606, "智能转人工的订单,不可退回!");
    public static final ErrorResult ANSWER_CARD_IS_CORRECT_NOW = ErrorResult.create(1000607, "正在人工批改中,请勿重复提交!");
    public static final ErrorResult ANSWER_CARD_CORRECT_MODE_ERROR = ErrorResult.create(1000608, "答题卡类型不对");
    public static final ErrorResult ONLINE_GOODS_BING_COURSE = ErrorResult.create(1000609, "此商品绑定着赠送课程,不可下线或删除!");
    public static final ErrorResult GOODS_NOT_EXIT = ErrorResult.create(1000610, "商品不存在!");

    public static final ErrorResult USER_NOT_EXIST = ErrorResult.create(1000611, "用户不存在。");

    //课后作业
    public static final ErrorResult PAPER_ONLY_CAN_BIND_ONE = ErrorResult.create(1000612, "课后作业套题只允许添加一套，如需更改请先移除已选套题");
    public static final ErrorResult COURSE_EXERCISE_EXIST = ErrorResult.create(1000613, "此试题已经被绑定过了！");
    public static final ErrorResult COURSE_EXERCISE_TYPE_WRONG = ErrorResult.create(1000614, "课后作业试题类型错误!");
    public static final ErrorResult SEARCH_CONTENT_MUST_NUMBER=ErrorResult.create(1000615, "ID必须为数字!");
    public static final ErrorResult ID_WRONG=ErrorResult.create(1000616, "课后作业ID错误!");

}

