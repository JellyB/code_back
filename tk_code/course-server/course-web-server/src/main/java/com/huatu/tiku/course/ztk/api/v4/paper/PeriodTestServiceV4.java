package com.huatu.tiku.course.ztk.api.v4.paper;

import java.util.Set;

import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.huatu.tiku.course.bean.NetSchoolResponse;

/**
 * 
 * @author zhangchong
 *
 */
@FeignClient(value = "ztk-service", fallbackFactory = PeriodTestServiceV4.PeriodTestServiceV4FallbackFallbackFactory.class, path = "/p")
public interface PeriodTestServiceV4 {

	/**
	 * 获取用户试卷信息
	 * 
	 * @param userId
	 * @param syllabusId
	 * @param paperId
	 * @return
	 */
	@GetMapping(value = "/v4/periodTest/{userId}/{syllabusId}/{paperId}")
	NetSchoolResponse getPaperStatus(@PathVariable("userId") int userId, @PathVariable("syllabusId") long syllabusId,
			@PathVariable("paperId") long paperId);

	/**
	 * 批量获取用户试卷信息
	 * @param userId
	 * @param paperSyllabusSet
	 * @return
	 */
	@PostMapping(value = "/v4/periodTest/getPaperStatusBath/{userId}")
	NetSchoolResponse getPaperStatusBath(@PathVariable("userId") int userId, @RequestBody Set<String> paperSyllabusSet);


	@Slf4j
	@Component
	class PeriodTestServiceV4FallbackFallbackFactory implements Fallback<PeriodTestServiceV4>{
		@Override
		public PeriodTestServiceV4 create(Throwable throwable, HystrixCommand command) {
			return new PeriodTestServiceV4(){
				/**
				 * 获取用户试卷信息
				 *
				 * @param userId
				 * @param syllabusId
				 * @param paperId
				 * @return
				 */
				@Override
				public NetSchoolResponse getPaperStatus(int userId, long syllabusId, long paperId) {
					log.error("PeriodTestService v4 getPaperStatus fall back error, params userId:{}, syllabusId:{}, paperId:{}, fall back reason:{}", userId, syllabusId, paperId, throwable);
					return NetSchoolResponse.DEFAULT_ERROR;
				}

				/**
				 * 批量获取用户试卷信息
				 *
				 * @param userId
				 * @param paperSyllabusSet
				 * @return
				 */
				@Override
				public NetSchoolResponse getPaperStatusBath(int userId, Set<String> paperSyllabusSet) {
					log.error("PeriodTestService v4 getPaperStatusBath fall back error, params userId:{}, paperSyllabusSet:{}, fall back reason:{}", userId, paperSyllabusSet, throwable );
					return NetSchoolResponse.DEFAULT_ERROR;
				}
			};
		}
	}
}
