package com.huatu.ztk.pc.service;

import com.huatu.ztk.pc.bean.ShenlunPaper;
import com.huatu.ztk.pc.bean.ShenlunSummary;
import com.huatu.ztk.pc.dao.ShenlunPaperDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.Collator;
import java.util.List;


/**
 * Created by ht on 2016/9/23.
 */
@Service
public class ShenlunService {

    private static final Logger logger = LoggerFactory.getLogger(ShareService.class);

    @Autowired
    private ShenlunPaperDao shenlunPaperDao;

    /**
     * 获取论首页展示数据
     *
     * @return
     */
    public List<ShenlunSummary> querySummary() {
        //查询首页展示所需数据
        List<ShenlunSummary> summaryList = shenlunPaperDao.querySummary();
        //排序 按首字母排序（国家排最前）
        summaryList.sort((ShenlunSummary m1, ShenlunSummary m2) -> Collator.getInstance(java.util.Locale.CHINA).compare(m1.getAreaName(), m2.getAreaName()));
        return summaryList;
    }

    /**
     * 按地区获取申论列表
     *
     * @param areaId
     * @return
     */
    public List<ShenlunPaper> findByAreaId(int areaId) {
        return shenlunPaperDao.findByAreaId(areaId);
    }

    /**
     * 根据试题id真题试卷
     *
     * @return
     */
    public ShenlunPaper findById(int id) throws IOException {
        return shenlunPaperDao.findById(id);
    }
}
