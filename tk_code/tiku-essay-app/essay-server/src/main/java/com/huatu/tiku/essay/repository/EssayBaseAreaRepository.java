package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayBaseArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/17
 */
public interface EssayBaseAreaRepository extends JpaRepository<EssayBaseArea, Long>, JpaSpecificationExecutor<EssayBaseArea> {

    List<EssayBaseArea> findByParentId(Long parentId);

}
