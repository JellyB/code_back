package com.huatu.tiku.course.service.v1.impl.practice;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.CourseQuestionTypeEnum;
import com.huatu.tiku.course.bean.practice.PracticeRoomRankUserBo;
import com.huatu.tiku.course.bean.practice.QuestionInfo;
import com.huatu.tiku.course.bean.practice.QuestionMetaBo;
import com.huatu.tiku.course.bean.practice.TeacherQuestionBo;
import com.huatu.tiku.course.common.CoursePracticeQuestionInfoEnum;
import com.huatu.tiku.course.service.cache.CoursePracticeCacheKey;
import com.huatu.tiku.course.service.v1.CourseBreakpointService;
import com.huatu.tiku.course.service.v1.practice.*;
import com.huatu.tiku.entity.CourseBreakpointQuestion;
import com.huatu.tiku.entity.CoursePracticeQuestionInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2019/2/21
 */
@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final LiveCourseRoomInfoService liveCourseRoomInfoService;

    private final CourseBreakpointService courseBreakpointService;
    private final CoursePracticeQuestionInfoService coursePracticeQuestionInfoService;

    private final QuestionInfoService questionInfoService;
    private final PracticeMetaComponent practiceMetaComponent;

    @Autowired
    @Qualifier(value = "persistTemplate")
    private RedisTemplate persistTemplate;

    @Autowired
    private CoursewarePracticeQuestionInfoService coursewarePracticeQuestionInfoService;
    @Override
    public Map<String,Object> getQuestionInfoByRoomId(Long roomId) throws ExecutionException, InterruptedException {
    	Map<String,Object> retMap = Maps.newHashMap();
        List<Integer> liveCourseIdListByRoomId = liveCourseRoomInfoService.getLiveCourseIdListByRoomId(roomId);
        if (CollectionUtils.isEmpty(liveCourseIdListByRoomId)) {
            return retMap;
        }
        //由于各个课件对应的试题信息肯定一致，此处只需要获取一个
       final Long courseId = Long.valueOf(liveCourseIdListByRoomId.get(0));
       // final Long courseId = 938040L;
        List<CourseBreakpointQuestion> courseBreakpointQuestionList = courseBreakpointService.listQuestionByCourseTypeAndId(CourseQuestionTypeEnum.CourseType.LIVE.getCode(), courseId);
        if (CollectionUtils.isEmpty(courseBreakpointQuestionList)) {
        	return retMap;
        }
        //获取所有试题的状态
        final List<Long> questionIdList = courseBreakpointQuestionList.stream()
                .map(CourseBreakpointQuestion::getQuestionId)
                .collect(Collectors.toList());
        //查询
        ListenableFuture<List<CoursePracticeQuestionInfo>> coursePracticeQuestionInfoByRoomId = getAsyncCoursePracticeQuestionInfoByRoomId(roomId, questionIdList);
        ListenableFuture<List<QuestionInfo>> baseQuestionInfoByRoomId = getAsyncBaseQuestionInfo(questionIdList);
        final CountDownLatch questionSearchTask = new CountDownLatch(2);

        Consumer<ListenableFuture> addCallBack = future ->
                future.addCallback(new ListenableFutureCallback<List>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        questionSearchTask.countDown();
                    }

                    @Override
                    public void onSuccess(List result) {
                        questionSearchTask.countDown();
                    }
                });
        addCallBack.accept(coursePracticeQuestionInfoByRoomId);
        addCallBack.accept(baseQuestionInfoByRoomId);
        questionSearchTask.await(3, TimeUnit.SECONDS);
        //获取两端数据
        List<QuestionInfo> baseQuestionInfoList = baseQuestionInfoByRoomId.get();
        List<CoursePracticeQuestionInfo> coursePracticeQuestionInfoList = coursePracticeQuestionInfoByRoomId.get();
		List<TeacherQuestionBo> teacherQuestionBoList = combinationQuestionInfo(baseQuestionInfoList,
				courseBreakpointQuestionList, coursePracticeQuestionInfoList);
		return ImmutableMap.of("courseId", courseId, "teacherQuestionBoList", teacherQuestionBoList);
    }


    /**
     * 通过 试题ID 获取试题详情
     */
    @Async
    public ListenableFuture<List<QuestionInfo>> getAsyncBaseQuestionInfo(List<Long> questionIdList) {
        return new AsyncResult<>(questionInfoService.getBaseQuestionInfo(questionIdList));
    }

    /**
     * 通过 roomId 获取试题统计详情
     */
    @Async
    public ListenableFuture<List<CoursePracticeQuestionInfo>> getAsyncCoursePracticeQuestionInfoByRoomId(Long roomId, List<Long> questionIdList) {
        if (CollectionUtils.isEmpty(questionIdList)) {
            return new AsyncResult<>(Lists.newArrayList());
        }
        List<CoursePracticeQuestionInfo> questionInfoList = coursePracticeQuestionInfoService.listByRoomIdAndQuestionId(roomId, questionIdList);
        return new AsyncResult<>(questionInfoList);
    }

    @Override
    public void saveQuestionPracticeInfo(Long roomId, Long questionId, Integer practiceTime) {
        CoursePracticeQuestionInfo coursePracticeQuestionInfo = getCoursePracticeQuestionInfoByRoomIdAndQuestionId(roomId, questionId);
        if (null != coursePracticeQuestionInfo
                && null != coursePracticeQuestionInfo.getStartPracticeTime()
                && 0 != coursePracticeQuestionInfo.getStartPracticeTime()) {
            throw new BizException(ErrorResult.create(5000000, "该试题已经开始考试"));
        }
		if (coursePracticeQuestionInfo == null) {
			coursePracticeQuestionInfo = CoursePracticeQuestionInfo.builder().roomId(roomId)
					.questionId(questionId.intValue()).startPracticeTime(System.currentTimeMillis())
					.practiceTime(practiceTime).build();
		}
		coursePracticeQuestionInfo.setBizStatus(CoursePracticeQuestionInfoEnum.FINISH.getStatus());
		coursePracticeQuestionInfo.setStartPracticeTime(System.currentTimeMillis());
		// 添加房间练习题数量到缓存
		practiceMetaComponent.addRoomPracticedQuestion(roomId, questionId);
        coursePracticeQuestionInfoService.save(coursePracticeQuestionInfo);
    }

    @Override
    public void updateQuestionPracticeTime(Long roomId, Long questionId, Integer practiceTime) {
        CoursePracticeQuestionInfo coursePracticeQuestionInfo = getCoursePracticeQuestionInfoByRoomIdAndQuestionId(roomId, questionId);
        if (coursePracticeQuestionInfo == null) {
            final CoursePracticeQuestionInfo info = CoursePracticeQuestionInfo.builder()
                    .roomId(roomId)
                    .questionId(questionId.intValue())
                    .practiceTime(practiceTime)
                    .build();
            //bizstatus 默认为1
            coursePracticeQuestionInfoService.save(info);
        } else {
            coursePracticeQuestionInfo.setPracticeTime(practiceTime);
            coursePracticeQuestionInfoService.save(coursePracticeQuestionInfo);
        }
    }

    private CoursePracticeQuestionInfo getCoursePracticeQuestionInfoByRoomIdAndQuestionId(Long roomId, Long questionId) {
        WeekendSqls<CoursePracticeQuestionInfo> weekendSql = WeekendSqls.<CoursePracticeQuestionInfo>custom()
                .andEqualTo(CoursePracticeQuestionInfo::getRoomId, roomId)
                .andEqualTo(CoursePracticeQuestionInfo::getQuestionId, questionId);
        Example example = Example.builder(CoursePracticeQuestionInfo.class)
                .where(weekendSql)
                .build();
        final List<CoursePracticeQuestionInfo> practiceQuestionInfoList = coursePracticeQuestionInfoService.selectByExample(example);
        if (CollectionUtils.isEmpty(practiceQuestionInfoList)) {
            return null;
        }
        return practiceQuestionInfoList.get(0);
    }

    @Override
    public QuestionMetaBo getQuestionStatisticsByRoomIdAndQuestionId(Long roomId, Long questionId) throws ExecutionException, InterruptedException {
        ListenableFuture<List<CoursePracticeQuestionInfo>> asyncCoursePracticeQuestionInfoByRoomId = getAsyncCoursePracticeQuestionInfoByRoomId(roomId, Lists.newArrayList(questionId));
        final QuestionMetaBo questionMetaBo = practiceMetaComponent.getQuestionMetaBo(roomId, questionId);
        List<CoursePracticeQuestionInfo> practiceQuestionInfoList = asyncCoursePracticeQuestionInfoByRoomId.get();
		if (CollectionUtils.isNotEmpty(practiceQuestionInfoList)) {
			CoursePracticeQuestionInfo coursePracticeQuestionInfo = practiceQuestionInfoList.get(0);
			if (coursePracticeQuestionInfo.getBizStatus() == CoursePracticeQuestionInfoEnum.FORCESTOP.getStatus()) {
				// 如果是强制结束的则剩余时间为0
				questionMetaBo.setLastPracticeTime(-1);
			} else {
				Long practiceTime = (System.currentTimeMillis() - coursePracticeQuestionInfo.getStartPracticeTime())
						/ 1000;
				// 计算剩余时间
				questionMetaBo.setLastPracticeTime(practiceTime > coursePracticeQuestionInfo.getPracticeTime() ? -1
						: coursePracticeQuestionInfo.getPracticeTime() - practiceTime.intValue());
			}
		} else {
            questionMetaBo.setLastPracticeTime(-1);
        }
        return questionMetaBo;
    }

    @Override
    public PageInfo<PracticeRoomRankUserBo> getQuestionRankInfo(Long roomId, Integer page, Integer pageSize) {
        List<PracticeRoomRankUserBo> roomRankInfoList = practiceMetaComponent.getRoomRankInfo(roomId, (page - 1) * pageSize, page * pageSize - 1);
        Long totalInfo = practiceMetaComponent.getRoomRankTotalInfo(roomId);
        PageInfo<PracticeRoomRankUserBo> pageInfo = PageInfo.of(roomRankInfoList);
        pageInfo.setTotal(totalInfo);
        int totalPageNum = (int) ((totalInfo  +  pageSize  - 1) / pageSize);
		pageInfo.setPages(totalPageNum);
		pageInfo.setPageNum(page);
        return pageInfo;
    }

    /**
     * 数据合并
     *
     * @param baseQuestionInfoList           试题基础信息
     * @param courseBreakpointQuestionList   试题与断点绑定信息
     * @param coursePracticeQuestionInfoList 试题-直播 信息
     * @return 组合数据
     */
    private static List<TeacherQuestionBo> combinationQuestionInfo(
            final List<QuestionInfo> baseQuestionInfoList,
            List<CourseBreakpointQuestion> courseBreakpointQuestionList,
            final List<CoursePracticeQuestionInfo> coursePracticeQuestionInfoList) {
        if (CollectionUtils.isEmpty(courseBreakpointQuestionList)) {
            return Lists.newArrayList();
        }
        List<TeacherQuestionBo> result = courseBreakpointQuestionList.parallelStream()
                .map(courseBreakpointQuestion -> {
                    final TeacherQuestionBo teacherQuestionBo = new TeacherQuestionBo();
                    Optional<QuestionInfo> baseQuestionInfoOptional = baseQuestionInfoList.stream()
                            .filter(questionInfo -> questionInfo.getId().equals(courseBreakpointQuestion.getQuestionId()))
                            .findAny();
                    if (baseQuestionInfoOptional.isPresent()) {
                        BeanUtils.copyProperties(baseQuestionInfoOptional.get(), teacherQuestionBo);
                    }
                    Optional<CoursePracticeQuestionInfo> practiceQuestionInfoOptional = coursePracticeQuestionInfoList.stream()
                            .filter(coursePracticeQuestionInfo -> coursePracticeQuestionInfo.getQuestionId().equals(courseBreakpointQuestion.getQuestionId().intValue()))
                            .findAny();
					if (practiceQuestionInfoOptional.isPresent()) {
						final CoursePracticeQuestionInfo coursePracticeQuestionInfo = practiceQuestionInfoOptional
								.get();
						teacherQuestionBo.setStartPracticeTime(coursePracticeQuestionInfo.getStartPracticeTime());
						// 开始答题时间
						Long startPracticeTime = coursePracticeQuestionInfo.getStartPracticeTime();
						// 已经开始
						if (startPracticeTime != null) {
								Long practiceTime = (System.currentTimeMillis()
										- coursePracticeQuestionInfo.getStartPracticeTime()) / 1000;
								// 计算剩余时间
								teacherQuestionBo.setLastPracticeTime(
										practiceTime > coursePracticeQuestionInfo.getPracticeTime() ? -1
												: coursePracticeQuestionInfo.getPracticeTime()
														- practiceTime.intValue());
						}
						if (coursePracticeQuestionInfo.getBizStatus() == CoursePracticeQuestionInfoEnum.FORCESTOP.getStatus()) {
							// 此时该试题考试状态为老师强制关闭
							teacherQuestionBo.setLastPracticeTime(-1);
						} 
						// 设置的练习时间
						teacherQuestionBo.setPracticeTime(coursePracticeQuestionInfo.getPracticeTime());
					}
					// 如果是默认值则修改为null
					if (courseBreakpointQuestion.getPptIndex() != null
							&& courseBreakpointQuestion.getPptIndex() == 9999) {
						teacherQuestionBo.setPptIndex(null);
					} else {
						teacherQuestionBo.setPptIndex(courseBreakpointQuestion.getPptIndex());
					}
                    return teacherQuestionBo;
                })
                .collect(Collectors.toList());
        return result;
    }


	@Override
	public void stopAnswer(Long roomId, Long questionId) {
		CoursePracticeQuestionInfo coursePracticeQuestionInfo = getCoursePracticeQuestionInfoByRoomIdAndQuestionId(
				roomId, questionId);
		if (coursePracticeQuestionInfo != null) {
			coursePracticeQuestionInfo.setBizStatus(CoursePracticeQuestionInfoEnum.FORCESTOP.getStatus());
		}
		coursePracticeQuestionInfoService.save(coursePracticeQuestionInfo);

	}

    public List<QuestionMetaBo> getCoursewareAnswerQuestionInfo(Long roomId){
        coursewarePracticeQuestionInfoService.generateCoursewareAnswerCardInfo(roomId);
       return null;
    }

    /**
     * 根据课件Id查询课件的随堂练习正确率
     * @param coursewareId 课件Id
     * @return
     */
    public Integer getCourseRightRate(Long coursewareId,Long roomId){
        //获取课件下答对题的数目
        final String key = CoursePracticeCacheKey.roomRightQuestionSum(coursewareId);
        //使用持久化存储数据
        Integer rightNum=Integer.parseInt(persistTemplate.opsForValue().get(key,0,-1));

        //获取课件下作答总人数
        //使用持久化存储数据
        final SetOperations<String, Long> opsForSet = persistTemplate.opsForSet();
        final String allUserSumKey = CoursePracticeCacheKey.roomAllUserSum(coursewareId);
        Integer answerNum = opsForSet.members(allUserSumKey).size();

        //获取课件下试题的数量
        List<Integer> questionIds=coursePracticeQuestionInfoService.getQuestionsInfoByRoomId(roomId);
        Integer questionNum=questionIds.size();
        if (questionNum==0){
            questionNum=1;
        }

        //计算课件的正确率
        Integer rightRate=0;
        if (answerNum!=0){
            rightRate=rightNum/(answerNum * questionNum)  * 100;
        }
        return rightRate;
    }
}
