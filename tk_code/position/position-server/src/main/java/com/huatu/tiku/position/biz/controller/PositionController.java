package com.huatu.tiku.position.biz.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.position.base.domain.BaseDomain;
import com.huatu.tiku.position.base.exception.NoLoginException;
import com.huatu.tiku.position.biz.domain.*;
import com.huatu.tiku.position.biz.dto.PositionInfoDto;
import com.huatu.tiku.position.biz.enums.*;
import com.huatu.tiku.position.biz.service.*;
import com.huatu.tiku.position.biz.util.Cosine;
import com.huatu.tiku.position.biz.util.MapUtil;
import com.huatu.tiku.position.biz.util.RedisUtil;
import com.huatu.tiku.position.biz.vo.PageVo;
import com.huatu.tiku.position.biz.vo.PositionVo;
import com.huatu.tiku.position.biz.vo.ScoreLineVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 职位
 *
 * @author wangjian
 **/
@Slf4j
@RestController
@RequestMapping("position")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @Autowired
    private RecommendReccordService recommendReccordService;

    @Autowired
    private BrowseRecordService browseRecordService;

    @Autowired
    private EnrollService enrollService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    private static final String POSTITIONCOUNT="postitionCount";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private Cache<Long, Position> positionCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    /**
     * 多条件查询
     *
     * @param type       职位类型
     * @param areas      地区 多选
     * @param education  学历
     * @param degree     学位
     * @param political  政治面貌
     * @param exp        经验
     * @param baseExp    基层工作经历
     * @param sex        性别
     * @param year       年份
     * @param status     状态
     * @param page       分页参数
     * @param search 搜索关键字
     * @param searchType 关键字类型
     */
    @GetMapping("findPosition")
    public PageVo findPosition(PositionType type,
                               Long[] areas,
                               Education education,
                               Degree degree,
                               Political political,
                               Exp exp,
                               BaseExp baseExp,
                               Sex sex,
                               @RequestParam(defaultValue = "2019") Integer year,
                               PositionStatus status,
                               Pageable page,
                               String search,
                               Integer searchType,
                               @RequestParam(defaultValue = "PLACE") Nature nature) {
        List ids = null == areas ? null : new ArrayList(Arrays.asList(areas));//数组转集合
        if (ids != null && ids.contains(0L)) {
            ids = null;
        }

        PageVo<PositionVo> positionPageVo = positionService.findPositionForCache(type, ids, education, degree, political,
                exp, baseExp, sex, year, status, page, search, searchType, nature);

        positionService.renderDynamicData(positionPageVo);

        if (StringUtils.isNotBlank(search) && null != searchType) {
            List<PositionVo> content = Lists.newArrayList(positionPageVo.getContent() );
            content.forEach(vo -> {
                switch (searchType) {
                    case -1:
                        Double similarity=0.0;
                        String name = vo.getName();
                        if (StringUtils.isNotBlank(name)) {
                            similarity+= Cosine.getSimilarity(name, search);
                        }
                        String specialtyString = vo.getSpecialtyString();
                        if (StringUtils.isNotBlank(specialtyString)) {
                            similarity+= Cosine.getSimilarity(specialtyString, search);
                        }
                        vo.setSimilarityScope(similarity);
                        break;
                    case 1:
                        String specialtys = vo.getSpecialtyString();
                        if (StringUtils.isNotBlank(specialtys)) {
                            vo.setSimilarityScope(Cosine.getSimilarity(specialtys, search));
                        }
                        break;
                    case 2:
                        if (null != vo.getDepartment()) {
                            vo.setSimilarityScope(Cosine.getSimilarity(vo.getDepartment().getName(), search));
                        }
                        break;
                    case 3:
                        String nameStr = vo.getName();
                        if (StringUtils.isNotBlank(nameStr)) {
                            vo.setSimilarityScope(Cosine.getSimilarity(nameStr, search));
                        }
                        break;
                }
            });
            content.sort((o2, o1) -> (o1.getSimilarityScope().compareTo(o2.getSimilarityScope())));//值大的正相关 往前排
            return positionPageVo;
        } else {
            return positionPageVo;
        }
    }

    /**
     * 职位详情
     */
    @GetMapping("positionInfo/{id}")
    public Map findPositionById(@PathVariable Long id,
                                @RequestHeader(required = false) String openId) {
        Position position = positionCache.get(id, key -> positionService.findOne(id));

        if(null == position){
            return null;
        }
        Double start = 0.0;
        List<String> label=Lists.newArrayList();
        Boolean isEnroll=null;
        Boolean isAccord=null;
        Boolean isCollection=null;
        PositionVo positionVo = new PositionVo(position,true);
        if(StringUtils.isNotBlank(openId)){
            try {
                User user=userService.findByOpenId(openId);
                if (null!=user) {  //是否登录
                    try {
                        PositionInfoDto recommendationRank = positionService.getRecommendationRank(position, user);
                        start =recommendationRank.getStart();//计算推荐星级
                        label=recommendationRank.getLabel();
                    } catch (Exception e) {
                        start=0.0;
                    }
                    isEnroll = enrollService.findByUserIdAndPositionIdAndStatus(user.getId(), id, (byte) 1);  //是否意向报名
                    BrowseRecord browseRecord = browseRecordService.findByUserIdAndPositionId(user.getId(), id);//是否符合备注
                    if(null!=browseRecord) {
                        isAccord = browseRecord.getAccordFlag();
                        isCollection = browseRecord.getCollectionFlag();
                    }
                }
            } catch (Exception e) {
                //未登录不处理
            }
        }
        positionVo.setStart(start);
        positionVo.setLabel(label);
        Integer enrollCount = enrollService.getEnrollCount(id);
        positionVo.setEnrollCount(enrollCount);
        Notice notice = position.getNotice();
        String noticeUrl=null==notice?null:notice.getUrl();
        return MapUtil.of("Position",positionVo,
                "isEnroll", isEnroll, "isAccord", isAccord,
                "isCollection",isCollection,"noticeUrl",noticeUrl);
    }

    /**
     * 获取历年分数线
     */
    @GetMapping("scoreLine/{id}")
    public List<ScoreLineVo> scoreLine(@PathVariable Long id) {
        List<ScoreLine> scoreLines = positionService.findByPositionId(id);

        scoreLines.sort(Comparator.comparingInt(ScoreLine::getYear));//按年份排序
        List<ScoreLineVo> scoreLineVos = Lists.newArrayList();
        DecimalFormat df = new DecimalFormat("0.00");
        scoreLines.forEach(sc -> {
            ScoreLineVo scoreLine = new ScoreLineVo();
            BeanUtils.copyProperties(sc, scoreLine);
            scoreLine.setInterviewScope(BigDecimal.valueOf(sc.getInterviewScope()));
            scoreLine.setCount(sc.getEnrolment());
            if (null == scoreLine.getNumber()) {  //招录最少1人
                scoreLine.setNumber(1);
            }
            if (null == scoreLine.getCount()) { //报名最少0人
                scoreLine.setCount(0);
            }
            BigDecimal count = BigDecimal.valueOf(scoreLine.getCount());//总人数
            BigDecimal number = BigDecimal.valueOf(scoreLine.getNumber());//录取人数
            scoreLine.setProportion(df.format(count.divide(number, 2, BigDecimal.ROUND_HALF_UP)) + "/1");//两位小数点
            scoreLineVos.add(scoreLine);
        });
        List<ScoreLineVo> resultList = Lists.newArrayList();
        Integer year=0;
        for(int i=0;i<scoreLineVos.size();i++){
            ScoreLineVo scoreLineVo = scoreLineVos.get(i);
            if(year.equals(scoreLineVo.getYear())){//与上一条数据年份相同
                ScoreLineVo scoreLine = resultList.get(resultList.size() - 1);
                Integer yearCount = scoreLine.getYearCount();//相同个数

//                Double interviewScope = scoreLine.getInterviewScope()*yearCount;//之前总分
//                interviewScope+=scoreLineVo.getInterviewScope();//累计总分
//                interviewScope/=yearCount+1;//总个数
//                scoreLine.setInterviewScope(interviewScope);

                BigDecimal interviewScope = scoreLine.getInterviewScope().multiply(new BigDecimal(yearCount));//之前总分
                interviewScope=interviewScope.add(scoreLineVo.getInterviewScope());
                interviewScope=interviewScope.divide(new BigDecimal(yearCount+1),2, BigDecimal.ROUND_HALF_UP);
                scoreLine.setInterviewScope(interviewScope);

                Integer number = scoreLine.getNumber();//目前招录人数
                number+=scoreLineVo.getNumber();//总个数
                scoreLine.setNumber(number);

                Integer count = scoreLine.getCount();//目前报考人数
                count+=scoreLineVo.getCount();//总个数
                scoreLine.setCount(count);

                BigDecimal countSc = BigDecimal.valueOf(scoreLine.getCount());//总人数
                BigDecimal numberSc = BigDecimal.valueOf(scoreLine.getNumber());//录取人数
                scoreLine.setProportion(df.format(countSc.divide(numberSc, 2, BigDecimal.ROUND_HALF_UP)) + "/1");//两位小数点

                scoreLine.setYearCount(yearCount+1);
            }else{
                resultList.add(scoreLineVo);
            }
            year=scoreLineVo.getYear();
        }
        List<String> yearList=Lists.newArrayList();
        yearList.add("2015");
        yearList.add("2016");
        yearList.add("2017");
        yearList.add("2018");
        List<ScoreLineVo> newResultList = Lists.newArrayList();
        newResultList.add(null);
        newResultList.add(null);
        newResultList.add(null);
        newResultList.add(null);
        for (ScoreLineVo scoreLineVo : resultList) {
            String yearString = String.valueOf(scoreLineVo.getYear());
            int i = yearList.indexOf(yearString);
            newResultList.set(i,scoreLineVo);
        }
        for (int k=0;k< newResultList.size() ;k++) {
            ScoreLineVo scoreLineVo = newResultList.get(k);
            if(null==scoreLineVo){
                scoreLineVo = new ScoreLineVo();
                scoreLineVo.setCount(0);
                scoreLineVo.setNumber(0);
                scoreLineVo.setInterviewScope(BigDecimal.valueOf(0));
                scoreLineVo.setProportion("-");
                scoreLineVo.setYear(Integer.valueOf(yearList.get(k)));
                newResultList.set(k,scoreLineVo);
            }
        }
        return newResultList;
    }

    /**
     * 生成浏览记录
     *
     * @param positionId 职位id
     */
    @PutMapping("addRecord/{positionId}")
    public void addRecord(@RequestHeader String openId, @PathVariable Long positionId) {
        User user = userService.findByOpenId(openId);
        browseRecordService.addRecord(user.getId(), positionId);
    }

    /**
     * 添加符合备注要求
     *
     * @param positionId 职位id
     * @param accordFlag 是否符合
     */
    @PutMapping("addPositionRemark/{positionId}")
    public Boolean addPositionRemark(@RequestHeader String openId, @PathVariable Long positionId,@RequestParam Boolean accordFlag) {
        User user = userService.findByOpenId(openId);
        if(null==user){
            throw new NoLoginException("请登录");
        }else {
            return 0 != browseRecordService.addPositionRemark(user.getId(), positionId, accordFlag);
        }
    }

    /**
     * 加入意向报名
     *
     * @param positionId 职位id
     */
    @PutMapping("addEnroll/{positionId}")
    public void addEnroll(@RequestHeader String openId, @PathVariable Long positionId) {
        User user = userService.findByOpenId(openId);
        enrollService.addEnroll(user.getId(), positionId);
    }

    /**
     * 意向报名职位列表
     */
    @GetMapping("findEnrollPositions")
    public Object findEnrollPositions(Pageable page, @RequestHeader String openId) {
        User user = userService.findByOpenId(openId);
        Page<Position> enrollPositions = positionService.findEnrollPositions(page, user.getId());

        PageVo<PositionVo> positionPageVo = new PageVo(enrollPositions, enrollPositions.getContent().stream().map(PositionVo::new).collect(Collectors.toList()));

        positionService.renderDynamicData(positionPageVo);

        return positionPageVo;
    }

    /**
     * 添加符合收藏
     */
    @PutMapping("addPositionCollection/{positionId}")
    public Boolean addPositionCollection(@RequestHeader String openId, @PathVariable Long positionId, @RequestParam Boolean flag) {
        User user = userService.findByOpenId(openId);
        if(null==user){
            throw new NoLoginException("请登录");
        }else {
            return 0 != browseRecordService.addPositionCollection(user.getId(), positionId, flag);
        }
    }

    /**
     * 收藏职位列表
     */
    @GetMapping("findCollectionPositions")
    public Object findCollectionPositions(Pageable page, @RequestHeader String openId){
        User user = userService.findByOpenId(openId);
        Page<Position> enrollPositions = positionService.findCollectionPositions(page, user.getId());
        return new PageVo(enrollPositions, enrollPositions.getContent().stream().map(PositionVo::new).collect(Collectors.toList()));
    }

    /**
     * 推荐职位列表
     */
    @GetMapping("getRecommendationList")
    public PageVo getRecommendationList(Pageable page, @RequestHeader String openId,PositionType type){
        User user = userService.findByOpenId(openId);
        Set<Area> areas = user.getAreas();
        List ids = areas.stream().map(BaseDomain::getId).collect(Collectors.toList());//转集合
        if (ids.size() == 1 && ids.contains(0L)) {
            ids = Lists.newArrayList();
        }
        List<Position> positions = positionService.findPosition(type, ids, user.getEducation(), user.getDegree(), user.getPolitical(),
                user.getExp(), user.getBaseExp(), user.getSex(), null, null, Nature.PLACE);
        List<PositionVo> resultList=new ArrayList<>();
        for (Position position : positions) {
            Double start ;//计算推荐度
            List<String> label;
            try {
                PositionInfoDto recommendationRank = positionService.getRecommendationRank(position, user);
                start =recommendationRank.getStart();
                label=recommendationRank.getLabel();
            } catch (Exception e) {
                continue;
            }
            PositionVo positionVo = new PositionVo(position);
            positionVo.setStart(start);
            positionVo.setLabel(label);
            resultList.add(positionVo);
        };
        resultList.sort((o2, o1) -> (o1.getStart().compareTo(o2.getStart())));//推荐星级排序
        PageVo<PositionVo> positionPageVo = new PageVo(page,resultList);

        positionService.renderDynamicData(positionPageVo);

        return positionPageVo;
    }

    /**
     * 推荐职位信息汇总
     */
    @GetMapping("getRecommendationCommon")
    public Object getRecommendationCommon(@RequestHeader String openId){
        User user = userService.findByOpenId(openId);
        Set<Area> areas = user.getAreas();
        List ids = areas.stream().map(BaseDomain::getId).collect(Collectors.toList());//转集合
        if (ids.size() == 1 && ids.contains(0L)) {
            ids = Lists.newArrayList();
        }
        List<Position> positions = positionService.findPosition(PositionType.GWY, ids, user.getEducation(), user.getDegree(), user.getPolitical(),
                user.getExp(), user.getBaseExp(), user.getSex(), null, null, Nature.PLACE);
        Map<Double,Integer> map= Maps.newTreeMap(Double::compareTo);
        for (Position position : positions) {
            Double start;//计算推荐度
            try {
                PositionInfoDto recommendationRank = positionService.getRecommendationRank(position, user);
                start =recommendationRank.getStart();
            } catch (Exception e) {  //迭代器 接收到异常 即不符合要求 删除此数据
//                log.error("getRecommendationCommon exception", e);
                continue;
            }
            Integer count = map.get(start);
            if(null==count){
                count= 0;
            }
            map.put(start,++count);//计算各个星级个数
        }
        List<Double> startKeys=Lists.newArrayList();
        List<Integer> startValues=Lists.newArrayList();
        AtomicReference<Integer> positionCount= new AtomicReference<>(0);
        AtomicReference<Integer> needConfirm= new AtomicReference<>(0);
        Double oneStart=1.0;
        Double twoStart=2.0;
        map.forEach((key, value) -> {
            startKeys.add(key);
            startValues.add(value);
            positionCount.updateAndGet(v -> v + value);
            if(oneStart.equals(key)||twoStart.equals(key)){
                needConfirm.updateAndGet(v -> v + value);
            }
        });
        List list = userService.computeAreaUserInfo(areas,user);
        String positionTotal = redisUtil.get(POSTITIONCOUNT);
        if(StringUtils.isBlank(positionTotal)){
            Integer count=positionService.findCount();
            positionTotal=String.valueOf(count);
            redisUtil.set(POSTITIONCOUNT,positionTotal, 5 * 60L);
        }
        recommendReccordService.saveX(user.getId());//生成推荐报告记录
        return  MapUtil.of("startKeys",startKeys,"startValues",startValues,
                "count",list,"positionCount",positionCount,"needConfirm",needConfirm,"positionTotal",positionTotal);
    }

    /**
     * 检查更新职位数
     */
    @GetMapping("checkUpdate")
    public Map checkUpdate(@RequestHeader String openId){
        User user = userService.findByOpenId(openId);
        Integer updateCount = recommendReccordService.checkUpdate(user.getId());
        return  ImmutableMap.of("updateCount",updateCount);
    }

    /**
     * 移除意向报名
     *
     * @param openId 用户ID
     * @param positionId 职位ID
     */
    @DeleteMapping("enroll/{positionId}")
    public void removeEnroll(@RequestHeader String openId, @PathVariable Long positionId) {
        User user = userService.findByOpenId(openId);
        enrollService.removeEnroll(user.getId(), positionId);
    }
}
