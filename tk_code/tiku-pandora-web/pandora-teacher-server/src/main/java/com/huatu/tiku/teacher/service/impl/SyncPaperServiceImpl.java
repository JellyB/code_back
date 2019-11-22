package com.huatu.tiku.teacher.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.bean.BaseStatusEnum;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.constants.RabbitKeyConstant;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperArea;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.response.paper.SelectModuleResp;
import com.huatu.tiku.response.paper.SelectPaperEntityInfoResp;
import com.huatu.tiku.response.question.v1.SelectQuestionRespV1;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.dao.paper.PaperActivityMapper;
import com.huatu.tiku.teacher.dao.paper.PaperEntityMapper;
import com.huatu.tiku.teacher.enums.ActivityTagEnum;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import com.huatu.tiku.teacher.enums.BizStatusEnum;
import com.huatu.tiku.teacher.enums.StatusEnum;
import com.huatu.tiku.teacher.service.*;
import com.huatu.tiku.teacher.service.common.AreaService;
import com.huatu.tiku.teacher.service.impl.download.v1.WordWriteServiceImplV1;
import com.huatu.tiku.teacher.service.paper.PaperAreaService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.question.DuplicateQuestionService;
import com.huatu.tiku.teacher.service.question.OldQuestionService;
import com.huatu.tiku.teacher.service.question.SyncQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.service.question.v1.QuestionServiceV1;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperStatus;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.GenericSubjectiveQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.ReflectQuestion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\7\6 0006.
 */
@Service
@Slf4j
public class SyncPaperServiceImpl implements SyncPaperService {
    @Autowired
    PaperEntityService paperEntityService;
    @Autowired
    PaperEntityMapper paperEntityMapper;
    @Autowired
    PaperActivityService paperActivityService;
    @Autowired
    OldPaperService oldPaperService;
    @Autowired
    MatchService matchService;
    @Autowired
    AreaService areaService;
    @Autowired
    SyncQuestionService syncQuestionService;
    @Autowired
    PaperQuestionService paperQuestionService;
    @Autowired
    OldQuestionService oldQuestionService;
    @Autowired
    CommonQuestionServiceV1 questionService;
    @Autowired
    ReflectQuestionDao reflectQuestionDao;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PaperAreaService paperAreaService;
    @Autowired
    PaperActivitySubjectService paperActivitySubjectService;
    @Autowired
    PaperActivityMapper paperActivityMapper;
    @Value("${spring.profiles}")
    public String env;
    @Autowired
    DuplicateQuestionService duplicateQuestionService;

    /**
     * 同步模考大赛和真题演练数据到mysql
     *
     * @param id
     */
    @Override
    @Transactional
    public void syncPaper(Integer id) {
        Paper paper = findPaperById(id);
        if (paper == null) {
            throw new BizException(TeacherErrors.NO_EXISTED_PAPER);
        }
        syncPaperSingle(id);
        Long paperId = new Long(id);
        //再同步绑定关系
        //paperEntityService.updatePaperQuestion(paperId,paper.getQuestions().stream().map(i->new Long(i)).collect(Collectors.toList()));
        //绑定实体试卷
        paperActivityService.bindEntityPaperId(new Long(paper.getId()), paperId);
    }

    /**
     * 只迁移试卷及活动数据，试题模块相关数据不迁移
     *
     * @param id
     */
    @Override
    @Transactional
    public void syncPaperSingle(Integer id) {
        PaperEntity paperEntityNew = paperEntityService.selectByPrimaryKey(new Long(id));
        if (paperEntityNew != null) {
            return;
        }
        Paper paper = findPaperById(id);
        if (paper == null) {
            throw new BizException(TeacherErrors.NO_EXISTED_PAPER);
        }
        int type = paper.getType();
        //先同步实体试卷(试卷id自增）
        PaperEntity paperEntity = new PaperEntity();
        paperEntity.setMode(type == PaperType.TRUE_PAPER ? PaperInfoEnum.ModeEnum.TRUE_PAPER.getCode() : PaperInfoEnum.ModeEnum.TEST_PAPER.getCode());
        paperEntity.setName(paper.getName());
        paperEntity.setSubjectId(new Long(paper.getCatgory()));
        paperEntity.setYear(paper.getYear());
        paperEntity.setAreaIds(paper.getArea() + "");
        paperEntity.setLimitTime(paper.getTime() / 60);   //mongo中以秒为单位存储，mysql中以分钟为单位存储
        paperEntity.setTotalScore(new Double(paper.getScore()));
        paperEntity.setId(new Long(paper.getId()));
        paperEntity.setMissFlag(BaseInfo.YESANDNO.NO.getCode());
        int bizStatus = paper.getStatus() == PaperStatus.AUDIT_SUCCESS ? PaperInfoEnum.BizStatus.PUBLISH.getCode() : PaperInfoEnum.BizStatus.NO_PUBLISH.getCode();
        paperEntity.setBizStatus(bizStatus);
        paperEntity.setSpecialFlag(BaseInfo.YESANDNO.NO.getCode());
        /**
         * 物理删除试卷信息
         */
        paperEntityMapper.deleteByPrimaryKey(new Long(id));
        paperEntityService.insertPaper(paperEntity);
        List<Module> modules = paper.getModules();
        List<String> moduleNames = modules.stream().map(i -> {
            String name = "第" + WordWriteServiceImplV1.num_lower[modules.indexOf(i)] + "部分  " + i.getName();
            return name;
        }).collect(Collectors.toList());
        //数据补偿————无模块问题
        List<Integer> counts = paper.getModules().stream().map(i -> i.getQcount()).collect(Collectors.toList());
        int mCount = 0;
        for (Integer count : counts) {
            mCount += count;
        }
        if (paper.getQuestions().size() > mCount) {
            String name = "第" + WordWriteServiceImplV1.num_lower[modules.size()] + "部分  (无模块)";
            moduleNames.add(name);
        }
        paperEntityService.saveModuleInfo(new Long(paper.getId()), moduleNames);

        addPaperActivity(paper, paperEntity);
    }

    /**
     * 根据实体卷和ztk_paper 创建活动试卷(数据迁移和生成实体卷同ID的活动卷专用)
     *
     * @param paper
     * @param paperEntity
     */
    public void addPaperActivity(Paper paper, PaperEntity paperEntity) {
        //接着根据试卷类型，创建活动试卷
        PaperActivity paperActivity = new PaperActivity();
        BeanUtils.copyProperties(paperEntity, paperActivity);
        paperActivity.setPaperId(paperEntity.getId());
        paperActivity.setType(paper.getType());
        paperActivity.setTotalScore(paperEntity.getTotalScore());
        paperActivity.setLimitTime(paperEntity.getLimitTime());
        paperActivity.setSubjectIds(Lists.newArrayList(paperEntity.getSubjectId()));
        //获取地区
        List<PaperArea> areas = paperAreaService.list(paperEntity.getId(), PaperInfoEnum.TypeInfo.ENTITY);
        List<Long> aresIdsList = areas.stream().map(area -> area.getAreaId()).collect(Collectors.toList());
        paperActivity.setAreaIds(aresIdsList);
        paperActivity.setStatus(paper.getStatus() == PaperStatus.DELETED ? StatusEnum.DELETE.getValue() : StatusEnum.NORMAL.getValue());
        paperActivity.setBizStatus(paper.getStatus() == PaperStatus.AUDIT_SUCCESS ? ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_PUBLISH.getKey() : ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_NO_PUBLISH.getKey());
        if (paper instanceof EstimatePaper) {
            paperActivity.setLookParseTime(((EstimatePaper) paper).getLookParseTime());
            paperActivity.setHideFlag(((EstimatePaper) paper).getHideFlag());
            paperActivity.setStartTime(new Timestamp(((EstimatePaper) paper).getStartTime()));
            paperActivity.setEndTime(new Timestamp(((EstimatePaper) paper).getEndTime()));
            paperActivity.setOnlineTime(new Timestamp(((EstimatePaper) paper).getOnlineTime()));
            paperActivity.setOfflineTime(new Timestamp(((EstimatePaper) paper).getOfflineTime()));
        }
        if (paper.getType() == PaperType.MATCH) {
            Match match = matchService.findById(paper.getId());
            int tagCode = match.getTag();
            int subject = match.getSubject();
            try {
                ActivityTagEnum.TagEnum tag = ActivityTagEnum.Subject.getTag(subject, tagCode);
                paperActivity.setTag(tag.getTagId());
            } catch (Exception e) {
                paperActivity.setTag(tagCode);
            }
            paperActivity.setCourseId(new Long(match.getCourseId()));
            paperActivity.setCourseInfo(match.getCourseInfo());
            paperActivity.setInstruction(match.getInstruction());
            paperActivity.setInstructionPC(match.getInstructionPC());
            if (match.getEssayPaperId() > 0) {
                paperActivity.setEssayId(match.getEssayPaperId());
                paperActivity.setEssayStartTime(new Timestamp(match.getEssayStartTime()));
                paperActivity.setEssayEndTime(new Timestamp(match.getEssayEndTime()));
            }
        }
        paperActivityMapper.deleteByPrimaryKey(new Long(paper.getId()));
        paperActivityService.insert(paperActivity);
        long subject = new Long(paperEntity.getSubjectId());
        paperActivitySubjectService.deleteByPaperId(paperActivity.getId());
        paperActivitySubjectService.insertPaperSubject(paperActivity.getId(), Lists.newArrayList(subject));
        long area = new Long(paper.getArea());
        paperAreaService.deletePaperAreaInfo(paperActivity.getPaperId(), PaperInfoEnum.TypeInfo.SIMULATION);
        paperAreaService.savePaperAreaInfo(paperActivity.getId(), Lists.newArrayList(area), PaperInfoEnum.TypeInfo.SIMULATION);

    }

    @Override
    public Paper findPaperById(Integer id) {
        return oldPaperService.findPaperById(id);
    }

    @Override
    public Object findPaperDetail(Integer paperId) {

        //试卷信息处理
        Paper paper = findPaperById(paperId);
        if (null == paper) {
            return null;
        }
        //如果试卷未被迁移，则直接迁移试卷
        PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(new Long(paperId));
        if (paperEntity == null) {
            syncPaperSingle(paperId);
        }
        SelectPaperEntityInfoResp selectPaperEntityInfoResp = new SelectPaperEntityInfoResp();
        selectPaperEntityInfoResp.setPaperId(new Long(paper.getId()));
        selectPaperEntityInfoResp.setPaperName(paper.getName());
        selectPaperEntityInfoResp.setMode(paper.getType() == PaperType.TRUE_PAPER ? 1 : 2);
        selectPaperEntityInfoResp.setYear(paper.getYear());
        selectPaperEntityInfoResp.setAreaIds(Lists.newArrayList(new Long(paper.getArea())));
        List<String> areaNames = areaService.findNameByIds(selectPaperEntityInfoResp.getAreaIds());
        selectPaperEntityInfoResp.setAreaName(areaNames);
        selectPaperEntityInfoResp.setBizStatus(paper.getStatus() == PaperStatus.AUDIT_SUCCESS ? BizStatusEnum.PUBLISH.getValue() : BizStatusEnum.NO_PUBLISH.getValue());
        selectPaperEntityInfoResp.setStatus(paper.getStatus() == PaperStatus.AUDIT_SUCCESS ? "发布" : "未发布");
        selectPaperEntityInfoResp.setQCount(paper.getQcount());
        //模块处理
        List<SelectModuleResp> selectModuleResps = Lists.newArrayList();
        List<Module> modules = paper.getModules();
        List<Integer> questionIds = paper.getQuestions();
        //题序排列
        Map<Integer, Integer> sortMap = Maps.newHashMap();
        for (Integer questionId : questionIds) {
            sortMap.put(questionId, questionIds.indexOf(questionId) + 1);
        }
        List<Question> questions = oldQuestionService.findQuestions(questionIds);
        /**
         * 日志添加
         */
        List<Integer> questionIdList = new ArrayList<>();
        if (questions.size() < questionIds.size()) {
            List<Integer> collect = questions.stream().map(Question::getId).collect(Collectors.toList());
            List<Integer> collect1 = questionIds.stream().filter(i -> !collect.contains(i)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect1)) {
                log.error("试题在mongo中不存在：{}", collect1);
            } else {
                ArrayList<Integer> ids = Lists.newArrayList();
                for (Integer question : questionIds) {
                    if (!ids.contains(question)) {
                        ids.add(question);
                    } else {
                        log.error("试卷{}，存在重复试题:{}", paperId, question);
                        questionIdList.add(question);
                    }
                }
            }
        }
        //保存重复的试题信息
        saveDuplicateQuestionId(paperId, questionIdList);
        log.error("试卷{},存在重复试题{}", paperId, questionIdList);

        /**
         * 获取 - 试题ID - (展示的试题列表中)“已处理”或者“可以被自动迁移处理”的试题
         */
        List<Long> movedIds = getMovedQuestionIds(paper, questions, sortMap);
        /**
         * 抽取某个模块下的试题列表
         * @param ids 试题ID
         * @return 按照题序排序的试题列表
         */
        Function<List<Integer>, List<Question>> getQuestionList = (ids -> {
            List<Question> results = questions.stream().filter(i -> ids.contains(i.getId())).collect(Collectors.toList());
            //按题序排序
            results.sort(Comparator.comparing(i -> sortMap.get(i.getId())));
            return results;
        });
        //按模块批量处理
        int index = 0;
        for (int i = 0; i < modules.size(); i++) {
            SelectModuleResp selectModuleResp = new SelectModuleResp();
            Module module = modules.get(i);
            selectModuleResp.setModuleName(module.getName());
            selectModuleResp.setQcount(module.getQcount());
            selectModuleResp.setSort(i);
            List<Integer> ids = questionIds.subList(index, index + module.getQcount());
            index = index + module.getQcount();
            //试题处理(试题题序调整)
            List<SelectQuestionRespV1> tempQuestions = syncQuestionService.findQuestionByIds(getQuestionList.apply(ids), movedIds, sortMap);
            selectModuleResp.setQuestions(tempQuestions);
            selectModuleResps.add(selectModuleResp);
        }
        selectPaperEntityInfoResp.setModules(selectModuleResps);
        return selectPaperEntityInfoResp;
    }

    /**
     * 保存试卷中重复的试题ID
     *
     * @param paperId
     * @param questionIdList
     */
    public void saveDuplicateQuestionId(Integer paperId, List<Integer> questionIdList) {
      /*  DuplicateQuestion duplicateQuestion = new DuplicateQuestion();
        Example example = new Example(DuplicateQuestion.class);
        example.and().andEqualTo("paperId", paperId);
        List<DuplicateQuestion> duplicateQuestionList = duplicateQuestionService.selectByExample(example);
        if (CollectionUtils.isNotEmpty(duplicateQuestionList)) {
            Long id = duplicateQuestionList.get(0).getId();
            duplicateQuestion.setId(id);
        }
        duplicateQuestion.setPaperId(paperId);
        duplicateQuestion.setQuestionId(JSONObject.toJSONString(questionIdList));
        duplicateQuestionService.save(duplicateQuestion);*/

    }

    /**
     * 对试卷下的试题做处理标识判断，返回已处理的试题ID
     *
     * @param paper     试卷id - mongo
     * @param questions 试题对象 - mongo
     * @param sortMap
     * @return 已被处理的试题 ID
     */
    public List<Long> getMovedQuestionIds(Paper paper, List<Question> questions, final Map<Integer, Integer> sortMap) {
        List<Long> movedIds = Lists.newArrayList();
        int paperId = paper.getId();
        //试题模块归属计算
        Map<Integer, String> moduleMap = Maps.newHashMap();
        List<Integer> ids = paper.getQuestions();
        List<Module> modules = paper.getModules();
        int index = 0;
        for (Module module : modules) {
            List<Integer> tempIds = ids.subList(index, index + module.getQcount());
            String name = "第" + WordWriteServiceImplV1.num_lower[modules.indexOf(module)] + "部分  " + module.getName();
            module.setName(name);
            tempIds.forEach(i -> moduleMap.put(i, name));
            index = index + module.getQcount();
        }
        //情况1（已被处理）：试卷下的试题本身已做迁移
        List<PaperQuestion> paperQuestions = paperQuestionService.findByPaperIdAndType(new Long(paperId), PaperInfoEnum.TypeInfo.ENTITY);
        if (CollectionUtils.isNotEmpty(paperQuestions)) {
            movedIds.addAll(paperQuestions.stream().map(i -> i.getQuestionId()).collect(Collectors.toList()));
        }
        //筛选需要做处理的试题ID(包含复合题ID)
        List<Integer> checkIds = questions.stream().map(i -> i.getId()).distinct().collect(Collectors.toList());
        for (Question question : questions) {
            int parent = getParent(question);
            if (parent > 0 && !checkIds.contains(parent)) {
                checkIds.add(parent);
                //如果子节点（子题）已被处理,则父节点（复合题）认定已被处理
                if (movedIds.contains(question.getId())) {
                    movedIds.add(new Long(parent));
                }
            }
        }
        checkIds.removeIf(i -> movedIds.contains(new Long(i)));
        if (CollectionUtils.isEmpty(checkIds)) {
            return movedIds;
        }
        checkIds = checkIds.stream().distinct().collect(Collectors.toList());
        //情况2（已被处理）：试题本身正常迁移，试卷未绑定
        List<Long> tempIds = checkIds.stream().map(Long::new).collect(Collectors.toList());
        Example example = new Example(BaseQuestion.class);
        example.and().andIn("id", tempIds);
        List<BaseQuestion> baseQuestions = questionService.selectByExample(example);
        if (CollectionUtils.isNotEmpty(baseQuestions)) {
            //TODO 消息队列 - 直接绑定试卷
            for (BaseQuestion baseQuestion : baseQuestions) {
                int questionId = baseQuestion.getId().intValue();
                if (sortMap.get(questionId) != null) {
                    sendMqMsg(questionId, paperId, sortMap.get(questionId), moduleMap.get(questionId), -1);
                }
            }
            //排除掉试题id
            List<Long> questionIds = baseQuestions.stream().map(i -> i.getId()).collect(Collectors.toList());
            tempIds.removeAll(questionIds);
            movedIds.addAll(questionIds);
            checkIds.removeIf(i -> questionIds.contains(new Long(i)));
        }
        if (CollectionUtils.isEmpty(checkIds)) {
            return movedIds;
        }
        //情况3（已被处理）：试题本身被去重迁移，试卷未绑定
        List<ReflectQuestion> reflectQuestions = reflectQuestionDao.findByIds(checkIds);
        if (CollectionUtils.isNotEmpty(reflectQuestions)) {
            //消息队列 - 直接绑定试卷
            for (ReflectQuestion reflectQuestion : reflectQuestions) {
                int questionId = reflectQuestion.getOldId();
                if (sortMap.get(questionId) != null) {
                    sendMqMsg(questionId, paperId, sortMap.get(questionId), moduleMap.get(questionId), -1);
                }
            }
            //排除掉试题id
            List<Integer> questionIds = reflectQuestions.stream().map(i -> i.getOldId()).collect(Collectors.toList());
            movedIds.addAll(questionIds.stream().map(Long::new).collect(Collectors.toList()));
            checkIds.removeAll(questionIds);
        }
        if (CollectionUtils.isEmpty(checkIds)) {
            return movedIds;
        }
        //情况4：未被处理-需要查重
        ConcurrentMap<Integer, Integer> checkMap = Maps.newConcurrentMap();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int id : checkIds) {
            Runnable runnable = () -> {
                try {
                    Question question = oldQuestionService.findQuestion(id);
                    Integer type = question.getType();
                    QuestionServiceV1 questionServiceV1 = questionService.choiceService(QuestionInfoEnum.getSaveTypeByQuestionType(type));
                    Object duplicateQuestion = questionServiceV1.findDuplicateQuestion(question, BaseInfo.YESANDNO.YES.getCode());
                    if (duplicateQuestion == null) {
                        //无重复数据，可以正常迁移的ID
                        checkMap.put(id, 1);
                    } else if (duplicateQuestion instanceof List && CollectionUtils.isEmpty((Collection) duplicateQuestion)) {
                        //查重结果是空，可以正常迁移的ID
                        checkMap.put(id, 1);
                    } else {
                        checkMap.put(id, 0);
                    }
                } catch (Exception e) {
                    checkMap.put(id, 0);
                    log.info("question findDuplicateQuestion error,id={} ", id);
                    e.printStackTrace();
                }
            };
            executor.execute(runnable);
        }
        while (checkMap.size() != checkIds.size()) {
            try {
                Thread.currentThread().sleep(100);
                log.info("进度={}/{}", checkMap.size(), checkIds.size());
                log.info("finishedIds = {},checkIds={}", checkMap.keySet(), checkIds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        //question判断-- 子题如果要迁移，还要保证其复合题可以迁移
        Map<Integer, Question> questionMap = Maps.newHashMap();
        for (Question question : questions) {
            questionMap.put(question.getId(), question);
        }
        List<Integer> movingIds = Lists.newArrayList(); //需要做正常迁移的试题ID
        for (Map.Entry<Integer, Integer> entry : checkMap.entrySet()) {
            //无重题的试题，可以直接迁移（子题需要做进一步判断）
            if (entry.getValue() == 1) {
                Integer questionId = entry.getKey();
                Question question = questionMap.get(questionId);
                int parent = getParent(question);
                if (parent > 0) {
                    /**
                     * 如果父节点 未被迁移过 且 不可迁移（有重复数据），则子题不做处理
                     * !movedIds.contains(new Long(parent) 表示未被迁移
                     * checkMap.getOrDefault(parent,1) 的值为1表示可以被处理或者已经被处理，0表示不能被处理的
                     */
                    if (!movedIds.contains(new Long(parent)) && checkMap.getOrDefault(parent, 1) == 0) {
                        continue;
                    }
                }
                movingIds.add(questionId);
            }
        }
        for (Integer movingId : movingIds) {
            sendMqMsg(movingId, paperId, sortMap.getOrDefault(movingId, -1), moduleMap.getOrDefault(movingId, ""), 1);
        }
        movedIds.addAll(movingIds.stream().map(Long::new).collect(Collectors.toList()));
        return movedIds;
    }

    /**
     * 查询试题的父节点ID
     *
     * @param question
     * @return
     */
    private int getParent(Question question) {
        int parent = 0;
        if (question instanceof GenericSubjectiveQuestion) {
            parent = ((GenericSubjectiveQuestion) question).getParent();
        } else if (question instanceof GenericQuestion) {
            parent = ((GenericQuestion) question).getParent();
        }
        return parent;
    }

    /**
     * 发送迁移数据请求
     *
     * @param questionId
     * @param paperId
     * @param sort
     * @param moduleName 第N部分  ****
     * @param flag       -1 只绑定，1迁移+绑定
     */
    private void sendMqMsg(int questionId, int paperId, int sort, String moduleName, int flag) {
        Map map = Maps.newHashMap();
        map.put("questionId", questionId);
        map.put("paperId", paperId);
        map.put("sort", sort);
        map.put("moduleName", moduleName);
        map.put("flag", flag);
        rabbitTemplate.convertAndSend("", RabbitKeyConstant.getQuestion_2_mysql(env), map);
    }

    /**
     * 根据实体卷ID创建活动信息
     *
     * @param paperId
     */
    @Override
    @Transactional
    public void createActivityByPaperId(Long paperId) {
        log.info("处理实体卷ID：{}", paperId);
        PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperId);
        if (null == paperEntity) {
            return;
        }
        //填充活动信息
        Paper paper = findPaperById(paperId.intValue());
        if (null == paper) {
            return;
        }
        List<Long> paperActivityId = paperActivityService.findByPaperId(paperId);
        if (CollectionUtils.isNotEmpty(paperActivityId) && paperActivityId.contains(paperId)) {
            return;
        }
        //开始正式生成活动卷数据
        //只处理真题演练
        switch (paper.getType()) {
            case PaperType.TRUE_PAPER:
                log.info("符合处理条件ID:{}", paperId);
                addPaperActivity(paper, paperEntity);
                break;
            case PaperType.MATCH:
                log.info("符合处理条件ID:{}", paperId);
                addPaperActivity(paper, paperEntity);
                break;
            default:
                log.error("其他类型试卷不做处理，paperId={},type={}", paperId, paper.getType());
        }
    }


    public void syncPaperEntityToPaperActivity() {
        Long subject = 4L; //职测
        int activityType = 1;//真题卷

        //1.查询paperEntity,科目是职测
        Example example = new Example(PaperEntity.class);
        example.and().andEqualTo("subjectId", subject);
        List<PaperEntity> paperEntityList = paperEntityService.selectByExample(example);
        log.info("需要同步的试题数量是:{}", paperEntityList.size());
        List<Long> activityIds = new ArrayList<>();
        if (CollectionUtils.isEmpty(paperEntityList)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(paperEntityList)) {
            paperEntityList.forEach(paperEntity -> {
                //2.写入到paperActivity中,(全部是未发布状态)
                PaperActivity paperActivity = new PaperActivity();
                paperActivity.setName(paperEntity.getName());
                paperActivity.setType(ActivityTypeAndStatus.ActivityTypeEnum.TRUE_PAPER.getKey());
                paperActivity.setPaperId(paperEntity.getId());
                paperActivity.setCreatorId(0L);
                paperActivity.setYear(paperEntity.getYear());
                paperActivity.setTotalScore(paperEntity.getTotalScore());
                paperActivity.setLimitTime(paperEntity.getLimitTime());
                paperActivity.setBizStatus(ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_NO_PUBLISH.getKey());
                //获取当前时间
                paperActivity.setGmtCreate(new Timestamp(System.currentTimeMillis()));
                paperActivity.setStatus(BaseStatusEnum.NORMAL.getCode());
                paperActivity.setScoreFlag(0);
               // paperActivityMapper.deleteByPrimaryKey(new Long(paperEntity.getId()));
                paperActivityService.insert(paperActivity);
                activityIds.add(paperActivity.getId());
                //3.科目写入paper_activity_subject
                long subjectNewId = new Long(paperEntity.getSubjectId());
                //paperActivitySubjectService.deleteByPaperId(paperActivity.getId());
                paperActivitySubjectService.insertPaperSubject(paperActivity.getId(), Lists.newArrayList(subjectNewId));
                //写入地区表 paper_area
                List<PaperArea> paperAreaList = paperAreaService.list(paperEntity.getId(), PaperInfoEnum.TypeInfo.ENTITY);
                List<Long> paperAresIds = paperAreaList.stream().map(PaperArea::getAreaId).collect(Collectors.toList());
               // paperAreaService.deletePaperAreaInfo(paperActivity.getPaperId(), PaperInfoEnum.TypeInfo.SIMULATION);
                paperAreaService.savePaperAreaInfo(paperActivity.getId(), paperAresIds, PaperInfoEnum.TypeInfo.SIMULATION);
            });
        }
        log.info("同步试卷总数是:{},试卷ID是:{}", activityIds.size(), activityIds);

    }

}