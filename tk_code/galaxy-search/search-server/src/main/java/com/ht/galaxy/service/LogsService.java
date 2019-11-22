package com.ht.galaxy.service;

import com.ht.galaxy.repository.LogsResopitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author gaoyuchao
 * @create 2018-08-06 9:50
 */
@Service
public class LogsService {

    @Autowired
    private LogsResopitory logsResopitory;

    public Object selectClassCvr(String classId) throws Exception {
        return logsResopitory.selectClassCvr(classId);
    }

}
