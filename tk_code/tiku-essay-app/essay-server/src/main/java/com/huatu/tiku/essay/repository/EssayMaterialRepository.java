package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EssayMaterialRepository extends JpaRepository<EssayMaterial, Long> {
    List<EssayMaterial> findByPaperIdOrderBySortAsc(long paperId);

    EssayMaterial findByPaperIdAndSort(long questionId, int sort);

    List<EssayMaterial> findByPaperIdAndBizStatusAndStatusOrderBySortAsc(long paperId, int bizStatus, int status);
    @Transactional
    @Modifying
    @Query("update EssayMaterial em set em.status= -1  where em.paperId = ?1")
    int modifyByPaperId( long paperId);
//    int upToDeleteByPaperId( long paperId);

    List<EssayMaterial> findByPaperId(long paperId);

    @Query("select em.id from EssayMaterial em where em.paperId = ?1 and em.status = ?2")
    List<Long> findIdByPaperIdAndStatus(long paperId, int status);

    @Transactional
    @Modifying
    @Query("update EssayMaterial em set em.status= -1  where em.id in ?1")
    int deleteByList(List<Long> deleteIdList);



    @Query("select em.id from  EssayMaterial em where em.paperId = ?1")
    List<Long> findIdByPaperId(long paperId);

    List<EssayMaterial> findByPaperIdAndStatusOrderBySortAsc(Long paperId, int status);

    /**
     * 将真题卷中的材料信息绑定到模考卷中
     * @param paperId
     * @param mockId
     * @return
     */
    @Transactional
    @Modifying
    @Query("update EssayMaterial em set em.bizStatus = 1 ,em.paperId= ?2  where em.paperId = ?1")
	int updatePaper2Mock(Long paperId, Long mockId);


//    List<EssayMaterial> findByPaperIdInAndStatus(List<Long> paperIdList, int status);





}

