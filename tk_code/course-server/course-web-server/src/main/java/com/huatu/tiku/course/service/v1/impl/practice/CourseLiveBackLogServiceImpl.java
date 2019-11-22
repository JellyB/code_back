package com.huatu.tiku.course.service.v1.impl.practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.consts.SimpleCourseLiveBackLog;
import com.huatu.tiku.course.netschool.api.v6.LessonServiceV6;
import com.huatu.tiku.course.util.CourseCacheKey;
import com.huatu.tiku.course.util.ResponseUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.huatu.tiku.course.service.v1.practice.CourseLiveBackLogService;
import com.huatu.tiku.entity.CourseLiveBackLog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import service.impl.BaseServiceHelperImpl;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;


/**
 * 
 * @author zhangchong
 *
 */
@Service
@Slf4j
public class CourseLiveBackLogServiceImpl extends BaseServiceHelperImpl<CourseLiveBackLog>
		implements CourseLiveBackLogService {

	@Autowired
	private LessonServiceV6 lessonService;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	@Qualifier(value = "courseExecutorService")
	private ExecutorService executorService;

	private final Cache<String, SimpleCourseLiveBackLog> logCache = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.DAYS).maximumSize(5000).build();

	public CourseLiveBackLogServiceImpl() {
		super(CourseLiveBackLog.class);
	}

	@Override
	public CourseLiveBackLog findByRoomIdAndLiveCoursewareId(final Long roomId, final Long coursewareId) {
		final ValueOperations<String,String> operations = redisTemplate.opsForValue();
		String key = CourseCacheKey.findByRoomIdAndLiveCourseWareId(roomId, coursewareId);
		final CountDownLatch countDownLatch = new CountDownLatch(1);

		Runnable phpTask = () -> {
			//线程异步获取 courseLiveBackLog 信息，并入库 & 写入redis
			executorService.execute(() -> {
				log.info("调用 php 获取直播回放对应的直播课件id:直播回放id:{},房间id:{}", coursewareId, roomId);
				Map<String,Object> params = Maps.newHashMap();
				params.put("liveBackCoursewareId",coursewareId);
				params.put("roomId", roomId);
				NetSchoolResponse netSchoolResponse = lessonService.obtainLiveWareId(params);
				if(ResponseUtil.isSuccess(netSchoolResponse)){
					LinkedHashMap<String,Object> result = (LinkedHashMap<String,Object>) netSchoolResponse.getData();
					long liveCourseWareId = MapUtils.getLong(result, "liveCoursewareId");
					if(liveCourseWareId != 0){
						CourseLiveBackLog courseLiveBackLog = new CourseLiveBackLog();
						courseLiveBackLog.setLiveBackCoursewareId(coursewareId);
						courseLiveBackLog.setRoomId(roomId);
						courseLiveBackLog.setLiveCoursewareId(liveCourseWareId);
						courseLiveBackLog.setLiveBackCoursewareId(coursewareId);
						courseLiveBackLog.setCreatorId(10L);
						insert(courseLiveBackLog);
						operations.set(key, JSONObject.toJSONString(courseLiveBackLog), 30, TimeUnit.MINUTES);
						log.info("get live courseWareId from remote by rest:roomId:{}, coursewareId:{}", roomId, coursewareId);
					}
				}
			});
		};

		Callable<CourseLiveBackLog> redisTask = () -> {
			try {
				if(redisTemplate.hasKey(key)){
					String value = operations.get(key);
					log.info("get live courseWareId from redis:roomId:{}, courseWareId:{}", roomId, coursewareId);
					CourseLiveBackLog courseLiveBackLog = JSONObject.parseObject(value,CourseLiveBackLog.class);
					return courseLiveBackLog;
				}else{
					return null;
				}
			}finally{
				countDownLatch.countDown();
			}
		};

		Callable<CourseLiveBackLog> mysqlTask = () -> {
			final WeekendSqls<CourseLiveBackLog> courseLiveBackLogWeekendSql = WeekendSqls.<CourseLiveBackLog>custom()
					.andEqualTo(CourseLiveBackLog::getRoomId, roomId)
					.andEqualTo(CourseLiveBackLog::getLiveBackCoursewareId, coursewareId);
			final Example example = Example.builder(CourseLiveBackLog.class).where(courseLiveBackLogWeekendSql).build();
			List<CourseLiveBackLog> courseLiveBackLogList = selectByExample(example);

			try{
				if (CollectionUtils.isNotEmpty(courseLiveBackLogList)) {
					CourseLiveBackLog courseLiveBackLog = courseLiveBackLogList.get(0);
					operations.set(key, JSONObject.toJSONString(courseLiveBackLog), 30, TimeUnit.MINUTES);
					log.info("get live courseWareId from mysql:roomId:{}, courseWareId:{}", roomId, coursewareId);
					return courseLiveBackLog;
				}else{
					executorService.execute(phpTask);
					return null;
				}
			}finally {
				countDownLatch.countDown();
			}
		};

		Future<CourseLiveBackLog> redisFuture = executorService.submit(redisTask);
		Future<CourseLiveBackLog> mysqlFuture = executorService.submit(mysqlTask);

		try{
			countDownLatch.await();
			if(redisFuture.isDone() || mysqlFuture.isDone()){
				if(redisFuture.isDone()){
					log.debug("findByRoomIdAndLiveCourseWareId redis result:{}", redisFuture.get());
					return redisFuture.get();
				}else if(mysqlFuture.isDone()){
					log.debug("findByRoomIdAndLiveCourseWareId mysql result:{}", mysqlFuture.get());
					return mysqlFuture.get();
				}else{
					return null;
				}
			}else{
				log.error("redis & mysql task both is unDone");
			}
		}catch (Exception e){
			log.error("get future task result failed!");
			return null;
		}
		return null;
	}


	/**
	 * 通过 guava 或者 database 获取直播课件id
	 *
	 * @param roomId
	 * @param courseWareId
	 * @return
	 */
	@Override
	public SimpleCourseLiveBackLog findByRoomIdAndLiveCourseWareIdV2(final Long roomId, final Long courseWareId) {

		String key = roomId.longValue() + "_" + courseWareId;
		try {
			return logCache.get(key, new Callable<SimpleCourseLiveBackLog>() {
				@Override
				public SimpleCourseLiveBackLog call() throws Exception {
					log.info("logCache.data.size:{}", logCache.size());
					return findFromDataBase();
				}

				/**
				 * 从数据库获取直播课件信息
				 * @return
				 */
				private SimpleCourseLiveBackLog findFromDataBase() {
					WeekendSqls<CourseLiveBackLog> courseLiveBackLogWeekendSql = WeekendSqls.<CourseLiveBackLog>custom()
							.andEqualTo(CourseLiveBackLog::getRoomId, roomId)
							.andEqualTo(CourseLiveBackLog::getLiveBackCoursewareId, courseWareId);
					final Example example = Example.builder(CourseLiveBackLog.class).where(courseLiveBackLogWeekendSql).build();
					List<CourseLiveBackLog> courseLiveBackLogList = selectByExample(example);
					if (CollectionUtils.isNotEmpty(courseLiveBackLogList)) {
						CourseLiveBackLog courseLiveBackLog = courseLiveBackLogList.get(0);
						log.debug("get live courseWareId from mysql:roomId:{}, courseWareId:{}", roomId, courseWareId);
						return SimpleCourseLiveBackLog.builder()
								.liveBackCourseWareId(courseLiveBackLog.getLiveBackCoursewareId())
								.liveCourseWareId(courseLiveBackLog.getLiveCoursewareId())
								.roomId(courseLiveBackLog.getRoomId()).build();
					} else {
						new Thread(() ->findFromPhp()).start();
						return SimpleCourseLiveBackLog.builder()
								.liveBackCourseWareId(courseWareId)
								.liveCourseWareId(null)
								.roomId(roomId).build();
					}
				}

				/**
				 * PHP 获取数据
				 * @return
				 */
				private SimpleCourseLiveBackLog findFromPhp(){
					StopWatch stopwatch = new StopWatch("调用 php 获取直播回放信息");
					stopwatch.start("start");
					log.info("new thread request live back info from php:{},{}", courseWareId, roomId);
					Map<String,Object> params = Maps.newHashMap();
					params.put("liveBackCoursewareId",courseWareId);
					params.put("roomId", roomId);
					log.info("course live back info request from php, courseWareId:{}, roomId:{}", courseWareId, roomId);
					CourseLiveBackLog courseLiveBackLog = null;
					NetSchoolResponse netSchoolResponse = lessonService.obtainLiveWareId(params);
					SimpleCourseLiveBackLog simpleCourseLiveBackLog = SimpleCourseLiveBackLog.builder()
							.liveBackCourseWareId(courseWareId).liveCourseWareId(null).roomId(roomId).build();
					if(ResponseUtil.isSuccess(netSchoolResponse)){
						LinkedHashMap<String,Object> result = (LinkedHashMap<String,Object>) netSchoolResponse.getData();
						long liveCourseWareId = MapUtils.getLong(result, "liveCoursewareId");
						if(liveCourseWareId != 0){
							courseLiveBackLog = new CourseLiveBackLog();
							courseLiveBackLog.setLiveBackCoursewareId(courseWareId);
							courseLiveBackLog.setRoomId(roomId);
							courseLiveBackLog.setLiveCoursewareId(liveCourseWareId);
							courseLiveBackLog.setLiveBackCoursewareId(courseWareId);
							courseLiveBackLog.setCreatorId(10L);
							insert(courseLiveBackLog);
							logCache.put(key, simpleCourseLiveBackLog);
							simpleCourseLiveBackLog.setLiveCourseWareId(liveCourseWareId);
							log.info("get live courseWareId from remote by rest:roomId:{}, coursewareId:{}", roomId, courseWareId);
						}
					}
					stopwatch.stop();
					log.info("调用 php 获取直播回放信息-结果:{}", JSONObject.toJSONString(simpleCourseLiveBackLog));
					return simpleCourseLiveBackLog;
				}
			});
		} catch (Exception e) {
			log.error("课后作业数据修正---  guava 获取直播课件信息异常:roomId:{}, wareId:{}, error:{}", roomId, courseWareId, e.getMessage());
			return SimpleCourseLiveBackLog.builder()
					.liveBackCourseWareId(courseWareId).liveCourseWareId(null).roomId(roomId).build();
		}
	}
}
