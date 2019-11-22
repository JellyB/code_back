package com.huatu.tiku.match.initTest;

import com.google.common.collect.Maps;
import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.match.bo.paper.StandAnswerCardBo;
import com.huatu.tiku.match.enums.ShareInfoEnum;
import com.huatu.tiku.match.service.v1.practice.PracticeService;
import com.huatu.tiku.match.util.BeanUtil;
import com.huatu.tiku.match.util.IdClientUtil;
import com.huatu.ztk.pc.bean.Share;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Map;

import static com.huatu.tiku.match.service.impl.v1.share.ShareCreateServerImpl.MATCH_SHARE_LINE_TEST;

/**
 * MongoDB 连接测试
 * Created by lijun on 2018/10/11
 */
public class MongoDBBaseTest extends BaseWebTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    PracticeService practiceService;

    @Test
    public void test() {
        StandAnswerCardBo answerCard = null;
//        try {
////            answerCard = practiceService.getUserAnswerCard(3527008, 233906356);
//        } catch (BizException e) {
//            e.printStackTrace();
//        }
        Map resultMap = Maps.newHashMap();
        if (null !=answerCard) {
            resultMap.putAll(BeanUtil.transBean2Map(answerCard));
//            resultMap.putAll(beanMap);
        }
        String s = IdClientUtil.generaChareId();
        Share share = Share.builder()
                .id(s)
                .title("ceshi")
                .desc(String.format(MATCH_SHARE_LINE_TEST, 1, 2, 3))
                .type(ShareInfoEnum.ShareTypeEnum.SHARE_PRACTICE.getKey())
                .outerId(s + "," + 1 + "," + ShareInfoEnum.ShareReportTypeEnum.LINETESTONLY.getKey())
                .reportInfo(resultMap)
                .url("http://" + "ns.huatu.com" + "/pc/v4/share/match/" + s)
                .build();
        mongoTemplate.save(share);
    }


}
