package com.huatu.tiku.position.biz.respository;

import com.huatu.tiku.position.base.repository.BaseRepository;
import com.huatu.tiku.position.biz.domain.ScoreLine;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wangjian
 **/
public interface ScoreLineRepository extends BaseRepository<ScoreLine,Long>{

//    @Query(nativeQuery = true,value = "SELECT sc.* FROM score_line sc ,position p where p.id=?1 and p.name_str=sc.name_str and p.company=sc.company and p.department_id=sc.department_id")
//    @Query(nativeQuery = true,value = "SELECT\tsc.* FROM\tscore_line sc LEFT JOIN department d1 on d1.id =sc.department_id,\tposition p LEFT JOIN department d2 on d2.id =p.department_id WHERE p.id=?1 AND p.name_str = sc.name_str AND p.company = sc.company AND d1.`name`=d2.`name`\n")
    @Query(nativeQuery = true,value = "SELECT\tsc.* FROM\tscore_line sc LEFT JOIN department d1 on d1.id =sc.department_id,\tposition p LEFT JOIN department d2 on d2.id =p.department_id WHERE p.id=?1 AND p.name_str = sc.name_str  AND d1.`name`=d2.`name`\n")
    List<ScoreLine> findByPositionId(Long id);
}
