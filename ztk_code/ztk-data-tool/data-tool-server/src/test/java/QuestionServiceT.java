import com.google.common.collect.Lists;
import com.huatu.ztk.backend.BaseTestW;
import com.huatu.ztk.backend.question.dao.PointDao;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.backend.question.service.QuestionService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\5\31 0031.
 */
@Slf4j
public class QuestionServiceT extends BaseTestW {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    PointDao pointDao;
    private static final Logger logger = LoggerFactory.getLogger(QuestionServiceT.class);
    /**
     * 刷新所有题干中或选项中带<br></p>标签的试题
     */
    @Test
    public void test(){
        int startId = 0;
        while(true){
            Query query = new Query(Criteria.where("_id").gt(startId).and("type").ne(107).and("_class").is("com.huatu.ztk.question.bean.CompositeSubjectiveQuestion"));
            query.with(new Sort(Sort.Direction.ASC,"_id")).limit(200);
            List<Question> questions = mongoTemplate.find(query, Question.class, "ztk_question_new");
            if(CollectionUtils.isEmpty(questions)){
                logger.info("任务结束");
                return;
            }else{
                startId = questions.stream().map(i->i.getId()).max(Integer::compare).get();
            }
            logger.info("question={}",questions==null?"null":questions.stream().map(i->i.getId()).collect(Collectors.toList()));
            String[] strings = new String[questions.size()];
            for (int k=0;k< questions.size();k++) {
                Question question = questions.get(k);
                strings[k] = question.getId()+"";
            }

//            List<Integer> subjectIds = Lists.newArrayList(1,2,3,24);
//            long start = System.currentTimeMillis();
//            List<QuestionPointTreeMin> allPonits = pointDao.findAllPonits().stream().filter(i->subjectIds.contains(i.getSubject())).collect(Collectors.toList());
//            List<QuestionPointTreeMin> oneLevels = allPonits.stream().filter(i->i.getLevel()==0).collect(Collectors.toList());
//            final SetOperations setOperations = redisTemplate.opsForSet();
//            final String pointSummaryKey = RedisKnowledgeKeys.getPointSummaryKey();
//            HashMap<String,String> hashMap = Maps.newHashMap();
//            int l =0;
//            for (QuestionPointTreeMin oneLevel : oneLevels) {
//                int oneLevelId = oneLevel.getId();
//                long twoTotal = 0;
//                List<QuestionPointTreeMin> twoLevels = allPonits.stream().filter(i->i.getParent()==oneLevelId).collect(Collectors.toList());
//                for (QuestionPointTreeMin twoLevel : twoLevels) {
//                    int twoLevelId = twoLevel.getId();
//                    List<QuestionPointTreeMin> threeLevels = allPonits.stream().filter(i->i.getParent()==twoLevelId).collect(Collectors.toList());
//                    long threeTotal = 0;
//                    for (QuestionPointTreeMin threeLevel : threeLevels) {
//                        String pointQuesionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(threeLevel.getId());
////"3080194"," 3080514"," 3084231"," 3090208"," 3090426"," 3090433"," 21915712"," 21917769"," 21919117"," 21919262"," 21921059"," 21921415"," 21921488"," 21921818"," 21922397"," 21924081"," 21925611"," 21948267"," 21948268"," 21948269"," 21948270"," 21948271"," 21948272"," 21948273"," 21948274"," 21948275"," 21948276"," 21948277"," 21948278"," 21948279"," 21948280"," 21948281"," 21948282"," 21948283"," 21948284"," 21948285"," 21948286"," 21948287"," 21948288"," 21948289"," 21948290"," 21948291"," 21948292"," 21948293"," 21948294"," 21948295"," 21948296"," 21948297"," 21948298"," 21948299"," 21948300"," 21948301"," 21948302"," 21948303"," 21948304"," 21948305"," 21948307"," 21948308"," 21948310"," 21948312"," 21948315"," 21948316"," 21948318"," 21948320"," 21948323"," 21948329"," 21948333"," 21948335"," 21948336"," 21948338"," 21948341"," 21948343"," 21948344"," 21948346"," 21948348"," 21949770"," 21952179"," 21952695"," 21952722"," 21953043"," 21953160"," 21953170"," 21955331"," 21955334"," 21955336"," 21955338"," 21955340"," 21955342"," 21955344"," 21955348"," 21955354"," 21955358"," 21955360"," 21955363"," 21955365"," 21955366"," 21955368"," 21955371"," 21955373"," 21955374"," 21955375"," 21955379"," 21955384"," 21955386"," 21955388"," 21955389"," 21955391"," 21955395"," 21955399"," 21955403"," 21955406"," 21955461"," 21955468"," 21955469"," 21955480"," 21955483"," 21955489"," 21955491"," 21955492"," 21955495"," 21955497"," 21955503"," 21955505"," 21955506"," 21955508"," 21955513"," 21955515"," 21955524"," 21955526"," 21955528"," 21955532"," 21955538"," 21955540"," 21955541"," 21955542"," 21955543"," 21955545"," 21955548"," 21955550"," 21955551"," 21955553"," 21955554"," 21955561"," 21955562"," 21955563"," 21955564"," 21955567"," 21955568"," 21955569"," 21955570"," 21955571"," 21955572"," 21955573"," 21955575"," 21955577"," 21955579"," 21955581"," 21955583"," 21955584"," 21955585"," 21955586"," 21955602"," 21955603"," 21955604"," 21955605"," 21955606"," 21955619"," 21955620"," 21955621"," 21955622"," 21955623"," 21955664"," 21955665"," 21955666"," 21955667"," 21955668"," 21955670"," 21955671"," 21955672"," 21955673"," 21955674"," 21955682"," 21955683"," 21955684"," 21955685"," 21955686"," 21955728"," 21955729"," 21955732"," 21955733"," 21955734"," 21955737"," 21955738"," 21955739"," 21955740"," 21955741"," 21955743"," 21955746"," 21955748"," 21955749"," 21955752"," 21955762"," 21955763"," 21955764"," 21955765"," 21955766"," 21955768"," 21955769"," 21955770"," 21955771"," 21955772"," 21955782"," 21955783"," 21955784"," 21955785"," 21955786"," 21956026"," 21956093"," 21956094"," 21956095"," 21956096"," 21956097"," 21956098"," 21956099"," 21956100"," 21956101"," 21956102"," 21956103"," 21956104"," 21956105"," 21956106"," 21956107"," 21956108"," 21956109"," 21956110"," 21956111"," 21956112"," 21956113"," 21956114"," 21956115"," 21956116"," 21956117"," 21956118"," 21956119"
//                        setOperations.remove(pointQuesionIdsKey, strings);
//                        Long size = setOperations.size(pointQuesionIdsKey);
//                        threeTotal += size;
//                        if(size==null||size>0){
//                            hashMap.put(threeLevel.getId()+"",size+"");
//                            logger.info("累加知识点=={}",l++);
//                        }
//                    }
//                    hashMap.put(twoLevel.getId()+"",threeTotal+"");
//                    logger.info("累加知识点=={}",l++);
//                    twoTotal += threeTotal;
//                }
//                hashMap.put(oneLevel.getId()+"",twoTotal+"");
//                logger.info("累加知识点=={}",l++);
//            }
//            long end = System.currentTimeMillis();
//            logger.info("最终的数据={},耗时={}",hashMap,(end-start)/1000);
//            redisTemplate.opsForHash().putAll(pointSummaryKey,hashMap);
        }

    }
    @Test
    public void test1(){
        int subjectId = 2;
        long start = System.currentTimeMillis();
        Long total = questionDao.countBySubject(subjectId);
        int cursor = 0;
        int size = 100;
        int count = 0;
        List<Integer> questionIds = Lists.newArrayList();
        while(true){
            List<Question> questionList = questionDao.findQuestionsForPage(cursor,size,subjectId);
            if(CollectionUtils.isEmpty(questionList)){
                logger.info("已无试题需要处理，进程结束");
                break;
            }
            cursor = questionList.get(questionList.size()-1).getId();
            for (Question question : questionList) {
                if(question instanceof GenericQuestion){
                    if(((GenericQuestion) question).getChoices().size()==2&&question.getType()!= QuestionType.WRONG_RIGHT){
                        questionIds.add(question.getId());
                    }else if(question.getType()== QuestionType.SINGLE_CHOICE&&((GenericQuestion) question).getAnswer()>10){
                        questionIds.add(question.getId());
                    }else if(question.getType()== QuestionType.MULTIPLE_CHOICE&&((GenericQuestion) question).getAnswer()<10){
                        questionIds.add(question.getId());
                    }
                }
            }
            count += questionList.size();
            logger.info("刷新进程：+++++{}/{}",count,total);
        }
        long end = System.currentTimeMillis();
        logger.info("刷新需要时间：{}",(end-start)/1000);
        logger.info("questionIds={}",questionIds);
    }

}

