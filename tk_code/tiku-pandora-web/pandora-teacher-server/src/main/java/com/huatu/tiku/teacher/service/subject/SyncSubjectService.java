package com.huatu.tiku.teacher.service.subject;

/**
 * 原考试类型和科目表数据迁移到现有科目表中的实现接口
 * Created by huangqingpeng on 2018/8/24.
 */
public interface SyncSubjectService {

    /**
     * 同步科目信息通过考试类型名称
     * @param name
     * @return 返回同步成功地数据结构
     */
    Object syncQuestionByCatGory(String name);

    /**
     * 同步科目信息通过考试类型ID
     * @param id
     * @return 返回同步成功地数据结构
     */
    Object syncQuestionByCatGoryId(Integer id);
}
