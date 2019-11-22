package com.huatu.ztk.paper.service.v4.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticeForCoursePaper;
import com.huatu.ztk.paper.bean.PracticePointsSummary;
import com.huatu.ztk.paper.bo.AnswerInfoBo;
import com.huatu.ztk.paper.bo.PaperBo;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.common.CourseType;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.dao.PracticePointsSummaryDao;
import com.huatu.ztk.paper.enums.CoursePaperType;
import com.huatu.ztk.paper.service.v4.PracticeCourseService;
import com.huatu.ztk.paper.vo.PracticeReportVo;

/**
 * @author shanjigang
 * @date 2019/3/1514:35
 */
@Service
public class PracticeCourseServiceImpl implements PracticeCourseService {
	
	private final static Logger logger = LoggerFactory.getLogger(PracticeCourseServiceImpl.class);

    @Autowired
    private PracticePointsSummaryDao practicePointsSummaryDao;

    @Autowired
    private AnswerCardDao answerCardDao;
    
    @Autowired
    private RedisTemplate redisTemplate;
    

    public PracticeReportVo getPracticeReport (Long courseId,int type,int subject,Long userId,Integer courseType)throws BizException {
    	//如果是随堂练习答题卡类型转换为paper类型
		if (AnswerCardType.COURSE_BREAKPOINT == type) {
			type = CoursePaperType.COURSE_BREAKPOINT.getCode();
		}
		long t1 = System.currentTimeMillis();
       AnswerCard answerCard = answerCardDao.findCourseAnswerCard(userId.intValue(), courseId, courseType, type);
       long t2 = System.currentTimeMillis();
       logger.info("getPracticeReport findCourseAnswerCard expend:{}",t2-t1);
       if (answerCard==null){
           throw new BizException(CommonErrors.RESOURCE_NOT_FOUND, "答题卡不存在");
       }
       if (answerCard.getStatus() == AnswerCardStatus.FINISH) {//已经结束的,则设置知识点汇总
            //查询设置知识点汇总
            final PracticePointsSummary practicePointsSummary = practicePointsSummaryDao.findByPracticeId(answerCard.getId());
            if(practicePointsSummary!=null){
                answerCard.setPoints(practicePointsSummary.getPoints());
            }
        } else {
            answerCard.setPoints(new ArrayList<>());
        }
       
        PracticeCard standardCard=(PracticeCard)answerCard;

        PaperBo paperBo=new PaperBo();
        paperBo.setModules(standardCard.getPaper().getModules());
        paperBo.setQuestions(standardCard.getPaper().getQuestions());

        //班级平均作对数量
        int averageRcount=0;
        //班级平均用时
        int averageTime=0;
        //随堂练习总题数
        int qcount = 0;
        // todo taiman
        /*List<AnswerCard> answerCards=answerCardDao.getCardsByCourseTypeAndCourseId(courseId,type,courseType);
        if(CollectionUtils.isNotEmpty(answerCards)){
            averageRcount=(int)answerCards.stream().mapToInt(AnswerCard::getRcount).average().getAsDouble();
            averageTime=(int)answerCards.stream().mapToInt(AnswerCard::getSpeed).average().getAsDouble();

        }*/
        //如果是录播填充统计信息
		if (courseType == CourseType.RECORD) {
			 long t3 = System.currentTimeMillis();
			PracticeForCoursePaper practiceForCoursePaper = (PracticeForCoursePaper) ((PracticeCard) answerCard)
					.getPaper();
			HashOperations<String, Object, String> opsForHash = redisTemplate.opsForHash();
			String practiceKey = PaperRedisKeys.getPracticeCourseIdAndCourseTypeKey(
					practiceForCoursePaper.getCourseId(), practiceForCoursePaper.getCourseType());
			Map<Object, String> entries = opsForHash.entries(practiceKey);
			logger.info("录播课件id:{},获取随堂练参与人数为:{}", practiceForCoursePaper.getCourseId(),entries.size());
			List<String> answerList = entries.entrySet().stream().map(Map.Entry::getValue)
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(answerList)) {
				averageRcount = (int) answerList.stream().mapToInt(answerStr -> {
					AnswerInfoBo answerInfoBo = JSONObject.parseObject(answerStr, AnswerInfoBo.class);
					return answerInfoBo.getRcount();
				}).average().getAsDouble();
				averageTime = (int) answerList.stream().mapToInt(answerStr -> {
					AnswerInfoBo answerInfoBo = JSONObject.parseObject(answerStr, AnswerInfoBo.class);
					return answerInfoBo.getSpeed();
				}).average().getAsDouble();
				logger.info("录播课件id:{},获取随堂练统计平均做对数据:{},平均用时数据{}", practiceForCoursePaper.getCourseId(), averageRcount,
						averageTime);

			}
			logger.info("getPracticeReport 录播获取统计信息 expend:{}", System.currentTimeMillis() - t3);
            qcount = practiceForCoursePaper.getQcount();
		}
        
		

        List<QuestionPointTree> pointTrees = Lists.newArrayList();
        //设置金币数量
        Integer coin=0;
        if(courseType==CourseType.LIVE){
            coin=answerCard.getRcount() * 2;
        }
        PracticeReportVo practiceReportVo=new PracticeReportVo();
        practiceReportVo.setAnswers(answerCard.getAnswers());
        practiceReportVo.setAverageTime(answerCard.getSpeed());
        practiceReportVo.setClassAverageRcount(averageRcount);
        practiceReportVo.setClassAverageTime(averageTime);
        practiceReportVo.setCoin(coin);
        practiceReportVo.setCorrects(answerCard.getCorrects());
        practiceReportVo.setDoubts(answerCard.getDoubts());
        practiceReportVo.setPaper(paperBo);
        practiceReportVo.setTcount(qcount == 0 ? (answerCard.getRcount() + answerCard.getUcount() + answerCard.getWcount()) : qcount);
        practiceReportVo.setRcount(answerCard.getRcount());
        practiceReportVo.setWcount(answerCard.getWcount());
        practiceReportVo.setUcount(answerCard.getUcount());
        practiceReportVo.setTimesTotal(Arrays.stream(answerCard.getTimes()).sum());
        practiceReportVo.setId(String.valueOf(answerCard.getId()));
        practiceReportVo.setSubmitTimeInfo(answerCard.getCreateTime());
        /**
         * 只返回3级知识点汇总
         */
        if(CollectionUtils.isNotEmpty(answerCard.getPoints())){
            answerCard.getPoints().forEach(level1 ->{
                if(CollectionUtils.isEmpty(level1.getChildren())){
                    return;
                }
                level1.getChildren().forEach(level2 ->{
                    if(CollectionUtils.isEmpty(level2.getChildren())){
                        return;
                    }
                    level2.getChildren().forEach(level3 -> pointTrees.add(level3));
                });
            });
        }
        practiceReportVo.setPoints(pointTrees);
        return practiceReportVo;
    }

    public List<HashMap<String,Object>> getBatchCoursePracticeStatus(Long userId,List<HashMap<String,Object>> courseMap){
        List<HashMap<String,Object>> list= Lists.newArrayList();
		
        courseMap.forEach(map->{
            //答题卡状态 0未创建 1创建 2未完成 3完成
            Integer answercardStatus=0;

           AnswerCard answerCard = answerCardDao.findCourseAnswerCard(userId.intValue(),
					Long.parseLong(String.valueOf(map.get("courseId"))),
					Integer.parseInt(String.valueOf(map.get("courseType"))), CoursePaperType.COURSE_BREAKPOINT.getCode());
            if (answerCard!=null){
                if (CourseType.RECORD==Integer.parseInt(String.valueOf(map.get("courseType")))){
                    if(answerCard.getStatus()!=1){
                        answercardStatus=1;
                    }
                }else{
                    answercardStatus=1;
                }

            }

            HashMap<String,Object> statusMap=Maps.newHashMap();
            statusMap.put("courseId",map.get("courseId"));
            statusMap.put("courseType",map.get("courseType"));
            statusMap.put("reportStatus",answercardStatus);
            list.add(statusMap);
        });
        return list;
    }
    
}
