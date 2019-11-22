package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.PracticeRemark;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by x6 on 2018/4/11.
 */
public interface PracticeRemarkRepository extends JpaRepository<PracticeRemark,Long> {

    List<PracticeRemark> findByPracticeContentId(long typeId,Sort sort);
}
