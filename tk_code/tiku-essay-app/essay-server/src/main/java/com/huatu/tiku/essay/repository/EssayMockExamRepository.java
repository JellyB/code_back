package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by x6 on 2017/12/28.
 */
public interface EssayMockExamRepository extends JpaRepository<EssayMockExam, Long>, JpaSpecificationExecutor<EssayMockExam> {

    @Transactional
    @Modifying
    @Query("update EssayMockExam em set em.bizStatus = ?1,em.practiceId = ?2  where em.id = ?3 and em.bizStatus <> 3 and em.bizStatus <> 4 and em.status <> -1")
    int updateMockStatus(int status, long practiceId, long id);

    List<EssayMockExam> findByBizStatusAndTag(int bizStatus, int tag, Pageable pageRequest);

    List<EssayMockExam> findByBizStatus(int bizStatus, Pageable pageRequest);

    //查询当前进行的模考
    @Query("select em from  EssayMockExam em where em.status = 1 and em.bizStatus = 2 or em.bizStatus =3 order by em.startTime desc")
    List<EssayMockExam> findCurrent();

    @Modifying
    @Query("select em from  EssayMockExam em where em.practiceId = ?1 and em.status <> -1")
    List<EssayMockExam> findByPracticeId(long practiceId);

    int countByBizStatus(int bizStatus);

    List<EssayMockExam> findByBizStatusAndStatusOrderByEndTimeDesc(int bizStatus, int status);


    //查询当前进行的模考
    @Query("select em from  EssayMockExam em where em.status = 1 and em.bizStatus = 2 or em.bizStatus =3  or em.bizStatus = 4 and em.startTime > ?1 order by em.startTime asc")
    List<EssayMockExam> findCurrentEssayMock(Date date);

    List<EssayMockExam> findByStatusAndBizStatusInAndStartTimeGreaterThanOrderByStartTime(int status,List<Integer> bizStatus,Date startTime);


    List<EssayMockExam> findByStatus(int status);


}
