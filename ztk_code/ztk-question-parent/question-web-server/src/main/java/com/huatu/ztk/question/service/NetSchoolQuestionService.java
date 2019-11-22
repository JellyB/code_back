package com.huatu.ztk.question.service;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.dao.NetSchoolQuestionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网校试题service层
 * Created by linkang on 8/29/16.
 */
@Service
public class NetSchoolQuestionService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private QuestionDubboService questionDubboService;


    // add by hanchao,2017-10-10
    // 不知道为什么去掉了，也不知道为什么这个没有走缓存或者Mongo,临时加上，供网站使用
    @Autowired
    private NetSchoolQuestionDao netSchoolQuestionDao;

    public List findBath(List<Integer> idList) throws Exception{
        List<Question> ret = netSchoolQuestionDao.findBath(idList);
        return ret;
    }

    /**
     * redis 试题id列表key
     * @param pointId
     * @return
     */
    public static final String getListKey(int pointId) {
        return "netschool_question_list_" + pointId;
    }

//    public List findBath(List<Integer> idList) throws Exception{
//        List<Question> ret = netSchoolQuestionDao.findBath(idList);
//        return ret;
//    }

    /**
     * 分页查询试题
     *
     * @param moduleIds
     * @param size
     * @param page
     * @return
     * @throws BizException
     */
    public List<Question> findQuestions(List<Integer> moduleIds, int size, int page) throws BizException{
        int moduleCount = moduleIds.size();
        int qcount = size / moduleCount;
        if (size % moduleCount != 0) { //不能整除,每个模块取的试题数+1
            ++qcount;
        }

        List<Question> resultList = new ArrayList<>();

        for (int i = 0; i < moduleCount; i++) {
            Integer moduleId = moduleIds.get(i);

            String listKey = getListKey(moduleId);

            ListOperations<String, String> opsForList = redisTemplate.opsForList();

            //第一页：0-19
            List<String> qidStrings = opsForList.range(listKey, (page - 1) * 20, page * 20 - 1);

            List<Integer> qids = qidStrings.stream().map(Integer::new).collect(Collectors.toList());
            List<Question> questions = questionDubboService.findBath(qids);

            if (i == moduleCount - 1) { //最后一个模块的试题数
                qcount = size - resultList.size();
            }

            if (questions.size() < qcount) {
                throw new BizException(ErrorResult.create(10002, "该模块已练习完毕"));
            }

            Collections.shuffle(questions);
            List<Question> subList = questions.subList(0, qcount);
            resultList.addAll(subList);
        }

        return resultList;
    }
}
