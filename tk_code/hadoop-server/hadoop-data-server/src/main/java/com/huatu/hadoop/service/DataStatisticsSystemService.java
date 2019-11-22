package com.huatu.hadoop.service;

import com.huatu.hadoop.bean.CourseWareDTO;
import com.huatu.hadoop.util.HBaseUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataStatisticsSystemService {

    /**
     * @param userId
     * @param list
     * @return
     */
    public Object queryAccuracy(Long userId, List<CourseWareDTO> list) {
        HBaseUtil hbase = new HBaseUtil();
        if (list != null) {

            for (CourseWareDTO cw : list) {

                String rowKey = userId + "-" + cw.getCoursewareId() + "_" + cw.getCoursewareType() + "_" + cw.getQuestionSource();

                Map accuracy = new HashMap();
                try {
                    accuracy = HBaseUtil.get2Version("courseAccuracy", rowKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (accuracy != null) {
                    String courseAnalyze = "";

                    Map courseWareAccuracyInfo = (Map<String, String>) accuracy.get("accuracyinfo");
                    if (courseWareAccuracyInfo != null) {

                        courseAnalyze = (String) courseWareAccuracyInfo.get("accuracy");
                    }

                    cw.setAccuracy(courseAnalyze);

                } else {
                    cw.setAccuracy("");
                }
            }
        }
        return list;
    }
}
