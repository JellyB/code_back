package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayPaperBase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by x6 on 2017/11/24.
 */
public interface EssayPaperBaseRepository extends JpaRepository<EssayPaperBase, Long>, JpaSpecificationExecutor<EssayPaperBase> {
    List<EssayPaperBase> findByStatusAndBizStatusAndAreaIdIn(int status, int bizStatus, List<Long> areaIds, Pageable pageable);

    Long countByStatusAndBizStatusAndAreaIdIn(int status, int bizStatus, List<Long> areaIds);

    List<EssayPaperBase> findByIdIn(List<Long> paperIds);

    List<EssayPaperBase> findByStatusNotAndNameLike(int status, String name);

    List<EssayPaperBase> findByStatusNotAndNameAndIdNot(int status, String name, Long id);

    List<EssayPaperBase> findByStatusNotAndAreaIdAndNameAndPaperYear(int status, long areaId, String name, String paperYear, Pageable pageable);

    @Transactional
    @Modifying
    @Query("update EssayPaperBase pb set pb.bizStatus=?1 , pb.status=?2 where pb.id=?3 and pb.bizStatus in ?4 and pb.status in ?5")
    int modifyPaperStatus(int bizStatus, int status, long paperId, List<Integer> oldBizStatus, List<Integer> oldStatus);

    @Transactional
    @Modifying
    @Query("update EssayPaperBase pb set pb.status=-1 where pb.id=?1 and (pb.bizStatus <> 1 or pb.status <> 4) ")
    int modifyPaperToDelete(long paperId);

    @Transactional
    @Modifying
    @Query("update EssayPaperBase pb set  pb.bizStatus=?1 ,pb.status=?2 where pb.id=?3 ")
    int modifyPaperToOffline(int bizStatus, int status, long paperId);

    @Transactional
    @Modifying
    @Query("update EssayPaperBase qb set qb.downloadCount = qb.downloadCount +1 where qb.id=?1")
    int updateDownloadCount(long paperId);


    EssayPaperBase findByIdAndBizStatus(long paperId, int bizStatus);

    List<EssayPaperBase> findByStatusNotOrderByIdAsc(int status);

    List<EssayPaperBase> findByAreaIdAndStatusNotOrderByGmtCreateAsc(long areaId, int status);

    List<EssayPaperBase> findByStatusAndBizStatusAndType(int status, int bizStatus, int type);

    @Transactional
    @Modifying
    @Query("update EssayPaperBase qb set qb.videoAnalyzeFlag = ?2 where qb.id=?1")
    int updateVideoFlag(long paperId, boolean flag);

    List<EssayPaperBase> findByAreaIdAndBizStatusAndStatus(long areaId, int status, int bizStatus);

    /**
     * 修改真题卷状态
     *
     * @param status
     * @param paperId
     * @return
     */
    @Transactional
    @Modifying
    @Query("update EssayPaperBase qb set qb.status = ?1 where qb.id=?2")
    int updateStatusById(int status, Long paperId);

    /**
     * 查询课后练习试卷
     */
    EssayPaperBase findByIdAndBizStatusAndStatus(long paperId, int bizStatus, int status);

}
