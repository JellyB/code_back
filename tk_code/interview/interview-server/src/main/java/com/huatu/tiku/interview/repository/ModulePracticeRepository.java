package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.ModulePractice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by x6 on 2018/4/12.
 */
public interface ModulePracticeRepository  extends JpaRepository<ModulePractice, Long> {

    List<ModulePractice> findByOpenIdAndAnswerDateAndStatus(String openId, String answerDate, int status);


    List<ModulePractice> findByPracticeContentAndStatusOrderByAnswerDateAsc(long type,int status);
}
