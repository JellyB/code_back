package com.huatu.tiku.course.web.controller.v6.practice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.service.v1.practice.StudentService;
import com.huatu.tiku.springboot.users.support.Token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by lijun on 2019/2/18
 */
@RestController
@RequestMapping("practice/student")
@RequiredArgsConstructor
@ApiVersion("v6")
@Slf4j
public class StudentController {

	private final StudentService studentService;

	/**
	 * 用户提交答案
	 */
	@PutMapping("{roomId}/{questionId}/putAnswer")
	public Object putAnswer(@Token UserSession userSession, @PathVariable Long roomId, @PathVariable Long questionId,
			@RequestParam Long courseId, @RequestParam String answer, @RequestParam Integer time) {
		log.info("随堂练房间id:{},questionId:{} 用户:{}提交答案:{}", roomId, questionId, userSession.getUname(), answer);
		return studentService.putAnswer(roomId, courseId, userSession.getId(), userSession.getNick(), questionId,
				answer, time);
	}

	/**
	 * 获取答题情况
	 */
	@GetMapping("{roomId}/{questionId}/questionStatistics")
	public Object getQuestionStatistics(@Token UserSession userSession, @PathVariable Long roomId,
			@PathVariable Long questionId, @RequestParam Long courseId) {
		return studentService.getStudentQuestionMetaBo(userSession.getId(), roomId, courseId, questionId);
	}

	/**
	 * 获取排名信息
	 */
	@GetMapping("{roomId}/questionRankInfo")
	public Object getQuestionRankInfo(@PathVariable Long roomId) {
		return studentService.listPracticeRoomRankUser(roomId, 0, 9);
	}

	/**
	 * 获取用户个人排名信息
	 */
	@GetMapping("{courseId}/userRankInfo")
	public Object getUserRankInfo(@Token UserSession userSession, @PathVariable Long courseId) {
		return studentService.getUserRankInfo(userSession.getId(), courseId);
	}
}
