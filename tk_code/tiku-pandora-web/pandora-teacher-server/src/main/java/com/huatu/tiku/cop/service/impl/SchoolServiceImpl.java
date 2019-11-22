package com.huatu.tiku.cop.service.impl;

import com.google.common.collect.Lists;
import com.huatu.tiku.constants.cache.RedisKeyConstant;
import com.huatu.tiku.cop.service.SchoolService;
import com.huatu.tiku.dto.*;
import com.huatu.tiku.entity.cop.School;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.response.area.AreaWithSchoolRespVO;
import com.huatu.tiku.teacher.dao.SchoolMapper;
import com.huatu.tiku.teacher.enums.ActivityTagEnum;
import com.huatu.tiku.teacher.enums.StatusEnum;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.activity.PaperAnswerCardService;
import com.huatu.tiku.util.http.ResponseMsg;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.question.common.QuestionCorrectType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.huatu.tiku.service.impl.BaseServiceImpl.throwBizException;

/**
 * @author zhaoxi
 * @Description: 公安招警-院校管理
 * @date 2018/8/17下午3:21
 */
@Service
public class SchoolServiceImpl implements SchoolService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SchoolMapper schoolMapper;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${userUrl}")
    private String userUrl;

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private PaperActivityService paperActivityService;

    public static final Logger logger = LoggerFactory.getLogger(SchoolServiceImpl.class);


    /**
     * 查询所有地区和学院列表
     *
     * @return
     */
    @Override
    public Object findAreaList() {
        WeekendSqls<School> sql = WeekendSqls.custom();
        sql.andEqualTo(School::getStatus, StatusEnum.NORMAL.getValue());
        Example example = Example.builder(School.class)
                .where(sql)
                .orderBy("id ")
                .build();
        List<School> schoolList = schoolMapper.selectByExample(example);

        Map<Long, AreaWithSchoolRespVO> map = new HashMap<>();

        for (School school : schoolList) {
            AreaWithSchoolRespVO areaVO = map.get(school.getAreaId());

            List<AreaWithSchoolRespVO.SchoolRespVO> schoolRespVOS = new LinkedList<>();
            AreaWithSchoolRespVO.SchoolRespVO schoolRespVO = new AreaWithSchoolRespVO.SchoolRespVO();
            schoolRespVO.setSchoolId(school.getId());
            schoolRespVO.setSchoolName(school.getName());
            schoolRespVOS.add(schoolRespVO);

            if (areaVO == null) {
                areaVO = new AreaWithSchoolRespVO();
                areaVO.setAreaId(school.getAreaId());
                areaVO.setAreaName(school.getAreaName());
            } else {
                schoolRespVOS.addAll(areaVO.getSchoolList());
            }
            areaVO.setSchoolList(schoolRespVOS);
            map.put(school.getAreaId(), areaVO);
        }

        return map.values();
    }

    /**
     * 查询院校信息
     *
     * @return
     */
    @Override
    public Object findSchoolList() {

        String schoolListKey = RedisKeyConstant.getSchoolListKey();
        List<School> schoolList = (List<School>) redisTemplate.opsForValue().get(schoolListKey);
        if (CollectionUtils.isNotEmpty(schoolList)) {
            return schoolList;
        } else {
            WeekendSqls<School> sql = WeekendSqls.custom();
            sql.andEqualTo(School::getStatus, StatusEnum.NORMAL.getValue());

            Example example = Example.builder(School.class)
                    .where(sql)
                    .orderBy("id ")
                    .build();
            schoolList = schoolMapper.selectByExample(example);
            redisTemplate.opsForValue().set(schoolListKey, schoolList, 7, TimeUnit.DAYS);


            return schoolList;
        }

    }

    /**
     * 导出机考的模考数据
     *
     * @return
     */
    @Override
    public ModelAndView importData(int paperId) {

        List<MatchResultVO> list = new LinkedList<>();

        Criteria criteria = Criteria.where("paperId").is(paperId).and("practiceId").ne(-1);
        Query query = new Query(criteria);
        query.with(new Sort(new Sort.Order(Sort.Direction.ASC, "userId")));
        List<MatchUserMeta> matchUserMetas = mongoTemplate.find(new Query(criteria), MatchUserMeta.class);
        //获取用户信息
        List<Map<String, Object>> userInfoList = getUserInfoByMatchUserInfo(matchUserMetas);

        if (CollectionUtils.isNotEmpty(matchUserMetas)) {
            for (MatchUserMeta matchUserMeta : matchUserMetas) {
                AnswerCard answerCard = mongoTemplate.findById(matchUserMeta.getPracticeId(), AnswerCard.class);

                Map userInfoMap = new HashMap();
                List<Map<String, Object>> collect = userInfoList.stream().filter(map -> Long.valueOf(map.get("id").toString()) == matchUserMeta.getUserId())
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    userInfoMap = collect.get(0);
                    Object nickObject = userInfoMap.get("nick");
                    Object mobileObject = userInfoMap.get("mobile");
                    String nick = (nickObject == null) ? "" : nickObject.toString();
                    String mobile = (mobileObject == null) ? "" : mobileObject.toString();

                    if (answerCard != null) {
                        List<Double> moduleScore = getModuleScore(answerCard);
                        MatchResultVO matchResultVO = MatchResultVO.builder()
                                .userId(answerCard.getUserId())
                                .score(answerCard.getScore())
                                .paperName(answerCard.getName())
                                .endTime(DateFormatUtils.format(new Date(answerCard.getCreateTime()), "yyyy-MM-dd HH:mm:ss"))
                                .expendTime(answerCard.getExpendTime() / 60)
                                .moduleScore(moduleScore)
                                .nick(nick)
                                .mobile(mobile)
                                .build();

                        list.add(matchResultVO);
                    }
                }
            }
        }
        list.sort((a, b) -> (int) (a.getUserId() - b.getUserId()));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("members", list);
        map.put("name", "模考成绩统计-" + paperId);
        ExcelView excelView = new MatchResultView();
        return new ModelAndView(excelView, map);
    }


    private List<Double> getModuleScore(AnswerCard answerCard) {

        List<Double> moduleScore = new LinkedList<>();
        int totalScore = 100;
        double examScore = 0D;
        List<Module> modules = new LinkedList<>();
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();

            //试卷分数
            int paperScore = paper.getScore();
            if (paperScore >= 100) {
                totalScore = paperScore;
            }
            modules = paper.getModules();
        }
        int[] corrects = answerCard.getCorrects();
        List<Integer> correctList = new ArrayList<>(corrects.length);
        for (int c : corrects) {
            correctList.add(new Integer(c));
        }

        //按模块计算分数
        if (CollectionUtils.isNotEmpty(modules)) {
            int startIndex = 0;
            int endIndex = 0;
            //按模块取出答对的题目个数和打错的题目个数
            int moduleIndex = 0;
            for (Module module : modules) {
                double mScore = 0D;
                double rcount = 0;
                double wcount = 0;
                endIndex += module.getQcount();
                List<Integer> subList = correctList.subList(startIndex, endIndex);
                //遍历输出错题个数，和答对的题目个数

                for (Integer correct : subList) {
                    if (correct.equals(QuestionCorrectType.RIGHT)) {
                        rcount++;
                    } else if (correct.equals(QuestionCorrectType.WRONG)) {
                        wcount++;
                    }
                }
                //计算模块得分
                switch (moduleIndex) {
                    case 0:
                        mScore = 0.2 * rcount - 0.2 * wcount;
                        break;
                    case 1:
                        mScore = 0.5 * rcount;
                        break;
                    case 2:
                        mScore = 0.6 * rcount;

                        break;
                    case 3:
                        mScore = 1.2 * rcount;
                        break;
                    case 4:
                        mScore = 0.7 * rcount;
                        break;
                    case 5:
                        mScore = 1.0 * rcount;
                        break;
                }

                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                if (mScore != 0) {
                    mScore = Double.valueOf(decimalFormat.format(mScore));
                }
                moduleScore.add(mScore);
                moduleIndex++;
                startIndex += module.getQcount();
            }

        }

        return moduleScore;
    }

    public List<Map<String, Object>> getUserInfo(List<Long> matchUserIds) {

        String userIds = matchUserIds.stream().
                map(String::valueOf).collect(Collectors.joining(","));
        RestTemplate restTemplate = new RestTemplate();
        String userBatchUrl = userUrl + "/v1/users/batch";
        System.out.println("访问地址是：{}" + userBatchUrl);
        HashMap<String, String> params = new HashMap<>();
        params.put("userIds", userIds);
        //批量查询
        ResponseMsg<List<Map<String, Object>>> responseMsg = restTemplate.postForObject(userBatchUrl, params, ResponseMsg.class);
        if (responseMsg.getCode() != 1000000) {
            throwBizException(responseMsg.getMsg());
        }
        final List<Map<String, Object>> data = responseMsg.getData();
        return data;
    }


    public List<Map<String, Object>> getUserInfoByMatchUserInfo(List<MatchUserMeta> matchUserMetas) {

        if (CollectionUtils.isEmpty(matchUserMetas)) {
            return Lists.newArrayList();
        }
        List<Long> userIds = matchUserMetas.stream().map(MatchUserMeta::getUserId).collect(Collectors.toList());
        List<Map<String, Object>> userResult = new ArrayList<>();

        int pageSize = 500;
        int totalCount = userIds.size();
        int totalPage = (int) Math.ceil((double) totalCount / (double) pageSize);
        // 根据页码取数据
        for (int page = 0; page < totalPage; page++) {
            // 索引开始位置
            int startIndex = page * pageSize;
            int endIndex = Math.min((page + 1) * pageSize, totalCount);

            List<Long> result = userIds.subList(startIndex, endIndex);
            userResult.addAll(getUserInfo(result));
            logger.info("分页开始索引:{}，结束索引：{},本页数据是：{}", startIndex, endIndex, result);
        }
        return userResult;
    }


    @Override
    public ModelAndView importMockData(int subject) {
        //根据科目查询可用模考
        Criteria criteria = Criteria.where("subject").is(subject)
                .and("startTime").gte(1514736000L)
                .and("status").is(2);
        Query query = new Query(criteria);
        query.with(new Sort(new Sort.Order(Sort.Direction.ASC, "startTime")));
        List<Match> matches = mongoTemplate.find(new Query(criteria), Match.class);

        List<MatchDataVO> list = new ArrayList<>();
        for (Match match : matches) {

            Criteria enrollCriteria = Criteria.where("paperId").is(match.getPaperId());
            long enrollCount = mongoTemplate.count(new Query(enrollCriteria), MatchUserMeta.class);

            enrollCriteria.and("practiceId").ne(-1);
            long examCount = mongoTemplate.count(new Query(enrollCriteria), MatchUserMeta.class);

            MatchDataVO matchDataVO = MatchDataVO.builder()
                    .id(match.getPaperId())
                    .name(match.getName())
                    .timeInfo(match.getTimeInfo())
                    .tag(match.getTag() + "")
                    .enrollCount(enrollCount)
                    .examCount(examCount)
                    .build();


            try {
                ActivityTagEnum.TagEnum tagEnum = ActivityTagEnum.Subject.getTag(subject, match.getTag());
                if (null != tagEnum) {
                    matchDataVO.setTag(tagEnum.getTagName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            list.add(matchDataVO);

        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("members", list);
        map.put("name", "模考数据统计-" + subject);
        ExcelView excelView = new MatchView();
        return new ModelAndView(excelView, map);
    }

    @Override
    public ModelAndView importLineData(int paperId) {

        List<MatchResultVO> list = new LinkedList<>();

        Criteria criteria = Criteria.where("paperId").is(paperId).and("practiceId").ne(-1);
        Query query = new Query(criteria);
        query.with(new Sort(new Sort.Order(Sort.Direction.ASC, "userId")));
        List<MatchUserMeta> matchUserMetas = mongoTemplate.find(new Query(criteria), MatchUserMeta.class);
        //获取用户信息
        List<Map<String, Object>> userInfoList = getUserInfoByMatchUserInfo(matchUserMetas);

        if (CollectionUtils.isNotEmpty(matchUserMetas)) {
            for (MatchUserMeta matchUserMeta : matchUserMetas) {
                AnswerCard answerCard = mongoTemplate.findById(matchUserMeta.getPracticeId(), AnswerCard.class);

                Map userInfoMap = new HashMap();
                List<Map<String, Object>> collect = userInfoList.stream().filter(map -> Long.valueOf(map.get("id").toString()) == matchUserMeta.getUserId())
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    userInfoMap = collect.get(0);
                    Object nickObject = userInfoMap.get("nick");
                    Object mobileObject = userInfoMap.get("mobile");
                    String nick = (nickObject == null) ? "" : nickObject.toString();
                    String mobile = (mobileObject == null) ? "" : mobileObject.toString();

                    if (answerCard != null) {
                        List<Double> moduleScore = getModuleScoreOfLine(answerCard);
                        MatchResultVO matchResultVO = MatchResultVO.builder()
                                .userId(answerCard.getUserId())
                                .score(answerCard.getScore())
                                .paperName(answerCard.getName())
                                .endTime(DateFormatUtils.format(new Date(answerCard.getCreateTime()), "yyyy-MM-dd HH:mm:ss"))
                                .expendTime(answerCard.getExpendTime() / 60)
                                .moduleScore(moduleScore)
                                .nick(nick)
                                .mobile(mobile)
                                .build();

                        list.add(matchResultVO);
                    }
                }
            }
        }
        list.sort((a, b) -> (int) (a.getUserId() - b.getUserId()));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("members", list);
        map.put("name", "模考成绩统计-" + paperId);
        ExcelView excelView = new MatchResultView();
        return new ModelAndView(excelView, map);
    }

    private List<Double> getModuleScoreOfLine(AnswerCard answerCard) {

        List<Double> moduleScore = new LinkedList<>();
        int totalScore = 100;
        double examScore = 0D;
        List<Module> modules = new LinkedList<>();
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();

            //试卷分数
            int paperScore = paper.getScore();
            if (paperScore >= 100) {
                totalScore = paperScore;
            }
            modules = paper.getModules();
        }
        int[] corrects = answerCard.getCorrects();
        List<Integer> correctList = new ArrayList<>(corrects.length);
        for (int c : corrects) {
            correctList.add(new Integer(c));
        }

        //按模块计算分数
        if (CollectionUtils.isNotEmpty(modules)) {
            int startIndex = 0;
            int endIndex = 0;
            //按模块取出答对的题目个数和打错的题目个数
            int moduleIndex = 0;
            for (Module module : modules) {
                double mScore = 0D;
                double rcount = 0;
                double wcount = 0;
                endIndex += module.getQcount();
                List<Integer> subList = correctList.subList(startIndex, endIndex);
                //遍历输出错题个数，和答对的题目个数

                for (Integer correct : subList) {
                    if (correct.equals(QuestionCorrectType.RIGHT)) {
                        rcount++;
                    }
                }
                //计算模块得分
                switch (moduleIndex) {
                    case 0:
                        mScore = 0.71 * rcount;
                        break;
                    case 1:
                        mScore = 0.71 * rcount;
                        break;
                    case 2:
                        mScore = 0.71 * rcount;

                        break;
                    case 3:
                        mScore = 0.71 * rcount;
                        break;
                    case 4:
                        mScore = 0.71 * rcount;
                        break;
                }

                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                if (mScore != 0) {
                    mScore = Double.valueOf(decimalFormat.format(mScore));
                }
                moduleScore.add(mScore);
                moduleIndex++;
                startIndex += module.getQcount();
            }

        }

        return moduleScore;
    }

}

