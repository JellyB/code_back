package com.huatu.tiku.interview.service.impl;

import com.huatu.tiku.interview.constant.TemplateEnum;
import com.huatu.tiku.interview.constant.UserStatusConstant;
import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.entity.po.*;
import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.entity.template.TemplateMsgResult;
import com.huatu.tiku.interview.entity.template.WechatTemplateMsg;
import com.huatu.tiku.interview.entity.vo.response.*;
import com.huatu.tiku.interview.manager.AdviceManager;
import com.huatu.tiku.interview.repository.*;
import com.huatu.tiku.interview.service.LearningReportService;
import com.huatu.tiku.interview.service.LearningSituationMaterialService;
import com.huatu.tiku.interview.service.LearningSituationService;
import com.huatu.tiku.interview.service.WechatTemplateMsgService;
import com.huatu.tiku.interview.util.json.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by x6 on 2018/1/17.
 */
@Service
@Slf4j
public class LearningReportServiceImpl  implements LearningReportService {

    @Autowired
    private ModulePracticeRepository modulePracticeRepository;
    @Autowired
    private MockPracticeRepository mockPracticeRepository;
    @Autowired
    QuestionAnswerRepository questionAnswerRepository;
    @Autowired
    UserClassRelationRepository userClassRelationRepository;
    @Autowired
    private LearningAdviceRepository learningAdviceRepository;
    @Autowired
    private PaperPracticeRepository paperPracticeRepository;
    @Autowired
    ClassInfoRepository classInfoRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    WechatTemplateMsgService templateMsgService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LearningSituationService learningSituationService;
    @Autowired
    PracticeContentTypeRepository practiceContentTypeRepository;
    @Autowired
    LearningSituationMaterialService learningSituationMaterialService;
    @Value("${dailyReportURL}")
    private String dailyReportURL;
    @Autowired
    private UserReportPushLogRepository userReportPushLogRepository;

    @Override
    public Object detail(String openId, String date,int type) {
        if(StringUtils.isEmpty(date)){
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = sdf.format(d);
        }

        List<PracticeContentTypeVO> practiceContentTypeList = new LinkedList<>();

        //根据学员当天所有的学习情况
        List<ModulePractice> modulePracticeList = new LinkedList<>();
        if(-1 != type && type != 13 && type != 14){
            modulePracticeList =  modulePracticeRepository.findByOpenIdAndAnswerDateAndStatus
                    (openId, date, WXStatusEnum.Status.NORMAL.getStatus());
        }else if(-1 == type ){
            modulePracticeList = modulePracticeRepository.findByOpenIdAndAnswerDateAndStatus
                    (openId, date, WXStatusEnum.Status.NORMAL.getStatus());
        }

        List<PaperPractice> paperPracticeList = new LinkedList<>();
        if(type == -1 || type == 13){
            paperPracticeList  = paperPracticeRepository.findByOpenIdAndAnswerDateAndStatus
                    (openId, date, WXStatusEnum.Status.NORMAL.getStatus());
        }

        //将当天练习内容放入map中
        Map<Object, List<Object>> map = new HashMap<>();
        List<MockPractice> mockPracticeList = new LinkedList<>();
        if(type == -1 || type == 14){
            learningSituationService.upMockList(map,openId,date);
        }
        //模块练习
        if(CollectionUtils.isNotEmpty(modulePracticeList)){
            for(ModulePractice i:modulePracticeList){

                List<Object> modulePractices = map.get(i.getPracticeContent()+"");
                if(CollectionUtils.isEmpty(modulePractices)){
                    modulePractices = new LinkedList<Object>();
                }
                ModulePracticeVO vo = new ModulePracticeVO();
                BeanUtils.copyProperties(i,vo);
                //练习内容
                PracticeContentType practiceContent = practiceContentTypeRepository.findOne(i.getPracticeContent());
                vo.setPracticeContentName(practiceContent.getName());
                // 表现
                ExpressionVO expressionVO = getExpression(vo.getPronunciation(), vo.getFluencyDegree(), vo.getDeportment());
                BeanUtils.copyProperties(expressionVO,vo);
                //优点+问题
                RemarkListVO remarkList = getRemarkList(i.getAdvantage(), i.getDisAdvantage(), i.getPracticeContent());
                vo.setAdvantageList(remarkList.getAdvantageList());
                vo.setDisAdvantageList(remarkList.getDisAdvantageList());

                modulePractices.add(vo);
                map.put(i.getPracticeContent()+"",modulePractices);

            }

        }
        //套题演练
        if(CollectionUtils.isNotEmpty(paperPracticeList)){
            List<Object> practiceList = map.get(13+"");
            for(PaperPractice i:paperPracticeList) {
                if (CollectionUtils.isEmpty(practiceList)) {
                    practiceList = new LinkedList<Object>();
                }
                PaperPracticeVO vo = new PaperPracticeVO();
                BeanUtils.copyProperties(i, vo);

               //建议
                vo.setBehaviorAdvice(AdviceManager.getAdvice(i.getBehavior(), 1,learningAdviceRepository));
                vo.setLanguageExpressionAdvice(AdviceManager.getAdvice(i.getLanguageExpression(), 2,learningAdviceRepository));
                vo.setFocusTopicAdvice(AdviceManager.getAdvice(i.getFocusTopic(), 3,learningAdviceRepository));
                vo.setIsOrganizedAdvice(AdviceManager.getAdvice(i.getIsOrganized(), 4,learningAdviceRepository));
                vo.setHaveSubstanceAdvice(AdviceManager.getAdvice(i.getHaveSubstance(), 5,learningAdviceRepository));
                practiceList.add(vo);
            }
                map.put(""+13,practiceList);
        }


         return convertReport(map, practiceContentTypeList);
    }

    //查询时间列表
    @Override
    public List<String> date(String openId) {
        List<String> dateList = new LinkedList<>();
        List<User> userList= userRepository.findByOpenIdAndStatus(openId, WXStatusEnum.Status.NORMAL.getStatus());
        if(CollectionUtils.isEmpty(userList) || userList.get(0).getBizStatus() == UserStatusConstant.BizStatus.UN_BIND.getBizSatus()){
            return dateList;
        }
        List<UserClassRelation> ucList = userClassRelationRepository.findByOpenIdAndStatus(openId, 1);
        long classId = 1L;
        if(CollectionUtils.isNotEmpty(ucList)){
            classId = ucList.get(0).getClassId();
        }
        //设置初始时间
        ClassInfo classInfo = classInfoRepository.findOne(classId);
        Date start = classInfo.getStartTime();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        int startHour = startCalendar.get(Calendar.HOUR_OF_DAY);

        //获取当前时间
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String nowStr = sdf.format(now);
        int hour = now.getHours();
        Calendar tempCalendar = startCalendar;
        for(int i = 0;i < 10;i++){
            String temp = sdf.format(tempCalendar.getTime());

            temp = sdf.format(tempCalendar.getTime());
            //当天十点之前，不展示当天信息
            if(temp.compareTo(nowStr) < 0 || (hour >= startHour && temp.compareTo(nowStr) == 0)){
                dateList.add(temp);
                tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }else{
                break;
            }
        }
        return dateList;
    }

    private List<PracticeContentTypeVO> convertReport(Map<Object, List<Object>> map, List<PracticeContentTypeVO> result) {
        List<PracticeContentTypeVO> practiceContentTypeList = (List<PracticeContentTypeVO>)learningSituationMaterialService.getPracticeContent();

        for(PracticeContentTypeVO vo:practiceContentTypeList){
           long id = vo.getId();
           PracticeContentTypeVO newVO = new PracticeContentTypeVO();
           BeanUtils.copyProperties(vo,newVO);
           //一级模块
           List<Object> questionList = map.get(""+id);
           if(CollectionUtils.isNotEmpty(questionList)){
               newVO.setQuestionList(questionList);
               result.add(newVO);
           }else{
               //二级模块
               List<PracticeContentTypeVO> subList = vo.getSubList();
               LinkedList<PracticeContentTypeVO>  newSubList = new LinkedList<>();
               for(PracticeContentTypeVO subVO:subList){
                   long subVoId = subVO.getId();
                   PracticeContentTypeVO newSubVO = new PracticeContentTypeVO();
                   BeanUtils.copyProperties(subVO,newSubVO);
                   List<Object> subQuestionList = map.get(""+subVoId);
                   if(CollectionUtils.isNotEmpty(subQuestionList)){
                       newSubVO.setQuestionList(subQuestionList);
                       newSubList.add(newSubVO);
                   }
               }
               if(CollectionUtils.isNotEmpty(newSubList)){
                   newVO.setSubList(newSubList);
                   result.add(newVO);
               }
           }

       }
       return result;

    }

    private RemarkListVO getRemarkList(String advantage,String disAdvantage,long practiceContent) {
        RemarkListVO remarkList =(RemarkListVO) learningSituationMaterialService.getRemarkList(practiceContent);
        List<PracticeRemark> ad = new LinkedList<>();
        List<PracticeRemark> disAd = new LinkedList<>();
        if(StringUtils.isNotEmpty(advantage)){
            String[] split = advantage.split(",");
            remarkList.getAdvantageList().forEach(j -> {
                for(int k= 0;k<split.length;k++){
                    if(new Long(split[k]) == j.getId() ){
                        ad.add(j);
                    }
                }
            });
        }

        if(StringUtils.isNotEmpty(disAdvantage)){
            String[] split = disAdvantage.split(",");
            remarkList.getDisAdvantageList().forEach(j -> {
                for(int k= 0;k<split.length;k++){
                    if(new Long(split[k]) == j.getId() ){
                        disAd.add(j);
                    }
                }
            });
        }
        return  RemarkListVO.builder()
                .advantageList(ad)
                .disAdvantageList(disAd)
                .build();

    }


    ExpressionVO getExpression(String pronunciation, String fluencyDegree, String deportment){
        ExpressionVO expressionList =(ExpressionVO) learningSituationMaterialService.getExpressionList();
        ExpressionVO vo = new ExpressionVO();
        //语音语调
        expressionList.getPronunciationList().forEach(j -> {
            if(StringUtils.isNotEmpty(pronunciation) && j.getId() == new Long(pronunciation)){
                List<PracticeExpression> temp = new LinkedList<>();
                temp.add(j);
                vo.setPronunciationList(temp);
            }
        });
        //流畅程度
        expressionList.getFluencyDegreeList().forEach(j -> {
            if(StringUtils.isNotEmpty(fluencyDegree) && j.getId() == new Long(fluencyDegree)){
                List<PracticeExpression> temp = new LinkedList<>();
                temp.add(j);
                vo.setFluencyDegreeList(temp);
            }
        });
        //仪态动作
        expressionList.getDeportmentList().forEach(j -> {
            if(StringUtils.isNotEmpty(deportment) && j.getId() == new Long(deportment)){
                List<PracticeExpression> temp = new LinkedList<>();
                temp.add(j);
                vo.setDeportmentList(temp);
            }
        });
        return vo;
    }


        /**
     *  生成每日学习报告
     * @return
     */
    @Override
    public Result dailyReport() {

        List<User> userList = userRepository.findByStatusAndBizStatus
                (WXStatusEnum.Status.NORMAL.getStatus(), UserStatusConstant.BizStatus.COMPLETED.getBizSatus());

        if(CollectionUtils.isNotEmpty(userList)){
            for(User user:userList){
                String openId = user.getOpenId();
                //查询用户当前所在班级
                List<UserClassRelation> relationList = userClassRelationRepository.findByOpenIdAndStatus(openId,WXStatusEnum.Status.NORMAL.getStatus());
                long classId = 0L;
                if(CollectionUtils.isNotEmpty(relationList)){
                    classId = relationList.get(0).getClassId();
                }

                 //根据所属班级查询总统计天数
                ClassInfo classInfo = classInfoRepository.findOne(classId);
                String startDate = "2018-04-26";
                String endDate = "2018-05-05";
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String now = sdf.format(d);
                if(null != classInfo){
                    endDate = sdf.format(classInfo.getEndTime());
                    startDate = sdf.format(classInfo.getStartTime());
                    if(now.compareTo(endDate)<=0 && now.compareTo(startDate) >=0){
                        pushDailyReport(openId,now);
                    }
                }else{
                    log.info("该学员没有所属班级，openId:{}",openId);
                }

            }

        }

        return Result.ok();
    }


    /**
     *  推送每日学习报告消息
     */
    @Override
    public TemplateMsgResult pushDailyReport(String openId,String now)  {
        //判断用户是否已经推送学习报告
        List<UserReportPushLog> pushList = userReportPushLogRepository.findByOpenIdAndDate(openId, now);
        if(CollectionUtils.isNotEmpty(pushList)){
            //当日学习报告已经推送，无需重复推送
            return null;
        }

        //根据学员当天所有的学习情况
        List<PaperPractice> paperPracticeList = paperPracticeRepository.findByOpenIdAndAnswerDateAndStatus
                (openId, now, WXStatusEnum.Status.NORMAL.getStatus());

        List<ModulePractice> modulePracticeList = modulePracticeRepository.findByOpenIdAndAnswerDateAndStatus
                (openId, now, WXStatusEnum.Status.NORMAL.getStatus());
        List<MockPractice> mockPracticeList = mockPracticeRepository.findByOpenIdAndAnswerDateAndStatus
                (openId, now, WXStatusEnum.Status.NORMAL.getStatus());

        if(CollectionUtils.isNotEmpty(paperPracticeList)  || CollectionUtils.isNotEmpty(modulePracticeList) || CollectionUtils.isNotEmpty(mockPracticeList))  {
            WechatTemplateMsg templateMsg = new WechatTemplateMsg(openId, TemplateEnum.DailyReport);

            templateMsg.setUrl(String.format(dailyReportURL,openId,now));
            String templateMsgJson = JsonUtil.toJson(templateMsg);
            TemplateMsgResult result = templateMsgService.sendTemplate(templateMsgJson);
            UserReportPushLog reportPushLog = UserReportPushLog.builder()
                    .date(now)
                    .openId(openId)
                    .build();
            userReportPushLogRepository.save(reportPushLog);
        }
        return null;
    }
//
//    //推送学习历程消息
//    public TemplateMsgResult pushTotalReport(String openId)  {
//        String accessToken = stringRedisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN_KEY);
//        WechatTemplateMsg templateMsg = new WechatTemplateMsg(openId, TemplateEnum.TotalReport);
//
//        //根据openId查询用户姓名
//        List<User> userList = userRepository.findByOpenIdAndStatus(openId, WXStatusEnum.Status.NORMAL.getStatus());
//        String username= "";
//        if(CollectionUtils.isNotEmpty(userList)){
//            username = userList.get(0).getName();
//        }
//        TreeMap<String, TreeMap<String, String>> data = templateMsg.getData();
//        data.put("keyword1", WechatTemplateMsg.item(username,"#000000"));
//        templateMsg.setData(data);
//        templateMsg.setUrl(dailyReportURL+openId);
//        String templateMsgJson = JsonUtil.toJson(templateMsg);
//        TemplateMsgResult result = templateMsgService.sendTemplate(
//                accessToken,
//                templateMsgJson);
//        return result;
//    }
//
//
//    /**
//     * 生成每日报告
//     */
//    private LearningReport saveDailyReport(int daySort,String openId) {
//
//        //平均分统计
//        List<Object[]> avgList = paperPracticeRepository.countTodayAvg(openId);
//
//        //答题数量统计
//        List<Object[]> answerCountList = paperPracticeRepository.countTodayAnswerCount(openId);
//        LearningReport learningReport = buildReport(avgList.get(0), answerCountList);
//        learningReport.setDaySort(daySort);
//
//        String format = DateFormatUtil.NORMAL_DAY_FORMAT.format(new Date());
//        learningReport.setReportDate(format);
//        learningReport.setType(DAILY_REPORT.getCode());
//
//        learningReport.setOpenId(openId);
//        learningReport.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
//        learningReport = learningReportRepository.save(learningReport);
//
//        return learningReport;
//    }
//
//    /**
//     * 生成总结报告
//     */
//    private LearningReport saveTotalReport(String openId) {
//
//        //平均分统计
//        List<Object[]> avgList = paperPracticeRepository.countTotalAvg(openId);
//        //答题数量统计
//        List<Object[]> answerCountList = paperPracticeRepository.countTotalAnswerCount(openId);
//        LearningReport learningReport = buildReport(avgList.get(0), answerCountList);
//        String format = DateFormatUtil.NORMAL_DAY_FORMAT.format(new Date());
//
//        learningReport.setReportDate(format);
//        learningReport.setDaySort(7);
//        learningReport.setType(ReportTypeConstant.TOTAL_REPORT.getCode());
//        learningReport.setOpenId(openId);
//        learningReport.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
//        learningReport = learningReportRepository.save(learningReport);
//
//        return learningReport;
//    }
//
//
//    /**
//     * 将查询结果封装成report对象
//     * @param avgList
//     * @param answerCountList
//     * @return
//     */
//    private LearningReport  buildReport(Object[] avgList,List<Object[]> answerCountList){
//        LearningReport.LearningReportBuilder builder = LearningReport.builder();
//        String avgBehaviorStr = String .format("%.2f",avgList[0]);
//        double avgBehavior = Double.parseDouble(avgBehaviorStr);
//        String avgLanguageExpressionStr = String .format("%.2f",avgList[1]);
//        double avgLanguageExpression = Double.parseDouble(avgLanguageExpressionStr);
//        String avgFocusTopicStr = String .format("%.2f",avgList[2]);
//        double avgFocusTopic = Double.parseDouble(avgFocusTopicStr);
//        String avgIsOrganizedStr = String .format("%.2f",avgList[3]);
//        double avgIsOrganized = Double.parseDouble(avgIsOrganizedStr);
//        String avgHaveSubstanceStr = String .format("%.2f",avgList[4]);
//        double avgHaveSubstance = Double.parseDouble(avgHaveSubstanceStr);
//
//        builder.behavior(avgBehavior)
//                .languageExpression(avgLanguageExpression)
//                .focusTopic(avgFocusTopic)
//                .isOrganized(avgIsOrganized)
//                .haveSubstance(avgHaveSubstance);
//
//            int totalCount = 0;
//            for(Object[] answerCount:answerCountList){
//                Object practiceType = answerCount[0];
//                Object count = answerCount[1];
//                totalCount = totalCount + Integer.parseInt(count.toString());
//                switch (Integer.parseInt(practiceType.toString())) {
//                    case 1: {
//                        builder.oneAnswerCount(Integer.parseInt(count.toString()));
//                        break;
//                    }
//                    case 2: {
//                        builder.twoAnswerCount(Integer.parseInt(count.toString()));
//                        break;
//                    }
//                    case 3: {
//                        builder.threeAnswerCount(Integer.parseInt(count.toString()));
//                        break;
//                    }
//                    case 4: {
//                        builder.fourAnswerCount(Integer.parseInt(count.toString()));
//                        break;
//                    }
//                    case 5: {
//                        builder.fiveAnswerCount(Integer.parseInt(count.toString()));
//                        break;
//                    }
//                    case 6: {
//                        builder.sixAnswerCount(Integer.parseInt(count.toString()));
//                        break;
//                    }
//                };
//            }
//
//        builder.totalAnswerCount(totalCount);
//        LearningReport report = builder.build();
//        return report;
//    }
//
//
//
//    @Override
//    public Result learningReport(String openId) {
//
//        //根据openId查询用户id
//        User user = userRepository.getUserByOpenIdAndStatus(openId, WXStatusEnum.Status.NORMAL.getStatus());
//        if(null == user){
//            log.warn("请求参数异常，不存在对应的用户信息。OpenId:{}",openId);
//            return Result.build(ResultEnum.OPENID_ERROR);
//        }
//        //查询用户的所有报告，按天数序号排列
//        List<LearningReport> reportList = learningReportRepository.findByOpenIdOrderByDaySortAsc(user.getOpenId());
//
//        List<ReportResponseVO> resultList = new LinkedList<>();
//        for(LearningReport report: reportList){
//            ReportResponseVO reportResponseVO = new ReportResponseVO();
//            BeanUtils.copyProperties(report,reportResponseVO);
//            if( TOTAL_REPORT.getCode() == report.getType()){
//                //根据成绩给出建议
//                // 举止仪态
//                Double behavior = report.getBehavior();
//                reportResponseVO.setBehaviorAdvice(getAdvice(behavior,1));
//
//                Double languageExpression = report.getLanguageExpression();
//                reportResponseVO.setLanguageExpressionAdvice(getAdvice(languageExpression,2));
//
//                Double focusTopic = report.getFocusTopic();
//                reportResponseVO.setFocusTopicAdvice(getAdvice(focusTopic,3));
//
//                Double isOrganized = report.getIsOrganized();
//                reportResponseVO.setIsOrganizedAdvice(getAdvice(isOrganized,4));
//
//                Double haveSubstance = report.getHaveSubstance();
//                reportResponseVO.setHaveSubstanceAdvice(getAdvice(haveSubstance,5));
//            }else if( DAILY_REPORT.getCode() == report.getType()){
//                //老师评语(查询当天学员所有的学习记录)
//                String reportDate = report.getReportDate();
//                List<String> remarkList = paperPracticeRepository.findRemarksByOpenIdAndAnswerDateAndStatusOrderByGmtCreateAsc(openId,reportDate);
//                reportResponseVO.setRemarkList(remarkList);
//            }
//            resultList.add(reportResponseVO);
//        }
//        return Result.ok(resultList);
//    }
//
//    @Override
//    public Result check(String openId) {
//        List<User> userList = userRepository.findByOpenIdAndStatus(openId, WXStatusEnum.Status.NORMAL.getStatus());
//
//        String content = "";
//        if(CollectionUtils.isEmpty(userList)){
//            log.info("校验用户状态 抱歉，您尚未填写个人信息，无法核实您的学员身份~");
////            content = "校验用户状态 抱歉，您尚未填写个人信息，无法核实您的学员身份~";
////            pushText(openId,content);
//            return Result.ok(NO_INFO.getStatus());
//
//        }else{
//            //判断报告是否已经生成
//            List<LearningReport> learningReports = learningReportRepository.findByOpenIdOrderByIdAsc(openId);
//            if(CollectionUtils.isEmpty(learningReports)){
//                log.info("学习报告尚未生成~");
////                content = "学习报告尚未生成~";
////                pushText(openId,content);
//                return Result.ok(NO_REPORT.getStatus());
//            }
//        }
//        return Result.ok(EXIST_REPORT.getStatus());
//    }
//
//    public void pushText(String openId,String content) {
//
//        String msg = "{\"touser\":"+openId+", \"msgtype\":\"text\",\"text\":{ \"content\": "+content+"\"} }";
//        String jsonString = new Gson().toJson(msg).toString();
//        // 调用接口获取access_token
//        String at = redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN);
//        String requestUrl = WeChatUrlConstant.MESSAGE_MANY_URL.replace(WeChatUrlConstant.ACCESS_TOKEN, at);
//
//        HttpHeaders headers = new HttpHeaders();
//        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
//        headers.setContentType(type);
//        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
//        HttpEntity<String> formEntity = new HttpEntity<>(msg, headers);
//
//        String result =  restTemplate.postForObject(requestUrl,formEntity,String.class);
//        log.info("result:"+result);
//    }
//
//
//
//



}
