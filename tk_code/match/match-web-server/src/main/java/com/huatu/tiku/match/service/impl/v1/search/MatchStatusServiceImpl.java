package com.huatu.tiku.match.service.impl.v1.search;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.bo.MatchHeadUserBo;
import com.huatu.tiku.match.common.MatchHeadUserInfo;
import com.huatu.tiku.match.common.MatchSimpleStatus;
import com.huatu.tiku.match.common.TimeGapConstant;
import com.huatu.tiku.match.enums.MatchStatusEnum;
import com.huatu.tiku.match.manager.MatchManager;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.paper.PaperService;
import com.huatu.tiku.match.service.v1.search.MatchStatusService;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author huangqingpeng
 * 首页用户数据缓存维护
 */
@Service
@Slf4j
public class MatchStatusServiceImpl implements MatchStatusService {
    /**
     * 报名人数及最新的状态用户数据查询服务
     */
    @Autowired
    private MatchUserMetaService matchUserMetaService;
    /**
     * 查询缓存试卷信息
     */
    @Autowired
    private PaperService paperService;

    @Autowired
    private MatchManager matchManager;

    /**
     * 存在考试高峰期的用户首页数据缓存
     */
    Cache<String, MatchHeadUserInfo> MATCH_HEAD_USER_INFO_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.HOURS)
            .maximumSize(10000)
            .build();

    @Override
    public List<MatchHeadUserBo> getMatchHeadUserInfo(int userId, int subject) throws BizException {
        List<Match> matches = matchManager.findMatchesBySubjectWithCache(subject);
        if (CollectionUtils.isEmpty(matches)) {
            return Lists.newArrayList();
        }
        boolean cacheFlag = checkCacheFlag(matches);
        Function<Integer, MatchSimpleStatus> initStatus = (matchId) -> MatchSimpleStatus.builder().matchId(matchId)
                .userId(userId)
                .positionId(-10)
                .practiceId(-1l)
                .submitFlag(0)
                .build();
        if (cacheFlag) {
            //使用guava缓存逻辑
            String cache = getUserInfoCache(userId, subject);
            MatchHeadUserInfo matchHeadUserInfo = MATCH_HEAD_USER_INFO_CACHE.getIfPresent(cache);
            BiFunction<MatchHeadUserInfo, Integer, MatchSimpleStatus> getSimpleStatus = ((userInfo, id) -> {
                if (null == userInfo) {
                    return initStatus.apply(id);
                }
                List<MatchSimpleStatus> userStatus = userInfo.getUserStatus();
                if (CollectionUtils.isEmpty(userStatus)) {
                    return initStatus.apply(id);
                }
                Optional<MatchSimpleStatus> any = userStatus.stream().filter(i -> i.getMatchId() == id).findAny();
                if (any.isPresent()) {
                    return any.get();
                } else {
                    return initStatus.apply(id);
                }
            });
            List<MatchHeadUserBo> result = Lists.newArrayList();
            List<MatchSimpleStatus> statuses = Lists.newArrayList();
            for (Match match : matches) {
                int enrollTotal = matchUserMetaService.getEnrollTotal(match.getPaperId());
                MatchSimpleStatus matchSimpleStatus = getSimpleStatus.apply(matchHeadUserInfo, match.getPaperId());
                int status = getMatchUserStatus(match, matchSimpleStatus, (a, b) -> matchUserMetaService.findMatchUserEnrollInfo(a, b)).getKey();
                result.add(MatchHeadUserBo.builder().matchId(match.getPaperId())
                        .positionId(matchSimpleStatus.getPositionId())
                        .practiceId(matchSimpleStatus.getPracticeId())
                        .status(status)
                        .enrollCount(enrollTotal)
                        .build());
                statuses.add(matchSimpleStatus);
            }
            MATCH_HEAD_USER_INFO_CACHE.put(cache, MatchHeadUserInfo.
                    builder()
                    .userId(userId)
                    .subjectId(subject)
                    .userStatus(statuses).build());
            return result;
        }
        //直接查询redis数据添加缓存
        return matches.stream().map(match -> {
            int enrollTotal = matchUserMetaService.getEnrollTotal(match.getPaperId());
            MatchUserMeta userMeta = matchUserMetaService.findMatchUserEnrollInfo(userId, match.getPaperId());
            MatchSimpleStatus simpleStatus = initStatus.apply(match.getPaperId());
            if (null == userMeta) {
                return MatchHeadUserBo.builder().matchId(match.getPaperId())
                        .positionId(-10)
                        .practiceId(-1)
                        .status(getMatchUserStatus(match, simpleStatus, (a, b) -> userMeta).getKey())
                        .enrollCount(enrollTotal)
                        .build();
            }
            return MatchHeadUserBo.builder().matchId(match.getPaperId())
                    .positionId(userMeta.getPositionId())
                    .practiceId(userMeta.getPracticeId())
                    .status(getMatchUserStatus(match, simpleStatus, (a, b) -> userMeta).getKey())
                    .enrollCount(enrollTotal)
                    .build();
        }).collect(Collectors.toList());

    }

    /**
     * 判断用户数据是否写入guava缓存
     *
     * @param matches
     * @return
     */
    private boolean checkCacheFlag(List<Match> matches) {
        long currentTimeMillis = System.currentTimeMillis();
        Optional<Match> any = matches.stream().filter(match -> match.getStartTime() - TimeUnit.MINUTES.toMillis(10) < currentTimeMillis)
                .filter(match -> match.getEndTime() + TimeUnit.MINUTES.toMillis(10) > currentTimeMillis)
                .findAny();
        return any.isPresent();
    }


    private String getUserInfoCache(int userId, int subject) {
        return userId + "_" + subject;
    }

    /**
     * 查询用户考试状态
     *
     * @param match        模考大赛信息
     * @param simpleStatus 用户本地缓存状态查询
     * @param getUserMeta  用户最新状态查询
     * @return
     */
    private MatchStatusEnum getMatchUserStatus(Match match,
                                               MatchSimpleStatus simpleStatus,
                                               BiFunction<Integer, Integer, MatchUserMeta> getUserMeta) {
        long startTime = match.getStartTime();
        long currentTimeMillis = System.currentTimeMillis();
        boolean started = currentTimeMillis >= startTime - TimeUnit.MINUTES.toMillis(TimeGapConstant.FIVE_MINUTES);
        boolean preStart = currentTimeMillis >= startTime - TimeUnit.HOURS.toMillis(TimeGapConstant.ONE_HOUR) && !started;
        boolean stopEnroll = currentTimeMillis >= startTime + TimeUnit.MINUTES.toMillis(TimeGapConstant.THIRTY_MINUTES);
        boolean ended = currentTimeMillis >= match.getEndTime();

        if (!started) {      //考试未开始
            int positionId = simpleStatus.getPositionId();
            if (positionId >= -9) {
                return preStart ? MatchStatusEnum.START_UNAVAILABLE : MatchStatusEnum.ENROLL;
            } else {
                MatchUserMeta apply = getUserMeta.apply(simpleStatus.getUserId(),simpleStatus.getMatchId());
                if (null == apply) {
                    return MatchStatusEnum.UN_ENROLL;
                } else {
                    simpleStatus.setPositionId(apply.getPositionId());
                    return preStart ? MatchStatusEnum.START_UNAVAILABLE : MatchStatusEnum.ENROLL;
                }
            }
        }
        if (!ended) {         //考试进行中
            if (simpleStatus.getPositionId() < -9) {        //本地无报名数据
                MatchUserMeta apply = getUserMeta.apply(simpleStatus.getUserId(),simpleStatus.getMatchId());
                if (null == apply) {      //实时无报名数据
                    return stopEnroll ? MatchStatusEnum.PASS_UP_ENROLL : MatchStatusEnum.UN_ENROLL;
                } else {          //有报名数据
                    simpleStatus.setPositionId(apply.getPositionId());
                    return stopEnroll ? answerCardNoCreateSync.apply(apply, simpleStatus) : answerCardSync.apply(apply, simpleStatus);
                }
            } else if (simpleStatus.getPracticeId() == -1) {     //本地有报名信息，但是无答题卡信息
                MatchUserMeta apply = getUserMeta.apply(simpleStatus.getUserId(),simpleStatus.getMatchId());
                if (null == apply) {      //做预防
                    return MatchStatusEnum.DEFAULT;
                }
                return stopEnroll ? answerCardNoCreateSync.apply(apply, simpleStatus) : answerCardSync.apply(apply, simpleStatus);
            } else if (simpleStatus.getSubmitFlag() == 0) {      //有答题卡信息，本地未交卷
                MatchUserMeta apply = getUserMeta.apply(simpleStatus.getUserId(),simpleStatus.getMatchId());
                if (null == apply) {
                    return MatchStatusEnum.DEFAULT;
                }
                return submitFlagSync.apply(apply, simpleStatus);
            } else {
                return MatchStatusEnum.REPORT_UNAVAILABLE;
            }
        }
        if (ended) {        //考试结束后
            boolean finished = matchUserMetaService.isFinished(match.getPaperId());
            if (simpleStatus.getSubmitFlag() == 0) {        //无交卷数据的全部查询缓存
                MatchUserMeta apply = getUserMeta.apply(simpleStatus.getUserId(),simpleStatus.getMatchId());
                if (null == apply) {
                    return MatchStatusEnum.PASS_UP_ENROLL;
                }
                Long practiceId = apply.getPracticeId();
                simpleStatus.setPracticeId(practiceId);
                if (practiceId == -1L) {
                    return MatchStatusEnum.MATCH_UNAVAILABLE;
                }
                simpleStatus.setSubmitFlag(1);
            }
            return finished ? MatchStatusEnum.REPORT_AVAILABLE : MatchStatusEnum.REPORT_UNAVAILABLE;
        }
        return MatchStatusEnum.DEFAULT;
    }

    /**
     * 考试进行中，本地缓存无法判断是否交卷，通过meta获取考试状态
     */
    private static final BiFunction<MatchUserMeta, MatchSimpleStatus, MatchStatusEnum> submitFlagSync = ((meta, simpleStatusBo) -> {
        if (null == meta.getSubmitTime()) {
            return MatchStatusEnum.NOT_SUBMIT;
        }
        simpleStatusBo.setSubmitFlag(1);
        return MatchStatusEnum.REPORT_UNAVAILABLE;
    });
    /**
     * 考试开始未超过30分钟之前，通过判断答题卡ID状态，获取考试状态
     */
    private static final BiFunction<MatchUserMeta, MatchSimpleStatus, MatchStatusEnum> answerCardSync = ((meta, simpleStatusBo) -> {
        Long practiceId = meta.getPracticeId();
        if (practiceId == -1L) {
            return MatchStatusEnum.START_AVAILABLE;
        }
        simpleStatusBo.setPracticeId(practiceId);
        return submitFlagSync.apply(meta, simpleStatusBo);
    });

    /**
     * 考试开始超过30分钟，通过判断答题卡ID状态，获取考试状态
     */
    private static final BiFunction<MatchUserMeta, MatchSimpleStatus, MatchStatusEnum> answerCardNoCreateSync = ((meta, simpleStatusBo) -> {
        Long practiceId = meta.getPracticeId();
        if (practiceId == -1L) {
            return MatchStatusEnum.MATCH_UNAVAILABLE;
        }
        simpleStatusBo.setPracticeId(practiceId);
        return submitFlagSync.apply(meta, simpleStatusBo);
    });

}
