package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayLabelTotal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by x6 on 2018/7/6.
 */
public interface EssayLabelTotalRepository extends JpaRepository<EssayLabelTotal, Long>, JpaSpecificationExecutor<EssayLabelTotal> {
    /**
     * 已结束的批注个数
     *
     * @param id
     * @param status
     * @param bizStatus
     * @return
     */
    List<EssayLabelTotal> findByAnswerIdAndStatusAndBizStatusIsNotOrderByGmtModifyAsc(long id, int status, int bizStatus);

    /**
     * 已结束&&进行中的批注个数
     *
     * @param answerId
     * @param status
     * @return
     */
    List<EssayLabelTotal> findByAnswerIdAndStatus(long answerId, int status);

    /**
     * 将创建超过两小时且未完成的批注关闭
     *
     * @return
     */
    @Transactional
    @Modifying
    @Query("update EssayLabelTotal t set t.status= -1  where t.gmtCreate <= ?1 and t.status = 1 and t.bizStatus <> 1 and t.labelFlag <>2")
    int updateToClose(Date date);

    List<EssayLabelTotal> findByStatus(int status);

    List<EssayLabelTotal> findByCreatorAndStatusAndBizStatus(String admin, int status, int bizStatus);

    @Transactional
    @Modifying
    @Query("update EssayLabelTotal t set t.status= -1  where t.id = ?1 and t.bizStatus <> 1 and t.labelFlag <>2")
    int updateToDelById(long id);

    @Transactional
    @Modifying
    @Query("update EssayLabelTotal t set t.labeledContent= ?1  where t.id = ?2")
    int updateLabeledContentById(String labeledContent, Long totalId);

    List<EssayLabelTotal> findByAnswerIdAndStatusOrderByGmtModifyAsc(long id, int status);

    List<EssayLabelTotal> findByStatusAndBizStatusAndLabelFlag(Pageable pageable ,int status, int bizStatus, int labelFlag);

    /**
     * 查询指定时间内有效批注
     */
    List<EssayLabelTotal> findByStatusAndBizStatusIsNotAndGmtCreateBetween(int status, int bizStatus, Date start, Date end);

    @Query("select t from  EssayLabelTotal t  where  t.id > ?1  and t.status = 1 and t.bizStatus = 1 and t.isFinal = 1")
    List<EssayLabelTotal> findLabelXml(Long labelId);


    /**
     * 查询指定题目的某一时间段内有效批注
     */
    List<EssayLabelTotal> findByQuestionIdAndIsFinalAndStatusIsNotAndBizStatusAndGmtCreateBetween(long questionId, int isFinal, int status, int bizStatus, Date start, Date end);


    List<EssayLabelTotal> findByAnswerIdInAndStatusAndBizStatusIsNotOrderByGmtModifyAsc(List<Long> answerIdList, int status, int bizStatus);


    List<EssayLabelTotal> findByAnswerIdInAndStatusAndLabelFlagOrderByGmtCreate(List<Long> answerIdList, int status, int labelFlag);


    List<EssayLabelTotal> findByAnswerIdAndStatusAndLabelFlag(long answerId, int status, int labelFlag);

    @Transactional
    @Modifying
    @Query("update EssayLabelTotal t set t.labelFlag= ?3  where t.answerId = ?1 and  t.status=?2")
    int updateLabelFlag(long answerId, int status, int labelFlag);
}
