import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.ztk.backend.BaseTestW;
import com.huatu.ztk.backend.mysql.dao.MultiSqlDao;
import com.huatu.ztk.backend.mysql.dao.PaperQuestionSqlDao;
import com.huatu.ztk.backend.mysql.dao.PaperSqlDao;
import com.huatu.ztk.backend.mysql.dao.QuestionSqlDao;
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
 * Created by huangqp on 2018\4\9 0009.
 */
public class CompositeUpdate extends BaseTestW {
    private static final Logger logger = LoggerFactory.getLogger(CompositeUpdate.class);
    @Autowired
    private PaperQuestionSqlDao paperQuestionDao;
    @Autowired
    private PaperSqlDao paperSqlDao;
    @Autowired
    private MultiSqlDao multiSqlDao;
    @Autowired
    private QuestionSqlDao questionSqlDao;

    /**
     * 三个维度去查询试卷，单题复合题
     */
    @Test
    public void test(){
        List<Map<Integer,List<Integer>>> multiIds =  multiSqlDao.findAll();
//        logger.info("msize={},mutiIds={}",multiIds.size(),multiIds);
        Map<Integer,List<Integer>> multiMap = Maps.newHashMap();
        multiIds.forEach(i->multiMap.putAll(i));
        Map<Integer,List<Integer>> updateMultiMap = Maps.newHashMap();
        Map<Integer,List<Integer>> multiSpecialMap = Maps.newHashMap();
        for(Map.Entry<Integer,List<Integer>> entry:multiMap.entrySet()){
            int multiId = entry.getKey();
            List<Integer> children = entry.getValue();
            List<Map<String,Integer>> questions = questionSqlDao.findByMulti(multiId);
            //改情况
            List<Map<String,Integer>> list = questions.stream().filter(i->i.get("flag")==0).collect(Collectors.toList());
            List<Integer> questionIds = list.stream().map(i->i.get("id")).collect(Collectors.toList());
            questionIds.sort((a,b)->(a-b));
            List<Map<String,Integer>> paperQuestions = paperQuestionDao.findByIdIn(questionIds);

            while(true){
                if(CollectionUtils.isEmpty(paperQuestions)){
                    logger.info("复合题下的试题没有在任意一道试卷中全部出现过 :multiId= {}",multiId);
                    multiSpecialMap.put(multiId,questionIds);
                    break;
                }
                int paperId = paperQuestions.get(0).get("paperId");
                List<Map<String,Integer>> subRelations = paperQuestions.stream().filter(i->i.get("paperId")==paperId).collect(Collectors.toList());
                Set<Integer> set = subRelations.stream().map(i->i.get("questionId")).collect(Collectors.toSet());
                if(set.size()!=list.size()){
                    paperQuestions.removeAll(subRelations);
                }else{
                    List<Integer> subIds = getSortedIds(subRelations);
                    logger.info("复合题{}的子题由{}改为{}",multiId,children,subIds);
                    updateMultiMap.put(multiId,subIds);
                    break;
                }
            }
        }
        multiSqlDao.updateChildIds(updateMultiMap);
        multiSqlDao.updateChildIds(multiSpecialMap);
    }

    private List<Integer> getSortedIds(List<Map<String, Integer>> subRelations) {
        List<Integer> list = Lists.newArrayList();
        subRelations.sort((a,b)->(a.get("order")-a.get("order")));
        for(Map<String, Integer> map:subRelations){
            if(!list.contains(map.get("questionId"))){
                list.add(map.get("questionId"));
            }
        }
        for(int i=list.size();i<10;i++){
            list.add(0);
        }
        return list;
    }
    @Test
    public void testChild(){
        List<Map<Integer,Integer>> list = questionSqlDao.findUpdate();
        Map<Integer,Integer> mapData = Maps.newHashMap();
        list.forEach(i->mapData.putAll(i));
        logger.info("mapdata.size={}",mapData.size());
        List<Integer> ids =mapData.keySet().stream().collect(Collectors.toList());
        Map<Integer,Set<Integer>> parentMap = Maps.newHashMap();
        for(int i=1;i<=10;i++){
            List<Map<Integer,Integer>> relations = multiSqlDao.findParentByIdIn(ids,i);
            for(Map<Integer,Integer> map:relations){
                Set<Integer> temp = parentMap.getOrDefault(map.get(1),Sets.newHashSet());
                temp.add(map.get(0));
                parentMap.put(map.get(1),temp);
            }
        }
        List<Integer> rightIds = parentMap.entrySet().stream().filter(i->i.getValue().size()==1).map(j->j.getKey()).collect(Collectors.toList());
        List<Integer> errorIds = parentMap.entrySet().stream().filter(i->i.getValue().size()>1).map(j->j.getKey()).collect(Collectors.toList());
        logger.info("errorIds={}",errorIds);
        for(Integer id:errorIds){
            logger.info("id={},parent={}",id,parentMap.get(id));
        }
        logger.info("rightIds={},errorids={}",rightIds.size(),errorIds.size());
        if(errorIds.size()!=0){
            return;
        }
        Map<Integer,Integer> rightMap = Maps.newHashMap();
        for(int id:rightIds){
            int parent = parentMap.get(id).stream().findFirst().get();
            rightMap.put(id,parent);
            logger.info("id={},parent={}",id,parent);
        }
        questionSqlDao.updateChildQuestion(rightMap);
    }
    @Test
    public void finishTest(){
        List<Map<Integer,Integer>> multiList = multiSqlDao.findCount();
        Map<Integer,Integer> map = Maps.newHashMap();
        multiList.forEach(i->map.putAll(i));
        List<Map<Integer,Integer>> objList = questionSqlDao.findCount();
        Map<Integer,Integer> map1 = Maps.newHashMap();
        objList.forEach(i->map1.putAll(i));
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int key = entry.getKey();
            if(map1.get(key)!=entry.getValue()){
                logger.info("复合题的数量不对：multi={},size={},size1={}",key,map1.get(key),entry.getValue());
            }
        }
    }
    @Test
    public void testAgain(){
        List<Map<Integer,List<Integer>>> multiList = multiSqlDao.findAll();
        Map<Integer,List<Integer>> map = Maps.newHashMap();
        multiList.forEach(i->map.putAll(i));
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            int multi = entry.getKey();
            List<Integer> children = entry.getValue();
            List<Integer> objs = questionSqlDao.findIdByMulti(multi);
            if(CollectionUtils.isEmpty(objs)){
                logger.info("无子题：multiId={}",multi);
            }
            if(objs.size()==children.size()){
                objs.removeIf(i->children.contains(i));
                if(CollectionUtils.isEmpty(objs)){
                    continue;
                }
            }
            logger.info("试题不一致。multi={},children={},objs={}",multi,children,objs);
        }
    }
}
