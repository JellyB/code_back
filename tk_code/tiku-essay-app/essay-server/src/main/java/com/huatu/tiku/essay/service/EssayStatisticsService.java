package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssaySimilarQuestionGroupVO;
import com.huatu.tiku.essay.util.PageUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

public interface EssayStatisticsService {

    PageUtil<List<EssaySimilarQuestionGroupVO>> findAllGroup(Pageable pageable, String title, int type);


    long countCorrectSum(int type,int paperType);


    Object findAllPaper(Pageable pageable, String title, String year,int areaId,int type);

    Object findBySingleGroupId(Long id, EssayAnswerCardEnum.ModeTypeEnum normal);

    Object findBySingleGroupIdAndPage(Pageable pageable, Long areaId,Long questionId);

    Object findByPaperId(Long id, EssayAnswerCardEnum.ModeTypeEnum normal);

    ModelAndView getPageExcel(Long pageId, EssayAnswerCardEnum.ModeTypeEnum normal);

    ModelAndView getQuestionExcel(Long groupId, EssayAnswerCardEnum.ModeTypeEnum normal);


}
