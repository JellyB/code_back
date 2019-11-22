package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huangqingpeng
 * @title: EssayQuestionPdfServiceT
 * @description: TODO
 * @date 2019-08-0816:59
 */
public class EssayQuestionPdfServiceT extends TikuBaseTest {

    @Autowired
    EssayQuestionPdfService essayQuestionPdfService;


    @Test
    public void test(){
        long answerId = 1775425;
        essayQuestionPdfService.getSingleCorrectPdfPath(answerId);
    }

    @Test
    public void test1(){
        long answerId = 0L;
        essayQuestionPdfService.getMultiCorrectPdfPath(answerId);
    }
}
