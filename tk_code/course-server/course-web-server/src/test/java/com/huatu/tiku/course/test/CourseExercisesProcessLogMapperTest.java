package com.huatu.tiku.course.test;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.bean.vo.LiveRecordInfo;
import com.huatu.tiku.course.bean.vo.LiveRecordInfoWithUserInfo;
import com.huatu.tiku.course.bean.vo.SyllabusWareInfo;
import com.huatu.tiku.course.common.YesOrNoStatus;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.dao.manual.CourseExercisesProcessLogMapper;
import com.huatu.tiku.course.service.manager.CourseExercisesProcessLogManager;
import com.huatu.tiku.course.service.manager.CourseExercisesStatisticsManager;
import com.huatu.tiku.course.service.v1.CourseExercisesService;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.paper.PracticeCardServiceV1;
import com.huatu.tiku.entity.CourseExercisesProcessLog;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticeForCoursePaper;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-02-25 下午6:14
 **/
@Slf4j
public class CourseExercisesProcessLogMapperTest extends BaseWebTest {

    @Autowired
    private CourseExercisesProcessLogMapper courseExercisesProcessLogMapper;


    @Autowired
    private CourseExercisesProcessLogManager courseExercisesProcessLogManager;

    @Autowired
    private CourseExercisesService courseExercisesService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PracticeCardServiceV1 practiceCardService;

    @Autowired
    private CourseExercisesStatisticsManager courseExercisesStatisticsManager;



    @Test
    public void insert(){
        CourseExercisesProcessLog courseExercisesProcessLog = new CourseExercisesProcessLog();
        courseExercisesProcessLogMapper.insert(courseExercisesProcessLog);
    }

    @Test
    public void testInfo(){
        Set<Long> syllabusId = Sets.newHashSet();
        syllabusId.add(8361563L);
        syllabusId.add(8361564L);
        syllabusId.add(8361562L);
        syllabusId.add(8361565L);
        Table<String, Long, SyllabusWareInfo> table =  courseExercisesProcessLogManager.dealSyllabusInfo2Table(syllabusId);

        table.row("lesson").values().forEach(item -> {
            log.info("SyllabusWareInfo:{}", JSONObject.toJSONString(item));
        });
    }

    @Test
    public void dealList(){
        Set<Long> syllabusId = Sets.newHashSet(8361563L,8361564L,8361562L,8361565L,4227750L);

        syllabusId.forEach(item ->{
            try{
                courseExercisesProcessLogManager.putIntoDealList(item);
                TimeUnit.SECONDS.sleep(1);
            }catch (InterruptedException e){
                log.error("and intterrupted error!");
            }
        });

    }


    @Test
    public void testCourseWorkList(){
        List<Integer> list = Lists.newArrayList(AnswerCardStatus.CREATE, AnswerCardStatus.UNDONE);
        Example example = new Example(CourseExercisesProcessLog.class);
        example.and().andEqualTo("status", YesOrNoStatus.YES.getCode())
                .andIn("bizStatus", list);

        List<CourseExercisesProcessLog> workList = courseExercisesProcessLogMapper.selectByExample(example);
        Set<Long> userIds = workList.stream().map(CourseExercisesProcessLog::getUserId).collect(Collectors.toSet());
        for (Long userId : userIds) {
            Object object = courseExercisesProcessLogManager.courseWorkList(userId, 1, 100);
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>:{}", JSONObject.toJSONString(object));
        }
    }

    @Test
    public void correctData(){
        List<Integer> list = Lists.newArrayList(AnswerCardStatus.CREATE, AnswerCardStatus.UNDONE);
        Example example = new Example(CourseExercisesProcessLog.class);
        example.and().andEqualTo("status", YesOrNoStatus.YES.getCode())
                .andIn("bizStatus", list);

        List<CourseExercisesProcessLog> workList = courseExercisesProcessLogMapper.selectByExample(example);
        Set<Long> syllabusIds = workList.stream().map(CourseExercisesProcessLog::getSyllabusId).collect(Collectors.toSet());
        Map<Long, SyllabusWareInfo> syllabusWareInfoMap = syllabusIds.stream().collect(Collectors.toMap(i -> i, i ->{
            SyllabusWareInfo syllabusWareInfo = courseExercisesProcessLogManager.dealSyllabusInfo2Info(i);
            return syllabusWareInfo;
        }));
        for (CourseExercisesProcessLog courseExercisesProcessLog : workList) {
            long syllabusId = courseExercisesProcessLog.getSyllabusId();
            if(!syllabusWareInfoMap.containsKey(syllabusId)){
                log.error("大纲id查询不到:{}", syllabusId);
                continue;
            }
            SyllabusWareInfo syllabusWareInfo = syllabusWareInfoMap.get(syllabusId);
            if(syllabusWareInfo.getClassId() != courseExercisesProcessLog.getCourseId()
                    || syllabusWareInfo.getVideoType() != courseExercisesProcessLog.getCourseType()
                    || syllabusWareInfo.getCoursewareId() != courseExercisesProcessLog.getLessonId()){
                log.error("数据库数据:课程:{},课件:{},类型:{}, 大纲数据:课程:{}, 课件:{}, 类型:{}",
                        courseExercisesProcessLog.getCourseId(),
                        courseExercisesProcessLog.getLessonId(),
                        courseExercisesProcessLog.getCourseType(),
                        syllabusWareInfo.getClassId(),
                        syllabusWareInfo.getVideoType());
            }

        }
    }

    /**
     * 查询课后作业习题信息
     */
    @Test
    public void listQuestionByCourseIdTest(){
        int courseType = 1;
        long courseWareId = 937995l;
        List<Map<String, Object>> result = courseExercisesService.listQuestionByCourseId(courseType, courseWareId);
        log.info("课后练习试题信息:{}", JSONObject.toJSONString(result));
    }



    @Test
    public void testCourseWorkListSingleUser(){
        int userId = 235519519;
        Object object = courseExercisesProcessLogManager.courseWorkList(userId, 1, 100);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>:{}", JSONObject.toJSONString(object));
    }

    @Test
    public void testRequestSingleSyllabusInfoWithCache(){
        long syllabusId = 5476937L;
        SyllabusWareInfo syllabusWareInfo = courseExercisesProcessLogManager.dealSyllabusInfo2Info(syllabusId);
        log.error("syllabusWareInfo:{}", JSONObject.toJSONString(syllabusWareInfo));

    }

    /**
     * 处理课后作业统计信息
     */
    @Test
    public void testDealQuestionStatistic(){
        String token = "c8a751bca2754f73be2ac5b55eb1bde6";
        long cardId = 872385456975301694L;
        NetSchoolResponse netSchoolResponse = practiceCardService.getAnswerCard(token, 1, cardId);
        if(null == netSchoolResponse.getData()){
            log.error("课后作业答题卡信息不存在:{}", cardId);
        }
        Object response = ResponseUtil.build(netSchoolResponse);
        JSONObject data = new JSONObject((LinkedHashMap<String, Object>) response);

        JSONObject paper = data.getJSONObject("paper");
        PracticeForCoursePaper practiceForCoursePaper = JSONObject.parseObject(paper.toJSONString(), PracticeForCoursePaper.class);

        PracticeCard practiceCard = JSONObject.parseObject(data.toJSONString(), PracticeCard.class);
        practiceCard.setPaper(practiceForCoursePaper);
        courseExercisesStatisticsManager.dealCourseExercisesStatistics(practiceCard);
    }

    /**
     * 课后作业统计信息详情
     */
    @Test
    public void testDealQuestionStatisticDetail(){
        long statisticsTableId = 41;
        Object object = courseExercisesStatisticsManager.statisticsDetail(statisticsTableId);
        log.info("课后作业统计信息详情:{}", JSONObject.toJSONString(object));
    }

    /**
     *
     */
    @Test
    public void saveLiveReport(){
        for(int i = 0; i < 1000; i ++){
            LiveRecordInfo liveRecordInfo = LiveRecordInfo.builder()
                    .courseWareId(1000123)
                    .classId(73987)
                    .syllabusId(5476947)
                    .bjyRoomId("19032545565293")
                    .build();

            LiveRecordInfoWithUserInfo liveRecordInfoWithUserId = LiveRecordInfoWithUserInfo
                    .builder()
                    .subject(1)
                    .terminal(1)
                    .userId(234934290)
                    .liveRecordInfo(liveRecordInfo).build();
            rabbitTemplate.convertAndSend("", RabbitMqConstants.COURSE_LIVE_REPORT_LOG, JSONObject.toJSONString(liveRecordInfoWithUserId));
        }
    }

    @Test
    public void duplicateDate(){
        List<Integer> list = Lists.newArrayList(412,4156,86424,65238);

        for(Integer id  :  list){
            CourseExercisesProcessLog log = new CourseExercisesProcessLog();
            log.setId(Long.valueOf(id));
            log.setStatus(YesOrNoStatus.NO.getCode());
            log.setModifierId(10010L);
            log.setGmtModify(new Timestamp(System.currentTimeMillis()));
            int result = courseExercisesProcessLogMapper.updateByPrimaryKeySelective(log);
            System.err.println(".>>>>>>>>>>>>>:"+ id + "<> " + result );
        }
    }


    @Test
    public void test(){
        Random random = new Random();
        for(;;){
            Map<String,Object> map = Maps.newHashMap();
            map.put(String.valueOf(random.nextInt(100)), random.nextInt(100));
            map.put(String.valueOf(random.nextInt(100)), random.nextInt(100));
            map.put(String.valueOf(random.nextInt(100)), random.nextInt(100));
            map.put(String.valueOf(random.nextInt(100)), random.nextInt(100));
            List<Map<String,Object>> sendDataList = Lists.newArrayList();
            sendDataList.add(map);
            DataInfo dataInfo = DataInfo.builder().data(sendDataList).build();
            rabbitTemplate.convertAndSend("", RabbitMqConstants.COURSE_BREAKPOINT_PRACTICE_SAVE_DB_QUEUE, JSONObject.toJSONString(dataInfo));
        }

    }

    @Data
    @Getter
    @Setter
    @NoArgsConstructor
    static class DataInfo implements Serializable{
        private List<Map<String,Object>> data;

        @Builder
        public DataInfo(List<Map<String, Object>> data) {
            this.data = data;
        }
    }
}
