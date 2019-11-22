package com.huatu.hadoop.service;

import com.huatu.hadoop.util.HBaseUtil;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestService {

    public static void main(String[] args) throws Exception {

        Map totalStationReport = HBaseUtil.get("test_total_station_ability_assessment", 9952189 + "-" + 1);
        Map aai = (Map) totalStationReport.get("ability_assessment_info");
        String spn = Bytes.toString(aai.get("spn").toString().getBytes());
        String sp = Bytes.toString(aai.get("sp").toString().getBytes());

        String[] pointName = spn.split(",");
        String[] pointid = sp.split(",");

        List<Map<String, Object>> l = new ArrayList<>();
        for (int i = 0; i < pointid.length; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", Integer.parseInt(pointid[i]));
            m.put("name", pointName[i]);
            m.put("accuracy", 0.00);
            m.put("num", 0);

            l.add(m);
        }
        String grade = aai.get("grade").toString();
        if (grade != null && !grade.equals("")) {
            String[] points = grade.split("_");

            for (String s : points) {

                String[] sr = s.split(":");
                int point = Integer.parseInt(sr[0]);
                int correct = Integer.parseInt(sr[1]);
                int num = Integer.parseInt(sr[2]);
                for (int i = 0; i < l.size(); i++) {
                    Map<String, Object> objectMap = l.get(i);
                    if (Integer.parseInt(objectMap.get("id").toString()) == point) {
                        objectMap.put("accuracy", correct * 1.0 / num);
                        objectMap.put("num", num);
                        continue;
                    }
                }
            }
        }

        System.out.println(l);
    }
}
