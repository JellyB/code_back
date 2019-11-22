package com.huatu.tiku.essay.constant.status;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/24 13:36
 * @Description
 */
public class ACArithParams {

    public static final String[] title = {
            "考试","试卷",
    };
    public static final String[] halfTitle = {
            "时长","总分","分数"
    };
    public static final String[] duration = {
            "时长",
    };
    public static final String[] totalScore = {
            "总分","分数"
    };
    public static final String[] area = {
            "北京","天津","上海","重庆","河北","山西","辽宁","吉林","黑龙江","江苏","浙江","安徽","福建",
            "江西","山东","河南","湖北","湖南","广东","海南","四川","贵州","云南","陕西","甘肃","青海",
            "台湾","蒙古","广西","西藏","宁夏","新疆","香港","澳门",
    };
    public static final String[] subArea = {
            "A","B","C","D","县级","乡镇","乙级","甲级","省直","公检法","县乡",
    };
    public static final String[] cn_parenthesis = {
            "（","）"
    };
    public static final String[] cn_parenthesis_score = {
            "（","分）"
    };
    public static final String[] en_parenthesis = {
            "(",")"
    };
    public static final String[] en_parenthesis_score = {
            "(","分)"
    };
    public static final String[] zishu = {
            "字数限制：","字数限制:","字数：","字数:"
    };
    public static final String[] zishu_kou = {
            "每少","字扣"
    };
    public static final String[] questionSort = {
            "一","二","三","四","五","六","七","八","九","十",
    };
    //鉴于【批改得分】这个字段在文档中存在的不确定性,所以不设其为检索字段,只检索得分项和扣分项
    public static final String[] questionParams = {
            "标题：","题型：","【答案书写】","得分项：","扣分项：","【参考答案】","【标准答案】","【阅卷规则】","【试题分析】","【材料与参考答案点评】","【材料与标准答案点评】",
    };
    //这个有点坑啊
    public static final String[] uuti = {
            "主题：",
    };
    public static final String[] questionParams_daoriuxd = {
            "得分项："
    };
    public static final String[] questionType = {
            "概括归纳","综合分析","解决问题","应用","文章写作",
    };
    public static final String[] questionDifficultGrade = {
            "较小","适中","较大",
    };

    public static final String material = "给定资料";
    public static final String zuodayaoqiu = "作答要求";
    public static final String[] outer = {
            // 以下是必需的参数，资料试题必须放在一个文档中
            "【标题】","【时间】","【地区】","【试卷总分】","【答题时限】","给定资料","【试题】"};
    public static final String[] gzt_question = {//归纳概括题与综合分析题和提出对策题
            "/试题类型/","/试题分数/","/标题/","/给定资料/","/作答要求/","/参考答案/","/批改得分/",
            "/扣分标准/","/阅卷规则/","/试题分析/","/材料与参考答案点评/"
    };
    public static final String[] yy_question = {//应用文
            "/试题类型/","/试题分数/","/标题/","/给定资料/","/作答要求/","/参考答案/","/格式/",
            "/关键句/","/扣分标准/","/阅卷规则/","/试题分析/","/材料与参考答案点评/"
    };
    public static final String[] yl_question = {//议论文
            "/试题类型/","/试题分数/","/标题/","/给定资料/","/作答要求/","/参考答案/","/主题/",
            "/中心论点/","/扣分标准/","/阅卷规则/","/试题分析/","/材料与参考答案点评/"
    };
}
