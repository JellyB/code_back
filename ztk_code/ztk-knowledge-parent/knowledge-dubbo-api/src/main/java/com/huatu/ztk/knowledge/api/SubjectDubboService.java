package com.huatu.ztk.knowledge.api;

import com.huatu.ztk.knowledge.bean.SubjectTree;

import java.util.List;

/**
 * Created by linkang on 17-4-18.
 */
public interface SubjectDubboService {

    /**
     * 获得科目对应的考试类型
     *
     * @param subject
     */
    int getCatgoryBySubject(int subject);

    /**
     * 判断是否为金融下的考试类型
     * 如果是,返回中国银行的科目id
     * 如果是申论科目返回行测
     * @param subject
     * @return
     */
    int getBankSubject(int subject);
    
    /**
     * 根据考试类型id获取考试类型名称
     * @param categoryId
     * @return
     */
    String getCategoryNameById(int categoryId);


    List<SubjectTree> getSubjectTree();
}
