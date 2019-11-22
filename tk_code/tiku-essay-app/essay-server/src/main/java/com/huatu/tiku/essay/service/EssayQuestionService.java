package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import com.huatu.tiku.essay.entity.EssayStandardAnswer;
import com.huatu.tiku.essay.vo.resp.EssayCenterThesisVO;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.resp.EssayStandardAnswerVO;
import com.huatu.tiku.essay.vo.resp.EssayUpdateVO;
import com.huatu.tiku.essay.vo.admin.AdminMaterialListVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionRelationVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionTypeVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionVO;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.correct.ResponseExtendVO;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Created by huangqp on 2017\12\5 0005.
 */
public interface EssayQuestionService {
    List<EssayQuestionBase> findQuestionsByPaperId(long paperId);

    EssayQuestionDetail findQuestionDetailById(long detailId);

    List<EssayMaterialVO> materialList( long questionBaseId,long paperId);

    AdminMaterialListVO saveMaterial(AdminMaterialListVO vo);

    AdminQuestionVO saveQuestionDetail(AdminQuestionVO question, long paperId, int uid);

    EssayQuestionBase findQuestionBaseById(long questionBaseId);

    PageUtil<EssayCenterThesisVO> findThesisByCondition(int page, int pageSize , String questionName , long areaId, String year);

    AdminQuestionRelationVO findQuestionRelationInfo(long questionBaseId);

    PageUtil<AdminQuestionVO> findQuestionListByCondition(String stem, String year, long areaId, int type, PageRequest pageable);

    String findPaperName(long paperId);

    EssayUpdateVO adopt(long id, int type);

    EssayStandardAnswer saveStandardAnswer(EssayStandardAnswerVO standardAnswer);

    int delStandardAnswer(long id);

    List<EssayStandardAnswer> findStandardAnswer(long questionBaseId);

    AdminQuestionTypeVO getQuestionType(int type);

    Object boundVideo(Long questionId, Integer videoId);

    Object cancelBoundVideo(Long questionId);

    Object setCorrectType(Long questionDetailId, int correctType);

    Object getAnswerByConditions(long id, double examScoreMax, double examScoreMin, int inputWordMax, int inputWordMin,int page,int pageSize);

    /**
     * question extend info 处理
     * @param answers
     * @param responseExtendVO
     */
    void dealQuestionResponseExtendInfo(List<EssayQuestionAnswer> answers, ResponseExtendVO responseExtendVO);
}
