package com.huatu.ztk.search.hystrix;

import com.huatu.ztk.search.bean.KeywordSearchBean;
import com.huatu.ztk.search.dao.CourseKeyWordDao;
import com.huatu.ztk.search.util.SpringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author zhengyi
 * @date 2019-03-07 15:22
 **/
public class CommendHotWord extends HotWordFactory {

    private static final Logger log = LoggerFactory.getLogger(CommendHotWord.class);


    public CommendHotWord(long userId, int catgory) {
        super(userId, catgory);
    }

    @Override
    protected List<String> run() {
//        try {
//            if (new Random().nextBoolean()) {
//
//                Thread.sleep(1000);
//            }
//        } catch (InterruptedException e) {
//            log.error(e.toString());
//        }
        CourseKeyWordDao courseKeyWordDao = (CourseKeyWordDao) SpringTool.getBean("CourseKeyWordDao");
        //search all history
        List<KeywordSearchBean> results = courseKeyWordDao.queryMyWords(userId, catgory);
        return results.stream().map(KeywordSearchBean::getKeyword).collect(Collectors.toList());
    }

    @Override
    protected List<String> getFallback() {
        CommendHotWordRedis commendHotWordRedis = new CommendHotWordRedis(userId, catgory);
        log.error("this is fall-back : {}", "redis");
        try {
            Future<List<String>> queue = commendHotWordRedis.queue();
            return queue.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    protected String getCacheKey() {
        return "jbzm-nb";
    }
}