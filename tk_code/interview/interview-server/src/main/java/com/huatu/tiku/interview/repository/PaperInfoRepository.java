package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.PaperInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by x6 on 2018/4/11.
 */

public interface PaperInfoRepository extends JpaRepository<PaperInfo, Long> {
    List<PaperInfo> findByExamTypeAndStatus(int examType, int status);
}
