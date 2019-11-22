package com.huatu.tiku.essay.service.courseExercises;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.courseExercises.EssayExercisesAnswerMeta;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.CreateAnswerCardVO;
import com.huatu.tiku.essay.vo.resp.PaperCommitVO;
import com.huatu.tiku.essay.vo.resp.ResponseVO;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述
 */
public interface EssayExercisesAnswerMetaService {


    /**
     * 创建答题卡关联关系
	 * @param responseVO
	 * @param createAnswerCardVO
	 * @param metas
	 */
    void create(ResponseVO responseVO, CreateAnswerCardVO createAnswerCardVO, List<EssayExercisesAnswerMeta> metas);

    /**
     * 检查创建课后作业答题卡逻辑
	 * @param createAnswerCardVO
	 * @return
	 */
    List<EssayExercisesAnswerMeta> createPreCheck(CreateAnswerCardVO createAnswerCardVO);

    /**
     * 交卷完成
     * @param userSession
     * @param paperCommitVO
     */
    void commit(UserSession userSession, PaperCommitVO paperCommitVO);

    List<EssayExercisesAnswerMeta> findByAnswerIdAndType(long answerId, EssayAnswerCardEnum.TypeEnum typeEnum);

    void save(EssayExercisesAnswerMeta essayExercisesAnswerMeta);
    
    /**
     * 根据大纲id和试题id获取班级答题信息
     * @param paperOrQuestionId
     * @param type
     * @param syllabusId
     * @param status
     * @return
     */
	List<EssayExercisesAnswerMeta> findByPQidAndAnswerTypeAndSyllabusIdAndStatus(Long paperOrQuestionId, Integer type, Long syllabusId, Integer status);
	
	/**
	 * 根据答题卡id和类型查询
	 * @param answerIdList
	 * @param type
	 * @param status
	 * @return
	 */
	List<EssayExercisesAnswerMeta> findByAnswerIdInAndTypeAndStatus(List<Long> answerIdList, Integer type, Integer status);


	void updateQuestionStatus(EssayQuestionAnswer questionAnswer);

	void updatePaperStatus(EssayPaperAnswer essayPaperAnswer);

	List<EssayExercisesAnswerMeta> findByPQidAndAnswerTypeAndSyllabusIdAndUserIdAndStatus(Long pQid, Integer answerType,
			Long syllabusId, Integer userId, int status);





}
