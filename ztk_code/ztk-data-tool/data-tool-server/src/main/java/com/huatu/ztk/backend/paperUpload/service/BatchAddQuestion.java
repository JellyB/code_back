package com.huatu.ztk.backend.paperUpload.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paper.bean.BackendPaperStatus;
import com.huatu.ztk.backend.paper.bean.PaperErrors;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.dao.PaperQuestionDao;
import com.huatu.ztk.backend.paper.service.PaperQuestionService;
import com.huatu.ztk.backend.paperUpload.bean.PaperUploadError;
import com.huatu.ztk.backend.paperUpload.dao.LogIterator;
import com.huatu.ztk.backend.question.bean.QuestionFull;
import com.huatu.ztk.backend.question.dao.QuestionOperateDao;
import com.huatu.ztk.backend.question.service.QuestionOperateService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.common.QuestionType;
import com.itextpdf.text.BadElementException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lenovo on 2017/5/16.
 *
 */
@Service
public class BatchAddQuestion extends LogIterator{
    private static Logger logger = LoggerFactory.getLogger(BatchAddQuestion.class);
    @Autowired
    private PaperQuestionService paperQuestionService;
    @Autowired
    private QuestionOperateDao questionOperateDao;
    @Autowired
    private PaperQuestionDao paperQuestionDao;
    @Autowired
    private PaperDao paperDao;
    //存储并获取已被占用的questionIds
    private static ThreadLocal<Integer> questionIdLock = new ThreadLocal<>();
    //全局变量试卷信息
    private static ThreadLocal<Map> paperLocal = new ThreadLocal<>();
    public int getQuestionId(){
        int i =questionIdLock.get();
        questionIdLock.set(i+1);
        return i+1;
    }
    public void setQuestionId(int i){
        questionIdLock.set(i-1);
    }
    public Map getPaper(){
        return paperLocal.get();
    }
    public void setPaper(Map map){
        paperLocal.set(map);
    }
    /**
     * 添加试题信息
     * @param paper
     * @param uid
     * @param questionList
     */
    public Map addQuestionList(Map paper, long uid, LinkedList questionList) throws BizException, IOException, BadElementException {
        Map mapData = Maps.newHashMap();
        int qid = questionOperateDao.findId(Integer.parseInt(String.valueOf(paper.get("qcount"))));
        mapData.put("startId",qid);
        setQuestionId(qid);
        setPaper(paper);
        Map<String,LinkedList> assemResult = new HashMap<>();
        LinkedList<Question> questions = new LinkedList();
        LinkedList<QuestionExtend> questionExtends = new LinkedList();
        LinkedList<QuestionExtend> questionsToPaper = new LinkedList();
        assemResult.put("questions",questions);
        assemResult.put("questionExtends",questionExtends);
        assemResult.put("questionsToPaper",questionsToPaper);
        for(Object obj:questionList){
            if(obj instanceof Map){
                if(obj==null||((Map) obj).isEmpty()){
                    continue;
                }
                logger.info("开始添加第"+((Map) obj).get("sequence")+"道题"+new Date());
                String type = ((Map) obj).get("type").toString();
                if("99".equals(type)||"100".equals(type)||"101".equals(type)||"109".equals(type)){
                    //客观题
                    addGenericObjectQuestion(assemResult,(Map)obj,uid);
                }else if("105".equals(type)){
                    //复合客观题
                    addCompositeObjectiveQuestion(assemResult,(Map)obj,uid);
                }else if("107".equals(type)){
                    //复合主观题
                    addCompositeSubjectiveQuestion(assemResult,(Map)obj,uid);
                }else{
                    //主观题
                    addGenericSubjectiveQuestion(assemResult,(Map)obj,uid);
                }
            }else{
                logger.error("试题信息不能为空");
            }
        }
        logger.info("cone10:assemResult:"+JsonUtil.toJson(assemResult));
        batchAddQuestions(paper,assemResult);
        mapData.put("endId",questionIdLock.get());
        return mapData;
    }

    private void batchAddQuestions(Map paper,Map<String, LinkedList> assemResult) throws BizException {
        LinkedList<Question> questions = assemResult.get("questions");
        LinkedList<QuestionExtend> questionExtends = assemResult.get("questionExtends");
        LinkedList<QuestionExtend> questionsToPaper = assemResult.get("questionsToPaper");
        long start0 = System.currentTimeMillis();
        questionOperateDao.insertAll(questions);
        long end0 = System.currentTimeMillis();
        logger.info("批量添加试卷主要信息用时={}",end0-start0);
        setLoggerList("time","批量添加试卷主要信息用时"+(end0-start0),logger.getName(),"");
        questionOperateDao.insertExtendAll(questionExtends);
        long end1 = System.currentTimeMillis();
        logger.info("批量添加试卷扩展信息用时={}",end1-end0);
        setLoggerList("time","批量添加试卷扩展信息用时"+(end1-end0),logger.getName(),"");
        long start = System.currentTimeMillis();
        if(!"-1".equals(String.valueOf(paper.get("id")))){
            //整理试卷，模块，题目之间的关系
            insertQuestion(questionsToPaper);
            long end = System.currentTimeMillis();
            logger.info("查看试卷总用时={}",end-start);
            setLoggerList("time","查看试卷总用时"+(end-start),logger.getName(),"");
        }
    }
    public void insertQuestion(LinkedList<QuestionExtend> questionsToPaper) throws BizException{
        Paper paper = paperDao.findById(Integer.parseInt(String.valueOf(this.getPaper().get("id"))));
        //得到就的questionId集合
        List<Integer> oldQuestions = paper.getQuestions();
        ArrayListMultimap<Integer,QuestionExtend> moduleIdToQuestion = ArrayListMultimap.create();
        for(QuestionExtend questionExtend:questionsToPaper){
            moduleIdToQuestion.put(questionExtend.getModuleId(),questionExtend);
        }
        //得到所有模块，修改试卷模块信息
        List<Module> modules = paper.getModules();
        //如果集合为空，创建集合
        if (CollectionUtils.isEmpty(oldQuestions)) {
            //遍历模块，匹配题目
            List<Integer> newQuestionIds = new ArrayList<>();
            for (Module module : modules) {
                List<QuestionExtend> newQuestionList =  moduleIdToQuestion.get(module.getCategory());
                module.setQcount(newQuestionList.size());
                newQuestionList.sort((a, b) -> (a.getSequence() > b.getSequence() ? 1 : -1));
                newQuestionList.forEach(e -> newQuestionIds.add(e.getQid()));
            }
            //模块所含题目数量
            paper.setModules(modules);
            //试卷所含题目id
            paper.setQuestions(newQuestionIds);
            //试卷所含题目数量
            paper.setQcount(newQuestionIds.size());
        }else{
            //已经排好序号的题目extend
            List<QuestionExtend> totalExtendList = paperQuestionDao.findExtendBath(oldQuestions);
            //moduleId,extend  map
            ArrayListMultimap<Integer, QuestionExtend> map = ArrayListMultimap.create();
            for (QuestionExtend extend : totalExtendList) {
                map.put(extend.getModuleId(), extend);
            }
            //新的试题id列表
            List<Integer> newQuestions = new ArrayList<>();

            for (Module module:modules) {
                List<QuestionExtend> extendList = map.get(module.getCategory());
                List<QuestionExtend> newQuestionList =  moduleIdToQuestion.get(module.getCategory());
                extendList.addAll(newQuestionList);
                module.setQcount(extendList.size());
                List<Integer> newQuestionsForModule = new ArrayList<>();
                extendList.forEach(e -> newQuestionsForModule.add(e.getQid()));
                newQuestionsForModule.sort((a,b)->(a>b)?1:-1);
                newQuestions.addAll(newQuestionsForModule);
            }
            paper.setQuestions(newQuestions);
            paper.setQcount(newQuestions.size());
        }
        paperQuestionService.updateBigQustionsAndPaper(paper);
    }
    /**
     * 根据传输过来的json，添加单一主观题
     * @param map
     * @param uid
     * @return
     */
    public void addGenericSubjectiveQuestion(Map<String,LinkedList> assemResult,Map map,long uid) throws BizException, IOException, BadElementException {

        int qid = this.getQuestionId();
        QuestionFull questionFull = assembleGenericSubjectQuestion(map,uid);
        GenericSubjectiveQuestion question = (GenericSubjectiveQuestion) questionFull.getQuestion();
        QuestionExtend questionExtend = questionFull.getQuestionExtend();
        question.setId(qid);//为question分配id
        question.setParent(0);//为question设置父节点
        questionExtend.setQid(qid);//为questionExtend分配id
        LinkedList questions = assemResult.get("questions");
        LinkedList questionExtends = assemResult.get("questionExtends");
        LinkedList questionsToPaper = assemResult.get("questionsToPaper");
        questions.addLast(question);
        questionExtends.addLast(questionExtend);
        questionsToPaper.addLast(questionExtend);
    }
    /**
     * 组装单一主观题
     * @param result
     * @param uid
     * @return
     * @throws BadElementException
     * @throws BizException
     * @throws IOException
     */
    public QuestionFull assembleGenericSubjectQuestion(Map result,long uid) throws BadElementException, BizException, IOException {
        long start = System.currentTimeMillis();

        //question所有字段
        String answerRequire = "" ;//答题要求
        String referAnalysis = null ;//参考解析，作为参考答案
        String examPoint = "" ;//审题要求
        List<String> materials = new ArrayList<>() ;//材料
        String material = null;
        String require = "" ;//题目要求
        float score = 0 ;//分数
        String scoreExplain = "" ;//赋分说明
        String solvingIdea = "" ;//解题思路
        String stem = null;//题干
        long creatTime =  System.currentTimeMillis();
        int mode = -1;//试题的模型类型
        int status = -1;//试题状态
        int subject = -1;//考试科目
        int year = -1;//年份
        String from = "";//来源
        int area = -1;//地区id
        String teachType = "";//教研题型
        int paperId = -1;//试卷Id
        int maxWordCount = -1;//最大字数限制
        int minWordCount = -1;//最小字数限制
        int channel = -1;

        if(result.get("answerRequire")!=null){
            answerRequire = String.valueOf(result.get("answerRequire"));
        }
        if(result.get("referAnalysis")!=null){
            referAnalysis = String.valueOf(result.get("referAnalysis"));
        }
        if(result.get("examPoint")!=null){
            examPoint = String.valueOf(result.get("examPoint"));
        }
        if(result.get("materials")!=null){
            materials = (List<String>) result.get("materials");
            for(int i=0;i<materials.size();i++){
                String ml = materials.get(i);
                material +=ml;
                materials.set(i,ml);
            }
        }
        if(result.get("require")!=null){
            require = String.valueOf(result.get("require"));
        }
        if(result.get("score")!=null){
            score = Float.parseFloat(String.valueOf(result.get("score")));
        }
        if(result.get("scoreExplain")!=null){
            scoreExplain = String.valueOf(result.get("scoreExplain"));
        }
        if(result.get("solvingIdea")!=null){
            solvingIdea = String.valueOf(result.get("solvingIdea"));
        }
        if(result.get("stem")!=null){
            stem = String.valueOf(result.get("stem"));
        }
        if(getPaper()!=null){
            Map<String,Object> paper = (Map<String, Object>) getPaper();
            paperId = Integer.parseInt(String.valueOf(paper.get("id")));
            status = QuestionStatus.CREATED;
            if(paperId!=-1){
                if(paper.get("questionStatus")!=null&&Integer.parseInt(String.valueOf(paper.get("questionStatus")))==4){
                    status = QuestionStatus.DELETED;
                }
                if (paper.get("catgory")!=null&&isNumeric(paper.get("catgory"))) {
                    subject = Integer.parseInt(String.valueOf(paper.get("catgory")));
                }
                if (paper.get("year")!=null&&isNumeric(paper.get("year"))) {
                    year = Integer.parseInt(String.valueOf(paper.get("year")));
                }
                from = paper.get("name")==null?"":String.valueOf(paper.get("name"));
                if (paper.get("area")!=null&&isNumeric(paper.get("area"))) {
                    area = Integer.parseInt(String.valueOf(paper.get("area")));
                }
                if (paper.get("type")!=null&&isNumeric(paper.get("type"))) {
                    mode = Integer.parseInt(String.valueOf(paper.get("type")));
                }
            }else{
                if (result.get("year")!=null&&isNumeric(result.get("year"))) {
                    year = Integer.parseInt(String.valueOf(result.get("year")));
                }
                if (result.get("catgory")!=null&&isNumeric(result.get("catgory"))) {
                    subject = Integer.parseInt(String.valueOf(result.get("catgory")));
                }
                if (result.get("area")!=null&&isNumeric(result.get("area"))) {
                    area = Integer.parseInt(String.valueOf(result.get("area")));
                }
                if (result.get("mode")!=null&&isNumeric(result.get("mode"))) {
                    mode = Integer.parseInt(String.valueOf(result.get("mode")));
                }
                from = result.get("name")==null?"":String.valueOf(result.get("name"));
            }

        }
        if(result.get("teachType")!=null){teachType = String.valueOf(result.get("teachType"));}
        if(result.get("maxWordCount")!=null){maxWordCount = Integer.parseInt(String.valueOf(result.get("maxWordCount")));}
        if(result.get("minWordCount")!=null){minWordCount = Integer.parseInt(String.valueOf(result.get("minWordCount")));}

        GenericSubjectiveQuestion genericSubjectiveQuestion = new  GenericSubjectiveQuestion();
        genericSubjectiveQuestion.setAnswerRequire(answerRequire);
        genericSubjectiveQuestion.setExamPoint(examPoint);
        genericSubjectiveQuestion.setMaterials(materials);
        genericSubjectiveQuestion.setReferAnalysis(referAnalysis);
        genericSubjectiveQuestion.setRequire(require);
        genericSubjectiveQuestion.setScore(score);
        genericSubjectiveQuestion.setScoreExplain(scoreExplain);
        genericSubjectiveQuestion.setSolvingIdea(solvingIdea);
        genericSubjectiveQuestion.setStem(stem);
        genericSubjectiveQuestion.setType(QuestionType.SINGLE_SUBJECTIVE);
        genericSubjectiveQuestion.setCreateTime(creatTime);
        genericSubjectiveQuestion.setCreateBy(uid);
        genericSubjectiveQuestion.setFrom(from);
        genericSubjectiveQuestion.setYear(year);
        genericSubjectiveQuestion.setArea(area);
        genericSubjectiveQuestion.setStatus(status);
        genericSubjectiveQuestion.setMode(mode);
        genericSubjectiveQuestion.setSubject(subject);
        genericSubjectiveQuestion.setMaterial(material);
        genericSubjectiveQuestion.setTeachType(teachType);
        if(maxWordCount>0){
            genericSubjectiveQuestion.setMaxWordCount(maxWordCount);
        }
        if(minWordCount>=0){
            genericSubjectiveQuestion.setMinWordCount(minWordCount);
        }
        if(channel>0){
            genericSubjectiveQuestion.setChannel(channel);
        }
        //QuestionExtend所有字段
        float sequence = 0;//题序
        String author = null ;//作者
        int moduleId = -1;//模块id

        if(result.get("moduleId")!=null){
            moduleId = Integer.parseInt(String.valueOf(result.get("moduleId")));
        }
        if(result.get("author")!=null){
            author = String.valueOf(result.get("author"));
        }
        if(result.get("sequence")!=null){
            sequence = Float.parseFloat(String.valueOf((result.get("sequence"))));
        }

        QuestionExtend questionExtend = new QuestionExtend();
        questionExtend.setPaperId(paperId);
        questionExtend.setAuthor(author);
        questionExtend.setSequence(sequence);
        questionExtend.setModuleId(moduleId);

        QuestionFull questionFull = QuestionFull.builder()
                .question(genericSubjectiveQuestion)
                .questionExtend(questionExtend)
                .build();
        long end = System.currentTimeMillis();
        logger.info("组装单一主观题用时={}",end-start);
        return questionFull;
    }
    /**
     *根据传输过来的json，添加复合主观题
     * @param result
     * @param uid
     */
    public void addCompositeSubjectiveQuestion(Map<String,LinkedList> assemResult,Map result,long uid) throws BizException, IOException, BadElementException {
        QuestionFull questionFull = assembleCompositeSubjectiveQuestion(result,uid);
        LinkedList questions = assemResult.get("questions");
        LinkedList questionExtends = assemResult.get("questionExtends");
        LinkedList questionsToPaper = assemResult.get("questionsToPaper");
        CompositeSubjectiveQuestion question = (CompositeSubjectiveQuestion) questionFull.getQuestion();
        QuestionExtend questionExtend = questionFull.getQuestionExtend();
        List<QuestionFull> subQuestionFulls = questionFull.getSubQuestions();
        int num = subQuestionFulls.size();
        int qid = this.getQuestionId();
        List<Integer> subQuestionIds = new ArrayList<>();
        question.setId(qid);//为question分配id
        questionExtend.setQid(qid);//为questionExtend分配id
        long startP = System.currentTimeMillis();
        float sequence = questionExtend.getSequence();
        long endP = System.currentTimeMillis();
        logger.info("查看大题题序用时={}",endP-startP);
        logger.info("题序={}",Math.round(sequence));
        for(int i=0;i<num;i++){
            int subQid = this.getQuestionId();
            subQuestionIds.add(subQid);
            if(subQuestionFulls.get(i).getQuestion().getType()==QuestionType.SINGLE_SUBJECTIVE){
                GenericSubjectiveQuestion subQuestion = (GenericSubjectiveQuestion) subQuestionFulls.get(i).getQuestion();
                QuestionExtend subQuestionExtend = subQuestionFulls.get(i).getQuestionExtend();
                subQuestion.setId(subQid);//为question分配id
                subQuestion.setParent(qid);//为question设置父节点
                subQuestionExtend.setQid(subQid);//为questionExtend分配id

                long start = System.currentTimeMillis();
                long end = System.currentTimeMillis();
                logger.info("查看题序用时={}",end-start);
                questions.addLast(subQuestion);
                questionExtends.addLast(subQuestionExtend);
                questionsToPaper.addLast(subQuestionExtend);
            }else{
                GenericQuestion subQuestion = (GenericQuestion) subQuestionFulls.get(i).getQuestion();
                QuestionExtend subQuestionExtend = subQuestionFulls.get(i).getQuestionExtend();
                subQuestion.setId(subQid);//为question分配id
                subQuestion.setParent(qid);//为question设置父节点
                subQuestionExtend.setQid(subQid);//为questionExtend分配id
                long start = System.currentTimeMillis();
                long end = System.currentTimeMillis();
                logger.info("查看题序用时={}",end-start);
                questions.addLast(subQuestion);
                questionExtends.addLast(subQuestionExtend);
                questionsToPaper.addLast(subQuestionExtend);
            }

        }
        question.setQuestions(subQuestionIds);
        questions.add(question);
        questionExtends.add(questionExtend);
    }
    public QuestionFull assembleCompositeSubjectiveQuestion(Map result,long uid) throws BadElementException, BizException, IOException {
        long start = System.currentTimeMillis();
        //question的所有字段
        List<String> materials = new ArrayList<>() ;//材料
        String material = "";
        String require = "";
        long creatTime =  System.currentTimeMillis();
        int mode = -1;//试题的模型类型
        int status = -1;//试题状态
        int subject = -1;//考试科目
        int year = -1;//年份
        String from = "";//来源
        int area = -1;//地区id
        int paperId = -1;//试卷Id
        int channel = -1;
        if(result.get("materials")!=null){
            materials = (List<String>) result.get("materials");
            for(int i=0;i<materials.size();i++){
                String ml = materials.get(i);
                material +=ml;
                materials.set(i,ml);
            }
        }
        if(result.get("require")!=null){
            require = String.valueOf(result.get("require"));
        }
        if(this.getPaper()!=null){
            Map<String,Object> paper = (Map<String, Object>)this.getPaper();
            paperId = Integer.parseInt(String.valueOf(paper.get("id")));
            status = QuestionStatus.CREATED;
            if(paper.get("channel")!=null){
                channel = Integer.parseInt(String.valueOf(paper.get("channel")));
            }
            if(paperId!=-1){
                if(paper.get("questionStatus")!=null&&Integer.parseInt(String.valueOf(paper.get("questionStatus")))==4){
                    status = QuestionStatus.DELETED;
                }
                if (paper.get("catgory")!=null&&isNumeric(paper.get("catgory"))) {
                    subject = Integer.parseInt(String.valueOf(paper.get("catgory")));
                }
                if (paper.get("year")!=null&&isNumeric(paper.get("year"))) {
                    year = Integer.parseInt(String.valueOf(paper.get("year")));
                }
                from = paper.get("name")==null?"":String.valueOf(paper.get("name"));
                if (paper.get("area")!=null&&isNumeric(paper.get("area"))) {
                    area = Integer.parseInt(String.valueOf(paper.get("area")));
                }
                if (paper.get("type")!=null&&isNumeric(paper.get("type"))) {
                    mode = Integer.parseInt(String.valueOf(paper.get("type")));
                }
            }else{
                from = result.get("name")==null?"":String.valueOf(result.get("name"));
                if (result.get("year")!=null&&isNumeric(result.get("year"))) {
                    year = Integer.parseInt(String.valueOf(result.get("year")));
                }
                if (result.get("catgory")!=null&&isNumeric(result.get("catgory"))) {
                    subject = Integer.parseInt(String.valueOf(result.get("catgory")));
                }
                if (result.get("area")!=null&&isNumeric(result.get("area"))) {
                    area = Integer.parseInt(String.valueOf(result.get("area")));
                }
                if (result.get("mode")!=null&&isNumeric(result.get("mode"))) {
                    mode = Integer.parseInt(String.valueOf(result.get("mode")));
                }
            }
        }


        CompositeSubjectiveQuestion compositeSubjectiveQuestion = new CompositeSubjectiveQuestion();
        compositeSubjectiveQuestion.setArea(area);
        compositeSubjectiveQuestion.setFrom(from);
        compositeSubjectiveQuestion.setYear(year);
        compositeSubjectiveQuestion.setSubject(subject);
        compositeSubjectiveQuestion.setMode(mode);
        compositeSubjectiveQuestion.setCreateTime(creatTime);
        compositeSubjectiveQuestion.setCreateBy(uid);
        compositeSubjectiveQuestion.setMaterials(materials);
        compositeSubjectiveQuestion.setStatus(status);
        compositeSubjectiveQuestion.setType(QuestionType.MULTI_SUBJECTIVE);//占时定位107
        compositeSubjectiveQuestion.setUpdateBy(0);
        compositeSubjectiveQuestion.setUpdateTime(0);
        compositeSubjectiveQuestion.setId(-1);
        compositeSubjectiveQuestion.setRequire(require);
        compositeSubjectiveQuestion.setMaterial(material);
        if(channel>0){
            compositeSubjectiveQuestion.setChannel(channel);
        }
        //QuestionExtend所有字段
        float sequence = 0;//题序
        int moduleId = -1;//模块id

        if(result.get("moduleId")!=null){
            moduleId = Integer.parseInt(String.valueOf(result.get("moduleId")));
        }
        if(result.get("sequence")!=null){
            sequence = Float.parseFloat(String.valueOf((result.get("sequence"))));
        }
        QuestionExtend questionExtend = new QuestionExtend();
        questionExtend.setPaperId(paperId);
        questionExtend.setAuthor("");
        questionExtend.setReviewer("");
//        questionExtend.setExtend("");
        questionExtend.setSequence(sequence);
        questionExtend.setModuleId(moduleId);

        //subQuestion 字段
        List<Map> subQuestionstr;
        List<Integer> subQuestionsType;
        List<QuestionFull> subQuestions = new ArrayList<>();
        long startAssemble = System.currentTimeMillis();
        if(result.get("subQuestionStr")!=null&&result.get("subQuestionsType")!=null){
            subQuestionstr = (List<Map>) result.get("subQuestionStr");
            subQuestionsType = (List<Integer>) result.get("subQuestionsType");
            for(int i=0;i<subQuestionstr.size();i++){
                if(subQuestionsType.get(i)==1){//1为主观题
                    subQuestions.add(assembleGenericSubjectQuestion(subQuestionstr.get(i),uid));
                }else{
                    subQuestions.add(assembleGenericObjectQuestion(subQuestionstr.get(i),uid));
                }

            }
        }
        long endAssemble = System.currentTimeMillis();
        logger.info("组装子题总用时={}",endAssemble-startAssemble);

        QuestionFull questionFull = QuestionFull.builder()
                .question(compositeSubjectiveQuestion)
                .questionExtend(questionExtend)
                .subQuestions(subQuestions)
                .build();
        long end = System.currentTimeMillis();
        logger.info("组装用时={}",end-start);
        return questionFull;
    }
    /**
     * 根据传输过来的json，添加复合客观题
     */
    public void addCompositeObjectiveQuestion(Map<String,LinkedList> assemResult,Map result,long uid) throws BizException, IOException, BadElementException {
        QuestionFull questionFull = assembleCompositeObjectiveQuestion(result,uid);
        LinkedList questions = assemResult.get("questions");
        LinkedList questionExtends = assemResult.get("questionExtends");
        LinkedList questionsToPaper = assemResult.get("questionsToPaper");

        CompositeQuestion question = (CompositeQuestion) questionFull.getQuestion();
        QuestionExtend questionExtend = questionFull.getQuestionExtend();
        List<QuestionFull> subQuestionFulls = questionFull.getSubQuestions();
        int num = subQuestionFulls.size();
        int qid = this.getQuestionId();
        List<Integer> subQuestionIds = new ArrayList<>();
        question.setId(qid);//为question分配id
        questionExtend.setQid(qid);//为questionExtend分配id
        for(int i=0;i<num;i++){
            int subQid = this.getQuestionId();
            subQuestionIds.add(subQid);
            GenericQuestion subQuestion = (GenericQuestion) subQuestionFulls.get(i).getQuestion();
            QuestionExtend subQuestionExtend = subQuestionFulls.get(i).getQuestionExtend();
            subQuestion.setId(subQid);//为question分配id
            subQuestion.setParent(qid);//为question设置父节点
            subQuestionExtend.setQid(subQid);//为questionExtend分配id
            questions.addLast(subQuestion);
            questionExtends.addLast(subQuestionExtend);
            questionsToPaper.addLast(subQuestionExtend);
        }
        question.setQuestions(subQuestionIds);
        logger.info("id={},subQids={}",qid,subQuestionIds);
        questions.addLast(question);
        questionExtends.addLast(questionExtend);
    }
    public QuestionFull assembleCompositeObjectiveQuestion(Map result,long uid) throws BadElementException, BizException, IOException {
        long start = System.currentTimeMillis();
        //question所有字段
        String material = String.valueOf(result.get("material"));//材料
        List<String> materials = Lists.newArrayList();
        float score = 0 ;//分数
        int difficult = -1;//难度
        long creatTime =  System.currentTimeMillis();
        int mode = -1;//试题的模型类型
        int status = -1;//试题状态
        int subject = -1;//考试科目
        int year = -1;//年份
        String from = "";//来源
        int area = -1;//地区id
        int paperId = -1;//试卷Id
        int channel = -1;
        if(result.get("material")!=null){
            material = String.valueOf(result.get("material"));
            materials.add(material);
        }
        if(result.get("score")!=null){
            score = Float.parseFloat(String.valueOf(result.get("score")));
        }
        if(result.get("difficult")!=null){
            difficult = Integer.parseInt(String.valueOf(result.get("difficult")));
        }
        if(this.getPaper()!=null){
            Map<String,Object> paper = (Map<String, Object>) this.getPaper();
            paperId = Integer.parseInt(String.valueOf(paper.get("id")));
            status = QuestionStatus.CREATED;
            if(paper.get("channel")!=null){
                channel = Integer.parseInt(String.valueOf(paper.get("channel")));
            }
            if(paperId!=-1){
                if(paper.get("questionStatus")!=null&&Integer.parseInt(String.valueOf(paper.get("questionStatus")))==4){
                    status = QuestionStatus.DELETED;
                }
                if (paper.get("catgory")!=null&&isNumeric(paper.get("catgory"))) {
                    subject = Integer.parseInt(String.valueOf(paper.get("catgory")));
                }
                if (paper.get("year")!=null&&isNumeric(paper.get("year"))) {
                    year = Integer.parseInt(String.valueOf(paper.get("year")));
                }
                from = paper.get("name")==null?"":String.valueOf(paper.get("name"));
                if (paper.get("area")!=null&&isNumeric(paper.get("area"))) {
                    area = Integer.parseInt(String.valueOf(paper.get("area")));
                }
                if (paper.get("type")!=null&&isNumeric(paper.get("type"))) {
                    mode = Integer.parseInt(String.valueOf(paper.get("type")));
                }
            }else{
                from = result.get("name")==null?"":String.valueOf(result.get("name"));
                if (result.get("year")!=null&&isNumeric(result.get("year"))) {
                    year = Integer.parseInt(String.valueOf(result.get("year")));
                }
                if (result.get("catgory")!=null&&isNumeric(result.get("catgory"))) {
                    subject = Integer.parseInt(String.valueOf(result.get("catgory")));
                }
                if (result.get("area")!=null&&isNumeric(result.get("area"))) {
                    area = Integer.parseInt(String.valueOf(result.get("area")));
                }
                if (result.get("mode")!=null&&isNumeric(result.get("mode"))) {
                    mode = Integer.parseInt(String.valueOf(result.get("mode")));
                }
            }
        }

        CompositeQuestion compositeQuestion = new CompositeQuestion();
        compositeQuestion.setArea(area);
        compositeQuestion.setFrom(from);
        compositeQuestion.setYear(year);
        compositeQuestion.setSubject(subject);
        compositeQuestion.setMode(mode);
        compositeQuestion.setCreateTime(creatTime);
        compositeQuestion.setCreateBy(uid);
        compositeQuestion.setMaterial(material);
        compositeQuestion.setStatus(status);
        compositeQuestion.setType(QuestionType.COMPOSITED);
        compositeQuestion.setUpdateBy(0);
        compositeQuestion.setUpdateTime(0);
        compositeQuestion.setId(-1);
        compositeQuestion.setMaterials(materials);
        compositeQuestion.setScore(score);
        compositeQuestion.setDifficult(difficult);
        if(channel>0){
            compositeQuestion.setChannel(channel);
        }
        //QuestionExtend所有字段
        float sequence = 0;//题序
        int moduleId = -1;//模块id
        String orgin = null;

        if(result.get("moduleId")!=null){
            moduleId = Integer.parseInt(String.valueOf(result.get("moduleId")));
        }
        if(result.get("sequence")!=null){
            sequence = Float.parseFloat(String.valueOf((result.get("sequence"))));
        }
        if(result.get("orgin")!=null){
            orgin = String.valueOf(result.get("orgin"));
        }

        QuestionExtend questionExtend = new QuestionExtend();
        questionExtend.setPaperId(paperId);
        questionExtend.setAuthor("");
        questionExtend.setReviewer("");
//        question.setExtend("");
        questionExtend.setSequence(sequence);
        questionExtend.setOrgin(orgin);
        questionExtend.setModuleId(moduleId);

        //subQuestion 字段
        List<Map> subQuestionstr = new ArrayList<>();
        List<QuestionFull> subQuestions = new ArrayList<>();

        long startAssemble = System.currentTimeMillis();
        if(result.get("subQuestionStr")!=null){
            subQuestionstr = (List<Map>) result.get("subQuestionStr");
            for(int i=0;i<subQuestionstr.size();i++){
                long startAssembleI = System.currentTimeMillis();
                subQuestions.add(assembleGenericObjectQuestion(subQuestionstr.get(i),uid));
                long endAssembleI = System.currentTimeMillis();
                logger.info("组装第"+i+"道子题总用时={}",endAssembleI-startAssembleI);
            }
        }
        long endAssemble = System.currentTimeMillis();
        logger.info("组装子题总用时={}",endAssemble-startAssemble);

        QuestionFull questionFull = QuestionFull.builder()
                .question(compositeQuestion)
                .questionExtend(questionExtend)
                .subQuestions(subQuestions)
                .build();
        long end = System.currentTimeMillis();
        logger.info("组装用时={}",end-start);
        return questionFull;

    }

    public void addGenericObjectQuestion(Map<String,LinkedList> assemResult,Map map,long uid) throws BizException, IOException, BadElementException {
        int qid = getQuestionId();
        QuestionFull questionFull = assembleGenericObjectQuestion(map,uid);
        GenericQuestion question = (GenericQuestion) questionFull.getQuestion();
        QuestionExtend questionExtend = questionFull.getQuestionExtend();
        question.setId(qid);//为question分配id
        question.setParent(0);//为question设置父节点
        questionExtend.setQid(qid);
        LinkedList questions = assemResult.get("questions");
        LinkedList questionExtends = assemResult.get("questionExtends");
        LinkedList questionsToPaper = assemResult.get("questionsToPaper");
        questions.addLast(question);
        questionExtends.addLast(questionExtend);
        questionsToPaper.addLast(questionExtend);
    }
    public QuestionFull assembleGenericObjectQuestion(Map map,long uid) throws BadElementException, BizException, IOException {
        long start = System.currentTimeMillis();
        logger.info("resule={}",map);

        //定义question各个字段
        int type = -1;//保存试题类型
        List<String> pointsName = new ArrayList<>();//保存试题的知识点名称列表，包括1\2\3级知识点
        List<Integer> pointsId = new ArrayList<>();//保存试题的知识点Id列表，包括1\2\3级知识点
        int difficult = -1 ;//试题难度

        String stem = null;//试题题干
        List<String> choices = new ArrayList<>();//选项内容
        int answer = 0;//试题答案
        String analysis = null;//解析
        float score = 0 ;//分数
        String material = null;//材料
        List<String> materials = (List<String>) map.get("materials") ;//材料列表
        long createTime =  System.currentTimeMillis();//创建时间
        int mode = -1;//试题的模型类型
        int status = -1;//试题状态
        int subject = -1;//考试科目
        int year = -1;//年份
        String from = "";//来源
        int area = -1;//地区id
        String teachType = "";//教研题型
        int paperId = -1;//试卷Id
        int channel = -1;
        long endDefine = System.currentTimeMillis();
        logger.info("定义用时={}",endDefine-start);


        if(map.get("type")!=null){
            type = Integer.parseInt(String.valueOf(map.get("type")));
        }
        if(map.get("pointsName")!=null&&map.get("pointsId")!=null){
            pointsName = (List<String>) map.get("pointsName");
            pointsId = (List<Integer>) map.get("pointsId");
            logger.info("pointsNameNew={},pointsId={}",pointsName,pointsId);
        }
        long endFindPoint = System.currentTimeMillis();
        logger.info("查找知识点用时={}",endFindPoint-endDefine);
        if(map.get("difficult")!=null){
            difficult = Integer.parseInt(String.valueOf(map.get("difficult")));
        }
        if(map.get("stem")!=null){
            stem = String.valueOf(map.get("stem"));
//            stem = questionService.htmlManage(stem);
        }
        long endFindStem = System.currentTimeMillis();
        logger.info("题干用时={}",endFindStem-endFindStem);
        if(map.get("options")!=null){
            List<Map<String,Object>> options = (List<Map<String,Object>>) map.get("options");
            for(int i=0;i<options.size();i++){
                String con = String.valueOf(options.get(i).get("con"));//选项内容
                choices.add(con);
            }
        }
        long endOptions = System.currentTimeMillis();
        logger.info("选项用时={}",endOptions-endFindStem);
        if(map.get("analysis")!=null){
            analysis = String.valueOf(map.get("analysis")) ;
        }
        long endAnalysis = System.currentTimeMillis();
        logger.info("选项用时={}",endAnalysis-endOptions);
        if(map.get("score")!=null){
            score = Float.parseFloat(String.valueOf(map.get("score")));
        }
        if(map.get("materials")!=null){
            materials = (List<String>) map.get("materials");
            for(int i=0;i<materials.size();i++){
                String ml = materials.get(i);
                material +=ml;
                materials.set(i,ml);
            }
        }
        if(map.get("material")!=null){
            material =  String.valueOf(map.get("material"));
            if(materials==null){
                materials = Lists.newArrayList();
            }
            materials.add(material);
        }
        long endMaterials = System.currentTimeMillis();
        logger.info("材料用时={}",endMaterials-endAnalysis);
        if(this.getPaper()!=null){
            Map<String,Object> paper = (Map<String, Object>)this.getPaper();
            paperId = Integer.parseInt(String.valueOf(paper.get("id")));
            if(paper.get("channel")!=null){
                channel = Integer.parseInt(String.valueOf(paper.get("channel")));
            }
            status = QuestionStatus.CREATED;
            if(paperId!=-1){
                if (paper.get("type")!=null&&isNumeric(paper.get("type"))) {
                    mode = Integer.parseInt(String.valueOf(paper.get("type")));
                }
                if(paper.get("questionStatus")!=null&&Integer.parseInt(String.valueOf(paper.get("questionStatus")))==4){
                    status = QuestionStatus.DELETED;
                }
                if (paper.get("catgory")!=null&&isNumeric(paper.get("catgory"))) {
                    subject = Integer.parseInt(String.valueOf(paper.get("catgory")));
                }
                if (paper.get("year")!=null&&isNumeric(paper.get("year"))) {
                    year = Integer.parseInt(String.valueOf(paper.get("year")));
                }
                from = paper.get("name")==null?"":String.valueOf(paper.get("name"));
                if (paper.get("area")!=null&&isNumeric(paper.get("area"))) {
                    area = Integer.parseInt(String.valueOf(paper.get("area")));
                }
            }else{
                if (map.get("year")!=null&&isNumeric(map.get("year"))) {
                    year = Integer.parseInt(String.valueOf(map.get("year")));
                }
                if (map.get("catgory")!=null&&isNumeric(map.get("catgory"))) {
                    subject = Integer.parseInt(String.valueOf(map.get("catgory")));
                }
                if (map.get("area")!=null&&isNumeric(map.get("area"))) {
                    area = Integer.parseInt(String.valueOf(map.get("area")));
                }
                if (map.get("mode")!=null&&isNumeric(map.get("mode"))) {
                    mode = Integer.parseInt(String.valueOf(map.get("mode")));
                }
                from = map.get("name")==null?"":String.valueOf(map.get("name"));
            }
        }
        long endPaper = System.currentTimeMillis();
        logger.info("材料用时={}",endPaper-endMaterials);
        if(map.get("teachType")!=null){teachType = String.valueOf(map.get("teachType"));}
        if(map.get("answer")!=null){
            answer = Integer.parseInt(String.valueOf(map.get("answer")));
        }
        GenericQuestion question = new GenericQuestion();//bean没有加builder
        question.setStem(stem);
        question.setAnalysis(analysis);
        question.setAnswer(answer);
        question.setDifficult(difficult);
        question.setChoices(choices);
        question.setPoints(pointsId);
        question.setPointsName(pointsName);
        question.setType(type);
        question.setStatus(status);
        question.setCreateBy(uid);
        question.setCreateTime(createTime);
        question.setUpdateBy(0);
        question.setUpdateTime(0);
        question.setMode(mode);
        question.setSubject(subject);
        question.setYear(year);
        question.setFrom(from);
        question.setArea(area);
        question.setScore(score);
        question.setMaterials(materials);
        question.setMaterial(material);
        question.setTeachType(teachType);
        String expand = null ;//拓展
        if(map.get("expand")!=null){
            expand = String.valueOf(map.get("expand"));
        }
        question.setExtend(expand);
        if(channel>0){
            question.setChannel(channel);
        }
        long endQuestion = System.currentTimeMillis();
        logger.info("组装题用时={}",endQuestion-start);

        //questionExtend所有字段
        int moduleId = -1;//试题模块Id

        String author = null ;//作者
        String orgin = null ;//题源
        String review = null ;//审核人
        float sequence = 0;//题序
        long startExtend = System.currentTimeMillis();
        logger.info("材料用时={}",endPaper-endMaterials);
        if(map.get("moduleId")!=null){
            moduleId = Integer.parseInt(String.valueOf(map.get("moduleId")));
        }

        if(map.get("orgin")!=null){
            orgin = String.valueOf(map.get("orgin"));
        }
        if(map.get("author")!=null){
            author = String.valueOf(map.get("author"));
        }
        if(map.get("review")!=null){
            review = String.valueOf(map.get("review"));
        }
        if(map.get("sequence")!=null){
            sequence = Float.parseFloat(String.valueOf((map.get("sequence"))));
        }
        QuestionExtend questionExtend = new QuestionExtend();
        questionExtend.setPaperId(paperId);
        questionExtend.setAuthor(author);
        questionExtend.setReviewer(review);
        questionExtend.setSequence(sequence);
        questionExtend.setOrgin(orgin);
        questionExtend.setModuleId(moduleId);
        long endExtend = System.currentTimeMillis();
        logger.info("组装扩展表={}",endExtend-startExtend);

        QuestionFull questionFull = QuestionFull.builder()
                .question(question)
                .questionExtend(questionExtend)
                .build();
        long end = System.currentTimeMillis();
        logger.info("组装单一客观题用时={}",end-start);
        return questionFull;
    }

    public boolean isNumeric(Object obj){
        String str = obj.toString();
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
}
