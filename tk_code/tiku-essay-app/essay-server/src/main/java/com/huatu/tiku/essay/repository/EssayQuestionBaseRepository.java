package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by huangqp on 2017\11\22 0022.
 */
public interface EssayQuestionBaseRepository extends JpaRepository<EssayQuestionBase, Long>, JpaSpecificationExecutor<EssayQuestionBaseRepository> {

    int countByPaperId(long paperBaseId);

    @Query("select qb from  EssayQuestionBase qb  where qb.id  in ?1 order by qb.areaSort asc")
    LinkedList<EssayQuestionBase> findList(List<Long> similarIds);

    /**
     * 根据试卷ID获取试题并排序
     * @param paperId
     * @param bizStatus
     * @param status
     * @return
     */
    List<EssayQuestionBase> findByPaperIdAndBizStatusAndStatusOrderBySortAsc(long paperId,int bizStatus, int status);

    List<EssayQuestionBase> findByPaperIdInAndStatusNot(List<Long> ids, int status);

    List<EssayQuestionBase> findByPaperIdAndStatusNot(long paperId, int status);

    List<EssayQuestionBase> findByPaperIdAndStatusOrderBySortAsc(long paperId, int status);

    @Transactional
    @Modifying
    @Query("update EssayQuestionBase qb set qb.paperId=0,qb.status=-1  where qb.id=?1")
    int deleteQuestionBase(long questionId);

    /**
     * 代码优化 走索引
     *
     * @param paperId
     * @param bizStatus
     * @param status
     * @return
     */
    long countByPaperIdAndBizStatusAndStatus(long paperId, int bizStatus, int status);

    @Transactional
    @Modifying
    @Query("update EssayQuestionBase qb set qb.bizStatus = ?2  where qb.paperId =?1")
    int modifyStatusByPaperId(long paperId, int bizStatus);

    @Transactional
    @Modifying
    @Query("update EssayQuestionBase qb set qb.downloadCount = qb.downloadCount +1 where qb.id=?1")
    int updateDownloadCount(long questionId);

    List<EssayQuestionBase> findByPaperIdAndStatus(long paperId, Sort sort, int status);

    @Transactional
    @Modifying
    @Query("update EssayQuestionBase qb set qb.status = -1 where qb.paperId=?1")
    int modifyQuestionToDeleteByPaperId(long paperId);

    @Query("select qb.id from  EssayQuestionBase qb where qb.paperId=?1")
    List<Long> findQuestionBaseIdByPaperId(long paperId);


    List<EssayQuestionBase> findByStatus(int status);

    @Transactional
    @Modifying
    @Query("update EssayQuestionBase qb set qb.status = -1 where qb.id=?1")
    int updateToDel(long questionId);

    @Transactional
    @Modifying
    @Query("update EssayQuestionBase qb set qb.videoId = ?1 where qb.id=?2")
    int upVideoIdById(Integer videoId, Long questionId);

    @Query("select t.id from  EssayQuestionBase t where t.detailId in ?1 and t.status = 1")
    List<Long> findByDetailIdIn(List<Long> detailIds);

    /**
     * 查询
     * @param mockId
     * @return
     */
	EssayQuestionBase findByPaperId(Long mockId);
	
	/**
	 * 删除指定试卷中的试题信息
	 * @param paperId
	 * @return
	 */
	@Transactional
    @Modifying
    @Query("update EssayQuestionBase qb set qb.status = -1 where qb.paperId=?1")
    int updateStatus(long paperId);

	/**
	 * 修改试题绑定关系（由真题卷绑定到模考卷中）
	 * @param paperId
	 * @param mockId
	 * @return
	 */
	@Transactional
    @Modifying
    @Query("update EssayQuestionBase qb set qb.status = 1 , qb.bizStatus = 1 , qb.paperId = ?2 where qb.paperId=?1")
	int updatePaper2Mock(Long paperId, Long mockId);


	EssayQuestionBase findByIdAndStatus(Long questionId,Integer status);
}
