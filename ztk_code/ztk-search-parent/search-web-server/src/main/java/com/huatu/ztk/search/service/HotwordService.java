package com.huatu.ztk.search.service;

import com.huatu.ztk.search.config.HotWordConfig;
import com.huatu.ztk.search.dao.CourseKeyWordDao;
import com.huatu.ztk.search.dao.HotwordDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by renwenlong on 2016/9/8.
 */
@Service
public class HotwordService {
    private static final Logger logger = LoggerFactory.getLogger(HotwordService.class);

    @Autowired
    private HotwordDao hotwordDao;
    @Autowired
    private HotWordConfig hotWordConfig;

    /**
     * 根据科目查询热搜词
     *
     * @param catgory
     * @return
     */
    public List<String> query(int catgory) {
        //List<String> hotwords = hotwordDao.query(catgory);
        return hotWordConfig.hotWordComponent();
    }


}
