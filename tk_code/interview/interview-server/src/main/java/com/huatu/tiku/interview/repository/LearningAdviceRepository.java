package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.LearningAdvice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by x6 on 2018/1/17.
 */
public interface LearningAdviceRepository extends JpaRepository<LearningAdvice, Long> {

    List<LearningAdvice> findByTypeAndLevel(int type, int level);
}
