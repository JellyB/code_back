package com.huatu.tiku.essay.repository.v2;

import com.huatu.tiku.essay.entity.correct.LabelCommentRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EssayLabelCommentRelationRepository extends JpaRepository<LabelCommentRelation, Long>, JpaSpecificationExecutor<LabelCommentRelation> {


    List<LabelCommentRelation> findByLabelIdAndTypeAndStatus(long labelId, int type, int status);


    @Transactional
    @Modifying
    @Query("update LabelCommentRelation qm set qm.status= ?3  where  qm.labelId = ?1 and type = ?2")
    int updateStatusByLabelId(long labelId, int type, int status);

    List<LabelCommentRelation> findByStatusAndTypeAndLabelIdIn(int status, int type, List<Long> labelIds);

}
