package com.huatu.tiku.schedule.biz.repository;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.ConfirmToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional
	@Modifying
	@Query(value = "UPDATE confirm_token set expire = TRUE where sourse_id=?1 and course_live_teacher_id=?2 and teacher_type=?3",nativeQuery = true)
    void updateExpire(Long liveId, Long courseLiveTeacherId, Integer type);

	@Transactional
	@Modifying
	@Query(value = "UPDATE confirm_token set expire = TRUE where sourse_id=?1 and teacher_type=?2",nativeQuery = true)
	void updateExpireZJ(Long liveId, int ordinal);
}
