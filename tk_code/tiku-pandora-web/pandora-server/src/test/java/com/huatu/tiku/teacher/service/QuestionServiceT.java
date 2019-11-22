package com.huatu.tiku.teacher.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.dto.KnowledgeVO;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.material.QuestionMaterialService;
import com.huatu.ztk.commons.JsonUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/9/10.
 */
@Slf4j
public class QuestionServiceT extends TikuBaseTest{

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    KnowledgeService knowledgeService;

    @Autowired
    QuestionMaterialService questionMaterialService;

    @Autowired
    ImportService importService;
    @Test
    public void test(){
        Map<String,Object> attrMap = Maps.newHashMap();
        int subject  = 1;
        attrMap.put("status",2);
        attrMap.put("subject",subject);
        //选填
        attrMap.put("year",1);
        attrMap.put("mode",1);
        attrMap.put("type",1);
        //试题知识点查询
        Map<Integer, List<Integer>> questionMap = findByAttrs(attrMap,"ztk_question_new");
        Map<Integer,List<Integer>> knowledgeMap = Maps.newHashMap();
        for (Map.Entry<Integer, List<Integer>> entry : questionMap.entrySet()) {
            Integer questionId = entry.getKey();
            List<Integer> points = entry.getValue();
            for (Integer point : points) {
                List<Integer> ids = knowledgeMap.getOrDefault(point, Lists.newArrayList());
                ids.add(questionId);
                knowledgeMap.put(point,ids);
            }
        }
        Set<Long> set = knowledgeMap.keySet().stream().map(Long::new).collect(Collectors.toSet());
        List<Knowledge> knowledgeList = knowledgeService.findAll().stream().filter(i -> set.contains(i.getId())).collect(Collectors.toList());
        List<KnowledgeVO> knowledgeVOS = knowledgeService.assertKnowledgeTree(knowledgeList, 0L);
        assertKnowledge(knowledgeVOS,knowledgeMap);
        System.out.println("cone:");
        System.out.println(JsonUtil.toJson(knowledgeVOS));
    }

    private void assertKnowledge(List<KnowledgeVO> knowledgeList, Map<Integer, List<Integer>> knowledgeMap) {
        for (KnowledgeVO knowledgeVO : knowledgeList) {
            Long knowledgeId = knowledgeVO.getKnowledgeId();
            knowledgeVO.setCount(knowledgeMap.getOrDefault(knowledgeId.intValue(),Lists.newArrayList()).size());
            if(CollectionUtils.isNotEmpty(knowledgeVO.getKnowledgeTrees())){
                assertKnowledge(knowledgeVO.getKnowledgeTrees(),knowledgeMap);
            }
        }
    }


    /**
     * 根据各种条件查询试题的某些属性（缓存刷新专用）
     *
     * @param attrMap
     */
    public Map<Integer,List<Integer>> findByAttrs(Map<String, Object> attrMap,String collectionName) {
        DBObject queryObject = new BasicDBObject();
        queryObject.put("_class","com.huatu.ztk.question.bean.GenericQuestion");
        if(null != attrMap.get("status")){
            queryObject.put("status",attrMap.get("status"));
        }
        if(null != attrMap.get("subject")){
            queryObject.put("subject",attrMap.get("subject"));
        }
        if(null != attrMap.get("year")){
            queryObject.put("year",new BasicDBObject("$gt", 2008));
        }
        if(null != attrMap.get("mode")){
            queryObject.put("mode",1);
        }
        if(null != attrMap.get("type")){
            queryObject.put("type",new BasicDBObject("$in", Lists.newArrayList(99,100,109)));
        }
        DBObject fieldsObject = new BasicDBObject();
        fieldsObject.put("_id",1);
        fieldsObject.put("points",1);
        System.out.println("查询语句：" + queryObject.toString());
        DBCursor dbCursor =mongoTemplate.getCollection(collectionName).find(queryObject,fieldsObject);
        Map<Integer,List<Integer>> map = Maps.newHashMap();
        while (dbCursor.hasNext()){
            DBObject object=dbCursor.next();
            BasicDBList basicDBList = (BasicDBList)object.get("points");
            if(basicDBList == null){
                continue;
            }
            List<Integer> list = basicDBList.stream().map(String::valueOf).map(Integer::parseInt).collect(Collectors.toList());
            map.put(Integer.parseInt(String.valueOf(object.get("_id"))),list);
        }
        return map;

    }

    @Test
    public void testMaterial(){
        ArrayList<Integer> idInts = Lists.newArrayList(40025386, 40025387, 40025388, 40025389, 40025390, 40025391, 40025393, 40025394, 40025395, 40025396, 40025397, 40025399, 40025400, 40025402, 40025403, 40025404, 40025405, 40025406, 40025407, 40025408, 40025409, 40025410, 40025411, 40025412, 40025414, 40025415, 40025416, 40025418, 40025419, 40025420, 40025421, 40025424, 40025425, 40025426, 40025427, 40025428, 40025429, 40025430, 40025431, 40025432, 40025434, 40025436, 40025437, 40025438, 40025440, 40025441, 40025442, 40025443, 40025444, 40025445, 40025446, 40025447, 40025448, 40025449, 40025450, 40025451, 40025453, 40025454, 40025456, 40025457, 40025459, 40025460, 40025461, 40025462, 40025463, 40025464, 40025465, 40025466, 40025467, 40025469, 40025470, 40025471, 40025472, 40025473, 40025474, 40025476, 40025477, 40025478, 40025479, 40025480, 40025482, 40025483, 40025487, 40025489, 40025490, 40025491, 40025492, 40025493, 40025494, 40025496, 40025498, 40025499, 40025500, 40025501, 40025502, 40025504, 40025509, 40025510, 40025511, 40025512, 40025513, 40025516, 40025519, 40025521, 40025522, 40025524, 40025525, 40025526, 40025527, 40025528, 40025529, 40025531, 40025533, 40025536, 40025538, 40025539, 40025540, 40025541, 40025542, 40025543, 40025545, 40025546, 40025548, 40025549, 40025550, 40025552, 40025553, 40025554, 40025555, 40025556, 40025558, 40025559, 40025560, 40025561, 40025562, 40025563, 40025565, 40025566, 40025568, 40025570, 40025571, 40025572, 40025573, 40025575, 40025577, 40025578, 40025579, 40025581, 40025582, 40025584, 40025585, 40025587, 40025588, 40025589, 40025590, 40025591, 40025592, 40025593, 40025598, 40025600, 40025601, 40025602, 40025603, 40025604, 40025605, 40025609, 40025610, 40025611, 40025612, 40025613, 40025614, 40025615, 40025616, 40025618, 40025619, 40025621, 40025622, 40025623, 40025624, 40025625, 40025627, 40025628, 40025630, 40025631, 40025633, 40025634, 40025635, 40025636, 40025637, 40025638, 40025640, 40025641, 40025642, 40025643, 40025645, 40025646, 40025647, 40025648, 40025649, 40025650, 40025651, 40025652, 40025653, 40025654, 40025655, 40025656, 40025658, 40025659, 40025660, 40025661, 40025662, 40025663, 40025664, 40025665, 40025666, 40025669, 40025670, 40025671, 40025672, 40025673, 40025674, 40025675, 40025676, 40025677, 40025678, 40025679, 40025680, 40025681, 40025682, 40025683, 40025684, 40025685, 40025686, 40025687, 40025688, 40025690, 40025691, 40025692, 40025693, 40025694, 40025695, 40025696, 40025697, 40025698, 40025699, 40025700, 40025701, 40025702, 40025704, 40025705, 40025707, 40025709, 40025710, 40025711, 40025712, 40025713, 40025714, 40025715, 40025716, 40025717, 40025718, 40025719, 40025720, 40025721, 40025722, 40025723, 40025724, 40025726, 40025727, 40025728, 40025729, 40025730, 40025731, 40025732, 40025734, 40025735, 40025736, 40025737);
        importService.sendQuestion2Mongo(idInts);
//        List<Long> ids = idInts.stream().map(Long::new).collect(Collectors.toList());
//        for (Long id : ids) {
//            System.out.println("id = " + id);
//            Example example = new Example(QuestionMaterial.class);
//            example.and().andEqualTo("questionId",id);
//            List<QuestionMaterial> questionMaterials = questionMaterialService.selectByExample(example);
//            if(CollectionUtils.isNotEmpty(questionMaterials)&&questionMaterials.size()>1){
//                System.out.println("before = " + questionMaterials.stream().map(QuestionMaterial::getMaterialId).collect(Collectors.toList()));
//                questionMaterials.remove(0);
//                System.out.println("after = " + questionMaterials.stream().map(QuestionMaterial::getMaterialId).collect(Collectors.toList()));
//                questionMaterials.stream().forEach(i->{
//                    i.setStatus(-1);
//                    questionMaterialService.save(i);
//                });
//            }

//        }
    }
}
