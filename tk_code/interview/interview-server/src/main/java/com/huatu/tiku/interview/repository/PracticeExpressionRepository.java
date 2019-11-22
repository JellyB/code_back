package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.PracticeExpression;
import com.huatu.tiku.interview.entity.po.RemarkWord;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by x6 on 2018/4/11.
 */
public interface PracticeExpressionRepository extends JpaRepository<PracticeExpression,Long> {
    List<RemarkWord> findByStatus(int status, Sort sort);
}
