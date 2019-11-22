package com.huatu.one.biz.service;

import com.huatu.one.biz.mapper.UsageRecordMapper;
import com.huatu.one.biz.model.UsageRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 使用记录
 *
 * @author geek-s
 * @date 2019-08-30
 */
@Slf4j
@Service
public class UsageRecordService {

    @Autowired
    private UsageRecordMapper usageRecordMapper;

    /**
     * 保存使用记录
     *
     * @param openid 微信ID
     * @param menuId 功能ID
     */
    @Async
    public void saveRecord(String openid, Integer menuId) {
        UsageRecord usageRecord = new UsageRecord();
        usageRecord.setOpenid(openid);
        usageRecord.setMenuId(menuId);

        Date now = new Date();
        usageRecord.setGmtCreate(now);
        usageRecord.setGmtModified(now);

        usageRecordMapper.insertSelective(usageRecord);
    }

}
