package com.huatu.tiku.teacher.service.impl.download.v1;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.entity.download.BaseTool;
import com.huatu.tiku.entity.teacher.*;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.dao.mongo.OldPaperDao;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.common.QuestionMetaService;
import com.huatu.tiku.teacher.service.download.v1.DownloadWriteServiceV1;
import com.huatu.tiku.teacher.service.impl.paper.PaperModuleHandler;
import com.huatu.tiku.teacher.service.paper.PaperAssemblyQuestionService;
import com.huatu.tiku.teacher.service.paper.PaperAssemblyService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.util.file.PDFHeaderFooter;
import com.huatu.tiku.teacher.util.image.ImageUtil;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.question.bean.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.math.raw.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/8/15.
 */
@Slf4j
@Service
public class DownloadWriteServiceImplV1 implements DownloadWriteServiceV1 {

    @Autowired
    PaperAssemblyService paperAssemblyService;

    @Autowired
    PaperAssemblyQuestionService paperAssemblyQuestionService;

    @Autowired
    PaperEntityService paperEntityService;

    @Autowired
    PaperActivityService paperActivityService;

    @Autowired
    PaperQuestionService paperQuestionService;

    @Autowired
    NewQuestionDao questionDao;

    @Autowired
    QuestionMetaService questionMetaService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    OldPaperDao paperDao;

    private static final Cache<Integer, Question> QUESTION_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    /**
     * 将PaperQuestionSimpleInfo中有用字段转化存储到hashMap中
     *
     * @param question
     * @param sort
     * @return
     */
    @Override
    public Map<String, Object> transMapData(Question question, Integer sort) {
        HashMap<String, Object> questionMap = Maps.newHashMap();
        int type = question.getType();
        if (sort < 0) {
            type = QuestionInfoEnum.QuestionTypeEnum.COMPOSITE.getCode();
        }
        QuestionInfoEnum.QuestionSaveTypeEnum saveTypeEnum = QuestionInfoEnum.getSaveTypeByQuestionType(type);
        QuestionElementEnum.QuestionOperateEnum questionOperateEnum = QuestionElementEnum.QuestionOperateEnum.create(saveTypeEnum);
        List<QuestionElementEnum.ElementEnum> value = questionOperateEnum.getValue();
        //题型属性获取（题型是必需字段，无论展不展示）
        if (sort > 0) {
            questionMap.put(QuestionElementEnum.ElementEnum.TYPE.getKey(), question.getType());
        } else {
            questionMap.put(QuestionElementEnum.ElementEnum.TYPE.getKey(), QuestionInfoEnum.QuestionTypeEnum.COMPOSITE.getCode());
        }
        for (QuestionElementEnum.ElementEnum elementEnum : value) {
            switch (elementEnum) {
                case SORT:
                    questionMap.put(elementEnum.getKey(), sort);
                    break;
                case STEM:
                    if (question instanceof GenericQuestion) {
                        questionMap.put(elementEnum.getKey(), ((GenericQuestion) question).getStem());
                    } else if (question instanceof GenericSubjectiveQuestion) {
                        questionMap.put(elementEnum.getKey(), ((GenericSubjectiveQuestion) question).getStem());
                    }
                    break;
                case TYPE:
                    //题型在遍历属性之前已经获取
                    break;
                case ANSWER:
                    if (question instanceof GenericQuestion) {
                        int answer = ((GenericQuestion) question).getAnswer();
                        questionMap.put(elementEnum.getKey(), HtmlConvertUtil.getAnswer(String.valueOf(answer)));
                    }
                    break;
                case CHOICE:
                    if (question instanceof GenericQuestion) {
                        questionMap.put(elementEnum.getKey(), HtmlConvertUtil.assertChoicesContent(((GenericQuestion) question).getChoices()));
                    }
                    break;
                case ANALYSIS:
                    if (question instanceof GenericQuestion) {
                        questionMap.put(elementEnum.getKey(), ((GenericQuestion) question).getAnalysis());
                    } else if (question instanceof GenericSubjectiveQuestion) {
                        questionMap.put(elementEnum.getKey(), ((GenericSubjectiveQuestion) question).getReferAnalysis());
                    }
                    break;
                case EXTEND:
                    if (question instanceof GenericQuestion) {
                        questionMap.put(elementEnum.getKey(), ((GenericQuestion) question).getExtend());
                    }
                    break;
                case SOURCE:
                    questionMap.put(elementEnum.getKey(), question.getFrom());
                    break;
                case MATERIAL:
                    List<String> materialContent = question.getMaterials();
                    //材料通过"【资料】"字符串隔开
                    questionMap.put(elementEnum.getKey(), StringUtils.join(materialContent, elementEnum.getName()));
                    break;
                case KNOWLEDGE:
                    List<KnowledgeInfo> pointList = question.getPointList();
                    if (CollectionUtils.isNotEmpty(pointList)) {
                        List<String> names = pointList.stream().map(i -> StringUtils.join(i.getPointsName(), "-")).collect(Collectors.toList());
                        questionMap.put(elementEnum.getKey(), StringUtils.join(names, ","));
                    } else if (question instanceof GenericQuestion) {
                        questionMap.put(elementEnum.getKey(), StringUtils.join(((GenericQuestion) question).getPointsName(), "-"));
                    }
                    break;
                case QUESTION_ID:
                    if (sort > 0) {
                        questionMap.put(elementEnum.getKey(), question.getId());
                    } else {
                        if (question instanceof GenericQuestion) {
                            questionMap.put(elementEnum.getKey(), ((GenericQuestion) question).getParent());
                        } else if (question instanceof GenericSubjectiveQuestion) {
                            questionMap.put(elementEnum.getKey(), ((GenericSubjectiveQuestion) question).getParent());
                        }
                    }
                    break;
                case ANSWER_COMMENT:
                    if (question instanceof GenericSubjectiveQuestion) {
                        questionMap.put(elementEnum.getKey(), ((GenericSubjectiveQuestion) question).getReferAnalysis());
                    }
                    break;
                case ACCURACY:
                    //只有客观题才会有正确率字段
                    QuestionMeta questionMeta = findQuestionMeta(question);
                    if (null != questionMeta) {
                        int[] percents = questionMeta.getPercents();
                        questionMap.put(elementEnum.getKey(), percents[questionMeta.getRindex()] + "%");
                    }
                    break;
                case TRAIN_TIME:
                    QuestionMeta meta = findQuestionMeta(question);
                    if (null != meta) {
                        questionMap.put(elementEnum.getKey(), meta.getCount() + "次");
                    }
                    break;
            }
        }
        return questionMap;
    }

    /**
     * 查询试题统计数据
     *
     * @param question
     * @return
     */
    private QuestionMeta findQuestionMeta(Question question) {
        if (question instanceof GenericQuestion) {
            QuestionMeta meta = ((GenericQuestion) question).getMeta();
            if (null != meta) {
                return meta;
            } else {
                QuestionMeta meta1 = questionMetaService.findMeta((GenericQuestion) question);
                ((GenericQuestion) question).setMeta(meta1);
                return meta1;
            }
        }
        return null;
    }


    @Override
    public String makeWordByPaper(long paperId, BaseTool baseTool, BiFunction<BaseTool, String, BaseTool> initWriteTool, BiConsumer<BaseTool, String> titleWrite, BiConsumer<BaseTool, PaperModuleInfo> moduleWrite, BiConsumer<BaseTool, Map<String, Object>> questionWrite) {
        StopWatch stopWatch = new StopWatch("makeWordByPaper:"+paperId);
        String name = "";
        String moduleInfo = "";
        int subject = -1;
        List<PaperQuestion> paperQuestionList = Lists.newArrayList();
        List<PaperAssemblyQuestion> paperAssemblyQuestions = Lists.newArrayList();
        PaperInfoEnum.TypeInfo typeInfo = baseTool.getTypeInfo();
        if (typeInfo.equals(PaperInfoEnum.TypeInfo.ENTITY)) {
            PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperId);
            if (null != paperEntity) {
                name = paperEntity.getName();
                moduleInfo = paperEntity.getModule();
                paperQuestionList.addAll(paperQuestionService.findByPaperIdAndType(paperId, typeInfo));
                subject = null==paperEntity.getSubjectId()?-1:paperEntity.getSubjectId().intValue();
            }
        } else if (typeInfo.equals(PaperInfoEnum.TypeInfo.SIMULATION)) {
            PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(paperId);
            if (null != paperActivity) {
                Paper paper = paperDao.findById(new Long(paperId).intValue());
                subject = null==paper?-1:paper.getCatgory();
                name = paperActivity.getName();
                if (null == paperActivity.getPaperId() || paperActivity.getPaperId().intValue() <= 0) {
                    moduleInfo = paperActivity.getModule();
                    paperQuestionList.addAll(paperQuestionService.findByPaperIdAndType(paperId, typeInfo));
                } else {
                    Long paperEntityId = paperActivity.getPaperId();
                    PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperEntityId);
                    if (null != paperEntity) {
                        moduleInfo = paperEntity.getModule();
                        paperQuestionList.addAll(paperQuestionService.findByPaperIdAndType(paperEntityId, PaperInfoEnum.TypeInfo.ENTITY));
                    }
                }
            }
        } else {
            stopWatch.start("getPaperInfo.getPaperAssembly");
            PaperAssembly paperAssembly = paperAssemblyService.selectByPrimaryKey(paperId);
            subject = null==paperAssembly.getSubjectId()?-1:paperAssembly.getSubjectId().intValue();
            stopWatch.stop();
            if (null != paperAssembly) {
                stopWatch.start("getPaperInfo.getpaperAssemblyQuestions");
                name = paperAssembly.getName();
                Example example = new Example(PaperAssemblyQuestion.class);
                example.and().andEqualTo("paperId", paperId);
                paperAssemblyQuestions.addAll(paperAssemblyQuestionService.selectByExample(example));
                stopWatch.stop();
            }
        }
        if ("".equals(name)) {
            throw new BizException(TeacherErrors.NO_EXISTED_PAPER);
        }
        name = name.trim();
        baseTool.setSubject(subject);
        BaseTool tool = initWriteTool.apply(baseTool, name);
        titleWrite.accept(tool, name);

        //真题卷和模拟卷部分数据写入word
        if (typeInfo.equals(PaperInfoEnum.TypeInfo.ENTITY) || typeInfo.equals(PaperInfoEnum.TypeInfo.SIMULATION)) {
            if (CollectionUtils.isEmpty(paperQuestionList)) {
                log.error("试卷下无题，paperId={},paperType={}", paperId, typeInfo.getName());
                throw new BizException(ErrorResult.create(1000001, "无绑定试题"));
            }
            Map<Integer, List<PaperQuestion>> moduleMap = paperQuestionList.stream().collect(Collectors.groupingBy(PaperQuestion::getModuleId));
            List<PaperModuleInfo> paperModuleInfos = PaperModuleHandler.analysisModuleStr(moduleInfo);
            List<Integer> questionIds = paperQuestionList.stream().map(PaperQuestion::getQuestionId).map(Long::intValue).collect(Collectors.toList());
            List<Question> questions = questionDao.findByIds(questionIds);
            Map<Integer, Question> questionMap = Maps.newHashMap();
            for (Question question : questions) {
                questionMap.put(question.getId(), question);
            }
            String join = StringUtils.join(questions.stream().map(JsonUtil::toJson).collect(Collectors.toList()));
            ImageUtil.save(new StringBuilder(join));
            boolean flag = false;   //指定模块是否存在
            int realSort = 0;       //按照试题写入的顺序重新排序
            for (PaperModuleInfo module : paperModuleInfos) {
                if (StringUtils.isNotBlank(baseTool.getModuleName()) && module.getName().indexOf(baseTool.getModuleName()) == -1) {
                    System.out.println("module.getName() = " + module.getName() + ">>>>>> " + baseTool.getModuleName());
                    continue;
                }
                flag = true;
                List<PaperQuestion> paperQuestions = moduleMap.get(module.getId());
                //写入模块信息
                moduleWrite.accept(tool, module);
                if (CollectionUtils.isEmpty(paperQuestions)) {
                    continue;
                }
                paperQuestions.sort(Comparator.comparing(PaperQuestion::getSort));
                int parentId = 0;
                for (PaperQuestion paperQuestion : paperQuestions) {
                    Question question = questionMap.get(paperQuestion.getQuestionId().intValue());
                    if(null == question){           //试卷为同步到mongo库中
                        log.error("question is null, id = {}",paperQuestion.getQuestionId());
                        continue;
                    }
                    //检查试题科目，如果科目为特定科目，则下载的内容没有尾页二维码
                    checkSubject(question, baseTool);
                    if (baseTool.isDuplicateFlag()) {
                        SetOperations setOperations = redisTemplate.opsForSet();
                        String key = "duplicate_set";
                        Boolean member = setOperations.isMember(key, question.getId() + "");
                        if (member) {
                            continue;
                        }
                        setOperations.add(key, question.getId() + "");
                        redisTemplate.expire(key, 1, TimeUnit.HOURS);
                    }
                    Integer apply = getParentFunction().apply(question);
                    realSort++;
                    if (parentId != apply.intValue()) {
                        addQuestionElement(tool, question, true, realSort, questionWrite);
                        parentId = apply;
                    } else {
                        addQuestionElement(tool, question, false, realSort, questionWrite);
                    }
                }
            }
            if (!flag) {
                log.error("指定模块不存在:paperName = {},module = {}", name, baseTool.getModuleName());
                throw new BizException(ErrorResult.create(1000010, "指定模块不存在"));
            }
        } else {      //手工组卷部分数据写入
            stopWatch.start("questionInfo");
            List<Integer> questionIds = paperAssemblyQuestions.stream().map(PaperAssemblyQuestion::getQuestionId).map(Long::intValue).collect(Collectors.toList());
            List<Question> questions = questionDao.findByIds(questionIds);
            Map<Integer, Question> questionMap = questions.stream().collect(Collectors.toMap(i -> i.getId(), i -> i));
            String join = StringUtils.join(questions.stream().map(JsonUtil::toJson).collect(Collectors.toList()));
            ImageUtil.save(new StringBuilder(join));
            paperAssemblyQuestions.sort(Comparator.comparing(PaperAssemblyQuestion::getSort));
            stopWatch.stop();
            int parentId = 0;
            for (PaperAssemblyQuestion paperAssemblyQuestion : paperAssemblyQuestions) {
                Question question = questionMap.get(paperAssemblyQuestion.getQuestionId().intValue());
                if (null == question) {
                    continue;
                }
                Integer apply = getParentFunction().apply(question);
                if (parentId != apply.intValue()) {
                    stopWatch.start("addQuestionElement:"+question.getId());
                    addQuestionElement(tool, question, true, paperAssemblyQuestion.getSort(), questionWrite);
                    stopWatch.stop();
                    parentId = apply;
                } else {
                    stopWatch.start("addQuestionElement:"+question.getId());
                    addQuestionElement(tool, question, false, paperAssemblyQuestion.getSort(), questionWrite);
                    stopWatch.stop();
                }
            }
        }
        log.info("StopWatch makeWordByPaper:{}",stopWatch.prettyPrint());
        return tool.getFile().getName();
    }

    @Override
    public List<String> getModuleNames(List<Long> ids, PaperInfoEnum.TypeInfo typeInfo) {
        Function<List<String>, List<String>> getModuleNames = (moduleInfos -> {
            Set<String> names = Sets.newHashSet();
            for (String info : moduleInfos) {
                List<PaperModuleInfo> paperModuleInfos = PaperModuleHandler.analysisModuleStr(info);
                names.addAll(paperModuleInfos.stream().map(PaperModuleInfo::getName).collect(Collectors.toSet()));
            }
            return names.stream().map(name -> {
                name = name.trim();
                String[] split = name.split(" ");
                        if (split.length > 2) {
                            return split[split.length-1];
                        }
                        return name;
                    }
            ).collect(Collectors.toList());
        });
        if (typeInfo.equals(PaperInfoEnum.TypeInfo.ENTITY)) {
            Example example = new Example(PaperEntity.class);
            example.and().andIn("id", ids);
            List<PaperEntity> paperEntities = paperEntityService.selectByExample(example);
            if (CollectionUtils.isNotEmpty(paperEntities)) {
                return getModuleNames.apply(paperEntities.stream().map(PaperEntity::getModule).collect(Collectors.toList()));
            }
        } else if (typeInfo.equals(PaperInfoEnum.TypeInfo.SIMULATION)) {
            Example example = new Example(PaperActivity.class);
            example.and().andIn("id", ids);
            List<PaperActivity> paperActivities = paperActivityService.selectByExample(example);
            //活动试卷获得模块信息
            Function<PaperActivity, String> getModule = (paperActivity -> {
                if (null == paperActivity.getPaperId() || paperActivity.getPaperId().intValue() <= 0) {
                    return paperActivity.getModule();
                } else {
                    Long paperEntityId = paperActivity.getPaperId();
                    PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperEntityId);
                    if (null != paperEntity) {
                        return paperEntity.getModule();
                    }
                }
                return "";
            });
            if (CollectionUtils.isNotEmpty(paperActivities)) {
                return getModuleNames.apply(paperActivities.stream().map(getModule::apply).collect(Collectors.toList()));
            }
        }
        return Lists.newArrayList();
    }

    @Override
    public List<Integer> getQuestionByModule(List<Long> ids, PaperInfoEnum.TypeInfo typeInfo, boolean duplicateFlag, String moduleName) {
        ArrayList<Integer> list = Lists.newArrayList();
        for (Long paperId : ids) {
            List<Integer> questionIds = getQuestionByModuleSingle(paperId, typeInfo, moduleName);
            if (CollectionUtils.isNotEmpty(questionIds)) {
                list.addAll(questionIds);
            }
        }
        if (duplicateFlag) {
            return list.stream().distinct().collect(Collectors.toList());
        } else {
            return list.stream().collect(Collectors.toList());
        }

    }

    @Override
    public void writeQuestions(BaseTool tool, BiConsumer<BaseTool, Map<String, Object>> writeQuestion, List<Integer> questionIds, QuestionElementEnum.QuestionFieldEnum exportType) {
        int parentId = 0;

        List<Question> questions = findByIds(questionIds);
        if (CollectionUtils.isEmpty(questions)) {
            return;
        }
        int i = 1;
        for (Question question : questions) {
            if (null == question) {
                continue;
            }
            Integer apply = getParentFunction().apply(question);
            if (parentId != apply.intValue()) {
                addQuestionElement(tool, question, true, i, writeQuestion);
                parentId = apply;
            } else {
                addQuestionElement(tool, question, false, i, writeQuestion);
            }
            i++;
        }
    }

    @Override
    public String makeWordByPracticeCard(PracticeCard practiceCard,
                                         PdfWriteServiceImplV1.PdfWriteTool baseTool,
                                         BiFunction<BaseTool, String, BaseTool> initWriteTool,
                                         BiConsumer<BaseTool, String> titleWrite,
                                         BiConsumer<BaseTool, PaperModuleInfo> writeModule,
                                         BiConsumer<BaseTool, Map<String, Object>> writeQuestion) {
        baseTool.setSubject(practiceCard.getSubject());
        BaseTool tool = initWriteTool.apply(baseTool, practiceCard.getName());
        titleWrite.accept(tool, practiceCard.getName());
        List<Module> modules = practiceCard.getPaper().getModules();
        int skip = 0;
        int realSort = 0;
        List<Integer> ids = practiceCard.getPaper().getQuestions();
        Map<Integer, Question> questionMap = questionDao.findByIds(ids).stream().collect(Collectors.toMap(i -> i.getId(), i -> i));
        for (Module module : modules) {
            if (StringUtils.isNotBlank(baseTool.getModuleName()) && module.getName().indexOf(baseTool.getModuleName()) == -1) {
                System.out.println("module.getName() = " + module.getName() + ">>>>>> " + baseTool.getModuleName());
                continue;
            }
            PaperModuleInfo moduleInfo = PaperModuleInfo.builder().id(module.getCategory()).name(module.getName()).build();
            //写入模块信息
            writeModule.accept(tool, moduleInfo);
            List<Integer> tempIds = ids.stream().skip(skip).limit(module.getQcount()).collect(Collectors.toList());
            int parentId = 0;
            for (Integer id: tempIds) {
                Question question = questionMap.get(id);
                if(null == question){           //试卷为同步到mongo库中
                    log.error("question is null, id = {}",id);
                    continue;
                }
                //检查试题科目，如果科目为特定科目，则下载的内容没有尾页二维码
                checkSubject(question, baseTool);
                if (baseTool.isDuplicateFlag()) {
                    SetOperations setOperations = redisTemplate.opsForSet();
                    String key = "duplicate_set";
                    Boolean member = setOperations.isMember(key, question.getId() + "");
                    if (member) {
                        continue;
                    }
                    setOperations.add(key, question.getId() + "");
                    redisTemplate.expire(key, 1, TimeUnit.HOURS);
                }
                Integer apply = getParentFunction().apply(question);
                realSort++;
                if (parentId != apply.intValue()) {
                    addQuestionElement(tool, question, true, realSort, writeQuestion);
                    parentId = apply;
                } else {
                    addQuestionElement(tool, question, false, realSort, writeQuestion);
                }
            }
            skip += module.getQcount();
        }
        return tool.getFile().getName();
    }

    private List<Question> findByIds(List<Integer> questionIds) {
        ArrayList<Question> list = Lists.newArrayList();
        List<Integer> missIds = Lists.newArrayList();
        for (Integer questionId : questionIds) {
            Question question = QUESTION_CACHE.getIfPresent(questionId);
            if(null == question){
                missIds.add(questionId);
            }else{
                list.add(question);
            }
        }
        if(CollectionUtils.isEmpty(missIds)){
            return list;
        }
        List<Question> questions = questionDao.findByIds(missIds);
        for (Question question : questions) {
            QUESTION_CACHE.put(question.getId(),question);
        }
        list.addAll(questions);
        Map<Integer, Question> questionMap = list.stream().collect(Collectors.toMap(i -> i.getId(), i -> i));
        List<Question> collect = questionIds.stream().filter(i -> null != questionMap.get(i)).map(questionMap::get).collect(Collectors.toList());
        return collect;
    }

    private List<Integer> getQuestionByModuleSingle(Long paperId, PaperInfoEnum.TypeInfo typeInfo, String moduleName) {
        String moduleInfo = "";
        List<PaperQuestion> paperQuestionList = Lists.newArrayList();
        if (typeInfo.equals(PaperInfoEnum.TypeInfo.ENTITY)) {
            PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperId);
            if (null != paperEntity) {
                moduleInfo = paperEntity.getModule();
                paperQuestionList.addAll(paperQuestionService.findByPaperIdAndType(paperId, typeInfo));
            }
        } else if (typeInfo.equals(PaperInfoEnum.TypeInfo.SIMULATION)) {
            PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(paperId);
            if (null != paperActivity) {
                if (null == paperActivity.getPaperId() || paperActivity.getPaperId().intValue() <= 0) {
                    moduleInfo = paperActivity.getModule();
                    paperQuestionList.addAll(paperQuestionService.findByPaperIdAndType(paperId, typeInfo));
                } else {
                    Long paperEntityId = paperActivity.getPaperId();
                    PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperEntityId);
                    if (null != paperEntity) {
                        moduleInfo = paperEntity.getModule();
                        paperQuestionList.addAll(paperQuestionService.findByPaperIdAndType(paperEntityId, PaperInfoEnum.TypeInfo.ENTITY));
                    }
                }
            }
        }
        Map<Integer, List<PaperQuestion>> moduleMap = paperQuestionList.stream().collect(Collectors.groupingBy(PaperQuestion::getModuleId));
        List<PaperModuleInfo> paperModuleInfos = PaperModuleHandler.analysisModuleStr(moduleInfo);
        Optional<PaperModuleInfo> first = paperModuleInfos.stream().filter(i -> i.getName().indexOf(moduleName)>-1).findFirst();
        if (!first.isPresent()) {
            return Lists.newArrayList();
        }
        PaperModuleInfo paperModuleInfo = first.get();
        List<PaperQuestion> paperQuestions = moduleMap.getOrDefault(paperModuleInfo.getId(), Lists.newArrayList());
        if (CollectionUtils.isEmpty(paperQuestions)) {
            return Lists.newArrayList();
        }
        return paperQuestions.stream().map(PaperQuestion::getQuestionId).map(Long::intValue).collect(Collectors.toList());
    }

    /**
     * 判断科目
     *
     * @param question
     * @param baseTool
     */
    private void checkSubject(Question question, BaseTool baseTool) {
        int subject = question.getSubject();
        final ArrayList<Integer> subjectIds = Lists.newArrayList(24);
        if (subjectIds.contains(subject)) {
            baseTool.setFooterFlag(false);
        }
    }

    private void addQuestionElement(BaseTool tool, Question question, boolean withMaterial, Integer sort, BiConsumer<BaseTool, Map<String, Object>> questionWrite) {
        //题型
        if (withMaterial) {
            //复合题写入
            questionWrite.accept(tool, transMapData(question, -1));
        }
        //单题写入
        questionWrite.accept(tool, transMapData(question, sort));
    }

    public static Function<Question, Integer> getParentFunction() {
        return (question -> {
            int multiId = 0;
            if (question instanceof GenericQuestion) {
                multiId = ((GenericQuestion) question).getParent();
            } else if (question instanceof GenericSubjectiveQuestion) {
                multiId = ((GenericSubjectiveQuestion) question).getParent();
            }
            return multiId;
        });
    }
}
