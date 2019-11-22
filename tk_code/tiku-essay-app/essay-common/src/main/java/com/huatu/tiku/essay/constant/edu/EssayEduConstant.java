package com.huatu.tiku.essay.constant.edu;

import com.google.common.base.Joiner;

/**
 * Created by x6 on 2018/3/27.
 */
public class EssayEduConstant {

    /**
     * 试卷信息分页缓存
     * @return
     */
    public static String  getEduPaperList(long areaId,int page, int pageSize) {
        return Joiner.on("_").join("edu_paper_list",areaId,page,pageSize);
    }
    
    /**
     * 试卷信息分页缓存（无分页）
     * @param areaId
     * @return
     */
    public static String  getEduPaperList(long areaId) {
        return Joiner.on("_").join("edu_paper_list_all",areaId);
    }


}
