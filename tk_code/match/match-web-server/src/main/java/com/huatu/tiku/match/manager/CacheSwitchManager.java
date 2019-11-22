package com.huatu.tiku.match.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.ztk.paper.bean.Match;
import org.apache.commons.collections.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

public class CacheSwitchManager {

    private static final Integer MATCH_INTERVAL_MINUTES_SIZE = 10;      //十分钟的缓存区间

    /**
     * guava缓存使用控制开关
     */
    public static Cache<Integer, CacheBean> Guava_Switch_Cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(30)
            .build();

    /**
     * 判断读取缓存开关
     *
     * @param subject
     * @return
     */
    public static boolean isReadOpen(Integer subject) {
        CacheBean ifPresent = Guava_Switch_Cache.getIfPresent(subject);
        if (null == ifPresent) {
            return false;
        }
        //open标识true并且高峰期未结束，则返回true，否则返回false
        return ifPresent.isOpen() && ifPresent.getEndTime() > System.currentTimeMillis();
    }


    /**
     * 检查用户缓存开关(开关关闭或者高峰期已过，才会进入该阶段)
     *
     * @param matchList
     * @return
     */
    public static boolean checkMatchCacheFlag(List<Match> matchList, Integer subject) {
        CacheBean ifPresent = Guava_Switch_Cache.getIfPresent(subject);
        long currentTimeMillis = System.currentTimeMillis();
        if (null != ifPresent) {      //如果不为空，判断是否要写入guava缓存
            long endTime = ifPresent.getEndTime();
            if (endTime >= currentTimeMillis) {
                long startTime = ifPresent.getStartTime();
                if (startTime <= currentTimeMillis) {     //开始进入高峰期
                    ifPresent.setOpen(true);
                    return true;
                } else {                      //仍未进入高峰期
                    return false;
                }
            } else {      //高峰期已过，则直接删除开关标识
                Guava_Switch_Cache.invalidate(subject);
                return false;
            }
        }
        if (CollectionUtils.isEmpty(matchList)) {//如果查询不到模考大赛信息，则标识数据需要情况
            Guava_Switch_Cache.invalidate(subject);
            return false;
        }
        OptionalLong startMin = matchList.stream().mapToLong(Match::getStartTime).filter(i -> checkDueTime(i, currentTimeMillis)).min();
        OptionalLong endMin = matchList.stream().mapToLong(Match::getEndTime).filter(i -> checkDueTime(i, currentTimeMillis)).min();
        long dueTime = Math.min(startMin.orElse(-1), endMin.orElse(-1));
        if (dueTime < 0) {      //不存在高峰期节点
            Guava_Switch_Cache.invalidate(subject);
            return false;
        }
        CacheBean build = creatBean(dueTime,currentTimeMillis,subject);
        if (isCreateSwitch(dueTime, currentTimeMillis)) {      //允许创建开关控制标识判断
            Guava_Switch_Cache.put(subject, build);
        }
        return build.isOpen();
    }

    private static CacheBean creatBean(long dueTime, long currentTimeMillis, Integer subject) {
        return CacheBean.builder().subject(subject).startTime(dueTime - TimeUnit.MINUTES.toMillis(MATCH_INTERVAL_MINUTES_SIZE) / 2)
                .endTime(dueTime + TimeUnit.MINUTES.toMillis(MATCH_INTERVAL_MINUTES_SIZE) / 2)
                .open(isRange(dueTime, MATCH_INTERVAL_MINUTES_SIZE, currentTimeMillis))
                .build();
    }

    public static boolean isRange(long dueTime, Integer size, long current) {
        return (dueTime - TimeUnit.MINUTES.toMillis(size) / 2) < current &&
                (dueTime + TimeUnit.MINUTES.toMillis(size) / 2) > current;
    }

    /**
     * 时间节点是否可以生成开关（离时间节点还有一个小时时，前后size分钟内，不允许生成缓存控制标识开关）
     *
     * @return
     */
    public static boolean isCreateSwitch(long dueTime, long current) {
        dueTime = dueTime - TimeUnit.HOURS.toMillis(1);
        return !((dueTime - TimeUnit.MINUTES.toMillis(MATCH_INTERVAL_MINUTES_SIZE)) < current && (dueTime + TimeUnit.MINUTES.toMillis(MATCH_INTERVAL_MINUTES_SIZE)) > current);
    }

    /**
     * 时间节点判断（当前时间未查过高峰期节点值，则都是有效节点）
     *
     * @param dueTime
     * @param current
     * @return
     */
    private static boolean checkDueTime(long dueTime, long current) {
        long time = TimeUnit.MINUTES.toMillis(MATCH_INTERVAL_MINUTES_SIZE);
        return dueTime + time / 2 > current;
    }

//    public static void main(String[] args) {
//        System.out.println(System.currentTimeMillis());
//        System.out.println(new Date(2019-1900,7-1,4,20,15,0).getTime());
//        Match match = Match.builder().paperId(1)
//                .startTime(new Date(2019-1900,7-1,4,20,10,0).getTime())
//                .endTime(new Date(2019-1900,7-1,4,22,0,0).getTime())
//                .build();
//        boolean cacheFlag = checkMatchCacheFlag(Lists.newArrayList(match), 1);
//        System.out.println("cacheFlag = " + cacheFlag);
//        boolean readOpen = isReadOpen(1);
//        System.out.println("readOpen = " + readOpen);
//        CacheBean ifPresent = Guava_Switch_Cache.getIfPresent(1);
//        System.out.println("new Date(ifPresent.getStartTime()).toString() = " + new Date(ifPresent.getStartTime()).toString());
//        System.out.println("new Date(ifPresent.getStartTime()).toString() = " + new Date(ifPresent.getEndTime()).toString());
//    }
}
