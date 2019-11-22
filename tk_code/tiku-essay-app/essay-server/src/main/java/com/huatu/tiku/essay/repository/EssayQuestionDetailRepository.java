package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by huangqp on 2017\11\22 0022.
 */
public interface EssayQuestionDetailRepository extends JpaRepository<EssayQuestionDetail, Long>, JpaSpecificationExecutor<EssayQuestionDetail> {

    //   List<EssayQuestionDetail> findByBizStatusAndStatusAndType(int bizStatus, int status, int type, Pageable pageRequest);


    List<EssayQuestionDetail> findByStatusAndType(int status, int type);

    //   long countByTypeAndBizStatusAndStatus(int type,int bizStatus, int status);


    List<EssayQuestionDetail> findByIdIn(List<Long> detailIds);

    List<EssayQuestionDetail> findByStatusNot(int status);

    EssayQuestionDetail findById(long detailId);

    List<EssayQuestionDetail> findByStatus(int status);

    @Query("select eqd.id from  EssayQuestionDetail eqd where eqd.type = ?1 and trim(eqd.stem) like  ?2")
    List<Long> findIdByTypeAndStem(int type, String stem);
//    List<EssayPaperDetail> findEssayPaperByEssayQuestionDetail(long id);

//    @Transactional
//    @Query("SELECT ep FROM EssayQuestionDetail eq JOIN eq.essayPaper ep")
//    List<EssayPaperDetail> findPaperList(long id);

    EssayQuestionDetail findByIdAndStatus(long detailId, int status);
}
