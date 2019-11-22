package com.huatu.ztk.paper.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.MatchBackendStatus;
import com.huatu.ztk.paper.common.MatchConfig;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.paper.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author by linkang on 17-7-14.
 * @modify zhouwei 2017-12-23 14:59:05
 */
@Repository
public class MatchDao {
    private static final Logger logger = LoggerFactory.getLogger(MatchDao.class);


    @Autowired
    private MongoTemplate mongoTemplate;

    @Resource(name = "coreRedisTemplate")
    private ValueOperations valueOperations;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MatchConfig matchConfig;

    /**
     * 模考大赛 试卷
     */
    private final Cache<Integer, List<Integer>> SUBJECT_PAPER = CacheBuilder.newBuilder()
            .expireAfterWrite(3 * 60, TimeUnit.SECONDS)
            .build();

    /**
     * 查询大赛信息
     *
     * @param paperId
     * @return
     */
    public Match findById(int paperId) {
        return mongoTemplate.findById(paperId, Match.class);
    }

    /**
     * 保存用户大赛报名信息
     *
     * @param userMeta
     */
    public void saveUserMeta(MatchUserMeta userMeta) {
        logger.info("save match user meta,json={}", JsonUtil.toJson(userMeta));
        mongoTemplate.save(userMeta);
    }

    /**
     * 更新练习id
     *
     * @param userMeta
     */
    public int updatePracticeId(MatchUserMeta userMeta) {
        logger.info("update match user meta,json={}", JsonUtil.toJson(userMeta));
        Update update = new Update();
        update.set("practiceId", userMeta.getPracticeId());

        Criteria criteria = Criteria.where("_id").is(userMeta.getId());
        criteria.and("practiceId").is(-1);

        Query query = new Query(criteria);
        return mongoTemplate.updateFirst(query, update, MatchUserMeta.class).getN();
    }

    /**
     * 查询用户大赛报名信息
     *
     * @param id
     * @return
     */
    public MatchUserMeta findMatchUserMeta(String id) {
        return mongoTemplate.findById(id, MatchUserMeta.class);
    }

    public List<MatchUserMeta> findMatchUserMetaByIds(List<String> ids) {
        Criteria criteria = Criteria.where("_id").in(ids);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, MatchUserMeta.class);
    }

    /**
     * 查询所有模考大赛
     *
     * @return
     */
    public List<Match> findAll(long startTime, int tag, int subject) {

        Criteria criteria = Criteria.where("subject").is(subject)
                .and("status").is(MatchBackendStatus.AUDIT_SUCCESS);

        if (startTime > 0) {
            criteria.and("startTime").lte(startTime);
        }

        if (tag > 0) {
            criteria.and("tag").is(tag);
        }

        Query query = new Query(criteria);
        //时间升序
        query.with(new Sort(Sort.Direction.ASC, "startTime"));
        logger.info("query={}", query);
        return mongoTemplate.find(query, Match.class);
    }

    /**
     * 1、当前科目下审核通过的模考
     * 2、今天0点以后 最近一个日期内所有的模考
     * 示例：今天 1月1号10：00点     未来的模考：1月2号 10：00到11：00（A）12：00 到 13点(B)  1月3号 10：00到11：00 (C) 12：00 到 13点(D)
     * 今天去查 ，只展示 2号的两场  顺序： AB
     * 明天（1月2号 13点前去查 ）  和1月10号查的结果一样，只有 2号的两场  顺序： AB
     * 13点以后展示3号的两场 顺序：CD
     * <p>
     * 排序规则：当天的考试 时间由小到大
     * <p>
     * 查询当前科目下所有有效的模考大赛
     *
     * @return
     */
    public List<Match> findUsefulMatchOld(int subject) {

        if (subject == SubjectType.GWY_XINGCE_SHENLUN) {
            subject = SubjectType.GWY_XINGCE;
        }

        Criteria criteria = Criteria.where("subject").is(subject)
                .and("status").is(MatchBackendStatus.AUDIT_SUCCESS)
                .and("startTime").lte(DateUtil.getTodayStartMillions());
        Query query = new Query(criteria);
        //时间升序查询
        //暂时为解决pc不展示多个模考的问题，修改排序规则
        query.with(new Sort(Sort.Direction.ASC, "startTime"))
                .limit(10);
        List<Match> matches = mongoTemplate.find(query, Match.class);
        //过滤  只展示 今天0点以后 最近一个日期内的模考
        List<Match> usefulMatches = null;
        if (CollectionUtils.isNotEmpty(matches)) {
            while (true) {
                //取最近的日期，日期如果比当天大，则可以直接返回，如果是当天判断是否每个考试都已结束，如果结束，则循环处理下一天的数据
                String current = DateUtil.getFormatDateString(matches.get(0).getStartTime());
                usefulMatches = matches.stream()
                        .filter(match -> DateUtil.getFormatDateString(match.getStartTime()).equals(current))
                        .collect(Collectors.toList());
                if (!current.equals(DateUtil.getFormatDateString(System.currentTimeMillis()))) {
                    break;
                }
                boolean isFinished = true;  //当天的考试是否都已结束
                for (Match match : usefulMatches) {
                    long endTime = match.getEssayPaperId() > 0 ? match.getEssayEndTime() : match.getEndTime();
                    if (endTime + TimeUnit.MINUTES.toMillis(matchConfig.getNextMatchDelayTime()) > System.currentTimeMillis()) {
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
        logger.info("size={},limit={}", usefulMatches.size(), matchConfig.getMaxMatchShowSize());
        if (usefulMatches.size() > matchConfig.getMaxMatchShowSize()) {
            usefulMatches = usefulMatches.subList(usefulMatches.size() - matchConfig.getMaxMatchShowSize(), usefulMatches.size());
        }
        return usefulMatches;
    }

    /**
     * 查询模考大赛首页列表 - 对应 findUsefulMatchOld
     *
     * @param subject 科目ID
     * @return 模考大赛列表
     */
    public List<Match> findUsefulMatch(int subject) {
        List<Integer> allUsefulMatchPaperId = getAllUsefulMatchPaperId(subject);
        if (CollectionUtils.isEmpty(allUsefulMatchPaperId)) {
            return Lists.newArrayList();
        }

        if (subject == SubjectType.GWY_XINGCE_SHENLUN) {
            subject = SubjectType.GWY_XINGCE;
        }
        Criteria criteria = Criteria.where("subject").is(subject)
                .and("status").is(MatchBackendStatus.AUDIT_SUCCESS)
                .and("startTime").gte(DateUtil.getTodayStartMillions())
                .and("_id").in(allUsefulMatchPaperId);

        Query query = new Query(criteria);
        //时间升序查询
        //暂时为解决pc不展示多个模考的问题，修改排序规则
        query.with(new Sort(Sort.Direction.ASC, "startTime"))
                .limit(10);
        //logger.info("match dao query={}", query);
        List<Match> matches = mongoTemplate.find(query, Match.class);
        if (CollectionUtils.isEmpty(matches)) {
            return Lists.newArrayList();
        }
        //过滤已经结束的模考大赛信息
        List<Match> matchList = matches.stream()
                .filter(match -> {
                    long endTime = match.getEssayPaperId() > 0 ? match.getEssayEndTime() : match.getEndTime();
                    return (endTime + TimeUnit.MINUTES.toMillis(matchConfig.getNextMatchDelayTime()) > System.currentTimeMillis());
                })
                .collect(Collectors.toList());
        /**
         * 加入自定义筛选功能，如果出现自定义筛选，则只显示当前的自定义
         */
        List<Integer> appMatchShowPaperIdInfoCollection = matchConfig.getAppMatchShowPaperIdInfoCollection();
        if (CollectionUtils.isNotEmpty(appMatchShowPaperIdInfoCollection)) {
            List<Match> resultMatchList = matchList.stream()
                    .filter(match -> appMatchShowPaperIdInfoCollection.contains(match.getPaperId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(resultMatchList)) {
                resultMatchList.sort(Comparator.comparingInt(match -> appMatchShowPaperIdInfoCollection.indexOf(match.getPaperId())));
                return resultMatchList;
            }
        }

        if (CollectionUtils.isNotEmpty(matchList) && matchList.size() > matchConfig.getMaxMatchShowSize()) {
            return matchList.subList(0, matchConfig.getMaxMatchShowSize());
        }
        return matchList;
    }


    /**
     * 查询最新的/当前的模考大赛
     *
     * @return
     */
    public Match findCurrent(int subject) {
        if (subject == SubjectType.GWY_XINGCE_SHENLUN) {
            subject = SubjectType.GWY_XINGCE;
        }
        Criteria criteria = Criteria.where("subject").is(subject)
                .and("status").is(MatchBackendStatus.AUDIT_SUCCESS);
        Query query = new Query(criteria);
        //时间倒序
        query.with(new Sort(Sort.Direction.DESC, "startTime"))
                .with(new Sort(Sort.Direction.ASC, "_id"))
                .limit(2);
        List<Match> matches = mongoTemplate.find(query, Match.class);

        //默认展示最新的，除非当前时间包含在第二个考试时间➕一小时 时间内，或者第二个还没开始则展示第二个
        Match result = chooseMatchWithEssay(matches);

        return result;
    }

    /**
     * 获取所有上线的试卷
     *
     * @param subject 科目
     * @return 试卷ID
     */
    private List<Integer> getAllUsefulMatchPaperId(int subject) {
        List<Integer> subjectPaperIfPresent = SUBJECT_PAPER.getIfPresent(subject);
        if (CollectionUtils.isNotEmpty(subjectPaperIfPresent)) {
            return subjectPaperIfPresent;
        }

        if (subject == SubjectType.GWY_XINGCE_SHENLUN) {
            subject = SubjectType.GWY_XINGCE;
        }
        Criteria criteria = Criteria.where("catgory").is(subject)
                .and("onlineTime").lte(System.currentTimeMillis())
                .and("offlineTime").gte(System.currentTimeMillis())
                .and("type").is(PaperType.MATCH);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "startTime"))
                .with(new Sort(Sort.Direction.ASC, "_id"));

        List<Paper> list = mongoTemplate.find(query, Paper.class);
        List<Integer> ids = list.stream().map(Paper::getId).collect(Collectors.toList());
        SUBJECT_PAPER.put(subject, ids);
        return ids;
    }

    /**
     * 查询最新的/当前的模考大赛 - 新增配置文件的权重
     *
     * @param subject 科目
     * @return
     */
    public Match findCurrentForPc(int subject) {
        List<Integer> allUsefulMatchPaperId = getAllUsefulMatchPaperId(subject);
        if (CollectionUtils.isEmpty(allUsefulMatchPaperId)) {
            return null;
        }
        if (subject == SubjectType.GWY_XINGCE_SHENLUN) {
            subject = SubjectType.GWY_XINGCE;
        }
        Criteria criteria = Criteria.where("subject").is(subject)
                .and("status").is(MatchBackendStatus.AUDIT_SUCCESS)
                .and("_id").in(allUsefulMatchPaperId);
        Query query = new Query(criteria);
        //时间倒序
        query.with(new Sort(Sort.Direction.DESC, "startTime"))
                .with(new Sort(Sort.Direction.ASC, "_id"));
        List<Match> matches = mongoTemplate.find(query, Match.class);
        if (CollectionUtils.isEmpty(matches)) {
            return null;
        }
        List<Integer> pcMatchShowPaperIdInfoCollection = matchConfig.getPcMatchShowPaperIdInfoCollection();
        if (CollectionUtils.isNotEmpty(pcMatchShowPaperIdInfoCollection)) {
            Optional<Match> anyMatch = matches.stream()
                    .filter(match -> pcMatchShowPaperIdInfoCollection.contains(match.getPaperId()))
                    .findAny();
            if (anyMatch.isPresent()) {
                return anyMatch.get();
            }
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        return chooseMatchWithEssay(matches.subList(0, 2));

    }

    private Match chooseMatch(List<Match> matches) {
        if (CollectionUtils.isNotEmpty(matches)) {
            //如果只有一个，则直接返回
            if (matches.size() == 1) {
                return matches.get(0);
            } else {
                long currentTime = System.currentTimeMillis();
                //1是时间比较小的
                Match match = matches.get(1);
                if (match == null) {
                    return matches.get(0);
                }
                //如果时间小的考试还没开始，或者结束在一小时之内则优先展示
                if (match.getStartTime() > currentTime || (match.getEndTime() + TimeUnit.HOURS.toMillis(1) > currentTime)) {
                    return match;
                } else {
                    return matches.get(0);
                }
            }


        }
        return null;
    }

    /**
     * 查询模考大赛
     *
     * @param matches
     * @return
     */
    private Match chooseMatchWithEssay(List<Match> matches) {
        if (CollectionUtils.isNotEmpty(matches)) {
            //如果只有一个，则直接返回
            if (matches.size() == 1) {
                return matches.get(0);
            } else {
                long currentTime = System.currentTimeMillis();
                //1是时间比较小的
                Match match = matches.get(0);
                //TODO 暂时处理pc不支持多个的情况
                /* if (match == null) {*/
                /*     return matches.get(1);*/
                /* }*/
                //如果是联合申论的模考大赛，则结束时间以申论结束时间为准
                long endTime = match.getEndTime();
                if (0 < match.getEssayPaperId()) {
                    endTime = match.getEssayEndTime();
                }
                //如果时间小的考试还没开始，或者结束在一小时之内则优先展示
                if (match.getStartTime() > currentTime || (endTime + TimeUnit.MINUTES.toMillis(matchConfig.getNextMatchDelayTime()) > currentTime)) {
                    return match;
                } else {
                    return matches.get(0);
                }
            }


        } else {
            logger.error("have no matches");
        }
        return null;
    }


    /**
     * 查询用户所有模考大赛信息
     *
     * @return
     */
    public List<MatchUserMeta> findAllMatchUserMeta(List<String> ids) {
        Criteria criteria = Criteria.where("_id").in(ids);
        return mongoTemplate.find(new Query(criteria), MatchUserMeta.class);
    }


    /**
     * 仿照试卷汇总的方法，用groupby查询出所有集合中的所有科目id
     *
     * @return
     */
    public List<Integer> findSubjects() {
        final Criteria criteria = Criteria.where("status").is(MatchBackendStatus.AUDIT_SUCCESS);
        GroupByResults<Map> results = mongoTemplate.group(
                criteria,
                "ztk_match",
                GroupBy.key("subject").initialDocument("{ count: 0 }").reduceFunction("function(doc, prev) { prev.count += 1 }"),
                Map.class);

        ArrayList<Map> maps = Lists.newArrayList(results.iterator());

        List<Integer> subjectIds = new ArrayList<>();
        for (Map map : maps) {
            if (map.get("subject") != null) {
                subjectIds.add(Double.valueOf(map.get("subject") + "").intValue());
            } else {
                logger.error("map is null");
            }

        }
        logger.info("subjectIds={}", subjectIds);
        return subjectIds;
    }

    public Long findPastMatchesTotal(int tag, int subject) {
        Criteria criteria = Criteria.where("subject").is(subject)
                .and("status").is(MatchBackendStatus.AUDIT_SUCCESS)
                .and("endTime").lte(System.currentTimeMillis());
        if (tag > 0) {
            criteria.and("tag").is(tag);
        }

        Query query = new Query(criteria);
        return mongoTemplate.count(query, Match.class);


    }

    public List<Match> findPastMatches(int tag, int offset, int size, int subject) {
        Criteria criteria = Criteria.where("subject").is(subject)
                .and("status").is(MatchBackendStatus.AUDIT_SUCCESS)
                .and("endTime").lte(System.currentTimeMillis());
        if (tag > 0) {
            criteria.and("tag").is(tag);
        }

        Query query = new Query(criteria);
        //时间升序
        query.with(new Sort(Sort.Direction.DESC, "startTime")).skip(Integer.max((offset - 1), 0) * size).limit(size);
        return mongoTemplate.find(query, Match.class);


    }

    /**
     * PC端模考大赛数据返回
     * --活动期间的所有模考大赛或者自定义配置的模考大赛（diconf）
     * --按照时间开始顺序排序,分区段，考试结束查过30分钟的放到列表后面
     *
     * @param subjectId
     * @return
     */
    public List<Match> findListByPc(int subjectId) {
        List<Integer> allUsefulMatchPaperId = getAllUsefulMatchPaperId(subjectId);
        if (CollectionUtils.isEmpty(allUsefulMatchPaperId)) {
            return Lists.newArrayList();
        }
        Criteria criteria = Criteria.where("subject").is(subjectId)
                .and("status").is(MatchBackendStatus.AUDIT_SUCCESS)
                .and("_id").in(allUsefulMatchPaperId);

        Query query = new Query(criteria);
        //时间升序查询
        //暂时为解决pc不展示多个模考的问题，修改排序规则
        query.with(new Sort(Sort.Direction.ASC, "startTime"))
                .limit(10);
        //logger.info("match dao query={}", query);
        List<Match> matches = mongoTemplate.find(query, Match.class);
        if (CollectionUtils.isEmpty(matches)) {
            return Lists.newArrayList();
        }
        /**
         * 加入自定义筛选功能，如果出现自定义筛选，则只显示当前的自定义
         */
        List<Integer> pcMatchShowPaperIdInfoCollection = matchConfig.getPcMatchShowPaperIdInfoCollection();
        if (CollectionUtils.isNotEmpty(pcMatchShowPaperIdInfoCollection)) {
            List<Match> resultMatchList = matches.stream()
                    .filter(match -> pcMatchShowPaperIdInfoCollection.contains(match.getPaperId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(resultMatchList)) {
                return resultMatchList;
            }
        }
        long currentTimeMillis = System.currentTimeMillis();
        long delayTime = TimeUnit.MINUTES.toMillis(matchConfig.getNextMatchDelayTime());
        //考试结束未超过30分钟的
        List<Match> preMatches = matches.stream().filter(i -> i.getEndTime() + delayTime > currentTimeMillis).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(preMatches)){
            return preMatches;
        }
        return Lists.newArrayList();
    }

    public List<MatchUserMeta> findMetaByPaperWithPractice(int paperId) {
        Criteria criteria = Criteria.where("paperId").is(paperId)
                .and("practiceId").ne(-1);

        Query query = new Query(criteria);
        return mongoTemplate.find(query,MatchUserMeta.class);
    }
}
