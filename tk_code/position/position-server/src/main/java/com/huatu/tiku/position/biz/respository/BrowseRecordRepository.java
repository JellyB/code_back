package com.huatu.tiku.position.biz.respository;

import com.huatu.tiku.position.base.repository.BaseRepository;
import com.huatu.tiku.position.biz.domain.BrowseRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wangjian
 **/
public interface BrowseRecordRepository extends BaseRepository<BrowseRecord,Long> {

    @Modifying
    @Transactional
    @Query(value = "update BrowseRecord br set br.accordFlag=?3 where br.userId=?1 and br.positionId=?2")
    int addPositionRemark(Long userId, Long positionId, Boolean accordFlag);

    BrowseRecord findByUserIdAndPositionId(Long userId, Long positionId);

    @Modifying
    @Transactional
    @Query(value = "update BrowseRecord br set br.collectionFlag=?3 where br.userId=?1 and br.positionId=?2")
    int addPositionCollection(Long userId, Long positionId, Boolean flag);

    /**
     * 统计浏览数
     *
     * @param positionId 职位ID
     * @return 总数
     */
    Integer countByPositionId(Long positionId);
}
