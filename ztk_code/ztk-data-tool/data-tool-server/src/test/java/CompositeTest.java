import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.ztk.backend.BaseTestW;
import com.huatu.ztk.backend.mysql.dao.MultiSqlDao;
import com.huatu.ztk.backend.mysql.dao.PaperQuestionSqlDao;
import com.huatu.ztk.backend.mysql.dao.PaperSqlDao;
import com.huatu.ztk.backend.mysql.dao.QuestionSqlDao;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\4\3 0003.
 */
public class CompositeTest extends BaseTestW {
    private static final Logger logger = LoggerFactory.getLogger(CompositeTest.class);
    @Autowired
    private PaperQuestionSqlDao paperQuestionDao;
    @Autowired
    private PaperSqlDao paperSqlDao;
    @Autowired
    private MultiSqlDao multiSqlDao;
    @Autowired
    private QuestionSqlDao questionSqlDao;
//    @Test
//    public void test(){
//        /**
//         * 获取到所有更改过绑定关系的试卷位置（只统计复合题子题的）
//         */
//        List<Map> list = paperQuestionDao.findChangedBindings();
//        logger.info("list = {}", JsonUtil.toJson(list));
//        List<Map<String,Integer>> changeList = Lists.newArrayList();
//        for(Map map:list){
//            int paperId = Integer.parseInt(String.valueOf(map.get("paperId")));
//            int order = Integer.parseInt(String.valueOf(map.get("order")));
//            List<Map> questionList = paperQuestionDao.findQuestionByLocation(paperId,order);
//            checkChangedAndAddList(questionList,changeList);
//        }
//        logger.info("替换情况：{}",changeList);
//    }
//
//    private void checkChangedAndAddList(List<Map> questionList, List<Map<String, Integer>> changeList) {
//        if(CollectionUtils.isEmpty(questionList)){
//            return;
//        }
//        Map rightMap = questionList.stream().filter(question->Integer.parseInt(String.valueOf(question.get("status")))==1).findFirst().orElse(null);
//        if(rightMap==null){
////            logger.error("questionList 中无正确的绑定关系！！！{}",JsonUtil.toJson(questionList));
//            return;
//        }
//        questionList.removeIf(i-> rightMap.get("questionId").equals(i.get("questionId")));
//        if(CollectionUtils.isEmpty(questionList)){
////            logger.info("试卷的绑定关系没有发生替换，{}",rightMap);
//            return;
//        }
//        Map errorMap = getErrorMap(rightMap,questionList);
//        if(errorMap==null){
//            return;
//        }
//        Map<String,Integer> changeMap = Maps.newHashMap();
//        changeMap.put("oldId",Integer.parseInt(String.valueOf(errorMap.get("questionId"))));
//        changeMap.put("newId",Integer.parseInt(String.valueOf(rightMap.get("questionId"))));
//        changeMap.put("oldMid",Integer.parseInt(String.valueOf(errorMap.get("multiId"))));
//        changeMap.put("newMid",Integer.parseInt(String.valueOf(rightMap.get("multiId"))));
//        changeList.add(changeMap);
//    }
//
//    private boolean isCommonStem(Map map, String content) {
//        String stem = String.valueOf(map.get("stem"));
//        stem = stem.replaceAll("<[^>]+>","").replace("&nbsp;","");
//        int total = 0;
//        int patterned = 0;
//        for(int i= 0;i<stem.length();i=i+2){
//            total++;
//            if(content.contains(stem.charAt(i)+"")){
//                patterned ++;
//            }
//        }
//        if(total==0){
//            logger.error("没有题干的试题？,{}",stem);
//            return false;
//        }
//        map.put("similar",new Double(patterned)/total);
//        if(new Double(patterned)/total>0.8){
//            return true;
//        }
//        logger.info("content={}",content);
//        logger.info("stem={}",stem);
//        return false;
//    }
//
//    public Map getErrorMap(Map rightMap, List<Map> questionList) {
//        Map errorMap = Maps.newHashMap();
//        String content = String.valueOf(rightMap.get("stem"));
//        List<Map> errorList = Lists.newArrayList();
//        for (Map map : questionList) {
//            if(isCommonStem(map,content)){
//                errorList.add(map);
//            }
//        }
//        if(CollectionUtils.isEmpty(errorList)){
//            logger.error("没有匹配的重复试题");
//            return null;
//        }else if (errorList.size()==1){
//            errorMap.putAll(errorList.get(0));
//        }else{
//            errorList.sort((a,b)->(Double.parseDouble(String.valueOf(b.get("similar")))-Double.parseDouble(String.valueOf(a.get("similar")))>0?1:-1));
//            logger.info("有多个相似题，取相似度最大的一道：{}",errorList);
//            errorMap.putAll(errorList.get(0));
//        }
//        return errorMap;
//    }
    @Test
    public void test1(){
        //查询所有的试卷信息
        List<Integer> paperIds = paperSqlDao.findAllPaper();
        for(int id:paperIds){
//            if(id!=928){
//                continue;
//            }
            try {
                dealUnitRangePaper(id);
            } catch (BizException e) {
//                e.printStackTrace();
                logger.info("{}",e.getMessage());
//                break;
            }
        }

    }

    private void dealUnitRangePaper(int id) throws BizException {
        List<Map> list = paperQuestionDao.findSubObjectById(id);
        //multiId 和 正常子题个数
        Map<Integer,Integer> childMap = Maps.newHashMap();
        //复合题和 子题绑定记录
        Map<Integer,List<Map>> locationMap = Maps.newHashMap();
        //试题id 和 父id
        Map<Integer,Integer> parentMap = Maps.newHashMap();
        //题序 和 复合题对应关系
        Map<Integer,Set<Integer>> orderMultiMap = Maps.newHashMap();
        //题序 和 绑定的子题信息
        Map<Integer,Integer> orderBinddMap = Maps.newHashMap();
        Map<Integer,Integer> orderTimedMap = Maps.newHashMap();
        List<Integer> orders = Lists.newArrayList();
        for (Map map : list) {
            Integer multiId = Integer.parseInt(String.valueOf(map.get("multiId")));
            Integer questionId = Integer.parseInt(String.valueOf(map.get("questionId")));
            Integer status = Integer.parseInt(String.valueOf(map.get("status")));
            Integer order = Integer.parseInt(String.valueOf(map.get("order")));
            Integer time = Integer.parseInt(String.valueOf(map.get("time")));
            Set<Integer> multiIds = orderMultiMap.getOrDefault(order, Sets.newHashSet());
            multiIds.add(multiId);
            orderMultiMap.put(order,multiIds);
            if(status==1){
                if(orderBinddMap.get(order)==null){
                    orderBinddMap.put(order,questionId);
                    orderTimedMap.put(order,time);
                }else if(orderTimedMap.get(order)<time){
                    orderBinddMap.put(order,questionId);
                    orderTimedMap.put(order,time);
                }

                childMap.put(multiId,childMap.getOrDefault(multiId,0)+1);
                orders.add(order);
            }
            parentMap.put(questionId,multiId);
            List<Map> tempList =  locationMap.getOrDefault(multiId,Lists.newArrayList());
            tempList.add(map);
            locationMap.put(multiId,tempList);
        }
        //获取所有涉及到的复合题子题的复合题信息
        List<Map<Integer, List<Integer>>> multiList = multiSqlDao.findByIds(childMap.keySet().stream().collect(Collectors.toList()));
        Map<Integer,List<Integer>> multiMap = Maps.newHashMap();
        multiList.forEach(i->multiMap.putAll(i));
        orders.sort((a,b)->(a-b));
        //要替换的复合题子题id (key试题id。value为试题修改后的复合题id)
        Map<Integer,Integer> changeChildMap = Maps.newHashMap();
        //复合题id关联的修改后的子题集合 （key复合题id,value 修改后顺序排列的子题id）
        Map<Integer,List<Integer>> changeMultiMap = Maps.newHashMap();
        int k = 0;
        while(k<orders.size()){
            int order = orders.get(k);
            Set<Integer> multiIds = orderMultiMap.get(order);
            if(CollectionUtils.isEmpty(multiIds)){
                //删除绑定关系全是删除状态的复合题id
                multiIds.removeIf(i->childMap.get(i)==null);
            }
            if(multiIds.size()<2){
                k++;
                continue;
            }
            int realId = 0;
            int max = 0;
            for(int multiId:multiIds){
                int size = locationMap.get(multiId).stream().map(i->i.get("order")).collect(Collectors.toSet()).size();
                if(max<size){
                    max = size;
                    realId = multiId;
                }
            }
            List<Map> maps = locationMap.get(realId);
            maps.sort((a,b)->(Integer.parseInt(String.valueOf(a.get("order")))-Integer.parseInt(String.valueOf(b.get("order")))));
            List<Integer> newIds = Lists.newArrayList();
            boolean flag = false;
            for(Map map:maps){
                int subOrder = Integer.parseInt(String.valueOf(map.get("order")));
                if("-1".equals(String.valueOf(map.get("status")))){
                    if(orderBinddMap.get(subOrder)==null){
                        logger.info("paperId：{};order={}",id,subOrder);
                    }
                    changeChildMap.put(orderBinddMap.get(subOrder),realId);
                    if(!newIds.contains(orderBinddMap.get(subOrder))){
                        newIds.add(orderBinddMap.get(subOrder));
                    }

                    flag = true;
                }else{
                    if(!newIds.contains(Integer.parseInt(String.valueOf(map.get("questionId"))))){
                        newIds.add(Integer.parseInt(String.valueOf(map.get("questionId"))));
                    }
                }
                k = orders.indexOf(order);
            }
            if(flag){
                changeMultiMap.put(realId,newIds);
            }
            k++;
        }
        if(changeChildMap.size()!=0){
            logger.info("需要替换的单题有：{}",changeChildMap);
        }
        if(changeMultiMap.size()!=0){
            logger.info("需要修改替换的复合题有：{}",changeMultiMap);
        }

        questionSqlDao.updateChildQuestion(changeChildMap);
        multiSqlDao.updateChildIds(changeMultiMap);

        if(changeChildMap.size()!=0||changeMultiMap.size()!=0){
            throw new BizException(ErrorResult.create(1000001,"有修改，paperId="+id));
        }

    }
    @Test
    public void test(){
        List<Integer> paperIds = paperSqlDao.findAllPaper();
        Map<Integer,Integer> multiMap = Maps.newHashMap();
        multiSqlDao.findCount().forEach(i->multiMap.putAll(i));
        logger.info("试卷共有{}个",paperIds.size());
        Set<Integer> errorIds = Sets.newHashSet();
        for(int id:paperIds){
            List<Map> list = paperQuestionDao.findSubObjectById(id);
            Map<Integer,Integer> relationMap = Maps.newHashMap();
            for (Map map : list) {
                Integer multiId = Integer.parseInt(String.valueOf(map.get("multiId")));
//                Integer questionId = Integer.parseInt(String.valueOf(map.get("questionId")));
                Integer status = Integer.parseInt(String.valueOf(map.get("status")));
//                Integer order = Integer.parseInt(String.valueOf(map.get("order")));
//                Integer time = Integer.parseInt(String.valueOf(map.get("time")));
                if(status == 1){
                    relationMap.put(multiId,relationMap.getOrDefault(multiId,0)+1);
                }
            }
            for (Map.Entry<Integer, Integer> entry : relationMap.entrySet()) {
                int key = entry.getKey();
                if(relationMap.get(key)!=multiMap.get(key)){
                    errorIds.add(id);
                    break;
                }
            }
        }
        logger.info("出错试卷共有{}个",errorIds.size());
        logger.info("出错的试卷有{}",errorIds);
    }


}
