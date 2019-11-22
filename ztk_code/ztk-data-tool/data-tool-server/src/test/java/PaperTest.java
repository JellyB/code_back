import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.ztk.backend.BaseTestW;
import com.huatu.ztk.backend.mysql.dao.PaperQuestionSqlDao;
import com.huatu.ztk.backend.mysql.dao.PaperSqlDao;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\4\5 0005.
 */
public class PaperTest extends BaseTestW {
    private static  final Logger logger = LoggerFactory.getLogger(PaperTest.class);
    @Autowired
    private PaperQuestionSqlDao paperQuestionSqlDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PaperSqlDao paperSqlDao;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private QuestionDubboService questionDubboService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Test
    public void test(){
        List<Integer> paperIds = paperSqlDao.findAllPaper();
        int total = 0;
        for(int id:paperIds){
//            if(id!=827){
//                continue;
//            }
            List<Map<String,Object>> binds = paperQuestionSqlDao.findBindings(id);
            List<Integer> bigIds = Lists.newArrayList();
            Map<Integer,Integer> orderMap = Maps.newHashMap();
            Map<Integer,Integer> orderTimeMap = Maps.newHashMap();
            for (Map<String, Object> bind : binds) {
                int questionId = Integer.parseInt(String.valueOf(bind.get("question_id")));
                int order = Integer.parseInt(String.valueOf(bind.get("order")));
                int bb103 = Integer.parseInt(String.valueOf(bind.get("bb103")));
                int bb106 = Integer.parseInt(String.valueOf(bind.get("bb106")));
                bigIds.add(questionId);
                if(orderMap.get(order)==null){
                    orderMap.put(order,questionId);
                    orderTimeMap.put(order,Integer.max(bb103,bb106));
                }else if(Integer.max(bb103,bb106)>orderTimeMap.get(order)){
                    orderMap.put(order,questionId);
                    orderTimeMap.put(order,Integer.max(bb103,bb106));
                }
            }
            logger.info("paperId={},size={},having={}",id,orderMap.size(),orderMap.values());
            bigIds.removeIf(i->orderMap.values().contains(i));
            paperQuestionSqlDao.updateStatus(bigIds,id,-1);
            total += bigIds.size();
        }
        logger.info("total={}",total);

    }
    @Test
    public void test1(){
        String sql = "select pastpaper_id,display_order,count(1) as size from v_pastpaper_question_r where bb102 =1 group by pastpaper_id,display_order having count(1)>1";
        List<Map<String,Integer>> list = jdbcTemplate.query(sql,(rs,i)->{
            Map<String,Integer> map = Maps.newHashMap();
            map.put("paperId",rs.getInt("pastpaper_id"));
            map.put("order",rs.getInt("display_order"));
            map.put("size",rs.getInt("size"));
            return map;
        });
        List<Integer> ids = Lists.newArrayList();
        for (Map<String, Integer> map : list) {
            String subSql = "select pukey,question_id,bb103,bb106 from v_pastpaper_question_r where pastpaper_id = ? and display_order=? and bb102 = 1 and question_type = 'o'";
            Object[] param = {map.get("paperId"),map.get("order")};
            List<Map<String,Integer>> queryList = jdbcTemplate.query(subSql,param,(rs,i)->{
                Map<String,Integer> resut = Maps.newHashMap();
                resut.put("id",rs.getInt("pukey"));
                resut.put("questionId",rs.getInt("question_id"));
                resut.put("time",Integer.max(rs.getInt("bb103"),rs.getInt("bb106")));
                return resut;
            });
            Set<Integer> set =  queryList.stream().map(i->i.get("questionId")).collect(Collectors.toSet());
            if(set.size()>1){
                logger.info("error,paper={},order={},questionIds = {}",map.get("paperId"),map.get("order"),set);
            }
            int id = queryList.stream().max(Comparator.comparing(i->i.get("time"))).get().get("id");
            ids.addAll(queryList.stream().map(i->i.get("id")).filter(i->i!=id).collect(Collectors.toList()));

        }
        logger.info("size = {},ids = {}",ids.size(),ids);
        String updateSql = "update  v_pastpaper_question_r set bb102 = -1 ,eb104 = '20180405' where pukey in (:ids)";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedJdbcTemplate.update(updateSql,parameters);
    }
    @Test
    public void paperTest(){
        int id = 3526742;
        Paper paper = paperDao.findById(id);
        List<Integer> questionIds = paper.getQuestions();
        logger.info("questionIds.size={}",questionIds.size());
        List<Integer> ids = questionDao.findAllTypeByIds(questionIds).stream().filter(i->i.getStatus()!=2).map(j->j.getId()).collect(Collectors.toList());
        logger.info("ids = {}",ids);
        for (Integer questionId : questionIds) {
            try {
                Question question = questionDao.findAllTypeById(questionId);
                question.setFrom("电工类专业本科生/专科生");
                questionDubboService.update(question);
            } catch (IllegalQuestionException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * @Huangqp 20180508
     * 查询status==0的试题所属的试卷，并对试卷进行分析，做处理方案
     */
    @Test
    public void paperQuestionTest(){
        Query query = new Query(Criteria.where("status").is(0));
        List<Question> questions = mongoTemplate.find(query, Question.class, "ztk_question_new");
        if(CollectionUtils.isEmpty(questions)){
            return;
        }
        List<Integer> qids = questions.stream().map(i->i.getId()).collect(Collectors.toList());
        List<QuestionExtend> extendList = questionDao.findExtendByIds(qids);
        if(CollectionUtils.isEmpty(extendList)){
            return;
        }
        Map<Integer,Float> seqMap = Maps.newHashMap();
        Set<Integer> paperIds = Sets.newHashSet();
        extendList.forEach(i-> {
            paperIds.add(i.getPaperId());
            seqMap.put(i.getQid(),i.getSequence());
        });
        logger.info("paperIds={}",paperIds);
        List<Paper> papers = paperDao.findByIds(paperIds.stream().collect(Collectors.toList()));
        papers.sort(Comparator.comparingInt(Paper::getCatgory));
        Map<Integer,String> paperMap = Maps.newHashMap();
        Map<Integer,List<Integer>> questionMap  = Maps.newHashMap();
        for (Paper paper : papers) {
            if(paper.getStatus()==4){
                continue;
            }
            List<Integer> questionIds  = paper.getQuestions();
            questionIds.removeIf(i->!qids.contains(i));
            if(CollectionUtils.isEmpty(questionIds)){
                continue;
            }
            questionMap.put(paper.getId(),questionIds);
            paperMap.put(paper.getId(),paper.getName());
            System.out.println("需要处理的试卷： "+ paper.getId() + "--" + paper.getName() );
            System.out.println("需要处理的试题： "+ JsonUtil.toJson(questionIds));

        }
        logger.info("paperMap={}",paperMap);
        logger.info("questionMap={}",questionMap);

    }

    @Test
    public void unionPaperTest(){
        Integer paperId = 3526950;
        Paper paper = paperDao.findById(paperId);
        List<Integer> paperIds = Lists.newArrayList(3526948,3526949);
        List<Paper> paperList = paperDao.findByIds(paperIds);

        List<Module> moduleList = Lists.newArrayList();
        List<Integer> questions = Lists.newArrayList();
        List<Integer> bigQuestions= Lists.newArrayList();

        for (Paper tempPaper : paperList) {
            int qcount = tempPaper.getQcount();
            if(tempPaper.getCatgory()==2){
                Module module = Module.builder().category(tempPaper.getCatgory()).name("公基部分").qcount(qcount).build();
                moduleList.add(module);
            }else
            {
                Module module = Module.builder().category(tempPaper.getCatgory()).name("职测部分").qcount(qcount).build();
                moduleList.add(module);
            }
            questions.addAll(tempPaper.getQuestions());
            if(CollectionUtils.isNotEmpty(bigQuestions)){
                bigQuestions.addAll(tempPaper.getBigQuestions());
            }
        }

        paper.setModules(moduleList);
        paper.setQuestions(questions);
        paper.setQcount(questions.size());
        paperDao.update(paper);
        testClearPaperCache();
    }

    @Test
    public void testClearPaperCache(){
        List<Integer> paperIds = Lists.newArrayList(3526950);
        for(int paperId:paperIds){
            ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
            String key = "paper-web-server."+ PaperRedisKeys.getPaperKey(paperId);
            String result = valueOperations.get(key);
            logger.info("result={}",result);
            redisTemplate.delete(key);
        }

    }
}
