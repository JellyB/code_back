package com.huatu.ztk.knowledge.api;


import com.huatu.ztk.knowledge.bean.Module;

import java.util.List;

/**
 * 模块dubbo接口
 * Created by shaojieyue
 * Created time 2016-05-19 13:29
 */
public interface ModuleDubboService {

    /**
     * 根据知识点id查询模块
     * @param pointId 知识点id
     * @return
     */
    public Module findByPointId(int pointId);


    public List<com.huatu.ztk.commons.Module> findSubjectModules(int subject);
}
