package com.huatu.tiku.dataClean;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.paper.PaperActivityListService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.ztk.paper.common.PaperType;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * 同步试卷 信息至 mongoDB
 * Created by lijun on 2019/1/31
 */
public class SynchronizePaperInfo extends TikuBaseTest {

    @Autowired
    private PaperEntityService entityService;

    @Autowired
    private PaperActivityListService activityService;

    @Autowired
    private ImportService importService;

    private final static int SUBJECT_ID = 1;

    @Test
    public void test() {
        final WeekendSqls<PaperEntity> paperEntityWeekendSql = WeekendSqls.<PaperEntity>custom()
                .andEqualTo(PaperEntity::getMode, PaperType.TRUE_PAPER)
                .andEqualTo(PaperEntity::getSubjectId, SUBJECT_ID);
        Example paperEntityExample = Example.builder(PaperEntity.class)
                .where(paperEntityWeekendSql)
                .build();
        List<PaperEntity> paperEntityList = entityService.selectByExample(paperEntityExample);
        if (CollectionUtils.isNotEmpty(paperEntityList)) {
            final List<Long> paperEntityIdList = paperEntityList.stream()
                    .map(PaperEntity::getId)
                    .collect(toList());

            WeekendSqls<PaperActivity> paperActivityWeekendSql = WeekendSqls.<PaperActivity>custom()
                    .andIn(PaperActivity::getPaperId, paperEntityIdList);
            Example paperActivityExample = Example.builder(PaperActivity.class)
                    .where(paperActivityWeekendSql)
                    .build();
            List<PaperActivity> paperActivityList = activityService.selectByExample(paperActivityExample);
            final List<Long> activityIdList = paperActivityList.stream()
                    .map(PaperActivity::getId)
                    .collect(toList());
            System.out.println("》》》》》》》》》》》》" + activityIdList.size());

            importService.importPaper(activityIdList);



            for (; ; ) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
