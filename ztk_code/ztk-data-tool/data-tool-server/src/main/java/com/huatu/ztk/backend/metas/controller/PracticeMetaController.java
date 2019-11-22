package com.huatu.ztk.backend.metas.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.ztk.backend.constant.RedisKeyConstant;
import com.huatu.ztk.backend.metas.service.PracticeMetaService;
import com.huatu.ztk.backend.paper.dao.MatchDao;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.util.DateFormat;
import com.huatu.ztk.backend.util.ExcelManageUtil;
import com.huatu.ztk.backend.util.FunFileUtils;
import com.huatu.ztk.backend.util.MailUtil;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperStatus;
import com.huatu.ztk.user.bean.UserDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2017\11\16 0016.
 */
@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping(value = "/meta", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PracticeMetaController {
    private final static Logger logger = LoggerFactory.getLogger(PracticeMetaController.class);
    @Autowired
    private PracticeMetaService practiceMetaService;
    @Autowired
    private MatchDao matchDao;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    PaperDao paperDao;
    /**
     * 统计某一模考大赛的信息
     * @param paperId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "count/all", method = RequestMethod.GET)
    public Object getMatchMetaInfo(@RequestParam int paperId) throws BizException{
        SetOperations setOperations = redisTemplate.opsForSet();
        String matchCountSet = RedisKeyConstant.getMatchCountSet();
        Boolean member = setOperations.isMember(matchCountSet, String.valueOf(paperId));
        if(member){
            return SuccessMessage.create("模考大赛考试数据已经被处理过了");
        }
        Match match = matchDao.findById(paperId);
        if(match==null||match.getStatus()!= PaperStatus.AUDIT_SUCCESS){
            throw new BizException(ErrorResult.create(2000012,"无效的模考大赛ID"));
        }
        Map mapData = Maps.newHashMap();
        Map map = practiceMetaService.getCountByPosition(paperId);
        mapData.putAll(map);
        Long submitCount = practiceMetaService.getCountSubmit(paperId);
        mapData.put("submitCount",submitCount);
        Map map1 = practiceMetaService.getMaxScoreInfo(paperId);
        mapData.putAll(map1);
        double average = practiceMetaService.getAverage(paperId);
        mapData.put("average",average);
        Line line = practiceMetaService.getLine(paperId);
        mapData.put("line",line);
        List<Map> list =  practiceMetaService.getWrongQuestionMeta(paperId);
        mapData.put("wrongList",list);
        practiceMetaService.parseStatements(mapData,paperId);

        setOperations.add(matchCountSet,String.valueOf(paperId));
        return mapData;
    }
    /**
     * 统计某一模考大赛的信息
     * @param paperId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "enroll/user", method = RequestMethod.GET)
    public Object getMatchEnrollInfo(@RequestParam int paperId) throws BizException{
        SetOperations setOperations = redisTemplate.opsForSet();
        String matchEnrollSet = RedisKeyConstant.getMatchEnrollSet();
        Boolean member = setOperations.isMember(matchEnrollSet, String.valueOf(paperId));
        if(member){
            return SuccessMessage.create("模考大赛报名数据已经被处理过了");
        }
        Long start = System.currentTimeMillis();
        List<MatchUserMeta> matchUserMetas = practiceMetaService.getMatchUserListByMatchId(paperId);
//        matchUserMetas = matchUserMetas.stream().filter(i->i.getPracticeId()>0).collect(Collectors.toList());
        Long start1 = System.currentTimeMillis();
        logger.info("查询报名信息用时：{}",start1-start);
        List<UserDto> list = practiceMetaService.getUserInfoListByUserMate(matchUserMetas);
        long start2 = System.currentTimeMillis();
        logger.info("查询用户信息用时：{}",start2-start1);
        List dataList = Lists.newArrayList();
        for (int index = 0; index < list.size(); index++) {
            UserDto userDto = list.get(index);
            dataList.add(Lists.newArrayList(userDto.getName(),userDto.getNick(),userDto.getMobile()));
        }
        String[] title = {"名称","昵称","手机号"};
        try {
            ExcelManageUtil.writer(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH,"MatchEnrollInfo_"+paperId,"xls",dataList,title);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map mapData = Maps.newHashMap();
        mapData.put("title","MatchEnrollInfo_"+paperId);
        mapData.put("text","MatchEnrollInfo_"+paperId+"_"+new Date());
        mapData.put("filePath",FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH+"MatchEnrollInfo_"+paperId+".xls");
        mapData.put("attachName","MatchEnrollInfo_"+System.currentTimeMillis());

        try {
            MailUtil.sendMail(mapData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long start3 = System.currentTimeMillis();
        logger.info("写入excel用时：{}",start3-start2);

        setOperations.add(matchEnrollSet,String.valueOf(paperId));
        return SuccessMessage.create("报名数据统计成功");
    }

    /**
     * 考试概况
     * 按照分数统计模考大赛
     * 总答题人数，最高分、最高分所在地区、最低分、平均分、80分以上人数
     * 分数值分布情况，平均分按地区分布情况、各省答题人数分布情况
     * @return
     */
    @RequestMapping(value="match/score", method = RequestMethod.GET)
    public Object MatchUserWitchScore(@RequestParam int paperId) throws BizException {
        Match match = matchDao.findById(paperId);
        if(match==null||match.getStatus()!= PaperStatus.AUDIT_SUCCESS){
            throw new BizException(ErrorResult.create(2000012,"无效的模考大赛ID"));
        }
        Map mapData = Maps.newHashMap();
        //交卷人数
        Long submitCount = practiceMetaService.getCountSubmit(paperId);
        mapData.put("submitCount",submitCount);
        //最高分，最高分所在地区可以放在一起
        Map maxScoreMap = practiceMetaService.getMaxScoreAndPosition(paperId);
        mapData.putAll(maxScoreMap);
        //最低分
        Double minScore = practiceMetaService.getMinScore(paperId);
        mapData.put("minScore",minScore);
        //平均分
        Double average = practiceMetaService.getAverage(paperId);
        mapData.put("average",average);
        //80分以上人数可以跟分数分布情况一起查询
        Line line = practiceMetaService.getLine(paperId);
        mapData.put("line",practiceMetaService.parseLine(line));
        Integer count = practiceMetaService.getCountByScore(paperId,80,100);
        mapData.put("countByScore",count);
        //地区平均分排名
        List<Map> scoreResult = practiceMetaService.getAveragePositionSort(paperId);
        mapData.put("averagePositionSort",practiceMetaService.parseScoreResult(scoreResult));
        //地区报名人数排名
        List<Map> numResult = practiceMetaService.getEnrollPositionSort(paperId);
        mapData.put("enrollPositionSort",practiceMetaService.parseNumResult(numResult));
        return mapData;
    }
    /**
     * 考生明细（用户名，手机号，地区，分数，正确率，作答题目，作答时间）
     */
    @RequestMapping(value = "user/detail", method = RequestMethod.GET)
    public Object findUserInfo(@RequestParam int paperId,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size) throws BizException {
        //交卷人数
        Map resultMap = Maps.newHashMap();
        Long submitCount = practiceMetaService.getCountSubmit(paperId);
        int skip = (page-1)*size;
        if(submitCount<=skip){
            throw new BizException(ErrorResult.create(10001023,"无数据"));
        }
        List<Map> list = practiceMetaService.getUserInfoByPage(paperId,skip,size);
        resultMap.put("total",submitCount);
        resultMap.put("list",list);
        resultMap.put("next",page*size<submitCount);
        return resultMap;
    }

    /**
     * 试题明细
     * @param paperId
     * @param page
     * @param size
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "question/detail", method = RequestMethod.GET)
    public Object findQuestionInfo(@RequestParam int paperId,
                               @RequestParam(defaultValue = "-1") int moduleId,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size) throws BizException {
        //交卷人数
        return practiceMetaService.findQuestionInfo(paperId,moduleId,page,size);
    }

    /**
     * 用户模考大赛数据入库（统计使用）
     * @param paperId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "match/mysql",method = RequestMethod.POST)
    public Object insertMatchToDb(@RequestParam(required = false) int paperId)throws BizException{
        int size = practiceMetaService.saveMatchUserTODB(paperId);
        return SuccessMessage.create("共处理用户数据"+size+"条");
    }

    /**
     * 参加考试用户id集合
     * @param id
     * @return
     */
    @RequestMapping(value = "estimate/count",method = RequestMethod.GET)
    public Object countPaper(@RequestParam int id){
        practiceMetaService.countEstimatePaper(id);
        return SuccessMessage.create("查询完成");
    }


    /**
     * 统计某一时间段内，某个地区，报名模考大赛的信息
     * @param start
     * @param end
     * @param area
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "enroll/user/all", method = RequestMethod.GET)
    public Object getMatchEnrollInfoAll(@RequestParam String start,
                                        @RequestParam String end,
                                        @RequestParam(defaultValue = "1") Integer subject,
                                        @RequestParam String area) throws BizException{
        start = start.replace("-","/");
        end = end.replace("-","/");
        ArrayList<String> areas = Lists.newArrayList(area.replace("，",",").split(","));
        Long startTime = DateUtil.parseYYYYMMDDDate(start).getTime() ;
        Long endTime = DateUtil.parseYYYYMMDDDate(end).getTime() ;
        List<Match> matches = matchDao.findAll().stream().filter(i->i.getSubject()==subject).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(matches)) {
            throw new BizException(ErrorResult.create(10001231,"没有符合条件的模考大赛数据"));
        }
        //模考大赛考试人数
        List<Integer> paperIds = matches.stream().filter(i -> i.getStartTime() > startTime).filter(i -> i.getEndTime() < endTime).map(i -> i.getPaperId()).collect(Collectors.toList());
        Map<Integer,Long> paperMap = matches.stream().collect(Collectors.toMap(i->i.getPaperId(),i->i.getStartTime()));
        Map<Long,Long> userMap = Maps.newHashMap();
        Map<Long,Long> userPaperMap = Maps.newHashMap();
        Map<Long,String> userInfoMap = Maps.newHashMap(); //userId对应手机号
        Map<Long,String> userNameMap = Maps.newHashMap();   //userId对应用户名
        Map<Long,String> userAreaMap = Maps.newHashMap();   //userId 对应 地区名称
        logger.info("需要处理试卷个数：{}个：{}",paperIds.size(),paperIds);
        for (Integer paperId : paperIds) {
            logger.info("处理试卷：{}",paperId);
            List<MatchUserMeta> matchUserMetas = practiceMetaService.getMatchUserListByMatchId(paperId);
            for (MatchUserMeta matchUserMeta : matchUserMetas) {
                Long userId = matchUserMeta.getUserId();
                Long practiceId = userMap.getOrDefault(userId,0L);
                if(practiceId<matchUserMeta.getPracticeId()){
                    if(!isAreaContain(areas,matchUserMeta.getPositionName())){
                        continue;
                    }
                    userPaperMap.put(userId,paperMap.get(matchUserMeta.getPaperId()));
                    userMap.put(userId,practiceId);
                    userAreaMap.put(userId,matchUserMeta.getPositionName());
                }
            }

            List<UserDto> list = practiceMetaService.getUserInfoListByUserMate(matchUserMetas);
            logger.info("list.size={}",list.size());
            if(CollectionUtils.isNotEmpty(list)){
                userInfoMap.putAll(list.stream().collect(Collectors.toMap(i->i.getId(),i->i.getMobile()==null?"":i.getMobile())));
                userNameMap.putAll(list.stream().collect(Collectors.toMap(i->i.getId(),i->i.getName()==null?"":i.getName())));
            }
            logger.info("处理完毕：{}",paperId);
        }
        List<List> dataList = Lists.newArrayList();
        List<Map.Entry<Long, Long>> entries = userPaperMap.entrySet().stream().collect(Collectors.toList());
        entries.sort(Comparator.comparing(Map.Entry::getValue));
        for (Map.Entry<Long, Long> longLongEntry : entries) {
            Long userId = longLongEntry.getKey();
            Long time = longLongEntry.getValue();
            String areaName = userAreaMap.getOrDefault(userId,"");
            String phone  = userInfoMap.get(userId);
            String userName = userNameMap.get(userId);
            List list = Lists.newArrayList(DateFormat.dateTostr(new Date(time)),phone, areaName,userName);
            dataList.add(list);
        }
        long start2 = System.currentTimeMillis();
        String[] title = {"时间","手机号","地区","用户名"};
        try {
            ExcelManageUtil.writer(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH,"MatchEnrollInfo_all","xls",dataList,title);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map mapData = Maps.newHashMap();
        mapData.put("title","MatchEnrollInfo_all");
        mapData.put("text","MatchEnrollInfo_all"+"_"+new Date());
        mapData.put("filePath",FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH+"MatchEnrollInfo_all"+".xls");
        mapData.put("attachName","MatchEnrollInfo_"+System.currentTimeMillis());

        try {
            MailUtil.sendMail(mapData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long start3 = System.currentTimeMillis();
        logger.info("写入excel用时：{}",start3-start2);
        return SuccessMessage.create("报名数据统计成功");
    }

    /**
     * 报名地区是否在特定地区内
     * 如果特定地区为空，则返回true,如果特定地区不为空，但是报名地区为不在此列，则返回false
     * @param areas
     * @param positionName
     * @return
     */
    private boolean isAreaContain(ArrayList<String> areas, String positionName) {
        if(CollectionUtils.isEmpty(areas)){
            return true;
        }
        if(StringUtils.isBlank(areas.get(0))){
            return true;
        }
        if(areas.contains(positionName)){
            return true;
        }
        return false;
    }

    /**
     * 测试数据
     * @return
     */
    @RequestMapping(value = "estimate", method = RequestMethod.GET)
    public Object time(){
        practiceMetaService.countEstimatePhoneWithTimt();
        return SuccessMessage.create("参考数据统计成功");
    }

    /**
     * 统计某一个时间段的考试参考人数和参考人次
     * @param start
     * @param end
     * @param subject
     * @param paperType
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "count/simple", method = RequestMethod.GET)
    public Object getMatchCountInfoAll(@RequestParam String start,
                                        @RequestParam String end,
                                        @RequestParam(defaultValue = "-1") Integer subject,
                                        @RequestParam(defaultValue = "2,9") String paperType) throws BizException{
        start = start.replace("-","/");
        end = end.replace("-","/");
        List<Integer> paperTypes = Arrays.stream(paperType.replace("，", ",").split(",")).map(i -> Integer.parseInt(i)).collect(Collectors.toList());
        Long startTime = DateUtil.parseYYYYMMDDDate(start).getTime() ;
        Long endTime = DateUtil.parseYYYYMMDDDate(end).getTime() ;
        List<Paper> paperList = paperDao.findPaperList(subject,startTime,endTime,paperTypes);
        Set<Long> userIds = Sets.newHashSet();
        Integer total = 0;
        for (Paper paper : paperList) {
            Set<Long> tempIds = practiceMetaService.countEstimatePaper(paper.getId());
            total += tempIds.size();
            userIds.addAll(tempIds);
        }
        logger.info("参考人数{}，参考人次{}",userIds.size(),total);
        return SuccessMessage.create(start+"到"+end+"参考人数:"+userIds.size()+"参考人次："+total);
    }
}
