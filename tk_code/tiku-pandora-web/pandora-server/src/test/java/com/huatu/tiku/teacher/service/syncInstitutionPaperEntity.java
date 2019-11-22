package com.huatu.tiku.teacher.service;

import com.huatu.common.bean.BaseEntity;
import com.huatu.tiku.teacher.service.impl.SyncPaperServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/2/27
 * @描述 将职测试题卷同步为对应的活动卷（活动类型为真题卷）
 */
@Slf4j
public class syncInstitutionPaperEntity extends BaseEntity {
    @Autowired
    SyncPaperService syncPaperService;
    @Autowired
    PaperActivityService paperActivityService;

    @Test
    public void syncPaperEntityToPaperActivity() {
       // syncPaperService.syncPaperEntityToPaperActivity();
        paperActivityService.selectByPrimaryKey(1L);
    }

}
