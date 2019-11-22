import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.BaseTestW;
import com.huatu.ztk.backend.metas.bean.MatchUserBean;
import com.huatu.ztk.backend.metas.dao.UserMetaDao;
import com.huatu.ztk.backend.metas.service.PracticeMetaService;
import com.huatu.ztk.backend.paper.dao.AnswerCardDao;
import com.huatu.ztk.backend.paper.dao.MatchDao;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.user.dao.UserHuatuDao;
import com.huatu.ztk.backend.util.*;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.user.bean.UserDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.poi.ss.usermodel.DateUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\3\22 0022.
 */
public class RestTemplateTest extends BaseTestW {
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateTest.class);
    @Autowired
    UserMetaDao userMetaDao;
    @Autowired
    private PracticeMetaService practiceMetaService;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private MatchDao matchDao;
    @Autowired
    private AnswerCardDao answerCardDao;
    @Autowired
    UserHuatuDao userHuatuDao;

    @Test
    public void test123(){

    }
    @Test
    public void  postTest(){
        String url = RestTemplateUtil.URL_ROOT + "user/ztk/storm";
        List<Map<String,Object>> matchUserBeans =  userMetaDao.findLogsByIndex1(0,500);
        boolean flag = RestTemplateUtil.postLogs(matchUserBeans,url);
        logger.info("flag={}",flag);
    }

    private long postLogs(long index, int size) {
        List<MatchUserBean> matchUserBeans =  userMetaDao.findLogsByIndex(index,size);
        if(CollectionUtils.isEmpty(matchUserBeans)){
            return -1;
        }
        //发送失败则直接跳出
        if(!RestTemplateUtil.postLog(matchUserBeans)){
            return -1;
        }
        //发送成功，但是数量不足size个，也需要跳出
        if(matchUserBeans.size()<size){
            return -1;
        }
        return matchUserBeans.get(matchUserBeans.size()-1).getId();
    }
    @Test
    public void test1(){
        List<Match> matches =  matchDao.findAll();
        long start = System.currentTimeMillis();
        for(Match match:matches){

            try {
                if(match.getEndTime()< System.currentTimeMillis()){
                    practiceMetaService.saveMatchUserTODB(match.getPaperId());
                }
            } catch (BizException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        logger.info("总耗时：{}",(end-start)/1000);

    }
    @Test
    public void test3(){
        practiceMetaService.postByIndex();

    }

    @Test
    public void test4(){
        List<Long> practiceIds = Lists.newArrayList();
        practiceIds.add(1909073049219497984L);
        List<AnswerCard> cards = answerCardDao.findByIdsV2(practiceIds);
        logger.info("cards={}",cards);
    }


}
