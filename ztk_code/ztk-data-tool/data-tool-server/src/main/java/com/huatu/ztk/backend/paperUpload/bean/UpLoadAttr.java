package com.huatu.ztk.backend.paperUpload.bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by lenovo on 2017/6/12.
 * 所有标签解析特性集合
 */
public class UpLoadAttr {
    //所有试卷属性的集合
    public static final Map<String,String> paperAttrMap = new HashMap<>();
    static{
        paperAttrMap.put("areas", PaperAttr.PAPER_AREA);
        paperAttrMap.put("catgory",PaperAttr.PAPER_SUBJECT);
        paperAttrMap.put("name",PaperAttr.PAPER_NAME);
        paperAttrMap.put("score",PaperAttr.PAPER_SCORE);
        paperAttrMap.put("time",PaperAttr.PAPER_TIME);
        paperAttrMap.put("year",PaperAttr.PAPER_YEAR);
    }
    //所有富文本字段的集合
    public static final Set<String> textSet= new HashSet<String>();
    static {
        textSet.add("analysis");
        textSet.add("stem");
        textSet.add("scoreExplain");
        textSet.add("referAnalysis");
        textSet.add("examPoint");
        textSet.add("answerRequire");
        textSet.add("solvingIdea");
        textSet.add("require");
        textSet.add("material");
        textSet.add("orgin");
        textSet.add("expand");
    }
}
