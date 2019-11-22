package com.huatu.tiku.course.service.v1.impl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.huatu.tiku.common.CourseQuestionTypeEnum.CourseType;
import com.huatu.tiku.course.bean.CourseBreakPointPracticeDto;
import com.huatu.tiku.course.bean.CourseBreakpointQuestionDTO;
import com.huatu.tiku.course.bean.practice.QuestionInfo;
import com.huatu.tiku.course.common.YesOrNoStatus;
import com.huatu.tiku.course.dao.manual.CourseBreakpointQuestionUserStatisticsMapper;
import com.huatu.tiku.course.dao.manual.CourseExercisesChoicesStatisticsMapper;
import com.huatu.tiku.course.dao.manual.CourseExercisesQuestionsStatisticsMapper;
import com.huatu.tiku.course.dao.manual.CourseExercisesStatisticsMapper;
import com.huatu.tiku.course.service.cache.CacheUtil;
import com.huatu.tiku.course.service.cache.CourseBreakpointCacheKey;
import com.huatu.tiku.course.service.v1.CourseBreakpointQuestionService;
import com.huatu.tiku.course.service.v1.CourseBreakpointService;
import com.huatu.tiku.course.service.v1.practice.QuestionInfoService;
import com.huatu.tiku.course.util.ZTKResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.question.QuestionServiceV1;
import com.huatu.tiku.entity.CourseBreakpointQuestion;
import com.huatu.tiku.entity.CourseBreakpointQuestionUserStatistics;
import com.huatu.tiku.entity.CourseExercisesChoicesStatistics;
import com.huatu.tiku.entity.CourseExercisesQuestionsStatistics;
import com.huatu.tiku.entity.CourseExercisesStatistics;
import com.huatu.tiku.entity.CourseLiveBackLog;

import lombok.extern.slf4j.Slf4j;
import service.impl.BaseServiceHelperImpl;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

/**
 * Created by lijun on 2018/6/20
 */
@Slf4j
@Service
public class CourseBreakpointQuestionServiceImpl extends BaseServiceHelperImpl<CourseBreakpointQuestion>
		implements CourseBreakpointQuestionService {

	public CourseBreakpointQuestionServiceImpl() {
		super(CourseBreakpointQuestion.class);
	}

	@Autowired
	private CacheUtil cacheUtil;

	@Autowired
	private QuestionServiceV1 questionService;

	@Autowired
	private CourseExercisesStatisticsMapper courseExercisesStatisticsMapper;

	@Autowired
	private CourseBreakpointService courseBreakpointService;

	@Autowired
	private QuestionInfoService questionInfoService;

	@Autowired
	private CourseBreakpointQuestionUserStatisticsMapper courseBreakpointQuestionUserStatisticsMapper;

	@Autowired
	private CourseExercisesQuestionsStatisticsMapper questionsStatisticsMapper;
	
	@Autowired
    private CourseExercisesChoicesStatisticsMapper choicesStatisticsMapper;

	@Override
	public List<Map<String, Object>> listQuestionIdByPointId(Long breakpointId) {
		// 1.生成key
		Supplier keySupplier = () -> CourseBreakpointCacheKey.breakpointQuestionKey(breakpointId);
		// 2.生成value
		Supplier<List<Map<String, Object>>> valueSupplier = () -> {
			WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
			sql.andEqualTo(CourseBreakpointQuestion::getBreakpointId, breakpointId);

			Example example = Example.builder(CourseBreakpointQuestion.class).where(sql).build();
			List<CourseBreakpointQuestion> courseBreakpointQuestionList = selectByExample(example);
			if (courseBreakpointQuestionList.size() == 0) {
				return null;
			}
			// 获取所有的questionId
			Stopwatch stopwatch = Stopwatch.createStarted();
			String questionIds = courseBreakpointQuestionList.stream().map(CourseBreakpointQuestion::getQuestionId)
					.map(String::valueOf).collect(Collectors.joining(","));
			Object listQuestionByIds = questionService.listQuestionByIds(questionIds);
			log.info("课程-断点-获取考题信息 ===》{}", stopwatch.elapsed(TimeUnit.MILLISECONDS));
			List<Map<String, Object>> result = (List<Map<String, Object>>) ZTKResponseUtil.build(listQuestionByIds);
			return result;
		};

		List<Map<String, Object>> cacheStringValue = cacheUtil.getCacheStringValue(keySupplier, valueSupplier, 5,
				TimeUnit.MINUTES);
		return cacheStringValue;
	}

	@Override
	public List<CourseBreakpointQuestion> listQuestionIdByBreakpointIdList(List<Long> list) {
		if (CollectionUtils.isEmpty(list)) {
			return Lists.newArrayList();
		}
		WeekendSqls<CourseBreakpointQuestion> sql = WeekendSqls.custom();
		sql.andIn(CourseBreakpointQuestion::getBreakpointId, list);
		Example example = Example.builder(CourseBreakpointQuestion.class).where(sql).orderByAsc("pptIndex", "sort")
				.build();
		List<CourseBreakpointQuestion> questions = selectByExample(example);
		return questions;
	}

	/**
	 * 持久化录播随堂练
	 */
	@Override
	public void saveCourseBreakpointPracticeInfo(List<CourseBreakPointPracticeDto> payload) {
		payload.forEach(practice -> {
			List<QuestionInfo> baseQuestionInfoList = questionInfoService
					.getBaseQuestionInfo(Arrays.asList(practice.getQuestionId().longValue()));

			if (CollectionUtils.isNotEmpty(baseQuestionInfoList)) {
				QuestionInfo questionInfo = baseQuestionInfoList.get(0);

				// 更新考试统计表
				Example example = new Example(CourseExercisesStatistics.class);
				example.and().andEqualTo("courseId", practice.getCourseWareId())
						.andEqualTo("courseType", CourseType.RECORD.getCode())
						.andEqualTo("status", YesOrNoStatus.YES.getCode())
						// 1为随堂练
						.andEqualTo("type", YesOrNoStatus.YES.getCode());
				CourseExercisesStatistics courseExercisesStatistics = courseExercisesStatisticsMapper
						.selectOneByExample(example);

				if (null == courseExercisesStatistics) {
					// 获取课件绑定的试题数量
					List<CourseBreakpointQuestionDTO> quesitionList = courseBreakpointService
							.listAllQuestionId(CourseType.RECORD.getCode(), practice.getCourseWareId());
					courseExercisesStatistics = new CourseExercisesStatistics();
					courseExercisesStatistics.setStatus(YesOrNoStatus.YES.getCode());
					courseExercisesStatistics.setCorrects(practice.getCorrect() == 1 ? 1 : 0);
					courseExercisesStatistics.setCosts(practice.getTime());
					courseExercisesStatistics.setCounts(1);
					courseExercisesStatistics.setQuestionCount(quesitionList.size());
					courseExercisesStatistics.setCourseType(CourseType.RECORD.getCode());
					courseExercisesStatistics.setCourseId(practice.getCourseWareId().longValue());
					courseExercisesStatistics.setGmtModify(new Timestamp(System.currentTimeMillis()));
					courseExercisesStatistics.setGmtCreate(new Timestamp(System.currentTimeMillis()));
					courseExercisesStatisticsMapper.insertSelective(courseExercisesStatistics);
				} else {
					// 获取该随堂练是否做过
					WeekendSqls<CourseBreakpointQuestionUserStatistics> weekendSqls = WeekendSqls
							.<CourseBreakpointQuestionUserStatistics>custom()
							.andEqualTo(CourseBreakpointQuestionUserStatistics::getCourseId, practice.getCourseWareId())
							.andEqualTo(CourseBreakpointQuestionUserStatistics::getUserId, practice.getUserId());
					Example userStatisticsexample = Example.builder(CourseBreakpointQuestionUserStatistics.class)
							.where(weekendSqls).build();
					int count = courseBreakpointQuestionUserStatisticsMapper
							.selectCountByExample(userStatisticsexample);

					courseExercisesStatistics.setCounts(count > 0 ? courseExercisesStatistics.getCounts()
							: (courseExercisesStatistics.getCounts() + 1));
					courseExercisesStatistics.setCosts(courseExercisesStatistics.getCosts() + practice.getTime());
					courseExercisesStatistics
							.setCorrects(courseExercisesStatistics.getCorrects() + practice.getCorrect() == 1 ? 1 : 0);
					courseExercisesStatistics.setGmtModify(new Timestamp(System.currentTimeMillis()));
					courseExercisesStatisticsMapper.updateByPrimaryKeySelective(courseExercisesStatistics);

				}
				// 保存用户做题记录表
				CourseBreakpointQuestionUserStatistics courseBreakpointQuestionUserStatistics = CourseBreakpointQuestionUserStatistics
						.builder().courseId(practice.getCourseWareId().longValue()).userId(practice.getUserId())
						.questionId(practice.getQuestionId().longValue()).correct(practice.getCorrect()).build();
				courseBreakpointQuestionUserStatisticsMapper.insertSelective(courseBreakpointQuestionUserStatistics);
				// 判断是否有本题统计信息
				WeekendSqls<CourseExercisesQuestionsStatistics> weekendSqlsStatistics = WeekendSqls
						.<CourseExercisesQuestionsStatistics>custom()
						.andEqualTo(CourseExercisesQuestionsStatistics::getStatisticsId,
								courseExercisesStatistics.getId())
						.andEqualTo(CourseExercisesQuestionsStatistics::getQuestionId,
								practice.getQuestionId().longValue());
				Example statisticsexample = Example.builder(CourseExercisesQuestionsStatistics.class)
						.where(weekendSqlsStatistics).build();
				List<CourseExercisesQuestionsStatistics> questionList = questionsStatisticsMapper
						.selectByExample(statisticsexample);
				if (questionList.size() > 0) {
					CourseExercisesQuestionsStatistics courseExercisesQuestionsStatistics = questionList.get(0);
					if (practice.getCorrect() == 1) {
						// 正确数加1
						courseExercisesQuestionsStatistics
								.setCorrects(courseExercisesQuestionsStatistics.getCorrects() + 1);
					}
					courseExercisesQuestionsStatistics.setCounts(courseExercisesQuestionsStatistics.getCounts() + 1);
					questionsStatisticsMapper.updateByPrimaryKeySelective(courseExercisesQuestionsStatistics);
					//更新选项表
		            Stream.of(practice.getAnswer().toCharArray()).forEach(item ->{
						int choice = Integer.parseInt(item + "");
		                WeekendSqls<CourseExercisesChoicesStatistics> weekendSqlsChoicesStatistics = WeekendSqls
								.<CourseExercisesChoicesStatistics>custom()
								.andEqualTo(CourseExercisesChoicesStatistics::getQuestionId,
										courseExercisesQuestionsStatistics.getId())
								.andEqualTo(CourseExercisesChoicesStatistics::getChoice,
										choice);
						Example statisticsChoiceseExample = Example.builder(CourseExercisesChoicesStatistics.class)
								.where(weekendSqlsChoicesStatistics).build();
						List<CourseExercisesChoicesStatistics> selectByChoices = choicesStatisticsMapper.selectByExample(statisticsChoiceseExample);
						if(selectByChoices.size()>0) {
							CourseExercisesChoicesStatistics courseExercisesChoicesStatistics = selectByChoices.get(0);
							courseExercisesChoicesStatistics.setCounts(courseExercisesChoicesStatistics.getCounts()+1);
							choicesStatisticsMapper.updateByPrimaryKeySelective(courseExercisesChoicesStatistics);
						}
		            });
					
					
				} else {

					// 保存试题统计表
					CourseExercisesQuestionsStatistics questionsStatistics = new CourseExercisesQuestionsStatistics();
					questionsStatistics.setCorrects(0);
					questionsStatistics.setCounts(0);
					questionsStatistics.setStatus(YesOrNoStatus.YES.getCode());
					questionsStatistics.setStatisticsId(courseExercisesStatistics.getId());
					questionsStatistics.setQuestionId(practice.getQuestionId().longValue());
					questionsStatistics.setGmtCreate(new Timestamp(System.currentTimeMillis()));
					questionsStatistics.setGmtModify(new Timestamp(System.currentTimeMillis()));
					questionsStatisticsMapper.insertSelective(questionsStatistics);
					//初始化选项统计信息
					for (int choice = 1; choice <= questionInfo.getChoiceList().size(); choice ++){
			            CourseExercisesChoicesStatistics courseExercisesChoicesStatistics = new CourseExercisesChoicesStatistics();
			            courseExercisesChoicesStatistics.setQuestionId(questionsStatistics.getId());
			            courseExercisesChoicesStatistics.setChoice(choice);
			            if(practice.getAnswer().indexOf(choice) != -1) {
			            	courseExercisesChoicesStatistics.setCounts(1);
			            }else {
			            	courseExercisesChoicesStatistics.setCounts(0);
			            }
			            courseExercisesChoicesStatistics.setStatus(YesOrNoStatus.YES.getCode());
			            courseExercisesChoicesStatistics.setGmtCreate(new Timestamp(System.currentTimeMillis()));
			            courseExercisesChoicesStatistics.setGmtModify(new Timestamp(System.currentTimeMillis()));
			            choicesStatisticsMapper.insertSelective(courseExercisesChoicesStatistics);
			        }
				}
			}

		});
	}
}
