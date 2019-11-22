package com.huatu.tiku.match.service.v1.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.match.bo.CourseInfoBo;
import com.huatu.tiku.match.bo.MatchBo;
import com.huatu.tiku.match.bo.MatchUserMetaBo;
import com.huatu.tiku.match.common.MatchConfig;
import com.huatu.tiku.match.common.TimeGapConstant;
import com.huatu.tiku.match.enums.EssayMatchStatusEnum;
import com.huatu.tiku.match.enums.MatchInfoEnum;
import com.huatu.tiku.match.enums.MatchStatusEnum;
import com.huatu.tiku.match.manager.MatchManager;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.util.UserInfoHolder;
import com.huatu.ztk.paper.bean.Match;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 描述：首页模考大赛列表
 *
 * @author biguodong
 * Create time 2019-01-14 下午3:21
 **/
@Slf4j
public abstract class SearchTemplate implements SearchService {

    @Autowired
    private MatchUserMetaService matchUserMetaService;
    @Autowired
    public MatchConfig matchConfig;

    @Autowired
    private CourseService courseService;

    @Autowired
    private GiftPackageService giftPackageService;
//    @Autowired
//    private MatchEssayUserMetaService matchEssayUserMetaService;

    private static final List<Integer> sortIds = Lists.newArrayList(4002149,4002150,4002151,4002152);

    Comparator<MatchBo> matchSort = (a, b)->{
        if(sortIds.contains(a.getMatchId()) && sortIds.contains(b.getMatchId())){
            return sortIds.indexOf(a.getMatchId()) - sortIds.indexOf(b.getMatchId());
        }
        return a.getStartTime() <= b.getStartTime() ? -1 : 1;
    };
    @Autowired
    private MatchManager matchManager;


    /**
     * 获取当前subject下可用的模考大赛分页信息；
     *
     * @param result
     * @param matchList
     * @param subjectId
     * @param page
     * @param size
     * @param filterHead
     * @return
     * @throws BizException
     */
    private void dealMatchPageInfo(final HashMap<String, Object> result, List<Match> matchList, int subjectId, int page, int size, Predicate<Match> filterHead) throws BizException {
        result.put("page", page);
        result.put("size", size);
        result.put("next", 0);
        result.put("total", 0);
        List<Match> matches = matchManager.findMatchesBySubjectWithCache(subjectId);
        if (CollectionUtils.isEmpty(matches)) {
            return;
        }
        List<Match> list = Lists.newArrayList();
        //TODO by biguodong 时间配置到apollo
        List<Match> preList = matches.stream().filter(filterHead).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(preList)) {
            list.addAll(preList);
        }
        int startIndex = (page - 1) * size;
        if (startIndex < 0 || startIndex >= list.size()) {
            return;
        }
        result.put("total", list.size());
        int end = page * size;
        if (page * size < list.size()) {
            result.put("next", 1);
        } else {
            result.put("next", 0);
            end = list.size();
        }
        //指定页面展示的模考大赛
        matchList.addAll(list.subList(startIndex, end));
    }

    /**
     * 处理返回MatchBo
     *
     * @param matchList
     * @throws BizException
     */
    private void dealUserMetaBos(final List<MatchBo> matchBos, final List<Match> matchList, int subject, final long currentTime) throws BizException {
        for (Match match : matchList) {
            MatchBo matchBo = MatchBo.builder().build();
            BeanUtils.copyProperties(match, matchBo);
            matchBo.setMatchId(match.getPaperId());
            //活动礼包默认关闭
            matchBo.setIconUrl(null);
            CourseInfoBo courseInfoBo = (CourseInfoBo) courseService.courseInfo(match.getCourseId());
            courseInfoBo.setCourseTitle(match.getCourseInfo());

            MatchUserMetaBo matchUserMetaBo = dealUserMatchUserMetaBo(matchBo.getMatchId());
            dealPackageMatchInfo(matchBo, matchUserMetaBo, currentTime);
            matchBo.setStage(MatchInfoEnum.StageEnum.TEST.getKey());
            matchBo.setFlag(MatchInfoEnum.FlagEnum.ONLY_TEST.getKey());
            dealMatchDefaultStage(match);
            dealMatchEnrollCount(matchBo);
            matchBo.setUserMeta(matchUserMetaBo);
            matchBo.setCourseInfo(courseInfoBo);
            matchBos.add(matchBo);
        }
        if (matchConfig.checkCollectionContainsCurrentSubject(subject)) {
            matchBos.forEach(i -> i.setEnrollFlag(MatchInfoEnum.EnrollFlagEnum.NO_AREA.getKey()));
        }

    }

    /**
     * 查看是否携带申论的混合模考大赛
     * = 0 只有行测的模考大赛；
     * > 0 携带申论的模考大赛；
     *
     * @param essayPaperId
     * @return
     */
    private boolean isCarryEssayInfo(long essayPaperId) {
        return essayPaperId <= 0;
    }


    /**
     * 判断是否有答题卡信息
     *
     * @param practiceId
     * @return
     */
    private boolean checkPracticeExist(long practiceId) {
        return practiceId > 0;
    }


    /**
     * 检查当前模考大赛是否已经结束
     *
     * @param paperId
     * @param currentTime
     * @param endTime
     * @return
     */
    private boolean checkCurrentMatchIsFinished(int paperId, long currentTime, long endTime) {
        boolean isTimeEnd = currentTime > endTime;
        boolean isFinished = matchUserMetaService.isFinished(paperId);
        return isTimeEnd && isFinished;
    }


    /**
     * 判断申论模考大赛是否已经交卷且已经批改
     *
     * @param status
     * @return
     */
    private boolean checkMatchEssayIsSubmitted(EssayMatchStatusEnum status) {
        return EssayMatchStatusEnum.SUBMITTED.valueEquals(status.getKey()) ||
                EssayMatchStatusEnum.CORRECTED.valueEquals(status.getKey());
    }

    /**
     * 检查用户是否有行测分数
     *
     * @param paperId
     * @param userId
     * @return
     */
    private boolean checkUserHasTestScore(int paperId, int userId) {
        return matchUserMetaService.isExistedScore(paperId, userId);
    }

    /**
     * 检查当前申论默模考大赛是否应结束
     *
     * @param essayPaperId
     * @param currentTime
     * @param endTime
     * @param status
     * @return
     */
//    private boolean checkCurrentEssayIsFinished(long essayPaperId, long currentTime, long endTime, EssayMatchStatusEnum status) {
//        boolean isFinished = matchEssayUserMetaService.isFinished(essayPaperId);
//        boolean isTimeEnd = currentTime > endTime + TimeUnit.MINUTES.toMillis(matchConfig.getEssayDelayReportTime());
//        boolean isCorrected = EssayMatchStatusEnum.CORRECTED.valueEquals(status.getKey());
//        return isTimeEnd && isFinished && isCorrected;
//    }

    /**
     * stage 为行测并且可查看报告
     *
     * @param matchBo
     * @return
     */
    private boolean checkStageIsTestAndReportAvailable(final MatchBo matchBo) {
        return MatchInfoEnum.StageEnum.TEST.valueEquals(matchBo.getStage()) &&
                MatchStatusEnum.REPORT_AVAILABLE.valueEquals(matchBo.getStatus());
    }

    /**
     * stage 为申论并且用户有行测模考分数
     *
     * @param status
     * @param isHashTestLineScore
     * @return
     */
    private boolean checkStageIsEssayAndReportAvailableAndHasLineTestScore(int stage, int status, boolean isHashTestLineScore) {
        return MatchInfoEnum.StageEnum.ESSAY.valueEquals(stage) &&
                MatchStatusEnum.REPORT_AVAILABLE.valueEquals(status) &&
                isHashTestLineScore;
    }

    /**
     * stage 为申论并且用户没有行测模考分数
     *
     * @param stage
     * @param status
     * @param isHashTestLineScore
     * @return
     */
    private boolean checkStageIsEssayAndReportAvailableAndHasNoLineTestScore(int stage, int status, boolean isHashTestLineScore) {
        return MatchInfoEnum.StageEnum.ESSAY.valueEquals(stage) &&
                MatchStatusEnum.REPORT_AVAILABLE.valueEquals(status) &&
                (!isHashTestLineScore);

    }

    /**
     * stage stage为申论并且报告不可获取并且用户有模考成绩
     *
     * @param stage
     * @param status
     * @param isHashTestLineScore
     * @return
     */
    private boolean checkStageIsEssayAndReportUnavailableAndHasLineTestScore(int stage, int status, boolean isHashTestLineScore) {
        return MatchInfoEnum.StageEnum.ESSAY.valueEquals(stage) &&
                (!MatchStatusEnum.REPORT_AVAILABLE.valueEquals(status)) &&
                isHashTestLineScore;
    }


    /**
     * 处理考试中的模考状态
     *
     * @param matchBo
     * @param currentTime
     * @param isSubmit
     * @param currentFinish
     */
    private void dealMatchStatusInExamStatus(MatchBo matchBo, final long currentTime, final boolean isSubmit, final boolean currentFinish) {
        if (isSubmit) {
            matchBo.setStatus(currentFinish ? MatchStatusEnum.REPORT_AVAILABLE.getKey() : MatchStatusEnum.REPORT_UNAVAILABLE.getKey());
        } else if (currentTime < matchBo.getEndTime()) {
            matchBo.setStatus(MatchStatusEnum.NOT_SUBMIT.getKey());
        } else {
            matchBo.setStatus(MatchStatusEnum.REPORT_UNAVAILABLE.getKey());
        }
    }

    /**
     * 用户在行测模式模考大赛的状态判断 || 用户在行测 & 申论联合模式模考大赛，行测考试阶段的用户状态判断
     *
     * @param matchBo
     * @param userMeta
     */
    private void dealPackageMatchInfo(final MatchBo matchBo, final MatchUserMetaBo userMeta, final long currentTime) {
        /**
         * 已报名
         */
        if (userMeta != null) {
//            int positionCount = matchUserMetaService.getPositionTotal(matchBo.getMatchId(), userMeta.getPositionId());
//            userMeta.setPositionCount(positionCount);
            userMeta.setPositionCount(0);
            //答题卡id
            long practiceId = userMeta.getPracticeId();
            if (checkPracticeExist(practiceId)) {
                boolean isSubmit = userMeta.isSubmitFlag();
                boolean currentFinished = checkCurrentMatchIsFinished(matchBo.getMatchId(), currentTime, matchBo.getEndTime());
                dealMatchStatusInExamStatus(matchBo, currentTime, isSubmit, currentFinished);
            } else {
                if (matchBo.getStartTime() - currentTime >= TimeUnit.HOURS.toMillis(TimeGapConstant.ONE_HOUR)) {
                    /**
                     * 距开始大于一个小时
                     */
                    matchBo.setStatus(MatchStatusEnum.ENROLL.getKey());
                } else {
                    dealMatchStatusStillAnHour2Start(matchBo, matchBo.getStartTime(), matchBo.getEndTime(), currentTime);
                }
            }
        } else {
            matchBo.setStatus(MatchStatusEnum.UN_ENROLL.getKey());
            /**
             * @update 2018/08/20 huangqp
             * 前提:未报名
             * 原逻辑：已经开始30分钟或者考试已结束，如果是联合考试，置状态为9，否则为5
             * 现逻辑：已经开始30分钟或者考试已结束，状态都置为9
             */
            //已经开始30分钟或者考试已结束
            if (currentTime - matchBo.getStartTime() >= TimeUnit.MINUTES.toMillis(TimeGapConstant.THIRTY_MINUTES)
                    || currentTime > matchBo.getEndTime()) {
                //状态置为“未报名且错过报名”
                matchBo.setStatus(MatchStatusEnum.PASS_UP_ENROLL.getKey());
            }
        }
    }

    /**
     * 处理携带申论的模考大赛信息
     *
     * @param matchBo
     * @param matchUserMetaBo
     */
    private void dealPackageMatchInfoWithEssay(final MatchBo matchBo, final MatchUserMetaBo matchUserMetaBo, final long currentTime) {
        /**
         * 行测和申论的分割时间点
         */
        long splitTime = matchBo.getEssayStartTime() - TimeUnit.MINUTES.toMillis(matchConfig.getEssayLeadTime());
        if (currentTime < splitTime) {
            /**
             * 行测阶段的判断
             */
            dealPackageMatchInfo(matchBo, matchUserMetaBo, currentTime);
            matchBo.setStage(MatchInfoEnum.StageEnum.TEST.getKey());
        }
//        else {
//            /**
//             * 申论阶段的判断
//             */
//            dealPackageEssayMatchInfo(matchBo, matchUserMetaBo, currentTime);
//            matchBo.setStage(MatchInfoEnum.StageEnum.ESSAY.getKey());
//        }
    }


    /**
     * 处理申论模考大赛信息
     *
     * @param matchBo
     * @param userMeta
     */
//    private void dealPackageEssayMatchInfo(final MatchBo matchBo, final MatchUserMetaBo userMeta, final long currentTime) {
//        //已报名
//        if (userMeta != null) {
//            int positionCount = matchUserMetaService.getPositionTotal(matchBo.getMatchId(), userMeta.getPositionId());
//            userMeta.setPositionCount(positionCount);
//
//            //答题卡id
//            //todo 完善status 状态
//            EssayMatchStatusEnum status = matchEssayUserMetaService.getEssayUserAnswerStatus(matchBo.getEssayPaperId(), userMeta.getUserId());
//            if (EssayMatchStatusEnum.DEFAULT.valueNotEquals(status.getKey())) {
//                boolean isSubmit = checkMatchEssayIsSubmitted(status);
//                boolean currentFinished = checkCurrentEssayIsFinished(matchBo.getEssayPaperId(), currentTime, matchBo.getEndTime(), status);
//                dealMatchStatusInExamStatus(matchBo, currentTime, isSubmit, currentFinished);
//            } else {
//                dealMatchStatusStillAnHour2Start(matchBo, matchBo.getEssayStartTime(), matchBo.getEssayEndTime(), currentTime);
//            }
//        } else {
//            matchBo.setStatus(MatchStatusEnum.PASS_UP_ENROLL.getKey());
//        }
//    }

    /**
     * 处理还有一小时就开始考试的模考大赛状态
     *
     * @param matchBo
     * @param startTime
     * @param endTime
     * @param currentTime
     */
    private void dealMatchStatusStillAnHour2Start(MatchBo matchBo, long startTime, long endTime, final long currentTime) {
        if (startTime - currentTime >= TimeUnit.MINUTES.toMillis(TimeGapConstant.FIVE_MINUTES)) {
            /**
             * 距开始小于一个小时,大于5分钟
             */
            matchBo.setStatus(MatchStatusEnum.START_UNAVAILABLE.getKey());
        } else if (currentTime - startTime < TimeUnit.MINUTES.toMillis(TimeGapConstant.THIRTY_MINUTES) &&
                currentTime < endTime) {
            /**
             * 距开始小于5分钟,且距离开始后小于30分钟
             */
            matchBo.setStatus(MatchStatusEnum.START_AVAILABLE.getKey());
        } else {
            /**
             * 已经开始30分钟
             */
            matchBo.setStatus(MatchStatusEnum.MATCH_UNAVAILABLE.getKey());
        }
    }

    /**
     * 判断用户考试成绩情况(0表示没有成绩报告1表示只有行测报告2只有申论报告3行测申论报告都有)
     *
     * @param matchBo
     */
    private MatchInfoEnum.FlagEnum dealMatchFlag(final MatchBo matchBo, final MatchUserMetaBo matchUserMetaBo) {
        /**
         * DEFAULT
         */
        MatchInfoEnum.FlagEnum flag = MatchInfoEnum.FlagEnum.DEFAULT;
        if (matchUserMetaBo == null) {
            return flag;
        }
        if (checkStageIsTestAndReportAvailable(matchBo)) {
            flag = MatchInfoEnum.FlagEnum.ONLY_TEST;
        }
        boolean isHashTestLineScore = checkUserHasTestScore(matchBo.getMatchId(), matchUserMetaBo.getUserId());
        if (checkStageIsEssayAndReportAvailableAndHasLineTestScore(matchBo.getStage(), matchBo.getStatus(), isHashTestLineScore)) {
            flag = MatchInfoEnum.FlagEnum.TEST_AND_ESSAY;
        }
        if (checkStageIsEssayAndReportAvailableAndHasNoLineTestScore(matchBo.getStage(), matchBo.getStatus(), isHashTestLineScore)) {
            flag = MatchInfoEnum.FlagEnum.ONLY_ESSAY;
        }
        if (checkStageIsEssayAndReportUnavailableAndHasLineTestScore(matchBo.getStage(), matchBo.getStatus(), isHashTestLineScore)) {
            flag = MatchInfoEnum.FlagEnum.ONLY_TEST;
        }
        return flag;
    }

    /**
     * 处理模考大赛stage信息
     *
     * @param match
     */
    private void dealMatchDefaultStage(Match match) {
        match.setStage(MatchInfoEnum.StageEnum.TEST.getKey());
//        /**
//         * 当状态为未报名和停止报名时，stage 统一置为default
//         */
//        if (MatchStatusEnum.UN_ENROLL.valueEquals(match.getStatus()) ||
//                MatchStatusEnum.PASS_UP_ENROLL.valueEquals(match.getStatus())) {
//            match.setStage(MatchInfoEnum.StageEnum.DEFAULT.getKey());
//        }
//        //如果是申论阶段，则将tag换为3
//        if (MatchInfoEnum.StageEnum.ESSAY.valueEquals(match.getStage())) {
//            match.setTag(3);
//        }
    }


    /**
     * 处理模考大赛报名总数
     *
     * @param matchBo
     */
    private void dealMatchEnrollCount(MatchBo matchBo) {
        int enrollCount = matchUserMetaService.getEnrollTotal(matchBo.getMatchId());
        matchBo.setEnrollCount(enrollCount);
    }


    /**
     * 白名单处理
     *
     * @param matchBos
     */
    protected abstract void filterWhiteList(List<MatchBo> matchBos);


    /**
     * 处理用户报名信息
     *
     * @param matchId
     * @return
     * @throws BizException
     */
    protected abstract MatchUserMetaBo dealUserMatchUserMetaBo(int matchId);

    /**
     * 模考大赛列表入口
     *
     * @param subject
     * @param page
     * @param size
     * @param filterHead
     * @return
     * @throws BizException
     */
    @Override
    public final HashMap<String, Object> matchEntrance(int subject, int page, int size, Predicate<Match> filterHead) throws BizException {
        long currentTime = System.currentTimeMillis();
        HashMap<String, Object> result = Maps.newHashMap();
        List<Match> matchList = Lists.newArrayList();
        List<MatchBo> matchBos = Lists.newArrayList();
        dealMatchPageInfo(result, matchList, subject, page, size, filterHead);
        dealUserMetaBos(matchBos, matchList, subject, currentTime);
        matchBos.sort(matchSort);
        result.put("list", matchBos);
        UserInfoHolder.clear();
        return result;
    }

    @Override
    public final Object getUserMatchInfo(int subjectId, int paperId) throws BizException {
        Match match = matchManager.findById(paperId);
        if(null == match){
            throw new BizException(ErrorResult.create(1021312,"模考大赛不存在"));
        }
        List<Match> matchList = Lists.newArrayList(match);
        List<MatchBo> matchBos = Lists.newArrayList();
        dealUserMetaBos(matchBos, matchList, subjectId, System.currentTimeMillis());
        return matchBos.get(0);
    }

}
