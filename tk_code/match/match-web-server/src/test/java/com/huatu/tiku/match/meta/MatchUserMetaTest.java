package com.huatu.tiku.match.meta;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.dao.document.MatchUserMetaDao;
import com.huatu.tiku.match.dao.manual.meta.MatchUserMetaMapper;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import org.apache.commons.collections4.MapUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StopWatch;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/4/1.
 */
public class MatchUserMetaTest extends BaseWebTest {

    @Autowired
    MatchUserMetaService matchUserMetaService;

    @Autowired
    MatchUserMetaDao matchUserMetaDao;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MatchUserMetaMapper matchUserMetaMapper;

    @Test
    public void single() {
        StopWatch stopWatch = new StopWatch("matchUserMeta");
        stopWatch.start("getIds");
        List<String> ids = getIds();
        stopWatch.stop();
        stopWatch.start("meta");
//        com.huatu.ztk.paper.bean.MatchUserMeta meta = matchUserMetaDao.findById(ids.get(0));
        ids.parallelStream().forEach(matchUserMetaDao::findById);
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }

    public List<String> getIds() {
        Example example = new Example(MatchUserMeta.class);
        example.and().andEqualTo("matchId", 4001456);
        List<MatchUserMeta> matchUserMetas = matchUserMetaService.selectByExample(example);
        List<String> results = matchUserMetas.stream()
                .map(i -> matchUserMetaDao.getMatchUserMetaId(i.getUserId(), i.getMatchId()))
                .collect(Collectors.toList());
        return results;
    }

    @Test
    public void test() {
        String ids = "2005318,2005323,2005327,2005341,2005342,2005345,2005346,2005351,2005352,2005356,3526529,3526531,3526533,3526534,3526558,3526618,3526633,3526634,3526635,3526689,3526701,3526734,3526744,3526831,3526876,3526881,3526885,3526890,3526892,3526894,3526895,3526897,3526898,3526899,3526901,3526909,3526910,3526911,3526921,3526922,3526924,3526925,3526930,3526934,3526936,3526937,4000885,4000908,4000909,4000915,4000916,4000918,4000919,4000920,4000921,4000922,4000923,4000924,4000925,4000926,4000927,4000929,4000930,4000932,4000933,4000934,4000935,4000936,4000937,4000938,4000939,4000940,4000941,4000946,4000947,4000948,4000949,4000950,4000951,4000952,4000953,4000954,4000955,4000956,4000959,4000964,4000966,4000976,4000981,4000982,4000987,4000988,4000989,4000990,4000992,4000994,4000995,4000996,4001010,4001012,4001015,4001017,4001019,4001020,4001022,4001025,4001026,4001030,4001031,4001042,4001048,4001049,4001051,4001065,4001066,4001067,4001068,4001069,4001071,4001072,4001074,4001077,4001081,4001083,4001084,4001092,4001093,4001099,4001101,4001102,4001104,4001105,4001107,4001108,4001109,4001110,4001111,4001112,4001114,4001115,4001116,4001117,4001118,4001119,4001120,4001121,4001122,4001126,4001127,4001128,4001132,4001134,4001135,4001138,4001139,4001140,4001141,4001142,4001143,4001144,4001145,4001148,4001149,4001150,4001151,4001152,4001153,4001156,4001157,4001158,4001161,4001162,4001163,4001164,4001170,4001172,4001173,4001175,4001179,4001180,4001181,4001182,4001183,4001184,4001185,4001188,4001189,4001190,4001193,4001194,4001197,4001199,4001205,4001207,4001214,4001280,4001281,4001282,4001289,4001291,4001292,4001296,4001300,4001301,4001302,4001303,4001304,4001305,4001306,4001401,4001403,4001407,4001416,4001422,4001430,4001433,4001456,4001481,4001482,4001484,4001492,4001496,4001497,4001499,4001515,4001516,4001520,4001521,4001522,4001523,4001527,4001528,4001529,4001530,4001531,4001636,4001637,4001674,4001690,4001691,4001692,4001693,4001694,4001695,4001696,4001735,4001749,4001750,4001751,4001752,4001758";
        List<String> collect = Arrays.stream(ids.split(",")).map(Integer::parseInt).map(PaperRedisKeys::getPaperPracticeIdSore).collect(Collectors.toList());
        for (String s : collect) {
            redisTemplate.expire(s, 1, TimeUnit.DAYS);
        }
    }

    @Test
    public void clearCache() {
        List<HashMap> list = matchUserMetaMapper.findMatchIds();
        for (HashMap hashMap : list) {
            Integer paperId = MapUtils.getInteger(hashMap, "matchId");
            if(null == paperId){
                continue;
            }
            String userSubmitCount = MatchInfoRedisKeys.getUserSubmitCount(paperId.intValue());
            redisTemplate.delete(userSubmitCount);
        }
    }

}
