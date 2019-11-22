package com.huatu.ztk.search.service;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.search.bean.KeyWordSearchBeanNew;
import com.huatu.ztk.search.bean.KeywordSearchBean;
import com.huatu.ztk.search.bean.SearchErrors;
import com.huatu.ztk.search.dao.CourseKeyWordDao;
import com.huatu.ztk.search.hystrix.CommendHotWord;
import com.huatu.ztk.search.observer.HotWordObservable;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by renwenlong on 2016/9/8.
 */
@Service
public class CourseKeywordService {
    private static final Logger logger = LoggerFactory.getLogger(CourseKeywordService.class);
    private static final int KEYWORD_MAX_LENGTH = 20;

    @Autowired
    private CourseKeyWordDao courseKeyWordDao;

    @Autowired
    private HotWordObservable hotWordObservable;

    /**
     * 保存搜索记录
     *
     * @param userId
     * @param catgory
     * @param q
     */
    public void save(long userId, int catgory, String q) throws BizException {
        //获取当前时间
        long updateTime = System.currentTimeMillis();
        //格式化关键字
        String keyword = format(q);

        //查询该关键词历史搜索
        KeywordSearchBean result = courseKeyWordDao.query(userId, catgory, q);
        KeyWordSearchBeanNew keyWordSearchBeanNew = new KeyWordSearchBeanNew();
        if (result == null) {//搜索历史为空，新建记录
            //封装搜索信息
            KeywordSearchBean searchBean = KeywordSearchBean.builder()
                    .uid(userId)
                    .catgory(catgory)
                    .keyword(keyword)
                    .count(1)
                    .updateTime(updateTime)
                    .build();

            BeanUtils.copyProperties(searchBean, keyWordSearchBeanNew);
            keyWordSearchBeanNew.setOption(KeyWordSearchBeanNew.Option.INSERT);
            hotWordObservable.setChanged();
            hotWordObservable.notifyObservers(keyWordSearchBeanNew);
        } else {
            result.setCount(result.getCount() + 1);
            result.setUpdateTime(updateTime);
            BeanUtils.copyProperties(result, keyWordSearchBeanNew);
            keyWordSearchBeanNew.setOption(KeyWordSearchBeanNew.Option.UPDATE);
            hotWordObservable.setChanged();
            hotWordObservable.notifyObservers(keyWordSearchBeanNew);
        }
    }

    /**
     * 格式化关键字
     *
     * @param q
     * @return
     */
    private String format(String q) throws BizException {
        //去空格
        String keyword = StringUtils.trimToEmpty(q).replaceAll("\\s*", "");
        //判空
        if (StringUtils.isBlank(keyword)) {
            throw new BizException(SearchErrors.KEYWORD_EMPTY);
        }
        //过滤HTML标签注入
        keyword = StringEscapeUtils.escapeHtml4(keyword);

        //判断关键字长度
        if (keyword.length() > KEYWORD_MAX_LENGTH) {
            keyword = keyword.substring(0, KEYWORD_MAX_LENGTH);
        }

        return keyword;
    }

    /**
     * 查询我的历史搜索
     *
     * @param userId
     * @param catgory
     * @return
     */
    public List<String> queryMyWords(long userId, int catgory) {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        CommendHotWord commendHotWord = new CommendHotWord(userId, catgory);
        try {
            return commendHotWord.queue().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        context.close();
        return null;
    }

    /**
     * 根据关键字删除历史搜索记录
     *
     * @param userId
     * @param catgory
     * @param q
     * @return
     */
    public void delete(long userId, int catgory, String q) throws BizException {
        if (StringUtils.isBlank(q)) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        KeyWordSearchBeanNew keyWordSearchBeanNew = new KeyWordSearchBeanNew();
        keyWordSearchBeanNew.setOption(KeyWordSearchBeanNew.Option.DELETE);
        keyWordSearchBeanNew.setUid(userId);
        keyWordSearchBeanNew.setCatgory(catgory);
        keyWordSearchBeanNew.setKeyword(q);
        hotWordObservable.setChanged();
        hotWordObservable.notifyObservers(keyWordSearchBeanNew);
    }

    /**
     * 清空某用户搜索记录
     *
     * @param userId
     * @param catgory
     */
    public void clearAllKeywords(long userId, int catgory) {
        courseKeyWordDao.clearAllKeywords(userId, catgory);
    }
}
