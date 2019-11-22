package com.huatu.ztk.pc.common;

import com.huatu.ztk.knowledge.bean.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ht on 2016/9/14.
 */
public class ModuleConvert {
    public static final Map<Integer,String> knowledgeMap = new HashMap();

    static {
        knowledgeMap.put(392,"常识判断");
        knowledgeMap.put(435,"言语理解与表达");
        knowledgeMap.put(482,"数量关系");
        knowledgeMap.put(642,"判断推理");
        knowledgeMap.put(754,"资料分析");
    }
}
