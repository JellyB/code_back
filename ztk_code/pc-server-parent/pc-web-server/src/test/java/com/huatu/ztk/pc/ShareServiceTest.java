package com.huatu.ztk.pc;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.pc.bean.Share;
import com.huatu.ztk.pc.service.ShareService;
import com.huatu.ztk.pc.util.NumberFormatUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by shaojieyue
 * Created time 2016-09-19 17:43
 */
public class ShareServiceTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(ShareServiceTest.class);

    @Autowired
    private ShareService shareService;
    long uid = 12252065;

    @Test
    public void sharePracticeTest(){
        long id = 24328744;
        try {
            final Share share = shareService.sharePractice(id,uid,1);
            System.out.println(JsonUtil.toJson(share));
        } catch (BizException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shareReportTest(){
        final Share share = shareService.shareReport(232909810,1,-9);
        System.out.println(JsonUtil.toJson(share));
    }
    @Test
    public void shareCourseTest(){
       // long courseid = 54521;
        long courseid = 54643;
        Share share=shareService.shareCourse(courseid,uid,1);
        System.out.println(JsonUtil.toJson(share));
    }
    @Test
    public void shareArenaRecordTest(){
        long arenaid = 23449942;
        Share share= null;
       // 10264614,
        //        12252065
       long userid=10264614;
        try {
            share = shareService.shareArenaRecord(userid,arenaid);
        } catch (BizException e) {
            e.printStackTrace();
        }
        System.out.println(JsonUtil.toJson(share));
    }

    @Test
    public void shareArenaSummaryTest(){
       final Share share= shareService.shareArenaSummary(10264614);
        System.out.println(JsonUtil.toJson(share));
    }

    @Test
    public void shareArenaTodayRank(){
        final Share share= shareService.shareArenaTodayRank(uid);
        System.out.println(JsonUtil.toJson(share));
    }
}
