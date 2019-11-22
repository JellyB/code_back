package com.huatu.tiku.essay.repository;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.vo.admin.PaperAnswerStatisVO;
import com.huatu.tiku.essay.vo.resp.EssayExecrisesQuestionAnswerRankVO;

public interface

EssayQuestionAnswerRepository extends JpaRepository<EssayQuestionAnswer, Long>, JpaSpecificationExecutor<EssayQuestionAnswer> {


    List<EssayQuestionAnswer> findByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndAnswerCardTypeOrderByGmtModifyDesc(int userId, Long questionBaseId, long paperId, int status, int answerCardType);

    List<EssayQuestionAnswer> findByUserIdAndPaperIdAndStatusAndQuestionBaseIdInAndAnswerCardType(int userId, long paperId, int status, List<Long> questionId,int answerCardType);

    List<EssayQuestionAnswer> findByUserIdAndPaperIdAndStatusAndQuestionBaseIdAndAnswerCardType(int userId, long paperId, int status, Long questionId,int answerCardType);

    List<EssayQuestionAnswer> findByUserIdAndPaperAnswerIdIn(int userId, long paperAnswerId);

    int countByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardType(int userId, long questionBaseId, long paperId, int status, int bizStatus, int answerCardType);

    List<EssayQuestionAnswer> findByPaperAnswerIdAndUserIdAndStatus(long paperAnswerId, int userId, int status);

    Page<EssayQuestionAnswer> findAll(Specification querySpecific, Pageable pageable);


    List<EssayQuestionAnswer> findByPaperAnswerIdAndStatusAndBizStatus(long paperAnswerId, int status, int bizStatus, Sort sort);

    List<EssayQuestionAnswer> findByPaperAnswerIdAndStatus(long paperAnswerId, int status, Sort sort);


    long countByUserIdAndPaperIdAndStatusAndBizStatusAndAnswerCardType(int userId, long paperId, int status, int bizStatus, int answerCardType);
    long countByUserIdAndPaperIdAndStatusAndBizStatus(int userId, long paperId, int status, int bizStatus);

    //试题总交卷人数
    @Query("select count(qa) from  EssayQuestionAnswer qa  where qa.userId = ?1 and qa.paperId = ?2 and qa.questionBaseId in ?3 and qa.status = ?4 and qa.bizStatus = ?5 ")
    long countByUserIdAndPaperIdAndQuestionBaseIdListAndStatusAndBizStatus(int userId, long paperId, List<Long> questionIdList, int status, int bizStatus);


    List<EssayQuestionAnswer> findByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtModifyDesc(int userId, long questionBaseId, long paperId, int status, int bizStatus,int answerCardType);

    /**
     * 统计整体的批改次数
     * @param i
     * @param bizStatus
     * @return
     */
    long countByPaperIdAndBizStatus(Long i, int bizStatus);


    List<EssayQuestionAnswer> findByQuestionBaseIdAndStatusAndBizStatusAndAnswerCardType(Long questionBaseId, int status, int bizStatus,int answerCardType);

    @Transactional
    @Modifying
    @Query(value = "update v_essay_question_answer set content = ?1,spend_time = ?2,input_word_num = ?3, biz_status = if(biz_status <> 3,?4,3) where id=?5", nativeQuery = true)
    int updateById(String content, int spendTime, int inputWordNum, int bizStatus, long id);

//    @Query("select pb from  EssayQuestionAnswer pb  where pb.id = ?1 and pb.bizStatus = 3")
//    EssayQuestionAnswer findCorrect(long answerCardId);

    int countByQuestionBaseIdAndStatusAndAnswerCardType(long questionBaseId, int status, int answerCardType);

    int countByQuestionBaseIdAndStatusAndBizStatusAndAnswerCardType(long questionBaseId, int status, int bizStatus, int answerCardType);
    int countByQuestionBaseIdAndStatusAndBizStatus(long questionBaseId, int status, int bizStatus);

    int countByQuestionBaseIdAndStatusInAndBizStatusAndCorrectModeIn(long questionBaseId, List<Integer> status, int bizStatus, List<Integer> correctMode);

    long countByPaperAnswerIdAndQuestionBaseIdAndStatusAndAnswerCardType(long paperAnswerId, long questionId, int status, int answerCardType);

    //自定义返回类型
    @Query(value = "select new com.huatu.tiku.essay.vo.admin.PaperAnswerStatisVO(max(eqa.examScore),min(eqa.examScore) ,avg(eqa.examScore) ,count(eqa)) from EssayQuestionAnswer eqa where eqa.questionBaseId=?1 and status=1 and bizStatus=3 and eqa.paperId =?2")
    List<PaperAnswerStatisVO> findStatisData(long questionBaseId, long paperId);

    @Transactional
    @Modifying
    @Query(value = "update v_essay_question_answer set status = 2 where id=?1", nativeQuery = true)
    int updateToRecycle(long answerId);


    List<EssayQuestionAnswer> findByUserIdAndPaperIdAndQuestionTypeNotAndStatusAndBizStatusInAndAnswerCardType(int userId, long paperId, int type, int status, List<Integer> bizStatusList, int answerCardType, Pageable pageRequest);
    List<EssayQuestionAnswer> findByUserIdAndPaperIdAndQuestionTypeNotAndStatusAndBizStatusIn(int userId, long paperId, int type, int status, List<Integer> bizStatusList, Pageable pageRequest);

    List<EssayQuestionAnswer> findByUserIdAndPaperIdAndQuestionTypeAndStatusAndBizStatusInAndAnswerCardType(int userId, long paperId, int type, int status, List<Integer> bizStatusList, int answerCardType, Pageable pageRequest);
    List<EssayQuestionAnswer> findByUserIdAndPaperIdAndQuestionTypeAndStatusAndBizStatusIn(int userId, long paperId, int type, int status, List<Integer> bizStatusList, Pageable pageRequest);

    Long countByUserIdAndPaperIdAndQuestionTypeNotAndStatusAndBizStatusIn(int userId, long paperId, int type, int status, List<Integer> bizStatusList);

    Long countByUserIdAndPaperIdAndQuestionTypeAndStatusAndBizStatusInAndAnswerCardType(int userId, long paperId, int type, int status, List<Integer> bizStatusList, int answerCardType);
    Long countByUserIdAndPaperIdAndQuestionTypeAndStatusAndBizStatusIn(int userId, long paperId, int type, int status, List<Integer> bizStatusList);

    List<EssayQuestionAnswer> findByUserIdAndPaperIdAndStatusAndBizStatusInAndAnswerCardType(int userId, long paperId, int status, LinkedList<Integer> bizStatusList, int answerCardType, Pageable pageRequest);

    @Transactional
    @Modifying
    @Query(value = "update v_essay_question_answer set copy_ratio = ?2 where id=?1", nativeQuery = true)
    int updateCopyRatioById(long answerId, double copyRatio);

    //未批改完成的答题卡
    @Query("select qa from  EssayQuestionAnswer qa  where qa.paperAnswerId = 0 and qa.bizStatus =2  and qa.status = 1 and qa.gmtModify > ?1 and qa.gmtModify < ?2 and qa.correctMode=1")
    List<EssayQuestionAnswer> findUnfinishedCard(Date tenMinutesBefore, Date fiveMinutesBefore);

    @Query(value = "select * from v_essay_question_answer where question_base_id = ?1 and biz_status = 3 and status = 1  and exam_score >= ?2 and exam_score <= ?3  order by correct_date desc  limit 5 ", nativeQuery = true)
    List<EssayQuestionAnswer> findByExportCondition(long id, double examScoreMin, double examScoreMax);

    List<EssayQuestionAnswer> findByIdIn(List<Long> answerIdList);

    //Test
    List<EssayQuestionAnswer> findByQuestionDetailId(long id);

    @Query(value = "select exam_score as examScore,question_base_id as id,paper_answer_id as paperAnswerId from v_essay_question_answer where question_base_id in ?1 and biz_status = 3 and status = 1 ", nativeQuery = true)
    List<Object[]> findScores(List<Long> ids);

    //人工批改
    EssayQuestionAnswer findByIdAndStatus(Long answerId, int status);

    /**
     * 获取指定类型的某个状态的答题卡数量
     *
     * @param userId
     * @param questionBaseId
     * @param
     * @param status
     * @param bizStatus
     * @return
     */
    long countByUserIdAndQuestionBaseIdAndPaperIdAndCorrectModeAndStatusAndBizStatusAndAnswerCardType(int userId,
                                                                                     Long questionBaseId, long paperId, int correctMode, int status, int bizStatus,int answerCardType);

//    /**
//     * 获取指定类型的某个用户答题卡数量
//     *
//     * @param userId
//     * @param questionBaseId
//     * @param i
//     * @param correctMode
//     * @param status
//     * @param bizStatus
//     * @return
//     */
//    List<EssayQuestionAnswer> findByUserIdAndQuestionBaseIdAndPaperIdAndCorrectModeAndStatusAndBizStatusOrderByGmtModifyDesc(
//            int userId, long questionBaseId, int i, int correctMode, int status, int bizStatus);

    //clear
    List<EssayQuestionAnswer> findByCorrectModeIn(List<Integer> modes);

    List<EssayQuestionAnswer> findByUserIdAndPaperIdAndStatusInAndQuestionBaseIdInAndAnswerCardType(int userId, long paperId, List<Integer> status, List<Long> questionId,int answerCardType);
    
    /**
     * 查询用户和答题卡id对应关系
     * @param ids
     * @return
     */
    //@Query(value = "select new com.huatu.tiku.essay.vo.resp.EssayExecrisesQuestionAnswerRankVO(user_id,id,spend_time,submit_time) from v_essay_question_answer where id in ?1 ")
    //List<EssayExecrisesQuestionAnswerRankVO> findUserIdByIds(List<Long> ids);
}

