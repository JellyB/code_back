package com.huatu.tiku.schedule.biz.repository;

import org.springframework.data.jpa.repository.Query;
import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.ConfirmToken;

/**
 * 教师确认认证Repository
 *
 * @author Geek-S
 */
public interface ConfirmTokenRepository extends BaseRepository<ConfirmToken, Long> {

	/**
	 * 根据Token查询
	 * 
	 * @param token
	 *            token
	 * @return 教师确认认证
	 */
	ConfirmToken findByToken(String token);


	/**
	 * 校验Token
	 * 
	 * @param token
	 *            Token
	 * @param courseLiveTeacherId
	 *            课程直播教师ID
	 */
	@Query(value = "select count(0) from course_live_teacher clt join course_live cl on clt.course_live_id = cl.id join confirm_token ct on cl.course_id = ct.sourse_id where ct.token = ?1 and clt.id = ?2", nativeQuery = true)
	Integer checkToken(String token, Long courseLiveTeacherId);
}
