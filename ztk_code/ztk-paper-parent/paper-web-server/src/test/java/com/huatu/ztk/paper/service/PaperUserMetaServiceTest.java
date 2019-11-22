package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.paper.bean.PaperUserMeta;
import com.huatu.ztk.paper.dao.PracticeRecordDao;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by ismyway on 16/5/19.
 */
public class PaperUserMetaServiceTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PaperUserMetaServiceTest.class);

    @Autowired
    private PaperUserMetaService paperUserMetaService;
    @Autowired
    private PracticeRecordDao practiceRecordDao;

    final int uid = 12252065;

    private int getParperId() {
        return RandomUtils.nextInt(860,866);
    }
    @Test
    public void addUndoPracticeTest(){
        for (int i = 0; i < 10; i++) {
            final int  paperId = getParperId();
            long practiceId = RandomUtils.nextLong(100000000,900000000);
            paperUserMetaService.addUndoPractice(uid,paperId,practiceId);
            PaperUserMeta paperUserMeta = paperUserMetaService.findBatch(uid, Lists.newArrayList(paperId)).get(0);
            Assert.assertEquals(paperUserMeta.getCurrentPracticeId(),practiceId);
            Assert.assertEquals(paperUserMeta.getUid(),uid);
            Assert.assertEquals(paperUserMeta.getPaperId(),paperId);
            Assert.assertEquals(paperUserMeta.getFinishCount(),paperUserMeta.getPracticeIds().size());
            Assert.assertEquals(paperUserMeta.getPracticeIds().get(0).longValue(),practiceId);
//            addFinishPracticeTest(paperId);
        }

    }

    @Test
    public void addFinishPracticeTest(){
        final int paperId = 1793;
        paperUserMetaService.addFinishPractice(uid, paperId, 100000000);
        final PaperUserMeta paperUserMeta = paperUserMetaService.findBatch(uid, Lists.newArrayList(paperId)).get(0);
        Assert.assertEquals(paperUserMeta.getCurrentPracticeId(),-1);
        Assert.assertEquals(paperUserMeta.getUid(),uid);
        Assert.assertEquals(paperUserMeta.getPaperId(), paperId);
        Assert.assertEquals(paperUserMeta.getFinishCount(),paperUserMeta.getPracticeIds().size());

    }
}
