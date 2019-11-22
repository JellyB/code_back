package com.huatu.ztk.backend.paper.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paper.bean.*;
import com.huatu.ztk.backend.paper.constant.EssayConstant;
import com.huatu.ztk.backend.paper.dao.MatchDao;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.dao.PaperQuestionDao;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.backend.subject.bean.SubjectBean;
import com.huatu.ztk.backend.subject.service.SubjectService;
import com.huatu.ztk.backend.system.bean.Catgory;
import com.huatu.ztk.backend.system.dao.RoleManageDao;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.backend.user.dao.UserDao;
import com.huatu.ztk.backend.util.FunFileUtils;
import com.huatu.ztk.backend.util.FuncStr;
import com.huatu.ztk.backend.util.UploadFileUtil;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.MatchBackendStatus;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.common.QuestionStatus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by ht on 2016/12/21.
 */
@Service
public class PaperService {

    private static final Logger logger = LoggerFactory.getLogger(PaperService.class);

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CreatePaperWordService createPaperWordService;

    @Autowired
    private CreatePaperPdfService createPaperPdfService;

    @Autowired
    private UploadFileUtil uploadFileUtil;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private UserDao userDao;

    @Autowired
    private PaperQuestionDao paperQuestionDao;

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private RoleManageDao roleManageDao;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private MatchDao matchDao;
    @Autowired
    private PracticeService practiceService;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 获取行测试卷
     *
     * @param catgory 学科
     * @param area    地区
     * @param year    年份
     * @param name    试卷名
     * @param type    试卷类型
     * @return
     */
    public List<PaperBean> findAll(String catgory, String area, int year, String name, int type, int uid) throws BizException {
        if (StringUtils.isEmpty(catgory)) {
            return null;
        }

        //查询的科目id列表
        List<Integer> catgoryIds = getCatgoryIds(catgory);

        List<Integer> areas = area.equals("0") ? Lists.newArrayList()
                : Arrays.stream(area.split(","))
                .map(Integer::new)
                .collect(Collectors.toList());

        List<Paper> paperList = paperDao.list(catgoryIds, areas, year, name, Arrays.asList(type),
                Arrays.asList(BackendPaperStatus.DELETED), null, findCreator(catgoryIds, uid));
        List<PaperBean> paperBeanList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(paperList)) {
            return paperBeanList;
        }

        SetOperations setOperations = redisTemplate.opsForSet();
        String key = BackendPaperRedisKeys.recommend_set_key;


        Set<String> members = setOperations.members(key);
        Set<Integer> recommendIds = members.stream().map(Integer::new).collect(Collectors.toSet());

        for (Paper paper : paperList) {
            PaperBean paperBean = shortCastPaper(paper);
            if (recommendIds.contains(paperBean.getId())) {
                paperBean.setRecommend(true);
            }
            paperBeanList.add(paperBean);
        }
        return paperBeanList;
    }

    public List<PaperBean> allDownList(String catgory, String area, int sYear, int eYear) {
        if (StringUtils.isEmpty(catgory)) {
            return null;
        }

        List<Integer> areas = area.equals("0") ? Lists.newArrayList()
                : Arrays.stream(area.split(","))
                .map(Integer::new)
                .collect(Collectors.toList());

        List<Paper> paperList = paperDao.allDownList(getCatgoryIds(catgory), areas, sYear, eYear, null);
        List<PaperBean> paperBeanList = checkoutRecommend(paperList);
        return paperBeanList;
    }

    public List<PaperBean> checkoutRecommend(List<Paper> paperList) {
        List<PaperBean> paperBeanList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(paperList)) {
            return paperBeanList;
        }
        SetOperations setOperations = redisTemplate.opsForSet();
        String key = BackendPaperRedisKeys.recommend_set_key;


        Set<String> members = setOperations.members(key);
        Set<Integer> recommendIds = members.stream().map(Integer::new).collect(Collectors.toSet());

        for (Paper paper : paperList) {
            PaperBean paperBean = shortCastPaper(paper);
            if (recommendIds.contains(paperBean.getId())) {
                paperBean.setRecommend(true);
            }
            paperBeanList.add(paperBean);
        }
        return paperBeanList;
    }


    /**
     * -1表示查询所有创建者
     *
     * @param catgoryIds
     * @param uid
     * @return
     * @throws BizException
     */
    public int findCreator(List<Integer> catgoryIds, int uid) throws BizException {
        //用户可以操作的考试类型
        List<Catgory> userCatgoryList = roleManageDao.findAllCatgoryByUserId(uid);

        //用户的当前的考试类型
        SubjectBean subjectBean = (SubjectBean) subjectService.findById(catgoryIds.get(0));
        int userCatgory = subjectBean.getCatgory();

        Catgory catgoryObj = userCatgoryList.stream().filter(i -> i.getId() == userCatgory).findAny().get();

        //查看权限，1为能查看该考试类型下的所有试卷，0为只能查看自己创建的试卷
        return (catgoryObj.getLookup() == 1) ? -1 : uid;
    }

    /**
     * 创建试卷
     *
     * @param paperBean
     */
    public void createPaper(PaperBean paperBean) throws BizException {
        paperBean.setStatus(BackendPaperStatus.CREATED);
        final int[] area = Arrays.stream(paperBean.getAreas().split(",")).mapToInt(Integer::valueOf).toArray();
        Paper paper = castPaperBean(paperBean);   //转换信息将paperBean转换为paper
        for (int i = 0; i < area.length; i++) {
            paper.setArea(area[i]);
            checkPaper(paper);
            paper.setId(generatePaperId());

            paperDao.createPaper(paper);
        }
    }

    /**
     * 获取试卷详情
     *
     * @param id
     * @return
     */
    public PaperBean findById(int id) throws BizException {
        Paper paper = paperDao.findById(id);
        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        PaperBean paperBean = castPaper(paper);

        SetOperations setOperations = redisTemplate.opsForSet();
        String key = BackendPaperRedisKeys.recommend_set_key;

        paperBean.setRecommend(setOperations.isMember(key, String.valueOf(id)));
        return paperBean;
    }

    /**
     * 修改保存试卷
     *
     * @param paperBean
     */
    public void update(PaperBean paperBean) throws BizException {
        Paper paper = paperDao.findById(paperBean.getId());
        paper.setName(paperBean.getName());
        paper.setArea(paperBean.getArea());
        paper.setCatgory(paperBean.getCatgory());
        paper.setScore(paperBean.getScore());
        paper.setType(paperBean.getType());
        paper.setYear(paperBean.getYear());
        paper.setTime(paperBean.getTime() * 60);


        checkPaper(paper);
        paperDao.update(paper);
    }

    /**
     * 删除试卷
     *
     * @param id
     */
    public void delete(int id) {
        paperDao.updatePaperStatus(id, BackendPaperStatus.DELETED);
    }

    /**
     * 试卷信息转换
     *
     * @param paper
     * @return
     */
    public PaperBean castPaper(Paper paper) {
        List<ModuleBean> moduleBeanList = getModuleBeanList(paper);
        PaperBean paperBean = shortCastPaper(paper);
        paperBean.setModules(moduleBeanList);
        return paperBean;
    }


    public PaperBean shortCastPaper(Paper paper) {
        PaperBean paperBean = PaperBean.builder()
                .id(paper.getId())
                .area(paper.getArea())
                .year(paper.getYear())
                .catgory(paper.getCatgory())
                .createdBy(paper.getCreatedBy())
                .name(paper.getName())
                .areaName(AreaConstants.getFullAreaNmae(paper.getArea()))
                .createTime(paper.getCreateTime())
                .type(paper.getType())
                .score(paper.getScore())
                .status(paper.getStatus())
                .time(paper.getTime() / 60)
                .build();
        return paperBean;
    }


    /**
     * 转换paperBean
     *
     * @param paperBean
     * @return
     */
    private Paper castPaperBean(PaperBean paperBean) {
        Paper paper = new Paper();
        paper.setType(paperBean.getType());
        paper.setCatgory(paperBean.getCatgory());
        paper.setYear(paperBean.getYear());
        paper.setName(paperBean.getName());
        paper.setScore(paperBean.getScore());
        paper.setTime(paperBean.getTime() * 60);
        paper.setCreateTime(new Date());
        paper.setStatus(paperBean.getStatus());
        paper.setCreatedBy(paperBean.getCreatedBy());
        return paper;
    }


    /**
     * 检查参数
     *
     * @param paper
     * @throws BizException
     */
    public void checkPaper(Paper paper) throws BizException {
        if (StringUtils.isBlank(paper.getName())) {
            throw new BizException(ErrorResult.create(1000101, "试卷名称不能为空"));
        }

        if (paper.getScore() <= 0) {
            throw new BizException(ErrorResult.create(1000101, "试卷分数应大于0"));
        }

        if (paper.getTime() <= 0) {
            throw new BizException(ErrorResult.create(1000101, "答题时间应大于0"));
        }

        if (paper.getYear() <= 0
                || paper.getCatgory() <= 0
                || paper.getArea() == 0) {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        if (paper.getType() == PaperType.MATCH) {
            EstimatePaper estimatePaper = (EstimatePaper) paper;

            long endTime = estimatePaper.getEndTime();
            long startTime = estimatePaper.getStartTime();

            int paperTime = estimatePaper.getTime();

            if (!DateUtils.isSameDay(new Date(startTime), new Date(endTime))) {
                throw new BizException(ErrorResult.create(1000101, "模考大赛考试时间不在同一天"));
            }

            if ((endTime - startTime) / 1000 != paperTime) {
                throw new BizException(ErrorResult.create(1000102, "答题时限必须等于考试结束时间和开始时间的差值"));
            }

        }
    }


    /**
     * 重新组装试卷模块
     *
     * @param paper
     * @return
     */
    public List<ModuleBean> getModuleBeanList(Paper paper) {
        List<Module> modules = paper.getModules();
        List<Integer> questions = paper.getQuestions();
        List<ModuleBean> moduleBeans = new ArrayList<>();
        int index = 0;
        int order = 1;  //题序

        if (CollectionUtils.isNotEmpty(modules)) {
            for (Module module : modules) {
                String moduleName = module.getName();
                int id = module.getCategory();

                //模块未添加试题的情况
                if (module.getQcount() == 0) {
                    ModuleBean bean = ModuleBean.builder()
                            .name(moduleName)
                            .id(id)
                            .questions(Maps.newHashMap())
                            .build();
                    moduleBeans.add(bean);
                    continue;
                }

                //题序，题目id map
                Map<Integer, Integer> map = new LinkedHashMap<>();

                List<Integer> qids = questions.subList(index, index + module.getQcount());

                for (Integer qid : qids) {
                    map.put(order++, qid);
                }

                ModuleBean bean = ModuleBean.builder()
                        .name(moduleName)
                        .id(id)
                        .questions(map)
                        .build();
                moduleBeans.add(bean);
                index += module.getQcount();
            }
        }

        return moduleBeans;
    }


    /**
     * 更新审核状态
     *
     * @param id
     */
    public String updateAudit(int id, long userId) throws BizException {

        Paper paper = paperDao.findById(id);

        int oldStaus = paper.getStatus();
        String msg = "";
        int newStatus = oldStaus;
        boolean flag = false;
        Match match = null;
        if (paper.getType() == PaperType.MATCH) {
            match = matchDao.findById(paper.getId());
            //zw 模考试卷已经过了时间的不能操作上下线
            if ((oldStaus == BackendPaperStatus.OFFLINE || oldStaus == BackendPaperStatus.ONLINE) && match != null && System.currentTimeMillis() > match.getEndTime()) {
                logger.error("模考试卷已经过了时间的不能操作上下线，id:{}", id);

                return msg;
            }
            if ((match.getEssayPaperId() != 0 && oldStaus == 7) || (oldStaus == 6 && match.getEssayPaperId() != 0)) {
                Map<String, String> mapData = practiceService.connectEssay(match, EssayConstant.EssayPracticeType.ONLINE.getType());
                PracticeService.changeEssayTime(match, mapData);
            }

            //上线是2  下线是7
            if (paper.getStatus() == 2 && match.getEssayPaperId() != 0) {
                practiceService.connectEssay(match, EssayConstant.EssayPracticeType.OFFLINE.getType());
            }
            flag = true;
        }


        switch (oldStaus) {
            case BackendPaperStatus.CREATED:
            case BackendPaperStatus.AUDIT_REJECT: {
                newStatus = BackendPaperStatus.AUDIT_PENDING;
                PaperCheck paperCheck = PaperCheck.builder()
                        .applyTime(System.currentTimeMillis())
                        .applierId(userId)
                        .checkStatus(newStatus)
                        .paperId(paper.getId())
                        .build();
                paperDao.insertPaperCheck(paperCheck);
                msg = "已提交审核";
                break;
            }

            case BackendPaperStatus.AUDIT_SUCCESS:
            {
                newStatus = BackendPaperStatus.ONLINE;
                checkModule(paper);
//                questionDao.editQuestionsStatusWithApp(paper.getQuestions(),QuestionStatus.AUDIT_SUCCESS);
                setPaperQuestionListStatus(paper.getId(),QuestionStatus.AUDIT_SUCCESS);
                msg = "已上线";
                break;
            }
            case BackendPaperStatus.OFFLINE: {
                newStatus = BackendPaperStatus.ONLINE;
                checkModule(paper);
                setPaperQuestionListStatus(paper.getId(),QuestionStatus.AUDIT_SUCCESS);
//                questionDao.editQuestionsStatusWithApp(paper.getQuestions(),QuestionStatus.AUDIT_SUCCESS);
                msg = "已上线";
                break;
            }

            case BackendPaperStatus.ONLINE: {
                newStatus = BackendPaperStatus.OFFLINE;
                msg = "已下线";
                break;
            }

            case BackendPaperStatus.AUDIT_PENDING: {
                msg = "该试卷正在审核中";
                break;
            }
        }

        paper.setStatus(newStatus);
        paperDao.update(paper);

        if (flag && match != null) {
            match.setStatus(paper.getStatus());
            matchDao.save(match);
        }
        /**
         * update by lijun 当试卷上线时，判断当前是否为新的真题卷 并缓存
         */
        if (msg == "已上线" && paper.getType() == PaperType.TRUE_PAPER) {
            Function<Integer, String> getNewTruePaperKey = (paperId) -> new StringBuilder().append("new:true:paper:").append(paperId).toString();
            Predicate<Date> isOldPaper = (createTime) -> createTime.toInstant().plusSeconds(60 * 60 * 24 * 3).isBefore(Instant.now());
            //缓存当前的真题试卷
            ValueOperations valueOperations = redisTemplate.opsForValue();
            if (redisTemplate.getExpire(getNewTruePaperKey.apply(paper.getId())) > 0
                    || isOldPaper.test(paper.getCreateTime())){
                //原本上线的试题，下线后再次上线
                //过了三天之后，再次下线-上线的题目过滤
                return msg;
            }
            valueOperations.set(getNewTruePaperKey.apply(paper.getId()),"" + paper.getArea(),3,TimeUnit.DAYS);
            /**
             * 区分类别
             */
            final int catgory = paper.getCatgory();
            /**
             * 此处用以更新各个区域版本信息
             * 此处使用zset 避免hash 中的事务操作
             */
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            Function<Integer, String> getAreaCacheVersionKey = (areaId) ->
                    new StringBuilder().append("new:true:paper:area:version:").append(String.valueOf(catgory)).toString();
            zSetOperations.incrementScore(getAreaCacheVersionKey.apply(paper.getArea()),String.valueOf(paper.getArea()),1);
            /**
             * 处理总版本信息
             */
            ValueOperations value = redisTemplate.opsForValue();
            value.increment("new:true:paper:root:version:" + catgory,1);

        }
        return msg;
    }

    private void checkModule(Paper paper) throws BizException {
        if(paper.getModules().stream().anyMatch(i->i.getQcount()==0)){
            throw new BizException(ErrorResult.create(10001,"试卷中存在没有试题的模块"));
        }
    }


    /**
     * 生成试卷id的方法
     *
     * @return
     */
    public int generatePaperId() {
        return paperDao.findMaxId() + 1;
    }

    /**
     * 取消推荐
     *
     * @param id
     */
    public void delRecommend(int id) throws BizException {
        Paper paper = paperDao.findById(id);
        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        SetOperations setOperations = redisTemplate.opsForSet();

        String key = BackendPaperRedisKeys.recommend_set_key;
        String idStr = String.valueOf(id);
        if (setOperations.isMember(key, idStr)) {
            setOperations.remove(key, idStr);
        }
    }


    /**
     * 添加推荐
     *
     * @param id
     */
    public void addRecommend(int id) throws BizException {
        Paper paper = paperDao.findById(id);
        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        SetOperations setOperations = redisTemplate.opsForSet();

        String key = BackendPaperRedisKeys.recommend_set_key;
        String idStr = String.valueOf(id);
        if (!setOperations.isMember(key, idStr)) {

            //检查试卷状态
            boolean available = paper.getStatus() == BackendPaperStatus.AUDIT_SUCCESS
                    || paper.getStatus() == BackendPaperStatus.ONLINE;

            if (!available) {
                throw new BizException(PaperErrors.RECOMMEND_UNAVAILABLE);
            }
            setOperations.add(key, idStr);
        } else {
            throw new BizException(PaperErrors.RECOMMEND_ALREADY);
        }
    }


    /**
     * 删除模块
     *
     * @param id
     * @param mid
     * @throws BizException
     */
    public void delModule(final int id, final int mid) throws BizException {
        Paper paper = paperDao.findById(id);
        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }


        List<Module> modules = paper.getModules();
        List<Integer> questions = paper.getQuestions();


        Module module = modules.stream()
                .filter(m -> m.getCategory() == mid)
                .findFirst()
                .orElse(null);

        if (module == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        List<ModuleBean> moduleBeanList = getModuleBeanList(paper);
        ModuleBean moduleBean = moduleBeanList.stream()
                .filter(mb -> mb.getId() == mid)
                .findFirst()
                .orElse(null);


        //移除该模块下的试题

        if (MapUtils.isNotEmpty(moduleBean.getQuestions())) {
            questions.removeAll(moduleBean.getQuestions().values());
        }

        //移除模块
        modules.remove(module);
        paper.setQcount(paper.getQcount() - module.getQcount());
        paper.setModules(modules);
        paper.setQuestions(questions);

        //更新试卷
//        if (CollectionUtils.isNotEmpty(paper.getQuestions())) {
        paperQuestionService.updateBigQustionsAndPaper(paper);
//        }
    }


    /**
     * 添加模块
     *
     * @param id
     * @param mid
     * @param name
     * @throws BizException
     */
    public void addModule(int id, int mid, String name) throws BizException {
        Paper paper = paperDao.findById(id);

        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        List<Module> modules = paper.getModules();

        //modules为空的情况
        if (CollectionUtils.isEmpty(modules)) {
            modules = Lists.newArrayList();
        }

        //如果已经存在同一个模块id，或模块名称
        if (modules.stream().anyMatch(m -> m.getCategory() == mid || m.getName().equals(name))) {
            throw new BizException(PaperErrors.EXISTS_MODULE);
        }

        Module module = Module.builder()
                .category(mid)
                .name(name)
                .qcount(0)
                .build();
        modules.add(module);

        paper.setModules(modules);
        paperDao.update(paper);
    }

    public List<PaperBean> queryPaperList(PaperBean paperBean, String catgory) throws BizException {
        List<PaperBean> paperBeanList = Lists.newArrayList();
        if (StringUtils.isEmpty(catgory)) {
            return paperBeanList;
        }
        if (paperBean != null) {
            List<Integer> areas = Lists.newArrayList();
            if (!("0").equals(paperBean.getAreas())) {
                areas = Arrays.stream(paperBean.getAreas().split(","))
                        .map(Integer::new)
                        .collect(Collectors.toList());
            }
            List<Integer> ids = Lists.newArrayList();
            if (paperBean.getId() != 0) {
                ids.add(paperBean.getId());
            }
            List<Integer> types = Lists.newArrayList();
            if (paperBean.getType() != 0) {
                types.add(paperBean.getType());

                if (paperBean.getType() == PaperType.CUSTOM_PAPER) {
                    types.add(PaperType.REGULAR_PAPER);
                    types.add(PaperType.MATCH);
                }
            } else {
                types.add(PaperType.TRUE_PAPER);
                types.add(PaperType.REGULAR_PAPER);
                types.add(PaperType.MATCH);
                types.add(PaperType.CUSTOM_PAPER);
            }

            List<Integer> delStatus = Arrays.asList(BackendPaperStatus.DELETED, BackendPaperStatus.CREATED, BackendPaperStatus.ONLINE,
                    BackendPaperStatus.AUDIT_SUCCESS, BackendPaperStatus.AUDIT_REJECT, BackendPaperStatus.OFFLINE);

            List<Integer> catgoryIds = getCatgoryIds(catgory);
            List<Paper> paperList = paperDao.list(catgoryIds, areas, paperBean.getYear(),
                    paperBean.getName(), types, delStatus, ids, findCreator(catgoryIds, paperBean.getCreatedBy()));
            List<Integer> uids = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(paperList)) {
                uids = paperList.stream().map(paper -> paper.getCreatedBy()).collect(Collectors.toList());
                paperBeanList = paperList.stream().map(paper -> castPaper(paper)).collect(Collectors.toList());
                List<User> userList = userDao.findAllById(uids);
                Map<Integer, String> userMap = userList.stream().collect(Collectors.toMap(User::getId, User::getAccount));
                for (PaperBean paperBean1 : paperBeanList) {
                    int uid = new Integer(paperBean1.getCreatedBy());
                    String username = userMap.get(uid);
                    paperBean1.setCreateUser(username);
                }
            }

        }
        return paperBeanList;

    }

    public boolean check(PaperCheck paperCheck) throws BizException {
        boolean success = true;
        try {
            //根据试卷pid获取该试卷相关的待审核状态的记录
            List<PaperCheck> paperChecks = paperDao.getPaperCheckByStatus(paperCheck.getPaperId(), BackendPaperStatus.AUDIT_PENDING);
            if (CollectionUtils.isNotEmpty(paperChecks)) {//存在待审核状态记录
                paperCheck.setId(paperChecks.get(0).getId());
                Paper paper = paperDao.findById(paperCheck.getPaperId());
                if(CollectionUtils.isEmpty(paper.getQuestions())&&paperCheck.getCheckStatus()==BackendPaperStatus.AUDIT_SUCCESS){
                    throw new BizException(ErrorResult.create(10001,"试卷中没有试题"));
                }
                if(paperCheck.getCheckStatus() == BackendPaperStatus.AUDIT_SUCCESS&&paper.getModules().stream().anyMatch(i->i.getQcount()==0)){
                    throw new BizException(ErrorResult.create(10001,"试卷中存在没有试题的模块"));
                }
                boolean flag = paperDao.updatePaperCheck(paperCheck);
                if (flag) {//更新试卷中审核状态完成数据同步
                    paperDao.updatePaperStatus(paperCheck.getPaperId(), paperCheck.getCheckStatus());


                    if (paperCheck.getCheckStatus() == BackendPaperStatus.ONLINE) {
                        //全部试题置为审核通过状态
                        setPaperQuestionListStatus(paperCheck.getPaperId(), QuestionStatus.AUDIT_SUCCESS);

                        if (paper.getType() == PaperType.MATCH) {
                            matchDao.updateStatus(paper.getId(), MatchBackendStatus.AUDIT_SUCCESS);
                        }
                    } else { //审核拒绝,全部试题置为新建状态
                        setPaperQuestionListStatus(paperCheck.getPaperId(), QuestionStatus.CREATED);

                        if (paper.getType() == PaperType.MATCH) {
                            matchDao.updateStatus(paper.getId(), MatchBackendStatus.AUDIT_REJECT);
                        }
                    }
                }
            }
        } catch (BizException e1){
            throw new BizException(e1.getErrorResult());
        } catch (Throwable e) {
            logger.error("paperCheck check fail paperCheck:{}", paperCheck, e);
            success = false;
        }
        return success;
    }

    /**
     * 生成文件
     *
     * @param paperIds
     */
    public void createFile(String paperIds) throws Exception {
        if (StringUtils.isNotEmpty(paperIds)) {
            String[] paperIdArr = paperIds.split(",");
            for (String paperId : paperIdArr) {
                Paper paper = findPaperById(Integer.parseInt(paperId));
                //生成word文件整体版
                String fileDocName = FuncStr.replaceDiagonal(paper.getName()) + ".doc";
                createPaperWordService.createUploadFileWord(paper, ExportType.PAPER_WORD_TYPE_ALL, fileDocName, null);
                //试题版
                String fileDocNameStem = FuncStr.replaceDiagonal(paper.getName()) + "试题.doc";
                createPaperWordService.createUploadFileWord(paper, ExportType.PAPER_WORD_TYPE_SIDE_STEM, fileDocNameStem, null);
                //答案版
                String fileDocNameAnswer = FuncStr.replaceDiagonal(paper.getName()) + "答案.doc";
                createPaperWordService.createUploadFileWord(paper, ExportType.PAPER_WORD_TYPE_SIDE_ANSWER, fileDocNameAnswer, null);

                //生成pdf文件整体版
                String fileName = FuncStr.replaceDiagonal(paper.getName()) + ".pdf";
                createPaperPdfService.createUploadFilePdf(paper, ExportType.PAPER_PDF_TYPE_All, fileName, null);
                //pdf 试题版
                String fileNameStem = FuncStr.replaceDiagonal(paper.getName()) + "试题.pdf";
                createPaperPdfService.createUploadFilePdf(paper, ExportType.PAPER_PDF_TYPE_STEM, fileNameStem, null);
                //pdf答案版
                String filePdfNameAnser = FuncStr.replaceDiagonal(paper.getName()) + "答案.pdf";
                createPaperPdfService.createUploadFilePdf(paper, ExportType.PAPER_PDF_TYPE_ANSER, filePdfNameAnser, null);
            }
        }

    }


    private void setPaperQuestionListStatus(int paperId, int status) {
        List<Integer> qids = paperDao.findById(paperId).getQuestions();
        if (CollectionUtils.isEmpty(qids)) {
            return;
        }

        Set<Integer> totalIds = new HashSet<>(qids);

        List<Question> questions = paperQuestionDao.findBath(qids);

        for (Question question : questions) {
            int parent = 0;
            if (question instanceof GenericQuestion) {
                parent = ((GenericQuestion) question).getParent();
            } else if (question instanceof GenericSubjectiveQuestion) {
                parent = ((GenericSubjectiveQuestion) question).getParent();
            }

            if (parent > 0) {
                totalIds.add(parent);
            }
        }

        if (CollectionUtils.isNotEmpty(qids)) {
            //批量修改试题状态
            questionDao.editQuestionsStatus(totalIds.stream().collect(Collectors.toList()), status);
        }

        paperQuestionDao.findBath(new ArrayList<>(totalIds)).parallelStream().forEach(q -> {
            rabbitTemplate.convertAndSend("","sync_question_update",q);
        });
    }

    //获取指定试卷pid集合，每个试卷最新的审核信息
    public List<PaperCheck> getPaperCheckByPids4Newest(List<Integer> pids) {
        List<PaperCheck> paperChecks = paperDao.getPaperCheckByPids(pids);
        if (CollectionUtils.isNotEmpty(paperChecks)) {
            return paperChecks;
        }
        return Lists.newArrayList();
    }

    /**
     * 真题试卷一体板下载
     *
     * @param ids
     * @param response
     */
    public String allPaperDownLoad(String ids, HttpServletResponse response) throws Exception {
        List<Integer> idLists = Arrays.stream(ids.split(","))
                .map(Integer::new)
                .collect(Collectors.toList());
        List<Paper> paperList = paperDao.allDownList(null, null, 0, 0, idLists);
        if (CollectionUtils.isEmpty(paperList)) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        List<String> nameLists = Lists.newArrayList();
        for (Paper paper : paperList) {// 生成一个一个的word文件
            logger.info("paper={}",paper);
            if (CollectionUtils.isEmpty(paper.getQuestions())) {
                throw new BizException(ErrorResult.create(1000106, "该试卷中不存在试题"));
            }
            nameLists.add(FuncStr.replaceDiagonal(paper.getName()));
            File file = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH + FuncStr.replaceDiagonal(paper.getName()) + ".doc");
            if (!FunFileUtils.fileExists(file)) {
                createPaperWordService.createUploadFileWord(paper, ExportType.PAPER_WORD_TYPE_ALL, file.getName(), file);
            }
        }
        //生成zip并下载
        String zipName = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
        boolean bln = FunFileUtils.unzipFile(zipName, 1, nameLists);
        logger.info("获取压缩包：{}",FunFileUtils.TMP_WORD_SOURCE_FILEPATH + zipName + ".zip");
        File fileZip = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH + zipName + ".zip");
        if (bln) {
            //ftp上传文件  ?  windows 系统 ，上次服务器放开注释
            uploadFileUtil.ftpUploadFile(fileZip, new String(fileZip.getName().getBytes("UTF-8"), "iso-8859-1"), FunFileUtils.WORD_FILE_SAVE_PATH);
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            //下载地址
            return FunFileUtils.WORD_FILE_SAVE_URL + zipName + ".zip";
            //return FunFileUtils.TMP_WORD_SOURCE_FILEPATH + zipName + ".zip";
        } else {
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            throw new BizException(ErrorResult.create(1000107, "下载试卷失败"));
        }

    }

    /**
     * 真题试卷分开版下载
     *
     * @param ids
     * @param response
     */
    public String sidePaperDownLoad(String ids, HttpServletResponse response) throws Exception {
        List<Integer> idLists = Arrays.stream(ids.split(","))
                .map(Integer::new)
                .collect(Collectors.toList());
        List<Paper> paperList = paperDao.allDownList(null, null, 0, 0, idLists);
        if (CollectionUtils.isEmpty(paperList)) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        List<String> nameLists = Lists.newArrayList();
        //创建word文档
        for (Paper paper : paperList) {
            if (CollectionUtils.isEmpty(paper.getQuestions())) {
                throw new BizException(ErrorResult.create(1000106, "该试卷中不存在试题"));
            }
            nameLists.add(FuncStr.replaceDiagonal(paper.getName()));
            //题干版
            File file = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH + FuncStr.replaceDiagonal(paper.getName()) + "试题.doc");
            if (!FunFileUtils.fileExists(file)) {
                createPaperWordService.createUploadFileWord(paper, ExportType.PAPER_WORD_TYPE_SIDE_STEM, file.getName(), file);
            }
            //答案版本
            File fileAnswer = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH + FuncStr.replaceDiagonal(paper.getName()) + "答案" + ".doc");
            if (!FunFileUtils.fileExists(fileAnswer)) {
                createPaperWordService.createUploadFileWord(paper, ExportType.PAPER_WORD_TYPE_SIDE_ANSWER, fileAnswer.getName(), fileAnswer);

            }
        }
        //生成zip包，并下载
        String zipName = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
        //FunFileUtils.fileToZip(zipName);
        boolean bln = FunFileUtils.unzipFile(zipName, 2, nameLists);
        File fileZip = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH + zipName + ".zip");
        if (bln) {
            //ftp上传文件  ?  windows 系统 ，上次服务器放开注释
            uploadFileUtil.ftpUploadFile(fileZip, new String(fileZip.getName().getBytes("UTF-8"), "iso-8859-1"), FunFileUtils.WORD_FILE_SAVE_PATH);
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            //下载地址
            return FunFileUtils.WORD_FILE_SAVE_URL + zipName + ".zip";
            //return FunFileUtils.TMP_WORD_SOURCE_FILEPATH + zipName + ".zip";
        } else {
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            throw new BizException(ErrorResult.create(1000107, "下载试卷失败"));
        }
    }


    public List<Integer> getCatgoryIds(String catgory) {
        List<Integer> catgorys = Arrays.stream(catgory.split(","))
                .map(Integer::new)
                .collect(Collectors.toList());
        return catgorys;
    }

    /**
     * 通过试卷id查试卷详情
     *
     * @param paper
     * @return
     */
    public Object getQuestionByPaperOld(Paper paper) throws BizException {
        List<Integer> totalQids = paper.getQuestions();
        if (CollectionUtils.isEmpty(totalQids)) {
            throw new BizException(ErrorResult.create(1000106, "该试卷中不存在试题"));
        }
        List<ModuleBean> moduleBeanList = getModuleBeanList(paper);
        Map total = new HashMap();
        for (ModuleBean bean : moduleBeanList) {
            if (MapUtils.isNotEmpty(bean.getQuestions())) {
                total.putAll(bean.getQuestions());
            }
        }
        ModuleBean moduleBean = ModuleBean.builder()
                .questions(total)
                .build();
        //题序,试题id map
        Map<Integer, Integer> indexQidMap = moduleBean.getQuestions();

        if (MapUtils.isEmpty(indexQidMap)) {
            return new BizException(ErrorResult.create(1000106, "该试卷中不存在试题"));
        }

        List<PaperQuestionBean> questionList = Lists.newArrayList();
        Map<Integer, Question> questionMap = paperQuestionDao.findBath(indexQidMap.values().stream().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(i -> i.getId(), i -> i));

        //获取拓展表信息，排序按题序
        List<QuestionExtend> extendList = paperQuestionDao.findExtendBath(indexQidMap.values().stream().collect(Collectors.toList()));

        extendList.sort((QuestionExtend q1, QuestionExtend q2) -> (new Float(q1.getSequence())).compareTo(new Float(q2.getSequence())));

        Map<Integer, QuestionExtend> extendMap = extendList.stream().collect(Collectors.toMap(QuestionExtend::getQid, i -> i));

        List<Integer> parentPaperIdList = Lists.newArrayList();

        for (int i = 0; i < extendList.size(); i++) {
            QuestionExtend questionExtend = extendList.get(i);
            Integer qid = questionExtend.getQid();
            Question currentQuestion = questionMap.get(qid);
            if (currentQuestion != null) {
                int parentId = 0;
                float score = 0f;
                if (currentQuestion instanceof GenericQuestion) {
                    GenericQuestion genericQuestion = (GenericQuestion) currentQuestion;
                    parentId = genericQuestion.getParent();
                    score = genericQuestion.getScore();
                } else if (currentQuestion instanceof GenericSubjectiveQuestion) {
                    GenericSubjectiveQuestion subjectiveQuestion = (GenericSubjectiveQuestion) currentQuestion;
                    parentId = subjectiveQuestion.getParent();
                    score = subjectiveQuestion.getScore();
                }
                if (parentId > 0) {// 表示此题为小题
                    if (parentPaperIdList.contains(parentId)) {
                        continue;
                    }
                    parentPaperIdList.add(parentId);
                    //子试题id
                    List<Integer> childrenIds = null;
                    //复合题材料
                    List<String> materials = new ArrayList<>();
                    List<PaperQuestionBean> childrens = new ArrayList<>();
                    Question parent = paperQuestionDao.findQuestionById(parentId);
                    if (parent instanceof CompositeQuestion) {
                        //复合客观题材料
                        CompositeQuestion compositeQuestion = (CompositeQuestion) parent;
                        materials.add(compositeQuestion.getMaterial());
                        childrenIds = compositeQuestion.getQuestions();
                    } else if (parent instanceof CompositeSubjectiveQuestion) {
                        if (CollectionUtils.isNotEmpty(parent.getMaterials())) {
                            //复合主观题，材料
                            materials.addAll(parent.getMaterials());
                        }
                        childrenIds = ((CompositeSubjectiveQuestion) parent).getQuestions();
                    }
                    for (int childId : childrenIds) {
                        Question child = questionMap.get(childId);
                        float childScore = 0f;
                        if (child instanceof GenericQuestion) {
                            childScore = ((GenericQuestion) child).getScore();
                        } else if (child instanceof GenericSubjectiveQuestion) {
                            childScore = ((GenericSubjectiveQuestion) child).getScore();
                        }
                        QuestionExtend childExtend = extendMap.get(childId);
                        PaperQuestionBean childBean = PaperQuestionBean.builder()
                                .tikuType(child instanceof GenericQuestion ?
                                        TikuQuestionType.SINGLE_OBJECTIVE : TikuQuestionType.SINGLE_SUBJECTIVE)
                                .question(child)
                                .index(i)
                                .score(childScore)
                                .extend(childExtend)
                                .build();
                        childrens.add(childBean);
                    }
                    QuestionExtend extend = paperQuestionDao.findExtendById(parentId);
                    PaperQuestionBean bean = PaperQuestionBean.builder()
                            .tikuType(parent instanceof CompositeQuestion ?
                                    TikuQuestionType.MULTI_OBJECTIVE : TikuQuestionType.MULTI_SUBJECTIVE)
                            .question(parent)
                            .extend(extend)
                            .childrens(childrens)
                            .build();
                    questionList.add(bean);
                } else {
                    QuestionExtend extend = extendMap.get(qid);
                    PaperQuestionBean bean = PaperQuestionBean.builder()
                            .tikuType(currentQuestion instanceof GenericQuestion ?
                                    TikuQuestionType.SINGLE_OBJECTIVE : TikuQuestionType.SINGLE_SUBJECTIVE)
                            .question(currentQuestion)
                            .extend(extend)
                            .score(score)
                            .index(i)
                            .childrens(null)
                            .build();
                    questionList.add(bean);
                }
            }
        }
        return questionList;
    }

    /**
     * 通过试卷获取试题详情
     *
     * @param paper
     * @return
     * @throws BizException
     */
    public Object getQuestionByPaper(Paper paper) throws BizException {
        List<Integer> qidsList = paper.getQuestions();
        if (CollectionUtils.isEmpty(qidsList)) {
            throw new BizException(ErrorResult.create(1000106, "该试卷中不存在试题"));
        }
        Map<Integer, Question> questionMap = paperQuestionDao.findBath(qidsList).stream().collect(Collectors.toMap(i -> i.getId(), i -> i));
        //获取拓展表信息，排序按题序
        Map<Integer, QuestionExtend> extendMap = paperQuestionDao.findExtendBath(qidsList).stream().collect(Collectors.toMap(i -> i.getQid(), i -> i));
        List<PaperQuestionBean> questionList = Lists.newArrayList();
        List<Integer> parentPaperIdList = Lists.newArrayList(); //放置试题的复合题的id
        for (Integer qid : qidsList) {
            Question currentQuestion = questionMap.get(qid);
            QuestionExtend currentExtend = extendMap.get(qid);
            if (currentQuestion != null) {
                int parentId = 0;
                float score = 0f;
                if (currentQuestion instanceof GenericQuestion) {
                    GenericQuestion genericQuestion = (GenericQuestion) currentQuestion;
                    parentId = genericQuestion.getParent();
                    score = genericQuestion.getScore();
                } else if (currentQuestion instanceof GenericSubjectiveQuestion) {
                    GenericSubjectiveQuestion subjectiveQuestion = (GenericSubjectiveQuestion) currentQuestion;
                    parentId = subjectiveQuestion.getParent();
                    score = subjectiveQuestion.getScore();
                }
                // 表示此题为小题
                if (parentId > 0) {
                    if (parentPaperIdList.contains(parentId)) {
                        continue;
                    }
                    parentPaperIdList.add(parentId);
                    //子试题id
                    List<Integer> childrenIds = null;
                    //复合题材料
                    List<String> materials = new ArrayList<>();
                    List<PaperQuestionBean> childrens = new ArrayList<>();
                    Question parent = paperQuestionDao.findQuestionById(parentId);
                    if (parent instanceof CompositeQuestion) {
                        //复合客观题材料
                        CompositeQuestion compositeQuestion = (CompositeQuestion) parent;
                        materials.add(compositeQuestion.getMaterial());
                        childrenIds = compositeQuestion.getQuestions();
                    } else if (parent instanceof CompositeSubjectiveQuestion) {
                        if (CollectionUtils.isNotEmpty(parent.getMaterials())) {
                            //复合主观题，材料
                            materials.addAll(parent.getMaterials());
                        }
                        childrenIds = ((CompositeSubjectiveQuestion) parent).getQuestions();
                    }
                    //移除试卷里不存在的子试题id
                    childrenIds.removeIf(i -> !qidsList.contains(i));
                    for (int childId : childrenIds) {
                        Question child = questionMap.get(childId);
                        float childScore = 0f;
                        if (child instanceof GenericQuestion) {
                            childScore = ((GenericQuestion) child).getScore();
                        } else if (child instanceof GenericSubjectiveQuestion) {
                            childScore = ((GenericSubjectiveQuestion) child).getScore();
                        }
                        QuestionExtend childExtend = extendMap.get(childId);
                        PaperQuestionBean childBean = PaperQuestionBean.builder()
                                .tikuType(child instanceof GenericQuestion ?
                                        TikuQuestionType.SINGLE_OBJECTIVE : TikuQuestionType.SINGLE_SUBJECTIVE)
                                .question(child)
                                .extend(childExtend)
                                .score(childScore)
                                .build();
                        childrens.add(childBean);
                    }
                    QuestionExtend extend = paperQuestionDao.findExtendById(parentId);
                    PaperQuestionBean bean = PaperQuestionBean.builder()
                            .tikuType(parent instanceof CompositeQuestion ?
                                    TikuQuestionType.MULTI_OBJECTIVE : TikuQuestionType.MULTI_SUBJECTIVE)
                            .question(parent)
                            .extend(extend)
                            .childrens(childrens)
                            .build();
                    questionList.add(bean);
                } else {
                    QuestionExtend extend = extendMap.get(qid);
                    PaperQuestionBean bean = PaperQuestionBean.builder()
                            .tikuType(currentQuestion instanceof GenericQuestion ?
                                    TikuQuestionType.SINGLE_OBJECTIVE : TikuQuestionType.SINGLE_SUBJECTIVE)
                            .question(currentQuestion)
                            .score(score)
                            .extend(extend)
                            .childrens(null)
                            .build();
                    questionList.add(bean);
                }
            }
        }
        return questionList;
    }

    public Paper findPaperById(int paperId) throws BizException {
        Paper paper = paperDao.findById(paperId);
        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        return paper;
    }

    /**
     * 试卷重置
     *
     * @param paperId
     */
    public void resetPaper(int paperId) {
        paperDao.updatePaperStatus(paperId, BackendPaperStatus.CREATED);

        setPaperQuestionListStatus(paperId, QuestionStatus.CREATED);

    }

    /**
     * 保存模块顺序
     *
     * @param moduleBeanList
     * @param id
     */
    public void saveModulesSort(List<PracticeModuleBean> moduleBeanList, int id) {
        Paper paper = paperDao.findById(id);

        Map<Integer, Module> moduleMap = paper.getModules().stream().collect(Collectors.toMap(i -> i.getCategory(), i -> i));

        List<ModuleBean> beanList = getModuleBeanList(paper);

        Map<Integer, Collection<Integer>> moduleQuestionMap = beanList.stream()
                .collect(Collectors.toMap(i -> i.getId(), i -> i.getQuestions().values()));

        List<Module> newModules = new ArrayList<>();
        List<Integer> newQids = new ArrayList<>();

        for (PracticeModuleBean bean : moduleBeanList) {
            int moduleId = bean.getId();

            newModules.add(moduleMap.get(moduleId));
            newQids.addAll(moduleQuestionMap.get(moduleId));
        }

        paper.setQuestions(newQids);
        paper.setModules(newModules);

        paperQuestionService.updateBigQustionsAndPaper(paper);
    }

    public String findPaperIdByRange(int startId, int endId) throws BizException {
        List<Paper> papers = paperDao.findPaperByRange(startId,endId);
        if(CollectionUtils.isEmpty(papers)){
            throw new BizException(ErrorResult.create(10000011,"没有数据"));
        }
        StringBuilder ids = new StringBuilder();
        for(Paper paper:papers){
            ids.append(paper.getId()).append(",");
        }
        return ids.substring(0,ids.length()-1);
    }


}
