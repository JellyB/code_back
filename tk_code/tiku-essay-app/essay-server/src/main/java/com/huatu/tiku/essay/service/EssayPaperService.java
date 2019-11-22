package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.admin.AdminPaperVO;
import com.huatu.tiku.essay.vo.admin.AdminPaperWithQuestionVO;
import com.huatu.tiku.essay.vo.admin.AdminSingleQuestionVO;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.correct.ResponseExtendVO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2017\11\23 0023.
 */
public interface EssayPaperService {
    /**
     * 通过用户和地区查询试卷信息
     */
    List<EssayPaperVO> findPaperListByArea(long areaId, int userId, EssayAnswerCardEnum.ModeTypeEnum normal, Pageable pageable);
    /**
     * 通过用户和地区统计试卷总数
     */
    Long countPapersByArea(long areaId, int userId);

    /**
     * 查询地区
     */
    List<EssayQuestionAreaVO> findAreaList();
//    /**
//     * 获取试卷所有的试题信息和答题情况
//     */
//    EssayPaperQuestionVO findQuestionDetailByPaperId(long paperId, int userId);

    /**
     * 多条件分页查询
     */
    PageUtil<AdminPaperWithQuestionVO> findByConditions(String name, long areaId, String year, int status, int type, int bizStatus, Pageable pageable,int mockType,int tag,long questionId,long paperId,String admin);
//    /**
//     * 多条件count
//     */
//    long countByConditions(String name, long areaId, String year,int status,int type,List<Long> mockIdList,long paperId);


    AdminPaperVO addEssayPaper(AdminPaperVO essayPaper);

//    EssayPaperVO saveEssayPaper(EssayPaperVO essayPaper);

    int modifyPaperStatusById(Integer type, long paperId);

//    int modifyPaperBizStatusById(Integer bizStatus, long paperId);

    EssayPaperDetailVO findPaperAllDetail(long paperId);

    List<AdminSingleQuestionVO> findQuestionListByPaper(long paperId,boolean redisFlag);

    List<EssaySimpleQuestionVO> findQuestionListByPapers(List<Long> ids);

    int deleteQuestion(long questionId,long paperId);


    EssayPaperBase findPaperInfoById(long paperId);

    void resetPaperStatus(long paperId);

    List<EssayQuestionAreaVO> findAreaListNoBiz(String admin);

    EssayUpdateVO addWhitePaper(Long paperId);

    EssayPaperQuestionVO findQuestionDetailByPaperIdV1(long paperId, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum);

    EssayPaperQuestionVO findQuestionDetailByPaperIdV2(long paperId, int userId, EssayAnswerCardEnum.ModeTypeEnum normal);
    
    EssayPaperQuestionVO findQuestionDetailByPaperIdV4(long paperId, int userId, Integer type, Integer bizStatus, Long cardId, EssayAnswerCardEnum.ModeTypeEnum normal);
    
    /**
     * 给得分app提供试题详情接口
     * @param paperId
     * @return
     */
    EssayPaperQuestionVO findQuestionDetailByPaperIdForDf(long paperId);


    void sendPaper2Search(Long paperId,int type);

    int resetQuestion(long questionBaseId);

    int delQuestionRuleByDetailId(long questionDetailId,boolean flag);

    Object getGuFenPapers();

    /**
     * 教育试卷列表返回
     * @param name
     * @param areaId
     * @return
     */
    List<Object> findByAreaOrName(String name, long areaId);

    /**
     * 教育试卷详情返回
     * @param paperId
     * @return
     */
    Object findInfoByIdForEdu(long paperId);

    /**
     * 试卷表全量查询
     * @return
     */
    List<EssayPaperBase> findAll();
    
    /**
     * 根据用户和地区查询套卷列表 包含人工批改试卷状态返回
     * @param areaId
     * @param userId
     * @param pageable
     * @param normal
     * @return
     */
    List<EssayPaperVO> findPaperListByAreaV2(long areaId, int userId, Pageable pageable, EssayAnswerCardEnum.ModeTypeEnum normal);

    /**
     * paperIds 转 map
     * @param userId
     * @param paperIds
     * @param modeTypeEnum
     * @return
     */
    Map<Long, List<EssayPaperAnswer>> convertPaperIds2PaperAnswerMap(int userId, List<Long> paperIds, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum);

    /**
     * 构建人工批改扩展信息
      * @param answers
     * @param paper
     */
    void buildManualCorrectExtendInfo(List<EssayPaperAnswer> answers, EssayPaperVO paper);

    /**
     * 处理 ResponseExtendInfo
     * @param answers
     * @param responseExtendVO
     */
    void dealPaperResponseExtendInfo(List<EssayPaperAnswer> answers, ResponseExtendVO responseExtendVO);

    /**
     * 教育查询模考大赛列表
     * @param name
     * @param status
     * @param startTime
     * @param endTime
     * @param tagId
     * @param paperId
     * @param pageable
     * @return
     */
    PageUtil<HashMap<String,Object>> findByConditionsForEdu(String name, int status, long startTime, long endTime, int tagId, String paperId, PageRequest pageable);

}


