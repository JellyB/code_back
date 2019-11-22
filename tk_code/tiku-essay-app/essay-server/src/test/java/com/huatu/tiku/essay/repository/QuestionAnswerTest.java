package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.constant.status.EssayLabelStatusConstant;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.correct.EssayPaperLabelTotal;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.repository.v2.EssayPaperLabelTotalRepository;
import com.huatu.tiku.essay.service.EssayQuestionPdfService;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * @author huangqingpeng
 * @title: QuestionAnswerTest
 * @description: TODO
 * @date 2019-08-0615:32
 */
public class QuestionAnswerTest extends TikuBaseTest {

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    EssayQuestionPdfService essayQuestionPdfService;

    @Autowired
    EssayLabelTotalRepository essayLabelTotalRepository;

    @Autowired
    EssayPaperLabelTotalRepository essayPaperLabelTotalRepository;
    @Test
    public void test(){
        EssayQuestionAnswer one = essayQuestionAnswerRepository.findOne(1L);
        one.setId(0);
        one.setCorrectMode(null);
        one.setStatus(-1);
        essayQuestionAnswerRepository.save(one);
    }

    @Test
    public void test2(){
        String singleCorrectPdfPath = essayQuestionPdfService.getSingleCorrectPdfPath(1767597);
        System.out.println("singleCorrectPdfPath = " + singleCorrectPdfPath);
    }

    @Test
    public void test3(){
        Pageable pageable = new PageRequest(0,50, Sort.Direction.ASC,"id");
        List<EssayLabelTotal> labelTotals = essayLabelTotalRepository.findByStatusAndBizStatusAndLabelFlag(pageable, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(),
                EssayLabelStatusConstant.EssayLabelBizStatusEnum.ONLINE.getBizStatus(),
                LabelFlagEnum.TEACHING_AND_RESEARCH.getCode());
        System.out.println("labelTotals = " + labelTotals);
        List<EssayPaperLabelTotal> paperLabelTotals = essayPaperLabelTotalRepository.findByStatusAndBizStatusAndLabelFlag(
                pageable,
                EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(),
                EssayLabelStatusConstant.EssayLabelBizStatusEnum.ONLINE.getBizStatus(),
                LabelFlagEnum.TEACHING_AND_RESEARCH.getCode());
        System.out.println("paperLabelTotals = " + paperLabelTotals);
    }
}
