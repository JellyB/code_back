package com.huatu.tiku.essay.web.controller.api.edu;

import java.util.ArrayList;
import java.util.List;

import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Stopwatch;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.service.EssayEduService;
import com.huatu.tiku.essay.service.EssayMaterialService;
import com.huatu.tiku.essay.service.EssayPaperReportService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.UserAnswerService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.edu.EssayEduPaperVO;
import com.huatu.tiku.essay.vo.file.YoutuVO;
import com.huatu.tiku.essay.vo.resp.EssayAnswerVO;
import com.huatu.tiku.essay.vo.resp.EssayPaperQuestionVO;
import com.huatu.tiku.essay.vo.resp.EssayPaperReportVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.vo.resp.PaperCommitVO;
import com.huatu.tiku.springboot.users.support.Token;

import lombok.extern.slf4j.Slf4j;

/**
 * 华图教育对接接口
 */
@RestController
@RequestMapping("api/edu")
@Slf4j
public class ApiEssayEduController {

	/**
	 * 交卷批改的接口地址也替换新的：
	 * {{server}}/cr/correct/essayCorrectEdu?answerCardId=1066644&type=0
	 */

	@Autowired
	EssayEduService essayEduService;
	@Autowired
	EssayPaperService essayPaperService;
	@Autowired
	RedisTemplate redisTemplate;
	@Autowired
	EssayMaterialService essayMaterialService;
	@Autowired
	UserAnswerService userAnswerService;
	@Autowired
	EssayPaperReportService essayPaperReportService;

	/**
	 * 通过地区返回试卷列表(教育的试卷暂定统一放在一个新的地区下)
	 *
	 * @param terminal
	 * @param cv
	 * @param areaId
	 * @param page
	 * @param pageSize
	 * @return
	 */
	@LogPrint
	@GetMapping(value = "paperList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object paperList(@Token(check = false) UserSession userSession, @RequestHeader int terminal, @RequestHeader String cv,
			@RequestParam(name = "areaId", defaultValue = "9998") long areaId,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

		Pageable pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "paperYear", "paperDate", "areaId",
				"subAreaId");
		long count = essayPaperService.countPapersByArea(areaId, userSession.getId());
		List<EssayEduPaperVO> papers = null;
		if (count > 0) {
			papers = essayEduService.findPaperListByArea(areaId, userSession.getId(), pageable,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
		}
		PageUtil pageUtil = PageUtil.builder().result(papers).next(((int) count) > page * pageSize ? 1 : 0)
				.total(count).build();
		return pageUtil;
	}

	/**
	 * 通过地区返回试卷列表(教育的试卷暂定统一放在一个新的地区下)无分页
	 *
	 * @param terminal
	 * @param cv
	 * @param areaId
	 * @return
	 */
	@LogPrint
	@GetMapping(value = "paperAllList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object paperAllList(@Token UserSession userSession, @RequestHeader int terminal, @RequestHeader String cv,
			@RequestParam(name = "areaId", defaultValue = "10000") long areaId) {

		return essayEduService.findPaperAllListByArea(areaId, userSession.getId(),EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
	}

	/**
	 * 获取试卷材料
	 *
	 * @param terminal
	 * @param cv
	 * @param paperId
	 * @return
	 */
	@LogPrint
	@GetMapping(value = "materialList/{paperId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object getMaterialsByPaper(@Token UserSession userSession, @RequestHeader int terminal, @RequestHeader String cv,
			@PathVariable(name = "paperId") long paperId) {
		return essayMaterialService.findMaterialsByPaperId(paperId);
	}

	/**
	 * 获取试卷题目以及学员答题情况
	 *
	 * @param terminal
	 * @param cv
	 * @param paperId
	 * @return
	 */
	@LogPrint
	@GetMapping(value = "questionList/{paperId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object getQuestionsWithAnswer(@Token UserSession userSession, @RequestHeader int terminal,
			@RequestHeader String cv, @PathVariable(name = "paperId") long paperId) {

		EssayPaperQuestionVO paperVO = essayPaperService.findQuestionDetailByPaperIdV2(paperId, userSession.getId(), EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
		return paperVO;
	}

	/**
	 * 创建答题卡/继续答题逻辑
	 *
	 * @return
	 */
	@LogPrint
	@PostMapping(value = "answerCard/{paperId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Long createAnswerCard(@Token UserSession userSession, @RequestHeader int terminal, @RequestHeader String cv,
			@PathVariable(name = "paperId") long paperId) {
		return essayEduService.createAnswerCard(userSession.getId(), paperId, terminal,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);

	}

	/**
	 * 保存或交卷
	 *
	 * @return
	 */
	@LogPrint
	@PutMapping(value = "answerCard", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public boolean commit(@Token UserSession userSession, @RequestHeader int terminal, @RequestHeader String cv,
			@RequestBody PaperCommitVO paperCommitVO) {
		if (paperCommitVO == null) {
			throw new BizException(EssayErrors.ESSAY_PARAM_ILLEGAL);
		}
		return essayEduService.paperCommit(userSession.getId(), paperCommitVO, terminal, cv);

	}

	/**
	 * 查询批改列表
	 *
	 * @return
	 */
	@Deprecated
	@LogPrint
	@GetMapping(value = "correctDetailList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public PageUtil correctList(@RequestHeader int userId, @RequestHeader int terminal, @RequestHeader String cv,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

		List<Sort.Order> orders = new ArrayList<Sort.Order>();
		orders.add(new Sort.Order(Sort.Direction.ASC, "bizStatus"));
		orders.add(new Sort.Order(Sort.Direction.DESC, "correctDate"));

		Pageable pageRequest = new PageRequest(page - 1, pageSize, new Sort(orders));
		List<EssayAnswerVO> l = essayEduService.paperCorrectList(userId, pageRequest,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);

		long c = essayEduService.countPaperCorrectList(userId,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);

		PageUtil p = PageUtil.builder().result(l).next(c > page * pageSize ? 1 : 0).build();
		return p;
	}

	/**
	 * 查询批改报告
	 *
	 * @return
	 * @throws BizException
	 */
	@LogPrint
	@GetMapping(value = "report/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public EssayPaperReportVO getPaperReport(@Token UserSession userSession, @PathVariable long answerId)
			throws BizException {

		return essayPaperReportService.getReport(answerId);
	}

	/**
	 * 图片识别接口
	 */
	@PostMapping(value = "photo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public YoutuVO photo(MultipartFile file, @Token UserSession userSession, @RequestHeader int terminal,
			@RequestHeader String cv) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		YoutuVO photo = new YoutuVO();
		try {
			photo = userAnswerService.photo(file, 0, userSession.getId(), terminal);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("拍照识别，用时" + String.valueOf(stopwatch.stop()));
		return photo;
	}

	/**
	 * 查询批改详情
	 *
	 * @return
	 */
	@LogPrint
	@GetMapping(value = "correctDetail/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public List<EssayQuestionVO> correctDetail(@Token UserSession userSession, @RequestHeader int terminal,
			@RequestHeader String cv, @PathVariable(value = "answerId", required = true) long answerId) {
		List<EssayQuestionVO> essayQuestionVOS = essayEduService.answerDetail(userSession.getId(), answerId, terminal, cv);

		return essayQuestionVOS;
	}

	/**
	 * 云笔图片识别测试
	 */
	@PostMapping(value = "photo/test", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ModelAndView photo() {
		return essayEduService.photo();
	}

	/*
	 * 查看某个试卷的批改记录列表
	 */
	@LogPrint
	@GetMapping(value = "correctDetailList/{paperId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object correctDetailListByPaperId(@Token UserSession userSession, @RequestHeader int terminal,
			@RequestHeader String cv, @PathVariable(value = "paperId", required = true) long paperId) {

		List<EssayAnswerVO> l = essayEduService.paperCorrectList(userSession.getId(), paperId,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);

		return l;
	}

}
