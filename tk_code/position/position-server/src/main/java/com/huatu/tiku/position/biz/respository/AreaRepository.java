package com.huatu.tiku.position.biz.respository;

import com.huatu.tiku.position.base.repository.BaseRepository;
import com.huatu.tiku.position.biz.domain.Area;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author wangjian
 **/
public interface AreaRepository extends BaseRepository<Area,Long>{

    List<Area> findByParentIdIn(List<Long> ids);

    @Query("select id from Area where parentId=?1")
    List<Long> findIdsByParentIdIn(Long id);

    List<Area> findByType(Integer type);

    List<Area> findByParentId(Long parentId);

    List<Area> findByName(String name);

}
