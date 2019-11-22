package com.huatu.tiku.essay.repository;


import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.vo.admin.PaperAnswerStatisVO;
import com.huatu.tiku.essay.vo.admin.answer.AdminPaperAnswerCountVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by huangqp on 2017\11\26 0026.
 */
public interface EssayPaperAnswerRepository extends JpaRepository<EssayPaperAnswer, Long>, JpaSpecificationExecutor<EssayPaperAnswer> {
    List<EssayPaperAnswer> findByUserIdAndStatusAndPaperBaseIdInAndAnswerCardType(int userId, int status, List<Long> paperIds, int answerCardType);

    List<EssayPaperAnswer> findByUserIdAndPaperBaseIdAndStatusAndAnswerCardType(int userId, Long paperId, int status, int answerCardType);

//    List<EssayPaperAnswer> findByUserIdAndPaperBaseIdAndBizStatusAndStatusOrderByGmtCreateDesc(int userId, long paperId, int bizStatus, int status);

    List<EssayPaperAnswer> findByUserIdAndPaperBaseIdAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(int userId, long paperId, int status, int answerCardType);

//    List<EssayPaperAnswer> findByUserId(int userId, Pageable pageRequest);

    /**
     * 用于答题卡批改次查询（不计批改形式）
     *
     * @param userId
     * @param paperBaseId
     * @param status
     * @param bizStatus
     * @return
     */
    int countByUserIdAndPaperBaseIdAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtModifyDesc(int userId, long paperBaseId, int status, int bizStatus, int answerCardType);


    int countByUserIdAndPaperBaseIdAndTypeAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtModifyDesc(int userId, long paperBaseId, int type, int status, int bizStatus, int answerCardType);

    /**
     * 废弃题目查询专用
     *
     * @param userId
     * @param status
     * @param bizStatusList
     * @param pageRequest
     * @return
     */
    List<EssayPaperAnswer> findByUserIdAndStatusAndBizStatusIn(int userId, int status, List<Integer> bizStatusList, Pageable pageRequest);

    List<EssayPaperAnswer> findByUserIdAndStatusAndAnswerCardTypeAndTypeAndBizStatusIn(int userId, int status, int answerCardType, int type, List<Integer> bizStatusList, Pageable pageRequest);


    long countByUserIdAndStatusAndAnswerCardTypeAndBizStatus(int userId, int status, int answerCardType, int bizStatus);

    /**
     * 废弃答题卡查询专用
     *
     * @param userId
     * @param status
     * @param bizStatus
     * @return
     */
    long countByUserIdAndStatusAndBizStatus(int userId, int status, int bizStatus);

    long countByUserIdAndStatusAndAnswerCardTypeAndBizStatusIn(int userId, int status, int answerCardType, List<Integer> bizStatus);

    @Query(value = "select area_name from essay.v_essay_paper_answer where exam_score =  (SELECT max(exam_score)  FROM essay.v_essay_paper_answer where paper_base_id = ?1) and paper_base_id = ?1", nativeQuery = true)
    List<String> getMaxScoreArea(long paperBaseId);

    @Query(value = "select area_name from v_essay_paper_answer where paper_base_id = ?1 group by area_name having avg(exam_score) order by avg(exam_score) desc", nativeQuery = true)
    List<String> getAvgMaxAreas(long paperBaseId);

    @Query(value = "select area_name from v_essay_paper_answer where paper_base_id = ?1 group by area_name order by count(id) desc", nativeQuery = true)
    List<String> getCountMaxAreas(long paperBaseId);

    @Query(value = "select count(id) from v_essay_paper_answer where exam_score >= ?1 and exam_score <= ?2 and paper_base_id = ?3", nativeQuery = true)
    Integer getScoreRange(Double score, Double score2, Long paperBaseId);


//    List<EssayPaperAnswer> findByAreaIdAndPaperBaseId(Long areaId, Long paperBaseId);

    List<EssayPaperAnswer> findByPaperBaseIdAndStatus(Long paperBaseId, int status);

    List<EssayPaperAnswer> findByPaperBaseIdAndStatusAndAnswerCardTypeAndBizStatus(Long paperBaseId, int status, int answerCardType, int bizStatus);

    List<EssayPaperAnswer> findByPaperBaseIdAndStatusAndAnswerCardTypeAndBizStatusAndAreaIdIn(Long paperBaseId, int status, int answerCardType, int bizStatus, List<Long> areaIds);

//    Long countByPaperBaseIdAndBizStatus(Long id, int bizStatus);

//    @Query(value = "select max(exam_score) from v_essay_paper_answer where paper_base_id=?1 ", nativeQuery = true)
//    Double findMaxScoreById(Long id);

//    @Query(value = "select min(exam_score) from v_essay_paper_answer where paper_base_id=?1 ", nativeQuery = true)
//    Double findMinScoreById(Long id);

//    @Query(value = "select avg(exam_score) from v_essay_paper_answer where paper_base_id=?1 ", nativeQuery = true)
//    Double findAverageScoreById(Long id);

    @Transactional
    @Modifying
    @Query(value = "update v_essay_paper_answer set last_index = ?1,spend_time = ?2,unfinished_count = ?3, biz_status = if(biz_status <> 3,?4,3) where id=?5", nativeQuery = true)
    int updateById(int lastIndex, int spendTime, int unfinishedCount, int bizStatus, long id);


    List<EssayPaperAnswer> findByUserIdAndPaperBaseIdAndBizStatusAndStatusAndTypeAndAnswerCardTypeOrderByGmtCreateDesc(int userId, long paperId, int bizStatus, int status, int type, int answerCardType);


//    @Query("select pb from  EssayPaperAnswer pb  where pb.id = ?1 and pb.bizStatus = 3")
//    EssayPaperAnswer findCorrect(long answerId);

//    @Query("select pb.id from  EssayPaperAnswer pb  where pb.type = 0 and pb.userId =  ?1 order by pb.correctDate desc")
//    List<Long> getLastMockId(int userId);

    Long countByPaperBaseIdAndStatusAndAnswerCardType(long id, int status, int answerCardType);

    List<EssayPaperAnswer> findByUserIdAndTypeAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtCreateDesc(int userId, int type, int status, int bizStatus, int answerCardType);

    List<EssayPaperAnswer> findByUserIdAndPaperBaseIdAndTypeAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(int userId, long paperId, int type, int status, int answerCardType);

    //自定义返回类型
    @Query(value = "select new com.huatu.tiku.essay.vo.admin.PaperAnswerStatisVO(max(eqa.examScore),min(eqa.examScore) ,avg(eqa.examScore) ,count(eqa)) from EssayPaperAnswer eqa where eqa.paperBaseId=?1 and eqa.status=1 and eqa.bizStatus=3 ")
    List<PaperAnswerStatisVO> findStatisData(long paperBaseId);

    List<EssayPaperAnswer> findByUserIdAndPaperBaseIdAndStatusAndTypeAndAnswerCardTypeOrderByGmtCreateDesc(int userId, long paperId, int status, int type, int answerCardType);

    @Transactional
    @Modifying
    @Query(value = "update v_essay_paper_answer set status = 2 where id=?1", nativeQuery = true)
    int updateToRecycle(long answerId);


    Long countByPaperBaseIdAndStatusAndBizStatusAndAnswerCardType(long paperId, int status, int bizStatus, int answerCardType);


    Long countByPaperBaseIdAndTypeAndStatusAndBizStatusAndAnswerCardType(long paperId, int type, int status, int bizStatus, int answerCardType);


    //未批改完成的试卷答题卡
    @Query("select pa from  EssayPaperAnswer pa  where pa.type = 1 and pa.bizStatus =2  and pa.status = 1 and pa.gmtModify > ?1 and pa.gmtModify < ?2 and pa.correctMode=1")
    List<EssayPaperAnswer> findUnfinishedCard(Date fiveMinutesBefore, Date tenMinutesBefore);

    //查找用户上次该套题答题记录
    @Query(nativeQuery = true, value = "select pa.* from  v_essay_paper_answer pa  where pa.paper_base_id = ?1 and pa.user_id = ?2 and pa.type = 1 and pa.biz_status =3  and pa.status = 1 and pa.correct_date < ?3 order by pa.correct_date desc limit 0,1")
    List<EssayPaperAnswer> findLastCard(long paperId, int userId, Date CorrectDate);


//    List<EssayPaperAnswer> findByPaperBaseIdAndType(long paperId, int type);

    /**
     * 查询用户指定试卷的批改记录列表
     *
     * @param userId
     * @param paperId
     * @param status
     * @param truePaper
     * @param bizStatusList
     * @return
     */
    List<EssayPaperAnswer> findByUserIdAndPaperBaseIdAndStatusAndTypeAndBizStatusInAndAnswerCardTypeOrderByGmtModifyDesc(int userId, long paperId,
                                                                                                                         int status, int truePaper, LinkedList<Integer> bizStatusList,
                                                                                                                         int answerCardType);

    List<EssayPaperAnswer> findByUserIdAndPaperBaseIdAndTypeAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtModifyDesc(int userId, long paperBaseId, int truePaper, int status, int bizStatus,
                                                                                                                       int answerCardType);

    EssayPaperAnswer findByIdAndStatus(long answerId, int status);

    @Query(value = "select new com.huatu.tiku.essay.vo.admin.answer.AdminPaperAnswerCountVO(paperAnswer.correctMode,count(paperAnswer.correctMode))from EssayPaperAnswer paperAnswer where paperAnswer.status in (1,2) and paperAnswer.paperBaseId=?1 and paperAnswer.answerCardType = ?2 and paperAnswer.bizStatus=3 group by paperAnswer.correctMode")
    List<AdminPaperAnswerCountVO> getPaperCorrectCountInfo(long paperId, int answerCardType);

    /**
     * 查询指定试卷的某种答题卡记录列表
     *
     * @param userId
     * @param paperId
     * @param correctMode
     * @param type
     * @param status
     * @return
     */
    List<EssayPaperAnswer> findByUserIdAndPaperBaseIdAndCorrectModeAndTypeAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(int userId,
                                                                                                                         long paperId, Integer correctMode, int type, int status, int answerCardType);

    List<EssayPaperAnswer> findByIdIn(List<Long> ids);

    /**
     * @param userId
     * @param paperBaseId
     * @param truePaper
     * @param status
     * @param bizStatus
     * @return
     */
    long countByUserIdAndPaperBaseIdAndTypeAndCorrectModeAndStatusAndBizStatusAndAnswerCardType(int userId,
                                                                                                Long paperBaseId, int truePaper, int correctMode, int status, int bizStatus, int answerCardType);


    List<EssayPaperAnswer> findByCorrectModeIn(List<Integer> correctModes);

    //根据试卷id查询所有训练量
    Long countByPaperBaseIdAndStatus(Long paperId,Integer status);


}
