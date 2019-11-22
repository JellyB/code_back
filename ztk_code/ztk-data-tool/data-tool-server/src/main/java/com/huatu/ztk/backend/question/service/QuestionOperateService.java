package com.huatu.ztk.backend.question.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.paper.bean.BackendPaperStatus;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.service.PaperQuestionService;
import com.huatu.ztk.backend.paper.service.PracticeService;
import com.huatu.ztk.backend.question.bean.QuestionFull;
import com.huatu.ztk.backend.question.bean.QuestionPointTreeMin;
import com.huatu.ztk.backend.question.dao.PointDao;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.backend.question.dao.QuestionOperateDao;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.common.QuestionType;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import com.itextpdf.text.BadElementException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-12  20:17 .
 */
@Service
public class QuestionOperateService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PaperQuestionService paperQuestionService;
    @Autowired
    private QuestionOperateDao questionOperateDao;
    @Autowired
    private PointDao pointDao;
//    @Autowired
//    private QuestionDubboService questionDubboService;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 根据传输过来的json，添加单一客观题
     * @param str,account,uid
     */
    public Object addGenericObjectQuestion(String str,String account,long uid) throws BizException, IOException, BadElementException, IllegalQuestionException {
        int qid = questionOperateDao.findId(1);
        QuestionFull questionFull = assembleGenericObjectQuestion(str,account,uid);
        GenericQuestion question = (GenericQuestion) questionFull.getQuestion();
        QuestionExtend questionExtend = questionFull.getQuestionExtend();
        question.setId(qid);//为question分配id
        question.setParent(0);//为question设置父节点
        questionExtend.setQid(qid);//为questionExtend分配id
        if(question.getMode()== PaperType.TRUE_PAPER){
            paperQuestionService.judgeDuplication(questionExtend.getPaperId(),Math.round(questionExtend.getSequence()));
        }
        questionOperateDao.insert(question);
        questionOperateDao.insertExtend(questionExtend);
        long start = System.currentTimeMillis();
        if(question.getMode()== PaperType.TRUE_PAPER){
            paperQuestionService.insertQuestion(questionExtend);
        }else  {
            practiceService.addQuestion2Paper(questionExtend.getPaperId(),questionExtend.getModuleId(),qid);
            if(question.getStatus()==QuestionStatus.AUDIT_SUCCESS){
//                questionDubboService.update(question);//Dubbo中更新试题
                questionService.updateQuestion(question,-1);
            }
        }
        long end = System.currentTimeMillis();
        logger.info("插入试卷题序用时={}",end-start);
        return qid;
    }

    /**
     * 组装单一客观题
     * @param str
     * @param account
     * @param uid
     * @return
     * @throws BadElementException
     * @throws BizException
     * @throws IOException
     */
    public QuestionFull assembleGenericObjectQuestion(String str,String account,long uid) throws BadElementException, BizException, IOException {
        long start = System.currentTimeMillis();
        Map<String,Object> result = JsonUtil.toMap(str);
        logger.info("resule={}",result);

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
        String material = "";//材料
        List<String> materials = (List<String>) result.get("materials") ;//材料列表
        long createTime = (long) System.currentTimeMillis();//创建时间
        int mode = -1;//试题的模型类型
        int status = -1;//试题状态
        int subject = -1;//考试科目
        int year = -1;//年份
        String from = null;//来源
        int area = -1;//地区id
        String teachType = null;//教研题型
        int paperId = -1;//试卷Id
        long endDefine = System.currentTimeMillis();
        logger.info("定义用时用时={}",endDefine-start);


        if(result.get("type")!=null){
            type = Integer.parseInt(String.valueOf(result.get("type")));
        }
       if(result.get("pointsName")!=null&&result.get("pointsId")!=null){
           pointsName = (List<String>) result.get("pointsName");
           pointsId = (List<Integer>) result.get("pointsId");
           logger.info("pointsNameNew={},pointsId={}",pointsName,pointsId);
        }
        long endFindPoint = System.currentTimeMillis();
        logger.info("查找知识点用时={}",endFindPoint-endDefine);
        if(result.get("difficult")!=null){
            difficult = Integer.parseInt(String.valueOf(result.get("difficult")));
        }
        if(result.get("stem")!=null){
            stem = String.valueOf(result.get("stem"));
            stem = questionService.imgManage(stem,account,0);
            stem = questionService.htmlManage(stem);
        }
        long endFindStem = System.currentTimeMillis();
        logger.info("题干用时={}",endFindStem-endFindStem);
        if(result.get("options")!=null){
            List<Map<String,Object>> options = (List<Map<String,Object>>) result.get("options");
            for(int i=0;i<options.size();i++){
                String con = String.valueOf(options.get(i).get("con"));//选项内容
                int isSelect = Integer.parseInt(String.valueOf(options.get(i).get("isSelect"))) ;
                if(isSelect==1){
                    answer = answer*10+i+1;//若该选项是答案的一部分，加入
                }
                con = questionService.imgManage(con,account,0);
                con = questionService.htmlManage(con);
                choices.add(con);
            }
        }
        long endOptions = System.currentTimeMillis();
        logger.info("选项用时={}",endOptions-endFindStem);
        if(result.get("analysis")!=null){
            analysis = String.valueOf(result.get("analysis")) ;
            analysis = questionService.imgManage(analysis,account,0);
            analysis = questionService.htmlManage(analysis);
        }
        long endAnalysis = System.currentTimeMillis();
        logger.info("选项用时={}",endAnalysis-endOptions);
        if(result.get("score")!=null){
            score = Float.parseFloat(String.valueOf(result.get("score")));
        }
        if(result.get("materials")!=null){
            materials = (List<String>) result.get("materials");
            for(int i=0;i<materials.size();i++){
                String ml = materials.get(i);
                ml = questionService.htmlManage(ml);
                ml = questionService.imgManage(ml,account,0);
                material +=ml;
                materials.set(i,ml);
            }
        }
        long endMaterials = System.currentTimeMillis();
        logger.info("材料用时={}",endMaterials-endAnalysis);
        if(result.get("paper")!=null){
            Map<String,Object> paper = (Map<String, Object>) result.get("paper");
            mode = Integer.parseInt(String.valueOf(paper.get("type")));
            int paperStatus = Integer.parseInt(String.valueOf(paper.get("status")));
            if(mode== PaperType.CUSTOM_PAPER&&(paperStatus== BackendPaperStatus.ONLINE
                    ||paperStatus==BackendPaperStatus.AUDIT_SUCCESS||paperStatus==BackendPaperStatus.OFFLINE
                    ||paperStatus== BackendPaperStatus.ING)){//若为模拟卷，且试卷状态为审核后状态，试题添加试题状态直接变为审核成功
                status = QuestionStatus.AUDIT_SUCCESS;
            }else{
                status = QuestionStatus.CREATED;
            }
            subject = Integer.parseInt(String.valueOf(paper.get("catgory")));
            year = Integer.parseInt(String.valueOf(paper.get("year")));
            from = String.valueOf(paper.get("name"));
            area = Integer.parseInt(String.valueOf(paper.get("area")));
            paperId = Integer.parseInt(String.valueOf(paper.get("id")));
        }
        long endPaper = System.currentTimeMillis();
        logger.info("材料用时={}",endPaper-endMaterials);
        if(result.get("teachType")!=null){teachType = String.valueOf(result.get("teachType"));}

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
        if(result.get("expand")!=null){
            expand = String.valueOf(result.get("expand"));
            expand = questionService.htmlManage(expand);
            expand = questionService.imgManage(expand,account,0);
        }
        question.setExtend(expand);
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
        if(result.get("moduleId")!=null){
            moduleId = Integer.parseInt(String.valueOf(result.get("moduleId")));
        }

        if(result.get("orgin")!=null){
            orgin = String.valueOf(result.get("orgin"));
            orgin = questionService.htmlManage(orgin);
            orgin = questionService.imgManage(orgin,account,0);
        }
        if(result.get("author")!=null){
            author = String.valueOf(result.get("author"));
        }
        if(result.get("review")!=null){
            review = String.valueOf(result.get("review"));
        }
        if(result.get("sequence")!=null){
            sequence = Float.parseFloat(String.valueOf((result.get("sequence"))));
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


    /**
     * 根据传输过来的json，添加单一主观题
     * @param str
     * @param account
     * @param uid
     * @return
     */
    public Object addGenericSubjectiveQuestion(String str,String account,long uid) throws BizException, IOException, BadElementException, IllegalQuestionException {

        int qid = questionOperateDao.findId(1);
        QuestionFull questionFull = assembleGenericSubjectQuestion(str,account,uid);
        GenericSubjectiveQuestion question = (GenericSubjectiveQuestion) questionFull.getQuestion();
        QuestionExtend questionExtend = questionFull.getQuestionExtend();
        question.setId(qid);//为question分配id
        question.setParent(0);//为question设置父节点
        questionExtend.setQid(qid);//为questionExtend分配id
        if(question.getMode()== PaperType.TRUE_PAPER){
            paperQuestionService.judgeDuplication(questionExtend.getPaperId(),Math.round(questionExtend.getSequence()));
        }
        questionOperateDao.insert(question);
        questionOperateDao.insertExtend(questionExtend);
        if(question.getMode()== PaperType.TRUE_PAPER){
            paperQuestionService.insertQuestion(questionExtend);
        }else  {
            practiceService.addQuestion2Paper(questionExtend.getPaperId(),questionExtend.getModuleId(),qid);
            if(question.getStatus()==QuestionStatus.AUDIT_SUCCESS){
                questionService.updateQuestion(question,-1);
            }
        }

        return qid;
    }

    /**
     * 组装单一主观题
     * @param str
     * @param account
     * @param uid
     * @return
     * @throws BadElementException
     * @throws BizException
     * @throws IOException
     */
    public QuestionFull assembleGenericSubjectQuestion(String str,String account,long uid) throws BadElementException, BizException, IOException {
        long start = System.currentTimeMillis();
        Map<String,Object> result = JsonUtil.toMap(str);

        //question所有字段
        String answerRequire = null ;//答题要求
        String referAnalysis = null ;//参考解析，作为参考答案
        String examPoint = null ;//审题要求
        List<String> materials = new ArrayList<>() ;//材料
        String material = "";
        String require = null ;//题目要求
        float score = 0 ;//分数
        String scoreExplain = null ;//赋分说明
        String solvingIdea = null ;//解题思路
        String stem = null;//题干
        long creatTime = (long) System.currentTimeMillis();
        int mode = -1;//试题的模型类型
        int status = -1;//试题状态
        int subject = -1;//考试科目
        int year = -1;//年份
        String from = null;//来源
        int area = -1;//地区id
        String teachType = null;//教研题型
        int paperId = -1;//试卷Id
        int maxWordCount = -1;//最大字数限制
        int minWordCount = -1;//最小字数限制


        if(result.get("answerRequire")!=null){
            answerRequire = String.valueOf(result.get("answerRequire"));
            answerRequire = questionService.htmlManage(answerRequire);
            answerRequire = questionService.imgManage(answerRequire,account,0);
        }
        if(result.get("referAnalysis")!=null){
            referAnalysis = String.valueOf(result.get("referAnalysis"));
            referAnalysis = questionService.htmlManage(referAnalysis);
            referAnalysis = questionService.imgManage(referAnalysis,account,0);
        }
        if(result.get("examPoint")!=null){
            examPoint = String.valueOf(result.get("examPoint"));
            examPoint = questionService.htmlManage(examPoint);
            examPoint = questionService.imgManage(examPoint,account,0);
        }
        if(result.get("materials")!=null){
            materials = (List<String>) result.get("materials");
            for(int i=0;i<materials.size();i++){
                String ml = materials.get(i);
                ml = questionService.htmlManage(ml);
                ml = questionService.imgManage(ml,account,0);
                material +=ml;
                materials.set(i,ml);
            }
        }
        if(result.get("require")!=null){
            require = String.valueOf(result.get("require"));
            require = questionService.htmlManage(require);
            require = questionService.imgManage(require,account,0);
        }
        if(result.get("score")!=null){
            score = Float.parseFloat(String.valueOf(result.get("score")));
        }
        if(result.get("scoreExplain")!=null){
            scoreExplain = String.valueOf(result.get("scoreExplain"));
            scoreExplain = questionService.htmlManage(scoreExplain);
            scoreExplain = questionService.imgManage(scoreExplain,account,0);
        }
        if(result.get("solvingIdea")!=null){
            solvingIdea = String.valueOf(result.get("solvingIdea"));
            solvingIdea = questionService.htmlManage(solvingIdea);
            solvingIdea = questionService.imgManage(solvingIdea,account,0);
        }
        if(result.get("stem")!=null){
            stem = String.valueOf(result.get("stem"));
            stem = questionService.htmlManage(stem);
            stem = questionService.imgManage(stem,account,0);
        }
        if(result.get("paper")!=null){
            Map<String,Object> paper = (Map<String, Object>) result.get("paper");

            int paperStatus = Integer.parseInt(String.valueOf(paper.get("status")));
            if(mode== PaperType.CUSTOM_PAPER&&(paperStatus== BackendPaperStatus.ONLINE
                    ||paperStatus==BackendPaperStatus.AUDIT_SUCCESS||paperStatus==BackendPaperStatus.OFFLINE
                    ||paperStatus== BackendPaperStatus.ING)){//若为模拟卷，且试卷状态为审核后状态，试题添加试题状态直接变为审核成功
                status = QuestionStatus.AUDIT_SUCCESS;
            }else{
                status = QuestionStatus.CREATED;
            }
            subject = Integer.parseInt(String.valueOf(paper.get("catgory")));
            year = Integer.parseInt(String.valueOf(paper.get("year")));
            from = String.valueOf(paper.get("name"));
            area = Integer.parseInt(String.valueOf(paper.get("area")));
            paperId = Integer.parseInt(String.valueOf(paper.get("id")));
            mode = Integer.parseInt(String.valueOf(paper.get("type")));
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
        genericSubjectiveQuestion.setMaxWordCount(maxWordCount);
        genericSubjectiveQuestion.setMinWordCount(minWordCount);

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
     * 根据传输过来的json，添加复合客观题
     */
    public void addCompositeObjectiveQuestion(String str,String account,long uid) throws BizException, IOException, BadElementException, IllegalQuestionException {

        List<Question> questions = new ArrayList<>();
        List<QuestionExtend> questionExtends = new ArrayList<>();
        QuestionFull questionFull = assembleCompositeObjectiveQuestion(str,account,uid);
        CompositeQuestion question = (CompositeQuestion) questionFull.getQuestion();
        QuestionExtend questionExtend = questionFull.getQuestionExtend();
        List<QuestionFull> subQuestionFulls = questionFull.getSubQuestions();
        int num = subQuestionFulls.size();
        int qid = questionOperateDao.findId(num+1);
        List<Integer> subQuestionIds = new ArrayList<>();
        question.setId(qid);//为question分配id
        questionExtend.setQid(qid);//为questionExtend分配id
        long startP = System.currentTimeMillis();
        float sequence = questionExtend.getSequence();
        if(sequence>0&&question.getMode()== PaperType.TRUE_PAPER){
            paperQuestionService.judgeDuplication(questionExtend.getPaperId(),Math.round(sequence));
        }

        long endP = System.currentTimeMillis();
        logger.info("查看大题题序用时={}",endP-startP);
        logger.info("题序={}",Math.round(questionExtend.getSequence()));
        for(int i=0;i<num;i++){
            int subQid = qid+(i+1);
            subQuestionIds.add(subQid);
            GenericQuestion subQuestion = (GenericQuestion) subQuestionFulls.get(i).getQuestion();
            QuestionExtend subQuestionExtend = subQuestionFulls.get(i).getQuestionExtend();
            subQuestion.setId(subQid);//为question分配id
            subQuestion.setParent(qid);//为question设置父节点
            subQuestionExtend.setQid(subQid);//为questionExtend分配id
            long start = System.currentTimeMillis();
            if(sequence<0&&question.getMode()== PaperType.TRUE_PAPER){
                paperQuestionService.judgeDuplication(subQuestionExtend.getPaperId(),Math.round(subQuestionExtend.getSequence()));
            }
            long end = System.currentTimeMillis();
            logger.info("查看题序用时={}",end-start);
            questions.add(subQuestion);
            questionExtends.add(subQuestionExtend);
        }
        question.setQuestions(subQuestionIds);
        logger.info("id={},subQids={}",qid,subQuestionIds);
        questions.add(question);
        questionExtends.add(questionExtend);
        questionOperateDao.insertAll(questions);
        questionOperateDao.insertExtendAll(questionExtends);
        if(question.getStatus()==QuestionStatus.AUDIT_SUCCESS){
//            questionDubboService.update(question);//Dubbo中更新试题
            questionService.updateQuestion(question,-1);
        }
        long start = System.currentTimeMillis();
        for(int i=0;i<num;i++){
            QuestionExtend subQuestionExtend = subQuestionFulls.get(i).getQuestionExtend();
            if(question.getMode()== PaperType.TRUE_PAPER){
                paperQuestionService.insertQuestion(subQuestionExtend);
                logger.info("第"+i+"个子题的id={},extend={}",subQuestionExtend.getQid(),subQuestionExtend);
            }else  {
                practiceService.addQuestion2Paper(subQuestionExtend.getPaperId(),subQuestionExtend.getModuleId(),subQuestionExtend.getQid());
                logger.info("第"+i+"个子题的id={},extend={}",subQuestionExtend.getQid(),subQuestionExtend);
                if(subQuestionFulls.get(i).getQuestion().getStatus()==QuestionStatus.AUDIT_SUCCESS){
//                    questionDubboService.update(subQuestionFulls.get(i).getQuestion());//Dubbo中更新试题
                    questionService.updateQuestion(subQuestionFulls.get(i).getQuestion(),-1);
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("查看试卷总用时={}",end-start);
    }


    public QuestionFull assembleCompositeObjectiveQuestion(String str,String account,long uid) throws BadElementException, BizException, IOException {
        long start = System.currentTimeMillis();
        Map<String,Object> result = JsonUtil.toMap(str);

        //question所有字段
        String material = String.valueOf(result.get("material"));//材料
        List<String> materials = Lists.newArrayList();
        float score = 0 ;//分数
        int difficult = -1;//难度
        long creatTime = (long) System.currentTimeMillis();
        int mode = -1;//试题的模型类型
        int status = -1;//试题状态
        int subject = -1;//考试科目
        int year = -1;//年份
        String from = null;//来源
        int area = -1;//地区id
        int paperId = -1;//试卷Id



        if(result.get("material")!=null){
            material = String.valueOf(result.get("material"));
            material = questionService.htmlManage(material);
            material = questionService.imgManage(material,account,0);
            materials.add(material);
        }
        if(result.get("score")!=null){
            score = Float.parseFloat(String.valueOf(result.get("score")));
        }
        if(result.get("difficult")!=null){
            difficult = Integer.parseInt(String.valueOf(result.get("difficult")));
        }
        if(result.get("paper")!=null){
            Map<String,Object> paper = (Map<String, Object>) result.get("paper");
            year = Integer.parseInt(String.valueOf(paper.get("year")));
            from = String.valueOf(paper.get("name"));
            area = Integer.parseInt(String.valueOf(paper.get("area")));
            paperId = Integer.parseInt(String.valueOf(paper.get("id")));
            int paperStatus = Integer.parseInt(String.valueOf(paper.get("status")));
            if(mode== PaperType.CUSTOM_PAPER&&(paperStatus== BackendPaperStatus.ONLINE
                    ||paperStatus==BackendPaperStatus.AUDIT_SUCCESS||paperStatus==BackendPaperStatus.OFFLINE
                    ||paperStatus== BackendPaperStatus.ING)){//若为模拟卷，且试卷状态为审核后状态，试题添加试题状态直接变为审核成功
                status = QuestionStatus.AUDIT_SUCCESS;
            }else{
                status = QuestionStatus.CREATED;
            }
            subject = Integer.parseInt(String.valueOf(paper.get("catgory")));
            mode = Integer.parseInt(String.valueOf(paper.get("type")));
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
            orgin = questionService.htmlManage(orgin);
            orgin = questionService.imgManage(orgin,account,0);
        }

        QuestionExtend questionExtend = new QuestionExtend();
        questionExtend.setPaperId(paperId);
        questionExtend.setAuthor("");
        questionExtend.setReviewer("");
//        questionExtend.setExtend("");
        questionExtend.setSequence(sequence);
        questionExtend.setOrgin(orgin);
        questionExtend.setModuleId(moduleId);

        //subQuestion 字段
        List<String> subQuestionstr = new ArrayList<>();
        List<QuestionFull> subQuestions = new ArrayList<>();

        long startAssemble = System.currentTimeMillis();
        if(result.get("subQuestionStr")!=null){
            subQuestionstr = (List<String>) result.get("subQuestionStr");
            for(int i=0;i<subQuestionstr.size();i++){
                long startAssembleI = System.currentTimeMillis();
                subQuestions.add(assembleGenericObjectQuestion(subQuestionstr.get(i),account,uid));
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


    /**
     *根据传输过来的json，添加复合主观题
     * @param str
     * @param account
     * @param uid
     */
    public void addCompositeSubjectiveQuestion(String str,String account,long uid) throws BizException, IOException, BadElementException, IllegalQuestionException {
        List<Question> questions = new ArrayList<>();
        List<QuestionExtend> questionExtends = new ArrayList<>();
        QuestionFull questionFull = assembleCompositeSubjectiveQuestion(str,account,uid);
        CompositeSubjectiveQuestion question = (CompositeSubjectiveQuestion) questionFull.getQuestion();
        QuestionExtend questionExtend = questionFull.getQuestionExtend();
        List<QuestionFull> subQuestionFulls = questionFull.getSubQuestions();
        int num = subQuestionFulls.size();
        int qid = questionOperateDao.findId(num+1);
        List<Integer> subQuestionIds = new ArrayList<>();
        question.setId(qid);//为question分配id
        questionExtend.setQid(qid);//为questionExtend分配id
        long startP = System.currentTimeMillis();
        float sequence = questionExtend.getSequence();
        if(sequence>0&&question.getMode()== PaperType.TRUE_PAPER){
            paperQuestionService.judgeDuplication(questionExtend.getPaperId(),Math.round(sequence));
        }
        long endP = System.currentTimeMillis();
        logger.info("查看大题题序用时={}",endP-startP);
        logger.info("题序={}",Math.round(questionExtend.getSequence()));
        for(int i=0;i<num;i++){
            int subQid = qid+(i+1);
            subQuestionIds.add(subQid);
            if(subQuestionFulls.get(i).getQuestion().getType()==QuestionType.SINGLE_SUBJECTIVE){
                GenericSubjectiveQuestion subQuestion = (GenericSubjectiveQuestion) subQuestionFulls.get(i).getQuestion();
                QuestionExtend subQuestionExtend = subQuestionFulls.get(i).getQuestionExtend();
                subQuestion.setId(subQid);//为question分配id
                subQuestion.setParent(qid);//为question设置父节点
                subQuestionExtend.setQid(subQid);//为questionExtend分配id
                long start = System.currentTimeMillis();
                if(sequence<0&&question.getMode()== PaperType.TRUE_PAPER){
                    paperQuestionService.judgeDuplication(subQuestionExtend.getPaperId(),Math.round(subQuestionExtend.getSequence()));
                }
                long end = System.currentTimeMillis();
                logger.info("查看题序用时={}",end-start);
                questions.add(subQuestion);
                questionExtends.add(subQuestionExtend);
            }else{
                GenericQuestion subQuestion = (GenericQuestion) subQuestionFulls.get(i).getQuestion();
                QuestionExtend subQuestionExtend = subQuestionFulls.get(i).getQuestionExtend();
                subQuestion.setId(subQid);//为question分配id
                subQuestion.setParent(qid);//为question设置父节点
                subQuestionExtend.setQid(subQid);//为questionExtend分配id
                long start = System.currentTimeMillis();
                if(sequence<0&&question.getMode()== PaperType.TRUE_PAPER){
                    paperQuestionService.judgeDuplication(subQuestionExtend.getPaperId(),Math.round(subQuestionExtend.getSequence()));
                }
                long end = System.currentTimeMillis();
                logger.info("查看题序用时={}",end-start);
                questions.add(subQuestion);
                questionExtends.add(subQuestionExtend);
            }

        }
        question.setQuestions(subQuestionIds);
        questions.add(question);
        questionExtends.add(questionExtend);
        questionOperateDao.insertAll(questions);
        questionOperateDao.insertExtendAll(questionExtends);
        if(question.getStatus()==QuestionStatus.AUDIT_SUCCESS){
//            questionDubboService.update(question);//Dubbo中更新试题
            questionService.updateQuestion(question,-1);
        }
        long start = System.currentTimeMillis();
        for(int i=0;i<num;i++){
            QuestionExtend subQuestionExtend = subQuestionFulls.get(i).getQuestionExtend();
            if(question.getMode()== PaperType.TRUE_PAPER){
                paperQuestionService.insertQuestion(subQuestionExtend);
            }else  {
                practiceService.addQuestion2Paper(subQuestionExtend.getPaperId(),subQuestionExtend.getModuleId(),subQuestionExtend.getQid());
                if(subQuestionFulls.get(i).getQuestion().getStatus()==QuestionStatus.AUDIT_SUCCESS){
//                    questionDubboService.update(subQuestionFulls.get(i).getQuestion());//Dubbo中更新试题
                    questionService.updateQuestion(subQuestionFulls.get(i).getQuestion(),-1);
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("查看试卷总用时={}",end-start);
    }


    public QuestionFull assembleCompositeSubjectiveQuestion(String str,String account,long uid) throws BadElementException, BizException, IOException {
        long start = System.currentTimeMillis();
        Map<String,Object> result = JsonUtil.toMap(str);

        //question的所有字段
        List<String> materials = new ArrayList<>() ;//材料
        String material = "";
        String require = null;
        long creatTime = (long) System.currentTimeMillis();
        int mode = -1;//试题的模型类型
        int status = -1;//试题状态
        int subject = -1;//考试科目
        int year = -1;//年份
        String from = null;//来源
        int area = -1;//地区id
        int paperId = -1;//试卷Id

        if(result.get("materials")!=null){
            materials = (List<String>) result.get("materials");
            for(int i=0;i<materials.size();i++){
                String ml = materials.get(i);
                ml = questionService.htmlManage(ml);
                ml = questionService.imgManage(ml,account,0);
                material +=ml;
                materials.set(i,ml);
            }
        }
        if(result.get("require")!=null){
            require = String.valueOf(result.get("require"));
            require = questionService.htmlManage(require);
            require = questionService.imgManage(require,account,0);
        }
        if(result.get("paper")!=null){
            Map<String,Object> paper = (Map<String, Object>) result.get("paper");
            year = Integer.parseInt(String.valueOf(paper.get("year")));
            paperId = Integer.parseInt(String.valueOf(paper.get("id")));
            int paperStatus = Integer.parseInt(String.valueOf(paper.get("status")));
            if(mode== PaperType.CUSTOM_PAPER&&(paperStatus== BackendPaperStatus.ONLINE
                    ||paperStatus==BackendPaperStatus.AUDIT_SUCCESS||paperStatus==BackendPaperStatus.OFFLINE
                    ||paperStatus== BackendPaperStatus.ING)){//若为模拟卷，且试卷状态为审核后状态，试题添加试题状态直接变为审核成功
                status = QuestionStatus.AUDIT_SUCCESS;
            }else{
                status = QuestionStatus.CREATED;
            }
            subject = Integer.parseInt(String.valueOf(paper.get("catgory")));
            from = String.valueOf(paper.get("name"));
            area = Integer.parseInt(String.valueOf(paper.get("area")));
            mode = Integer.parseInt(String.valueOf(paper.get("type")));
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
        List<String> subQuestionstr = new ArrayList<>();
        List<QuestionFull> subQuestions = new ArrayList<>();
        List<Integer> subQuestionsType = new ArrayList<>();

        long startAssemble = System.currentTimeMillis();
        if(result.get("subQuestionStr")!=null&&result.get("subQuestionsType")!=null){
            subQuestionstr = (List<String>) result.get("subQuestionStr");
            subQuestionsType = (List<Integer>) result.get("subQuestionsType");
            for(int i=0;i<subQuestionstr.size();i++){
                if(subQuestionsType.get(i)==1){//1为主观题
                    subQuestions.add(assembleGenericSubjectQuestion(subQuestionstr.get(i),account,uid));
                }else{
                    subQuestions.add(assembleGenericObjectQuestion(subQuestionstr.get(i),account,uid));
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

    public void operatesTemp(){
        /*List<QuestionPointTreeMin> points = pointDao.findAllPonitsBySubjectForCopy(1);
        List<QuestionPointTreeMin> pointTree = questionService.toTree(points);
        logger.info("points的长度={}，树的长度={}",points.size(),pointTree.size());

        List<Integer> subjects =  new ArrayList(Arrays.asList(100100127,100100128,
                100100129,
                100100130,
                100100131,
                100100134,
                100100135,
                100100136,
                100100137,
                100100138,
                100100139,
                100100140,
                100100141,
                100100142,
                100100143,
                100100144,
                100100145,
                100100146,
                100100147,
                100100148,
                100100149,
                100100150,
                100100151,
                100100152,
                100100153,
                100100154,
                100100155,
                100100156,
                100100157,
                100100158,
                100100159,
                100100160,
                100100161,
                100100162,
                100100163));
        List<Integer> subjects =  new ArrayList(Arrays.asList(3));
        logger.info("subjects长度={}，subjects={}",subjects.size(),subjects);
        for(int i=0;i<subjects.size();i++){
            operateTemp(subjects.get(i),pointTree);
        }*/
        //logger.info("试卷问题试题={}",allQuestionNoPoint());
        errorPoints();
    }

    public void errorPoints(){
        List<QuestionPointTreeMin> points = pointDao.findErrorPoints();
        logger.info("出现问题的知识点数量={}",points.size());

        if(CollectionUtils.isNotEmpty(points)){
            for(QuestionPointTreeMin point:points){

                pointDao.correctPoint(point);
            }
        }
    }

    public Map<String,List<Integer>> allQuestionNoPoint(){
        Map<String,List<Integer>> allQuestions = new HashMap<>();
        List<Question> questions = questionDao.filterNoPoint();
        logger.info("问题试题总数={}",questions.size());
        for(Question qs:questions){
            QuestionExtend qe = questionDao.filterNoPointExtend(qs.getId());
            int paperId = qe.getPaperId();
            Paper paper = paperDao.findByIdCopy(paperId);
            logger.info("paper={},qid={}",paper,qs.getId());
            String paperName = paper.getName();
            if(allQuestions.containsKey(paperName)){
                List<Integer> qids = allQuestions.get(paperName);
                qids.add(qs.getId());
                allQuestions.put(paperName,qids);
            }else{
                List<Integer> qids = new ArrayList<>();
                qids.add(qs.getId());
                allQuestions.put(paperName,qids);
            }
        }
        return allQuestions;
    }

    public void operateTemp(int subject,List<QuestionPointTreeMin> pointTree){
        logger.info("subject={}",subject);
        for(int i=0;i<pointTree.size();i++){
            QuestionPointTreeMin pointOne = pointTree.get(i);
            int idOne = pointDao.insertPointForint(pointOne.getName(),pointOne.getParent(),pointOne.getLevel(),subject);
            for(int j=0;j<pointOne.getChildren().size();j++){
                QuestionPointTreeMin pointTwo = pointOne.getChildren().get(j);
                logger.info("pointTwo={}",pointTwo);
                int idTwo = pointDao.insertPointForint(pointTwo.getName(),idOne,pointTwo.getLevel(),subject);
                for(int k=0;k<pointTwo.getChildren().size();k++){
                    QuestionPointTreeMin pointThree = pointTwo.getChildren().get(k);
                    pointDao.insertPointForint(pointThree.getName(),idTwo,pointThree.getLevel(),subject);
                }
            }
        }
    }


    /**
     * 过滤null
     */
    public void filterNull() {
        List<Question> questions = questionDao.filterNull();
        int len = questions.size();
        for(int i=0;i<len;i++){
            Question question = questions.get(i);
            logger.info("变化前的id={}，材料={}",question.getId(),question.getMaterial());
            List<String> materials = question.getMaterials();
            logger.info("材料数={}，材料s={}",materials.size(),materials);
            String material = "";
            for(int j=0;j<materials.size();j++){
                material+=materials.get(j);
            }
            logger.info("变换后的材料={}",material);
            questionDao.editMaterial(question.getId(),material);
        }
        logger.info("出现问题的questions共有={}",len);
    }




}
