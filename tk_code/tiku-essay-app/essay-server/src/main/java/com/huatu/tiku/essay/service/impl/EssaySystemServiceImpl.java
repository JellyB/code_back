package com.huatu.tiku.essay.service.impl;

import com.huatu.tiku.essay.constant.status.EssayIconConstant;
import com.huatu.tiku.essay.vo.system.EssaySystemVO;
import com.huatu.tiku.essay.repository.EssayIconRepository;
import com.huatu.tiku.essay.service.EssaySystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by x6 on 2017/12/16.
 */
//@Transactional
@Service
@Slf4j
public class EssaySystemServiceImpl implements EssaySystemService {


    @Value("${photoAnswerMsg}")
    private String photoAnswerMsg;
    @Autowired
    private EssayIconRepository essayIconRepository;
    @Override
    public EssaySystemVO photoAnswerMessage() {

        return EssaySystemVO.builder()
                .photoAnswerMsg(photoAnswerMsg)
                .build();
    }

    @Override
    public Object iconList() {

        return essayIconRepository.findByStatus(EssayIconConstant.EssayIconStatusEnum.NORMAL.getStatus());

    }
}
