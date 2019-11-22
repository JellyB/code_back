package com.huatu.tiku.essay.manager;

import com.huatu.tiku.essay.constant.status.AdminPaperConstant;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayMockExamConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssayMockExamAnswerVO;
import com.huatu.tiku.essay.repository.EssayMockExamRepository;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayPaperBaseRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouwei
 * @Description: 试卷信息管理
 * @create 2018-04-03 下午2:39
 **/
@Slf4j
public class PaperManager {

    /**
     * 查询模考试卷信息
     * @param paperId
     * @return
     */
    public static EssayPaperBase getPaperBase(EssayPaperBaseRepository essayPaperBaseRepository, EssayMockExamRepository essayMockExamRepository, RedisTemplate<String, Object> redisTemplate, long paperId, int mockRedisExpireTime) {
        String paperBaseKey = RedisKeyConstant.getPaperBaseKey(paperId);
        EssayPaperBase paperBase = (EssayPaperBase) redisTemplate.opsForValue().get(paperBaseKey);

        if (null == paperBase ) {
            paperBase = essayPaperBaseRepository.findOne(paperId);
            //存入redis
            if (null != paperBase) {

                //0226 名字是行测考试的名字-申论
                EssayMockExam essayMockExam = getMockDetail(paperId, redisTemplate, essayMockExamRepository, mockRedisExpireTime);
                String practiceInfoKey = RedisKeyConstant.getPracticeInfoKey();
                Object obj = redisTemplate.opsForHash().get(practiceInfoKey, essayMockExam.getPracticeId() + "");
                if (null != obj) {
                    paperBase.setName(obj + "-申论");
                }
                // 不为空才放缓存  加过期时间，防止临时老师修改试题信息
                redisTemplate.opsForValue().set(paperBaseKey, paperBase);
                redisTemplate.expire(paperBaseKey, mockRedisExpireTime, TimeUnit.MINUTES);
            }

        }
        return paperBase;
    }


    /**
     * 查询模考信息
     * @param paperId
     * @return
     */
    public static EssayMockExam getMockDetail(long paperId, RedisTemplate<String, Object> redisTemplate, EssayMockExamRepository essayMockExamRepository, int mockRedisExpireTime) {
        String mockDetailPrefix = RedisKeyConstant.getMockDetailPrefix(paperId);
        EssayMockExam essayMockExam = (EssayMockExam) redisTemplate.opsForValue().get(mockDetailPrefix);
        if (null == essayMockExam) {
            essayMockExam = essayMockExamRepository.findOne(paperId);
            if (null != essayMockExam) {
                // 不为空才放缓存  加过期时间，防止临时老师修改信息
                redisTemplate.opsForValue().set(mockDetailPrefix, essayMockExam);
                redisTemplate.expire(mockDetailPrefix, mockRedisExpireTime, TimeUnit.MINUTES);
            }
        }
        return essayMockExam;

    }



    /**
     * 查询模考  用户答题卡信息(答题卡不设置失效时间，接口统一清除缓存)
     * @param paperId
     * @return
     */
    public static EssayMockExamAnswerVO getAnswerCard(long paperId, int userId, RedisTemplate<String, Object> redisTemplate,
                                                EssayPaperAnswerRepository essayPaperAnswerRepository,EssayQuestionAnswerRepository essayQuestionAnswerRepository, int mockRedisExpireTime) {

        String examAnswerKey = RedisKeyConstant.getExamAnswerKey(paperId, userId);
        EssayMockExamAnswerVO obj = null;
        try{
            obj =  (EssayMockExamAnswerVO)redisTemplate.opsForValue().get(examAnswerKey);
        }catch (Exception e){
            Object o = redisTemplate.opsForValue().get(examAnswerKey);
            if(null!=o){
                log.error("o = " + o.toString());
            }
            log.error("getAnswerCard error through redis,{}",e.getMessage());
            redisTemplate.delete(examAnswerKey);
        }
            if (null == obj) {
            List<EssayPaperAnswer> essayPaperAnswerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndTypeAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(userId,
                    paperId,
                    AdminPaperConstant.MOCK_PAPER,
                    EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                    EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType()
            );
            if (CollectionUtils.isNotEmpty(essayPaperAnswerList)) {
                EssayPaperAnswer essayPaperAnswer = essayPaperAnswerList.get(0);
                List<EssayQuestionAnswer> questionAnswerList = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus
                        (essayPaperAnswer.getId(), EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "questionDetailId"));
                obj = EssayMockExamAnswerVO.builder()
                        .essayPaperAnswer(essayPaperAnswer)
                        .essayQuestionAnswerList(questionAnswerList)
                        .build();
                //不为空才放入缓存
                redisTemplate.opsForValue().set(examAnswerKey, obj);
//                redisTemplate.expire(examAnswerKey, mockRedisExpireTime, TimeUnit.MINUTES);
            }
        }
        return obj;

    }


    public static boolean checkAnswerCard(long paperId, int userId, RedisTemplate<String, Object> redisTemplate, EssayPaperAnswerRepository essayPaperAnswerRepository) {
        boolean flag = false;

        String examAnswerKey = RedisKeyConstant.getExamAnswerKey(paperId, userId);
        EssayMockExamAnswerVO obj =  (EssayMockExamAnswerVO)redisTemplate.opsForValue().get(examAnswerKey);

        if (null == obj) {
            List<EssayPaperAnswer> essayPaperAnswerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndTypeAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(userId,
                    paperId,
                    AdminPaperConstant.MOCK_PAPER,
                    EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                    EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType());
            if (CollectionUtils.isNotEmpty(essayPaperAnswerList)) {
                flag = true;
            }
        }else{
            flag = true;
        }
        return flag;
    }


    /**
     * 查询所有考完试的模考
     *
     * @return 模考ID列表
     */
    public static List<EssayMockExam> getAllFinishedMockExamList(RedisTemplate<String, Object> redisTemplate, EssayMockExamRepository essayMockExamRepository) {
        List<EssayMockExam> mockList;
        Object object = redisTemplate.opsForValue().get(RedisKeyConstant.MOCK_FINISHED_EXAM_ID_LIST);
        if (object != null) {
            return (List<EssayMockExam>) object;
        } else {
            mockList = essayMockExamRepository.findByBizStatusAndStatusOrderByEndTimeDesc
                    (EssayMockExamConstant.EssayMockExamBizStatusEnum.COMPLETED.getBizStatus(), EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus());
            redisTemplate.opsForValue().set(RedisKeyConstant.MOCK_FINISHED_EXAM_ID_LIST, mockList);
            redisTemplate.expire(RedisKeyConstant.MOCK_FINISHED_EXAM_ID_LIST, 3, TimeUnit.MINUTES);
        }
        return mockList;
    }
}
