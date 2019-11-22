package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.dao.PaperDao;
import com.huatu.ztk.paper.daoPandora.MatchUserMetaMapper;
import com.self.generator.core.WaitException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.huatu.ztk.paper.common.MatchRedisKeys.getMatchWhitUserReportKey;
import static javax.swing.UIManager.get;

/**
 * 模考大赛试卷处理-工具类
 * Created by lijun on 2018/11/24
 */
@Component
public class MatchServiceComponent {
    private static final Logger logger = LoggerFactory.getLogger(MatchServiceComponent.class);

    /**
     * 模考答题卡缓存时长
     */
    private final int CACHE_DAY_TIME = 7;

    /**
     * 刷入设备类型
     */
    private final int CREATE_DEFAULT_TERMINAL = 0;

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MatchUserMetaMapper matchUserMetaMapper;


    /**
     * 创建一张缓存的模考大赛答题卡
     *
     * @param paperId  试卷ID
     * @param subject  科目
     * @param userId   用户ID
     * @param terminal 设备类型
     * @return 答题卡信息
     */
    @Async
    public void createCachedMatchCardAndPutRedis(int paperId, int subject, long userId, int terminal) throws BizException, WaitException {
        Paper paper = paperDao.findById(paperId);
        if (paper == null || !(paper instanceof EstimatePaper)) {
            logger.info("userId:{},error:{}", userId, CommonErrors.RESOURCE_NOT_FOUND.getMessage());
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        StandardCard answerCard = paperAnswerCardService.createAnswerCard(paper, subject, userId, terminal);
        if (null != answerCard) {
            ValueOperations<String, StandardCard> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(getMatchCardRedisKey(paperId, subject, userId), answerCard, CACHE_DAY_TIME, TimeUnit.DAYS);

            //添加至总计数
            SetOperations<String, String> setOperations = redisTemplate.opsForSet();
            setOperations.add(getMatchAllKeyCache(paperId, subject), String.valueOf(userId));
        }
    }

    /**
     * 获取模考大赛 缓存用的key
     *
     * @param paperId 试卷ID
     * @param subject 科目
     * @param userId  用户ID
     * @return
     */
    public static String getMatchCardRedisKey(int paperId, int subject, long userId) {
        StringBuilder builder = new StringBuilder(64);
        return builder.append("_match:card:")
                .append(paperId).append(":")
                .append(subject).append(":")
                .append(userId)
                .toString();
    }

    public static String getMatchAllKeyCache(int paperId, int subject) {
        StringBuilder builder = new StringBuilder(64);
        return builder.append("_match:all:")
                .append(paperId).append(":")
                .append(subject).append(":")
                .toString();
    }

    /**
     * 从缓存中获取一张模考大赛答题卡
     *
     * @param paperId 试卷ID
     * @param subject 科目
     * @param userId  用户ID
     * @return 答题卡
     */
    public StandardCard getCachedMatchCardFromRedisAndDelete(int paperId, int subject, long userId) {
        String matchCardRedisKey = getMatchCardRedisKey(paperId, subject, userId);
        ValueOperations<String, StandardCard> valueOperations = redisTemplate.opsForValue();
        StandardCard standardCard = valueOperations.get(matchCardRedisKey);
        if (null != standardCard) {
            removeCacheInfo(paperId, subject, userId);
        }
        return standardCard;
    }

    /**
     * 删除缓存信息
     */
    public void removeCacheInfo(int paperId, int subject, long userId) {
        String matchCardRedisKey = getMatchCardRedisKey(paperId, subject, userId);
        redisTemplate.delete(matchCardRedisKey);
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        setOperations.remove(getMatchAllKeyCache(paperId, subject), String.valueOf(userId));
    }

    /**
     * 创建模考大赛 缓存答题卡信息
     *
     * @return 创建成功数量
     */
    @Async
    public void createMatchCacheAnswerCard(int paperId, boolean isWhite) throws BizException {
        ArrayList<Long> userIdList = Lists.newArrayList();
        if (isWhite) {
            userIdList.addAll(getMatchWhitUser());
        } else {
            userIdList.addAll(getAllUserIdList(paperId));
        }
        createMatchCacheAnswerCard(paperId, userIdList);
    }

    /**
     * 创建缓存答题卡
     *
     * @return 创建成功数量
     */
    @Async
    public void createMatchCacheAnswerCard(int paperId, List<Long> userIdList) throws BizException {
        if (CollectionUtils.isEmpty(userIdList)) {
            throw new BizException(ErrorResult.create(1000110, "不存在报名信息"));
        }
        Paper paper = paperDao.findById(paperId);
        if (null == paper) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        int catgory = paper.getCatgory();
        if (catgory != 1) {
            throw new BizException(ErrorResult.create(1000109, "试卷科目不是" + 1));
        }
        HashSet<Long> result = Sets.newHashSet();
        final int totalNum = userIdList.size();
        userIdList.forEach(userId -> {
            try {
                createCachedMatchCardAndPutRedis(paperId, catgory, userId, CREATE_DEFAULT_TERMINAL);
                result.add(userId);
                logger.info("创建缓存答题卡信息，处理USER_ID = {}，剩余数量 = {}", userId, totalNum - result.size());
            } catch (BizException e) {
                e.printStackTrace();
            } catch (WaitException e) {
                e.printStackTrace();
            }
        });
        logger.info("createMatchCacheAnswerCard >>>>>> 成功数量 ：{}", result.size());
    }

    @Async
    public void test() throws InterruptedException {
        for (int i = 0; i < 30; i++) {
            TimeUnit.SECONDS.sleep(1);
            logger.info(" test =>>>,{}", i);
        }
    }

    /**
     * 获取所有的报名用户ID
     *
     * @return 获取所有报名用户ID
     */
    private List<Long> getAllUserIdList(int paperId) {
        Criteria criteria = Criteria.where("paperId").is(paperId);
        List<MatchUserMeta> matchUserMetaList = mongoTemplate.find(new Query(criteria), MatchUserMeta.class);
        return matchUserMetaList.parallelStream().map(MatchUserMeta::getUserId).collect(Collectors.toList());
    }

    private List<Long> getMatchWhitUser() {
        SetOperations opsForSet = redisTemplate.opsForSet();
        Set<String> members = opsForSet.members(getMatchWhitUserReportKey());
        return members.parallelStream().map(Long::valueOf).collect(Collectors.toList());
    }

    /**
     * 考试结束后使用
     * @param matchId
     * @return
     */
    public int countMatchEnrollSize(int matchId){
        String totalEnrollCountKey = MatchRedisKeys.getTotalEnrollCountKey(matchId) + "_final";
        Example example = new Example(com.huatu.tiku.match.bean.entity.MatchUserMeta.class);
        example.and().andEqualTo("matchId",matchId);
        return countMatchUserMeta.apply(example,totalEnrollCountKey);
    }

    public int countMatchSubmitSize(int matchId){
        String submitKey = MatchRedisKeys.getMatchSubmitTotalKey(matchId);
        Example example = new Example(com.huatu.tiku.match.bean.entity.MatchUserMeta.class);
        example.and().andEqualTo("matchId",matchId).andNotEqualTo("practiceId",-1L);
        return countMatchUserMeta.apply(example,submitKey);
    }

    private final BiFunction<Example,String,Integer> countMatchUserMeta = ((example, key) -> {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object total = valueOperations.get(key);
        if(null != total){
            try {
                return Integer.parseInt(String.valueOf(total));
            }catch (Exception e){
                logger.error("key={},total={}",key,total);
                e.printStackTrace();
            }
        }
        int count =  matchUserMetaMapper.selectCountByExample(example);
        valueOperations.set(key,count+"",7,TimeUnit.DAYS);
        return count;
    });
}
