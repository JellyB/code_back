package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.PracticeContentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by x6 on 2018/4/11.
 */
public interface PracticeContentTypeRepository extends JpaRepository<PracticeContentType,Long> {


    List<PracticeContentType> findByStatusOrderBySortAsc(int status);

    List<PracticeContentType> findByStatusOrderByPidAsc(int status);
}
