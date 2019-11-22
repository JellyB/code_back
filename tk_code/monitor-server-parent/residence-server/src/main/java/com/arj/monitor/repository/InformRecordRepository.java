package com.arj.monitor.repository;

import com.arj.monitor.entity.InformRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2019-01-02 下午9:15
 **/
@RepositoryRestResource(collectionResourceRel = "informRecord",path = "informRecord")
public interface InformRecordRepository extends JpaRepository<InformRecord, Long>, JpaSpecificationExecutor<InformRecord> {
   List<InformRecord> findAllByBizStatus(int bizStatus);

    /**
     *
     * @param minute
     * @return
     */
    @Query(value="select count(id) from InformRecord where MINUTE = ?1 AND bizStaus=0", nativeQuery = true)
    long countIdByServerInfoIdAndMinute(long serverInfoId, int minute);


}
