package com.huatu.ztk.commons;


import com.google.common.collect.Maps;

import java.util.*;

/**
 * 各个模块id列表
 * Created by shaojieyue
 * Created time 2016-06-30 20:31
 */
@Deprecated
public class ModuleConstants {

    public static final Map<Integer,List<Module>>  MODULES_POINT_CATGORY = Maps.newHashMap();

    static {
        /**
         * 公务知识点模块列表
         */
        List<Module> gwyXingce= Collections.unmodifiableList(Arrays.asList(
                new Module(392, "常识判断"),
                new Module(435, "言语理解与表达"),
                new Module(482, "数量关系"),
                new Module(642, "判断推理"),
                new Module(754, "资料分析")
        ));
        //公务员行测知识点模块列表
        MODULES_POINT_CATGORY.put(SubjectType.GWY_XINGCE,gwyXingce);

        //事业单位行测知识点列表
        MODULES_POINT_CATGORY.put(SubjectType.SYDW_XINGCE,gwyXingce);
        /**
         * 公务知识点模块列表
         */
        List<Module> sydwGongji= Collections.unmodifiableList(Arrays.asList(
                new Module(3125, "政治"),
                new Module(3195, "经济"),
                new Module(3250, "管理"),
                new Module(3280, "公文"),
                new Module(3298, "人文科技"),
                new Module(3332, "法律")));

        //事业单位公共基础
        MODULES_POINT_CATGORY.put(SubjectType.SYDW_GONGJI,sydwGongji);
    }


    /**
     * 根据知识点类目查看该类目下所有的知识模块
     * @param subject
     * @return
     */
    public static final List<Module> getModulesBySubject(int subject){
        return MODULES_POINT_CATGORY.getOrDefault(subject,new ArrayList<>());
    }
}
