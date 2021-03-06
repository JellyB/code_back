package com.huatu.ztk.arena;

import com.huatu.ztk.arena.bean.*;
import com.huatu.ztk.arena.common.RedisArenaKeys;
import com.huatu.ztk.arena.dao.ArenaRoomDao;
import com.huatu.ztk.arena.dubbo.ArenaDubboService;
import com.huatu.ztk.arena.service.ArenaRoomService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.Result;
import com.huatu.ztk.paper.api.PracticeCardDubboService;
import com.huatu.ztk.paper.bean.AnswerCard;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by shaojieyue
 * Created time 2016-07-05 12:46
 */
public class ArenaRoomServiceTest extends BaseTest{
    private static final Logger logger = LoggerFactory.getLogger(ArenaRoomServiceTest.class);

    @Autowired
    private ArenaDubboService arenaDubboService;

    @Autowired
    private ArenaRoomService arenaRoomService;

    @Autowired
    private PracticeCardDubboService practiceCardDubboService;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private ArenaRoomDao arenaRoomDao;

    long uid = 12252065;

    @Test
    public void findByIdTest(){
        final ArenaRoom arenaRoom = arenaDubboService.findById(52583918L);
        System.out.println(JsonUtil.toJson(arenaRoom));
    }

    @Test
    public void createTest(){
        int[] counts = new int[]{2,4,8};
        for (int i = 0; i < 60; i++) {
            int count = counts[RandomUtils.nextInt(0,counts.length)];
            final ArenaRoom arenaRoom = arenaRoomService.create(-1);
            Assert.assertNotNull(arenaRoom);
            Assert.assertEquals(arenaRoom.getQcount(), ArenaConfig.getConfig().getQuestionCount());
            Assert.assertTrue(arenaRoom.getCreateTime()>0);
            Assert.assertTrue(arenaRoom.getId()>0);
            Assert.assertNotNull(arenaRoom.getPracticePaper());
        }
    }

    @Test
    public void addArenaResultTest(){
        final int arenaId = 52487248;
        //初始化
        ArenaRoom arenaRoom = arenaRoomService.findById(arenaId);
        arenaRoom.setStatus(ArenaRoomStatus.RUNNING);
        arenaRoom.setResults(null);
        arenaRoom.setWinner(0);
        arenaRoomDao.save(arenaRoom);

        long practiceId = 1525088653376749568L;
        arenaRoomService.addArenaResult(practiceId);
        AnswerCard answerCard = practiceCardDubboService.findById(practiceId);
        arenaRoom = arenaRoomService.findById(arenaId);
        ArenaResult arenaResult = null;
        for (ArenaResult result : arenaRoom.getResults()) {
            if (result.getUid() == answerCard.getUserId()) {
                arenaResult = result;
            }
        }
        Assert.assertEquals(answerCard.getRcount(),arenaResult.getRcount());
        Assert.assertEquals(answerCard.getExpendTime(),arenaResult.getElapsedTime());
        Assert.assertEquals(answerCard.getUserId(),arenaResult.getUid());
        Assert.assertEquals(arenaRoom.getStatus(),ArenaRoomStatus.RUNNING);
        Assert.assertEquals(0,arenaRoom.getWinner());

        arenaRoomService.addArenaResult(practiceId);
        answerCard = practiceCardDubboService.findById(practiceId);
        arenaRoom = arenaRoomService.findById(arenaId);
        arenaResult = null;
        for (ArenaResult result : arenaRoom.getResults()) {
            if (result.getUid() == answerCard.getUserId()) {
                arenaResult = result;
            }
        }
        Assert.assertEquals(answerCard.getRcount(),arenaResult.getRcount());
        Assert.assertEquals(answerCard.getExpendTime(),arenaResult.getElapsedTime());
        Assert.assertEquals(answerCard.getUserId(),arenaResult.getUid());
        Assert.assertEquals(arenaRoom.getStatus(),ArenaRoomStatus.RUNNING);
        Assert.assertEquals(0,arenaRoom.getWinner());

        practiceId = 1525088653443858432L;
        arenaRoomService.addArenaResult(practiceId);
        answerCard = practiceCardDubboService.findById(practiceId);
        arenaRoom = arenaRoomService.findById(arenaId);
        arenaResult = null;
        for (ArenaResult result : arenaRoom.getResults()) {
            if (result.getUid() == answerCard.getUserId()) {
                arenaResult = result;
            }
        }
        Assert.assertEquals(answerCard.getRcount(),arenaResult.getRcount());
        Assert.assertEquals(answerCard.getExpendTime(),arenaResult.getElapsedTime());
        Assert.assertEquals(answerCard.getUserId(),arenaResult.getUid());
        Assert.assertEquals(arenaRoom.getStatus(),ArenaRoomStatus.RUNNING);
        Assert.assertEquals(0,arenaRoom.getWinner());

        practiceId = 1525088653485801472L;
        arenaRoomService.addArenaResult(practiceId);
        answerCard = practiceCardDubboService.findById(practiceId);
        arenaRoom = arenaRoomService.findById(arenaId);
        arenaResult = null;
        for (ArenaResult result : arenaRoom.getResults()) {
            if (result.getUid() == answerCard.getUserId()) {
                arenaResult = result;
            }
        }
        Assert.assertEquals(answerCard.getRcount(),arenaResult.getRcount());
        Assert.assertEquals(answerCard.getExpendTime(),arenaResult.getElapsedTime());
        Assert.assertEquals(answerCard.getUserId(),arenaResult.getUid());
        Assert.assertEquals(arenaRoom.getStatus(),ArenaRoomStatus.RUNNING);
        Assert.assertEquals(0,arenaRoom.getWinner());

        //最后一个
        practiceId = 1525088653527744512L;
        arenaRoomService.addArenaResult(practiceId);
        answerCard = practiceCardDubboService.findById(practiceId);
        arenaRoom = arenaRoomService.findById(arenaId);
        arenaResult = null;
        for (ArenaResult result : arenaRoom.getResults()) {
            if (result.getUid() == answerCard.getUserId()) {
                arenaResult = result;
            }
        }
        Assert.assertEquals(answerCard.getRcount(),arenaResult.getRcount());
        Assert.assertEquals(answerCard.getExpendTime(),arenaResult.getElapsedTime());
        Assert.assertEquals(answerCard.getUserId(),arenaResult.getUid());
        Assert.assertEquals(arenaRoom.getStatus(),ArenaRoomStatus.FINISHED);
        Assert.assertEquals(10239481,arenaRoom.getWinner());
    }

    @Test
    public void closeArenaTest(){
        final int arenaId = 52487248;
        //初始化
        ArenaRoom arenaRoom = arenaRoomService.findById(arenaId);
        arenaRoom.setStatus(ArenaRoomStatus.RUNNING);
        arenaRoom.setResults(null);
        arenaRoom.setWinner(0);
        arenaRoomDao.save(arenaRoom);
        final String arenaDayRankKey = RedisArenaKeys.getArenaDayRankKey(DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd"));
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        final Map<Long, Integer> oldMap = arenaRoom.getPlayerIds().stream().collect(Collectors.toMap(obj -> obj, obj -> Optional.ofNullable(zSetOperations.score(arenaDayRankKey, obj.toString())).orElse(0d).intValue()));


        arenaRoomService.closeArena(arenaId);
        arenaRoom = arenaRoomService.findById(arenaId);
        Assert.assertNotNull(arenaRoom);
        Assert.assertEquals(arenaRoom.getQcount(),ArenaConfig.getConfig().getQuestionCount());
        Assert.assertEquals(arenaRoom.getStatus(),ArenaRoomStatus.FINISHED);
        Assert.assertEquals(10239481,arenaRoom.getWinner());
        for (int i = 0; i < arenaRoom.getPlayerIds().size(); i++) {
            Assert.assertEquals(arenaRoom.getPlayerIds().get(i).longValue(),arenaRoom.getPlayers().get(i).getUid());
            Assert.assertEquals(arenaRoom.getPlayerIds().get(i).longValue(),arenaRoom.getResults()[i].getUid());
        }
        for (int i = 0; i < arenaRoom.getPractices().size(); i++) {
            final AnswerCard answerCard = practiceCardDubboService.findById(arenaRoom.getPractices().get(i));
            Assert.assertEquals(answerCard.getUserId(),arenaRoom.getPlayerIds().get(i).longValue());
        }

        //检查胜场情况
        for (Long aLong : arenaRoom.getPlayerIds()) {
            final int intValue = zSetOperations.score(arenaDayRankKey, aLong.toString()).intValue();
            if (aLong.longValue() == arenaRoom.getWinner()) {
                Assert.assertEquals(intValue,oldMap.get(aLong).intValue()+1);
            }else {
                Assert.assertEquals(intValue,oldMap.get(aLong).intValue());
            }
        }

    }

    @Test
    public void historyTest(){
        final long userId = 10264614;
        long cursor = Long.MAX_VALUE;
        List<ArenaRoomSimple> records = arenaRoomDao.findForPage(userId,cursor,20);
        Assert.assertNotNull(records);
        List<Integer> statusList = records.stream().map(bean->bean.getStatus()).collect(Collectors.toList());
        for(Integer status: statusList){
            Assert.assertEquals(status,new Integer(3));
        }
        List<Long> winnerList = records.stream().map(bean->bean.getWinner()).collect(Collectors.toList());
        for(Long winner: winnerList){
            Assert.assertNotNull(winner);
        }
       logger.info("records={}", JsonUtil.toJson(records));

    }
}
