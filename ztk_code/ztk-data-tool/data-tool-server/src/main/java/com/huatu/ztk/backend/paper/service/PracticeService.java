package com.huatu.ztk.backend.paper.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.huatu.ztk.backend.config.UrlConfig;
import com.huatu.ztk.backend.paper.bean.*;
import com.huatu.ztk.backend.paper.constant.EssayConstant;
import com.huatu.ztk.backend.paper.dao.MatchDao;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.dao.PaperQuestionDao;
import com.huatu.ztk.backend.paper.dao.PracticeDao;
import com.huatu.ztk.backend.paper.error.EssayErrors;
import com.huatu.ztk.backend.question.bean.PracticeQuestion;
import com.huatu.ztk.backend.question.bean.ViewPracticeQuestion;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.backend.user.dao.UserDao;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.MatchBackendStatus;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.common.PaperStatus;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.question.bean.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by aatrox on 2017/3/6.
 */
@Service
public class PracticeService {
    private static final Logger logger = LoggerFactory.getLogger(PracticeService.class);

    public static final String MATCH_COURSE_INFO = "深度解析：考试当天19:00准时开讲！";

    public static final String MATCH_COURSE_INSTRUCTION = "考试说明：\n1. 开考前5分钟可提前进入考场查看题目，开考30分钟后则无法报名和进入考试。\n2. 开始答题后不可暂停计时，如需完全退出可直接提交试卷;考试结束自动交卷。\n3. 分享“报名成功”截图至微博并@华图网校即有机会免费获得本期模考直播解析课。";

    @Autowired
    private PracticeDao practiceDao;
    @Autowired
    private PaperService paperService;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RestOperations restTemplate;
    @Autowired
    private UserDao userDao;
    @Autowired
    private PaperQuestionDao paperQuestionDao;

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private MatchDao matchDao;

    @Autowired
    UrlConfig urlConfig;

    //获取行测试卷
    public List<PaperBean> findAll(String catgory, String area, int year, String name, int type, int onStatus, long createTime, int uid) throws BizException {
        if (StringUtils.isEmpty(catgory)) {
            return null;
        }
        List<Integer> areas = area.equals("0") ? Lists.newArrayList()
                : Arrays.stream(area.split(","))
                .map(Integer::new)
                .collect(Collectors.toList());
        List<Integer> catgoryIds = paperService.getCatgoryIds(catgory);
        List<EstimatePaper> paperList = practiceDao.list(catgoryIds, areas, year, name,
                type, onStatus, createTime, paperService.findCreator(catgoryIds, uid));
        List<PaperBean> paperBeanList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(paperList)) {
            return paperBeanList;
        }
        List<Integer> uids = Lists.newArrayList();
//        System.currentTimeMillis()
        for (EstimatePaper paper : paperList) {
            uids.add(paper.getCreatedBy());

            //已经结束
            if (paper.getStatus() == BackendPaperStatus.ONLINE && paper.getEndTime() < System.currentTimeMillis()) {
                System.out.println(paper.getEndTime());
                System.out.println();
                paper.setStatus(BackendPaperStatus.END);
            }

            PaperBean paperBean = castPaper(paper);
            paperBeanList.add(paperBean);
        }
        List<User> userList = userDao.findAllById(uids);
        Map<Integer, String> userMap = userList.stream()
                .collect(Collectors.toMap(User::getId, User::getAccount));
        for (PaperBean paperBean : paperBeanList) {
            int creatorBy = new Integer(paperBean.getCreatedBy());
            String username = userMap.get(creatorBy);
            paperBean.setCreateUser(username);
        }
        if (onStatus > 90) {
            List<PaperBean> paperBeanFindByOnStatus = Lists.newLinkedList();
            int finalOnStatus = onStatus / 100;
            paperBeanFindByOnStatus = paperBeanList.stream()
                    .filter(paper -> paper.getStatus() == finalOnStatus)
                    .collect(Collectors.toList());
            return paperBeanFindByOnStatus;
        }
        return paperBeanList;
    }

    public void createPaper(PracticePaperBean practicePaper, String areas) throws BizException {
        if (StringUtils.isNotEmpty(areas)) {
            final List<Integer> areaArr = Arrays.stream(areas.split(",")).filter(s -> StringUtils.isNotEmpty(s)).map(Integer::valueOf).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(areaArr)) {
                for (Integer area : areaArr) {
                    practicePaper.setArea(area);
                    savePracticePaper(practicePaper);
                }
            }
        } else {
            practicePaper.setArea(AreaConstants.QUAN_GUO_ID);
            savePracticePaper(practicePaper);
        }
    }

    private void savePracticePaper(PracticePaperBean practicePaper) throws BizException {
        practicePaper.setId(paperService.generatePaperId());
        practicePaper.setStartTime(truncateTime(practicePaper.getStartTime()));
        practicePaper.setEndTime(truncateTime(practicePaper.getEndTime()));

        EstimatePaper estimatePaper = castPracticePaperBean(practicePaper);

        paperService.checkPaper(estimatePaper);
        if (practicePaper.getType() == PaperType.MATCH) {

            Match match = new Match();
            match.setPaperId(practicePaper.getId());
            match.setName(practicePaper.getName());
            match.setCourseId(practicePaper.getCourseId());

            long startTime = practicePaper.getStartTime();
            long endTime = practicePaper.getEndTime();

            match.setStartTime(practicePaper.getStartTime());
            match.setEndTime(practicePaper.getEndTime());
            match.setTimeInfo(getTimeInfo(startTime, endTime));
            match.setCourseInfo(practicePaper.getCourseInfo());
            match.setInstruction(practicePaper.getInstruction());
            match.setTag(practicePaper.getTag());
            match.setSubject(practicePaper.getCatgory());
            match.setStatus(MatchBackendStatus.CREATE);
            if (practicePaper.getEssayId() != 0) {
                EssayPractice(practicePaper, match, EssayConstant.EssayPracticeType.CONNECTED.getType());
            } else {
                match.setInstructionPC(practicePaper.getInstructionPC());
            }
            matchDao.save(match);
        }

        practiceDao.createPaper(estimatePaper);
    }

    public Map<String, String> connectEssay(Match match, int type) throws BizException {
        Map<String, String> mapData = null;
        // try {
        //1已关联  2已上线  4解除绑定  5下线
        logger.info("urlConfig.getEssayUrl():{}", urlConfig.getEssayUrl());
        String essayUrl = urlConfig.getEssayUrl()==null ? "https://ns.huatu.com" : urlConfig.getEssayUrl();
        String url = essayUrl + "/e/api/v1/mock/status?id=" + match.getEssayPaperId() + "&practiceId=" + match.getPaperId() + "&type=" + type;

        ResponseMsg<EssayMockExam> responseMsgResponseEntity = restTemplate.postForObject(url, null, ResponseMsg.class);
        if (responseMsgResponseEntity.getCode() != 1000000) {
            throw new BizException(ErrorResult.create(responseMsgResponseEntity.getCode(), responseMsgResponseEntity.getMessage()));
        }
        if (responseMsgResponseEntity != null) {
            mapData = (Map) responseMsgResponseEntity.getData();
        }
//
//        } catch (Exception e) {
//            logger.error("绑定的申论id有误申论id：" + match.getEssayPaperId() + "行测id：" + match.getPaperId());
//            e.printStackTrace();
//        }
        return mapData;
    }

    private String getTimeInfo(long startTime, long endTime) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date(startTime));

        int day = instance.get(Calendar.DAY_OF_WEEK);

        //考试时间：2017年8月20日（周日）09:00-11:00
        String timeInfo = DateFormatUtils.format(startTime, "yyyy年M月d日") + "（%s）%s-%s";
        String dayString = "";
        switch (day) {
            case Calendar.SUNDAY:
                dayString = "周日";
                break;

            case Calendar.MONDAY:
                dayString = "周一";
                break;

            case Calendar.TUESDAY:
                dayString = "周二";
                break;
            case Calendar.WEDNESDAY:
                dayString = "周三";
                break;
            case Calendar.THURSDAY:
                dayString = "周四";
                break;
            case Calendar.FRIDAY:
                dayString = "周五";
                break;

            case Calendar.SATURDAY:
                dayString = "周六";
                break;
        }

        timeInfo = String.format(timeInfo, dayString, DateFormatUtils.format(startTime, "HH:mm"),
                DateFormatUtils.format(endTime, "HH:mm"));

        return "考试时间：" + timeInfo;
    }

    public PaperBean castPaper(EstimatePaper paper) {
        List<ModuleBean> moduleBeanList = paperService.getModuleBeanList(paper);
        PracticePaperBean paperBean = new PracticePaperBean();
        paperBean.setId(paper.getId());
        paperBean.setArea(paper.getArea());
        paperBean.setYear(paper.getYear());
        paperBean.setCatgory(paper.getCatgory());
        paperBean.setName(paper.getName());
        paperBean.setAreaName(AreaConstants.getFullAreaNmae(paper.getArea()));
        paperBean.setCreateTime(paper.getCreateTime());
        paperBean.setType(paper.getType());
        paperBean.setScore(paper.getScore());
        paperBean.setStatus(paper.getStatus());
        paperBean.setTime(paper.getTime() / 60);//将秒转换为分钟
        paperBean.setModules(moduleBeanList);

        paperBean.setStartTime(truncateTime(paper.getStartTime()));
        paperBean.setEndTime(truncateTime(paper.getEndTime()));

        paperBean.setOnlineTime(paper.getOnlineTime());
        paperBean.setOfflineTime(paper.getOfflineTime());
        paperBean.setDescrp(paper.getDescrp());
        paperBean.setUrl(paper.getUrl());
        paperBean.setHideFlag(paper.getHideFlag());
        paperBean.setLookParseTime(paper.getLookParseTime());
        paperBean.setCreatedBy(paper.getCreatedBy());
        //判断是否在线
        repairOnStatus(paperBean);


        //回显添加match内容
        if (paperBean.getType() == PaperType.MATCH) {
            Match match = matchDao.findById(paper.getId());
            paperBean.setCourseId(match.getCourseId());
            paperBean.setTag(match.getTag());
            paperBean.setCourseInfo(match.getCourseInfo());
            paperBean.setInstruction(match.getInstruction());
            paperBean.setEssayId(match.getEssayPaperId());
            paperBean.setInstructionPC(match.getInstructionPC());
        }

        return paperBean;
    }

    //设置上线状态
    public void repairOnStatus(PracticePaperBean practicePaperBean) {
        long onlineTime = practicePaperBean.getOnlineTime();//上线时间
        long offlineTime = practicePaperBean.getOfflineTime();//下线时间
        long time = new Date().getTime();
        if (onlineTime == 0) {
            practicePaperBean.setOnlineTime(practicePaperBean.getStartTime());
            onlineTime = practicePaperBean.getOnlineTime();//上线时间
        }
        if (offlineTime == 0) {
            practicePaperBean.setOfflineTime(practicePaperBean.getEndTime());
            offlineTime = practicePaperBean.getOfflineTime();//下线时间
        }

        if (time < onlineTime) {
            practicePaperBean.setOnStatus(BackendPaperStatus.BEFORE_ONLINE);//未上线
        } else if (time < offlineTime && practicePaperBean.getStatus() == BackendPaperStatus.AUDIT_SUCCESS) {//上线时间并且审核通过才是上线
            practicePaperBean.setOnStatus(BackendPaperStatus.ONLINE);//上线
        } else if (time >= offlineTime) {
            practicePaperBean.setOnStatus(BackendPaperStatus.OFFLINE);//下线
        } else {//上线时间但是审核未通过，此时未上线
            practicePaperBean.setOnStatus(BackendPaperStatus.BEFORE_ONLINE);//未上线
        }
    }

    //从redis中获取模考参加人数
    public long viewCountAnswer(int id) {
        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(id);
        long cardCounts = zSetOperations.zCard(paperPracticeIdSore);
        return cardCounts;
    }

    public PaperBean findById(int id) throws BizException {
        EstimatePaper paper = practiceDao.findById(id);
        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        PaperBean paperBean = castPaper(paper);
        if (paperBean instanceof PracticePaperBean) {
            List<PracticeModuleBean> practiceModuleBeans = castPracticeModuleBean(paper);
            ((PracticePaperBean) paperBean).setPracticeModuleBeans(practiceModuleBeans);
        }
        return paperBean;
    }

    public List<PracticeModuleBean> castPracticeModuleBean(EstimatePaper paper) {

        List<PracticeModuleBean> practiceModuleBeans = Lists.newArrayList();
        int index = 0;
        List<Module> modules = paper.getModules();
        if (CollectionUtils.isEmpty(modules)) {
            return Lists.newArrayList();
        }
        if (CollectionUtils.isNotEmpty(modules) && CollectionUtils.isEmpty(paper.getQuestions())) {
            for (Module module : modules) {
                PracticeModuleBean practiceModuleBean = new PracticeModuleBean();
                practiceModuleBean.setId(module.getCategory());
                practiceModuleBean.setName(module.getName());
                practiceModuleBean.setPracticeSorts(Lists.newArrayList());
                practiceModuleBeans.add(practiceModuleBean);
            }
            return practiceModuleBeans;
        }
        List<Question> questions = practiceDao.findAllQuestion(paper.getQuestions());
        //qid  parent
        Map<Integer, Integer> parentMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(questions)) {
            for (Question question : questions) {
                if (question instanceof GenericQuestion) {
                    parentMap.put(question.getId(), ((GenericQuestion) question).getParent());
                }
                if (question instanceof GenericSubjectiveQuestion) {
                    parentMap.put(question.getId(), ((GenericSubjectiveQuestion) question).getParent());
                }
                if(question instanceof CompositeQuestion||question instanceof CompositeSubjectiveQuestion){
                    parentMap.put(question.getId(), 0);
                }
            }
        }
        // qid
        List<Integer> qidList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(paper.getQuestions())) {
            for (Integer qid : paper.getQuestions()) {
                if(parentMap.get(qid)==null){
                    logger.error("qid={}，没有parent属性",qid);
                    continue;
                }
                if (parentMap.get(qid) == 0) {
                    qidList.add(qid);
                } else {
                    if (!qidList.contains(parentMap.get(qid))) {
                        qidList.add(parentMap.get(qid));
                    }
                }
            }
        }

        // int sortQuestion = 1;
        try {
            for (Module module : modules) {
                List<Integer> qids = paper.getQuestions().subList(index, index + module.getQcount());
                LinkedHashMap<Integer, List<String>> listLinkedHashMap = new LinkedHashMap<>();
                for (Integer qid : qids) {
                    int parent = parentMap.get(qid);
                    //int sort = paper.getQuestions().indexOf(qid) + 1;
                    if (parent == 0) {
                        List<String> sortList = Lists.newArrayList();
                        sortList.add((qidList.indexOf(qid) + 1) + "");
                        listLinkedHashMap.put(qid, sortList);
                    } else {
                        if (!listLinkedHashMap.containsKey(parent)) {
                            List<String> sortList = Lists.newArrayList();
                            sortList.add((qidList.indexOf(parent) + 1) + ".1");
                            listLinkedHashMap.put(parent, sortList);
                        } else {
                            List<String> sortList = listLinkedHashMap.get(parent);
                            sortList.add((qidList.indexOf(parent) + 1) + "." + (sortList.size() + 1));
                            listLinkedHashMap.put(parent, sortList);
                        }
                    }
                }
                index += module.getQcount();
                PracticeModuleBean practiceModuleBean = new PracticeModuleBean();
                practiceModuleBean.setId(module.getCategory());
                practiceModuleBean.setName(module.getName());
                List<PracticeSort> practiceSorts = Lists.newArrayList();
                for (Integer qid : listLinkedHashMap.keySet()) {
                    PracticeSort practiceSort = new PracticeSort();
                    practiceSort.setQid(qid);
                    practiceSort.setSort(qidList.indexOf(qid) + 1);
                    StringBuilder sbr = new StringBuilder();
                    if (CollectionUtils.isNotEmpty(listLinkedHashMap.get(qid))) {
                        for (int i = 0; i < listLinkedHashMap.get(qid).size(); i++) {
                            if (i == listLinkedHashMap.get(qid).size() - 1) {
                                sbr.append(listLinkedHashMap.get(qid).get(i));
                                break;
                            }
                            sbr.append(listLinkedHashMap.get(qid).get(i) + "-");
                        }
                    }
                    practiceSort.setName(sbr.toString());
                    practiceSorts.add(practiceSort);
                }
                practiceModuleBean.setPracticeSorts(practiceSorts);
                practiceModuleBeans.add(practiceModuleBean);
            }
        } catch (Throwable e) {
            logger.error("模拟试卷pid:" + paper.getId() + "数据库查询试题出错", e);
        }

        return practiceModuleBeans;
    }

    public List<ViewPracticeQuestion> getQuestionsByPid(int pid) {
//        List<QuestionExtend> questionExtends = questionDao.findExtendByPId(pid);
//        List<ViewPracticeQuestion> viewPracticeQuestions = Lists.newArrayList();
//        if (CollectionUtils.isNotEmpty(questionExtends)) {
//            questionExtends.stream().map(questionExtend -> questionExtend.getQid()).collect(Collectors.toList());
//        }
        return null;
    }

    public List<PracticeQuestion> queryQuestionByKnowledge(String paramStr) {
        Map<String, Object> result = JsonUtil.toMap(paramStr);
        if (MapUtils.isEmpty(result)) {
            return Lists.newArrayList();
        }
        int catgory = Ints.tryParse(String.valueOf(result.get("catgory")));
        int id = Integer.parseInt(String.valueOf(result.get("id")));
        String pointName = String.valueOf(result.get("point"));
        int module = Integer.parseInt(String.valueOf(result.get("module")));
        int difficult = Integer.parseInt(String.valueOf(result.get("difficult")));
        int mode = Integer.parseInt(String.valueOf(result.get("mode")));
        String stem = (String) result.get("stem");
        int questionType = Integer.parseInt(String.valueOf(result.get("questionType")));
        int paperId = Integer.parseInt(String.valueOf(result.get("paperId")));//当前搜索试题时候，模拟卷id
        List<Question> practiceQuestions = Lists.newArrayList();
        List<QuestionExtend> questionExtends = Lists.newArrayList();

        List<Integer> qids = Lists.newArrayList();
        if (id != 0) {
            qids.add(id);
        }
        List<Integer> questionAllIds = getQuestionAllIds(paperId, false);//获取当前试卷中所有试题qid，包括复合题qid
        if (module != 0) {
            //1 选择了模块，先根据module去试题扩展表中查找该模块所有试题qid集合
            questionExtends = practiceDao.findExtendByModule(module);
            List<Integer> qidList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(questionExtends)) {
                for (QuestionExtend questionExtend : questionExtends) {
                    qidList.add(questionExtend.getQid());
                }
            }
            if (CollectionUtils.isEmpty(qids)) {
                qids.addAll(qidList);
            }
            //2 根据其他条件查找试题
            practiceQuestions = practiceDao.findQuestions(catgory, qids, pointName, difficult, mode, stem, questionType);
            //3 将1查找的试题qid集合过滤其他条件查询出来试题对象集合
//            if (CollectionUtils.isNotEmpty(questionList)) {
//                practiceQuestions = questionList.stream().filter(question -> qidList.contains(question.getId())).collect(Collectors.toList());
//            }
            return castPracticeQuestion(practiceQuestions, questionExtends, questionAllIds);
        }
        //4 未选择模块条件，直接根据其他条件进行查找，并返回
        practiceQuestions = practiceDao.findQuestions(catgory, qids, pointName, difficult, mode, stem, questionType);
        if (CollectionUtils.isNotEmpty(practiceQuestions)) {
            List<Integer> questionIds = practiceQuestions.stream().map(question -> question.getId()).collect(Collectors.toList());
            questionExtends = practiceDao.findExtendByQids(questionIds);
        }
        return castPracticeQuestion(practiceQuestions, questionExtends, questionAllIds);
    }

    //按真题试卷进行查找
    public List<PracticeQuestion> queryQuestionByZhenTiPaper(String paramStr) {
        Map<String, Object> result = JsonUtil.toMap(paramStr);
        if (MapUtils.isEmpty(result)) {
            return Lists.newArrayList();
        }
        int catgory = Integer.parseInt(String.valueOf(result.get("catgory")));
        int pid = Integer.parseInt(String.valueOf(result.get("pid")));
        int area = Integer.parseInt(String.valueOf(result.get("area")));
        int module = Integer.parseInt(String.valueOf(result.get("module")));
        int difficult = Integer.parseInt(String.valueOf(result.get("difficult")));
        int year = Integer.parseInt(String.valueOf(result.get("year")));
        String stem = (String) result.get("stem");
        int questionType = Integer.parseInt(String.valueOf(result.get("questionType")));
        int paperId = Integer.parseInt(String.valueOf(result.get("paperId")));//当前搜索试题时候，模拟卷id

        List<Integer> questionAllIds = getQuestionAllIds(paperId, true);//获取当前试卷中所有试题qid，包括复合题qid  true包含复合题子试题qid

        if (pid != 0) {//根据真题试卷中试题进行查找
            //1 通过试卷pid，获取扩展表中该试卷所有相关试题扩展信息
            List<Integer> questionIdList = getQuestionAllIds(pid, true);
            List<QuestionExtend> questionExtends = practiceDao.findExtendByQids(questionIdList);
            //2 此时试题qid集合包含单一试题，复合题，复合题子试题qid
            List<Integer> qidList = questionExtends.stream()
                    .map(questionExtend -> questionExtend.getQid())
                    .collect(Collectors.toList());
            //3 通过试题qid，以及选中题库题型，获取指定试题对象集合
            List<Question> questions = practiceDao.getQuestions(qidList, catgory, area, difficult, year, stem, questionType);
            //4 根据是否选择模块，进行进一步处理
            if (module != 0) {
                questionExtends = questionExtends.stream()
                        .filter(questionExtend -> questionExtend.getModuleId() == module)
                        .collect(Collectors.toList());
            }
            //5 对3中获取的试题对象转换为输出对象
            return castPracticeQuestion(questions, questionExtends, questionAllIds);
        }
        //未选择试卷pid
        List<QuestionExtend> questionExtends = Lists.newArrayList();
        List<Integer> qids = Lists.newArrayList();
        if (module != 0) {
            questionExtends = practiceDao.findExtendByModule(module);
            List<Integer> qidList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(questionExtends)) {
                for (QuestionExtend questionExtend : questionExtends) {
                    qidList.add(questionExtend.getQid());
                }
            }
            qids.addAll(qidList);
            List<Question> questions = practiceDao.getQuestions(qids, catgory, area, difficult, year, stem, questionType);
            return castPracticeQuestion(questions, questionExtends, questionAllIds);
        }
        List<Question> questions = practiceDao.getQuestions(new ArrayList<>(), catgory, area, difficult, year, stem, questionType);
        if (CollectionUtils.isNotEmpty(questions)) {
            List<Integer> questionIds = questions.stream().map(question -> question.getId()).collect(Collectors.toList());
            questionExtends = practiceDao.findExtendByQids(questionIds);
            return castPracticeQuestion(questions, questionExtends, questionAllIds);
        }
        return Lists.newArrayList();
    }

    //将数据库中试题对象根据不同题库类型，进行转换
    private List<PracticeQuestion> castPracticeQuestion(List<Question> practiceQuestions, List<QuestionExtend> questionExtends, List<Integer> questionAllIds) {
        List<PracticeQuestion> practiceQuestionList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(practiceQuestions)) {
            Map<Integer, QuestionExtend> questionExtendMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(questionExtends)) {
                questionExtendMap = questionExtends.stream().collect(Collectors.toMap(QuestionExtend::getQid, (questionExtend) -> questionExtend));
            }
            for (Question question : practiceQuestions) {
                //TODO 正常情况下该试题必须有扩展信息，否则不在页面展示
                if (!questionExtendMap.containsKey(question.getId())) {
                    continue;
                }
                PracticeQuestion practiceQuestion = new PracticeQuestion();
                practiceQuestion.setArea(question.getArea());
                practiceQuestion.setId(question.getId());
                practiceQuestion.setAreaName(AreaConstants.getFullAreaNmae(question.getArea()));
                practiceQuestion.setCreateTime(question.getCreateTime());
                practiceQuestion.setSubject(question.getSubject());
                practiceQuestion.setIsContain(questionAllIds.contains(question.getId()) ? 1 : 0);
                practiceQuestion.setModuleId(questionExtendMap.get(question.getId()) == null ? 17 : questionExtendMap.get(question.getId()).getModuleId());
                if (question instanceof GenericQuestion) {
                    practiceQuestion.setStem(((GenericQuestion) question).getStem());
                    practiceQuestion.setDifficult(((GenericQuestion) question).getDifficult());
                    //暂时TODO试题扩展表数据缺失
                } else if (question instanceof CompositeQuestion) {
                    practiceQuestion.setStem(question.getMaterial());
                    //TODO通过子试题计算
                    //.difficult(compositeQuestion.getDifficult())
                    //暂时TODO试题扩展表数据缺失
                } else if (question instanceof GenericSubjectiveQuestion) {
                    practiceQuestion.setStem(((GenericSubjectiveQuestion) question).getStem());
                    practiceQuestion.setDifficult(((GenericSubjectiveQuestion) question).getDifficult());
                    //暂时TODO试题扩展表数据缺失
                } else if (question instanceof CompositeSubjectiveQuestion) {
                    practiceQuestion.setStem(((CompositeSubjectiveQuestion) question).getMaterials().toString());
                    //TODO通过子试题计算
                    //.difficult(compositeQuestion.getDifficult())
                    //暂时TODO试题扩展表数据缺失
                }
                practiceQuestionList.add(practiceQuestion);
            }
        }
        for (PracticeQuestion practiceQuestion : practiceQuestionList) {
            practiceQuestion.setStem(repairStem(practiceQuestion.getStem(), 30, "..."));
        }
        return practiceQuestionList;
    }

    public List<Paper> queryPaper(int area, int year, int catgory) {
        return practiceDao.queryPaper(area, year, catgory);
    }

    public int copyPaper(int pid, String pname, long uid) {
        //1更新试卷信息
        Paper paper = paperDao.findById(pid);
        paper.setName(pname);
        paper.setCreatedBy((int) uid);
        paper.setCreateTime(new Date());
        paper.setStatus(BackendPaperStatus.CREATED);//复制的试卷是新建状态
        int newPaperId = paperService.generatePaperId();
        paper.setId(newPaperId);
        paperDao.createPaper(paper);
        return newPaperId;
        //TODO 更新试题扩展表信息 模拟试卷中试题是否保存扩展信息
    }

    public void deletePaper(int pid, long uid) {
        Paper paper = paperDao.findById(pid);
        if (paper != null) {
            //审核中试卷如果删除 需要去试卷审核表中修改为拒绝审核，同时备注已被删除
            if (paper.getStatus() == BackendPaperStatus.AUDIT_PENDING) {
                List<PaperCheck> paperCheckList = paperDao.getPaperCheckByStatus(pid, BackendPaperStatus.AUDIT_PENDING);
                if (CollectionUtils.isNotEmpty(paperCheckList)) {
                    PaperCheck paperCheck = paperCheckList.get(0);
                    paperCheck.setCheckStatus(BackendPaperStatus.DELETED);
                    paperCheck.setCheckId(uid);
                    paperCheck.setCheckTime(new Date().getTime());
                    paperCheck.setSuggestion("该试卷被删除");
                    paperDao.updatePaperCheck(paperCheck);
                }
            }
            paper.setStatus(BackendPaperStatus.DELETED);
            paperDao.update(paper);

            if (paper.getType() == PaperType.MATCH) {
                matchDao.updateStatus(paper.getId(), MatchBackendStatus.DELETE);
            }
        }
    }

    public void updatePaper(long uid, PracticePaperBean paperBean) throws BizException {
        Paper paper = paperDao.findById(paperBean.getId());
        if (paper == null) {
            return;
        }
        EstimatePaper estimatePaper = castPracticePaperBean(paperBean);

        if (estimatePaper.getType() == PaperType.MATCH) {
            Match match = matchDao.findById(estimatePaper.getId());
            if (match == null) {
                match = new Match();
                match.setPaperId(paperBean.getId());
                match.setCourseInfo(MATCH_COURSE_INFO);
                match.setInstruction(MATCH_COURSE_INSTRUCTION);
            }
            match.setStatus(paperBean.getStatus());
            match.setCourseId(paperBean.getCourseId());
            match.setStartTime(paperBean.getStartTime());
            match.setEndTime(paperBean.getEndTime());
            match.setName(paperBean.getName());
            String timeInfo = getTimeInfo(match.getStartTime(), match.getEndTime());
            match.setTimeInfo(timeInfo);
            match.setTag(paperBean.getTag());
            match.setSubject(paperBean.getCatgory());
            match.setCourseInfo(paperBean.getCourseInfo());
            match.setInstruction(paperBean.getInstruction());
//            //添加申论内容
//            if (paperBean.getEssayId() != 0) {
//                //试卷状态不发生变动
//                if (paper.getStatus() == 2) {
//                    EssayPractice(paperBean, match, EssayConstant.EssayPracticeType.ONLINE.getType());
//                } else {
//                    EssayPractice(paperBean, match, EssayConstant.EssayPracticeType.CONNECTED.getType());
//                }
//            } else if ((paperBean.getEssayId() == 0 && match.getEssayPaperId() != 0)) {
//                //解绑模考大赛
//                EssayPractice(paperBean, match, EssayConstant.EssayPracticeType.UPDATE.getType());
//            } else {
//                match.setInstructionPC(paperBean.getInstructionPC());
//            }
            long newEssayId = paperBean.getEssayId();
            long oldEssayId = match.getEssayPaperId();

            if(oldEssayId!=newEssayId){
                //绑定模考大赛发生变动
                if(oldEssayId==0&&paper.getStatus() != PaperStatus.AUDIT_SUCCESS){
                    //上线之前可以绑定申论id
                    EssayPractice(paperBean, match, EssayConstant.EssayPracticeType.CONNECTED.getType());
                }else{
                    throw new BizException(ErrorResult.create(10000123,"绑定的申论模考不支持替换和解绑"));
                }
            }else{
                //模考大赛绑定关系没有发生变动
                if(paper.getStatus() == PaperStatus.AUDIT_SUCCESS&&newEssayId!=0){
                    EssayPractice(paperBean, match, EssayConstant.EssayPracticeType.ONLINE.getType());
                }else{
                    match.setInstructionPC(paperBean.getInstructionPC());
                }
            }
            matchDao.save(match);
        }
        paperService.checkPaper(estimatePaper);
        paperDao.update(estimatePaper);
    }

    private void EssayPractice(PracticePaperBean paperBean, Match match, int type) throws BizException {

        match.setEssayPaperId(paperBean.getEssayId());
        match.setInstructionPC(paperBean.getInstructionPC());
        if (StringUtils.isEmpty(paperBean.getInstructionPC())) {
            throw new BizException(ErrorResult.create(1000001,"pc考试说明内容不能为空"));
        }
        Map<String, String> mapData = connectEssay(match, type);
        changeEssayTime(match, mapData);

    }

    static void changeEssayTime(Match match, Map<String, String> mapData) {
        if (mapData != null && mapData.get("endTime") != null && mapData.get("startTime") != null) {
            String endTimeEssay = mapData.get("endTime");
            String startTimeEssay = mapData.get("startTime");
            Date dateStart = null;
            Date dateEnd = null;
            try {
                dateStart = DateUtils.parseDateStrictly(startTimeEssay, "yy-MM-dd HH:mm:ss");
                dateEnd = DateUtils.parseDateStrictly(endTimeEssay, "yy-MM-dd HH:mm:ss");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            match.setTimeInfo(match.getTimeInfo());
            match.setEssayStartTime(dateStart.getTime());
            match.setEssayEndTime(dateEnd.getTime());

        }
    }

    private EstimatePaper castPracticePaperBean(PracticePaperBean paperBean) {
        EstimatePaper estimatePaper = practiceDao.findById(paperBean.getId());

        if (estimatePaper == null) {
            estimatePaper = new EstimatePaper();
            estimatePaper.setCreateTime(paperBean.getCreateTime());
            estimatePaper.setCreatedBy(paperBean.getCreatedBy());
            estimatePaper.setStatus(paperBean.getStatus());
        }
        estimatePaper.setId(paperBean.getId());
        estimatePaper.setName(paperBean.getName());
        estimatePaper.setCatgory(paperBean.getCatgory());
        estimatePaper.setYear(paperBean.getYear());
        estimatePaper.setArea(paperBean.getArea());
        estimatePaper.setScore(paperBean.getScore());
        estimatePaper.setTime(paperBean.getTime() * 60);
        estimatePaper.setLookParseTime(paperBean.getLookParseTime());
        estimatePaper.setStatus(paperBean.getStatus());
        estimatePaper.setHideFlag(paperBean.getHideFlag());

        estimatePaper.setStartTime(truncateTime(paperBean.getStartTime()));
        estimatePaper.setEndTime(truncateTime(paperBean.getEndTime()));

        estimatePaper.setOfflineTime(paperBean.getOfflineTime());
        estimatePaper.setOnlineTime(paperBean.getOnlineTime());
        estimatePaper.setUrl(paperBean.getUrl());
        estimatePaper.setDescrp(paperBean.getDescrp());
        estimatePaper.setType(paperBean.getType());
        return estimatePaper;
    }

    private static long truncateTime(long time) {
        return DateUtils.truncate(new Date(time), Calendar.MINUTE).getTime();
    }

    //模拟试卷直接添加试题  qid 如果是复合题是一个个子试题qid
    public boolean addQuestion2Paper(int pid, int moduleId, int qid) {
        Paper paper = paperDao.findById(pid);
        List<Module> modules = paper.getModules();
        //List<Integer> questions = paper.getQuestions();
        if (CollectionUtils.isEmpty(modules)) {
            return false;
        }
        Map<Integer, List<Integer>> module_qidsMap = new HashMap<>();
        int index = 0;
        if (CollectionUtils.isNotEmpty(paper.getQuestions())) {
            for (Module module : modules) {
                int qcount = module.getQcount();
                List<Integer> qids = paper.getQuestions().subList(index, index + qcount);
                index += qcount;
                module_qidsMap.put(module.getCategory(), new ArrayList<>(qids));
            }
        } else {
            for (Module module : modules) {
                module_qidsMap.put(module.getCategory(), new ArrayList<>());
            }
        }
        //2 根据试题类型不同，添加不同的qid到相应模块
//        Question question = paperQuestionDao.findQuestionById(qid);
//        if (question instanceof GenericQuestion || question instanceof GenericSubjectiveQuestion) {
//            if (module_qidsMap.containsKey(moduleId)) {
//                List<Integer> tmpList = module_qidsMap.get(moduleId);
//                tmpList.add(qid);
//                module_qidsMap.put(moduleId, tmpList);
//            }
//        } else if (question instanceof CompositeQuestion || question instanceof CompositeSubjectiveQuestion) {
//            if (module_qidsMap.containsKey(moduleId)) {
//                List<Integer> tmpList = module_qidsMap.get(moduleId);
//                tmpList.addAll(((CompositeQuestion) question).getQuestions());
//                module_qidsMap.put(moduleId, tmpList);
//            }
//        }
        if (module_qidsMap.containsKey(moduleId)) {
            List<Integer> tmpList = module_qidsMap.get(moduleId);
            tmpList.add(qid);
            module_qidsMap.put(moduleId, tmpList);
        }
        // 3 对试卷中原有试题id集合和模块中试题数量进行修改
        List<Integer> qidList = new ArrayList<>();//更新后新的试题qid集合
        List<Module> modulesList = new ArrayList<>();//更新后新的模块集合
        Map<Integer, Module> moduleMap = modules.stream().collect(Collectors.toMap(Module::getCategory, p -> p));
        for (Module module : modules) {
            List<Integer> tmpList = module_qidsMap.get(module.getCategory());
            qidList.addAll(tmpList);
            if (moduleId == module.getCategory()) {
                module.setQcount(tmpList.size());
            }
            modulesList.add(module);
        }
        // 4  更新试卷信息
        paper.setModules(modulesList);
        paper.setQuestions(qidList);
        paper.setQcount(qidList.size());

        paperQuestionService.updateBigQustionsAndPaper(paper);

        return true;
    }

    //模拟试卷直接从数据库选择试题
    public void addQuestion2Paper(int pid, int moduleId, int qid, String moduleNameStr) {
        Map<Object, Object> result = JsonUtil.toMap(moduleNameStr);
        String moduleName = String.valueOf(result.get("moduleName"));
        Paper paper = paperDao.findById(pid);
        List<Module> modules = paper.getModules();
        List<Integer> questions = paper.getQuestions();
        //1 key moduleId value List<Integer> qids
        Map<Integer, List<Integer>> module_qidsMap = new HashMap<>();
        int index = 0;
        if (CollectionUtils.isNotEmpty(modules) && CollectionUtils.isNotEmpty(questions)) {
            for (Module module : modules) {
                int qcount = module.getQcount();
                List<Integer> qids = paper.getQuestions().subList(index, index + qcount);
                index += qcount;
                module_qidsMap.put(module.getCategory(), new ArrayList<>(qids));
            }
        }
        //2 根据试题类型不同，添加不同的qid到相应模块
        Question question = paperQuestionDao.findQuestionById(qid);
        if (question instanceof GenericQuestion || question instanceof GenericSubjectiveQuestion) {
            if (module_qidsMap.containsKey(moduleId)) {
                List<Integer> tmpList = module_qidsMap.get(moduleId);
                tmpList.add(qid);
                module_qidsMap.put(moduleId, tmpList);
            } else {
                List<Integer> tmpList = new ArrayList<>();
                tmpList.add(qid);
                module_qidsMap.put(moduleId, tmpList);
            }

        } else if (question instanceof CompositeQuestion || question instanceof CompositeSubjectiveQuestion) {
            if (module_qidsMap.containsKey(moduleId)) {
                List<Integer> tmpList = module_qidsMap.get(moduleId);
//                tmpList.addAll(((CompositeQuestion) question).getQuestions());
                tmpList.addAll(getQuestions(question));
                module_qidsMap.put(moduleId, tmpList);
            } else {
                List<Integer> tmpList = new ArrayList<>();
//                tmpList.addAll(((CompositeQuestion) question).getQuestions());
                tmpList.addAll(getQuestions(question));
                module_qidsMap.put(moduleId, tmpList);
            }
        }
        // 3 对试卷中原有试题id集合和模块中试题数量进行修改
        List<Integer> qidList = new ArrayList<>();//更新后新的试题qid集合
        List<Module> modulesList = new ArrayList<>();//更新后新的模块集合
        if (CollectionUtils.isNotEmpty(modules)) {
            Map<Integer, Module> moduleMap = modules.stream().collect(Collectors.toMap(Module::getCategory, p -> p));
            if (!moduleMap.containsKey(moduleId)) {
                for (Module module : modules) {
                    List<Integer> tmpList = module_qidsMap.get(module.getCategory());
                    if (CollectionUtils.isNotEmpty(tmpList)) {
                        qidList.addAll(tmpList);
                    }
                    modulesList.add(module);
                }
                Module module = new Module();
                module.setCategory(moduleId);
                List<Integer> tmpList = module_qidsMap.get(moduleId);
                qidList.addAll(tmpList);
                module.setQcount(tmpList.size());
                module.setName(moduleName);
                modulesList.add(module);
            } else {
                for (Module module : modules) {
                    List<Integer> tmpList = module_qidsMap.get(module.getCategory());
                    qidList.addAll(tmpList);
                    if (moduleId == module.getCategory()) {
                        module.setQcount(tmpList.size());
                    }
                    modulesList.add(module);
                }
            }
        } else {
            Module module = new Module();
            module.setCategory(moduleId);
            qidList = module_qidsMap.get(moduleId);
            module.setQcount(qidList.size());
            module.setName(moduleName);
            modulesList.add(module);
        }

        // 4  更新试卷信息
        paper.setModules(modulesList);
        paper.setQuestions(qidList);
        paper.setQcount(qidList.size());
//        paperDao.update(paper);
        //TODO 是否修改试题扩展表记录

        paperQuestionService.updateBigQustionsAndPaper(paper);
    }

    private Collection<? extends Integer> getQuestions(Question question) {
        if(question instanceof CompositeQuestion){
            return ((CompositeQuestion) question).getQuestions();
        }else if(question instanceof CompositeSubjectiveQuestion){
            return ((CompositeSubjectiveQuestion) question).getQuestions();
        }
        return Lists.newArrayList(question.getId());
    }

    public void deleteQuestion2Paper(int pid, int moduleId, int qid) {
        Paper paper = paperDao.findById(pid);
        if (paper != null) {
            List<Module> modules = paper.getModules();
            List<Integer> questions = paper.getQuestions();
            //1 key 模块id value 该模块对应试题qid集合
            Map<Integer, List<Integer>> module_qidsMap = new HashMap<>();
            int index = 0;
            if (CollectionUtils.isNotEmpty(modules) && CollectionUtils.isNotEmpty(questions)) {
                for (Module module : modules) {
                    int qcount = module.getQcount();
                    List<Integer> qids = paper.getQuestions().subList(index, index + qcount);
                    module_qidsMap.put(module.getCategory(), new ArrayList<>(qids));
                    index += qcount;
                }
            }
            //2 根据试题类型不同，添加不同的qid到相应模块
            if (MapUtils.isNotEmpty(module_qidsMap)) {
                Question question = paperQuestionDao.findQuestionById(qid);
                if (question instanceof GenericQuestion || question instanceof GenericSubjectiveQuestion) {
                    List<Integer> tmpList = module_qidsMap.get(moduleId);
                    tmpList.remove(tmpList.indexOf(qid));
                    module_qidsMap.put(moduleId, tmpList);
                } else if (question instanceof CompositeQuestion || question instanceof CompositeSubjectiveQuestion) {
                    List<Integer> tmpList = module_qidsMap.get(moduleId);
                    tmpList.removeAll(((CompositeQuestion) question).getQuestions());
                    module_qidsMap.put(moduleId, tmpList);
                }
            }
            // 3 对试卷中原有试题id集合和模块中试题数量进行修改
            List<Integer> qidList = new ArrayList<>();//更新后新的试题qid集合
            List<Module> moduleList = new ArrayList<>();//更新后新的模块集合
            if (CollectionUtils.isNotEmpty(modules)) {
                for (Module module : modules) {
                    if (CollectionUtils.isNotEmpty(module_qidsMap.get(module.getCategory()))) {
                        module.setQcount(module_qidsMap.get(module.getCategory()).size());//修改相应模块试题数量
                        qidList.addAll(module_qidsMap.get(module.getCategory()));
                        moduleList.add(module);
                    }
                }
            }
            // 4  更新试卷信息
            paper.setModules(moduleList);
            paper.setQuestions(qidList);
            paper.setQcount(qidList.size());
            paperDao.update(paper);
        }
    }

    private List<Integer> getQuestionAllIds(int paperId, boolean flag) {
        List<Integer> qidList = Lists.newArrayList();
        Paper paper = paperDao.findById(paperId);
        if (paper != null) {
            List<Integer> questions = paper.getQuestions();
            List<Question> questionList = practiceDao.findAllQuestion(questions);
            if (CollectionUtils.isNotEmpty(questionList)) {
                for (Question question : questionList) {
                    if (question instanceof GenericQuestion) {
                        if (((GenericQuestion) question).getParent() == 0) {
                            qidList.add(question.getId());
                        } else {
                            if (flag) {
                                qidList.add(question.getId());
                            }
                            qidList.add(((GenericQuestion) question).getParent());
                        }
                    }
                    if (question instanceof GenericSubjectiveQuestion) {
                        if (((GenericSubjectiveQuestion) question).getParent() == 0) {
                            qidList.add(question.getId());
                        } else {
                            if (flag) {
                                qidList.add(question.getId());
                            }
                            qidList.add(((GenericSubjectiveQuestion) question).getParent());
                        }
                    }
                }
            }
        }
        return qidList;
    }

    //保存模拟试卷调整题序
    public void savePracticePaperSort(String practiceModuleBeansJson, int pid) {
        List<PracticeModuleBean> practiceModuleBeans = JsonUtil.toList(practiceModuleBeansJson, PracticeModuleBean.class);
        if (CollectionUtils.isNotEmpty(practiceModuleBeans)) {
            Paper paper = paperDao.findById(pid);
            Map<Integer, List<Integer>> parentMap = getParentQidList(paper);
            List<Module> modules = Lists.newArrayList();
            List<Integer> questions = Lists.newArrayList();
            for (PracticeModuleBean practiceModuleBean : practiceModuleBeans) {
                String moduleName = practiceModuleBean.getName();
                int moduleId = practiceModuleBean.getId();
                List<Integer> qidList = Lists.newArrayList();
                for (PracticeSort practiceSort : practiceModuleBean.getPracticeSorts()) {
                    int qid = practiceSort.getQid();
                    if (CollectionUtils.isEmpty(parentMap.get(qid))) {
                        qidList.add(qid);
                    } else {
                        qidList.addAll(parentMap.get(qid));
                    }
                }
                Module module = Module.builder()
                        .category(moduleId)
                        .name(moduleName)
                        .qcount(qidList.size())
                        .build();
                modules.add(module);
                questions.addAll(qidList);
            }
            paper.setQcount(questions.size());
            paper.setModules(modules);
            paper.setQuestions(questions);
            paperDao.update(paper);
        }

    }

    //获取试卷中，试题与子试题映射
    private Map<Integer, List<Integer>> getParentQidList(Paper paper) {
        Map<Integer, List<Integer>> parentMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(paper.getQuestions())) {
            List<Question> questionList = paperQuestionDao.findBath(paper.getQuestions());
            for (Question question : questionList) {
                if (question instanceof GenericQuestion) {
                    int parent = ((GenericQuestion) question).getParent();
                    if (parent == 0) {
                        parentMap.put(question.getId(), null);
                        continue;
                    }
                    if (parentMap.containsKey(parent)) {
                        List<Integer> tmpList = parentMap.get(parent);
                        tmpList.add(question.getId());
                        parentMap.put(parent, tmpList);
                    } else {
                        List<Integer> tmpList = Lists.newArrayList();
                        tmpList.add(question.getId());
                        parentMap.put(parent, tmpList);
                    }
                }
            }
        }
        return parentMap;
    }

    public String repairStem(String stem, int length, String extend) {
        if (StringUtils.isNotEmpty(stem)) {
            stem = subStr(filterHtml(filterImg(stem)), length, extend);
        }
        return stem;
    }

    //将题干中<img></img>内容转换为汉字【图片】
    public String filterImg(String str) {
        if (StringUtils.isNotEmpty(str)) {
            str = str.replaceAll("<img.*>.*</img>", "【图片】").replaceAll("<img.*/>", "【图片】").replaceAll("<img.*>", "【图片】");
        }
        return str;
    }

    //将题干中<p></p>标签去除
    public String filterHtml(String str) {
        if (StringUtils.isNotEmpty(str)) {
            str = str.replaceAll("<p>", "").replaceAll("</p>", "");
        }
        return str;
    }

    //截取字符串指定长度 并指定结束符格式
    public String subStr(String str, int length, String extend) {
        if (StringUtils.isNotEmpty(str)) {
            str = str.substring(0, Math.min(length, str.length()));
            if (StringUtils.isNotEmpty(extend)) {
                str = str + extend;
            }
        }
        return str;
    }
}