package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayUserQuestionCollection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by x6 on 2018/1/31.
 */
@Repository
public interface EssayUserQuestionCollectionRepository extends JpaRepository<EssayUserQuestionCollection,Long> {

    @Transactional
    @Modifying
    @Query("update EssayUserQuestionCollection euc set euc.status= -1  where  euc.id = ?1")
    int upToDelete(long id);

    List<EssayUserQuestionCollection> findByUserIdAndSimilarIdAndQuestionBaseIdAndBizStatusAndStatus(Integer userId,Long similarId,Long questionBaseId,Integer bizStatus,Integer status);

    List<EssayUserQuestionCollection> findByUserIdAndSimilarIdAndQuestionBaseId(Integer userId,Long similarId,Long questionBaseId);

    List<EssayUserQuestionCollection> findByUserIdAndQuestionTypeAndBizStatusAndStatus(Integer userId,int questionType,Integer bizStatus,Integer status,Pageable pageRequest);

    long countByUserIdAndQuestionTypeAndBizStatusAndStatus(int userId, int questionType,int bizStatus, int status);

    List<EssayUserQuestionCollection> findByUserIdAndQuestionTypeNotAndBizStatusAndStatus(Integer userId,int questionType,Integer bizStatus,Integer status,Pageable pageRequest);

    long countByUserIdAndQuestionTypeNotAndBizStatusAndStatus(int userId, int questionType,int bizStatus, int status);

    List<EssayUserQuestionCollection> findByUserIdAndBizStatusAndStatus(int userId, int bizStatus, int status, Pageable pageRequest);

    long countByUserIdAndBizStatusAndStatus(int userId, int bizStatus, int status);
}
