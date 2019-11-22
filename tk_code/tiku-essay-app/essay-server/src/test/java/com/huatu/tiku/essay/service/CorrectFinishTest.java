package com.huatu.tiku.essay.service;

import com.google.gson.Gson;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.mq.listeners.ManualCorrectFinishListener;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import com.huatu.tiku.essay.util.file.Label2AppUtil;
import com.huatu.tiku.essay.vo.file.TagPosition;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author huangqingpeng
 * @title: CorrectFinishTest
 * @description: TODO
 * @date 2019-09-2611:25
 */
public class CorrectFinishTest extends TikuBaseTest {

    @Autowired
    private ManualCorrectFinishListener manualCorrectFinishListener;

    @Autowired
    private EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Test
    public void test(){
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findByIdAndStatus(1867491L, EssayStatusEnum.NORMAL.getCode());
        manualCorrectFinishListener.assemblingManualCorrectCommentContent(questionAnswer);
    }

}
