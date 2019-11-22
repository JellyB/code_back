package com.huatu.tiku.essay.web.controller.admin.edu;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.essay.service.EssayEduService;
import com.huatu.tiku.essay.service.EssayMaterialService;
import com.huatu.tiku.essay.service.EssayPaperReportService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.UserAnswerService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminPaperWithQuestionVO;
import com.huatu.tiku.essay.vo.resp.EssayPaperQuestionVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 华图教育得分对接接口
 */
@RestController
@RequestMapping("eduapi/df/paper")
@Slf4j
public class ApiEssayScoreController {

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
	 * 查询试卷列表
	 *
	 * @return 套题id，名称，地区，年份，日期，套题状态，审核状态
	 */
	@LogPrint
	@GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public PageUtil<AdminPaperWithQuestionVO> findByConditions(@RequestParam(defaultValue = "") String name,
			@RequestParam(defaultValue = "-1") long areaId, @RequestParam(defaultValue = "") String year,
			@RequestParam(defaultValue = "0") int status, @RequestParam(defaultValue = "-1") int bizStatus,
			@RequestParam(defaultValue = "-1") int type, @RequestParam(defaultValue = "-1") int mockType,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "-1") int tag,
			@RequestParam(defaultValue = "20") int pageSize, @RequestParam(defaultValue = "-1") int paperId,
			@RequestParam(defaultValue = "-1") int questionId,
			@RequestHeader(name = "admin", defaultValue = "") String admin) {
		PageRequest pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtCreate");
		if ("0".equals(year.trim())) {
			year = "";
		}
		return essayPaperService.findByConditions(name, areaId, year, status, type, bizStatus, pageable, mockType, tag,
				questionId, paperId, admin);

	}

	/**
	 * 返回试卷所在的所有地区列表
	 *
	 * @return
	 */
	@LogPrint
	@GetMapping(value = "areaList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public List<EssayQuestionAreaVO> multiQuestionList(@RequestHeader(name = "admin", defaultValue = "") String admin) {

		return essayPaperService.findAreaListNoBiz(admin);
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
	public Object getMaterialsByPaper(@PathVariable(name = "paperId") long paperId) {
		return essayMaterialService.findMaterialsByPaperId(paperId);
	}

	/**
	 * 获取试卷题目
	 *
	 * @param terminal
	 * @param cv
	 * @param paperId
	 * @return
	 */
	@LogPrint
	@GetMapping(value = "questionList/{paperId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object getQuestionsWithAnswer(@RequestHeader int terminal, @RequestHeader String cv,
			@PathVariable(name = "paperId") long paperId) {

		EssayPaperQuestionVO paperVO = essayPaperService.findQuestionDetailByPaperIdForDf(paperId);
		return paperVO;
	}

}
