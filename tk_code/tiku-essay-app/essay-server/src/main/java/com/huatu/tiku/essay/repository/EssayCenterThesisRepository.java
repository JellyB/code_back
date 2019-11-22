package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayCenterThesis;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Created by x6 on 2017/12/14.
 */
public interface EssayCenterThesisRepository   extends JpaRepository<EssayCenterThesis, Long>,JpaSpecificationExecutor<EssayCenterThesisRepository> {
    List<EssayCenterThesis> findByBizStatusIsNot(int bizStatus, Pageable pageable);

    long  countByBizStatusIsNot(int bizStatus);







}
