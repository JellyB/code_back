package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.MockPractice;
import com.huatu.tiku.interview.entity.po.PaperPractice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by x6 on 2018/4/12.
 */
public interface MockPracticeRepository extends JpaRepository<MockPractice, Long> {


    List<MockPractice> findByOpenIdAndAnswerDateAndStatus(String openId, String answerDate, int status);


    List<MockPractice> findByStatusOrderByAnswerDateAsc(int i);
}
