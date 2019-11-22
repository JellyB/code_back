package com.huatu.tiku.position.biz.respository;

import com.huatu.tiku.position.base.repository.BaseRepository;
import com.huatu.tiku.position.biz.domain.RecommendReccord;

import java.util.List;

/**推荐报告浏览记录
 * @author wangjian
 **/
public interface RecommendReccordRepository extends BaseRepository<RecommendReccord,Long> {
    List<RecommendReccord> findByUserId(Long id);
}
