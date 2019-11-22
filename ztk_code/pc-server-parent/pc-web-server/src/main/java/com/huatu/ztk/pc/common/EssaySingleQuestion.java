package com.huatu.ztk.pc.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jbzm
 * @date 2018下午9:02
 **/
public class EssaySingleQuestion {
    public final static List<String> essayQuesrion;

    static {
        essayQuesrion = new ArrayList<>();
        essayQuesrion.add("归纳概括");
        essayQuesrion.add("综合分析");
        essayQuesrion.add("提出对策");
        essayQuesrion.add("应用文");
        essayQuesrion.add("议论文");
    }

}
