package com.huatu.tiku.essay.web.controller.api.V1.courseExercises;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant.EssayAnswerBizStatusEnum;
import com.huatu.tiku.essay.entity.courseExercises.EssayExercisesAnswerMeta;
import com.huatu.tiku.essay.essayEnum.CourseExerciseTypeEnum;
import com.huatu.tiku.essay.essayEnum.CourseWareTypeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.UserAnswerService;
import com.huatu.tiku.essay.service.courseExercises.CourseExercisesReportService;
import com.huatu.tiku.essay.service.courseExercises.EssayCourseExercisesService;
import com.huatu.tiku.essay.service.courseExercises.EssayExercisesAnswerMetaService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.resp.CreateAnswerCardVO;
import com.huatu.tiku.essay.vo.resp.PaperCommitVO;
import com.huatu.tiku.essay.vo.resp.ResponseVO;
import com.huatu.tiku.essay.vo.resp.courseExercises.ExercisesListVO;
import com.huatu.tiku.essay.vo.resp.courseExercises.ExercisesListVO.ExercisesItemVO;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 课后作业
 *
 * @author zhangchong
 */
@Slf4j
@RestController
@RequestMapping("api/v1/course/exercises")
public class ApiCourseExercisesController {


    @Autowired
    CourseExercisesReportService courseExercisesReportService;

    @Autowired
    UserAnswerService userAnswerService;

    @Autowired
    EssayExercisesAnswerMetaService essayExercisesAnswerMetaService;

    @Autowired
    EssayCourseExercisesService courseExercisesService;

    /**
     * 多题列表
     *
     * @param userSession
     * @param courseWareId
     * @param courseType   @see CourseWareTypeEnum
     * @return
     */
    @GetMapping("list")
	public ExercisesListVO list(@Token UserSession userSession, Long courseWareId, Integer courseType, Long syllabusId) {
    	courseType = CourseWareTypeEnum.changeVideoType2TableCourseType(courseType);
		ExercisesListVO ret = courseExercisesService.getCourseExerciseQuestionList(userSession.getId(), courseWareId,
				courseType, syllabusId);
		return ret;

	}


    /**
     * 套题报告
     *
     * @param userSession
     * @param answerId
     * @return
     */
    @LogPrint
    @GetMapping("paper/report")
    public Object getReport(@Token UserSession userSession,
                            @RequestParam Long answerId,
                            @RequestParam Long syllabusId) {
        return courseExercisesReportService.getRealPaperReport(answerId, syllabusId);
    }

    /**
     * 单题报告
     *
     * @param userSession
     * @param answerId
     * @return
     */
    @LogPrint
    @GetMapping("question/report")
    public Object getQuestionReport(@Token UserSession userSession, @RequestParam Long answerId,
			@RequestParam Long syllabusId) {

		return courseExercisesReportService.getQuestionReport(answerId, syllabusId);
	}

    /**
     * 创建答题卡
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @param createAnswerCardVO
     * @return
     */
    @LogPrint
    @PostMapping(value = "answerCard", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseVO createAnswerCard(@Token UserSession userSession,
                                       @RequestHeader int terminal,
                                       @RequestHeader String cv,
                                       @RequestBody CreateAnswerCardVO createAnswerCardVO) {
        createAnswerCardVO.setUserId(userSession.getId());
		createAnswerCardVO
				.setCourseType(CourseWareTypeEnum.changeVideoType2TableCourseType(createAnswerCardVO.getCourseType()));
        List<EssayExercisesAnswerMeta> metas = essayExercisesAnswerMetaService.createPreCheck(createAnswerCardVO);
        ResponseVO answerCardV2 = userAnswerService.createAnswerCardV2(userSession.getId(), createAnswerCardVO, terminal, (a, b) -> 0L, EssayAnswerCardEnum.ModeTypeEnum.COURSE.getType());
        essayExercisesAnswerMetaService.create(answerCardV2, createAnswerCardVO, metas);
        return answerCardV2;

    }

    /**
     * 提交或保存答题卡
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @param paperCommitVO
     * @return
     */
    @LogPrint
    @PostMapping(value = "answerCard/submit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object commit(@Token UserSession userSession, @RequestHeader int terminal, @RequestHeader String cv,
                         @RequestBody PaperCommitVO paperCommitVO) {
        if (paperCommitVO == null) {
            throw new BizException(EssayErrors.ESSAY_PARAM_ILLEGAL);
        }
        paperCommitVO.setExercisesType(CourseExerciseTypeEnum.exercises.getCode());
        Object o = userAnswerService.paperCommitV2(userSession, paperCommitVO, terminal, cv);
        essayExercisesAnswerMetaService.commit(userSession, paperCommitVO);
        return o;
    }
}
