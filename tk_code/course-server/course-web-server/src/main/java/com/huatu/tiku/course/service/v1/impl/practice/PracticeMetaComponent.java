package com.huatu.tiku.course.service.v1.impl.practice;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.bean.practice.PracticeRoomRankUserBo;
import com.huatu.tiku.course.bean.practice.PracticeUserQuestionMetaInfoBo;
import com.huatu.tiku.course.bean.practice.QuestionInfo;
import com.huatu.tiku.course.bean.practice.QuestionMetaBo;
import com.huatu.tiku.course.bean.practice.StudentQuestionMetaBo;
import com.huatu.tiku.course.service.cache.CoursePracticeCacheKey;
import com.huatu.tiku.course.service.v1.practice.QuestionInfoService;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * Created by lijun on 2019/2/27
 */
@Component
@Slf4j
public class PracticeMetaComponent {

	// 试题信息统计 存放时间的Key
	private static final String QUESTION_TOTAL_TIME_KEY = "0";

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	@Qualifier(value = "persistTemplate")
	private RedisTemplate persistTemplate;

	@Autowired
	private QuestionInfoService questionInfoService;

	/**
	 * 构建用户统计 缓存信息
	 */
	public void buildUserQuestionMeta(Integer userId, Long courseId, Long questionId, String answer, Integer time,
			Integer correct) {
		final HashOperations<String, String, PracticeUserQuestionMetaInfoBo> hashOperations = redisTemplate
				.opsForHash();
		String key = CoursePracticeCacheKey.userMetaKey(userId, courseId);
		final PracticeUserQuestionMetaInfoBo metaInfo = PracticeUserQuestionMetaInfoBo.builder().time(time)
				.answer(answer).correct(correct).build();
		hashOperations.put(key, String.valueOf(questionId), metaInfo);
		persistTemplate.expire(key, CoursePracticeCacheKey.getDefaultKeyTTL(),
				CoursePracticeCacheKey.getDefaultTimeUnit());
	}

	/**
	 * 获取用户在该课件下的答题数据
	 *
	 * @param userId   用户ID
	 * @param courseId 课程ID
	 * @return
	 */
	public PracticeRoomRankUserBo getPracticeRoomRankUser(Integer userId, Long courseId) {
		final HashOperations<String, String, PracticeUserQuestionMetaInfoBo> hashOperations = redisTemplate
				.opsForHash();
		String key = CoursePracticeCacheKey.userMetaKey(userId, courseId);
		final Map<String, PracticeUserQuestionMetaInfoBo> entries = hashOperations.entries(key);
		if (null == entries) {
			return null;
		}
		final Collection<PracticeUserQuestionMetaInfoBo> values = entries.values();
		Integer totalTime = values.stream().map(PracticeUserQuestionMetaInfoBo::getTime).reduce(0, (a, b) -> a + b);
		Integer totalScore = values.stream()
				.map(practiceUserQuestionMetaInfoBo -> practiceUserQuestionMetaInfoBo.getCorrect() == 1 ? 2 : 0)
				.reduce(0, (a, b) -> a + b);
		return PracticeRoomRankUserBo.builder().id(userId).totalScore(totalScore).totalTime(totalTime).build();
	}

	/**
	 * 构建 房间统计信息
	 */
	public void buildRoomRank(Long roomId, Long courseId, Integer userId, String userName, Long questionId,
			String answer, Integer time, Integer correct) {
		// 先校验当前数据是否存在
		final PracticeRoomRankUserBo practiceRoomRankUserBo = getPracticeRoomRankUser(userId, courseId);
		if (null == practiceRoomRankUserBo) {
			buildUserQuestionMeta(userId, courseId, questionId, answer, time, correct);
		}
		final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		final String key = CoursePracticeCacheKey.roomRankKey(roomId);
		practiceRoomRankUserBo.setName(userName);
		// 此处不能有统计信息，否则 zSet 中会出现多个数据,使用ID-NAME 作为用户唯一标识
		final HashMap<String, Object> map = HashMapBuilder.<String, Object>newBuilder()
				.put("id", practiceRoomRankUserBo.getId()).put("name", practiceRoomRankUserBo.getName())
				// 此处加上 课程ID，在后续持久化使用
				.put("courseId", courseId).build();
		zSetOperations.add(key, JSONObject.toJSONString(map), practiceRoomRankUserBo.buildRankInfo());
		redisTemplate.expire(key, CoursePracticeCacheKey.getDefaultKeyTTL(),
				CoursePracticeCacheKey.getDefaultTimeUnit());
	}

	/**
	 * 获取统计信息
	 */
	public List<PracticeRoomRankUserBo> getRoomRankInfo(Long roomId, Integer start, Integer end) {
		final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		final String key = CoursePracticeCacheKey.roomRankKey(roomId);
		// 倒叙排序从高到低返回
		final Set<ZSetOperations.TypedTuple<String>> typedTupleSet = zSetOperations.reverseRangeWithScores(key, start,
				end);

		List<PracticeRoomRankUserBo> result = typedTupleSet.stream().map(typedTuple -> {
			int totalTimereverse = typedTuple.getScore().intValue() % 10000;
			int totalTime = 10000 - totalTimereverse;
			int totalScore = (typedTuple.getScore().intValue() - totalTimereverse) / 10000;
			final JSONObject jsonObject = JSONObject.parseObject(typedTuple.getValue());
			return PracticeRoomRankUserBo.builder().id(jsonObject.getInteger("id")).name(jsonObject.getString("name"))
					.courseId(jsonObject.getLong("courseId")).totalTime(totalTime)
					.totalScore(totalScore < 0 ? 0 : totalScore).build();
			// 学生端会排除积分为0的
		}).collect(Collectors.toList());
		return result;
	}

	/**
	 * 当前排名获取总数量
	 */
	public Long getRoomRankTotalInfo(Long roomId) {
		final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		final String key = CoursePracticeCacheKey.roomRankKey(roomId);
		return zSetOperations.size(key);
	}

	/**
	 * 构建试题统计信息 '0' 存储消耗总时间
	 */
	public void buildQuestionMeta(Long roomId, Long questionId, String answer, Integer time) {
		if (StringUtils.isBlank(answer)) {
			return;
		}
		final HashOperations<String, String, Integer> hashOperations = redisTemplate.opsForHash();
		final String key = CoursePracticeCacheKey.questionMetaKey(roomId, questionId);
		hashOperations.increment(key, QUESTION_TOTAL_TIME_KEY, time);
		hashOperations.increment(key, answer, 1);
		redisTemplate.expire(key, CoursePracticeCacheKey.getDefaultKeyTTL(),
				CoursePracticeCacheKey.getDefaultTimeUnit());
	}

	/**
	 * 获取试题统计信息
	 */
	public QuestionMetaBo getQuestionMetaBo(Long roomId, Long questionId) {
		final HashOperations<String, String, Integer> hashOperations = redisTemplate.opsForHash();
		final String key = CoursePracticeCacheKey.questionMetaKey(roomId, questionId);
		// 当前试题未答
		final Integer totalTime = hashOperations.get(key, QUESTION_TOTAL_TIME_KEY);
		// 当前试题信息不存在
		List<QuestionInfo> baseQuestionInfoList = questionInfoService
				.getBaseQuestionInfo(Lists.newArrayList(questionId));
		if (CollectionUtils.isEmpty(baseQuestionInfoList)) {
			return new QuestionMetaBo();
		}
		final QuestionInfo questionInfo = baseQuestionInfoList.get(0);

		if (null == totalTime || totalTime == 0) {
			return QuestionMetaBo.builder().id(questionInfo.getId()).answer(questionInfo.getAnswer()).percents(new int[0]).type(questionInfo.getType()).build();
		}
		final int[] answerCountNum = new int[questionInfo.getChoiceList().size()];
		final Set<Map.Entry<String, Integer>> entrySet = hashOperations.entries(key).entrySet();
		// 获取A/B/C/D 各个选项的数量
		entrySet.stream().forEach(entry -> {
			// 只统计A、B、C、D 四个选项  统计本题中所有选项
			IntStream.rangeClosed(1, questionInfo.getChoiceList().size()).forEach(index -> {
				if (entry.getKey().contains(String.valueOf(index))) {
					answerCountNum[index - 1] += entry.getValue();
				}
			});
		});
		// 各个试题选择数量为百分比
		int sum;
		if ((sum = Arrays.stream(answerCountNum).sum()) != 0) {
			// 最后一个选项单独计算 保证所有的加起来是百分之百
			int totalExpendLast = 0;
			for (int index = 0; index < answerCountNum.length - 1; index++) {
				answerCountNum[index] = (answerCountNum[index] * 100 / sum);
				totalExpendLast += answerCountNum[index];
			}
			answerCountNum[answerCountNum.length - 1] = 100 - totalExpendLast;
		}
		// 获取所有的答案总数量 - 需要考虑多选题的问题
		final Integer totalCount = entrySet.stream().filter(entry -> !entry.getKey().equals(QUESTION_TOTAL_TIME_KEY))
				.map(Map.Entry::getValue).reduce(0, (a, b) -> a + b);
		// 计算正确率 - 此处如果是多选题前端无法自行计算
		double correctCate = 0d;
		final Optional<Map.Entry<String, Integer>> correctInfo = entrySet.stream()
				.filter(entry -> {
					String answers = entry.getKey();
					char[] chars = answers.toCharArray();
					Arrays.sort(chars);
					String sortedAnswers = new String(chars);
					if (sortedAnswers.equals(questionInfo.getAnswer())) {
						return true;
					}
					return false;
				}).findFirst();
		if (correctInfo.isPresent()) {
			correctCate = ((double) correctInfo.get().getValue() / totalCount) * 100;
			//保留位数
			correctCate = new BigDecimal(correctCate).setScale(2,  BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		// 矫正最终的正确率，保证单选题的时候 选项选择率和正确率一致
		if (questionInfo.getAnswer().length() == 1 && Integer.valueOf(questionInfo.getAnswer()) < answerCountNum.length
				&& answerCountNum[Integer.valueOf(questionInfo.getAnswer()) - 1] != correctCate) {
			correctCate = answerCountNum[Integer.valueOf(questionInfo.getAnswer()) - 1];
		}
		return QuestionMetaBo.builder().id(questionInfo.getId()).answer(questionInfo.getAnswer())
				.avgTime(totalTime / totalCount).count(totalCount).percents(answerCountNum).correctCate(correctCate).type(questionInfo.getType())
				.build();
	}

	/**
	 * 获取学员统计信息
	 */
	public StudentQuestionMetaBo getStudentQuestionMetaBo(Integer userId, Long roomId, Long courseId, Long questionId) {
		final StudentQuestionMetaBo studentQuestionMetaBo = new StudentQuestionMetaBo();
		QuestionMetaBo questionMetaBo = getQuestionMetaBo(roomId, questionId);
		// 构建基础的统计信息
		BeanUtils.copyProperties(questionMetaBo, studentQuestionMetaBo);
		// 构建用户的基础信息
		final HashOperations<String, String, PracticeUserQuestionMetaInfoBo> hashOperations = redisTemplate
				.opsForHash();
		String key = CoursePracticeCacheKey.userMetaKey(userId, courseId);
		final Map<String, PracticeUserQuestionMetaInfoBo> entries = hashOperations.entries(key);
		if (null != entries) {
			// 已经回答总数
			int totalCount = entries.values().size();
			// 答对数量
			long rightCount = entries.values().stream().filter(value -> value.getCorrect() == 1).count();
			studentQuestionMetaBo.setUserQuestionNum(totalCount);
			studentQuestionMetaBo.setRightQuestionNum((int) rightCount);
			// 答题时间 - 答案
			int time;
			Optional<Map.Entry<String, PracticeUserQuestionMetaInfoBo>> infoBoEntry = entries.entrySet().stream()
					.filter(entry -> entry.getKey().equals(String.valueOf(questionId))).findAny();
			if (infoBoEntry.isPresent()) {
				PracticeUserQuestionMetaInfoBo value = infoBoEntry.get().getValue();
				time = value.getTime();
				String answer = value.getAnswer();
				studentQuestionMetaBo.setTime(time);
				studentQuestionMetaBo.setUserAnswer(answer);
			}
		}
		return studentQuestionMetaBo;
	}

	/**
	 * 添加已练习试题数量
	 *
	 * @param roomId     房间ID
	 * @param questionId 试题ID
	 */
	public void addRoomPracticedQuestion(Long roomId, Long questionId) {
		final SetOperations<String, String> setOperations = redisTemplate.opsForSet();
		final String key = CoursePracticeCacheKey.roomPractedQuestionNumKey(roomId);
		setOperations.add(key, questionId.toString());
		redisTemplate.expire(key, CoursePracticeCacheKey.getDefaultKeyTTL(),
				CoursePracticeCacheKey.getDefaultTimeUnit());
	}

	/**
	 * 获取所有已经练习试题
	 *
	 * @param roomId 房间ID
	 */
	public List<String> getRoomPracticedQuestion(Long roomId) {
		final SetOperations<String, String> setOperations = persistTemplate.opsForSet();
		final String key = CoursePracticeCacheKey.roomPractedQuestionNumKey(roomId);
		Set<String> set = setOperations.members(key);
		return set.stream().collect(Collectors.toList());
	}

	/**
	 * 构建统计信息
	 *
	 * @param roomId     房间ID
	 * @param userId     用户ID
	 * @param courseId   课件ID
	 * @param questionId 试题ID
	 * @param answer     用户答案
	 * @param time       用户答题耗时
	 * @param correct    是否正确 1 正确，2 错误
	 */
	public void buildMetaInfo(Integer userId, String userName, Long roomId, Long courseId, Long questionId,
			String answer, Integer time, Integer correct) {
		buildUserQuestionMeta(userId, courseId, questionId, answer, time, correct);
		buildQuestionMeta(roomId, questionId, answer, time);
		buildRoomRank(roomId, courseId, userId, userName, questionId, answer, time, correct);
		//构建总答题题数和作答人数
		buildRoomRightQuestionSum(courseId, courseId,correct);
		//创建课件中试题作答情况 答案 用时
		buildCourseQuestionMeta(roomId,courseId, questionId, answer, time);
	}

	/**
	 * 构建房间所有答题用户id+课程id set集合 用来直播下课时生成答题卡信息
	 * 
	 * @param userId
	 * @param roomId
	 * @param courseId
	 */
	public void setRoomInfoMeta(Integer userId, Long roomId, Long courseId) {
		final SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
		final String userMetaKey = CoursePracticeCacheKey.userMetaKey(userId, courseId);
		final String key = CoursePracticeCacheKey.roomInfoMetaKey(roomId);
		opsForSet.add(key, userMetaKey);
		redisTemplate.expire(key, CoursePracticeCacheKey.getDefaultKeyTTL(),
				CoursePracticeCacheKey.getDefaultTimeUnit());
	}

	/**
	 * 获取房间内所有答题用户set集合 set中成员即为userMetaKey中key
	 * 
	 * @param roomId
	 */
	public List<String> getRoomInfoMeta(Long roomId) {
		final SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
		final String key = CoursePracticeCacheKey.roomInfoMetaKey(roomId);
		Set<String> hashkeys = opsForSet.members(key);
		return hashkeys.stream().collect(Collectors.toList());
	}
	
	/**
	 * 存储随堂课某个课下答对题的题数和作答总人数用来统计正确率
	 * @param courseId
	 */
	public void buildRoomRightQuestionSum(Long courseId, Long userId, Integer correct) {
		final String key = CoursePracticeCacheKey.roomRightQuestionSum(courseId);
		if (2 == correct) {
			// 使用持久化存储数据
			persistTemplate.opsForValue().increment(key, 1);// 设置答对题数
		}
		// 使用持久化存储数据
		final SetOperations<String, Long> opsForSet = persistTemplate.opsForSet();
		
		final String allUserSumKey = CoursePracticeCacheKey.roomAllUserSum(courseId);
		// 设置作答总人数
		// 使用持久化存储数据
		opsForSet.add(allUserSumKey, userId);

	}

	/**
	 * 构建课件试题统计信息 '0' 存储消耗总时间
	 */
	public void buildCourseQuestionMeta(Long roomId,Long coursewareId, Long questionId, String answer, Integer time) {
		if (StringUtils.isBlank(answer)) {
			return;
		}
		final HashOperations<String, String, Integer> hashOperations = redisTemplate.opsForHash();
		final String key = CoursePracticeCacheKey.questionCoursewareMetaKey(roomId,coursewareId, questionId);
		hashOperations.increment(key, QUESTION_TOTAL_TIME_KEY, time);
		hashOperations.increment(key, answer, 1);
		redisTemplate.expire(key, CoursePracticeCacheKey.getDefaultKeyTTL(),
				CoursePracticeCacheKey.getDefaultTimeUnit());
	}

	/**
	 * 获取课件试题统计信息
	 */
	public QuestionMetaBo getCourseQuestionMetaBo(Long roomId,Long coursewareId, Long questionId) {
		final HashOperations<String, String, Integer> hashOperations = redisTemplate.opsForHash();
		final String key = CoursePracticeCacheKey.questionCoursewareMetaKey(roomId,coursewareId, questionId);
		// 当前试题未答
		final Integer totalTime = hashOperations.get(key, QUESTION_TOTAL_TIME_KEY);
		// 当前试题信息不存在
		List<QuestionInfo> baseQuestionInfoList = questionInfoService
				.getBaseQuestionInfo(Lists.newArrayList(questionId));
		if (CollectionUtils.isEmpty(baseQuestionInfoList)) {
			return new QuestionMetaBo();
		}
		final QuestionInfo questionInfo = baseQuestionInfoList.get(0);

		if (null == totalTime || totalTime == 0) {
			return QuestionMetaBo.builder().id(questionInfo.getId()).answer(questionInfo.getAnswer()).build();
		}
		final int[] answerCountNum = new int[questionInfo.getChoiceList().size()];
		final Set<Map.Entry<String, Integer>> entrySet = hashOperations.entries(key).entrySet();
		// 获取A/B/C/D 各个选项的数量
		entrySet.stream().forEach(entry -> {
			// 只统计A、B、C、D 四个选项  统计本题中所有选项
			IntStream.rangeClosed(1, questionInfo.getChoiceList().size()).forEach(index -> {
				if (entry.getKey().contains(String.valueOf(index))) {
					answerCountNum[index - 1] += entry.getValue();
				}
			});
		});
		// 各个试题选择数量为百分比
		int sum;
		if ((sum = Arrays.stream(answerCountNum).sum()) != 0) {
			// 最后一个选项单独计算 保证所有的加起来是百分之百
			int totalExpendLast = 0;
			for (int index = 0; index < answerCountNum.length - 1; index++) {
				answerCountNum[index] = (answerCountNum[index] * 100 / sum);
				totalExpendLast += answerCountNum[index];
			}
			answerCountNum[answerCountNum.length - 1] = 100 - totalExpendLast;
		}
		// 获取所有的答案总数量 - 需要考虑多选题的问题
		final Integer totalCount = entrySet.stream().filter(entry -> !entry.getKey().equals(QUESTION_TOTAL_TIME_KEY))
				.map(Map.Entry::getValue).reduce(0, (a, b) -> a + b);
		// 计算正确率 - 此处如果是多选题前端无法自行计算
		double correctCate = 0d;
		final Optional<Map.Entry<String, Integer>> correctInfo = entrySet.stream()
				.filter(entry -> {
					String answers = entry.getKey();
					char[] chars = answers.toCharArray();
					Arrays.sort(chars);
					String sortedAnswers = new String(chars);
					if (sortedAnswers.equals(questionInfo.getAnswer())) {
						return true;
					}
					return false;
				}).findFirst();
		if (correctInfo.isPresent()) {
			correctCate = ((double) correctInfo.get().getValue() / totalCount) * 100;
		}
		// 矫正最终的正确率，保证单选题的时候 选项选择率和正确率一致
		if (questionInfo.getAnswer().length() == 1 && Integer.valueOf(questionInfo.getAnswer()) < answerCountNum.length
				&& answerCountNum[Integer.valueOf(questionInfo.getAnswer()) - 1] != correctCate) {
			correctCate = answerCountNum[Integer.valueOf(questionInfo.getAnswer()) - 1];
		}

		return QuestionMetaBo.builder().id(questionInfo.getId()).answer(questionInfo.getAnswer())
				.avgTime(totalTime / totalCount).count(totalCount).percents(answerCountNum).correctCate(correctCate).type(questionInfo.getType())
				.build();
	}

	/**
	 * 检查用户是否作答本题(已作答返回false)
	 * @param userId
	 * @param courseId
	 * @param questionId
	 */
	public boolean checkUserHasAnswer(Integer userId, Long courseId, Long questionId) {
		final HashOperations<String, String, PracticeUserQuestionMetaInfoBo> hashOperations = redisTemplate
				.opsForHash();
		String key = CoursePracticeCacheKey.userMetaKey(userId, courseId);
		PracticeUserQuestionMetaInfoBo practiceUserQuestionMetaInfoBo = hashOperations.get(key, String.valueOf(questionId));
		if(practiceUserQuestionMetaInfoBo != null) {
			return false;
		}
		return true;
		
	}
}
