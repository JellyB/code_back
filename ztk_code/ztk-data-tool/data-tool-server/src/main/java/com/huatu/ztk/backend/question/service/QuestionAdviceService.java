package com.huatu.ztk.backend.question.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.paper.bean.TikuQuestionType;
import com.huatu.ztk.backend.question.bean.*;
import com.huatu.ztk.backend.question.bean.QuestionAdvice;
import com.huatu.ztk.backend.question.dao.QuestionAdviceDao;
import com.huatu.ztk.backend.util.FuncStr;
import com.huatu.ztk.question.bean.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by ht on 2017/1/4.
 */
@Service
public class QuestionAdviceService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionAdviceService.class);

    @Autowired
    private QuestionAdviceDao questionAdviceDao;

    @Autowired
    private QuestionService questionService;




    /**
     * 试题纠错列表
     * @param advice  试题纠错处理对象
     * @return
     */
    public List<AdviceBean> list(AdviceBean advice,String area){
        List<AdviceBean> adviceBeanList=questionAdviceDao.list(advice,area);
        if(CollectionUtils.isNotEmpty(adviceBeanList)&&adviceBeanList.size()>0){
            List<Integer> qids= Lists.newArrayList();
            for(AdviceBean adviceBean:adviceBeanList){
                qids.add(adviceBean.getQid());
            }
            Map<Integer,Question> questionMap=questionService.findQuestionByIds(qids);
            for(AdviceBean adviceBean:adviceBeanList){
              if(questionMap.containsKey(adviceBean.getQid())){
                  Question question=questionMap.get(adviceBean.getQid());
                  if(question instanceof  GenericQuestion){ //单一题
                      GenericQuestion genericQuestion=(GenericQuestion)question;
                      adviceBean.setStem(FuncStr.replaceHtml(genericQuestion.getStem()));
                      adviceBean.setType(TikuQuestionType.SINGLE_OBJECTIVE);
                  }else if (question instanceof GenericSubjectiveQuestion){ //复合题
                      GenericSubjectiveQuestion genericQuestion=(GenericSubjectiveQuestion)question;
                      adviceBean.setType(TikuQuestionType.SINGLE_SUBJECTIVE);
                      adviceBean.setStem(FuncStr.replaceHtml(genericQuestion.getStem()));
                  }else if(question instanceof CompositeQuestion){
                      adviceBean.setStem(FuncStr.replaceHtml(question.getMaterial()));
                      adviceBean.setType(TikuQuestionType.MULTI_OBJECTIVE);
                  }else if(question instanceof CompositeSubjectiveQuestion){
                      adviceBean.setStem(FuncStr.replaceHtml(question.getMaterial()));
                      adviceBean.setType(TikuQuestionType.MULTI_SUBJECTIVE);

                  }
                  adviceBean.setMode(question.getMode());
              }
            }
        }
        return adviceBeanList;
    }

    /**
     * 获取纠错详情
     * @param id
     * @return
     */
    public QuestionAdvice findAdvice(int id){
       QuestionAdvice questionAdvice=questionAdviceDao.findById(id);
        QuestionDetail questionDetail= (QuestionDetail) questionService.findAllTypeById(questionAdvice.getQid());
       questionAdvice.setQuestionDetail(questionDetail);
       return questionAdvice;
    }

    /**
     * 处理试题为不采纳
     * @param id
     * @param reason
     */
    public void dealNoAdoption(int id,String reason){
        questionAdviceDao.dealNoAdoption(id,reason);
    }

    /**
     * 处理试题采纳
     * @param id
     */
    public void dealAdoption(int id){
        questionAdviceDao.dealAdoption(id);
    }

    /**
     * 处理试题为不使用
     * @param id
     */
    public void dealNoUse(int id){
        questionAdviceDao.dealNoUse(id);
    }

    /**
     * 处理试题为使用状态
     * @param id
     */
    public void dealUse(int id){
        questionAdviceDao.dealUse(id);
    }

    /**
     * 删除试题纠错信息
     * @param id
     */
    public void delete(int id){
        questionAdviceDao.delete(id);
    }
}
