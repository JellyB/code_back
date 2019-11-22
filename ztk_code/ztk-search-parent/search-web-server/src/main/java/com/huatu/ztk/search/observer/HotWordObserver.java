package com.huatu.ztk.search.observer;

import com.huatu.ztk.search.bean.KeyWordSearchBeanNew;
import com.huatu.ztk.search.dao.CourseKeyWordDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Observable;
import java.util.Observer;

/**
 * @author zhengyi
 * @date 2019-03-07 15:48
 **/
@Component
public class HotWordObserver implements Observer {
    private static final Logger log = LoggerFactory.getLogger(HotWordObserver.class);
    @Autowired
    private CourseKeyWordDao courseKeyWordDao;

    @Override
    public void update(Observable o, Object arg) {
        KeyWordSearchBeanNew keyWordSearchBeanNew = (KeyWordSearchBeanNew) arg;
        switch (keyWordSearchBeanNew.getOption()) {
            case INSERT:
                courseKeyWordDao.insert(keyWordSearchBeanNew);
                break;
            case UPDATE:
                courseKeyWordDao.update(keyWordSearchBeanNew);
                break;
            case DELETE:
                int count = courseKeyWordDao.delete(keyWordSearchBeanNew.getUid(), keyWordSearchBeanNew.getCatgory(), keyWordSearchBeanNew.getKeyword());
                if (count == 1) {
                    log.info("delete keyword search record success,keyword={}", keyWordSearchBeanNew.getKeyword());
                }
            default:
        }
    }
}