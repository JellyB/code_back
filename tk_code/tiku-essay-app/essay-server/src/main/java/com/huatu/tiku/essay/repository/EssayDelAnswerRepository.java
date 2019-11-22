package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayDelAnswer;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * Created by x6 on 2018/7/6.
 */
public interface EssayDelAnswerRepository extends JpaRepository<EssayDelAnswer, Long> , JpaSpecificationExecutor<EssayDelAnswer> {

}
