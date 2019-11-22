package com.huatu.ztk.backend.paperUpload.service;

import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paperModule.bean.PaperModuleBean;
import com.huatu.ztk.backend.paperModule.dao.PaperModuleDao;
import com.huatu.ztk.backend.paperUpload.bean.PaperAttrCollection;
import com.huatu.ztk.backend.paperUpload.bean.PaperUploadError;
import com.huatu.ztk.backend.paperUpload.dao.LogIterator;
import com.huatu.ztk.backend.question.bean.QuestionPointTreeMin;
import com.huatu.ztk.backend.question.dao.PointDao;
import com.huatu.ztk.backend.subject.bean.SubjectBean;
import com.huatu.ztk.backend.subject.dao.SubjectDao;
import com.huatu.ztk.backend.subject.service.SubjectService;
import com.huatu.ztk.commons.Area;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import org.bouncycastle.crypto.Digest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by lenovo on 2017/6/12.
 */
@Service
public class QuestionsCheckService extends LogIterator{
    private static Logger logger = LoggerFactory.getLogger(QuestionsCheckService.class);
    @Autowired
    private PointDao pointDao;
    @Autowired
    private PaperModuleDao paperModuleDao;
    @Autowired
    private SubjectService subjectService;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();
    private static final CompletionService<Map> cs = new ExecutorCompletionService<>(threadPool);
    private static ThreadLocal<List<QuestionPointTreeMin>> pointList = new ThreadLocal<>();
    public void copyPointList(List<QuestionPointTreeMin> list){
        pointList.set(list);
    }
    public List<QuestionPointTreeMin> getPointList(){
        return pointList.get();
    }
    public Map getMapData() throws Exception{
        Future<Map> future= cs.take();
        return future.get();
    }
    public void checkQuestionByThread(final Map mapData,Map mapFlag){
        final Map temp = mapFlag;
        cs.submit(()  -> {
            try{
                //判断、补充题目属性
                checkQuestion(mapData,temp);
            }catch(BizException e1){
                String error = "第"+mapData.get("sequence")+"道试题的";
                error+=e1.getErrorResult().getMessage();
                this.setLoggerList(PaperUploadError.builder()
                        .errorMsg(error).errorType("error")
                        .floor(logger.getName()).location(getLocation(mapData)).build());
            }catch(Exception e){
                e.printStackTrace();
                String error = "第"+mapData.get("sequence")+"道试题存在编译失败，请检查格式！";
                logger.error(error);
                this.setLoggerList(PaperUploadError.builder()
                        .errorMsg(error).errorType("error")
                        .floor(logger.getName()).location(getLocation(mapData)).build());
            }finally {
                mapData.put("loggerList", getLoggerList());
            }
            mapData.remove("pointList");
            return mapData;
        });
    }

    private String getLocation(Map mapData) {
        String location = "";
        if(mapData.get("stem")!=null){
            location = mapData.get("stem").toString();
        }else if(mapData.get("material")!=null){
            location = UnPackTag(mapData.get("material").toString());
        }else if(mapData.get("materials")!=null){
            location = UnPackTag(mapData.get("materials").toString());
        }
        return location;
    }

    private  void  checkQuestion(final Map preQuestionMap,final Map temp) throws Exception {
        //判断题型
        clearLoggerList();
        //如果是散题导入，则判断试题额外的属性（科目、地区、试题类别、模块……）
        if("-1".equals(String.valueOf(temp.get("paperId")))){
            preQuestionMap.put("questionFlag","0");
            int uid = Integer.parseInt(String.valueOf(temp.get("uid")));
            checkCommonAttr(preQuestionMap,uid);
            int catgory = Integer.parseInt(String.valueOf(preQuestionMap.get("catgory")));
            preQuestionMap.put("pointList",pointDao.findAllPonitsBySubject(catgory));
            preQuestionMap.put("moduleList",pointDao.findAllPonitsBySubject(catgory));
            checkModule(preQuestionMap,catgory);
            checkAreas(preQuestionMap);
            if(preQuestionMap.get("mode")!=null&&String.valueOf(preQuestionMap.get("mode")).contains("真题")){
                preQuestionMap.put("mode",1);
            }else{
                preQuestionMap.put("mode",2);
            }
        }
        int count = 0;
        this.copyPointList((List)preQuestionMap.get("pointList"));
        if(preQuestionMap.get("subQuestionStr")==null){
            if("106".equals(preQuestionMap.get("type").toString())){
                //检查单一主观题构成
                checkSubjectiveQuestion(preQuestionMap,"");
                count++;
            }else{
                //检查单一客观题构成
                checkObjectiveQuestion(preQuestionMap,"");
                count++;
            }
        }else{
            //检查复合题
            String error = "第"+preQuestionMap.get("sequence")+"道题的";
            if("105".equals(preQuestionMap.get("type").toString())){
                count+=checkObjCompositeQuestion(preQuestionMap);
            }else if("107".equals(preQuestionMap.get("type").toString())){
                count+=checkSubCompositeQuestion(preQuestionMap);
            }else{
                error = error + "题型与与题目信息不符";
                logger.error(error);
                this.setLoggerList(error,preQuestionMap);
            }
        }

        preQuestionMap.put("qcount",count);
    }

    private void checkModule(Map preQuestionMap,int catgory) {
        if(preQuestionMap.get("moduleName")==null){
            String error = "试题无模块标签";
            logger.error(error);
            this.setLoggerList(PaperUploadError.builder()
                    .floor(logger.getName())
                    .errorType("error").errorMsg(error)
                    .errorFlag("paperAttr").location(getLocation(preQuestionMap)).build());
        }else{
            List<PaperModuleBean> moduleList = paperModuleDao.findAvailableAll();
            String moduleName = String.valueOf(preQuestionMap.get("moduleName"));
            Map<String,Integer> mapTemp = moduleList.stream().filter(i->i.getSubject()==catgory).collect(Collectors.toMap(i->i.getName(),i->i.getId()));
            if(mapTemp.get(moduleName)!=null){
                preQuestionMap.put("moduleId",mapTemp.get(moduleName));
            }else{
                String error = "试题模块\""+moduleName+"\"不存在";
                logger.error(error);
                this.setLoggerList(PaperUploadError.builder()
                        .floor(logger.getName())
                        .errorType("error").errorMsg(error)
                        .errorFlag("paperAttr").location(getLocation(preQuestionMap)).build());
            }
        }
    }

    private void checkAreas(Map preQuestionMap) throws Exception{
        if(preQuestionMap.get("areas")==null){
            preQuestionMap.put("areas","-9");
            return;
        }
        List<Area> areaList = AreaConstants.getAreas(2);
        String name = String.valueOf(preQuestionMap.get("areas"));
        String[] names = name.split(",|，");
        String areas = "";
        for(String str :names){
            int l = getAreaId(str,areaList);
            if(l==-1){
                String error = "地区\""+str+"\"不可查";
                logger.error(error);
                this.setLoggerList(PaperUploadError.builder()
                        .floor(logger.getName())
                        .errorType("error").errorMsg(error)
                        .errorFlag("paperAttr").location(getLocation(preQuestionMap)).build());
            }
            areas = areas+l+",";
        }
        areas = areas.substring(0,areas.length()-1);
        preQuestionMap.put("areas",areas);
        int[] area = Arrays.stream(preQuestionMap.get("areas").toString().split(",")).mapToInt(Integer::valueOf).toArray();
        preQuestionMap.put("area",area[0]);
        preQuestionMap.put("reply",area.length);
    }
    private int getAreaId(String str, List<Area> areaList) throws Exception {
        int l = -1;
        for(Area area:areaList){
            if(str.contains(area.getName())){
                l= area.getId();
                return l;
            }
            if(area.getChildren()!=null){
                l = getAreaId(str,area.getChildren());
                if(l!=-1){
                    return l;
                }
            }

        }
        return l;
    }
    private void checkCommonAttr(Map preQuestionMap,int uid) throws BizException{
        if(preQuestionMap.get("catgory")==null){
            String error ="第"+preQuestionMap.get("sequence")+"道题的考试科目不能为空";
            logger.error(error);
            this.setLoggerList(PaperUploadError.builder()
                    .floor(logger.getName()).errorType("error").errorFlag("paperAttr")
                    .errorMsg(error).location(getLocation(preQuestionMap)).build());
            preQuestionMap.put("catgory",-1);
        }else{
            String subjectName = String.valueOf(preQuestionMap.get("catgory"));
            int catgory = getSubjectIdByName(subjectName,uid);
            preQuestionMap.put("catgory",catgory);
        }
        if(preQuestionMap.get("year")==null){
            preQuestionMap.put("year",-1);
        }else if(!isInteger(String.valueOf(preQuestionMap.get("year")))){
            String error ="年份：\""+preQuestionMap.get("year")+"\"格式不对";
            logger.error(error);
            this.setLoggerList(PaperUploadError.builder()
                    .floor(logger.getName()).errorType("error").errorFlag("paperAttr").location(getLocation(preQuestionMap))
                    .errorMsg(error).build());
        }
    }
    /**
    * 判断是否为整数
    * @param str 传入的字符串
    * @return 是整数返回true,否则返回false
    */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]+$");
        return pattern.matcher(str).matches();
    }
    /**
     * 判断是否为数字类型
     * @param str 传入的字符串
     * @return 是数字返回true,否则返回false
     */
    public static boolean isFloat(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*[\\.]?[\\d]+$");
        return pattern.matcher(str).matches();
    }
    public int  getSubjectIdByName(String name, int uid){
        List<SubjectBean> subjectList = subjectService.findList(0,uid);
        int subjectId = -1;
        for(SubjectBean subject : subjectList){
            if(name.equals(subject.getName())){
                subjectId = subject.getId();
                break;
            }
        }
        if(subjectId==-1){
            String error ="科目：\""+name+"\"不存在所选范围内";
            logger.error(error);
            this.setLoggerList(PaperUploadError.builder()
                    .floor(logger.getName()).errorType("error").errorFlag("paperAttr")
                    .errorMsg(error).build());
        }
        return subjectId;
    }
    /**
     * 对复合主观题做后期的校验和处理
     * @param preQuestionMap
     * @return
     */
    private int checkSubCompositeQuestion(Map preQuestionMap) throws Exception{
        //复合客观题的主干难度不能为空
        int i = 1;
        String error = "第"+preQuestionMap.get("sequence")+"道题的";
        if(preQuestionMap.get("materials")==null){
            error = error+"材料不能为空";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }
        dealSubjectiveNullAttr(preQuestionMap);
        if(preQuestionMap.get("subQuestionStr")==null){
            error = error+"没有相应的子题";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }else{
            List<Map> subQuestionList = (List)preQuestionMap.get("subQuestionStr");
            String parentSeq = (int)preQuestionMap.get("sequence")+"";
            List<Integer> subQuestionsType = new ArrayList();
            int score = 0;
            for(Map subQuestionMap:subQuestionList){
                score += Integer.parseInt(subQuestionMap.get("score").toString().trim());
                subQuestionMap.put("materials",preQuestionMap.get("materials"));
                if("0".equals(preQuestionMap.get("questionFlag"))){
                    subQuestionMap.put("catgory",preQuestionMap.get("catgory"));
                    subQuestionMap.put("area",preQuestionMap.get("area"));
                    subQuestionMap.put("year",preQuestionMap.get("year"));
                    subQuestionMap.put("mode",preQuestionMap.get("mode"));
                    subQuestionMap.put("moduleId",preQuestionMap.get("moduleId"));
                }
                if("106".equals(subQuestionMap.get("type").toString())){
                    checkSubjectiveQuestion(subQuestionMap,parentSeq);
                    subQuestionsType.add(1);
                }else{
                    checkObjectiveQuestion(subQuestionMap,parentSeq);
                    subQuestionsType.add(0);
                }
                i++;
            }
            preQuestionMap.put("subQuestionsType",subQuestionsType);
            preQuestionMap.put("score",score);
        }
        return i;
    }

    /**
     * 对主观题和复合题子主观题做后期的校验和处理
     * @param preQuestionMap
     * @param parentSeq
     */
    private void checkSubjectiveQuestion(Map preQuestionMap, String parentSeq) throws Exception{
        checkSingleQuestion(preQuestionMap,parentSeq);
        String error = getInitialError(preQuestionMap,parentSeq);
        if(!"106".equals(preQuestionMap.get("type").toString())){
            error = error + "题型与与题目信息不符";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }
        //参考解析不能为空
        if(preQuestionMap.get("referAnalysis")==null){
            error = error+"参考解析不能为空";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }
        dealSubjectiveNullAttr(preQuestionMap);
    }

    private void dealSubjectiveNullAttr(Map preQuestionMap) {
        if(preQuestionMap.get("scoreExplain")==null){
            preQuestionMap.put("scoreExplain","");
        }
        if(preQuestionMap.get("answerRequire")==null){
            preQuestionMap.put("answerRequire","");
        }
        if(preQuestionMap.get("examPoint")==null){
            preQuestionMap.put("examPoint","");
        }
        if(preQuestionMap.get("solvingIdea")==null){
            preQuestionMap.put("solvingIdea","");
        }
    }
    /**
     * 对复合客观题做后期的校验和处理
     * @param preQuestionMap
     * @return
     */
    private int checkObjCompositeQuestion(Map preQuestionMap) throws Exception{
        //复合客观题的主干难度不能为空
        int i =1;
        String error = "第"+preQuestionMap.get("sequence")+"道题的";
        if(preQuestionMap.get("difficult")==null){
            error = error+"难度系数不能为空";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }
        if(preQuestionMap.get("material")==null){
            error = error+"材料不能为空";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }
        if(preQuestionMap.get("subQuestionStr")==null){
            error = error+"没有相应的子题";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }else{
            List<Map> subQuestionList = (List)preQuestionMap.get("subQuestionStr");
            String parentSeq = preQuestionMap.get("sequence")+"";
            int score = 0;
            for(Map subQuestionMap:subQuestionList){
                if(subQuestionMap.get("score")!=null){
                    score += Float.parseFloat(subQuestionMap.get("score").toString().trim());
                }
                subQuestionMap.put("material",preQuestionMap.get("material"));
                if("0".equals(preQuestionMap.get("questionFlag"))){
                    subQuestionMap.put("moduleId",preQuestionMap.get("moduleId"));
                    subQuestionMap.put("catgory",preQuestionMap.get("catgory"));
                    subQuestionMap.put("area",preQuestionMap.get("area"));
                    subQuestionMap.put("mode",preQuestionMap.get("mode"));
                    subQuestionMap.put("year",preQuestionMap.get("year"));
                }
                checkObjectiveQuestion(subQuestionMap,parentSeq);
                i++;
            }
            preQuestionMap.put("score",score);

        }
        return i;
    }

    /**
     * 对复合题的客观子题做后期校验和处理
     * @param preQuestionMap
     * @param parentSeq
     * @throws Exception
     */
    private void checkObjectiveQuestion(Map preQuestionMap, String parentSeq) throws Exception{

        String error = getInitialError(preQuestionMap,parentSeq);
        if("106".equals(preQuestionMap.get("type"))){
            String error1 = error + "题型与与题目信息不符";
            logger.error(error1);
            this.setLoggerList(error1,preQuestionMap);
        }
        //判断选项不能为空
        if(preQuestionMap.get("options")==null){
            String error1 = error+"选项不存在";
            logger.error(error1);
            this.setLoggerList(error1,preQuestionMap);
//            logger.error("题干信息为："+preQuestionMap.get("stem").toString().replaceAll("<[/]?p>",""));
        }else{
            List optionList = (List) preQuestionMap.get("options");
            if(optionList.size()<2){
                String error1 = error+"选项内容不符合格式";
                logger.error(error1);
                this.setLoggerList(error1,preQuestionMap);
            }
            Map optionMap = (Map)optionList.get(optionList.size()-1);
            char maxOpt = (char)('A'+optionList.size()-1);
            if(!(""+maxOpt).equals(optionMap.get("opt"))){
                String error1 = error+"选项内容不符合格式";
                logger.error(error1);
                this.setLoggerList(error1,preQuestionMap);
            }
        }

        //答案不能为空，通过答案来确定选项的isselect选项
        if(preQuestionMap.get("answer")==null){
            String error1 = error+"答案不能为空";
            logger.error(error1);
            this.setLoggerList(error1,preQuestionMap);
        }
        //解析不能为空
        if(preQuestionMap.get("analysis")==null){
            String error1 = error+"解析不能为空";
            logger.error(error1);
            this.setLoggerList(error1,preQuestionMap);
//            logger.error("题干信息为："+preQuestionMap.get("stem").toString().replaceAll("<[/]?p>",""));
        }
        //检查模块的合法性
        checkPointName(preQuestionMap,parentSeq);
        //检查难度是否存在
        if(preQuestionMap.get("difficult")==null){
            String error1 = error+"难度系数不能为空";
            logger.error(error1);
            this.setLoggerList(error1,preQuestionMap);
        }else if(Integer.parseInt(String.valueOf(preQuestionMap.get("difficult")))==0){
            String error1 = error+"难度系数无法匹配";
            logger.error(error1);
            String stem = "";
            if(preQuestionMap.get("stem")!=null){
                stem = getStemContent(preQuestionMap.get("stem").toString());
            }
            this.setLoggerList(PaperUploadError.builder().errorFlag("difficult")
                    .errorType("error").errorMsg(error1).location(stem)
                    .floor(logger.getName()).build());
        }
        checkSingleQuestion(preQuestionMap,parentSeq);
    }
    private void checkPointName(Map preQuestionMap, String parentSeq) throws Exception {
        String error = "";
        if("".equals(parentSeq)){
            error = "第"+preQuestionMap.get("sequence")+"道题的";
        }else{
            error = "第"+parentSeq+"道题的第"+preQuestionMap.get("sequence")+"道子题的";
        }

        if(preQuestionMap.get("point")==null){
            error = error+"知识点信息不能为空";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }else{
            QuestionPointTreeMin point = null;
            String[] pointNames = preQuestionMap.get("point").toString().split("-");
            //知识点信息查询，确定知识点是在所选科目下，且事底层知识点
            Map map = null;
            for (QuestionPointTreeMin p:getPointList()){
                if(checkPointTree(p,pointNames,pointNames.length-1)){
                    if(map==null){
                        map = getPointTreeMap(p);
                    }else if(checkPointMapRepeat(map,getPointTreeMap(p))){
                        error = error+"知识点信息\""+preQuestionMap.get("point").toString()+"\"不能被唯一匹配";
                        logger.error(error);
                        this.setLoggerList(error,preQuestionMap);
                    }
                }
            }
            if(map==null){
                error = error+"知识点\""+preQuestionMap.get("point")+"\"不存在";
                logger.error(error);
                this.setLoggerList(error,preQuestionMap);
//                logger.error("题干信息为:"+preQuestionMap.get("stem"));
            }else{
                preQuestionMap.putAll(map);
            }
        }
    }
    private boolean checkPointTree(QuestionPointTreeMin p, String[] pointNames, int i) {
        if(i<0){
            return true;
        }
        if(p==null){
            return false;
        }
        if(p.getName().trim().equals(pointNames[i])){
            return checkPointTree(pointDao.findPointParentById(p.getId()),pointNames,i-1);
        }
        return false;
    }
    private Map getPointTreeMap(QuestionPointTreeMin p) {
        if(p.getLevel()<2){
            return null;
        }
        Map map = new HashMap();
        QuestionPointTreeMin temp =getPointLevel2(p);
        assemblePoints(map, temp);
        map.put("pointsName",new ArrayList<String>((LinkedList)map.get("pointsName")));
        map.put("pointsId",new ArrayList<Integer>((LinkedList)map.get("pointsId")));
        if(map.isEmpty()){
            return null;
        }
        return map;
    }
    private boolean checkPointMapRepeat(Map map, Map pointTreeMap) {
        List<Integer> list1 = (List) map.get("pointsId");
        List<Integer> list2 = (List) pointTreeMap.get("pointsId");
        if(pointTreeMap==null){
            return true;
        }
        for(int i=0;i<list1.size();i++){
            if(list1.get(i)!=list2.get(i)){
                return false;
            }
        }
        return true;
    }


    private void assemblePoints(Map preQuestionMap, QuestionPointTreeMin point) {
        if(point==null){
            return;
        }
        LinkedList<String> pointsName = null;
        LinkedList<Integer> pointsId = null;
        if(preQuestionMap.get("pointsName")==null){
            pointsName = new LinkedList<>();
            pointsId = new LinkedList<>();
            preQuestionMap.put("pointsName",pointsName);
            preQuestionMap.put("pointsId",pointsId);
        }else{
            pointsName = (LinkedList<String>) preQuestionMap.get("pointsName");
            pointsId = (LinkedList<Integer>) preQuestionMap.get("pointsId");
        }
        pointsName.addFirst(point.getName());
        pointsId.addFirst(point.getId());
        if(pointsName.size()!=3){
            assemblePoints(preQuestionMap,pointDao.findPointParentById(point.getId()));
        }
    }

    private QuestionPointTreeMin getPointLevel2(QuestionPointTreeMin p) {
        if(p.getLevel()<2){
            return null;
        }
        if(p.getLevel()==2){
            return p;
        }
        return getPointLevel2(pointDao.findPointParentById(p.getId()));
    }
    /**
     * 对复合题的子题和单一题的通用属性做后期校验和处理
     * @param preQuestionMap
     * @param parentSeq
     */
    private void checkSingleQuestion(Map preQuestionMap, String parentSeq) throws Exception{
        String error = getInitialError(preQuestionMap,parentSeq);
        if(preQuestionMap.get("stemImg")!=null){
            preQuestionMap.put("stem",preQuestionMap.get("stem")+""+preQuestionMap.get("stemImg"));
        }
        if(preQuestionMap.get("type")==null){
            error = error+"题型信息不能为空";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }
        //分数为空
        if(preQuestionMap.get("score")==null||"".equals(preQuestionMap.get("score"))){
            error = error+"分数不能为空";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }else if(!isFloat(String.valueOf(preQuestionMap.get("score")))){
            error = error+"分数\""+String.valueOf(preQuestionMap.get("score"))+"\"格式不对";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }
        //题干判断

        if(preQuestionMap.get("stem")==null){
            error = error+"题干不能为空";
            logger.error(error);
            this.setLoggerList(error,preQuestionMap);
        }

    }


    public String UnPackTag(String str){
        Pattern pattern = Pattern.compile("<p>([^<]+)</p>");
        Matcher matcher = pattern.matcher(str);
        int i = 0;
        while(matcher.find(i)){
            i=matcher.end();
            String temp = matcher.group(1);
            if(temp.indexOf("<img")==-1&&temp.length()>20){
                return temp.substring(0,20)+"……";
            }
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
    private void setLoggerList(String error, Map questionMap) {
        String stem = "";
        if(questionMap.get("stem")!=null){
            stem = questionMap.get("stem").toString();
            stem = getStemContent(stem);
        }else if(questionMap.get("material")!=null){
            stem =questionMap.get("material").toString();
            stem = getStemContent(stem).substring( 0,50 );
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

}
