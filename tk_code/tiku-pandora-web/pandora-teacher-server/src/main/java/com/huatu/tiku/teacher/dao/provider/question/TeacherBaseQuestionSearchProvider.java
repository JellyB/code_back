package com.huatu.tiku.teacher.dao.provider.question;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.baseEnum.BaseStatusEnum;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.question.v1.BaseQuestionSearchReq;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import com.huatu.ztk.commons.SubjectType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lijun on 2018/7/17
 */
@Slf4j
public class TeacherBaseQuestionSearchProvider {

    /**
     * 限定试题来源查询的活动类型（这个可作为查询来源的限定条件，也可以作为）
     **/
    public final static List<ActivityTypeAndStatus.ActivityTypeEnum> SEARCH_ACTIVITY_TYPES = Lists.newArrayList(
            ActivityTypeAndStatus.ActivityTypeEnum.MATCH,
            ActivityTypeAndStatus.ActivityTypeEnum.TRUE_PAPER
    );
    //不做活动类型限定的查询
    public final static List<ActivityTypeAndStatus.ActivityTypeEnum> NO_ACTIVITY_TYPE_LIMIT = Lists.newArrayList();

    /**
     * 列表查询
     * searchType 传入 1 << 1 + 1 << 2 + 1 << 3 分别代表 题干、选项、材料 是否需要作为搜索条件
     */
    public String list(final BaseQuestionSearchReq baseQuestionSearchReq) {
        Supplier<String> whereSql = () -> {
            //按照传入的提交查询 （科目ID，试题ID，试题类型，是否模拟题，是否为废题，是否发布状态，难度级别，都可以从基础信息表查询出来）
            StringBuilder sql = new StringBuilder();
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getSubject())) {
                sql.append(" AND base_question.subject_id =").append(baseQuestionSearchReq.getSubject());
            }
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getQuestionId())) {
                sql.append(" AND base_question.id =").append(baseQuestionSearchReq.getQuestionId());
            }
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getQuestionType())) {//试题类型
                sql.append(" AND base_question.question_type =").append(baseQuestionSearchReq.getQuestionType());
            }
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getMode())) {//是否为模拟题
                sql.append(" AND base_question.`mode` =").append(baseQuestionSearchReq.getMode());
            }
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getAvailFlag())) {//是否为废题
                sql.append(" AND base_question.avail_flag =").append(baseQuestionSearchReq.getAvailFlag());
            }
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getBizStatus())) {//试题状态
                sql.append(" AND base_question.biz_status =").append(baseQuestionSearchReq.getBizStatus());
            }
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getDifficultyLevel())) {//难度等级
                sql.append(" AND base_question.difficulty_level =").append(baseQuestionSearchReq.getDifficultyLevel());
            }
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getSourceFlag())) {//是否有来源
                if (1 == baseQuestionSearchReq.getSourceFlag()) {
                    sql.append(" AND pbq.paper_id is not null");
                }
                if (0 == baseQuestionSearchReq.getSourceFlag()) {
                    sql.append(" AND pbq.paper_id is null");
                }
            }
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getKnowledgeIds())) {
                sql.append(" AND bqk.knowledge_id in (").append(baseQuestionSearchReq.getKnowledgeIds()).append(")");
            }
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getTagIds())) {
                sql.append(" AND bqt.tag_id in (").append(baseQuestionSearchReq.getTagIds()).append(")");
            }
            //年份、地区处理
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getAreaId()) || BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getYear())) {
                sql.append(" AND base_question.id in ( ")
                        .append(buildSearchIdByAreaAndYear(baseQuestionSearchReq.getAreaId(), baseQuestionSearchReq.getYear()))
                        .append(")");
            }
            ArrayList<String> list = Lists.newArrayList();
            //题干、选项、材料 模糊查询匹配
            if (BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getContent()) &&
                    BaseInfo.isNotDefaultSearchValue(baseQuestionSearchReq.getContentSearchType())) {

                if ((baseQuestionSearchReq.getContentSearchType() & 8) > 0) {//材料
                    list.add(buildSearchIdByMaterial(baseQuestionSearchReq.getContent()));
                }

                if ((baseQuestionSearchReq.getContentSearchType() & 4) > 0
                        || (baseQuestionSearchReq.getContentSearchType() & 2) > 0
                        || (baseQuestionSearchReq.getContentSearchType() & 16) > 0) { //题干、选项
                    list.add(buildSearchIdByStemAndChoice(baseQuestionSearchReq.getContent(), baseQuestionSearchReq.getContentSearchType()));
                }
            }
            if (CollectionUtils.isNotEmpty(list)) {
                sql.append(" AND base_question.id in (").append(list.stream().collect(Collectors.joining(" UNION "))).append(") ");
            }
            return sql.toString();
        };
        return buildQuestionSearchSQL(whereSql);
    }

    /**
     * 根据ID 批量查询
     *
     * @param questionIds id 合集
     * @return
     */
    public String listAllByQuestionId(@Param("arg0") List<Long> questionIds) {
        Supplier<String> whereSql = () -> {
            StringBuilder sql = new StringBuilder(128);
            String collect = questionIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            sql.append(" AND base_question.id in (")
                    .append(collect)
                    .append(")");
            return sql.toString();
        };
        //log.info("listAllByQuestionId sql  is :{}", whereSql);
        return buildQuestionSearchSQL(whereSql);
    }

    //批量查询

    private static String buildQuestionSearchSQL(Supplier<String> whereSql) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.qid,t.multiId,t.multiFlag,bq2.question_type AS 'questionType' ");
        sql.append(" FROM (");
        sql.append("SELECT ")
                .append(" DISTINCT (IF(base_question.multi_id = 0,base_question.id,base_question.multi_id)) AS 'qid',")
                .append(" base_question.multi_id AS 'multiId',")
                .append(" base_question.multi_flag AS 'multiFlag' ")
                .append(" FROM ")
                .append(" base_question")
                .append(" LEFT JOIN base_question_tag bqt ON bqt.question_id = base_question.id AND bqt.`status` = 1 ")
                .append(" LEFT JOIN paper_base_question pbq ON pbq.question_id = base_question.id AND pbq.`status` = 1 ")
                .append(" LEFT JOIN base_question_knowledge bqk ON bqk.question_id = base_question.id AND bqk.`status` = 1 ")
                .append(" WHERE 1 = 1 ");
        sql.append(" AND base_question.`status` =").append(BaseStatusEnum.NORMAL.getCode());
        sql.append(whereSql.get());
        sql.append(" GROUP BY qid ");
        sql.append(") t INNER JOIN  base_question bq2 ON t.qid = bq2.id");
        sql.append(" ORDER BY bq2.gmt_create desc,bq2.id");
        log.info(" buildQuestionSearchSQL question search list sql = {}", sql.toString());
        return sql.toString();
    }

    /**
     * 根据 试题年份、区域 查询试题ID信息
     *
     * @param areaIds 区域ID
     * @param year    年份
     * @return
     */
    private String buildSearchIdByAreaAndYear(String areaIds, Integer year) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT question_id ");
        sql.append(" FROM view_question_year_area ");
        sql.append(" WHERE 1 = 1 ");
        if (BaseInfo.isNotDefaultSearchValue(year)) {
            sql.append(" AND year =").append(year);
        }
        if (BaseInfo.isNotDefaultSearchValue(areaIds)) {
            sql.append(" AND area_id in (").append(areaIds).append(") ");
        }
        return sql.toString();
    }

    /**
     * 通过材料信息 获取试题ID
     *
     * @param materialContent 材料内容
     */
    private String buildSearchIdByMaterial(String materialContent) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT question.question_id ");
        sql.append(" FROM material INNER JOIN question_material question ON material.id = question.material_id ")
                .append(" AND question.`status` =").append(BaseStatusEnum.NORMAL.getCode());
        sql.append(" WHERE ")
                .append(" material.`status` =").append(BaseStatusEnum.NORMAL.getCode())
                .append(" AND material.content_filter LIKE '%").append(materialContent).append("%'");
        return sql.toString();
    }

    /**
     * 通过题干、选项 获取试题ID
     *
     * @param content 检索内容
     */
    private String buildSearchIdByStemAndChoice(String content, Integer searchType) {
        String searchSql = Stream.of(QuestionTypeSQLInfoEnum.values())
                .filter(info -> !(((searchType & 2) == 0) && info.getType() == QuestionTypeSQLInfoEnum.SUBJECTIVE.getType()))
                .map(info -> {
                    StringBuilder sql = new StringBuilder();
                    sql.append(" SELECT qdr.question_id ");
                    sql.append(" FROM question_duplicate_relation qdr ");
                    sql.append(info.getTableInfo());
                    sql.append(" WHERE qdr.`status` =").append(BaseStatusEnum.NORMAL.getCode());
                    switch (info) {
                        case OBJECTIVE:
                            sql.append(" AND (").append("qdo.stem_filter LIKE '%").append(content).append("%'");
                            if ((searchType & 4) > 0) {
                                sql.append(" or qdo.choices_filter LIKE '%").append(content).append("%'");
                            }
                            if ((searchType & 16) > 0) {
                                sql.append(" or qdo.analysis_filter like '%").append(content).append("%'");
                            }
                            sql.append(")");
                            break;
                        case SUBJECTIVE:
                            sql.append(" AND (").append("qds.stem_filter LIKE '%").append(content).append("%'");
                            if ((searchType & 16) > 0) {
                                sql.append("  or qds.analyze_question_filter like '%").append(content).append("%'");
                            }
                            sql.append(")");
                    }
                    return sql.toString();
                })
                .collect(Collectors.joining(" UNION "));
        return searchSql;
    }

    /**
     * 构建视图
     */
    private static String buildQuestionSourceViewBackUp(Supplier<String> questionId) {
        StringBuilder sql = new StringBuilder(256);
        StringBuilder tableSql = new StringBuilder();
        tableSql.append(
                " SELECT  " +
                        "   `paper_base_question`.`question_id` AS `question_id`," +
                        "   group_concat( concat(`paper`.`name`,'第',`paper_base_question`.`sort`,'题') ORDER BY `paper_base_question`.`sort` ASC SEPARATOR '、' ) AS `source` " +
                        " FROM  `paper_base_question` " +
                        " INNER JOIN `paper_entity` `paper` ON `paper_base_question`.`paper_id` = `paper`.`id` " +
                        "   AND `paper_base_question`.`status` = 1 AND `paper_base_question`.`paper_type` = 1 AND `paper`.`status` = 1 " +
                        " WHERE `paper_base_question`.`question_id` IN ( " + questionId.get() + ") " +
                        " GROUP BY   " +
                        "   `paper_base_question`.`question_id`   " +
                        "UNION   " +
                        " SELECT " +
                        "   `paper_base_question`.`question_id` AS `question_id`," +
                        "    group_concat(concat(`paper`.`name`,'第',`paper_base_question`.`sort`,'题') ORDER BY `paper_base_question`.`sort` ASC SEPARATOR '、') AS `source` " +
                        " FROM `paper_base_question`  " +
                        " INNER JOIN `paper_activity` `paper` ON `paper_base_question`.`paper_id` = `paper`.`id`   " +
                        "   AND `paper_base_question`.`status` = 1 AND `paper_base_question`.`paper_type` = 2 AND `paper`.`status` = 1  " +
                        " WHERE `paper_base_question`.`question_id` IN  (" + questionId.get() + ")"
        );
        sql.append("SELECT question_source.question_id,GROUP_CONCAT(question_source.source SEPARATOR '、') AS 'source'")
                .append(" FROM (").append(tableSql.toString()).append(") question_source")
                .append(" WHERE  question_source.`question_id` is not NULL ")
                .append(" GROUP BY question_source.question_id");
        return sql.toString();
    }

    /**
     * 构建视图
     */
    private String buildQuestionSourceView(Supplier<String> questionId, Supplier<String> type, Boolean entity) {
        StringBuilder sql = new StringBuilder(256);
        StringBuilder tableSql = new StringBuilder();
        final String typeInfo = type.get();
        Function<String, String> typeCheck = (str -> {
            if (StringUtils.isNotBlank(typeInfo)) {
                return "   AND paper_activity.type in (" + typeInfo + ") ";
            }
            return "";
        });
        tableSql.append("  SELECT " +
                "    paper_base_question.question_id, " +
                "    paper_activity.`name`, " +
                "    paper_base_question.sort  " +
                "  FROM " +
                "    paper_activity " +
                "    INNER JOIN paper_base_question ON paper_activity.id = paper_base_question.paper_id  " +
                "    AND paper_base_question.paper_type = 2  " +
                "    AND paper_base_question.`status` = 1  " +
                "    AND paper_activity.`status` = 1  " +
                "    AND paper_activity.source_flag = 1  " +
                "    AND paper_activity.biz_status = 2  ")
                .append(typeCheck.apply(typeInfo))
                .append("  where paper_base_question.question_id in (").append(questionId.get()).append(") ");
        tableSql.append("  UNION ");
        tableSql.append("  SELECT " +
                "    paper_base_question.question_id, " +
                "    paper_activity.`name`, " +
                "    paper_base_question.sort  " +
                "  FROM " +
                "    paper_activity " +
                "    INNER JOIN paper_base_question ON paper_activity.paper_id = paper_base_question.paper_id  " +
                "    AND paper_base_question.paper_type = 1  " +
                "    AND paper_base_question.`status` = 1  " +
                "    AND paper_activity.`status` = 1  " +
                "    AND paper_activity.source_flag = 1  " +
                "    AND paper_activity.biz_status = 2  ")
                .append(typeCheck.apply(typeInfo))
                .append("  where paper_base_question.question_id in (").append(questionId.get()).append(") ");
        if(entity){
            tableSql.append("  UNION ");
            tableSql.append("  SELECT " +
                    "    paper_base_question.question_id, " +
                    "    paper_entity.`name`, " +
                    "    paper_base_question.sort  " +
                    "  FROM " +
                    "    paper_entity " +
                    "    INNER JOIN paper_base_question ON paper_entity.id = paper_base_question.paper_id  " +
                    "    AND paper_base_question.paper_type = 1  " +
                    "    AND paper_base_question.`status` = 1  " +
                    "    AND paper_entity.`status` = 1  " +
                    "    AND paper_entity.source_flag = 1  ")
                    .append("  where paper_base_question.question_id in (").append(questionId.get()).append(") ");
        }
        sql.append("SELECT  " +
                "  question_source.question_id AS question_id,  " +
                "  GROUP_CONCAT(  " +
                "    CONCAT(  " +
                "      question_source.`name`,  " +
                "      '第',  " +
                "      question_source.sort,  " +
                "      '题'   " +
                "    )   " +
                "  ORDER BY  " +
                "    question_source.`name`,  " +
                "    question_source.sort ASC SEPARATOR '、'   " +
                "  ) AS `source`  ")
                .append(" FROM (").append(tableSql.toString()).append(") question_source")
                .append(" WHERE  " +
                        "  question_source.question_id IS NOT NULL   " +
                        "GROUP BY  " +
                        "  question_source.question_id ");
        System.out.println(sql.toString());
        return sql.toString();
    }


    public static void main(String[] args) {
        TeacherBaseQuestionSearchProvider teacherBaseQuestionSearchProvider = new TeacherBaseQuestionSearchProvider();
        String questionSourceForList = teacherBaseQuestionSearchProvider.getQuestionSourceForList(Lists.newArrayList(1L));
        System.out.println(questionSourceForList);
    }

    /**
     * 批量获取试题的基本信息
     * 试题类型 - 材料信息 - 题干 - 内容/选项 - 试题状态  - 正确答案
     */
    public String getQuestionSimpleInfoForList(List<BaseQuestion> params) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ")
                .append(" t.id,t.`questionType`,t.`bizStatus`,t.`missFlag`,t.`availFlag`,t.`materialContent`,t.knowledgeIds,")
                .append("t.`stem`,t.`answer`,t.`choices`,t.`analyze`,t.`extend`,t.`mode`,")
                .append("qs.`source`");
        sql.append(" FROM (");
        Supplier<String> questionId = () -> params.stream()
                .map(BaseQuestion::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String baseTableInfo = Stream.of(QuestionTypeSQLInfoEnum.values())
                .map(infoEnum -> buildSimpleInfo(infoEnum, questionId))
                .collect(Collectors.joining(" UNION "));
        sql.append(baseTableInfo).append(") t");
        sql.append(" LEFT JOIN (").append(buildQuestionSourceViewByQuestionId(questionId)).append(") qs ON t.id = qs.question_id");
        sql.append(" ORDER by t.`id` DESC");
        return sql.toString();
    }

    private String buildQuestionSourceViewByQuestionId(Supplier<String> questionId) {
        return buildQuestionSourceViewByQuestionIdWithType(questionId, NO_ACTIVITY_TYPE_LIMIT);
    }

    private String buildQuestionSourceViewByQuestionIdWithType(Supplier<String> questionId, List<ActivityTypeAndStatus.ActivityTypeEnum> types) {
        return buildQuestionSourceViewPre(questionId, types, true);
    }

    /**
     * 获取试题的基本信息
     * 试题类型 - 材料信息 - 题干 - 内容/选项 - 试题状态  - 正确答案
     */
    public String getQuestionSimpleInfo(long questionId, int questionType) {
        Integer type = QuestionInfoEnum.getDuplicateTypeByQuestionType(questionType).getCode();
        QuestionTypeSQLInfoEnum questionTypeSQLInfoEnum = QuestionTypeSQLInfoEnum.getByType(type);
        Supplier<String> whereSql = () -> " " + questionId;
        StringBuilder sql = new StringBuilder(buildSimpleInfo(questionTypeSQLInfoEnum, whereSql));
        sql.append(" ORDER BY base_question.gmt_create desc,base_question.id DESC");
        return sql.toString();
    }

    /**
     * 构建基础的 sql 语句
     *
     * @param typeSQLInfoEnum
     * @return
     */
    private static String buildSimpleInfo(QuestionTypeSQLInfoEnum typeSQLInfoEnum, Supplier<String> questionId) {
        /**
         * 1 - question_duplicate_objective
         * 2 - question_duplicate_subjective
         */
        //Integer type = QuestionInfoEnum.getDuplicateTypeByQuestionType(questionType).getCode();
        StringBuilder sql = new StringBuilder(256);
        sql.append("SELECT ");
        sql.append(" base_question.id,")
                .append("base_question.question_type AS 'questionType',")
                .append("base_question.biz_status AS 'bizStatus',")
                .append("base_question.avail_flag AS 'availFlag',")
                .append("base_question.miss_flag AS 'missFlag',")
                .append("material.content AS 'materialContent',")
                .append("knowledgeView.knowledgeIds AS 'knowledgeIds', ")
                .append(typeSQLInfoEnum.getColumnInfo())
                .append(" base_question.`mode`");//防止sql 出错
        sql.append(" FROM ");
        sql.append(" base_question ");
        sql.append(" LEFT JOIN question_material qm ON qm.question_id = base_question.id AND qm.`status` = 1 ");
        sql.append(" LEFT JOIN material ON qm.material_id = material.id AND material.`status` = 1 ");
        sql.append(" INNER JOIN question_duplicate_relation qdr ON qdr.question_id = base_question.id AND qdr.`status` = 1 ");
        sql.append(" LEFT JOIN ").append("(").append(buildKnowledgeByQuestionIds(questionId)).append(") knowledgeView ON base_question.id = knowledgeView.questionId ");
        sql.append(typeSQLInfoEnum.getTableInfo());
        sql.append(" WHERE ");
        sql.append(" base_question.`status` = 1");
        sql.append(" AND base_question.id in (").append(questionId.get()).append(")");
        return sql.toString();
    }


    /**
     * 新增从表之后 需在此添加枚举信息
     * 必须固定三个字段 stem、answer、choices、analyze、extend
     * 关联关系必须使用 INNER JOIN
     */
    @AllArgsConstructor
    @Getter
    enum QuestionTypeSQLInfoEnum {
        //客观题
        OBJECTIVE(1,
                " qdo.stem,qdo.answer,qdo.choices,qdo.`analysis` AS 'analyze',qdo.extend, ",
                " INNER JOIN question_duplicate_objective qdo " +
                        "ON qdo.id = qdr.duplicate_id " +
                        " AND qdo.`status` = " + BaseStatusEnum.NORMAL.getCode() +
                        " AND qdr.duplicate_type = " + QuestionInfoEnum.QuestionDuplicateTypeEnum.JUDGE_OBJECT.getCode()),
        //主观题（因为不同的表，对应的查询字段名称不一样）
        SUBJECTIVE(2,
                " qds.stem,qds.answer_comment AS 'answer','' AS 'choices',qds.analyze_question AS 'analyze',qds.extend, ",
                " INNER JOIN question_duplicate_subjective qds " +
                        "ON qds.id = qdr.duplicate_id " +
                        " AND qds.`status` =  " + BaseStatusEnum.NORMAL.getCode() +
                        " AND qdr.duplicate_type = " + QuestionInfoEnum.QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE.getCode()),
        ;
        private Integer type;
        private String columnInfo;
        private String tableInfo;

        public static QuestionTypeSQLInfoEnum getByType(int type) {
            Optional<QuestionTypeSQLInfoEnum> infoEnum = Stream.of(QuestionTypeSQLInfoEnum.values())
                    .filter(info -> info.getType() == type)
                    .findAny();
            if (!infoEnum.isPresent()) {
                throw new BizException(ErrorResult.create(5000000, "试题类型错误"));
            }
            return infoEnum.get();
        }

    }

//    /**
//     * 获取试题来源信息--视图方式得到的数据，可能因字段太长，返回不全
//     */
//    public String getQuestionSourceInfo(long questionId) {
//        StringBuilder sql = new StringBuilder(256);
//        sql.append(" SELECT ");
//        sql.append(" source ");
//        sql.append(" FROM ");
//        sql.append(" view_question_source ");  //视图“view_question_source”
//        sql.append(" WHERE ");
//        sql.append(" question_id = ").append(questionId);
//        return sql.toString();
//    }

    /**
     * 获取试题来源信息--通过直接连表查询获取数据
     */
    public String getQuestionSourceInfo(long questionId) {
        StringBuilder sql = new StringBuilder(256);
        sql.append(" SELECT ");
        sql.append(" paper_entity.`name` as `name`,paper_base_question.sort  ");
        sql.append(" FROM ");
        sql.append(" paper_entity ");
        sql.append(" JOIN paper_base_question ON paper_entity.id = paper_base_question.paper_id ");
        sql.append(" AND paper_entity.`status` = 1 ");
        sql.append(" AND paper_base_question.`status` = 1 ");
        sql.append(" AND paper_base_question.paper_type = 1 ");
        sql.append(" WHERE ");
        sql.append(" paper_base_question.question_id = ").append(questionId);

        sql.append(" UNION ");

        sql.append(" SELECT ");
        sql.append(" paper_activity.`name` as `name`,paper_base_question.sort  ");
        sql.append(" FROM ");
        sql.append(" paper_activity ");
        sql.append(" JOIN paper_base_question ON paper_activity.id = paper_base_question.paper_id ");
        sql.append(" AND paper_activity.`status` = 1 ");
        sql.append(" AND paper_base_question.`status` = 1 ");
        sql.append(" AND paper_base_question.paper_type = 2 ");
        sql.append(" WHERE ");
        sql.append(" paper_base_question.question_id = ").append(questionId);
        return sql.toString();
    }

    /**
     * 获取试题所属标签
     *
     * @param questionId
     * @return
     */
    public String getQuestionTagInfo(long questionId) {
        StringBuilder sql = new StringBuilder(256);
        sql.append(" SELECT ");
        sql.append(" t.id as id, ");
        sql.append(" t.`name` as name ");
        sql.append(" FROM ");
        sql.append(" tag t ");
        sql.append(" LEFT JOIN base_question_tag qt ON qt.tag_id = t.id ");
        sql.append(" AND qt.`status` = 1 ");
        sql.append(" WHERE ");
        sql.append(" t.`status` = 1 ");
        sql.append(" AND qt.question_id = ").append(questionId);
        return sql.toString();
    }

    /**
     * 查询某个科目下的 MongoDB数据
     */
    public String listAllMongoDBQuestionBySubject(long subjectId) {
        StringBuilder sql = new StringBuilder(256);
        sql.append("SELECT base_question.id,knowledge_id AS 'knowledgeId' ");
        sql.append(" FROM base_question ");
        if (SubjectType.GWY_XINGCE == subjectId) {
            sql.append(" INNER JOIN paper_base_question pbq ON base_question.id = pbq.question_id AND pbq.`status` = 1 AND paper_type = 1 ")
                    .append(" INNER JOIN paper_entity pe ON pe.id = pbq.paper_id AND pe.`status` = 1 ");
        }
        sql.append(" LEFT JOIN base_question_knowledge bqk ON base_question.id = bqk.question_id AND bqk.`status` = 1 ");
//                .append(" LEFT JOIN knowledge ON bqk.knowledge_id = knowledge.id AND knowledge.`status` = 1 ");

        sql.append(" where ")
                .append(" base_question.`status` = 1 ")
                .append(" AND base_question.biz_status = 2 ")
                .append(" AND base_question.subject_id = ").append(subjectId).append(" ");
        if (SubjectType.GWY_XINGCE == subjectId) {
            sql.append(" AND pe.`mode` = 1 and pe.year >= 2008 ");    //mode无用字段，由paper确定
            sql.append(" AND base_question.question_type IN (")
                    .append(QuestionInfoEnum.QuestionTypeEnum.SINGLE.getCode()).append(",")
                    .append(QuestionInfoEnum.QuestionTypeEnum.MULTI.getCode()).append(",")
                    .append(QuestionInfoEnum.QuestionTypeEnum.JUDGE.getCode()).append(",");
            sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
        } else {
            sql.append(" AND base_question.question_type IN (");
            QuestionInfoEnum.QuestionTypeEnum[] values = QuestionInfoEnum.QuestionTypeEnum.values();
            for (QuestionInfoEnum.QuestionTypeEnum value : values) {
                if (value.getDuplicateTypeEnum().equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.JUDGE_OBJECT)) {
                    sql.append(value.getCode()).append(",");
                }
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
        }
        return sql.toString();
    }

    /**
     * 分组查询复合题下子题个数，并返回子题个数大于5的复合题
     *
     * @return
     */
    public String findQuestionIdByMultiId() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(" multi_id, ");
        stringBuilder.append(" count(1) as total");
        stringBuilder.append(" FROM ");
        stringBuilder.append(" base_question ");
        stringBuilder.append(" WHERE ");
        stringBuilder.append(" `status` = 1 ");
        stringBuilder.append(" AND subject_id = 1 ");
        stringBuilder.append(" GROUP BY ");
        stringBuilder.append(" multi_id ");
        stringBuilder.append(" HAVING ");
        stringBuilder.append(" total < 5 ");
        return stringBuilder.toString();
    }

    /**
     * 批量查询试题来源--所有活动类型，不包含实体卷来源
     *
     * @param questionIds 试题ID
     * @return
     */
    public String getQuestionSourceForList(@Param("arg1") List<Long> questionIds) {
        return getQuestionSourceForListByType(questionIds, Lists.newArrayList());
    }

    /**
     * 批量查询试题来源--不包含实体卷来源
     *
     * @param questionIds 试题Id
     * @param types       活动类型
     * @return
     */
    public String getQuestionSourceForListByType(List<Long> questionIds, List<ActivityTypeAndStatus.ActivityTypeEnum> types) {
        return getQuestionSourceForListByTypeWithEntity(questionIds, types, true);
    }

    /**
     * 批量查询试题来源
     *
     * @param questionIds 试题ID
     * @param types       活动类型
     * @param entityFlag  是否包含实体卷来源（false实体卷来源不计入，true实体卷来源计入）
     * @return
     */
    public String getQuestionSourceForListByTypeWithEntity(List<Long> questionIds, List<ActivityTypeAndStatus.ActivityTypeEnum> types, Boolean entityFlag) {
        Supplier<String> questionId = () -> questionIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return buildQuestionSourceViewPre(questionId, types, entityFlag);
    }

    private String buildQuestionSourceViewPre(Supplier<String> questionId, List<ActivityTypeAndStatus.ActivityTypeEnum> types, Boolean entityFlag) {
        Supplier<String> type = () -> types.stream()
                .map(ActivityTypeAndStatus.ActivityTypeEnum::getKey)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return buildQuestionSourceView(questionId, type, entityFlag);
    }

    /**
     * 返回 试题与知识点的对应关系-聚合
     * 一个试题可能对应多个知识点
     *
     * @return 试题与知识点的对应关系-聚合
     */
    public static String buildKnowledgeByQuestionIds(Supplier<String> questionId) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT a.question_id as questionId,GROUP_CONCAT(a.knowledge_id) as knowledgeIds");
        stringBuffer.append(" FROM base_question_knowledge a");
        stringBuffer.append(" WHERE a.question_id in (");
        stringBuffer.append(questionId.get());
        stringBuffer.append(" ) AND a.`status`=1 ");
        stringBuffer.append(" GROUP BY question_id ");
        return stringBuffer.toString();
    }

    /**
     * 无论是否删除，都可以查询出来
     *
     * @param questionId
     * @return
     */
    public String findBaseQuestion(Long questionId) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" SELECT * FROM base_question b where b.id= ");
        stringBuffer.append(questionId);
        return stringBuffer.toString();
    }


}

