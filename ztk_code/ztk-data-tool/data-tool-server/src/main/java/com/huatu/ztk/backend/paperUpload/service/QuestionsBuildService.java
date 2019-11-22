package com.huatu.ztk.backend.paperUpload.service;

import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paperModule.bean.PaperModuleBean;
import com.huatu.ztk.backend.paperModule.service.PaperModuleService;
import com.huatu.ztk.backend.paperUpload.bean.*;
import com.huatu.ztk.backend.paperUpload.dao.LogIterator;
import com.huatu.ztk.backend.question.bean.QuestionPointTreeMin;
import com.huatu.ztk.backend.question.dao.PointDao;
import com.huatu.ztk.backend.teachType.bean.TeachTypeBean;
import com.huatu.ztk.backend.teachType.service.TeachTypeService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.lowagie.text.html.simpleparser.Img;
import org.reflections.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.huatu.ztk.backend.util.FuncStr.htmlManage;

/**
 * Created by lenovo on 2017/6/12.
 */
@Service
public class QuestionsBuildService extends LogIterator{
    private  static Logger logger = LoggerFactory.getLogger(QuestionsBuildService.class);
    @Autowired
    private QuestionsCheckService questionsCheckService;
    @Autowired
    private TeachTypeService teachTypeService;
    @Autowired
    private PaperModuleService paperModuleService;
    @Autowired
    private PointDao pointDao;
    //创建所有线程独立的变量类
    private static ThreadLocal<PaperLocal> threadVar = ThreadLocal.withInitial(() -> PaperLocal.builder()
            .paperCounter(PaperCounter.builder().questionCount(0).questionSeq(0).sucQuestionSeq(new HashSet<>()).build())
            .paperAttrCollection(PaperAttrCollection.builder().moduleList(new ArrayList<>()).pointList(new ArrayList<>()).teachTypeList(new ArrayList<>()).build())
            .build());
    //某一科目下的所有知识点，由线程内获得的subjectId得到
    public void setPointList(int subjectId){
        threadVar.get().getPaperAttrCollection().setPointList(pointDao.findAllPonitsBySubject(subjectId));
    }

    public List<QuestionPointTreeMin> getPointList(){
        return threadVar.get().getPaperAttrCollection().getPointList();
    }
    public void setModuleList(int subjectId,int uid){
        threadVar.get().getPaperAttrCollection().setModuleList(paperModuleService.findList(subjectId,uid));
    }
    public List<PaperModuleBean> getModuleList(){
        return threadVar.get().getPaperAttrCollection().getModuleList();
    }
    public void setTeachTypeList(int subjectId,int uid) throws BizException {
        threadVar.get().getPaperAttrCollection().setTeachTypeList(teachTypeService.findList(subjectId,uid));
    }
    public List<TeachTypeBean> getTeachTypeList(){
        return threadVar.get().getPaperAttrCollection().getTeachTypeList();
    }
    //设定处理成功计数器
    public int getSuccessSeq(){
        return threadVar.get().getPaperCounter().getSucQuestionSeq().size();
    }
    public boolean setSuccessSeq(int seq){
        Set temp = threadVar.get().getPaperCounter().getSucQuestionSeq();
        if(!temp.contains(seq)){
            temp.add(seq);
            return true;
        }
        return false;
    }
    public int getIncQuestionSeq(){
        int i = getQuestionSeq()+1;
        threadVar.get().getPaperCounter().setQuestionSeq(i);
        return i;
    }
    public int getQuestionSeq(){
        return threadVar.get().getPaperCounter().getQuestionSeq();
    }
    public void initQuestionSeq(){
        threadVar.get().getPaperCounter().setQuestionSeq(0);
    }
    public int getQuestionCount(){
        return threadVar.get().getPaperCounter().getQuestionCount();
    }
    public void incQuestinCount(int i){
        threadVar.get().getPaperCounter().setQuestionCount(getQuestionCount()+i);
    }
    public void clearThreadLocal(){
        threadVar.remove();
    }
    public Map<String,LinkedList> dealAndAddQuestionPre(Map paper, LinkedList<String> eleList, int uid) throws Exception{
        //初始化试题录入的题序
        int catgory = -1;
        if(!Objects.isNull(paper.get("catgory"))){
            catgory = Integer.parseInt(String.valueOf(paper.get("catgory")));
        }
        this.setPointList(catgory);
        this.setModuleList(catgory,uid);
        this.setTeachTypeList(catgory,uid);
        this.initQuestionSeq();
        Map<String,LinkedList> mapData;
        if(!"-1".equals(String.valueOf(paper.get("id")))){
            mapData = anayleEleList(eleList);
        }else{
            mapData = anayleEleListNoPaper(eleList,uid);
        }
        return mapData;
    }
    private Map<String,LinkedList> anayleEleList(List<String> eleList) throws Exception{
        //得到一个特定科目下的模块信息集合
        LinkedList<Map> moduleAttrList = new LinkedList<>();
        LinkedList<Map>  questionList = new LinkedList<>();
        Map mapFlag = new HashMap();
        mapFlag.put("flag","4"); //录题阶段，正在分析题目中1，分析题完毕2,分析完毕，没有新题出现记4
        mapFlag.put("type1",-1);     //0表示不确定，1标识单一题，2表示复合题
        mapFlag.put("type2",-1);     //0表示不确定，1表示客观题，2表示主观题

        for(String eleStr:eleList){
            if(eleStr.trim()==null||"".equals(eleStr.trim())){
                continue;
            }
            //如果是题目结束标签，后续不用再判断了
            if(checkTailFlag(mapFlag,eleStr,questionList)){
                continue;
            }
            //如果是模块，后续不用判断
            if(checkElePre(mapFlag,eleStr,moduleAttrList)){
                continue;
            }

            if(checkBranch(mapFlag,eleStr,questionList)){
                continue;
            }
            if(adapterStem(mapFlag,eleStr,questionList)){
                continue;
            }
            if(adapterOption(mapFlag,eleStr,questionList)){
                continue;
            }
            if(adapterRequire(mapFlag,eleStr,questionList)){
                continue;
            }
            if(adapterMaterial(mapFlag,eleStr,questionList)){
                continue;
            }
            if(adapterAnalyze(mapFlag,eleStr,questionList)){
                continue;
            }
            concatElement(eleStr,questionList,mapFlag);

        }
        if(mapFlag.get("tail")!=null){
            String error = "试卷没有正常结束";
            logger.error(error);
            this.setLoggerList(PaperUploadError.builder()
                    .errorMsg(error).errorType("error").floor(logger.getName()).errorFlag("noTial").build());
        }
        int c = 0;
        if(isLoggerError("noTial")){
            c++;
        }
        //处理线程返回结果
        dealThreadResult(c);
        final Map<String,LinkedList> mapData = new HashMap();
        if(CollectionUtils.isEmpty(moduleAttrList)){
            logger.error("无可识别模块内容");
            this.setLoggerList(PaperUploadError.builder()
                    .errorMsg("无可识别的模块内容").errorType("error").floor(logger.getName()).build());
        }
        if(CollectionUtils.isEmpty(questionList)){
            logger.error("无可识别的试题内容");
            this.setLoggerList(PaperUploadError.builder()
                    .errorMsg("无可识别的试题内容").errorType("error").floor(logger.getName()).build());
        }
        mapData.put("moduleAttrList",moduleAttrList);
        mapData.put("questionList",questionList);
        return mapData;
    }

    private Map<String,LinkedList> anayleEleListNoPaper(List<String> eleList,int uid) throws Exception{
        //得到一个特定科目下的模块信息集合
        LinkedList<Map>  questionList = new LinkedList<>();
        Map mapFlag = new HashMap();
        mapFlag.put("flag","4"); //录题阶段，正在分析题目中1，分析题完毕2,分析完毕，没有新题出现记4
        mapFlag.put("type1",-1);     //0表示不确定，1标识单一题，2表示复合题
        mapFlag.put("type2",-1);     //0表示不确定，1表示客观题，2表示主观题
        mapFlag.put("paperId",-1);
        mapFlag.put("moduleId",-1);
        mapFlag.put("uid",uid);
        for(String eleStr:eleList){
            if(eleStr.trim()==null||"".equals(eleStr.trim())){
                continue;
            }
            //如果是题目结束标签，后续不用再判断了
            if(checkTailFlag(mapFlag,eleStr,questionList)){
                continue;
            }
            if(checkBranch(mapFlag,eleStr,questionList)){
                continue;
            }
            if(adapterStem(mapFlag,eleStr,questionList)){
                continue;
            }
            if(adapterOption(mapFlag,eleStr,questionList)){
                continue;
            }
            if(adapterRequire(mapFlag,eleStr,questionList)){
                continue;
            }
            if(adapterMaterial(mapFlag,eleStr,questionList)){
                continue;
            }
            if(adapterAnalyze(mapFlag,eleStr,questionList)){
                continue;
            }
            concatElement(eleStr,questionList,mapFlag);

        }
        if(mapFlag.get("tail")!=null){
            String error = "试卷没有正常结束";
            logger.error(error);
            this.setLoggerList(PaperUploadError.builder()
                    .errorMsg(error).errorType("error").floor(logger.getName()).errorFlag("noTial").build());
        }
        int c = 0;
        if(isLoggerError("noTial")){
            c++;
        }
        dealThreadResult(c);
        final Map<String,LinkedList> mapData = new HashMap();
        if(questionList==null){
            logger.error("无可识别的试题内容");
            this.setLoggerList(PaperUploadError.builder()
                    .errorMsg("无可识别的试题内容").errorType("error").floor(logger.getName()).build());
        }
        mapData.put("questionList",questionList);
        return mapData;
    }
    private void dealThreadResult(int c) throws Exception{
        while(getSuccessSeq()<getQuestionSeq()-c){
            final Map tempMap  = questionsCheckService.getMapData();
            if(tempMap!=null){
                if(!"null".equals(String.valueOf(tempMap.get("qcount")))){
                    int count = Integer.parseInt(String.valueOf(tempMap.get("qcount")));
                    this.incQuestinCount(count);
                }
                boolean flag = setSuccessSeq(Integer.parseInt(String.valueOf(tempMap.get("sequence"))));
                if(tempMap.get("loggerList")!=null&&flag){
                    addLoggerList((List)tempMap.get("loggerList"));
                }
                Thread.currentThread().sleep(10);
            }
        }
    }
    private boolean checkTailFlag(Map mapFlag, String eleStr, LinkedList<Map> questionList) throws Exception {
        Pattern pattern = Pattern.compile("【"+ PaperAttr.QUESTION_FLAG+"】");
        Matcher matcher = pattern.matcher(eleStr.trim());
        if(matcher.find()){
            mapFlag.put("flag","2");
            mapFlag.put("type1",-1);
            mapFlag.put("type2",-1);
            mapFlag.put("tail",null);
            mapFlag.put("pre",null);
            addQuestionOperation(mapFlag,questionList,new HashMap());
            return true;
        }
        return false;
    }

    /**
     * 处理材料的初始段落存储（资料在复合客观题中只有一个，而单一主观题和复合主观题中可以是多个），
     * 由于无法区分是复合主观题还是复合客观题，资料信息暂时都以集合形式存储
     * 【规定】所有以“资料”+数字+“.”("、")开头的段落都是资料的起始段落
     * @param mapFlag
     * @param eleStr
     * @param questionList
     * @return
     */
    private boolean adapterMaterial(Map mapFlag, String eleStr, LinkedList<Map> questionList) throws Exception {
        boolean flag1 = false;
        boolean flag2 = false;
        if(!"1".equals(mapFlag.get("flag"))){
            return false;
        }
        if(mapFlag.get("tail").toString().contains("material")){
            flag1 = true;
        }
        //通过tail来判断材料部分是否开始或着通过已有的材料列表添加新的材料
        if(mapFlag.get("tail").toString().contains("materials")||((mapFlag.get("pre") instanceof List)&&((List) mapFlag.get("pre")).get(0) instanceof String)){
            flag2 = true;
        }
        if(!(flag1||flag2)){
            return false;
        }
        Pattern pattern = Pattern.compile("^资料([\\d])+[：|:]");
        Matcher matcher = pattern.matcher(eleStr.trim());
        if(matcher.find()){
            String material = packTagInfo(eleStr.trim());
            Map questionMap = questionList.getLast();
            if("105".equals(mapFlag.get("type1").toString())){
                questionMap.put("material",material);
                mapFlag.put("pre","material");
                mapFlag.put("tail","analyze");
            }else{
                List<String> materialsList = null;
                if(questionMap.get("materials")==null){
                    materialsList  = new ArrayList();
                    questionMap.put("materials",materialsList);
                }else{
                    materialsList = (List)questionMap.get("materials");
                }
                materialsList.add(material);
                mapFlag.put("pre",materialsList);
                if("106".equals(mapFlag.get("type1").toString())){
                    mapFlag.put("tail","material,stem");
                }else{
                    mapFlag.put("tail","material,analyze");
                }
            }
            return true;
        }
        return false;
    }
    /**
     * 适配注意事项内容（只存在单一主观题和复合主观题的主干部分上）
     * 【规定】注意事项的确认标识是“注意事项：”
     * @param mapFlag
     * @param eleStr
     * @param questionList
     * @return
     */
    private boolean adapterRequire(Map mapFlag, String eleStr, LinkedList<Map> questionList) throws Exception{
        boolean flag  = false;
        if(!mapFlag.get("tail").toString().contains("require")||!"1".equals(mapFlag.get("flag"))){
            return false;
        }
        Pattern pattern = Pattern.compile("^注意事项[：|:]");
        Matcher matcher = pattern.matcher(eleStr.trim());
        if(matcher.find()){
            Map questionMap = questionList.getLast();
            String require = packTagInfo(eleStr.trim().substring(matcher.end()));
            questionMap.put("require",require);
            mapFlag.put("pre","require");
            if("106".equals(mapFlag.get("type1").toString())){
                mapFlag.put("tail","materials,stem");
            }else{
                mapFlag.put("tail","materials");
            }
            return true;
        }
        return flag;
    }

    /**
     * 判断上传的数据是否是开始结束标识
     * @param mapFlag
     * @param eleStr
     * @param questionList
     * @return
     */
    private boolean checkBranch(Map mapFlag, String eleStr, LinkedList<Map> questionList)throws Exception{
        Pattern pattern = Pattern.compile("^【"+PaperAttr.QUESTION_QT_QRT+"】");
        Matcher matcher = pattern.matcher(eleStr.trim());
        if(matcher.find()){
            String typeName = eleStr.trim().substring(matcher.end());
            int type = -1;
            if(PaperAttr.SINGLE_OBJECTIVE0.equals(typeName)){
                type = 99;
            }else if(PaperAttr.SINGLE_OBJECTIVE2.equals(typeName)){
                type = 101;
            }else if(PaperAttr.SINGLE_OBJECTIVE1.equals(typeName)){
                type = 100;
            }else if(PaperAttr.SINGLE_OBJECTIVE3.equals(typeName)){
                type = 109;
            }else if(PaperAttr.SINGLE_SUBJECTIVE.contains(typeName)){
                type = 106;
            }else if(PaperAttr.MULTI_SUBJECTIVE.equals(typeName)){
                type = 107;
            }else if(PaperAttr.MULTI_OBJECTIVE.equals(typeName)){
                type = 105;
            }
            if ("1".equals(mapFlag.get("flag"))&&("105".equals(mapFlag.get("type1").toString())||"107".equals(mapFlag.get("type1").toString()))){
                //如果是复合题，并且处于录题状态，证明是子题的题型标签
                Map quetionMap = questionList.getLast();
                List subQuestionList= null;
                Map subQuestionMap = new HashMap();
                if(quetionMap.get("subQuestionStr")==null){
                    subQuestionList = new ArrayList();
                    quetionMap.put("subQuestionStr",subQuestionList);
                    float sequence = Float.parseFloat(String.valueOf(quetionMap.get("sequence")));
                    subQuestionMap.put("sequence",((float)Math.round((sequence+0.01)*100))/100);
                }else{
                    subQuestionList = (List)quetionMap.get("subQuestionStr");
                    subQuestionMap.put("sequence",((float)Math.round((getSubQuestionSeq(quetionMap)+0.01)*100))/100);
                }
                if(type==-1){
                    String error = getInitialError(quetionMap,String.valueOf(subQuestionList.size()+1))+"题型不规范";
                    logger.error(error);
                    this.setLoggerList(PaperUploadError.builder()
                            .floor(logger.getName())
                            .errorType("error").errorMsg(error).build());
                }
                subQuestionMap.put("type",type);
                subQuestionList.add(subQuestionMap);
                mapFlag.put("type2",type);
            }else{
                //主题的题型标签
                Map questionMap = new HashMap();
                questionMap.put("sequence",getIncQuestionSeq());
                questionMap.put("moduleId",mapFlag.get("moduleId"));
                questionMap.put("type",type);
                mapFlag.put("flag","1");
                mapFlag.put("type1",type);
                mapFlag.put("type2",-1);
                if(type==-1){
                    String error = getInitialError(questionMap,"")+"题型不规范";
                    logger.error(error);
                    this.setLoggerList(PaperUploadError.builder()
                            .floor(logger.getName())
                            .errorType("error").errorMsg(error).build());
                }
                addQuestionOperation(mapFlag, questionList,questionMap);
            }
            if(type==105){
                //接下来遇到的标签一定是材料
                mapFlag.put("tail","material");
            }else if(type==107){
                //接下来遇到的标签可能是注意事项和材料
                mapFlag.put("tail","require,material");
            }else if(type==106){
                //接下来
                mapFlag.put("tail","require,stem");
            }else{
                mapFlag.put("tail","stem");
            }
            mapFlag.put("pre",null);
            return true;
        }
        if("2".equals(mapFlag.get("flag"))){
            mapFlag.put("flag","4");    //如果之前题目已经结束，且新题目还没开始，就记flag = 4
        }
        if("4".equals(mapFlag.get("flag"))){
            return true;
        }
        return false;
    }

    /**
     * 判断是否为模块开头标识（此标识到下一题之间的字段都为无用信息，需要屏蔽）
     *
     * @param mapFlag
     * @param eleStr
     * @param moduleAttrList
     * @return
     */
    private boolean checkElePre(Map mapFlag, String eleStr, LinkedList<Map> moduleAttrList) throws Exception {
        Pattern pattern = Pattern.compile("【"+PaperAttr.QUESTION_MODULE+"】");
        Matcher matcher = pattern.matcher(eleStr.trim());
        if(matcher.find()){
            Set<Integer> moduleSet = null;
            if(mapFlag.get("moduleSet")==null){
                moduleSet = new HashSet<Integer>();
                mapFlag.put("moduleSet",moduleSet);
            }else{
                moduleSet = (Set)mapFlag.get("moduleSet");
            }
            String name = eleStr.trim().substring(matcher.end());
            mapFlag.put("moduleName",name);
            boolean match = false;
            for(PaperModuleBean module:getModuleList()){
                if(name.contains(module.getName())){
                    if(!moduleSet.contains(module.getId())){
                        Map map = new HashMap();
                        map.put("mid",module.getId());
                        map.put("name",module.getName());
                        moduleAttrList.addLast(map);
                        moduleSet.add(module.getId());
                    }
                    mapFlag.put("moduleId",module.getId());
                    mapFlag.put("tail","type");
                    mapFlag.put("pre",null);
                    match = true;
                    break;
                }
            }
            if(!match){
                logger.error("模块\""+name+"\"不存在");
                this.setLoggerList(PaperUploadError.builder()
                        .floor(logger.getName())
                        .errorType("error").errorMsg("模块\""+name+"\"不存在").build());
            }
            return true;
        }
        return  false;
    }

    private void concatElement(String eleStr, LinkedList<Map> questionList, Map mapFlag) throws Exception{
        if(!"1".equals(mapFlag.get("flag"))||mapFlag.get("pre")==null){
            return;
        }
        if(questionList.size()==0){
            return;
        }
        if(eleStr.trim()==null||eleStr.trim()==""){
            return;
        }
        Map questionMap= questionList.getLast();
        if(mapFlag.get("pre") instanceof ArrayList){
            List preList  = (List)mapFlag.get("pre");
            int preSize = preList.size();
            if(preList.get(preSize-1) instanceof Map){
                Map tailMap = (Map)preList.get(preSize-1);
                char isOpt = (char)('A'+preSize-1);
                if(tailMap.get("opt")!=null&&tailMap.get("opt").equals(isOpt+"")){
                    String tailCon = tailMap.get("con")+packTagInfo(eleStr);
                    tailMap.put("con",tailCon);
                }
            }else if(preList.get(preSize-1) instanceof String){
                String tailString = (String)preList.get(preSize-1);
                tailString = tailString + packTagInfo(eleStr);
                preList.set(preSize-1,tailString);
            }
        }else if(mapFlag.get("pre") instanceof String){
            String pre = mapFlag.get("pre").toString();
            String value = "";
            if(UpLoadAttr.textSet.contains(pre)){
                if("-1".equals(mapFlag.get("type2").toString())){
                    value = questionMap.get(pre)+packTagInfo(eleStr);
                    questionMap.put(pre,value);
                }else{
                    List subQuestionList = (List)questionMap.get("subQuestionStr");
                    Map subQuestionMap = (Map)subQuestionList.get(subQuestionList.size()-1);
                    value = subQuestionMap.get(pre)+packTagInfo(eleStr);
                    subQuestionMap.put(pre,value);
                }

            }else{
                if("-1".equals(mapFlag.get("type2").toString())){
                    value = questionMap.get(pre)+eleStr;
                    questionMap.put(pre,value);
                }else{
                    List subQuestionList = (List)questionMap.get("subQuestionStr");
                    Map subQuestionMap = (Map)subQuestionList.get(subQuestionList.size()-1);
                    value = subQuestionMap.get(pre)+eleStr;
                    subQuestionMap.put(pre,value);
                }
            }
        }
    }
    /**
     * 适配解析、答案等信息
     *【规定】所有题目的扩展信息包括答案、解析、分数，作者等都以“【+属性象征词（特定的属相表示名词）+】”作为段落的开头来声明
     * @param mapFlag
     * @param eleStr
     * @param questionList
     * @return
     * @throws Exception
     */
    private boolean adapterAnalyze(Map mapFlag, String eleStr, LinkedList<Map> questionList) throws Exception{
        boolean flag  = false;
        if(!"1".equals(mapFlag.get("flag"))){
            return false;
        }
        if(mapFlag.get("tail")==null){
            return false;
        }
        if(!String.valueOf(mapFlag.get("tail")).contains("analyze")){
            return false;
        }
        Pattern pattern = Pattern.compile("^【([\u4e00-\u9fa5]+)】");
        Matcher matcher = pattern.matcher(eleStr.trim());
        //适配所有以【】标识对的扩展信息
        if(matcher.find()){
            Map questionMap= questionList.getLast();
            String tmp = matcher.group(1);
            //标签信息包含答案，但不包含
            if(tmp.equals(PaperAttr.QUESTION_ANSWER)){
                String answer = eleStr.trim().substring(matcher.end())==null?"":eleStr.trim().substring(matcher.end());
                if("".equals(answer)){
                    String error ="第"+questionList.size()+"道题的答案不能为空";
                    logger.error(error);
                    setLoggerList(error,questionList.getLast());
                }else{
                    addAnswerInfo(mapFlag,questionMap,answer);
                }
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_POINT)){
                //【知识点】一层知识点--二层知识点--三级知识点（先不做处理）
                if(eleStr.trim().substring(matcher.end())==null||"".equals(eleStr.trim().substring(matcher.end()))){
                    String error ="第"+questionList.size()+"道题的知识点不能为空";
                    logger.error(error);
                    setLoggerList(error,questionList.getLast());
                }
                String point = eleStr.trim().substring(matcher.end()).replace("&nbsp;","").replace("&mdash;","-").replace("--","-");
                addGlobalAttr(mapFlag,questionMap,point,"point");
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_DIFFICULT)){
                if(eleStr.trim().substring(matcher.end())==null||"".equals(eleStr.trim().substring(matcher.end()))){
                    String error ="第"+questionList.size()+"道题的困难系数不能为空";
                    logger.error(error);
                    setLoggerList(error,questionList.getLast());
                }
                String diff = eleStr.trim().substring(matcher.end()).trim();
                addDifficutAttr(mapFlag,questionMap,diff);
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_AUTHOR)){
                String author = eleStr.trim().substring(matcher.end())==null?"":eleStr.trim().substring(matcher.end());
                addGlobalAttr(mapFlag,questionMap,author,"author");
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_REVIEW)){
                String review = eleStr.trim().substring(matcher.end())==null?"":eleStr.trim().substring(matcher.end());
                addGlobalAttr(mapFlag,questionMap,review,"review");
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_TEACHTYPE)){
                String teachTypeName = eleStr.trim().substring(matcher.end())==null?"":eleStr.trim().substring(matcher.end());
                addGlobalAttr(mapFlag,questionMap,teachTypeName,"teachType");
                return true;
            }
            if(tmp.equals(PaperAttr.QUESTION_ANALYSIS)){
                String analysis =packTagInfo(eleStr.trim().substring(matcher.end()));
                addGlobalAttr(mapFlag,questionMap,analysis,"analysis");
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_SCORE)) {
                if(eleStr.trim().substring(matcher.end())==null||"".equals(eleStr.trim().substring(matcher.end()))){
                    String error ="第"+questionList.size()+"道题的分数不能为空";
                    logger.error(error);
                    this.setLoggerList(error,questionMap);
                }
                String scoreStr =eleStr.trim().substring(matcher.end()).trim();
                addGlobalAttr(mapFlag,questionMap,scoreStr,"score");
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_LIMMIT_MAX)) {
                String limit = eleStr.trim().substring(matcher.end())==null?"":eleStr.trim().substring(matcher.end());
                addLimitInfo(mapFlag,questionMap,limit,1);
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_LIMMIT_MIN)) {
                String limit = eleStr.trim().substring(matcher.end())==null?"":eleStr.trim().substring(matcher.end());
                addLimitInfo(mapFlag,questionMap,limit,0);
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_SCOREEXPLAIN)){
                String scoreExplain = packTagInfo(eleStr.trim().substring(matcher.end()).trim());
                addGlobalAttr(mapFlag,questionMap,scoreExplain,"scoreExplain");
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_REFERANALYSIS)){
                String referAnalysis = packTagInfo(eleStr.trim().substring(matcher.end()).trim());
                addGlobalAttr(mapFlag,questionMap,referAnalysis,"referAnalysis");
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_EXAMPOINT)){
                String examPoint = packTagInfo(eleStr.trim().substring(matcher.end()).trim());
                addGlobalAttr(mapFlag,questionMap,examPoint,"examPoint");
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_ANSWERREQUIRE)){
                String answerRequire = packTagInfo(eleStr.trim().substring(matcher.end()).trim());
                addGlobalAttr(mapFlag,questionMap,answerRequire,"answerRequire");
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_SOLVINGIDEA)){
                String solvingIdea = packTagInfo(eleStr.trim().substring(matcher.end()).trim());
                addGlobalAttr(mapFlag,questionMap,solvingIdea,"solvingIdea");
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_ORGIN)){
                String orgin = packTagInfo(eleStr.trim().substring(matcher.end()).trim());
                addGlobalAttr(mapFlag,questionMap,orgin,"orgin");
                return true;
            }
            if(tmp.contains(PaperAttr.QUESTION_EXPAND)){
                String expand = packTagInfo(eleStr.trim().substring(matcher.end()).trim());
                addGlobalAttr(mapFlag,questionMap,expand,"expand");
                return true;
            }
            if("-1".equals(String.valueOf(mapFlag.get("paperId")))){
                if(tmp.contains(PaperAttr.PAPER_SUBJECT)){
                    String expand = eleStr.trim().substring(matcher.end()).trim();
                    addGlobalAttr(mapFlag,questionMap,expand,"catgory");
                    return true;
                }
                if(tmp.contains(PaperAttr.PAPER_AREA)){
                    String expand = eleStr.trim().substring(matcher.end()).trim();
                    addGlobalAttr(mapFlag,questionMap,expand,"areas");
                    return true;
                }
                if(tmp.contains(PaperAttr.PAPER_YEAR)){
                    String expand = eleStr.trim().substring(matcher.end()).trim();
                    addGlobalAttr(mapFlag,questionMap,expand,"year");
                    return true;
                }
                if(tmp.contains(PaperAttr.PAPER_TYPE)){
                    String expand = eleStr.trim().substring(matcher.end()).trim();
                    addGlobalAttr(mapFlag,questionMap,expand,"mode");
                    return true;
                }
            }
            if("-1".equals(String.valueOf(mapFlag.get("moduleId")))){
                if(tmp.contains(PaperAttr.QUESTION_MODULE_BRANCH)){
                    String expand = eleStr.trim().substring(matcher.end()).trim();
                    addGlobalAttr(mapFlag,questionMap,expand,"moduleName");
                    return true;
                }
            }
            String error = "未识别信息：>>>>"+eleStr;
            logger.error(error);
            String stem = "";
            if(questionMap.get("stem")!=null){
                stem = getStemContent(questionMap.get("stem").toString());
            }
            this.setLoggerList("waring",error,logger.getName(),stem);
            return true;
        }
        return flag;
    }
    /**
     * 字数限制是针对主观题的
     * @param mapFlag
     * @param questionMap
     * @param limit
     */
    private void addLimitInfo(Map mapFlag, Map questionMap, String limit,int l) throws Exception {
        String error = "";
        if(!"-1".equals(mapFlag.get("type2").toString())){
            error = "第"+questionMap.get("sequence")+"道题，第"+((List)questionMap.get("subQuestionStr")).size()+"道子题的";
        }else{
            error = "第"+questionMap.get("sequence")+"道题的";
        }
        boolean isDigit = true;
        for (int i = 0; i < limit.length(); i++){
            if (!Character.isDigit(limit.charAt(i))){
                error = error+ "分数"+((l==0)?"下限":"上限")+"不是纯数字";
                logger.error(error);
                this.setLoggerList(error,questionMap);
                isDigit =false;
            }
        }
        if(!isDigit){
            mapFlag.put("pre","limit");
            return;
        }
        if(!"-1".equals(mapFlag.get("type2").toString())){
            List subQuestionList = (List)questionMap.get("subQuestionStr");
            Map subQuestionMap = (Map)subQuestionList.get(subQuestionList.size()-1);
            subQuestionMap.put(((l==0)?"minWordCount":"maxWordCount"),Integer.parseInt(limit));
        }else{
            questionMap.put(((l==0)?"minWordCount":"maxWordCount"),Integer.parseInt(limit));
        }
        mapFlag.put("pre","limit");
    }

    /**
     * 该功能处理一些通用元素的初始部分的录入，这些元素对的特点是：作为复合题只存在子题涨，作为单一子题的存在题本身上
     * @param mapFlag
     * @param questionMap
     * @param info
     * @param tmp
     */
    private void addGlobalAttr(Map mapFlag, Map questionMap, Object info, String tmp) {
        if(!"-1".equals(mapFlag.get("type2").toString())){
            List subQuestionList = (List)questionMap.get("subQuestionStr");
            Map subQuestionMap = (Map)subQuestionList.get(subQuestionList.size()-1);
            subQuestionMap.put(tmp,info);
        }else{
            questionMap.put(tmp,info);
        }
        mapFlag.put("pre",tmp);
    }

    /**
     * 添加难度属性，单一客观题，复合客观题主题和所有子客观题都有
     * @param mapFlag
     * @param questionMap
     * @param diff
     */
    private void addDifficutAttr(Map mapFlag, Map questionMap, String diff) {
        int difficult = 0;
        if("简单".equals(diff)){
            difficult = 2;
        }else if("较易".equals(diff)){
            difficult = 4;
        }else if("中等".equals(diff)||"一般".equals(diff)){
            difficult = 6;
        }else if("较难".equals(diff)){
            difficult = 8;
        }else if("困难".equals(diff)||"难".equals(diff)){
            difficult = 10;
        }
        //当为客观子题时放入子题对应的Map里，否则放在主题干Map里
        if(!"-1".equals(mapFlag.get("type2").toString())&&questionMap.get("subQuestionStr")!=null){
            List subQuestionList = (List)questionMap.get("subQuestionStr");
            Map subQuestionMap = (Map)subQuestionList.get(subQuestionList.size()-1);
            subQuestionMap.put("difficult",difficult);
        }else{
            questionMap.put("difficult",difficult);
        }
        mapFlag.put("pre","difficult");
    }
    /**
     * 处理答案字段信息存储
     * @param mapFlag
     * @param questionMap
     * @param answer
     */
    private void addAnswerInfo(Map mapFlag, Map questionMap, String answer) {
        char[] answers = answer.toCharArray();
        String error = "";
        if(!"-1".equals(mapFlag.get("type2").toString())){
            List list = (List)questionMap.get("subQuestionStr");
            Map subQuestionMap = (Map)list.get(list.size()-1);
            error = "第"+questionMap.get("sequence")+"道题的第"+subQuestionMap.get("sequence")+"子题的";
            getAnswer(subQuestionMap,answers,error);
        }else {
            error = "第" + questionMap.get("sequence") + "道题的";
            getAnswer(questionMap,answers,error);
        }
        mapFlag.put("pre","answer");
    }
    public void getAnswer(Map questionMap,char[] answers,String error){
        int tmpAnswer = 0;
        if(questionMap.get("options")==null) {
            String error0 = error + "选项内容不能为空";
            logger.error(error0);
            this.setLoggerList(error0,questionMap);
            return;
        }
        List options = (List)questionMap.get("options");
        int size = options.size();
        for(int i= 0; i<answers.length;i++){
            if('A'<=answers[i]&&'Z'>=answers[i]){
                int l = answers[i]-'A';
                if(size-1<l){
                    String error0 = error +"道题的选项数量不对";
                    logger.error(error0);
                    this.setLoggerList(error0,questionMap);
                }
                try{
                    Map map = (Map)options.get(l);
                    String tmp = answers[i]+"";
                    if(!tmp.equals(map.get("opt"))){
                        String error0 = error +"道题的选项与答案不符";
                        logger.error(error0);
                        this.setLoggerList(error0,questionMap);
                    }else{
                        map.put("isSelect","1");
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                tmpAnswer = tmpAnswer*10+l+1;
            }
        }
        questionMap.put("answer",tmpAnswer);
        if(!checkOptions(questionMap,error)){
            String error0 = error +"道题的答案与题型不匹配";
            logger.error(error0);
            this.setLoggerList(error0,questionMap);
        }
    }

    /**
     * 适配并获取选项及内容(单一客观题，复合客观题，复合主观题的子客观题)
     *【规定】所有以A-Z加“、”或“.”为开头的段落都是选项部分内容，其他内容尽量规避这方面的设定
     * @param mapFlag
     * @param eleStr
     * @param questionList
     * @return
     */
    private boolean adapterOption(Map mapFlag, String eleStr, LinkedList<Map> questionList) throws Exception {
        boolean flag = false;
        if(!"1".equals(mapFlag.get("flag"))){
            return false;
        }
        if(!("options".equals(mapFlag.get("tail"))||((mapFlag.get("pre") instanceof List)&&((List)mapFlag.get("pre")).get(0) instanceof  Map))){
            return false;
        }
        Pattern pattern = Pattern.compile("^([A-Z])[\\s]*[．|.|、]");
        Matcher matcher = pattern.matcher(eleStr.trim());
        //适配选项信息
        if(matcher.find()){
            Map questionMap= questionList.getLast();
            List optionsList;
            //判定是否是复合题，是就是复合题的子客观题，否则是单一客观题
            if(!"-1".equals(mapFlag.get("type2").toString())){
                List subQuestions = null ;
                Map subQuestion = null;
                //复合客观题的处理到选项部分，证明已有题干录入，不应该为空，所以这里不做判断
                subQuestions = (List)questionMap.get("subQuestionStr");
                subQuestion = (Map)subQuestions.get(subQuestions.size()-1);
                //如果选项集合为空，证明是处理到第一个选项处，先建集合，再存数
                if(subQuestion.get("options")==null){
                    optionsList =  new ArrayList();
                    //将整个list作为元素，存在options映射值中
                    subQuestion.put("options",optionsList);
                }else{
                    optionsList = (List)subQuestion.get("options");
                }
            }else{
                //单一客观题只需处理试题下直属的options集合就好
                if(questionMap.get("options")==null){
                    optionsList = new ArrayList();
                    //将整个list作为元素，存在options映射值中
                    questionMap.put("options",optionsList);
                }else{
                    optionsList = (List)questionMap.get("options");
                }
            }
            Map<String,Object> optionsMap = new HashMap<String,Object>();
            String tmp = matcher.group();
            //得到选项和选项内容
            optionsMap.put("opt",tmp.substring(0,1));
            optionsMap.put("con",packTagInfo(eleStr.substring(matcher.end())));
            optionsMap.put("isSelect",0);
            optionsList.add(optionsMap);
            //由于选项是List结构，结尾表示指向List地址才能确切定位到该对象
            mapFlag.put("pre",optionsList);
            mapFlag.put("tail","analyze");
            return true;
        }
        return flag;
    }

    /**
     * 适配题干内容,题干所有题型都有所以各种情况都得考虑
     * 【规定】所有以数字+“、”或者“.”开头的段落都是题干起始段落
     * @param mapFlag
     * @param eleStr
     * @param questionList
     * @return
     */
    private boolean adapterStem(Map mapFlag, String eleStr, LinkedList<Map> questionList) throws Exception{
        boolean flag =false;
        if(!mapFlag.get("tail").toString().contains("stem")||!"1".equals(mapFlag.get("flag"))){
            return false;
        }
        String img = "";
        Pattern pattern = Pattern.compile("^<img[^>]+>");
        Matcher matcher = pattern.matcher(eleStr.trim());
        int i = 0;
        while(matcher.find(i)){
            i=matcher.end();
        }
        if(i>0){
            img = eleStr.trim().substring(0,i);
            eleStr = eleStr.trim().substring(i);
        }
        pattern = Pattern.compile("^(\\d+)[\\s]*[、|.|．]");
        matcher = pattern.matcher(eleStr.trim());
        //适配一道题的开始标识
        if(matcher.find()){
            if("-1".equals(mapFlag.get("type2").toString())){
//          没有创建子题 ，即题目是单一题
                Map questionMap = questionList.getLast();
                questionMap.put("moduleId",mapFlag.get("moduleId"));
                if(i>0){
                    questionMap.put("stemImg",packTagInfo(img));
                }
                questionMap.put("stem",packTagInfo(eleStr.substring(matcher.end())));
                if("106".equals(mapFlag.get("type1").toString())){
                    //主观题的后面跟的是解析
                    mapFlag.put("tail","analyze");
                }else{
                    mapFlag.put("tail","options");
                }
            }else{
                Map questionMap = questionList.getLast();
                List subQuestionList = (List)questionMap.get("subQuestionStr");
                Map subQuestionMap = (Map)subQuestionList.get(subQuestionList.size()-1);
                //题干
                subQuestionMap.put("stem",packTagInfo(eleStr.substring(matcher.end())));
                subQuestionMap.put("moduleId",mapFlag.get("moduleId"));
                if("106".equals(mapFlag.get("type2").toString())){
                    //主观题的后面跟的是解析
                    mapFlag.put("tail","analyze");
                }else{
                    mapFlag.put("tail","options");
                }
            }
            mapFlag.put("pre","stem");
            return true;
        }
        return flag;
    }

    private float getSubQuestionSeq(Map quetionMap) {
        List subQuestionList = (List)quetionMap.get("subQuestionStr");
        float sequence = Float.parseFloat(String.valueOf(((Map)subQuestionList.get(subQuestionList.size()-1)).get("sequence")));
        return sequence;
    }

    /**
     * 将添加新题的操作提取出来，并附带处理一些其他的问题
     * @param mapFlag
     * @param questionList
     * @param questionMap
     */
    private void addQuestionOperation(Map mapFlag, LinkedList<Map> questionList, Map questionMap) throws Exception{
        if(questionMap!=null&&!questionMap.isEmpty()){
            questionMap.put("checkFlag","0");
        }
        if(questionList.size()==0){
            questionList.addLast(questionMap);
            return;
        }
        Map preQuestionMap = questionList.getLast();
        if(preQuestionMap!=null&&!preQuestionMap.isEmpty()&&"0".equals(preQuestionMap.get("checkFlag"))){
            preQuestionMap.put("pointList",this.getPointList());
            preQuestionMap.put("checkFlag","1");
            final Map mapData = preQuestionMap;
            questionsCheckService.checkQuestionByThread(mapData,mapFlag);
        }

        if(questionMap!=null||!questionMap.isEmpty()){
            questionList.addLast(questionMap);
        }
    }
    /**
     * 通过题型和答案比较校验题的格式是否冲突
     * @param questionMap
     * @return
     */
    private boolean checkOptions(Map questionMap,String error) {
        if(questionMap.get("type")==null){
            return false;
        }
        int type = (int)questionMap.get("type");
        Integer answer =Integer.parseInt(String.valueOf(questionMap.get("answer")));
        switch (type){
            case 99:{
                if(answer<0||answer>10){
                    return false;
                }
                break;
            }
            case 100:{
                if(answer<10){
                    return false;
                }
                break;
            }
            case 109:{
                if(answer!=1&&answer!=2){
                    return false;
                }
                break;
            }
            default:break;
        }
        return true;
    }
    /**
     * 将一条信息包装一个<p></p>标签
     * @param str
     * @return
     */
    private String packTagInfo(String str) throws Exception{
        if(str==null||"".equals(str.trim())){
            return "<p></p>";
        }
        return "<p>"+str+"</p>";
    }

    private void setLoggerList(String error, Map questionMap) {
        String stem = "";
        if(questionMap.get("stem")!=null){
            stem = questionMap.get("stem").toString();
            stem = getStemContent(stem);
        }else if(questionMap.get("material")!=null){
            stem = questionMap.get("material").toString();
            stem = getStemContent(stem);
        }else{
            logger.info("question={}",questionMap);
        }
        this.setLoggerList(PaperUploadError.builder()
                .floor(logger.getName())
                .errorType("error").errorMsg(error)
                .location(stem).build());
    }
    private String getStemContent(String stem) {
        Pattern pattern = Pattern.compile("^<p>([^<]+)</p>");
        Matcher matcher = pattern.matcher(stem);
        if(matcher.find()){
            return matcher.group(1);
        }
        return "";
    }
    private String getInitialError(Map preQuestionMap, String parentSeq) {
        String error = "";
        if("".equals(parentSeq)){
            error = "第"+preQuestionMap.get("sequence")+"道题的";
        }else{
            error = "第"+parentSeq+"道题的第"+preQuestionMap.get("sequence")+"道子题的";
        }
        return error;
    }
}
