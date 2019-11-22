package com.huatu.tiku.essay.service.impl;

import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.date.TimeUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.error.EssayMockErrors;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.match.MatchRedisKeyConstant;
import com.huatu.tiku.essay.constant.status.EssayMockExamConstant;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.entity.EssayMockUserMeta;
import com.huatu.tiku.essay.entity.EssayQuestionBelongPaperArea;

import com.huatu.tiku.essay.entity.vo.report.MatchUserMeta;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.manager.AreaManager;
import com.huatu.tiku.essay.manager.PaperManager;
import com.huatu.tiku.essay.repository.EssayAreaRepository;
import com.huatu.tiku.essay.repository.EssayMockExamRepository;
import com.huatu.tiku.essay.repository.EssayMockUserMetaRepository;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.service.EssayMatchService;
import com.huatu.tiku.essay.service.task.AsyncMatchServiceImpl;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.ResponseMsg;
import com.huatu.tiku.essay.util.date.DateUtil;
import com.huatu.tiku.essay.vo.resp.EssayMockExamAnswerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Created by x6 on 2017/12/15.
 */
@Service
@Slf4j
public class EssayMatchServiceImpl implements EssayMatchService {
    @Autowired
    EssayMockExamRepository essayMockExamRepository;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Value("${mock_redis_expire_time}")
    private int mockRedisExpireTime;
    @Autowired
    EssayMockUserMetaRepository essayMockUserMetaRepository;
    @Autowired
    AsyncMatchServiceImpl asyncMatchServiceImpl;
    @Autowired
    EssayAreaRepository essayAreaRepository;
    @Autowired
    RestTemplate restTemplate;
    @Value("${mock_course_info_url}")
    private String mockCourseInfoUrl;

    private static final int MAX_MATCH_SIZE = 5;
    
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    private static final List<Long> sortIds = Lists.newArrayList(796L,789L,790L,791L);

    Comparator<EssayMockExam> matchSort = (a,b)->{
        if(sortIds.contains(a.getId()) && sortIds.contains(b.getId())){
            return sortIds.indexOf(a.getId()) - sortIds.indexOf(b.getId());
        }
        return a.getStartTime().compareTo(b.getStartTime());
    };

    /**
     * 查询当前进行的申论模考大赛
     */
    @Override
    public List<EssayMockExam> getCurrent() {
        List<EssayMockExam> usefulMatches = getUsefulMatchesV2();
        return usefulMatches;
    }

    List<EssayMockExam> getUsefulMatches() {
        //查询今天所有可用的申论模考
        long millions = DateUtil.getTodayStartMillions();
        Date date = new Date(millions);
        // 加缓存 5分钟失效
        String currentMatchKey = MatchRedisKeyConstant.getCurrentMatchKey();
        List<EssayMockExam> matches = (List<EssayMockExam>) redisTemplate.opsForValue().get(currentMatchKey);
        if (CollectionUtils.isEmpty(matches)) {
//            matches = essayMockExamRepository.findCurrentEssayMock(date);
            ArrayList<Integer> bizStatus = Lists.newArrayList(EssayMockExamConstant.EssayMockExamBizStatusEnum.ONLINE.getBizStatus(),
                    EssayMockExamConstant.EssayMockExamBizStatusEnum.FINISHED.getBizStatus(),
                    EssayMockExamConstant.EssayMockExamBizStatusEnum.COMPLETED.getBizStatus());
            matches = essayMockExamRepository.findByStatusAndBizStatusInAndStartTimeGreaterThanOrderByStartTime(EssayStatusEnum.NORMAL.getCode(),bizStatus,date);
            if (CollectionUtils.isNotEmpty(matches)) {
                matches.sort(matchSort);
                redisTemplate.opsForValue().set(currentMatchKey, matches, 5, TimeUnit.MINUTES);
            }
        }
        List<EssayMockExam> usefulMatches = null;

        if (CollectionUtils.isNotEmpty(matches)) {
            while (true) {
                //取最近的日期，日期如果比当天大，则可以直接返回，如果是当天判断是否每个考试都已结束，如果结束，则循环处理下一天的数据
                String current = DateUtil.getFormatDateString(matches.get(0).getStartTime().getTime());
                usefulMatches = matches.stream().filter(match -> DateUtil.getFormatDateString(match.getStartTime().getTime()).equals(current)).collect(Collectors.toList());
                if (!current.equals(DateUtil.getFormatDateString(System.currentTimeMillis()))) {
                    break;
                }
                boolean isFinished = true;  //当天的考试是否都已结束
                for (EssayMockExam match : usefulMatches) {
                    long endTime = match.getEndTime().getTime();
                    if (endTime + TimeUnit.MINUTES.toMillis(60) > System.currentTimeMillis()) {
                        isFinished = false;
                        break;
                    }
                }
                //如果当天还有考试没有结束直接返回当天的数据
                if (!isFinished) {
                    break;
                }
                //如果当天考试全部结束，删除当天数据
                matches.removeAll(usefulMatches);
                //如果还有剩下的数据则循环处理下一天的数据，否则还是返回当天数据
                if (CollectionUtils.isEmpty(matches)) {
                    break;
                }
            }
        }
        if (CollectionUtils.isEmpty(usefulMatches)) {
            usefulMatches = Lists.newArrayList();
        }
        return usefulMatches;
    }
    //获取最近可用考试
	List<EssayMockExam> getUsefulMatchesV2() {
		// 查询今天所有可用的申论模考
		long millions = DateUtil.getTodayStartMillions();
		Date date = new Date(millions);
		// 加缓存 5分钟失效
		String currentMatchKey = MatchRedisKeyConstant.getCurrentMatchKey();
		List<EssayMockExam> matches = (List<EssayMockExam>) redisTemplate.opsForValue().get(currentMatchKey);
		if (CollectionUtils.isEmpty(matches)) {
			ArrayList<Integer> bizStatus = Lists.newArrayList(
					EssayMockExamConstant.EssayMockExamBizStatusEnum.ONLINE.getBizStatus(),
					EssayMockExamConstant.EssayMockExamBizStatusEnum.FINISHED.getBizStatus(),
					EssayMockExamConstant.EssayMockExamBizStatusEnum.COMPLETED.getBizStatus());
			matches = essayMockExamRepository.findByStatusAndBizStatusInAndStartTimeGreaterThanOrderByStartTime(
					EssayStatusEnum.NORMAL.getCode(), bizStatus, date);
			if (CollectionUtils.isNotEmpty(matches)) {
                matches.sort(matchSort);
				redisTemplate.opsForValue().set(currentMatchKey, matches, 5, TimeUnit.MINUTES);
			}
		}
		List<EssayMockExam> usefulMatches = null;
		if (CollectionUtils.isNotEmpty(matches)) {
			usefulMatches = matches.stream().filter(
					match -> match.getEndTime().getTime() + TimeUnit.MINUTES.toMillis(60) > System.currentTimeMillis())
					.limit(MAX_MATCH_SIZE)
					.collect(Collectors.toList());
		}
		if (CollectionUtils.isEmpty(usefulMatches)) {
			usefulMatches = Lists.newArrayList();
		}
		return usefulMatches;
	}


    /**
     * 申论模考大赛报名
     *
     * @param paperId
     * @param userSession
     * @param positionId
     */
    @Override
    public void enroll(long paperId, UserSession userSession, int positionId) {
        int userId = userSession.getId();
        //获取模考信息  优先取缓存
        EssayMockExam essayMockExam = PaperManager.getMockDetail(paperId, redisTemplate, essayMockExamRepository, mockRedisExpireTime);
        if (essayMockExam == null) {
            throw new BizException(EssayMockErrors.MOCK_ID_NOT_EXIST);
        }
        //具体报名逻辑的实现（redis，mysql报名数据的存贮）
        enrollHelp(paperId, userId, positionId, essayMockExam);

    }


    private void enrollHelp(long paperId, int userId, int positionId, EssayMockExam essayMockExam) {
        //模考大赛考试开始30分钟后，不能报名
        long startTime = essayMockExam.getStartTime().getTime();
        Long essayPaperId = essayMockExam.getId();
        if (System.currentTimeMillis() - startTime > TimeUnit.MINUTES.toMillis(30)) {
            throw new BizException(EssayMockErrors.MISSING_MATCH);
        }

        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();

        //取消这里的逻辑
//        /*  无论，用户有没有报名过。该模考报名总数+1  */
//        String countKey = RedisKeyConstant.getTotalEnrollCountKey(paperId);
//        opsForValue.increment(countKey, 1);

        /*
          更新用户报名地区
         */
        String essayPaperKey = RedisKeyConstant.getMockUserAreaPrefix(essayPaperId);
        //1.查询用户旧的报名信息
        MatchUserMeta userMeta = findMatchUserMeta(userId, paperId);

        //2.第一次报名或报名地区变动，更新报名信息
        if (userMeta == null || positionId != userMeta.getPositionId()) {
            //更新报名地区（redis）
            hashOperations.put(essayPaperKey, userId + "", positionId + "");
            //异步更新mysql报名信息
            asyncMatchServiceImpl.saveEnrollToMysql(positionId, paperId, userId);
        }
        //第一次报名，更新报名人数
        if (userMeta == null) {
            String countKey = RedisKeyConstant.getTotalEnrollCountKey(paperId);
            redisTemplate.opsForValue().increment(countKey, 1);
        }
    }

    /**
     * 保存用户报名信息到mysql
     *
     * @param positionId
     * @param paperId
     * @param userId
     */
    @Override
    public void saveEnrollToMysql(int positionId, long paperId, int userId) {
        //将旧的报名信息置为不可用
        int del = essayMockUserMetaRepository.updateToDel(paperId, userId);
        //插入新的报名信息
        EssayMockUserMeta essayMockUserMeta = EssayMockUserMeta.builder()
                .paperId(paperId)
                .userId(userId)
                .positionCount(positionId)
                .build();
        //status 的处理
        essayMockUserMeta.setStatus(1);
        essayMockUserMetaRepository.save(essayMockUserMeta);
    }


    /**
     * 查询用户报名信息
     *
     * @param userId
     * @param paperId
     * @return
     */
    @Override
    public MatchUserMeta findMatchUserMeta(int userId, long paperId) {


        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        String mockUserAreaKey = RedisKeyConstant.getMockUserAreaPrefix(paperId);
        //查询旧报名地区
        Object positionObj = hashOperations.get(mockUserAreaKey, userId + "");
        Long enroll = hashOperations.size(mockUserAreaKey);
        if (null != positionObj) {
            //查询答题卡数据
            long answerId = 0L;
            String examAnswerKey = RedisKeyConstant.getExamAnswerKey(paperId, userId);
			try {
				EssayMockExamAnswerVO answer = (EssayMockExamAnswerVO) redisTemplate.opsForValue().get(examAnswerKey);
				if (null != answer) {
					answerId = answer.getEssayPaperAnswer().getId();
				}
			} catch (Exception e) {
				log.error("get examAnswerKey error:{}", e);
				redisTemplate.delete(examAnswerKey);
				EssayMockExamAnswerVO essayMockExamAnswerVO = PaperManager.getAnswerCard(paperId, userId, redisTemplate,
						essayPaperAnswerRepository, essayQuestionAnswerRepository, mockRedisExpireTime);
				if (essayMockExamAnswerVO != null) {
					redisTemplate.opsForValue().set(examAnswerKey, essayMockExamAnswerVO);
					answerId = essayMockExamAnswerVO.getEssayPaperAnswer().getId();
				}
			}
           
            MatchUserMeta userMeta = MatchUserMeta.builder()
                    .paperId((int) paperId)
                    .positionCount(enroll.intValue())
                    .positionId(Long.parseLong(positionObj.toString()))
                    .practiceId(answerId)
                    .build();
            //地区名称
            if ("-9".equals(positionObj.toString())) {
                userMeta.setPositionName("全国");
            } else {
                Map<Long, EssayQuestionBelongPaperArea> areaMap = AreaManager.getAreaMap(essayAreaRepository, redisTemplate);
                EssayQuestionBelongPaperArea area = areaMap.get(positionObj.toString());
                if(null != area){
                    userMeta.setPositionName(area.getName());
                }else{
                    userMeta.setPositionName("未知地区");
                }
            }
            return userMeta;
        }

        return null;

    }

    @Override
    public PageUtil getCurrentPage(int page, int pageSize) {

        List<EssayMockExam> usefulMatches = getUsefulMatchesV2();

        PageUtil.PageUtilBuilder<Object> builder = PageUtil.builder()
                .total(0)
                .totalPage(0)
                .result(Lists.newArrayList())
                .next(0);
        //处理分页next
        if (CollectionUtils.isNotEmpty(usefulMatches)) {
            int size = usefulMatches.size();

            return builder.total(size)
                    .totalPage((int) (Math.ceil((double) size / (double) pageSize)))
                    .result(usefulMatches.subList( Math.min(page * pageSize,size), Math.min((page + 1) * pageSize,size)))
                    .next((size > (page + 1) * pageSize) ? 1 : 0)
                    .build();
        }

        return builder.build();
    }

    @Override
    public Map<String, Map> getMockCourseList(String mockCourseIds) {
        String mockCourseInfoKey = MatchRedisKeyConstant.getMockCourseInfo(mockCourseIds);
        HashMap data = (HashMap) redisTemplate.opsForValue().get(mockCourseInfoKey);

        if (data == null || data.isEmpty()) {
            ResponseMsg resp = restTemplate.getForObject(mockCourseInfoUrl + mockCourseIds, ResponseMsg.class);
            data = (HashMap) resp.getData();
            if (!data.isEmpty()) {
                redisTemplate.opsForValue().set(mockCourseInfoKey, data, 5, TimeUnit.MINUTES);
            }
        }

        return data;
    }

}
