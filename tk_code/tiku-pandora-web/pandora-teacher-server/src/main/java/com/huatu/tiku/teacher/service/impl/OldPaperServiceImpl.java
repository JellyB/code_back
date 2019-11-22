package com.huatu.tiku.teacher.service.impl;

import com.huatu.tiku.teacher.dao.mongo.OldPaperDao;
import com.huatu.tiku.teacher.service.OldPaperService;
import com.huatu.ztk.paper.bean.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by huangqp on 2018\7\6 0006.
 */
@Service
public class OldPaperServiceImpl implements OldPaperService {
    @Autowired
    OldPaperDao oldPaperDao;
    @Override
    public Paper findPaperById(Integer paperId) {
        return oldPaperDao.findById(paperId);
    }
}

