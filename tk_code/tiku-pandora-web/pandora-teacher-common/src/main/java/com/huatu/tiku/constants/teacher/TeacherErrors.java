package com.huatu.tiku.constants.teacher;


import com.huatu.common.ErrorResult;

/**
 * 教师相关业务报错
 * Created by huangqp on 2017\12\3 0003.
 */
public class TeacherErrors {


    public static final ErrorResult NO_UPDATE_ID_FOR_QUESTION = ErrorResult.create(1000601, "没有修改试题的ID");
    public static final ErrorResult NO_SUPPLY_FIND_BY_COMPOSITE_SUBJECT = ErrorResult.create(1000602, "不支持多科目查询");
    public static final ErrorResult ILLEGAL_QUESTION_SAVE_TYPE = ErrorResult.create(1000602, "非法的试题类型");
    public static final ErrorResult ILLEGAL_QUESTION_SUBJECT= ErrorResult.create(1000602, "无效的试题科目");
    public static final ErrorResult ILLEGAL_PARAM = ErrorResult.create(1000602, "非法的参数");
    public static final ErrorResult ANALYSIS_FAILURE = ErrorResult.create(1000602, "解析失败");
    public static final ErrorResult ILLEGAL_EXAM_NODE = ErrorResult.create(1000602, "非法的考试节点");
    public static final ErrorResult ILLEGAL_EXAM_NODE_LEVEL = ErrorResult.create(1000602, "非法的考试节点层级");
    public static final ErrorResult NOT_NULL_EXAM_NODE_RELATION = ErrorResult.create(1000602, "节点关联关系为空");
    public static final ErrorResult NOT_NULL_PARAM = ErrorResult.create(1000602, "参数不能为空");
    public static final ErrorResult REPEAT_CHILD_EXAM_NODE = ErrorResult.create(1000602, "重复的子节点");
    public static final ErrorResult NO_EXISTED_QUESTION = ErrorResult.create(1000602, "试题不存在");
    public static final ErrorResult NO_EXISTED_PAPER_ENTITY = ErrorResult.create(1000602, "实体试卷不存在");
    public static final ErrorResult NO_EXISTED_PAPER = ErrorResult.create(1000602, "试卷不存在");
    public static final ErrorResult NO_EXISTED_QUESTION_AREA_OR_YERA = ErrorResult.create(1000602, "试题年份地区信息不全");
    public static final ErrorResult NO_EXISTED_QUESTION_KNOWLEDGE = ErrorResult.create(1000602, "试题没有绑定知识点");
    public static final ErrorResult NO_KNOWLEDGE_IN_CATEGORY = ErrorResult.create(1000602, "考试类型不能关联知识点");
    public static final ErrorResult NO_BIND_SUBJECT_IN_KNOWLEDEG = ErrorResult.create(1000602, "知识点未绑定科目信息");
    public static final ErrorResult ILLEGAL_DUPLICATE_PART = ErrorResult.create(1000602, "知识点未绑定科目信息");



    public static final ErrorResult NOT_MATCH_QUESTION_SAVE_TYPE = ErrorResult.create(1000010,"试题类型与操作不匹配");
    public static final ErrorResult NOT_MATCH_PAPER_TYPE = ErrorResult.create(1000010,"试卷类型匹配不到");
    public static final ErrorResult NO_EXISTED_QUESTION_TYPE_FOR_QUERY_BY_STEM = ErrorResult.create(1000010,"关键字查询时，试题类型不能为空");
    public static final ErrorResult NO_EXISTED_SUBJECT = ErrorResult.create(1000010,"不存在科目");
    public static final ErrorResult NO_EXISTED_SUBJECT_KNOWLEDGE = ErrorResult.create(1000010,"存在无知识点的科目");
    public static final ErrorResult DUPLICATE_QUESTION_ID = ErrorResult.create(1000010,"试题id已经存在");
    public static final ErrorResult NO_CANCEL_PUBLISH_QUESTIO=ErrorResult.create(1000011,"请将试题取消发布后才能删除试题");
    public static final ErrorResult IS_NULL=ErrorResult.create(1000011,"查询无结果");
    public static final ErrorResult test=ErrorResult.create(1000012,"请先将该试题从试卷中解绑。");

}

