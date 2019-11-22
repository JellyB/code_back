package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.edu.EssayEduPaperVO;
import com.huatu.tiku.essay.vo.resp.EssayAnswerVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.vo.resp.PaperCommitVO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by x6 on 2017/11/23.
 */
public interface EssayEduService {

    ModelAndView photo();

    List<EssayEduPaperVO> findPaperListByArea(long areaId, int userId, Pageable pageable, EssayAnswerCardEnum.ModeTypeEnum normal);

    List<EssayAnswerVO> paperCorrectList(int userId, Pageable pageRequest, EssayAnswerCardEnum.ModeTypeEnum normal);

    long countPaperCorrectList(int userId, EssayAnswerCardEnum.ModeTypeEnum normal);

    Long createAnswerCard(int userId, long paperId, int terminal, EssayAnswerCardEnum.ModeTypeEnum normal);

    boolean paperCommit(int userId, PaperCommitVO paperCommitVO, int terminal, String cv);

    List<EssayQuestionVO> answerDetail(int userId, long answerId, int terminal, String cv);

    List<EssayEduPaperVO> findPaperAllListByArea(long areaId, int userId, EssayAnswerCardEnum.ModeTypeEnum normal);

    /**
     * 查看指定试卷的用户批改记录
     * @param userId
     * @param paperId
     * @param normal
     * @return
     */
	List<EssayAnswerVO> paperCorrectList(int userId, long paperId, EssayAnswerCardEnum.ModeTypeEnum normal);

    PageUtil<HashMap> findUserMetas(long paperId, PageRequest pageable);
}
