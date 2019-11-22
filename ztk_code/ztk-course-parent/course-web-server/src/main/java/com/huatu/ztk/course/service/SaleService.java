package com.huatu.ztk.course.service;

import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.course.common.CourseClient;
import com.huatu.ztk.course.common.NetSchoolSydwUrl;
import com.huatu.ztk.course.common.NetSchoolUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by linkang on 11/30/16.
 */

@Service
public class SaleService {
    private static final Logger logger = LoggerFactory.getLogger(SaleService.class);

    @Autowired
    private CourseClient courseClient;

    @Autowired
    private CourseService courseService;

    private static final Map productIdMap = new HashMap();

    static {
        productIdMap.put(57713, "com.huatu.BrickQuestions.product57713");
        productIdMap.put(57461, "com.huatu.BrickQuestions.product57461");
        productIdMap.put(57834, "com.huatu.BrickQuestions.product57834");
        productIdMap.put(57835, "com.huatu.BrickQuestions.product57835");
        productIdMap.put(57544, "com.huatu.BrickQuestions.product57544");
    }

    public Object findDetail(Map<String, Object> parameterMap, String cv, int terminal, int catgory) throws Exception{
        Object result = courseClient.getJsonByEncryptJsonParams(parameterMap, getSaleDetailUrl(catgory), true);

        if (courseService.isIosAudit(catgory, terminal, cv)) {
            Map resultMap = (Map) result;
            int courseId = Integer.valueOf(parameterMap.get("rid").toString());
            resultMap.put("inPurchaseProductId", productIdMap.get(courseId));
        }

        return result;
    }

    private String getSaleDetailUrl(int catgory) {
        return catgory == CatgoryType.GONG_WU_YUAN ? NetSchoolUrl.SALE_DETAIL : NetSchoolSydwUrl.SYDW_SALE_DETAIL;
    }


}
