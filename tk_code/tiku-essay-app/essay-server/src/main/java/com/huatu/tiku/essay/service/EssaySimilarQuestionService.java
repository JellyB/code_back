package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.entity.EssaySimilarQuestionGroupInfo;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminSingleQuestionGroupVO;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionTypeVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by x6 on 2017/12/19.
 */
public interface EssaySimilarQuestionService {

    List<EssayMaterialVO> findMaterialList(long questionBaseId);

    List<EssayQuestionAreaVO> findAreaList(long similarId, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum);

    List<EssayQuestionAreaVO> findAreaList(EssaySimilarQuestionGroupInfo similarQuestion);

    List<EssayQuestionTypeVO> findQuestionType();

    AdminSingleQuestionGroupVO updateSingleQuestion(AdminSingleQuestionGroupVO singleQuestionGroupVO);

    PageUtil<AdminSingleQuestionGroupVO> findSingleQuestionList(Pageable pageRequest, String title, int type, int bizStatus, long questionId, long groupId);

    AdminSingleQuestionGroupVO saveSingleQuestion(AdminSingleQuestionGroupVO singleQuestionGroupVO);

    EssayQuestionVO findQuestionDetailV1(long questionBaseId, int id, EssayAnswerCardEnum.ModeTypeEnum normal);

    EssayQuestionVO findQuestionDetailV2(long questionBaseId, int id, EssayAnswerCardEnum.ModeTypeEnum normal);

    EssayQuestionVO findQuestionDetailV3(long questionBaseId, int id, int correctMode, Integer bizStatus, Long answerId, EssayAnswerCardEnum.ModeTypeEnum normal);

    List<EssayQuestionTypeVO> findQuestionTypeV2();

    List<EssayQuestionVO> findSingleQuestionListV2(Pageable pageRequest, int type, int id, EssayAnswerCardEnum.ModeTypeEnum normal);

    long countSingleQuestionByTypeV2(int type);

    int delQuestion(long questionId);

    void sendSimilarQuestion2Search(Long groupId, int type);

    /**
     * 根据试题ID查询单题组名称
     *
     * @param questionBaseId
     * @return
     */
    String getSimilarNameByQuestionId(long questionBaseId);

}
