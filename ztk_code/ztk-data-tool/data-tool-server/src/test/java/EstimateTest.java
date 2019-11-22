import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huatu.ztk.backend.BaseTestW;
import com.huatu.ztk.backend.metas.controller.PracticeMetaController;
import com.huatu.ztk.backend.metas.service.PracticeMetaService;
import com.huatu.ztk.backend.paper.dao.MatchDao;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\4\8 0008.
 */
public class EstimateTest extends BaseTestW {
    private static final Logger logger = LoggerFactory.getLogger(EstimateTest.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MatchDao matchDao;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    PracticeMetaService practiceMetaService;
    @Autowired
    PracticeMetaController practiceMetaController;
    @Test
    public void testClearPaperCache(){
        String sql = "select DISTINCT PUKEY from v_pastpaper_info where  bb102 = 1 ";
        List<Integer> paperIds = jdbcTemplate.queryForList(sql,Integer.class);
        for(int paperId:paperIds){
            ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
            String key = "paper-web-server."+ PaperRedisKeys.getPaperKey(paperId);
            String result = valueOperations.get(key);
            logger.info("result={}",result);
            redisTemplate.delete(key);
        }

    }
    /**
     * 查询某一试卷的参考userid
     */
    @Test
    public void test(){
        int paperId = 2005408;
        String key = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set<String> set = zSetOperations.range(key,0,Long.MAX_VALUE);
        if(CollectionUtils.isEmpty(set)){
            logger.info("没有人参考");
        }
        logger.info("参考答题卡数量有{}",set.size());
        List<Long> practiceIds = set.stream().map(Long::new).collect(Collectors.toList());
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("_id", new BasicDBObject("$in", practiceIds));
        DBObject fields = new BasicDBObject(); //only get the needed fields. 设置需要获取哪些域
        fields.put("_id", 1);
        fields.put("userId", 1);
        DBCursor dbCursor =mongoTemplate.getCollection("ztk_answer_card").find(query1,fields);
        Set<Long> userIds= Sets.newHashSet();
        while (dbCursor.hasNext()){
            DBObject object=dbCursor.next();
            userIds.add(Long.parseLong(object.get("userId").toString()));
        }
        logger.info("查询到有答题卡的用户有{}个，有{}",userIds.size(),userIds);
    }
    @Test
    public void copyPaperToMatch(){
        int matchId = 3526894;
        int paperId = 2005424;
        Match match = matchDao.findById(matchId);
        if(match==null){
            logger.error("暂无模考大赛信息");
        }
        Paper paper = paperDao.findById(paperId);
        if(paper==null){
            logger.info("试卷copy对象不存在");
        }
        paper.setId(matchId);
        paper.setType(9);
        paper.setStatus(match.getStatus());
        paper.setTime(7200);
        paper.setScore(100);
        paper.setArea(-9);
        paper.setName(match.getName());
        paper.setYear(2019);
        if(paper instanceof EstimatePaper){
            ((EstimatePaper) paper).setStartTime(match.getStartTime());
            ((EstimatePaper) paper).setEndTime(match.getEndTime());
        }
        paperDao.update(paper);


    }
    @Test
    public void test11(){
        practiceMetaService.getMatchUserListByMatchId(3526618);
    }

    @Test
    public void test12() throws BizException {
        practiceMetaController.getMatchEnrollInfoAll("2017-05-01","2018-07-16",1,"广东，山东，江苏");
    }


    @Test
    public void test1(){
        List<Match> all = matchDao.findAll();
        List<Integer> paperIds = all.stream().filter(i -> i.getSubject() == 1).filter(i -> i.getStartTime() >= 1527987600000L)
                .filter(i -> i.getStartTime() <= 1535245200000L).map(i -> i.getPaperId()).collect(Collectors.toList());
        logger.info("paperId={}",paperIds);
        for (Integer id : paperIds) {
//            matchDao.groupByNameFindOne(id);
        }
    }

    @Test
    public void test123(){
        List<Integer> ids = Lists.newArrayList(3526725,3526703,3526812,3526639,3004126,3004088,3526745,3526613,3004013,3004206,3004116,3525917,3525868,3004125,3525842,3525859,3004054,3004225,3526829,3526748,3526873,3526872,3526664,3526013,3004007,3526839,3526750,3526838,3526751,3526815,3526814,3004045,3004033,3526757,3526753,3526874,3526871,3526782,3526660,3526654,3526649,3526647,3526820,3525741,3004018,3526739,3525857,3526796,3526785,3526823,3526818,3526875,3526810,3526788,3526783,3526759,3526657,3526656,3526648,3526608,3004129,3526805,3526804,3526786,3526771,3526767,3526765,3526762,3526763,3526819,3526816,3525863,3526772,3526768,3526766,3525893,3526780,3526773,3526770,3526769,3526836,3526791,3526827,3526822,3526870,3526826,3526828,3526825,3526824,3526725,3526703,3526643,3526879,3526877,3526812,3526641,3525947,3526869,3526868,3526747,3526745,3526613,3526746,3526015,3526009,3526008,3526005,3525983,3525981,3525938,3525812,3526764,3526761,3525870,3525842,3525840,3526728,3526707,3526698,3526696,3526695,3525991,3525896,3525855,3525848,3525811,3525808,3525933,3526752,3525898,3526829,3525934,3525828,3004023,3526873,3526872,3526732,3526724,3526719,3526664,3526663,3526013,3525846,3525844,3525814,3525810,3526839,3526751,3526815,3525932,3526775,3526757,3526753,3526874,3526871,3526660,3526654,3526649,3526647,3526820,3525741,3526739,3525857,3525850,3525975,3525945,3525913,3525818,3526796,3526785,3526823,3525753,3526875,3526810,3526788,3526783,3526759,3526657,3526656,3526648,3526608,3525965,3525941,3526771,3526767,3526762,3526763,3525973,3525971,3525882,3526819,3526816,3525955,3525889,3525863,3526772,3526768,3526766,3526780,3526773,3526770,3526769,3526837,3526836,3525886,3525874,3525799,3526791,3526827,3526822,3525801,3526860,3526870,3525809,3004040,3526834,3526826,3526835,3525936,3526828,3526825,3526824);
        logger.info("ids.size={}",ids.size());
        logger.info("distinct.id.size={}",ids.stream().distinct().count());
        List<Paper> papers = paperDao.findByIds(ids);
        logger.info("papers.size={}",papers.size());
        ids.removeAll(papers.stream().map(Paper::getId).collect(Collectors.toList()));
        logger.info("papers.not_exsited.ids={}",ids);
    }
}

