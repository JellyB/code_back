package com.huatu.tiku.course.service.v1.impl.practice;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.huatu.tiku.course.bean.practice.QuestionMetaBo;
import com.huatu.tiku.course.bean.vo.CoursewarePracticeQuestionVo;
import com.huatu.tiku.course.service.v1.practice.CoursewarePracticeQuestionInfoService;
import com.huatu.tiku.course.service.v1.practice.LiveCourseRoomInfoService;
import com.huatu.tiku.course.service.v1.practice.QuestionInfoService;
import com.huatu.tiku.entity.CoursewarePracticeQuestionInfo;

import lombok.extern.slf4j.Slf4j;
import service.impl.BaseServiceHelperImpl;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

/**
 * @author shanjigang
 * @date 2019/3/23 19:07
 */
@Service
@Slf4j
public class CoursewarePracticeQuestionInfoServiceImpl extends BaseServiceHelperImpl<CoursewarePracticeQuestionInfo>
        implements CoursewarePracticeQuestionInfoService {
	
    public CoursewarePracticeQuestionInfoServiceImpl() {
        super(CoursewarePracticeQuestionInfo.class);
    }

    @Autowired
    private  LiveCourseRoomInfoService liveCourseRoomInfoService;

    @Autowired
    private  PracticeMetaComponent practiceMetaComponent;

    @Autowired
    private QuestionInfoService questionInfoService;
    /**
     * 根据 roomId coursewareId 列表查询
     */
	public CoursewarePracticeQuestionVo findByCoursewareIdAndRoomId(Long roomId, Long coursewareId) {
		WeekendSqls<CoursewarePracticeQuestionInfo> weekendSqls = WeekendSqls.<CoursewarePracticeQuestionInfo>custom()
				.andEqualTo(CoursewarePracticeQuestionInfo::getRoomId, roomId)
				.andEqualTo(CoursewarePracticeQuestionInfo::getCoursewareId, coursewareId);
		Example example = Example.builder(CoursewarePracticeQuestionInfo.class).where(weekendSqls).build();
		List<CoursewarePracticeQuestionInfo> list = selectByExample(example);
		if (CollectionUtils.isNotEmpty(list)) {
			CoursewarePracticeQuestionInfo info = list.get(0);
			String meta = info.getMeta();
			List<QuestionMetaBo> questionMetaBos = JSONArray.parseArray(meta, QuestionMetaBo.class);
			CoursewarePracticeQuestionVo course = new CoursewarePracticeQuestionVo();
			course.setCoursewareId(info.getCoursewareId());
			course.setRoomId(info.getRoomId());
			course.setMeta(questionMetaBos);
			return course;
		}
		return null;
	}

    /**
     * 生成课件下试题作答信息 入库
     */
    public void generateCoursewareAnswerCardInfo(Long roomId){
      //获取房间下的课件Id
        List<Integer> coursewareIds = liveCourseRoomInfoService.getLiveCourseIdListByRoomId(roomId);
        //获取房间下的已作答试题
        List<String> roomPracticedQuestion = practiceMetaComponent.getRoomPracticedQuestion(roomId);
        for(int i=0;i<coursewareIds.size();i++){
            Long courseId=coursewareIds.get(i).longValue();
            List<QuestionMetaBo> questionMetaBos=Lists.newArrayList();
            roomPracticedQuestion.forEach(questionId -> {
                QuestionMetaBo questionMetaBo = practiceMetaComponent.getCourseQuestionMetaBo(roomId,courseId,Long.parseLong(questionId));
               // List<QuestionInfo> baseQuestionInfoList = questionInfoService.getBaseQuestionInfo(Lists.newArrayList(Long.valueOf(questionId)));
                if(questionMetaBo.getAvgTime()==null){
                    questionMetaBo.setAvgTime(0);
                }
                if(questionMetaBo.getCorrectCate()==null){
                    questionMetaBo.setCorrectCate(0D);
                }
                if(questionMetaBo.getLastPracticeTime()==null){
                    questionMetaBo.setLastPracticeTime(0);
                }
                if(questionMetaBo.getType()==null){
                    questionMetaBo.setType(0);
                }
                if(questionMetaBo.getPercents()==null){
                    questionMetaBo.setPercents(new int[4]);
                }
                questionMetaBos.add(questionMetaBo);
            });
            //插入数据库
            CoursewarePracticeQuestionInfo coursewarePracticeQuestionInfo=new CoursewarePracticeQuestionInfo();

            String meta = JSONObject.toJSONString(questionMetaBos);
           
            
            WeekendSqls<CoursewarePracticeQuestionInfo> weekendSqls = WeekendSqls.<CoursewarePracticeQuestionInfo>custom()
    				.andEqualTo(CoursewarePracticeQuestionInfo::getRoomId, roomId)
    				.andEqualTo(CoursewarePracticeQuestionInfo::getCoursewareId, courseId);
    		Example example = Example.builder(CoursewarePracticeQuestionInfo.class).where(weekendSqls).build();
    		List<CoursewarePracticeQuestionInfo> list = selectByExample(example);
    		if(CollectionUtils.isNotEmpty(list)) {
    			coursewarePracticeQuestionInfo = list.get(0);
    			coursewarePracticeQuestionInfo.setGmtModify(new Timestamp(System.currentTimeMillis()));
    		}else {
    			coursewarePracticeQuestionInfo.setCoursewareId(courseId);
    			coursewarePracticeQuestionInfo.setRoomId(roomId);
    		}
    		coursewarePracticeQuestionInfo.setMeta(meta);
    		save(coursewarePracticeQuestionInfo);
    			
        }

    }
}
