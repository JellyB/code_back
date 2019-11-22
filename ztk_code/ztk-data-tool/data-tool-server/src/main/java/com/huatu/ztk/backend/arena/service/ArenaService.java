package com.huatu.ztk.backend.arena.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.arena.bean.ArenaUserSummary;
import com.huatu.ztk.arena.bean.Player;
import com.huatu.ztk.arena.bean.UserArenaRecord;
import com.huatu.ztk.backend.arena.bean.UserRank;
import com.huatu.ztk.backend.arena.dao.ArenaDao;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.bean.UserDto;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by linkang on 11/18/16.
 */

@Service
public class ArenaService {
    private static final Logger logger = LoggerFactory.getLogger(ArenaService.class);

    //最大数量100个,只返回前100的用户
    public static final int TODAY_MAX_RANK_COUNT = 100;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ArenaDao arenaDao;


    /**
     * 查询某日的竞技排名
     *
     * @param date
     * @return
     */
    public Object findRankByDate(String date) {
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        final String arenaDayRankKey = getArenaDayRankKey(date);
//        final String arenaDayRankKey = RedisArenaKeys.getArenaDayRankKey(DateFormatUtils.format(date, "yyyyMMdd"));
        final Set<String> uidStrs = zSetOperations.reverseRange(arenaDayRankKey, 0, TODAY_MAX_RANK_COUNT - 1);
        //若当天暂未有任何用户参加过竞技比赛，返回null
        if (CollectionUtils.isEmpty(uidStrs)) {
            return null;
        }
        List<UserArenaRecord> records = Lists.newArrayList();
        for (String uidStr : uidStrs) {
            //获胜场数
            final int winCount = zSetOperations.score(arenaDayRankKey, uidStr).intValue();

            final Player player = findPlayerById(Long.valueOf(uidStr));

            if (player == null) {
                logger.warn("missed user,id={}", uidStr);
                continue;
            }

            final UserArenaRecord arenaRecord = UserArenaRecord.builder()
                    .uid(player.getUid())
                    .player(player)
                    .winCount(winCount)
                    .build();
            records.add(arenaRecord);
        }

        //对排行做倒叙排
        records.sort((a,b) -> b.getWinCount() - a.getWinCount());

        for (int i = 0; i < records.size(); i++) {//设置名次
            records.get(i).setRank(i + 1);
        }

        return records;
    }


    /**
     * 查询账号的竞技排名
     *
     * @param account
     * @return
     * @throws BizException
     */
    public Object findRankByAccount(String account) throws BizException {
        UserDto userDto = arenaDao.findUserByAny(account);
        if (userDto == null) {
            logger.warn("missed user,account={}", account);
            return null;
        }

        long uid = userDto.getId();

        List<ArenaUserSummary> summaryList = arenaDao.findSummaryListByUserId(uid);

        //总统计以-1结尾
        List<ArenaUserSummary> totalList = summaryList.stream().filter(summary -> summary.getId().endsWith("-1")).collect(Collectors.toList());

        List<UserRank> ranks = Lists.newArrayList();
        for (ArenaUserSummary summary : summaryList) {
            String summaryId = summary.getId();
            if (summaryId.endsWith("-1")) {
                continue;
            }
            //当天统计的id为uid + DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd")
            //取子字符串即可获得竞技日期
            String date = summaryId.substring(String.valueOf(uid).length());
            UserRank userRank = UserRank.builder()
                    .winCount(summary.getWinCount())
                    .failCount(summary.getFailCount())
                    .date(date)
                    .build();
            ranks.add(userRank);
        }

        Map map = new LinkedHashMap<>();
        map.put("userDto", userDto);
        map.put("totalSummary", totalList.isEmpty() ? null : totalList.get(0));
        map.put("userRank", ranks);
        return map;
    }


    /**
     * 通过uid查询玩家信息
     *
     * @param uid
     * @return
     */
    private Player findPlayerById(long uid) {
        final UserDto userDto = arenaDao.findUserById(uid);
        if (userDto == null) {
            return null;
        }

        final Player player = Player.builder()
                .uid(userDto.getId())
                .avatar(userDto.getAvatar())
                .nick(userDto.getNick())
                .build();
        return player;
    }


    /**
     * 用户日排名key
     *
     * @param date 排名日期
     * @return
     */
    private static final String getArenaDayRankKey(String date) {
        return "arena_rank_" + date;
    }
}
