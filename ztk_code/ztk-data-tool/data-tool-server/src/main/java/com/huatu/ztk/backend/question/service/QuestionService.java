package com.huatu.ztk.backend.question.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paper.bean.PaperQuestionBean;
import com.huatu.ztk.backend.paper.bean.TikuQuestionType;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.service.PaperQuestionService;
import com.huatu.ztk.backend.question.bean.*;
import com.huatu.ztk.backend.question.common.error.QuestionError;
import com.huatu.ztk.backend.question.dao.PointDao;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.backend.util.DateFormat;
import com.huatu.ztk.backend.util.FuncStr;
import com.huatu.ztk.backend.util.UploadFileUtil;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.knowledge.common.QuestionPointLevel;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import com.itextpdf.text.BadElementException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



/**
 * Author: xuhuiqiang
 * Time: 2016-12-22  20:46 .
 */
@Service
public class QuestionService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private PointDao pointDao;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private UploadFileUtil uploadFileUtil;
    @Autowired
    private PaperQuestionService paperQuestionService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    MarkModify markModify = null;
    //修改的元素
    List<String> modifyAttribute = null;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 判读题序是否已经被占用了
     * @param paperId
     * @param sequence
     * @throws BizException
     */
    public void judgeDuplication(int paperId,int sequence) throws BizException {
        paperQuestionService.judgeDuplication(paperId,sequence);
    }

    /**
     * 根据搜索条件，查找试题列表（支持各种类型试题）
     * @param subject
     * @param module
     * @param area
     * @param year
     * @param databaseQuestionType
     * @param stem
     * @param questionId
     * @return
     */
    public Object findByDetail(int subject,int module,String area,int year,int mode,int databaseQuestionType,String stem,int questionId){
        List<QuestionMin> questionMins = new ArrayList<>();
        List<QuestionExtend> questionExtends = Lists.newArrayList();
        List<Question> questions = Lists.newArrayList();
        List<Integer> areas = Lists.newArrayList();
        int parent = 0;
        if(area.length()>0){
            String[] areasArray = area.split(",");
            for(int i=0,len=areasArray.length;i<len;i++){
                areas.add(Integer.parseInt(areasArray[i]));
            }
        }
        List<Integer> types = Lists.newArrayList();
        switch (databaseQuestionType){
            case TikuQuestionType.SINGLE_OBJECTIVE : types = Arrays.asList(99, 100, 101, 109); parent = 0; break;
            case TikuQuestionType.MULTI_OBJECTIVE : types = Arrays.asList(105); parent = -1;  break;
            case TikuQuestionType.SINGLE_SUBJECTIVE : types = Arrays.asList(106); parent = 0;  break;
            case TikuQuestionType.MULTI_SUBJECTIVE : types = Arrays.asList(107); parent = -1; break;
        }
        List<Integer> questionIds = Lists.newArrayList();
        if(questionId>-1){//若输入试题id，直接查询试题id
            questionIds.add(questionId);
            questions = questionDao.findByDetail(subject,areas,year,mode,types,stem,questionIds,parent);
            logger.info("subject={},areas={}",subject,areas);
            logger.info("year={},mode={}",year,mode);
            logger.info("types={},stem={}",types,stem);
            logger.info("questionIds={},parent={}",questionIds,parent);
            logger.info("questions={}",questions);
            questionExtends = questionDao.findExtendByIds(questionIds);
            if(CollectionUtils.isNotEmpty(questionExtends)&&CollectionUtils.isNotEmpty(questions)){
                int modeExtend = questionExtends.get(0).getModuleId();
                if(module==-1||modeExtend==module){
                    questionMins = (List<QuestionMin>) assemblequestinMins(questions,questionExtends);
                }
            }
        }else{//没有输入试题Id
            if(module!=-1){//条件中包括模块
                questionExtends = questionDao.findExtendByModule(module);
                if (CollectionUtils.isNotEmpty(questionExtends)) {
                    for (QuestionExtend questionExtend : questionExtends) {
                        questionIds.add(questionExtend.getQid());
                    }
                }

                questions = questionDao.findByDetail(subject,areas,year,mode,types,stem,questionIds,parent);
                if(CollectionUtils.isNotEmpty(questionExtends)&&CollectionUtils.isNotEmpty(questions)){
                    questionMins = (List<QuestionMin>) assemblequestinMins(questions,questionExtends);
                }
            }else{//条件中不包括模块
                //TODO 现在大量数据没有extend表，导致数据为空
                questions = questionDao.findByDetail(subject,areas,year,mode,types,stem,questionIds,parent);
                if (CollectionUtils.isNotEmpty(questions)) {
                    for (Question question : questions) {
                        questionIds.add(question.getId());
                    }
                }

                questionExtends = questionDao.findExtendByIds(questionIds);
                if(CollectionUtils.isNotEmpty(questions)){
                    questionMins = (List<QuestionMin>) assemblequestinMins(questions,questionExtends);
                }
            }
        }
        logger.info("符合条件的试题数量为：{}",questionMins.size());
        return questionMins;
    }

    /***
     * 组装questionMins
     * @param questions
     * @param questionExtends
     * @return
     */
    public Object assemblequestinMins(List<Question> questions,List<QuestionExtend> questionExtends){
        List<QuestionMin> questionMins = new ArrayList<>();
        Map<Integer, QuestionExtend> questionExtendMap = new HashMap<>();
        if(questionExtends!=null&&questionExtends.size()!=0){
            questionExtendMap = questionExtends.stream().collect(Collectors.toMap(QuestionExtend::getQid, (questionExtend) -> questionExtend));
        }
        for(Question question:questions){
            String stem = "";
            int difficult = 0;//主观题没有难度，设置为0
            if (question instanceof GenericQuestion) {
                stem = ((GenericQuestion) question).getStem();
                difficult = ((GenericQuestion) question).getDifficult();
            } else if (question instanceof CompositeQuestion) {
                stem = question.getMaterial();
                difficult = 6;
                //TODO 通过子试题计算
                //.difficult(compositeQuestion.getDifficult())
                //暂时TODO试题扩展表数据缺失
            } else if (question instanceof GenericSubjectiveQuestion) {
                stem = ((GenericSubjectiveQuestion) question).getStem();
            } else if (question instanceof CompositeSubjectiveQuestion) {
                stem = ((CompositeSubjectiveQuestion) question).getMaterials().get(0);
            }
            stem = (String) preTreat(stem);
            String areaName = AreaConstants.getFullAreaNmae(question.getArea());
            String format =  "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sf = new SimpleDateFormat(format);
            String creatTime = sf.format(new Date(question.getCreateTime()));
            int moduleId = 0;
            if(!questionExtendMap.isEmpty()&&questionExtendMap.containsKey(question.getId())){//若该试题有extend表
                moduleId = questionExtendMap.get(question.getId()).getModuleId();
            }
            QuestionMin questionMin = QuestionMin.builder()
                    .stem(stem)
                    .difficult(difficult)
                    .id(question.getId())
                    .area(areaName)
                    .mode(question.getMode())
                    .moduleId(moduleId)
                    .type(question.getType())
                    .createTime(creatTime)
                    .channel(question.getChannel())
                    .build();
            questionMins.add(questionMin);
        }
        return questionMins;
    }

    /**
     * 对字符串进行处理，去除html标签
     */
    public String preTreat(String stem){
        if (StringUtils.isNotEmpty(stem)) {
            String regex="<p>|</p>|<br>|</br>|<u>|</u>|<br/>";//去除html中这几种标签
            stem = stem.replaceAll(regex,"");
            stem = stem.replaceAll("<img.*>.*</img>", "【图片】").replaceAll("<img.*/>", "【图片】").replaceAll("<img.*>", "【图片】");//img标签替换为【图片】
            if(stem.length()>60){
                stem = stem.substring(0,30)+"……";
            }
            return stem;
        }

        return "";
    }

    /**
     * 根据传输的questionId，返回试题
     * @param questionId
     * @return
     */
    public Object findById(int questionId){
        return questionDao.findById(questionId).get(0);
    }

    /**
     * 根据传输的questionId，返回试题（支持各种类型）
     * @param questionId
     * @return
     */
    public Object findAllTypeById(int questionId){
        float score = 0;
        Question question = questionDao.findAllTypeById(questionId);
        if(null == question){
            return null;
        }
        QuestionExtend questionExtend = questionDao.findExtendById(questionId);
        if(questionExtend==null){
            questionExtend = new QuestionExtend();
        }
        if(question instanceof GenericQuestion && StringUtils.isNotBlank(((GenericQuestion) question).getExtend())){
            questionExtend.setExtend(((GenericQuestion) question).getExtend());
        }
        List<QuestionDetail> subQuestionsDetail = new ArrayList<>();

        Paper paper = new Paper();
        if(question!=null){
            if(question instanceof CompositeQuestion){
                CompositeQuestion compositeQuestion = (CompositeQuestion) question;
                subQuestionsDetail = (List<QuestionDetail>) findQueetionDetail(compositeQuestion.getQuestions());
            }else if(question instanceof CompositeSubjectiveQuestion){
                CompositeSubjectiveQuestion compositeSubjectiveQuestion = (CompositeSubjectiveQuestion) question;
                subQuestionsDetail = (List<QuestionDetail>) findQueetionDetail(compositeSubjectiveQuestion.getQuestions());
            }else if(question instanceof GenericSubjectiveQuestion){
                GenericSubjectiveQuestion genericSubjectiveQuestion = (GenericSubjectiveQuestion) question;
                score = genericSubjectiveQuestion.getScore();
            }else if(question instanceof GenericQuestion){
                GenericQuestion genericQuestion = (GenericQuestion) question;
                score = genericQuestion.getScore();
            }
            if(questionExtend!=null){
                if(paperDao.findById(questionExtend.getPaperId())!=null){
                    paper = paperDao.findById(questionExtend.getPaperId());
                }
            }
        }
        QuestionDetail questionDetail =  QuestionDetail.builder()
                .question(question)
                .questionExtend(questionExtend)
                .subQuestions(subQuestionsDetail)
                .paper(paper)
                .score(score)
                .status(question.getStatus())
                .build();
        return questionDetail;
    }

    /**
     * 根据ids列表，组装SubQuestionDetail列表
     * @param ids
     * @return
     */
    public Object findQueetionDetail(List<Integer> ids){
        List<QuestionDetail> questionDetails = new ArrayList<>();
        if(CollectionUtils.isEmpty(ids)){
            return questionDetails;
        }
        float score = 0;
        for(int i=0;i<ids.size();i++){
            Question question = questionDao.findAllTypeById(i);
            if(null != question){
                if(question instanceof GenericQuestion){
                    GenericQuestion genericQuestion = (GenericQuestion) question;
                    score = genericQuestion.getScore();
                }else if(question instanceof GenericSubjectiveQuestion){
                    GenericSubjectiveQuestion genericSubjectiveQuestion = (GenericSubjectiveQuestion) question;
                    score = genericSubjectiveQuestion.getScore();
                }
            }
            QuestionExtend questionExtend = new QuestionExtend();
            if(questionDao.findExtendById(ids.get(i))!=null){
                questionExtend = questionDao.findExtendById(ids.get(i));
            }
            Paper paper = paperDao.findById(questionExtend.getPaperId());
            QuestionDetail questionDetail =  QuestionDetail.builder()
                    .question(question)
                    .questionExtend(questionExtend)
                    .paper(paper)
                    .score(score)
                    .build();
            questionDetails.add(questionDetail);
        }
        return questionDetails;
    }



    /**
     * 获得知识点树
     * @return
     */
    public Object findPointTree(){
        return toTree(pointDao.findAllPonits());
    }

    /**
     * 根据科目，获得知识点点树
     * @param subject
     * @return
     */
    public Object findPointTreeBySubject(int subject){
        return toTree(pointDao.findAllPonitsBySubject(subject));
    }

    /**
     * 删除题
     * @param questionId
     * @return
     */
    public void deleteQuestion(int questionId){
        questionDao.deleteById(questionId);
    }

    /**
     * 申请编辑题
     */
    public void editApplyQuestion(String str,String account,int id) throws BizException, IOException, BadElementException {
        Map<String,Object> result = toMap(str);
        int questionId = -1;
        if(result.get("questionId")!=null){
            questionId = Integer.parseInt(String.valueOf(result.get("questionId")) );
        }
        int type = 99;
        if(result.get("type")!=null){
            type = Integer.parseInt(String.valueOf(result.get("type")) );
        }
        int subject = 1;
        if(result.get("subject")!=null){
            subject = Integer.parseInt(String.valueOf(result.get("subject")) );
        }
        int module = 17;
        if(result.get("module")!=null){
            module = Integer.parseInt(String.valueOf(result.get("module")) );
        }
        String content = "";
        if(result.get("content")!=null){
            content = String.valueOf(result.get("content"));
            logger.info("变化前content={}",content);
            content = imgManage(content,account,1);
            logger.info("变化后content={}",content);
        }
        String subSign = "";
        if(result.get("subSign")!=null){
            subSign = String.valueOf(result.get("subSign"));
        }
        int paperId = -1;
        if(result.get("paperId")!=null){
            paperId = Integer.parseInt(String.valueOf(result.get("paperId")) );
        }
        QuestionModify questionModify=QuestionModify.builder()
                .qid(questionId)
                .type(type)
                .subject(subject)
                .module(module)
                .content(content)
                .subSign(subSign)
                .paperId(paperId).build();
        if(questionDao.findEditByQuestionId(questionId).size()>0){
            throw new BizException(QuestionError.EDIT_ALEADY_EXIT);
        }else{
            try {
                questionDao.editApply(questionModify,account,id);
            }catch (Exception e){
                e.printStackTrace();
                throw new BizException(QuestionError.EDIT_FAIL);
            }
        }
    }

    /**
     * 直接进行修改，不进行审核
     * @param str
     * @param account
     * @param applierId
     * @throws BizException
     */
    public void directUpdateQuestion(String str,String account,int applierId) throws BizException, IOException, BadElementException, IllegalQuestionException {
        logger.info("修改传输过来str={}",str);
        int questionId = -1;
        String content = "";
        String delSubIds = "";
        int paperId = -1;
        Map<String,Object> result = toMap(str);//str转化为map形式
        if(result.containsKey("questionId")){
            questionId = Integer.parseInt(String.valueOf(result.get("questionId")));
        }if(result.containsKey("content")){
            content = String.valueOf(result.get("content"));
        }if(result.containsKey("delSubIds")){
            delSubIds = String.valueOf(result.get("delSubIds"));
        }if(result.containsKey("paperId")){
            paperId = Integer.parseInt(String.valueOf(result.get("paperId")));
        }

        if(StringUtils.isNotEmpty(delSubIds)){ //不等于空,表示删除
            List<String> qids=convertStrToList(delSubIds);
            for(String qid:qids){
                reviewDeleteQuestion(paperId, Integer.parseInt(qid),1);
            }
        }
        if(!content.equals("")&&content!=null&&content.length()>0){//content不为空
            QuestionModify questionModify = QuestionModify.builder()
                    .content(content)
                    .qid(questionId)
                    .paperId(paperId)
                    .uid(applierId)
                    .reviewerName(account)
                    .build();
            //编辑
            Question question = questionDao.findQuestionById(questionId);
            QuestionExtend questionExtend =questionDao.findExtendById(questionId);
            if(question instanceof GenericQuestion ||question instanceof GenericSubjectiveQuestion){
                editQuestionSingle("",question,questionExtend,questionModify,account,1);
            }else if(question instanceof CompositeQuestion || question instanceof CompositeSubjectiveQuestion){
                editQuestionMult(question,questionExtend,questionModify,account,1);
            }
        }
    }


    /**
     * 试题审核
     * @param questionId
     * @param opType
     * @throws BizException
     */
    public void reviewQuestion(int questionId,int opType,String reason,String account) throws BizException, IOException, BadElementException, IllegalQuestionException {
        List<QuestionModify> questionModifyList=questionDao.findEditByQuestionId(questionId);
        QuestionModify questionModify=new QuestionModify();
        if(CollectionUtils.isNotEmpty(questionModifyList)&&questionModifyList.size()>0){
            questionModify = questionModifyList.get(0);
        }
        int status = -1;
        if(questionModify!=null){
            status = questionModify.getStatus();
            if(status>-1){//已经被他人进行审核了
                throw new BizException(QuestionError.EDIT_ALEADY_REVIEW);
            }else{
                if(opType==0){//不通过
                    questionDao.modifyEditLogStatus(questionId,opType,reason);
                }else if(opType==1) {//通过
                    if (StringUtils.isNotEmpty(questionModify.getSubSign())) { //不等于空表示删除
                        List<String> qids = convertStrToList(questionModify.getSubSign());
                        for (String qid : qids) {
                            reviewDeleteQuestion(questionModify.getPaperId(), Integer.parseInt(qid),2);
                        }
                    }
                    if(StringUtils.isNotEmpty(questionModify.getContent())){
                        //编辑试题
                        Question question = questionDao.findQuestionById(questionId);
                        QuestionExtend questionExtend = questionDao.findExtendById(questionId);
                        if (question instanceof GenericQuestion || question instanceof GenericSubjectiveQuestion) {
                            editQuestionSingle("", question, questionExtend, questionModify,account,2);
                        } else if (question instanceof CompositeQuestion || question instanceof CompositeSubjectiveQuestion) {
                            editQuestionMult(question, questionExtend, questionModify,account,2);
                        }
                    }
                    //更改审核表试题状态
                    questionDao.modifyEditLogStatus(questionId,opType,"");
                }
            }
        }
    }

    /**
     * 审核通过删除试题，若此试题为子题，则首先移除试卷中的此试题，然后移除复合题中此试题，然后更改试题状态
     * @param paperId
     * @param questionId
     * @param flag 标记是否为直接删除  1：直接删除
     */
    public void reviewDeleteQuestion(int paperId, int questionId,int flag) throws BizException, IllegalQuestionException {

        //判断此题是否为复合题，若为复合题则从复合题中移除
        Question question=questionDao.findQuestionById(questionId);
        if(question.getChannel()!=3){
            //从试卷清除试题
            paperQuestionService.delQuestion(paperId,questionId);
        }
        //获取父集试题信息
        int parentId = 0;
        if (question instanceof GenericQuestion) {
            parentId = ((GenericQuestion) question).getParent();
        } else if (question instanceof GenericSubjectiveQuestion) {
            parentId = ((GenericSubjectiveQuestion) question).getParent();
        }
        //更改试题状态
        if(parentId>0){ //此题为小题
            List<Integer> questionIds=Lists.newArrayList();
            List<Integer> removeIds=Arrays.asList(questionId);
            Question parentQ=questionDao.findQuestionById(parentId);
            CompositeQuestion compositeQuestion=null;
            CompositeSubjectiveQuestion compositeSubjectiveQuestion=null;
            if (parentQ instanceof CompositeQuestion) {
                compositeQuestion = (CompositeQuestion) parentQ;
                questionIds=compositeQuestion.getQuestions();
            } else if (parentQ instanceof CompositeSubjectiveQuestion) {
                compositeSubjectiveQuestion = (CompositeSubjectiveQuestion) parentQ;
                questionIds=compositeSubjectiveQuestion.getQuestions();
            }
            //从试题列表中删除
            questionIds.removeIf(i -> removeIds.contains(i));
            if(compositeQuestion!=null){
                compositeQuestion.setQuestions(questionIds);
                updateQuestion(compositeQuestion,flag);
            }else if(compositeSubjectiveQuestion!=null){
                compositeSubjectiveQuestion.setQuestions(questionIds);
                updateQuestion(compositeSubjectiveQuestion,flag);
            }
        }
        //删除更改试题状态
        question.setStatus(QuestionStatus.DELETED);
        updateQuestion(question,flag);
    }

    /**
     * 更改试题
     * @param question
     * @param flag
     * @throws IllegalQuestionException
     */
    public void updateQuestion(Question question,int flag)  {
        questionDao.updateQuestion(question);
        logger.info("update:question={}",question);
        if(flag!=1){
            Map map = Maps.newHashMap();
            map.put("id",question.getId());
            rabbitTemplate.convertAndSend("","sync_question_update",map);
        }
    }
    /**
     * 修改单一试题
     * @param questionId
     * @param question
     * @param questionExtend
     */
    private void editQuestionSingle(String questionId,Question question,QuestionExtend questionExtend,QuestionModify questionModify,String account,int flag) throws BizException, IOException, BadElementException, IllegalQuestionException {
        if(question instanceof GenericQuestion){
            GenericQuestion genericQuestion=convertQuestionAfter(questionId,(GenericQuestion)question,questionModify.getContent(),account);
            QuestionExtend extend=convertExtend(questionId,questionExtend,questionModify);
            questionDao.insertExtend(extend);
            if(genericQuestion.getChannel()==3&&genericQuestion.getStatus()==QuestionStatus.AUDIT_REJECT){//为散题且状态为拒绝状态情况下
                genericQuestion.setStatus(QuestionStatus.CREATED);
            }
            updateQuestion(genericQuestion,flag);
        }else if(question instanceof GenericSubjectiveQuestion){
            GenericSubjectiveQuestion subjectiveQuestion=convertQuestionSubject(questionId,(GenericSubjectiveQuestion)question,questionModify.getContent(),questionModify.getUname());
            if(subjectiveQuestion.getChannel()==3&&subjectiveQuestion.getStatus()==QuestionStatus.AUDIT_REJECT){//为散题且状态为拒绝状态情况下
                subjectiveQuestion.setStatus(QuestionStatus.CREATED);
            }
            updateQuestion(subjectiveQuestion,flag);//更改试题

        }
    }

    /**
     * 修改复合试题
     * @param question
     * @param questionExtend
     * @param questionModify
     */
    private void editQuestionMult(Question question,QuestionExtend questionExtend,QuestionModify questionModify,String account,int flag) throws BizException, IOException, BadElementException, IllegalQuestionException {
        List<Integer> qids=Lists.newArrayList();
        if(question instanceof CompositeQuestion){
            CompositeQuestion compositeQuestion=convertComposite((CompositeQuestion)question,questionModify.getContent(),account);
            QuestionExtend extend=convertExtend("",questionExtend,questionModify);
            questionDao.insertExtend(extend);
            if(compositeQuestion.getChannel()==3&&compositeQuestion.getStatus()==QuestionStatus.AUDIT_REJECT){//为散题且状态为拒绝状态情况下
                compositeQuestion.setStatus(QuestionStatus.CREATED);
            }
            updateQuestion(compositeQuestion,flag);//更改试题
            //获取子试题
            qids=compositeQuestion.getQuestions();
            //更改复合题资料，需更改所有小题资料
            if(CollectionUtils.isNotEmpty(qids)){
                for (Integer qid:qids){
                    Question subQuestion = questionDao.findQuestionById(qid);
                    subQuestion.setMaterial(compositeQuestion.getMaterial());
                    subQuestion.setMaterials(compositeQuestion.getMaterials());
                    updateQuestion(subQuestion,flag);//更改试题
                }
            }
        }else if(question instanceof CompositeSubjectiveQuestion){
            CompositeSubjectiveQuestion subjectiveQuestion=convertCompositeSubject("",(CompositeSubjectiveQuestion)question,questionModify.getContent(),account);
            if(subjectiveQuestion.getChannel()==3&&subjectiveQuestion.getStatus()==QuestionStatus.AUDIT_REJECT){//为散题且状态为拒绝状态情况下
                subjectiveQuestion.setStatus(QuestionStatus.CREATED);
            }
            updateQuestion(subjectiveQuestion,flag);//更改试题
            //获取子试题
            qids=subjectiveQuestion.getQuestions();
            //更改复合题资料，需更改所有小题资料
            if(CollectionUtils.isNotEmpty(qids)){
                for (Integer qid:qids){
                    Question subQuestion = questionDao.findQuestionById(qid);
                    if(subQuestion instanceof GenericSubjectiveQuestion){
                        GenericSubjectiveQuestion subSubjectiveQuestion=(GenericSubjectiveQuestion)subQuestion;
                        subSubjectiveQuestion.setRequire(subjectiveQuestion.getRequire());
                        subSubjectiveQuestion.setMaterial(subjectiveQuestion.getMaterial());
                        subSubjectiveQuestion.setMaterials(subjectiveQuestion.getMaterials());
                        updateQuestion(subSubjectiveQuestion,flag);//更改试题
                    }else{
                        subQuestion.setMaterial(subjectiveQuestion.getMaterial());
                        subQuestion.setMaterials(subjectiveQuestion.getMaterials());
                        updateQuestion(subQuestion,flag);//更改试题
                    }


                }
            }
        }
        for (Integer qid:qids){
            Question subQuestion = questionDao.findQuestionById(qid);
            QuestionExtend subQuestionExtend =questionDao.findExtendById(qid);
            editQuestionSingle(qid+"",subQuestion,subQuestionExtend,questionModify,account,flag);
        }
    }

    /**
     * 数据预处理
     * @param data
     * @param account
     * @return
     */
    private String preTreatData(String data,String account) throws BizException, IOException, BadElementException {
        data = imgManage(data,account,0);
        data = htmlManage(data);
        return data;
    }
    private GenericQuestion convertQuestionAfter(String questionid,GenericQuestion genericQuestion,String content,String account) throws BizException, IOException, BadElementException {
        logger.info("传输过来的试题修改部分："+content);
        Map<String,Object> result;
        result = toMap(content);
        //选项
        List<String> choices = new ArrayList<>();
        choices.addAll(genericQuestion.getChoices());
        if(result.containsKey("type"+questionid)){//若在content中有stem属性，说明stem进行修改过
            genericQuestion.setType(Integer.parseInt(String.valueOf(result.get("type"+questionid))));
        }
        if(result.containsKey("stem"+questionid)){//若在content中有stem属性，说明stem进行修改过
            genericQuestion.setStem(preTreatData(String.valueOf(result.get("stem"+questionid)),account));
        }
        if(result.containsKey("extend"+questionid)){
            genericQuestion.setExtend(String.valueOf(result.get("extend"+questionid)));
        }
        if(result.containsKey("answer"+questionid)){  //答案
            genericQuestion.setAnswer(Integer.parseInt(String.valueOf(result.get("answer"+questionid))));
        }
        if(result.containsKey("analysis"+questionid)){ //解析
            genericQuestion.setAnalysis(preTreatData(String.valueOf(result.get("analysis"+questionid)),account));
        }
        if(result.containsKey("score"+questionid)){  //分数
            genericQuestion.setScore(Float.parseFloat(String.valueOf(result.get("score"+questionid))));
        }
        if(result.containsKey("difficult"+questionid)){  //难度
            genericQuestion.setDifficult(Integer.parseInt(String.valueOf(result.get("difficult"+questionid))));
        }
        if(result.containsKey("pointsName"+questionid)){  //知识点名称
            genericQuestion.setPointsName(convertStrToList(String.valueOf(result.get("pointsName"+questionid))));
        }
        if(result.containsKey("pointsId"+questionid)&&!"undefined".equals(String.valueOf(result.get("pointsId"+questionid)))){ //知识点id
            genericQuestion.setPoints(FuncStr.castToList(String.valueOf(result.get("pointsId"+questionid))));
        }
        if(result.containsKey("material"+questionid)){  //材料
            genericQuestion.setMaterial(preTreatData(String.valueOf(result.get("material"+questionid)),account));
        }
        if(result.containsKey("teachType"+questionid)){  //教研题型
            genericQuestion.setTeachType(String.valueOf(result.get("teachType"+questionid)));
        }

        for(Map.Entry<String,Object> entry : result.entrySet()){
            String attribute = entry.getKey();
            int num = 0;
            if(attribute.startsWith("choices"+questionid+"_")){
                num = Integer.parseInt(attribute.replaceAll("choices"+questionid+"_",""));
                if(num>choices.size()){//若增加的选项值数字超过原选项个数，直接添加到后端
                    choices.add(String.valueOf(entry.getValue()));
                }else{
                    if(String.valueOf(entry.getValue())==""){//值为空表示删除
                        for(int j=num-1;j<choices.size()-1;j++){
                            choices.set(j,choices.get(j+1));
                        }
                        choices.remove(choices.size()-1);
                    }else{
                        choices.set(num-1,preTreatData(String.valueOf(entry.getValue()),account));
                    }
                }
            }
        }
        genericQuestion.setChoices(choices);
        return genericQuestion;
    }

    private Map<String,Object> toMap(String content) throws BizException {
        Map<String,Object> result = null;
        try{
            result = JsonUtil.toMap(content);
        }catch(Exception e){
            logger.error("解析失败，数据为{}",content);
            content = styleTag(content);
            logger.info("解析双引号结果：splitStr = {}",content);
            try{
                result = JsonUtil.toMap(content);
            }catch (Exception e1){
                logger.info("解析处理双引号问题失败！");
                e1.printStackTrace();
                throw new BizException(ErrorResult.create(1000107, "解析数据错误"));
            }
        }
        return result;
    }

    private String styleTag(String content) {
        StringBuilder sb = new StringBuilder(content);
        Pattern pattern = Pattern.compile("<([^>]+)>");
        Matcher matcher = pattern.matcher(sb);
        int i = 0;
        while(matcher.find(i)){
            String temp = matcher.group(1);
            temp = temp.replace("\"","'");
            sb.replace(matcher.start(1),matcher.end(1),temp);
            i = matcher.end();
        }
        String result = sb.toString();
//        content = content.replace("\"","'");
//        logger.info("content={}",content);
//        content = content.replace("':'","\":\"").replace("','","\",\"").replace("{'","{\"")
//                .replace("'}","\"}");
        return result.replace("*","\\*");
    }

    private List<String> convertStrToList(String value){
        if (StringUtils.isNotEmpty(value)) return Arrays.asList(value.split(","));
        return null;
    }
    private GenericSubjectiveQuestion convertQuestionSubject(String questionId,GenericSubjectiveQuestion subjectiveQuestion,String content,String account) throws BizException, IOException, BadElementException {
        Map<String,Object> result = toMap(content);//content转化为map形式
        if(result.containsKey("stem"+questionId)){//若在content中有stem属性，说明stem进行修改过
            subjectiveQuestion.setStem(preTreatData(String.valueOf(result.get("stem"+questionId)),account));
        }if(result.containsKey("require"+questionId)){//要求
            subjectiveQuestion.setRequire(preTreatData(String.valueOf(result.get("require"+questionId)),account));
        }if(result.containsKey("scoreExplain"+questionId)){//赋分说明
            subjectiveQuestion.setScoreExplain(preTreatData(String.valueOf(result.get("scoreExplain"+questionId)),account));
        }if(result.containsKey("referAnalysis"+questionId)){  //解析即答案
            subjectiveQuestion.setReferAnalysis(preTreatData(String.valueOf(result.get("referAnalysis"+questionId)),account));
        }if(result.containsKey("answerRequire"+questionId)){  //答题要求
            subjectiveQuestion.setAnswerRequire(preTreatData(String.valueOf(result.get("answerRequire"+questionId)),account));
        }if(result.containsKey("examPoint"+questionId)){  //审题要求
            subjectiveQuestion.setExamPoint(preTreatData(String.valueOf(result.get("examPoint"+questionId)),account));
        }if(result.containsKey("solvingIdea"+questionId)){  //解题思路
            subjectiveQuestion.setSolvingIdea(preTreatData(String.valueOf(result.get("solvingIdea"+questionId)),account));
        }if(result.containsKey("difficult"+questionId)){  //难度
            subjectiveQuestion.setDifficult(Integer.parseInt(String.valueOf(result.get("difficult"+questionId))));
        } if(result.containsKey("score"+questionId)){//分数
            subjectiveQuestion.setScore(Float.parseFloat(String.valueOf(result.get("score"+questionId))));
        }
        if(result.containsKey("minWordCount"+questionId)){//最少字数
            subjectiveQuestion.setMinWordCount(Integer.parseInt(String.valueOf(result.get("minWordCount"+questionId))));
        }
        if(result.containsKey("maxWordCount"+questionId)){//最大字数
            subjectiveQuestion.setMaxWordCount(Integer.parseInt(String.valueOf(result.get("maxWordCount"+questionId))));
        }
        if(result.containsKey("teachType"+questionId)){  //教研题型
            subjectiveQuestion.setTeachType(String.valueOf(result.get("teachType"+questionId)));
        }
        //资料
        List<String> materials=subjectiveQuestion.getMaterials();//材料
        for(Map.Entry<String,Object> entry : result.entrySet()){
            String attribute = entry.getKey();
            int num = 0;
            if(attribute.startsWith("materials"+questionId+"_")){
                num = Integer.parseInt(attribute.replaceAll("materials"+questionId+"_",""))+1;
                if(num>materials.size()){//若增加的选项数字超过原选项个数，直接添加到后端
                    materials.add(preTreatData(String.valueOf(entry.getValue()),account));
                }else{
                    if(String.valueOf(entry.getValue())==""){//空值表示删除
                        for(int j=num-1;j<materials.size()-1;j++){
                            materials.set(j,preTreatData(materials.get(j+1),account));
                        }
                        materials.remove(materials.size()-1);
                    }else{
                        materials.set(num-1,preTreatData(String.valueOf(entry.getValue()),account));
                    }
                }
            }
        }
        subjectiveQuestion.setMaterials(materials);
        return subjectiveQuestion;
    }
    private CompositeQuestion convertComposite(CompositeQuestion compositeQuestion,String content,String account) throws BadElementException, BizException, IOException {
        Map<String,Object> result = toMap(content);//content转化为map形式
        if(result.containsKey("material")){
            String material = preTreatData(String.valueOf(result.get("material")),account);
            compositeQuestion.setMaterial(material);
            List<String> materials = new ArrayList<>();
            materials.add(material);
            compositeQuestion.setMaterials(materials);
        }
        if(result.containsKey("difficult")){
            compositeQuestion.setDifficult(Integer.parseInt(String.valueOf(result.get("difficult"))));
        }
        return compositeQuestion;
    }
    private CompositeSubjectiveQuestion convertCompositeSubject(String questionId,CompositeSubjectiveQuestion subjectiveQuestion,String content,String account) throws BadElementException, BizException, IOException {
        logger.info("content={},questionId={}",content,questionId);
        Map<String,Object> result = toMap(content);//content转化为map形式
        if(result.containsKey("require")){
            subjectiveQuestion.setRequire(preTreatData(String.valueOf(result.get("require"+questionId)),account));
        }
        List<String> materials=subjectiveQuestion.getMaterials();
        int flag = 0;//用于标记materials是否有修改，0为没有修改，否则修改了
        for(Map.Entry<String,Object> entry : result.entrySet()){
            String attribute = entry.getKey();
            int num = 0;
            if(attribute.startsWith("materials"+questionId+"_")){
                flag = 1;
                num = Integer.parseInt(attribute.replaceAll("materials"+questionId+"_",""))+1;
                if(num>materials.size()){//若增加的选项数字超过原选项个数，直接添加到后端
                    materials.add(String.valueOf(entry.getValue()));
                }else{
                    if(String.valueOf(entry.getValue())==""){//空值表示删除
                        for(int j=num-1;j<materials.size()-1;j++){
                            materials.set(j,preTreatData(materials.get(j+1),account));
                        }
                        materials.remove(materials.size()-1);
                    }else{
                        materials.set(num-1,preTreatData(String.valueOf(entry.getValue()),account));
                    }
                }
            }
        }
        if(flag==1){
            String material = "";
            for(String ma:materials){
                material += ma;
            }
            subjectiveQuestion.setMaterial(material);
        }
        subjectiveQuestion.setMaterials(materials);
        return subjectiveQuestion;
    }

    private QuestionExtend convertExtend(String questionid,QuestionExtend questionExtend,QuestionModify questionModify) throws BizException {
        Map<String,Object> result = toMap(questionModify.getContent());//content转化为map形式
        if(questionExtend==null){
            questionExtend=QuestionExtend.builder()
                    .qid(StringUtils.isNotEmpty(questionid)?Integer.parseInt(questionid):questionModify.getQid())
                    .paperId(questionModify.getPaperId())
                    .moduleId(questionModify.getModule())
                    .build();
        }
        if(result.containsKey("moduleId"+questionid)){
            questionExtend.setModuleId(Integer.parseInt(String.valueOf(result.get("moduleId"+questionid))));
        }

        if(result.containsKey("author"+questionid)){
            questionExtend.setAuthor(String.valueOf(result.get("author"+questionid)));
        }
        if(result.containsKey("reviewer"+questionid)){
            questionExtend.setReviewer(String.valueOf(result.get("reviewer"+questionid)));
        }
        return questionExtend;
    }


    /**
     * 根据字符串，查看是否包括图片信息，若包括将图片信息提取出来，生成本地图片，再上传ftp，并将url替换原图片sc
     * @param str
     * @return
     */
    public String imgManage(String str,String account,int type) throws BizException, IOException, BadElementException {//加入account参数是为了防止临时生成的图片有重名
        List<ImgInfo> result = findSrcs(str);
        for(int i=0;i<result.size();i++){
            String imgMessge = result.get(i).getImgsrc();
            String baseAll = result.get(i).getBase();
            String[] sourceStrArray = baseAll.split(";base64,");
            String imgType = sourceStrArray[0].replaceAll("data:image/","");
            String imgBase = sourceStrArray[1];
            long time = System.currentTimeMillis();//获得当前毫秒级
            String imgPath = account+time+"."+imgType;

            if(GenerateImage(imgBase,imgPath)){//生成图片成功
                File file = new File(imgPath);
                logger.info("file的绝对地址：",file.getCanonicalPath());
                int width = result.get(i).getWidth();
                int height = result.get(i).getHeight();
                if(width==-1&&height==-1){
                    BufferedImage bufferedImage = ImageIO.read(new File(imgPath));
                    width = bufferedImage.getWidth();
                    height = bufferedImage.getHeight();
                }
                final String imageUrl = uploadFileUtil.ftpUploadPic(file).replaceAll("\\\\\"","");;//上传ftp服务器
                String lastSrc = "src=\""+imageUrl+"\" width=\""+width+"\" height=\""+height+"\"/>";
                if(type==1){
                    lastSrc = "src=\\\""+imageUrl+"\\\" width=\\\""+width+"\\\" height=\\\""+height+"\\\"/>";
                }
                logger.info("imageUrl={},lastSrc={},imgMessge={}",imageUrl,lastSrc,imgMessge);
                str = str.replace(imgMessge,lastSrc);//替换原图片src为新ftp地址,以及添加图片高度和宽度
            }
        }
        return str;
    }

    //base64字符串转化成图片
    public  boolean GenerateImage(String imgStr,String imgFilePath)
    {   //对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) //图像数据为空
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try
        {
            //Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for(int i=0;i<b.length;++i)
            {
                if(b[i]<0)
                {//调整异常数据
                    b[i]+=256;
                }
            }
            //生成图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(b);
            out.flush();
            out.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    //删除图片
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }


    /**
     * 根据输入字符串，提取图片src
     * @param str
     * @return
     */
    public List<ImgInfo> findSrcs(String str){
        List<ImgInfo> result = new ArrayList<>();
        String regExImg = "src=\"?(.*?)(\">|\"/>)";
        Pattern p = Pattern.compile(regExImg);
        Matcher m = p.matcher(str);
        while (m.find()) {
            ImgInfo imgInfo = new ImgInfo();
            if(m.group(1).contains("ztk:image")){//若为base64形式，加入返回结果
                imgInfo.setImgsrc(m.group(0));
                imgInfo.setBase(m.group(1));
                if(m.group(0).contains("height:")){//若包含宽、高数据，也提取出来
                    String regExNew = "style=\"height: ([0-9]+)px;width: ([0-9]+)px;\"";
                    Pattern pNew = Pattern.compile(regExNew);
                    Matcher mNew = pNew.matcher(m.group(0));
                    while(mNew.find()){
                        imgInfo.setHeight(Integer.parseInt(mNew.group(1)));
                        imgInfo.setWidth(Integer.parseInt(mNew.group(2)));
                    }
                }
                result.add(imgInfo);
            }
        }
        return result;
    }

    /**
     * 对html字符串进行处理，只有一个段落，即只包含一个<p></p>，同时去除所有非<br><u><img>的标签，并识别其中换行
     * @param str
     * @return
     */
    public String htmlManage(String str){
        if(str.length()>0){//不为空进行处理
            int flag = 0;
            int charAt = 0;
            for(int i=0;i<str.length();i++){
                char ch = str.charAt(i);
                if(ch==' '||ch=='\f'||ch=='\r'||ch=='\t'||ch=='\b'){
                    continue;
                }else{
                    if(flag==0&&ch!='<'){
                        charAt = i;
                        break;
                    }else if(flag==0&&ch=='<'){
                        flag = 1;
                        if(str.length()>i+4){
                            logger.info("前面部分={}",str.substring(i+1,i+4));
                            if(str.substring(i+1,i+4).equals("img")){
                                logger.info("用于测试是否已经成功");
                                break;
                            }
                        }
                    }else if(flag==1&&ch=='>'){
                        flag = 0;
                    }
                }
            }
            str = str.substring(charAt);
            logger.info("过滤完前面html标签后的str={}",str);
            String regEx1 = "<(?!br|u(?!l)|img|/(p|u(?!l)|li))(.*?)>";//识别所有非<br><u><img></p><u></li>的标签
            str = str.replaceAll(regEx1,"");
            String regEx2 = "</(?!u(?!l))(.*?)>";//识别所有非</u>的</……>标签
            str = str.replaceAll(regEx2,"<br>");
            String regExNem = "<u>(<br>|<br/>)*</u>";//识别所有非</u>的</……>标签
            str = str.replaceAll(regExNem,"");
            String regEx3 = "(<br>|<br/>)((\\s|&nbsp;)*(<br>|<br/>|(\\s|&nbsp;| ))*)*";//识别连续多个<br>出现情况
            str = str.replaceAll(regEx3,"<br>");
            str = "<p>"+str+"</p>";
        }
        return str;
    }





    /**
     * 根据输入的知识点列表，构建知识点树
     * @param pointList
     * @return
     */
    public List<QuestionPointTreeMin> toTree(List<QuestionPointTreeMin> pointList){
        List<QuestionPointTreeMin> pointTree = new ArrayList<>();
        List<QuestionPointTreeMin> pointsTwo = new ArrayList<>();//临时存放二级知识点
        for (QuestionPointTreeMin point:pointList){
            int level = point.getLevel();
            if(level== QuestionPointLevel.LEVEL_ONE){
                pointTree.add(point);
            }else {
                if(level==QuestionPointLevel.LEVEL_TWO){
                    int pointid = point.getId();
                    int position = isContain(pointid,pointsTwo);//查询该二级知识点在ponitsTwo的位置
                    //若没有发现该二级知识点，则直接加入该二级知识点列表ponitsTwo，若有，则直接替换
                    if(position==-1){//没有查找到该二级知识点
                        pointsTwo.add(point);
                    }else{//查到该二级知识点
                        point.setChildren(pointsTwo.get(position).getChildren());
                        pointsTwo.set(position,point);
                    }
                }else{//是三级知识点
                    int preid = point.getParent();
                    int position = isContain(preid,pointsTwo);//查询该三级知识点的父级知识点在ponitsTwo的位置
                    if(position==-1){//没有查到该三级知识点的父级知识点
                        QuestionPointTreeMin pointTwo = QuestionPointTreeMin.builder()
                                .id(preid)
                                .level(QuestionPointLevel.LEVEL_TWO)
                                .build();
                        List<QuestionPointTreeMin> children = new ArrayList<>();
                        children.add(point);
                        pointTwo.setChildren(children);
                        pointsTwo.add(pointTwo);
                    }else{
                        QuestionPointTreeMin pointTwo = pointsTwo.get(position);
                        if(pointTwo.getChildren()==null){
                            List<QuestionPointTreeMin> children = new ArrayList<>();
                            children.add(point);
                            pointTwo.setChildren(children);
                        }else {
                            pointTwo.getChildren().add(point);
                        }

                        pointsTwo.set(position,pointTwo);
                    }
                }
            }
        }
        for(QuestionPointTreeMin pointTwo:pointsTwo){
            int preid = pointTwo.getParent();
            int position = isContain(preid,pointTree) ;
            if(position>=0){
                QuestionPointTreeMin pointOne = pointTree.get(position);
                if(pointOne.getChildren()!=null){
                    pointOne.getChildren().add(pointTwo);
                }else{
                    List<QuestionPointTreeMin> children = new ArrayList<>();
                    children.add(pointTwo);
                    pointOne.setChildren(children);
                }

                pointTree.set(position,pointOne);
            }
        }
        return pointTree;
    }


    /**
     * 查询某个id，是否是List中某个知识点的id,若存在返回该知识点在List中的下标，否则，返回-1
     * @param id，ponits
     * @return
     */
    public int isContain(int id,List<QuestionPointTreeMin> points){
        int result = -1;
        for(int i=0;i<points.size();i++){
            QuestionPointTreeMin point = points.get(i);
            if (point.getId()==id){
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * 根据三级知识点name，找到其二级知识点名、一级知识点名
     * @param pointId
     * @return
     */
    public List<String> findPointParentsName(int pointId){
        QuestionPointTreeMin pointThree = pointDao.findPointById(pointId);
        QuestionPointTreeMin pointTwo = pointDao.findPointParentById(pointId);
        QuestionPointTreeMin pointOne = pointDao.findPointParentById(pointTwo.getId());
        List<String> parents = new ArrayList<>();
        parents.add(pointOne.getName());
        parents.add(pointTwo.getName());
        parents.add(pointThree.getName());
        return parents;
    }



    /**
     * 根据三级知识点name，找到其二级知识点id、一级知识点id
     * @param pointId
     * @return
     */
    public List<Integer> findPointParentsId(int pointId){

        QuestionPointTreeMin pointTwo = pointDao.findPointParentById(pointId);
        QuestionPointTreeMin pointOne = pointDao.findPointParentById(pointTwo.getId());
        List<Integer> parents = new ArrayList<>();
        parents.add(pointOne.getId());
        parents.add(pointTwo.getId());
        parents.add(pointId);
        return parents;
    }

    /**
     * 获取试题信息
     * @param id
     * @return
     */
    public Question findQuestinbyId(int id){
         Question question=questionDao.findQuestionById(id);
         return question;
    }

    /**
     * 获取某个id区间的试题集合
     * @param startId
     * @param endId
     * @return
     */
    public Object findQuestinsbyRange(int startId,int endId){
        List<Question> questions=questionDao.findQuestionsByRange(startId,endId);
        List<QuestionExtend> questionExtends = Lists.newArrayList();
        List<QuestionMin> questionMins = (List<QuestionMin>) assemblequestinMins(questions,questionExtends);
        return questionMins;
    }


    /**
     *获取待修改的试题
     * @param subject
     * @param module
     * @param type
     * @param startTime
     * @param endTime
     * @param questionId
     * @return
     */
    public Object allEditLogList(int subject,int module,int type,long startTime,long endTime,int questionId,long id,int status){
        long end=0;
        long start=0;
        try {
            if(startTime>-1){
                String sdate=DateFormat.transferLongToDate("yyyy-MM-dd",startTime);
                start= DateFormat.stringToLong(sdate+" 00:00:00","yyyy-MM-dd hh:mm:ss");
            }
            if(endTime>-1){
                String edate=DateFormat.transferLongToDate("yyyy-MM-dd",endTime);
                end= DateFormat.stringToLong(edate+" 23:59:59","yyyy-MM-dd hh:mm:ss");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<QuestionModify> questionModifyList= questionDao.findEditLogByDetail(subject,module,type,start/1000,end/1000,questionId,id,status);
        if(CollectionUtils.isNotEmpty(questionModifyList)&&questionModifyList.size()>0){
            List<Integer> qids=Lists.newArrayList();
            for(QuestionModify questionModify:questionModifyList) {
                    qids.add(questionModify.getQid());
            }
            Map<Integer,Question> questionMap=findQuestionByIds(qids);
            int num=questionModifyList.size();
            for(int i=0;i<num;i++) {
                QuestionModify questionModify=questionModifyList.get(i);
                if(questionMap.containsKey(questionModify.getQid())) {
                    Question question=questionMap.get(questionModify.getQid());
                    if(question instanceof  GenericQuestion){ //单一题
                        GenericQuestion genericQuestion=(GenericQuestion)question;
                        question.setMaterial(FuncStr.replaceHtml(genericQuestion.getStem()));
                        questionModify.setQuestion(question);
                    }else if (question instanceof  GenericSubjectiveQuestion ){ //复合题
                        GenericSubjectiveQuestion genericQuestion=(GenericSubjectiveQuestion)question;
                        question.setMaterial(FuncStr.replaceHtml(genericQuestion.getStem()));
                        questionModify.setQuestion(question);
                    }else if(question instanceof CompositeQuestion||question instanceof CompositeSubjectiveQuestion){
                        question.setMaterial(FuncStr.replaceHtml(question.getMaterial()));
                        questionModify.setQuestion(question);
                    }
                    questionModify.setAreaName(AreaConstants.getFullAreaNmae(question.getArea()));
                }else{
                    questionModifyList.remove(i);
                    i--;
                    num--;
                }

            }
        }
        return questionModifyList;
    }

    /**
     * 获取审核的试题的详情
     * @param id
     * @return
     */
    public Object queryReviewById(int id) throws BizException{
        List<QuestionModify> questionModifyList=questionDao.findEditByQuestionId(id);
        if(CollectionUtils.isNotEmpty(questionModifyList)){
            QuestionModify questionModify=questionModifyList.get(0);
            if(FuncStr.checkIsNotNull(questionModify.getContent())){
                try{
                    JsonUtil.toMap(questionModify.getContent());
                }catch(Exception e){
                    logger.error("解析失败，数据为{}",questionModify.getContent());
                    String content = styleTag(questionModify.getContent());
                    logger.info("解析双引号结果：splitStr = {}",content);
                    try{
                        JsonUtil.toMap(content);
                        questionModify.setContent(content);
                    }catch (Exception e1){
                        logger.info("解析处理双引号问题失败！");
                        e1.printStackTrace();
                        throw new BizException(ErrorResult.create(1000107, "解析数据错误"));
                    }
                }
            }
            int qid = questionModify.getQid();//根据查找到的questionModify获取试题Id
            Question question = questionDao.findQuestionById(qid);
            QuestionExtend questionExtend =questionDao.findExtendById(qid);
            if(question!=null){
                if(question instanceof  GenericQuestion ||question instanceof  GenericSubjectiveQuestion){  //单一题
                    QuestionForReview questionForReview=establishSingle("",question,questionExtend,questionModify);
                    markModify=null;
                    modifyAttribute=null;
                    return questionForReview;
                }else if(question instanceof  CompositeQuestion || question instanceof  CompositeSubjectiveQuestion) {  //复合题
                    QuestionForReview questionForReview=establishMult(question,questionExtend,questionModify);
                    markModify=null;
                    modifyAttribute=null;
                    return questionForReview;

                }
            }
        }
        return null;
    }

    public Object findQuestionById(int id)throws BizException{
        List<QuestionModify> questionModifyList=questionDao.findByQuestionId(id);
        if(CollectionUtils.isNotEmpty(questionModifyList)) {
            QuestionModify questionModify = questionModifyList.get(0);
            PaperQuestionBean paperQuestionBean =(PaperQuestionBean) paperQuestionService.findQuestionById(id);
            questionModify.setPaperQuestionBean(paperQuestionBean);
            return questionModify;
        }
      return null;
    }

    /**
     * 获取审核试题详情
     * @param id
     * @return
     * @throws BizException
     */
    public QuestionModify showReviewInfo(int id) throws BizException{
        List<QuestionModify> questionModifyList=questionDao.findByQuestionId(id);
        if(CollectionUtils.isNotEmpty(questionModifyList)) {
            QuestionModify questionModify = questionModifyList.get(0);
            return questionModify;
        }
        return null;
    }

    /**
     * 获取修改后的客观题
     * @param questionid
     * @return
     */
    private GenericQuestion handleQuestionAfter(String questionid,Question question,QuestionModify questionModify) throws BizException {
        GenericQuestion genericQuestion=(GenericQuestion)question ;
        GenericQuestion questionAfter=new GenericQuestion();
        //更改后试题
        questionAfter.setType(genericQuestion.getType());
        questionAfter.setId(questionModify.getId());
        questionAfter.setStem(FuncStr.htmlManage(genericQuestion.getStem()));
        questionAfter.setAnswer(genericQuestion.getAnswer());
        questionAfter.setExtend(StringUtils.isNoneBlank(genericQuestion.getExtend())?genericQuestion.getExtend():"");
        //选项
        List<String> choices = new ArrayList<>();
        choices.addAll(genericQuestion.getChoices());
        questionAfter.setChoices(choices);
        questionAfter.setMaterial(FuncStr.htmlManage(genericQuestion.getMaterial()));
        questionAfter.setAnalysis(FuncStr.htmlManage(genericQuestion.getAnalysis()));
        questionAfter.setScore(genericQuestion.getScore());
        questionAfter.setDifficult(genericQuestion.getDifficult());
        questionAfter.setPointsName(genericQuestion.getPointsName());
        questionAfter.setArea(genericQuestion.getArea());

        if(markModify==null){
            markModify=new MarkModify();
        }
        if (modifyAttribute==null){
            modifyAttribute=Lists.newArrayList();
        }
        //选项
        List<Integer> choicesMark = new ArrayList<>();
        for(int i=0;i<choices.size();i++){
            choicesMark.add(0);//初始化0
        }
        if(FuncStr.checkIsNotNull(questionModify.getContent())){
             Map<String,Object> result = toMap(questionModify.getContent());//content转化为map形式
            if(result.containsKey("extend"+questionid)){
                questionAfter.setExtend(String.valueOf(result.get("extend"+questionid)));
                modifyAttribute.add("extend"+questionid);
                markModify.setExtendMark(1);
            }
            if(result.containsKey("stem"+questionid)){//若在content中有stem属性，说明stem进行修改过
                questionAfter.setStem(FuncStr.htmlManage((String.valueOf(result.get("stem"+questionid)))));
                modifyAttribute.add("stem"+questionid);
                markModify.setStemMark(1);
            }
            if(result.containsKey("type"+questionid)){//若在content中有stem属性，说明stem进行修改过
                questionAfter.setType(Integer.parseInt(String.valueOf(result.get("type"+questionid))));
                modifyAttribute.add("type"+questionid);
                markModify.setTypeMark(1);
            }
            if(result.containsKey("answer"+questionid)){  //答案
                questionAfter.setAnswer(Integer.parseInt(String.valueOf(result.get("answer"+questionid))));
                modifyAttribute.add("answer"+questionid);
                markModify.setAnswerMark(1);
            }
            if(result.containsKey("analysis"+questionid)){ //解析
                questionAfter.setAnalysis(FuncStr.htmlManage(String.valueOf(result.get("analysis"+questionid))));
                modifyAttribute.add("analysis"+questionid);
                markModify.setAnalysisMark(1);
            }
            if(result.containsKey("score"+questionid)){  //分数
                questionAfter.setScore(Float.parseFloat(String.valueOf(result.get("score"+questionid))));
                modifyAttribute.add("score"+questionid);
                markModify.setScoreMark(1);
            }
            if(result.containsKey("difficult"+questionid)){  //难度
                questionAfter.setDifficult(Integer.parseInt(String.valueOf(result.get("difficult"+questionid))));
                modifyAttribute.add("difficult"+questionid);
                markModify.setDifficultMark(1);
            }
            if(result.containsKey("pointsName"+questionid)){  //知识点名称
                questionAfter.setPointsName(convertStrToList(String.valueOf(result.get("pointsName"+questionid))));
                modifyAttribute.add("pointsName"+questionid);
                markModify.setPointsNameMark(1);
            }
            if(result.containsKey("material"+questionid)){  //材料
                questionAfter.setMaterial(FuncStr.htmlManage(String.valueOf(result.get("material"+questionid))));
                modifyAttribute.add("material"+questionid);
                markModify.setMaterialMark(1);
            }
            if(result.containsKey("teachType"+questionid)){  //教研题型
                questionAfter.setTeachType(FuncStr.htmlManage(String.valueOf(result.get("teachType"+questionid))));
                modifyAttribute.add("teachType"+questionid);
                markModify.setTeachTypeMark(1);
            }
            for(Map.Entry<String,Object> entry : result.entrySet()){
                String attribute = entry.getKey();
                int num = 0;
                if(attribute.startsWith("choices"+questionid+"_")){
                    num = Integer.parseInt(attribute.replaceAll("choices"+questionid+"_",""));
                    if(num>choices.size()){//若增加的选项值数字超过原选项个数，直接添加到后端
                        choices.add(String.valueOf(entry.getValue()));
                        modifyAttribute.add(attribute);
                        for(int j=choices.size();j<num-1;j++){//因map顺序不固定，前面几个为空的先初始化为0
                            choicesMark.add(0);
                        }
                        choicesMark.add(2);
                    }else{
                        modifyAttribute.add(attribute);
                        if(String.valueOf(entry.getValue())==""){//值为空表示删除
                            choicesMark.set(num-1,3);
                            for(int j=num-1;j<choices.size()-1;j++){
                                choices.set(j,choices.get(j+1));
                            }
                            choices.remove(choices.size()-1);
                        }else{
                            choicesMark.set(num-1,1);
                            choices.set(num-1,String.valueOf(entry.getValue()));
                        }
                    }
                    questionAfter.setChoices(choices);
                }
            }
        }
        markModify.setChoicesMark(choicesMark);
        return  questionAfter;
    }

    /**
     * 获取修改后的主观题
     * @param questionId
     * @param question
     * @param questionModify
     * @return
     */
    private GenericSubjectiveQuestion handleSubjectQuestionAfter(String questionId,Question question,QuestionModify questionModify) throws BizException {
        GenericSubjectiveQuestion subjectiveQuestion=(GenericSubjectiveQuestion)question;
        GenericSubjectiveQuestion subjectAfter=new GenericSubjectiveQuestion();
        List<String> materials=subjectiveQuestion.getMaterials();//材料
        subjectAfter.setMaterials(materials);
        subjectAfter.setRequire(FuncStr.htmlManage(subjectiveQuestion.getRequire()));
        subjectAfter.setScoreExplain(FuncStr.htmlManage(subjectiveQuestion.getScoreExplain()));
        subjectAfter.setReferAnalysis(FuncStr.htmlManage(subjectiveQuestion.getReferAnalysis()));
        subjectAfter.setAnswerRequire(FuncStr.htmlManage(subjectiveQuestion.getAnswerRequire()));
        subjectAfter.setExamPoint(FuncStr.htmlManage(subjectiveQuestion.getExamPoint()));
        subjectAfter.setSolvingIdea(FuncStr.htmlManage(subjectiveQuestion.getSolvingIdea()));
        subjectAfter.setStem(FuncStr.htmlManage(subjectiveQuestion.getStem()));
        subjectAfter.setScore(subjectiveQuestion.getScore());
        subjectAfter.setDifficult(subjectiveQuestion.getDifficult());
        if(markModify==null){
            markModify=new MarkModify();
        }
        if (modifyAttribute==null){
            modifyAttribute=Lists.newArrayList();
        }
        //资料
        List<Integer> materialsMark = new ArrayList<>();
        for(int i=0;i<materials.size();i++){
            materialsMark.add(0);//初始化0
        }
        if(FuncStr.checkIsNotNull(questionModify.getContent())){
            Map<String,Object> result = toMap(questionModify.getContent());//content转化为map形式
            if(result.containsKey("stem"+questionId)){//若在content中有stem属性，说明stem进行修改过
                subjectAfter.setStem(String.valueOf(result.get("stem"+questionId)));
                modifyAttribute.add("stem"+questionId);
                markModify.setStemMark(1);
            }
            if(result.containsKey("require"+questionId)){//要求
                subjectAfter.setRequire(FuncStr.htmlManage(String.valueOf(result.get("require"+questionId))));
                modifyAttribute.add("require"+questionId);
                markModify.setRequireMark(1);
            }
            if(result.containsKey("scoreExplain"+questionId)){//赋分说明
                subjectAfter.setScoreExplain(FuncStr.htmlManage(String.valueOf(result.get("scoreExplain"+questionId))));
                modifyAttribute.add("scoreExplain"+questionId);
                markModify.setScoreExplainMark(1);
            }
            if(result.containsKey("referAnalysis"+questionId)){  //解析即答案
                subjectAfter.setReferAnalysis(FuncStr.htmlManage(String.valueOf(result.get("referAnalysis"+questionId))));
                modifyAttribute.add("referAnalysis"+questionId);
                markModify.setReferAnalysisMark(1);
            }if(result.containsKey("answerRequire"+questionId)){  //答题要求
                subjectAfter.setAnswerRequire(FuncStr.htmlManage(String.valueOf(result.get("answerRequire"+questionId))));
                modifyAttribute.add("answerRequire"+questionId);
                markModify.setAnswerRequireMark(1);
            }if(result.containsKey("examPoint"+questionId)){  //审题要求
                subjectAfter.setExamPoint(FuncStr.htmlManage(String.valueOf(result.get("examPoint"+questionId))));
                modifyAttribute.add("examPoint"+questionId);
                markModify.setExamPoinMarkt(1);
            }if(result.containsKey("solvingIdea"+questionId)){  //解题思路
                subjectAfter.setSolvingIdea(FuncStr.htmlManage(String.valueOf(result.get("solvingIdea"+questionId))));
                modifyAttribute.add("solvingIdea"+questionId);
                markModify.setSolvingIdeaMark(1);
            }if(result.containsKey("difficult"+questionId)){  //难度
                subjectAfter.setDifficult(Integer.parseInt(String.valueOf(result.get("difficult"+questionId))));
                modifyAttribute.add("difficult"+questionId);
                markModify.setDifficultMark(1);
            } if(result.containsKey("score"+questionId)){//分数
                subjectAfter.setScore(Float.parseFloat(String.valueOf(result.get("score"+questionId))));
                modifyAttribute.add("score"+questionId);
                markModify.setScoreMark(1);
            }
            if(result.containsKey("minWordCount"+questionId)){ //最少字数
                subjectAfter.setMinWordCount(Integer.parseInt(String.valueOf(result.get("minWordCount"+questionId))));
                modifyAttribute.add("minWordCount"+questionId);
                markModify.setMinWordCountMark(1);
            }
            if(result.containsKey("maxWordCount"+questionId)){ //最大字数
                subjectAfter.setMaxWordCount(Integer.parseInt(String.valueOf(result.get("maxWordCount"+questionId))));
                modifyAttribute.add("maxWordCount"+questionId);
                markModify.setMaxWordCountMark(1);
            }
            if(result.containsKey("teachType"+questionId)){  //教研题型
                subjectiveQuestion.setTeachType(FuncStr.htmlManage(String.valueOf(result.get("teachType"+questionId))));
                modifyAttribute.add("teachType"+questionId);
                markModify.setTeachTypeMark(1);
            }
            for(Map.Entry<String,Object> entry : result.entrySet()){
                String attribute = entry.getKey();
                int num = 0;
                if(attribute.startsWith("materials"+questionId+"_")){
                    num = Integer.parseInt(attribute.replaceAll("materials"+questionId+"_",""))+1;
                    if(num>materials.size()){//若增加的选项数字超过原选项个数，直接添加到后端
                        materials.add(FuncStr.htmlManage(String.valueOf(entry.getValue())));
                        modifyAttribute.add(attribute);
                        for(int j=materials.size();j<num-1;j++){//因map顺序不固定，前面几个为空的先初始化为0
                            materialsMark.add(0);
                        }
                        materialsMark.add(2);
                    }else{
                        modifyAttribute.add(attribute);
                        if(String.valueOf(entry.getValue())==""){//空值表示删除
                            materialsMark.set(num-1,3);
                            for(int j=num-1;j<materials.size()-1;j++){
                                materials.set(j,materials.get(j+1));
                            }
                            materials.remove(materials.size()-1);
                        }else{
                            materialsMark.set(num-1,1);
                            materials.set(num-1,FuncStr.htmlManage(String.valueOf(entry.getValue())));
                        }
                    }
                    subjectAfter.setMaterials(materials);
                }
            }
        }

        markModify.setMaterialsMark(materialsMark);
        subjectAfter.setType(question.getType());
        subjectAfter.setId(questionModify.getQid());
        subjectAfter.setArea(question.getArea());
        return subjectAfter;
    }

    /**
     * 获取修改后的试题拓展信息
     * @param questionid
     * @param questionExtend
     * @param questionModify
     * @return
     */
    private QuestionExtend handleQuestionExtendAfter(String questionid,QuestionExtend questionExtend,QuestionModify questionModify) throws BizException {
         if(questionExtend==null){
             questionExtend=new QuestionExtend();
         }
            QuestionExtend questionExtendAfter=new QuestionExtend();
            questionExtendAfter.setModuleId(questionExtend.getModuleId());

            questionExtendAfter.setAuthor(questionExtend.getAuthor());
            questionExtendAfter.setReviewer(questionExtend.getReviewer());
            questionExtendAfter.setOrgin(questionExtend.getOrgin());
            questionExtendAfter.setQid(questionExtend.getQid());

            if(FuncStr.checkIsNotNull(questionModify.getContent())){
                //更改后的拓展信息
                Map<String,Object> result = toMap(questionModify.getContent());//content转化为map形式
                if(markModify==null){
                    markModify=new MarkModify();
                }
                if (modifyAttribute==null){
                    modifyAttribute=Lists.newArrayList();
                }
                if(result.containsKey("moduleId"+questionid)){
                    questionExtendAfter.setModuleId(Integer.parseInt(String.valueOf(result.get("moduleId"+questionid))));
                    modifyAttribute.add("moduleId"+questionid);
                    markModify.setModuleMark(1);
                }

                if(result.containsKey("author"+questionid)){
                    questionExtendAfter.setAuthor(String.valueOf(result.get("author"+questionid)));
                    modifyAttribute.add("author"+questionid);
                    markModify.setAuthorMark(1);
                }
                if(result.containsKey("review"+questionid)){
                    questionExtendAfter.setReviewer(String.valueOf(result.get("review"+questionid)));
                    modifyAttribute.add("review"+questionid);
                    markModify.setReviewerMark(1);
                }
            }
            return questionExtendAfter;
    }

    /**
     * 处理复合主观题
     * @param questionId
     * @param question
     * @param questionModify
     * @return
     */
    private QuestionForReview handleSubject(String questionId,Question question,QuestionModify questionModify) throws BizException {
        CompositeSubjectiveQuestion subjectQuestionAfter=null;
        String areaName=null;
            if(StringUtils.isEmpty(questionModify.getSubSign())|| !convertStrToList(questionModify.getSubSign()).contains(question.getId()+"")){
                CompositeSubjectiveQuestion compositeQuestion=(CompositeSubjectiveQuestion)question;

                subjectQuestionAfter=new CompositeSubjectiveQuestion();
                List<String> materials=compositeQuestion.getMaterials();//材料
                subjectQuestionAfter.setRequire(FuncStr.htmlManage(compositeQuestion.getRequire()));
                subjectQuestionAfter.setMaterials(compositeQuestion.getMaterials());
                if(markModify==null){
                    markModify=new MarkModify();
                }
                if (modifyAttribute==null){
                    modifyAttribute=Lists.newArrayList();
                }
                //资料初始化
                List<Integer> materialsMark = new ArrayList<>();
                for(int i=0;i<materials.size();i++){
                    materialsMark.add(0);//初始化0
                }
                if(FuncStr.checkIsNotNull(questionModify.getContent())){
                    Map<String,Object> result = toMap(questionModify.getContent());//content转化为map形式

                    if(result.containsKey("require"+questionId)){
                        subjectQuestionAfter.setRequire(FuncStr.htmlManage(String.valueOf(result.get("require"+questionId))));
                        modifyAttribute.add("require"+questionId);
                        markModify.setRequireMark(1);
                    }
                    for(Map.Entry<String,Object> entry : result.entrySet()){
                        String attribute = entry.getKey();
                        int num = 0;
                        if(attribute.startsWith("materials"+questionId+"_")){
                            num = Integer.parseInt(attribute.replaceAll("materials"+questionId+"_",""))+1;
                            if(num>materials.size()){//若增加的选项数字超过原选项个数，直接添加到后端
                                materials.add(FuncStr.htmlManage(String.valueOf(entry.getValue())));
                                modifyAttribute.add(attribute);
                                for(int j=materials.size();j<num-1;j++){//因map顺序不固定，前面几个为空的先初始化为0
                                    materialsMark.add(0);
                                }
                                materialsMark.add(2);
                            }else{
                                modifyAttribute.add(attribute);
                                if(String.valueOf(entry.getValue())==""){//空值表示删除
                                    materialsMark.set(num-1,3);
                                    for(int j=num-1;j<materials.size()-1;j++){
                                        materials.set(j,materials.get(j+1));
                                    }
                                    materials.remove(materials.size()-1);
                                }else{
                                    materialsMark.set(num-1,1);
                                    materials.set(num-1,FuncStr.htmlManage(String.valueOf(entry.getValue())));
                                }
                            }
                            subjectQuestionAfter.setMaterials(materials);
                        }
                    }
                }
                markModify.setMaterialsMark(materialsMark);
                areaName = AreaConstants.getFullAreaNmae(question.getArea());
            }
            QuestionForReview questionForReview = QuestionForReview.builder()
                    .applierName(questionModify.getUname())
                    .applyTime(questionModify.getCreateTime())
                    .status(StringUtils.isNotEmpty(questionModify.getSubSign())?1:0)
                    .modifyAttribute(modifyAttribute)
                    .questionBefore(question)
                    .questionAfter(subjectQuestionAfter)
                    .areaName(areaName)
                    .markModify(markModify)
                    .build();
            return questionForReview;
    }

    /**
     * 复合客观题
     * @param question
     * @param questionModify
     * @return
     */
    private CompositeQuestion establishComposite(Question question,QuestionModify questionModify) throws BizException {
        //更改后的拓展信息
        CompositeQuestion questionAfter=new CompositeQuestion();
        questionAfter.setId(questionModify.getQid());
        questionAfter.setType(question.getType());
        questionAfter.setMaterial(FuncStr.htmlManage(question.getMaterial()));
        if(markModify==null){
            markModify=new MarkModify();
        }
        if (modifyAttribute==null){
            modifyAttribute=Lists.newArrayList();
        }
        if(FuncStr.checkIsNotNull(questionModify.getContent())){
            Map<String,Object> result = toMap(questionModify.getContent());//content转化为map形式
            if(result.containsKey("material")){
                questionAfter.setMaterial(FuncStr.htmlManage(String.valueOf(result.get("material"))));
                modifyAttribute.add("material");
                markModify.setMaterialMark(1);
            }
            if(result.containsKey("difficult")){
                questionAfter.setDifficult(Integer.parseInt(String.valueOf(result.get("difficult"))));
                modifyAttribute.add("difficult");
                markModify.setDifficultMark(1);
            }
        }
        return questionAfter;
    }
    private List<Integer> convertValue(String value){
        if(StringUtils.isNotEmpty(value)){
            String[] str=value.split(",");
            List<Integer> list=Lists.newArrayList();
            for(String s:str){
                list.add(Integer.parseInt(s));
            }
            return list;
        }
        return null;
    }
    /**
     * 处理单一题
     * @param question
     * @param questionExtend
     * @param questionModify
     * @return
     */
    private QuestionForReview establishSingle(String questionId,Question question,QuestionExtend questionExtend,QuestionModify questionModify) throws BizException {
        if(questionId.length()>0){
            markModify=new MarkModify();
            modifyAttribute=Lists.newArrayList();
        }
        if(question instanceof  GenericQuestion){ //单一客观
            ((GenericQuestion) question).setStem((FuncStr.htmlManage(((GenericQuestion) question).getStem())));
            question.setMaterial(FuncStr.htmlManage(question.getMaterial()));
            ((GenericQuestion) question).setAnalysis(FuncStr.htmlManage(((GenericQuestion) question).getAnalysis()));
            ((GenericQuestion) question).setExtend(FuncStr.htmlManage(((GenericQuestion) question).getExtend()));
            GenericQuestion questionAfter=null;
            QuestionExtend extendAfter=null;
            if(StringUtils.isEmpty(questionModify.getSubSign())|| !convertStrToList(questionModify.getSubSign()).contains(question.getId()+"")){
                questionAfter=handleQuestionAfter(questionId,question,questionModify);
                extendAfter=handleQuestionExtendAfter(questionId,questionExtend,questionModify);
            }
            if(questionAfter==null)markModify=new MarkModify();
            String areaName = AreaConstants.getFullAreaNmae(question.getArea());
            QuestionForReview questionForReview = QuestionForReview.builder()
                    .applierName(questionModify.getUname())
                    .applyTime(questionModify.getCreateTime())
                    .status(StringUtils.isNotEmpty(questionModify.getSubSign())?1:0)
                    .modifyAttribute(modifyAttribute)
                    .questionBefore(question)
                    .questionAfter(questionAfter)
                    .extendBefore(questionExtend)
                    .extendAfter(extendAfter)
                    .areaName(areaName)
                    .scoreBefore(((GenericQuestion)question).getScore())
                    .scoreAfter(questionAfter!=null?questionAfter.getScore():0)
                    .markModify(markModify)
                    .build();
            return questionForReview;
        }else if(question instanceof  GenericSubjectiveQuestion){  //单一主观
            GenericSubjectiveQuestion questionAfter=null;
            QuestionExtend extendAfter=null;
            if(StringUtils.isEmpty(questionModify.getSubSign())|| !convertStrToList(questionModify.getSubSign()).contains(question.getId()+"")){
                questionAfter=handleSubjectQuestionAfter(questionId,question,questionModify);
                extendAfter=handleQuestionExtendAfter(questionId,questionExtend,questionModify);
            }
            ((GenericSubjectiveQuestion) question).setReferAnalysis(FuncStr.htmlManage(((GenericSubjectiveQuestion) question).getReferAnalysis()));
            ((GenericSubjectiveQuestion) question).setExamPoint(FuncStr.htmlManage(((GenericSubjectiveQuestion) question).getExamPoint()));
            ((GenericSubjectiveQuestion) question).setRequire(FuncStr.htmlManage(((GenericSubjectiveQuestion) question).getExamPoint()));
            if(questionAfter==null)markModify=new MarkModify();
            String areaName = AreaConstants.getFullAreaNmae(question.getArea());
            QuestionForReview questionForReview = QuestionForReview.builder()
                    .applierName(questionModify.getUname())
                    .applyTime(questionModify.getCreateTime())
                    .status(StringUtils.isNotEmpty(questionModify.getSubSign())?1:0)
                    .modifyAttribute(modifyAttribute)
                    .questionBefore(question)
                    .questionAfter(questionAfter)
                    .extendBefore(questionExtend)
                    .extendAfter(extendAfter)
                    .areaName(areaName)
                    .scoreBefore(((GenericSubjectiveQuestion)question).getScore())
                    .scoreAfter(questionAfter!=null?questionAfter.getScore():0)
                    .markModify(markModify)
                    .build();
            return questionForReview;
        }
        return null;
    }
    private QuestionForReview establishMult(Question question,QuestionExtend questionExtend,QuestionModify questionModify) throws BizException {
        List<QuestionForReview> questionForReviewList= Lists.newArrayList();
        if(question instanceof  CompositeQuestion){  //复合客观
            question.setMaterial(FuncStr.htmlManage(question.getMaterial())); //资料
            CompositeQuestion questionAfter=null;
            if(StringUtils.isEmpty(questionModify.getSubSign())|| !(convertStrToList(questionModify.getSubSign()).contains(question.getId()+""))){
                questionAfter=establishComposite(question,questionModify);
            }
            QuestionForReview questionForReview = QuestionForReview.builder()
                    .applierName(questionModify.getUname())
                    .applyTime(questionModify.getCreateTime())
                    .status(StringUtils.isNotEmpty(questionModify.getSubSign())?1:0)
                    .questionBefore(question)
                    .questionAfter(questionAfter)
                    .markModify(markModify)
                    .modifyAttribute(modifyAttribute)
                    .build();
            CompositeQuestion compositeQuestion=(CompositeQuestion)question;
            List<Integer> questionIds=compositeQuestion.getQuestions();
            for(Integer qid:questionIds){
                Question subQuestion=questionDao.findQuestionById(qid);
                QuestionExtend subQuestionExtend=questionDao.findExtendById(qid);
                QuestionForReview subQuestionForReview=establishSingle(qid+"",subQuestion,subQuestionExtend,questionModify);
                questionForReviewList.add(subQuestionForReview);
            }
            questionForReview.setQuestionReviewList(questionForReviewList);
            return questionForReview;
        }else if(question instanceof CompositeSubjectiveQuestion){ //复合主观
            ((CompositeSubjectiveQuestion) question).setRequire(FuncStr.htmlManage(((CompositeSubjectiveQuestion) question).getRequire())); //资料
            QuestionForReview questionForReview=handleSubject("",question,questionModify);
            CompositeSubjectiveQuestion compositeQuestion=(CompositeSubjectiveQuestion)question;
            List<Integer> questionIds=compositeQuestion.getQuestions();
            for(Integer qid:questionIds){
                Question subQuestion=questionDao.findQuestionById(qid);
                QuestionExtend subQuestionExtend=questionDao.findExtendById(qid);
                QuestionForReview subQuestionForReview=establishSingle(qid+"",subQuestion,subQuestionExtend,questionModify);
                questionForReviewList.add(subQuestionForReview);
            }
            questionForReview.setQuestionReviewList(questionForReviewList);
            return questionForReview;
        }
       return null;
    }
    private List<CompositeQuestion> findCompositeQuestionsByIds(Set<Integer> set) {
        Criteria criteria = Criteria.where("id").in(set);;
        return mongoTemplate.find(new Query(criteria),CompositeQuestion.class);
    }
    public Map<Integer,Question> findQuestionByIds(List<Integer> qids){
        List<Question> questionList=questionDao.findAllTypeByIds(qids);
        Map<Integer,Question> maps= Maps.newHashMap();
        for(Question question:questionList){
            maps.put(question.getId(),question);
        }
        return maps;
    }

    public void updateBySubject(int subject) throws IllegalQuestionException {
        Map<Integer, List<GenericQuestion>> questionMap = Maps.newHashMap();
        //查询所有的试题
        int trueSize = 0;
        int errorSize = 0;
        int startId = 0;
        while (true) {
            List<Question> childs = getChildNoMaterial(subject, startId, 1000);
            if (CollectionUtils.isEmpty(childs)) {
                break;
            }
            for (Question child : childs) {
                if (child.getId() > startId) {
                    startId = child.getId();
                }
                if (child instanceof GenericQuestion) {
                    GenericQuestion genericQuestion = (GenericQuestion) child;
                    if (genericQuestion.getMaterial() == null) {
                        errorSize++;
                        int parent = genericQuestion.getParent();
                        putQuestonMap(parent, genericQuestion, questionMap);
                    } else {
                        trueSize++;
                    }
                }
            }
        }
        //统计数量
        logger.info("有{}道试题没有材料，有{}道子题有材料",errorSize,trueSize);
        Set<Integer> set = questionMap.keySet();
        if(CollectionUtils.isEmpty(set)){
            return;
        }
        logger.info("有{}道复合题需要调整",set.size());
        List<CompositeQuestion> compositeQuestions = findCompositeQuestionsByIds(set);
        for(CompositeQuestion compositeQuestion:compositeQuestions){
            for(GenericQuestion genericQuestion:questionMap.get(compositeQuestion.getId())){
                genericQuestion.setMaterial(compositeQuestion.getMaterial());
                genericQuestion.setMaterials(compositeQuestion.getMaterials());
                updateQuestion(genericQuestion,2);

            }
        }
    }


    private void putQuestonMap(int parent, GenericQuestion genericQuestion, Map<Integer, List<GenericQuestion>> questionMap) {
        if(questionMap.get(parent)==null){
            List<GenericQuestion> questions = Lists.newArrayList();
            questions.add(genericQuestion);
            questionMap.put(parent,questions);
        }else{
            List<GenericQuestion> questions = questionMap.get(parent);
            questions.add(genericQuestion);
        }
    }

    private List<Question> getChildNoMaterial(int subject, int startId,int size) {
        logger.info("subject={},startId={}",subject,startId);
        Criteria criteria = Criteria.where("id").gt(startId).and("subject").is(subject).and("parent").ne(0).and("type").ne(105);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC, "id")).limit(size);
//        query+.limit(size);
        List<Question> questions = mongoTemplate.find(query, Question.class);
        return questions;
    }

    /**
     * 刷新试题格式的主方法
     */
    public void updateStyleMain(int subjectId){
        long start = System.currentTimeMillis();
        Long total = questionDao.countBySubject(subjectId);
        int cursor = 0;
        int size = 100;
        int count = 0;
        while(true){
            List<Question> questionList = questionDao.findQuestionsForPage(cursor,size,subjectId);
            if(CollectionUtils.isEmpty(questionList)){
                logger.info("已无试题需要处理，进程结束");
                break;
            }
            cursor = questionList.get(questionList.size()-1).getId();
            for (Question question : questionList) {
                updateQuestionStyle(question);
            }
            count += questionList.size();
            logger.info("刷新进程：+++++{}/{}",count,total);
        }
        long end = System.currentTimeMillis();
        logger.info("刷新需要时间：{}",(end-start)/1000);
    }

    /**
     * 刷新试题格式专用
     * @param question
     * @return
     */
    public Object updateQuestionStyle(Question question){
        if(question.getStatus()==1){
            question.setStatus(2);
            updateQuestion(question,-1);
            return null;
        }
        if(question instanceof GenericQuestion){
            List<String> contents = Lists.newArrayList();
            contents.addAll(((GenericQuestion) question).getChoices());
            contents.add(((GenericQuestion) question).getStem());
            if(isUpdate(contents)){
                updateQuestion(question,-1);
            }
        }else if(question instanceof CompositeQuestion){
            List<String> contents = Lists.newArrayList();
            if(CollectionUtils.isNotEmpty(question.getMaterials())){
                contents.addAll(question.getMaterials());
            }
            contents.add(question.getMaterial());
            if(isUpdate(contents)){
                updateQuestion(question,-1);
            }
        }
        return null;
    }

    private Boolean isUpdate(List<String> strings) {
        if(CollectionUtils.isEmpty(strings)){
            return false;
        }
        for(String str:strings){
            if(str.indexOf("<br></p>")!=-1){
                return true;
            }
        }
        return false;
    }


}
