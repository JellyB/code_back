package com.huatu.ztk.question.api;

import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import com.huatu.ztk.question.util.PageUtil;

import java.util.List;

/**
 * 试题dubbo服务
 * Created by shaojieyue
 * Created time 2016-05-09 20:49
 */
public interface QuestionDubboService {

    /**
     * 根据id查询试题
     *
     * @param id
     * @return
     */
    public Question findById(int id);

    /**
     * 插入试题
     *
     * @param question
     */
    public void insert(Question question) throws IllegalQuestionException;

    /**
     * 批量查询试题
     *
     * @param ids
     * @return
     */
    public List<Question> findBath(List<Integer> ids);

    /**
     * 更新试题
     *
     * @param Question
     * @throws IllegalQuestionException
     */
    public void update(Question Question) throws IllegalQuestionException;


    int getRecommendedTime(List<Integer> ids);


    List<Question> findBatchV3(List<Integer> idList);

    PageUtil<Question> findByConditionV3(Integer type, Integer difficult, Integer mode, String points, String content, String ids, Integer page, Integer pageSize,String subject);

    /**
     * 批量查询试题，查询结果为有效试题，去重试题只展示新题数据
     * @param questionIds
     * @return
     */
    List<Question> findBathWithFilter(List<Integer> questionIds);

}
