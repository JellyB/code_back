package com.huatu.tiku.match.service.impl.v1.share;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.match.bo.paper.StandAnswerCardBo;
import com.huatu.tiku.match.dao.document.ShareDao;
import com.huatu.tiku.match.enums.ShareInfoEnum;
import com.huatu.tiku.match.service.v1.practice.PracticeService;
import com.huatu.tiku.match.service.v1.share.ShareCreateServer;
import com.huatu.tiku.match.util.BeanUtil;
import com.huatu.tiku.match.util.IdClientUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.pc.bean.Share;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

/**
 * Created by huangqingpeng on 2019/1/11.
 */
@Slf4j
@Service
public class ShareCreateServerImpl implements ShareCreateServer {
    public static final String MATCH_SHARE_TITLE = "【华图在线】模考大赛，见证你的进步！";
    public static final String MATCH_SHARE_LINE_TEST = "我在华图在线参加了一次行测模考大赛，共答对了%s道题，用时%s分%s秒。";

    @Value("${spring.profiles}")
    public  String env;
    @Autowired
    private PracticeService practiceService;

    @Autowired
    private ShareDao shareDao;

    @Override
    public Share buildShareInfo(int paperId ,int userId, String userName, String token,String cv,int terminal) throws BizException {
        String id = IdClientUtil.generaChareId();
        StandAnswerCardBo answerCard = practiceService.getUserAnswerCard(paperId, userId, userName, token, cv, terminal);
        if(CollectionUtils.isEmpty(answerCard.getPoints())){
            answerCard.setPoints(Lists.newArrayList());
        }
        Map resultMap = Maps.newHashMap();
        Map beanMap = BeanUtil.transBean2Map(answerCard);
        if(MapUtils.isNotEmpty(beanMap)){
            resultMap.putAll(beanMap);
        }
        final Duration duration = Duration.ofSeconds(answerCard.getExpendTime());
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        resultMap.put("matchMark", "mark");
        resultMap.put("imgUrl","http://tiku.huatu.com/cdn/share/img/icon_app.png");
        Share share = Share.builder()
                .id(id)
                .title(MATCH_SHARE_TITLE)
                .desc(String.format(MATCH_SHARE_LINE_TEST, resultMap.get("rcount"), minutes, seconds))
                .type(ShareInfoEnum.ShareTypeEnum.SHARE_PRACTICE.getKey())
                .outerId(id + "," + answerCard.getSubject() + "," + ShareInfoEnum.ShareReportTypeEnum.LINETESTONLY.getKey())
                .reportInfo(resultMap)
                .url("http://" + getRemoteUrl() + "/pc/v4/share/match/" + id)
                .build();
        shareDao.save(share);
        share.setReportInfo(Maps.newHashMap());
        return share;
    }


    public String getRemoteUrl() {
        log.info("env = {}",env);
        //说明是测试环境,设置测试环境地址
        if (!env.equalsIgnoreCase("product")) {
            return  "weixin.htexam.com";
        }
        return "ns.huatu.com";
    }

}
