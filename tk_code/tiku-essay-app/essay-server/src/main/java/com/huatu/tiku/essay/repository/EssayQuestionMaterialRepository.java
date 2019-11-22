package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayQuestionMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by x6 on 2017/11/26.
 */
public interface EssayQuestionMaterialRepository extends JpaRepository<EssayQuestionMaterial, Long> {

    List<EssayQuestionMaterial> findByQuestionBaseIdAndStatusAndBizStatusOrderBySortAsc(long id,int status,int bizStatus);

   // List<EssayQuestionMaterial> findByBizStatusAndStatusAndQuestionBaseIdInOrderBySortAsc(int bizStatus,int status, List<Long> questionIds);

    List<EssayQuestionMaterial> findByQuestionBaseId(long questionId);
    @Transactional
    @Modifying
    @Query("update EssayQuestionMaterial qm set qm.status= -1  where qm.questionBaseId = ?1")
    int upToDeleteByQuestionId( long questionId);

    List<EssayQuestionMaterial> findByQuestionBaseIdAndMaterialId(long questionId, long materialId);



    @Query("select qm.materialId from  EssayQuestionMaterial qm  where qm.questionBaseId = ?1 and qm.bizStatus = ?2 and qm.status =?3")
    List<Long> findMaterialIdByQuestionBaseIdAndStatusAndBizStatusOrderBySortAsc(long questionId, int bizStatus, int status);

    List<EssayQuestionMaterial> findByQuestionBaseIdAndStatus(long questionBaseId, int status);

    @Transactional
    @Modifying
    @Query("update EssayQuestionMaterial qm set qm.status= -1  where  qm.questionBaseId = ?1 and qm.materialId in ?2 ")
    int deleteByQuestionIdAndMaterialIdList(long questionBaseId,List<Long> deleteIdList);


    @Transactional
    @Modifying
    @Query("update EssayQuestionMaterial qm set qm.status= -1  where qm.materialId in ?1 ")
    int deleteByMaterialIdList(List<Long> deleteIdList);


    @Query("select em.questionBaseId from  EssayQuestionMaterial em where em.materialId in ?1")
    List<Long>  findQuestionBaseIdByMaterialIdIn(List<Long> materialIdList);
}
