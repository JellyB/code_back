package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayUserPaperCollection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/1 18:57
 * @Description
 */
@Repository
public interface EssayUserPaperCollectionRepository extends JpaRepository<EssayUserPaperCollection,Long> {
    List<EssayUserPaperCollection> findByUserIdAndPaperBaseIdAndBizStatusAndStatus(Integer userId,Long paperBaseId,Integer bizStatus,Integer status);

    List<EssayUserPaperCollection> findByUserIdAndPaperBaseId(Integer userId,Long paperBaseId);

    @Transactional
    @Modifying
    @Query("update EssayUserPaperCollection euc set euc.status= -1  where  euc.id = ?1")
    int upToDelete(long id);

    List<EssayUserPaperCollection> findByUserIdAndBizStatusAndStatus(Integer userId, Integer bizStatus,Integer status,Pageable pageRequest);


    long countByUserIdAndBizStatusAndStatus(Integer userId, Integer bizStatus,Integer status);

}
