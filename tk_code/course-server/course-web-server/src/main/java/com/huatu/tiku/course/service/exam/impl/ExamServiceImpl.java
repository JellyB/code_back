package com.huatu.tiku.course.service.exam.impl;


import com.google.common.base.Stopwatch;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.common.ArticleTypeListEnum;
import com.huatu.tiku.course.netschool.api.ExamNetSchoolService;
import com.huatu.tiku.course.service.exam.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.text.StrBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/2/19
 * @描述 备考精华相关
 */

@Slf4j
@Service
public class ExamServiceImpl implements ExamService {


    @Autowired
    private ExamNetSchoolService examNetSchoolService;

    @Autowired
    RedisTemplate redisTemplate;


    /**
     * 备考精华列表查询
     *
     * @param
     * @return
     */
    public Object getArticleList(int type, int page, int pageSize, int category) {
        HashMap param = new HashMap();
        param.put("type", type);
        param.put("page", page);
        param.put("pageSize", pageSize);

        String articleListKey = articleListKey(type, page, pageSize, category);
        log.info("articleListKey是:{},category是:{}", articleListKey, category);
        HashOperations hashOperations = redisTemplate.opsForHash();
        Object article = hashOperations.get(articleListKey, category + "");
        if (null != article) {
            return article;
        }
        NetSchoolResponse article1List = examNetSchoolService.getArticleList(param);
        if (article1List.getData() != null) {
            hashOperations.put(articleListKey, category + "", article1List.getData());
            redisTemplate.expire(articleListKey, 1, TimeUnit.MINUTES);
        }
        return article1List.getData();
    }

    /**
     * 备考精华文章详情
     *
     * @param id
     * @return
     */
    public Object detail(int id) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        HashMap params = new HashMap();
        params.put("id", id);
        NetSchoolResponse detailResponse = examNetSchoolService.detail(params);
        log.info("备考精华详情文章ID是:{},耗时:{}", id, String.valueOf(stopwatch.stop()));
        return detailResponse.getData();
    }

    /**
     * 备考精华分类列表（直接写死在Java）
     *
     * @return
     */
    public List<HashMap<String, Object>> typeList() {
        ArticleTypeListEnum[] values = ArticleTypeListEnum.values();
        List<HashMap<String, Object>> result = new ArrayList<>();
        for (ArticleTypeListEnum article : values) {
            HashMap map = new HashMap();
            map.put("sort", article.getSort());
            map.put("type", article.getCode());
            map.put("name", article.getName());
            result.add(map);
        }
        return result;
    }


    /**
     * 用户点赞
     *
     * @param map
     * @return
     */
    public NetSchoolResponse like(HashMap map) {
        NetSchoolResponse like = examNetSchoolService.like(map);
        return like;
    }


    /**
     * 备考精华列表缓存key
     */
    public String articleListKey(int type, int page, int pageSize, int category) {
        StringBuffer listKey = new StringBuffer();
        return listKey.append("article:cache").append(":")
                .append(page).append(":")
                .append(pageSize).append(":")
                .append(type).toString();
    }

    /**
     * 文章详情缓存key
     */
    public String articleDetail(int aid) {
        StrBuilder detailKey = new StrBuilder();
        return detailKey.append("article").append(":")
                .append("detail").append(":")
                .append(aid).toString();
    }

}

