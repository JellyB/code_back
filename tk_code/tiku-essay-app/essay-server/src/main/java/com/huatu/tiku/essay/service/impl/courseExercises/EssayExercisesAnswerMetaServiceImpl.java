package com.huatu.tiku.essay.service.impl.courseExercises;

import java.util.List;
import java.util.Optional;

import com.huatu.tiku.essay.constant.status.AnswerSaveTypeConstant;
import com.huatu.tiku.essay.entity.courseExercises.EssayCourseExercisesQuestion;
import com.huatu.tiku.essay.repository.courseExercises.EssayCourseExercisesQuestionRepository;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.courseExercises.EssayExercisesAnswerMeta;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.courseExercises.EssayExercisesAnswerMetaRepository;
import com.huatu.tiku.essay.service.courseExercises.EssayExercisesAnswerMetaService;
import com.huatu.tiku.essay.service.paper.EssayPaperAnswerService;
import com.huatu.tiku.essay.service.question.EssayQuestionAnswerService;
import com.huatu.tiku.essay.vo.resp.CreateAnswerCardVO;
import com.huatu.tiku.essay.vo.resp.PaperCommitVO;
import com.huatu.tiku.essay.vo.resp.ResponseVO;

import lombok.extern.slf4j.Slf4j;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述
 */
@Slf4j
@Service
public class EssayExercisesAnswerMetaServiceImpl implements EssayExercisesAnswerMetaService {

    @Autowired
    EssayExercisesAnswerMetaRepository essayExercisesAnswerMetaRepository;

    @Autowired
    EssayPaperAnswerService essayPaperAnswerService;

    @Autowired
    EssayQuestionAnswerService essayQuestionAnswerService;

    @Autowired
    EssayCourseExercisesQuestionRepository essayCourseExercisesQuestionRepository;

    @Override
    public void create(ResponseVO responseVO, CreateAnswerCardVO createAnswerCardVO, List<EssayExercisesAnswerMeta> metas) {
        EssayExercisesAnswerMeta build = EssayExercisesAnswerMeta.builder().answerId(responseVO.getAnswerCardId())
                .answerType(createAnswerCardVO.getType())
                .courseId(createAnswerCardVO.getCourseId())
                .courseWareId(createAnswerCardVO.getCourseWareId())
                .syllabusId(createAnswerCardVO.getSyllabusId())
                .userId(createAnswerCardVO.getUserId())
                .spendTime(0L)
                .pQid(getPaperOrQuestionId(createAnswerCardVO))
                .courseType(createAnswerCardVO.getCourseType())
                .build();
        Optional<EssayExercisesAnswerMeta> first = metas.stream().filter(i -> null == i.getAnswerId()).findFirst();
        if(first.isPresent()){
            EssayExercisesAnswerMeta essayExercisesAnswerMeta = first.get();
            build.setId(essayExercisesAnswerMeta.getId());
            build.setCorrectNum(metas.size());
        }else{
            build.setCorrectNum(metas.size() + 1);
        }
        build.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
		essayExercisesAnswerMetaRepository.save(build);
    }

    private Long getPaperOrQuestionId(CreateAnswerCardVO createAnswerCardVO) {
        if (createAnswerCardVO.getType().intValue() == EssayAnswerCardEnum.TypeEnum.QUESTION.getType()) {
            return createAnswerCardVO.getQuestionBaseId();
        } else {
            return createAnswerCardVO.getPaperBaseId();
        }
    }

    @Override
    public List<EssayExercisesAnswerMeta> createPreCheck(CreateAnswerCardVO createAnswerCardVO) {
        isNotNull(createAnswerCardVO.getCourseId(), "课程ID不能为空");
        isNotNull(createAnswerCardVO.getSyllabusId(), "大纲ID不能为空");
        isNotNull(createAnswerCardVO.getCourseWareId(), "课件ID不能为空");
        isNotNull(createAnswerCardVO.getCourseType().longValue(), "课件类型不能为空");
        Integer userId = createAnswerCardVO.getUserId();
        Long paperOrQuestionId = getPaperOrQuestionId(createAnswerCardVO);
        List<EssayExercisesAnswerMeta> answerMetas = essayExercisesAnswerMetaRepository.findByUserIdAndPQidAndAnswerTypeAndSyllabusIdAndStatus(
                userId, paperOrQuestionId,
                createAnswerCardVO.getType(), createAnswerCardVO.getSyllabusId(),
                EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isNotEmpty(answerMetas)) {
            if (answerMetas.size() >= 2) {
                throw new BizException(ErrorResult.create(10001001, "课后作业已超过做题上限"));
            }
            boolean present = answerMetas.stream()
                    .filter(i-> null!= i.getAnswerId())
                    .filter(i -> i.getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus())
                    .findFirst().isPresent();
            if (present) {
                throw new BizException(ErrorResult.create(10001001, "存在未完成的课后作业"));
            }
        }
        return answerMetas;
    }

    @Override
    public void commit(UserSession userSession, PaperCommitVO paperCommitVO) {
        Long answerCardId = paperCommitVO.getAnswerCardId();
        Integer type = paperCommitVO.getType();
        List<EssayExercisesAnswerMeta> answerMetas = essayExercisesAnswerMetaRepository.findByAnswerIdAndAnswerTypeAndStatus(answerCardId, type, EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isEmpty(answerMetas)) {
            return;
        }
        EssayExercisesAnswerMeta essayExercisesAnswerMeta = answerMetas.get(0);
        if (type.intValue() == EssayAnswerCardEnum.TypeEnum.PAPER.getType()) {
            EssayPaperAnswer answer = essayPaperAnswerService.findById(answerCardId);
            essayExercisesAnswerMeta.setSpendTime(new Long(answer.getSpendTime()));
            essayExercisesAnswerMeta.setSubmitTime(answer.getSubmitTime());
            essayExercisesAnswerMeta.setExamScore(answer.getExamScore());
        } else {
            EssayQuestionAnswer answer = essayQuestionAnswerService.findById(answerCardId);
            essayExercisesAnswerMeta.setSpendTime(new Long(answer.getSpendTime()));
            essayExercisesAnswerMeta.setSubmitTime(answer.getSubmitTime());
            essayExercisesAnswerMeta.setExamScore(answer.getExamScore());
        }
        Integer saveType = paperCommitVO.getSaveType();
        if(null != saveType && saveType.intValue() == AnswerSaveTypeConstant.COMMIT){
            essayExercisesAnswerMeta.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
        }
        essayExercisesAnswerMetaRepository.save(essayExercisesAnswerMeta);
    }

    @Override
    public List<EssayExercisesAnswerMeta> findByAnswerIdAndType(long answerId, EssayAnswerCardEnum.TypeEnum typeEnum) {
        List<EssayExercisesAnswerMeta> metas = essayExercisesAnswerMetaRepository.findByAnswerIdAndAnswerTypeAndStatus(answerId, typeEnum.getType(), EssayStatusEnum.NORMAL.getCode());
        return metas;
    }

    @Override
    public void save(EssayExercisesAnswerMeta essayExercisesAnswerMeta) {
        essayExercisesAnswerMetaRepository.save(essayExercisesAnswerMeta);
    }

    private void isNotNull(Long courseId, String message) {
        if (null == courseId || courseId.intValue() < 0) {
            throw new BizException(ErrorResult.create(10001001, message));
        }
    }

    @Override
    public List<EssayExercisesAnswerMeta> findByPQidAndAnswerTypeAndSyllabusIdAndStatus(Long paperOrQuestionId,
                                                                                        Integer type, Long syllabusId, Integer status) {
        return essayExercisesAnswerMetaRepository.findByPQidAndAnswerTypeAndSyllabusIdAndStatus(paperOrQuestionId, type, syllabusId, status);
    }

    @Override
    public List<EssayExercisesAnswerMeta> findByAnswerIdInAndTypeAndStatus(List<Long> answerIdList, Integer type, Integer status) {
        return essayExercisesAnswerMetaRepository.findByAnswerIdInAndAnswerTypeAndStatus(answerIdList, type, status);
    }

    @Override
    public void updateQuestionStatus(EssayQuestionAnswer questionAnswer) {
        if (null != questionAnswer &&
                null != questionAnswer.getAnswerCardType() &&
                questionAnswer.getAnswerCardType().intValue() == EssayAnswerCardEnum.ModeTypeEnum.COURSE.getType()) {
            restStatus(questionAnswer.getBizStatus(),questionAnswer.getId(),EssayAnswerCardEnum.TypeEnum.QUESTION);
        }
    }

    private void restStatus(int bizStatus, long id, EssayAnswerCardEnum.TypeEnum typeEnum) {
        List<EssayExercisesAnswerMeta> answerMetas = findByAnswerIdAndType(id, typeEnum);
        if(CollectionUtils.isNotEmpty(answerMetas)){
            EssayExercisesAnswerMeta essayExercisesAnswerMeta = answerMetas.get(0);
            essayExercisesAnswerMeta.setBizStatus(bizStatus);
            save(essayExercisesAnswerMeta);
        }

    }

    @Override
    public void updatePaperStatus(EssayPaperAnswer essayPaperAnswer) {
        if (null != essayPaperAnswer &&
                null != essayPaperAnswer.getAnswerCardType() &&
                essayPaperAnswer.getAnswerCardType().intValue() == EssayAnswerCardEnum.ModeTypeEnum.COURSE.getType()) {
            restStatus(essayPaperAnswer.getBizStatus(),essayPaperAnswer.getId(),EssayAnswerCardEnum.TypeEnum.PAPER);
        }
    }

	@Override
	public List<EssayExercisesAnswerMeta> findByPQidAndAnswerTypeAndSyllabusIdAndUserIdAndStatus(Long pQid,
			Integer answerType, Long syllabusId, Integer userId, int status) {
		return essayExercisesAnswerMetaRepository.findByPQidAndAnswerTypeAndSyllabusIdAndUserIdAndStatus(pQid,
				answerType, syllabusId, userId, status);
	}
}
