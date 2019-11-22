package com.huatu.tiku.course.service.v1.impl.practice;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.huatu.tiku.course.dao.manual.CoursePracticeUserMataMapper;
import com.huatu.tiku.course.service.cache.CoursePracticeCacheKey;
import com.huatu.tiku.course.service.v1.practice.PracticeUserMetaService;
import com.huatu.tiku.entity.CoursePracticeUserMeta;

import lombok.extern.slf4j.Slf4j;
import service.impl.BaseServiceHelperImpl;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import javax.annotation.Resource;

/**
 * Created by lijun on 2019/3/7
 */
@Service
@Slf4j
public class PracticeUserMetaServiceImpl extends BaseServiceHelperImpl<CoursePracticeUserMeta>
		implements PracticeUserMetaService {

	public PracticeUserMetaServiceImpl() {
		super(CoursePracticeUserMeta.class);
	}

	@Autowired
	@Qualifier(value = "persistTemplate")
	private RedisTemplate persistTemplate;
	
	@Autowired
	private CoursePracticeUserMataMapper coursePracticeUserMataMapper;

	@Override
	public Long getLiveCourseIdListByRoomId(Long roomId, Long userId, Integer courseId) {

		List<CoursePracticeUserMeta> userMetaList = coursePracticeUserMataMapper
				.selectByExample(new Example.Builder(CoursePracticeUserMeta.class).where(WeekendSqls
						.<CoursePracticeUserMeta>custom().andEqualTo(CoursePracticeUserMeta::getUserId, userId)
						.andEqualTo(CoursePracticeUserMeta::getRoomId, roomId)
						.andEqualTo(CoursePracticeUserMeta::getCourseId, courseId)).build());
		if (CollectionUtils.isNotEmpty(userMetaList)) {
			return userMetaList.get(0).getAnswerCardId();
		}
		return null;
	}

	@Override
	public Map<String, Integer> getCountDateByRIdAndCId(Long roomId, Long coursewareId) {
		HashOperations<String, String, Integer> metaOpsForHash = persistTemplate.opsForHash();
		final SetOperations<String, Integer> setOperations = persistTemplate.opsForSet();
		Long count = setOperations.size(CoursePracticeCacheKey.roomIdUserMetaKey(roomId,coursewareId,2));
		if (count == null || count == 0) {
			count = 1L;
		}
		Map<String, Integer> metaEntries = metaOpsForHash
				.entries(CoursePracticeCacheKey.roomIdCourseIdTypeMetaKey(roomId, coursewareId, 2));
		
		Integer rCount = metaEntries.get(CoursePracticeCacheKey.RCOUNT);
		if(rCount == null) {
			rCount = 0;
		}
		Integer totalTime = metaEntries.get(CoursePracticeCacheKey.TOTALTIME);
		if(totalTime == null) {
			totalTime = 0;
		}
		Map<String, Integer> retMap = Maps.newHashMap();
		retMap.put("classAverageTime", totalTime / count.intValue());
		retMap.put("classAverageRcount", rCount / count.intValue());
		log.info("随堂练roomId:{},coursewareId:{},获取班级统计数据结果:{}", roomId, coursewareId, retMap.toString());
		return retMap;
	}
}
