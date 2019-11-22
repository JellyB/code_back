package com.huatu.tiku.course.dao.provider;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-06 5:48 PM
 **/
@Slf4j
public class CourseExercisesProcessEssayLogProvider {

    public String getEssayCoursePageInfo(long userId, int page, int size){
        String tempTable = essayTempTable(userId);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT * FROM (")
                .append(tempTable)
                .append(" ) AS temp_table")
                .append(" LIMIT ")
                .append((page -1) * size).append(",").append(size);
        return stringBuilder.toString();
    }

    /**
     * 申论查询信息
     * @param userId
     * @return
     */
    private String essayTempTable(long userId){
        List<Integer> list = Lists.newArrayList(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus(),
                EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus(),
                EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" course_id as courseId,");
        stringBuilder.append(" GROUP_CONCAT( distinct syllabus_id) AS syllabusIds");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_exercises_answer_meta");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" biz_status in (");
        stringBuilder.append(Joiner.on(",").join(list));
        stringBuilder.append(" )");
        stringBuilder.append(" AND user_id = ").append(userId);
        stringBuilder.append(" AND status = ").append(EssayStatusEnum.NORMAL.getCode());
        stringBuilder.append(" GROUP BY");
        stringBuilder.append(" course_id");
        //stringBuilder.append(" ORDER BY");
        //stringBuilder.append(" gmt_modify DESC");
        log.info("getEssayCoursePageInfo.sql info: userId:{}", userId);
        return stringBuilder.toString();
    }

    /**
     * 获取多题答题卡状态
     * @param userId
     * @param userId
     * @return
     */
    public String selectDistinctSyllabusIdByUserId(@Param(value = "userId") int userId){

        List<Integer> list = Lists.newArrayList(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus(),
                EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus(),
                EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" distinct syllabus_id");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_exercises_answer_meta");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" biz_status in (");
        stringBuilder.append(Joiner.on(",").join(list));
        stringBuilder.append(" )");
        stringBuilder.append(" AND user_id = ").append(userId);
        stringBuilder.append(" AND status = ").append(EssayStatusEnum.NORMAL.getCode());
        log.info("selectDistinctSyllabusIdByUserId.sql info: userId:{}", userId);
        return stringBuilder.toString();
    }


    public String selectQuestionBaseById(@Param(value = "questionBaseId") Long questionBaseId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" *");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_question_base t");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" t.id = ").append(questionBaseId);
        return stringBuilder.toString();
    }

    public String selectPaperBaseById(@Param(value = "paperBaseId") Long paperBaseId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" *");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_paper_base");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" id = ").append(paperBaseId);
        return stringBuilder.toString();
    }

    /**
     * 根据 id 查询 question_detail
     * @param detailId
     * @return
     */
    public String selectQuestionDetailById(@Param(value = "detailId") Long detailId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" *");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_question_detail");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" id = ").append(detailId);
        return stringBuilder.toString();
    }

    /**
     * 查询单题 答题卡
     * @param id
     * @return
     */
    public String selectQuestionAnswerById(@Param(value = "id") Long id){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" *");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_question_answer");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" id = ").append(id);
        return stringBuilder.toString();
    }


    /**
     * 查询套题 答题卡
     * @param id
     * @return
     */
    public String selectPaperAnswerById(@Param(value = "id") Long id){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" *");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_paper_answer");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" id = ").append(id);
        return stringBuilder.toString();
    }


    /**
     * 根据 questionBaseId 查询 similarId
     * @param questionBaseId
     * @return
     */
    public String selectByQuestionBaseId(@Param(value = "questionBaseId") Long questionBaseId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" *");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_similar_question");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" question_base_id = ").append(questionBaseId);
        return stringBuilder.toString();
    }


    /**
     * 获取meta表答题卡状态
     * @param answerCardId
     * @return
     */
    public String getBizStatusByCardId(@Param(value = "answerCardId") long answerCardId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" * ");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_exercises_answer_meta");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" answer_id = ").append(answerCardId);

        return stringBuilder.toString();
    }

    /**
     * 获取用户当前的答题卡信息
     * @param userId
     * @param syllabusId
     * @return
     */
    public String getAnswerCardInfoBySyllabusId(int userId, long syllabusId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" answer_id, biz_status");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_exercises_answer_meta");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" user_id = ").append(userId);
        stringBuilder.append(" AND syllabus_id = ").append(syllabusId);
        stringBuilder.append(" AND status = ").append(EssayStatusEnum.NORMAL.getCode());
        stringBuilder.append(" order by correct_num DESC limit 1");



        return stringBuilder.toString();
    }

    /**
     * 获取用户未做完课后作业练习数
     * @param userId
     * @param syllabusId
     * @return
     */
    public String selectUnDoQuestionCountBySyllabusId(int userId, long syllabusId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" count(id) as cnt ");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_exercises_answer_meta");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" user_id = ").append(userId);
        stringBuilder.append(" AND syllabus_id = ").append(syllabusId);
        stringBuilder.append(" AND biz_status = ").append(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        return stringBuilder.toString();
    }

    /**
     * 获取被退回信息
     * @param answerCardType
     * @param answerCardId
     * @return
     */
    public String selectByAnswerCardIdAndType(int answerCardType, long answerCardId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" *");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_correct_order");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" answer_card_type = ").append(answerCardType);
        stringBuilder.append(" AND answer_card_id = ").append(answerCardId);
        stringBuilder.append(" AND status = ").append(EssayStatusEnum.NORMAL.getCode());
        return stringBuilder.toString();
    }

    /**
     * 查询用户当前 correctNum
     * @param userId
     * @param syllabusId
     * @return
     */
    public String selectCurrentCorrectNum(int userId, long syllabusId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" max(correct_num) as correct_num");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_exercises_answer_meta");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" user_id = ").append(userId);
        stringBuilder.append(" AND syllabus_id = ").append(syllabusId);
        stringBuilder.append(" AND status = ").append(EssayStatusEnum.NORMAL.getCode());
        return stringBuilder.toString();
    }

    /**
     * 获取不同答题卡状态 count
     * @param userId
     * @param syllabusId
     * @param correctNum
     * @return
     */
    public String selectMultiQuestionBizStatusCount(int userId, long syllabusId, int correctNum){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" biz_status, count(biz_status) AS cnt ");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_exercises_answer_meta");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" user_id = ").append(userId);
        stringBuilder.append(" AND syllabus_id = ").append(syllabusId);
        stringBuilder.append(" AND correct_num = ").append(correctNum);
        stringBuilder.append(" AND status = ").append(EssayStatusEnum.NORMAL.getCode());
        stringBuilder.append(" GROUP BY (biz_status)");
        return stringBuilder.toString();
    }
    
    /**
     * 获取多题答题卡状态
     * @param userId
     * @param syllabusId
     * @return
     */
    public String selectMultiBizStatusCount(int userId, long syllabusId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" biz_status, id, answer_id, p_qid, correct_num");
        stringBuilder.append(" FROM");
        stringBuilder.append(" v_essay_exercises_answer_meta");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" user_id = ").append(userId);
        stringBuilder.append(" AND syllabus_id = ").append(syllabusId);
        stringBuilder.append(" AND status = ").append(EssayStatusEnum.NORMAL.getCode());
        return stringBuilder.toString();
    }
}
