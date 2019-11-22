package com.huatu.hadoop.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huatu.hadoop.bean.ConditionDTO;
import com.huatu.hadoop.bean.UserPictureDTO;
import com.huatu.hadoop.util.CalendarUtil;
import com.huatu.hadoop.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class UserPictureService {


    private static final String body_temp = "{" +
            "\"sql\":\"%s \"," +
            "\"offset\":0," +
            "\"limit\":50000," +
            "\"acceptPartial\":false," +
            "\"project\":\"%s\"" +
            "}";

    private final static SimpleDateFormat DATE_FORMAT_FROM = new SimpleDateFormat("yyyyMMdd");
    private final static SimpleDateFormat DATE_FORMAT_TO = new SimpleDateFormat("yyyy年MM月dd日");

    private final static SimpleDateFormat WEEK_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 第w周");

    private final static SimpleDateFormat MONTH_FORMAT_FROM = new SimpleDateFormat("yyyyMM");
    private final static SimpleDateFormat MONTH_FORMAT_TO = new SimpleDateFormat("yyyy年MM月");

    private final static SimpleDateFormat HOUR_FORMAR_FROM = new SimpleDateFormat("yyyyMMddHH");
    private final static SimpleDateFormat HOUR_FORMAR_TO = new SimpleDateFormat("yyyy年MM月dd日HH时");

    @Autowired
    private MysqlService mysqlService;

    private static String kylin_project = "v_user_answer";
    private static String kylin_project2 = "v_user_video";
    private static String kylin_project3 = "v_user_match";


    public Object getVideoActive(String start,
                                 String end,
                                 List<ConditionDTO> cons
    ) throws Exception {

        Map<String, Object> cMap = new HashMap<>();

        List<String> arr = new ArrayList<>();
        arr.add("时段");
        arr.add("活跃总数");
        arr.add("工作日活跃人数");
        arr.add("周末活跃人数");
        cMap.put("columns", arr);


        String sqlTemp = "" +
                "SELECT " +
                " a._time AS the_time, " +
                " COALESCE(a.isresult, 0) AS total_count, " +
                " COALESCE(b.isresult, 0) AS work_count, " +
                " COALESCE(c.isresult, 0) AS week_count " +
                "FROM (" +
                " SELECT " +
                "  count(DISTINCT V_USER_VIDEO.UID) AS isresult ,V_USER_VIDEO.CREATE_TIME_DH AS _time " +
                " FROM " +
                "  V_USER_VIDEO " +
                " LEFT JOIN  USER_PICTURE ON V_USER_VIDEO.UNAME =  USER_PICTURE.UNAME " +
                " WHERE " +
                "  %s " +
                " GROUP BY " +
                "  V_USER_VIDEO.CREATE_TIME_DH " +
                ") a " +
                "LEFT JOIN ( " +
                " SELECT " +
                "  count(DISTINCT V_USER_VIDEO.UID) AS isresult ,V_USER_VIDEO.CREATE_TIME_DH AS _time " +
                " FROM " +
                "  V_USER_VIDEO " +
                " LEFT JOIN  USER_PICTURE ON V_USER_VIDEO.UNAME =  USER_PICTURE.UNAME " +
                " WHERE " +
                "  %s " +
                " AND create_time_work = 1 " +
                " GROUP BY " +
                "  V_USER_VIDEO.CREATE_TIME_DH " +
                ") AS b ON a._time = b._time " +
                "LEFT JOIN ( " +
                " SELECT " +
                "  count(DISTINCT V_USER_VIDEO.UID) AS isresult ,V_USER_VIDEO.CREATE_TIME_DH AS _time " +
                " FROM " +
                "  V_USER_VIDEO " +
                " LEFT JOIN  USER_PICTURE ON V_USER_VIDEO.UNAME =  USER_PICTURE.UNAME " +
                " WHERE " +
                "  %s " +
                " AND create_time_work = 0 " +
                " GROUP BY " +
                "  V_USER_VIDEO.CREATE_TIME_DH " +
                ") AS c ON a._time = c._time";

        String where_append = String.format(" create_time_d >= '%s' and create_time_d <= '%s' and 1 = 1 ", start, end);

        if (cons != null && cons.size() > 0) {

            UserPictureDTO userPictureDTO = new UserPictureDTO(cons);
            where_append = where_append + " and " + userPictureDTO.createSql();
        }


        String new_sql_1 = String.format(sqlTemp, where_append, where_append, where_append);
        String new_sql_2 = new_sql_1 + " order by a._time ";


        List<Map<String, Object>> hha = createTimesMap();


        return activeResult(cMap, new_sql_2, arr, hha, kylin_project2);
    }

    public Object getAnswerActive(String start,
                                  String end,
                                  List<ConditionDTO> cons
    ) throws Exception {

        Map<String, Object> cMap = new HashMap<>();

        List<String> arr = new ArrayList<>();
        arr.add("时段");
        arr.add("活跃总数");
        arr.add("工作日活跃人数");
        arr.add("周末活跃人数");
        cMap.put("columns", arr);

        String sqlTemp = "" +
                "select  a._time  as the_time,COALESCE(a.isresult, 0) as total_count,COALESCE(b.isresult, 0) as work_count,COALESCE(c.isresult, 0) as week_count " +
                "from " +
                "(select count(distinct V_USER_ANSWER.UID) as isresult, V_USER_ANSWER.CREATE_TIME_DH  as _time from  V_USER_ANSWER left join  USER_PICTURE on V_USER_ANSWER.UNAME =  USER_PICTURE.UNAME   where %s  group by   V_USER_ANSWER.CREATE_TIME_DH ) a " +
                "left join " +
                "(select count(distinct V_USER_ANSWER.UID) as isresult, V_USER_ANSWER.CREATE_TIME_DH  as _time from  V_USER_ANSWER left join  USER_PICTURE on V_USER_ANSWER.UNAME =  USER_PICTURE.UNAME   where %s and create_time_work = 1  group by    V_USER_ANSWER.CREATE_TIME_DH " +
                ") as b on a._time=b._time left join (" +
                "select count(distinct V_USER_ANSWER.UID) as isresult, V_USER_ANSWER.CREATE_TIME_DH  as _time from  V_USER_ANSWER left join  USER_PICTURE on V_USER_ANSWER.UNAME =  USER_PICTURE.UNAME   where %s and create_time_work = 0  group by   V_USER_ANSWER.CREATE_TIME_DH " +
                ") as c on a._time=c._time";

        String where_append = String.format(" create_time_d >= '%s' and create_time_d <= '%s' and 1 = 1 ", start, end);
        if (cons != null && cons.size() > 0) {

            UserPictureDTO userPictureDTO = new UserPictureDTO(cons);
            where_append = where_append + " and " + userPictureDTO.createSql();
        }
        String new_sql_1 = String.format(sqlTemp, where_append, where_append, where_append);
        String new_sql_2 = new_sql_1 + " order by a._time ";

        List<Map<String, Object>> hha = createTimesMap();

        return activeResult(cMap, new_sql_2, arr, hha, kylin_project);
    }

    public Object getNum(String start,
                         String end,
                         List<ConditionDTO> cons) throws Exception {

        Map<String, Object> cMap = new HashMap<>();

        List<String> arr = new ArrayList<>();
        arr.add("范围");
        arr.add("人数");
        cMap.put("columns", arr);


        String sqlTemp = "" +
                "select sum(V_USER_ANSWER.QUESTION_NUM ),V_USER_ANSWER.UNAME from V_USER_ANSWER  " +
                "left join  USER_PICTURE on   V_USER_ANSWER.UNAME =  USER_PICTURE.UNAME " +
                "where %s and V_USER_ANSWER.UNAME != '' " +
                "group by V_USER_ANSWER.UNAME";


        String where_append = String.format(" create_time_d >= '%s' and create_time_d <= '%s' and 1 = 1 ", start, end);
        if (cons != null && cons.size() > 0) {

            UserPictureDTO userPictureDTO = new UserPictureDTO(cons);
            where_append = where_append + " and " + userPictureDTO.createSql();

        }

        String sql = String.format(sqlTemp, where_append);

        Map<String, Integer> countMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String k1 = "0";
                String k2 = "0";
                if (o1.contains("~")) {
                    k1 = o1.split("~")[0];
                } else if (o1.contains("以上")) {
                    k1 = "1000";
                } else {
                    k1 = "0";
                }
                if (o2.contains("~")) {
                    k2 = o2.split("~")[0];
                } else if (o2.contains("以上")) {
                    k2 = "1000";
                } else {
                    k2 = "0";
                }


                return -Integer.parseInt(k2) + Integer.parseInt(k1);
            }
        });

        JSONArray ja = sendToKylin(sql, kylin_project);
        countMap.put("0", 0);
        countMap.put("1~100", 0);
        countMap.put("101~200", 0);
        countMap.put("201~400", 0);
        countMap.put("401~600", 0);
        countMap.put("601~800", 0);
        countMap.put("801~1000", 0);
        countMap.put("1000以上", 0);


        for (int i = 0; i < ja.size(); i++) {
            JSONArray jo = ja.getJSONArray(i);

            Integer k = jo.getInteger(0) == null ? 0 : jo.getInteger(0);
            String name = jo.getString(1) == null ? "" : jo.getString(1);

            String key = "0";

            if (k == 0) {
                Integer temp = countMap.getOrDefault("0", 0);
                temp++;
                countMap.put("0", temp);
            }
            if (k >= 1 && k < 101) {
                Integer temp = countMap.getOrDefault("1~100", 0);
                temp++;
                countMap.put("1~100", temp);
            }
            if (k >= 101 && k < 201) {
                Integer temp = countMap.getOrDefault("101~200", 0);
                temp++;
                countMap.put("101~200", temp);
            }
            if (k >= 201 && k < 401) {
                Integer temp = countMap.getOrDefault("201~400", 0);
                temp++;
                countMap.put("201~400", temp);
            }
            if (k >= 401 && k < 601) {
                Integer temp = countMap.getOrDefault("401~600", 0);
                temp++;
                countMap.put("401~600", temp);
            }
            if (k >= 601 && k < 801) {
                Integer temp = countMap.getOrDefault("601~800", 0);
                temp++;
                countMap.put("601~800", temp);
            }
            if (k >= 801 && k < 1001) {
                Integer temp = countMap.getOrDefault("801~1000", 0);
                temp++;
                countMap.put("801~1000", temp);
            }
            if (k >= 1001) {
                Integer temp = countMap.getOrDefault("1000以上", 0);
                temp++;
                countMap.put("1000以上", temp);
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, Integer> en : countMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            String key = en.getKey();
            Integer value = en.getValue();
            map.put("范围", key);
            map.put("人数", value);
            rows.add(map);
        }
        Collections.sort(rows, (o1, o2) -> o1.get("范围").toString().compareTo(o1.get("范围").toString()));
        cMap.put("rows", rows);

        return cMap;
    }

    public Object getMatchActive(String start,
                                 String end,
                                 List<ConditionDTO> cons
    ) throws Exception {

        Map<String, Object> cMap = new HashMap<>();

        List<String> arr = new ArrayList<>();
        arr.add("时段");
        arr.add("活跃总数");
        arr.add("工作日活跃人数");
        arr.add("周末活跃人数");
        cMap.put("columns", arr);


        String sqlTemp = "" +
                "SELECT " +
                " a._time AS the_time, " +
                " a.isresult AS total_count, " +
                " b.isresult AS work_count, " +
                " c.isresult AS week_count " +
                "FROM " +
                " ( " +
                "  SELECT " +
                "   count(DISTINCT V_USER_MATCH.USER_ID) AS isresult, " +
                "   V_USER_MATCH.CREATE_TIME_DH AS _time " +
                "  FROM " +
                "   V_USER_MATCH " +
                "  LEFT JOIN USER_PICTURE ON V_USER_MATCH.UNAME = USER_PICTURE.UNAME " +
                "  WHERE " +
                "   CREATE_TIME_D >= '20190601' " +
                "  AND CREATE_TIME_D <= '20190702' " +
                "  AND 1 = 1 " +
                "  GROUP BY " +
                "   V_USER_MATCH.CREATE_TIME_DH " +
                " ) a " +
                "LEFT JOIN ( " +
                " SELECT " +
                "  count(DISTINCT V_USER_MATCH.USER_ID) AS isresult, " +
                "  V_USER_MATCH.CREATE_TIME_DH AS _time " +
                " FROM " +
                "  V_USER_MATCH " +
                " LEFT JOIN USER_PICTURE ON V_USER_MATCH.UNAME = USER_PICTURE.UNAME " +
                " WHERE " +
                "  CREATE_TIME_D >= '20190601' " +
                " AND CREATE_TIME_D <= '20190702' " +
                " AND 1 = 1 " +
                " AND create_time_work = 1 " +
                " GROUP BY " +
                "  V_USER_MATCH.CREATE_TIME_DH " +
                ") AS b ON a._time = b._time " +
                "LEFT JOIN ( " +
                " SELECT " +
                "  count(DISTINCT V_USER_MATCH.USER_ID) AS isresult, " +
                "  V_USER_MATCH.CREATE_TIME_DH AS _time " +
                " FROM " +
                "  V_USER_MATCH " +
                " LEFT JOIN USER_PICTURE ON V_USER_MATCH.UNAME = USER_PICTURE.UNAME " +
                " WHERE " +
                "  CREATE_TIME_D >= '20190601' " +
                " AND CREATE_TIME_D <= '20190702' " +
                " AND 1 = 1 " +
                " AND create_time_work = 0 " +
                " GROUP BY " +
                "  V_USER_MATCH.CREATE_TIME_DH " +
                ") AS c ON a._time = c._time " +
                "ORDER BY " +
                " a._time";

        String where_append = String.format(" play_d >= '%s' and play_d <= '%s' and 1 = 1 ", start, end);

        if (cons != null && cons.size() > 0) {

            UserPictureDTO userPictureDTO = new UserPictureDTO(cons);
            where_append = where_append + " and " + userPictureDTO.createSql();
        }

        String new_sql_1 = String.format(sqlTemp, where_append, where_append, where_append);
//        String new_sql_2 = new_sql_1 + " order by a._time ";

        List<Map<String, Object>> hha = createTimesMap();

        return activeResult(cMap, new_sql_1, arr, hha, kylin_project3);
    }

    public Object getMatchTop(String start,
                              String end,
                              List<ConditionDTO> cons
    ) throws Exception {
        Map<String, Object> m = new HashMap<>();

        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> result2 = new ArrayList<>();

        String sqlTemp = "" +
                "SELECT  " +
                "  V_USER_MATCH.MATCH_NAME,  " +
                "  COUNT(*),  " +
                "  sum(V_USER_MATCH.IS_PRACTICE)  " +
                "FROM  " +
                "  V_USER_MATCH  " +
                "LEFT JOIN USER_PICTURE ON V_USER_MATCH.UNAME = USER_PICTURE.UNAME  " +
                "WHERE  " +
                " %s " +
                "GROUP BY  " +
                "  V_USER_MATCH.MATCH_NAME  " +
                "ORDER BY  " +
                "  COUNT(*) DESC";

        String where_append = String.format(" create_time_d >= '%s' and create_time_d <= '%s' and 1 = 1 ", start, end);

        if (cons != null && cons.size() > 0) {

            UserPictureDTO userPictureDTO = new UserPictureDTO(cons);
            where_append = where_append + " and " + userPictureDTO.createSql();
        }
        String new_sql_1 = String.format(sqlTemp, where_append);

        JSONArray ja = sendToKylin(new_sql_1, kylin_project3);
        for (int i = 0; i < ja.size(); i++) {
            JSONArray jo = ja.getJSONArray(i);
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> map2 = new HashMap<>();
            map.put("name", jo.getString(0) == null ? 0 : jo.getString(0));
            map.put("b", jo.getInteger(1) == null ? 0 : jo.getInteger(1));
            map.put("c", jo.getInteger(1) == null ? 0 : jo.getInteger(2));


            map2.put("name", jo.getString(0) == null ? 0 : jo.getString(0));
            map2.put("b", jo.getInteger(1) == null ? 0 : jo.getInteger(1));
            map2.put("c", jo.getInteger(2) == null ? 0 : jo.getInteger(2));
            result.add(map);
            result2.add(map2);
        }

        Collections.sort(result, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {

                int i = Integer.parseInt(o1.get("b").toString());
                int i2 = Integer.parseInt(o2.get("b").toString());
                return i2 - i;
            }
        });
        result2.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                int i = Integer.parseInt(o1.get("c").toString());
                int i2 = Integer.parseInt(o2.get("c").toString());
                return i2 - i;
            }
        });

        m.put("chartData", result);
        m.put("chartData2", result2);
        return m;
    }

    public Object getMatchPer(String start, String end, List<ConditionDTO> cons) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();

        String sqlTemp = "" +
                "SELECT  " +
                "  COUNT(*),  " +
                "  sum(V_USER_MATCH.IS_PRACTICE )  " +
                "FROM  " +
                "  V_USER_MATCH  " +
                "LEFT JOIN USER_PICTURE ON V_USER_MATCH.UNAME = USER_PICTURE.UNAME " +
                " WHERE %s";

        String where_append = String.format(" create_time_d >= '%s' and create_time_d <= '%s' and 1 = 1 ", start, end);

        if (cons != null && cons.size() > 0) {

            UserPictureDTO userPictureDTO = new UserPictureDTO(cons);
            where_append = where_append + " and " + userPictureDTO.createSql();
        }
        String new_sql_1 = String.format(sqlTemp, where_append);

        JSONArray ja = sendToKylin(new_sql_1, kylin_project3);
        JSONArray jo = ja.getJSONArray(0);
        Map<String, Object> map = new HashMap<>();

        int c = jo.getInteger(0) == null ? 0 : jo.getInteger(0);
        int b = jo.getInteger(1) == null ? 0 : jo.getInteger(1);
        map.put("canjia", b);
        map.put("baoming", c);


        map.put("percentage", b * 1.0 / c);
        result.add(map);
        return result;
    }

    public static JSONArray sendToKylin(String sql, String project) {

        String results = HttpUtil.query(
                String.format(body_temp, sql, project));

        log.info("UserPictureService activeResult 1 created sql : {} ", sql);

        JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
        return ja;
    }


    private static List<Map<String, Object>> createTimesMap() throws ParseException {

        List<Map<String, Object>> timesMap = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            Map<String, Object> stringObjectMap = new HashMap<>();
            stringObjectMap.put("时段", i);
            stringObjectMap.put("活跃总数", 0);
            stringObjectMap.put("工作日活跃人数", 0);
            stringObjectMap.put("周末活跃人数", 0);
            timesMap.add(stringObjectMap);
        }
        return timesMap;
    }

    private synchronized Object activeResult(Map<String, Object> cMap,
                                             String sql,
                                             List<String> arr,
                                             List<Map<String, Object>> time_list, String project) throws ParseException {


        JSONArray ja = sendToKylin(sql, project);

        for (int i = 0; i < ja.size(); i++) {
            JSONArray jo = ja.getJSONArray(i);

            int _time = jo.getInteger(0) == null ? 0 : jo.getInteger(0);
            int _count = jo.getInteger(1) == null ? 0 : jo.getInteger(1);

            int iswork = jo.getInteger(2) == null ? 0 : jo.getInteger(2);
            int isweek = jo.getInteger(3) == null ? 0 : jo.getInteger(3);


            for (int j = 0; j < time_list.size(); j++) {
                Map<String, Object> map = time_list.get(j);
                int time = Integer.parseInt(map.getOrDefault(arr.get(0), 0).toString());


                if ((time + "").equals(_time + "")) {
                    map.put("时段", time);
                    map.put("活跃总数", _count);
                    map.put("工作日活跃人数", iswork);
                    map.put("周末活跃人数", isweek);
                    break;
                }
            }


        }

        Collections.sort(time_list, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                int i = Integer.parseInt(o1.get("时段").toString());
                int j = Integer.parseInt(o2.get("时段").toString());
                return i - j;
            }
        });
        cMap.put("rows", time_list);

        return cMap;
    }


    public Object getMatchCount(String start, String end, List<ConditionDTO> cons) throws Exception {

        Map<String, Object> cMap = new HashMap<>();
        Map<String, Object> cMap_child1 = new HashMap<>();
        Map<String, Object> cMap_child2 = new HashMap<>();

        List<String> chartData1 = new ArrayList<>();
        chartData1.add("次数");
        chartData1.add("报名人数");
        cMap_child1.put("columns", chartData1);

        List<String> charData2 = new ArrayList<>();
        charData2.add("次数");
        charData2.add("参加人数");
        cMap_child2.put("columns", charData2);


        String sqlTemp = "" +
                "select v_user_match.uname,count(*),sum(is_practice) from v_user_match   left join user_picture   on v_user_match.uname = user_picture.uname " +
                "where %s " +
                "group by v_user_match.uname";


        String where_append = String.format(" create_time_d >= '%s' and create_time_d <= '%s' and 1 = 1 ", start, end);
        if (cons != null && cons.size() > 0) {

            UserPictureDTO userPictureDTO = new UserPictureDTO(cons);
            where_append = where_append + " and " + userPictureDTO.createSql();

        }

        String sql = String.format(sqlTemp, where_append);

        Map<String, Integer> countMap = new TreeMap<>((o1, o2) -> -Integer.parseInt(o2.substring(0, 1)) + Integer.parseInt(o1.substring(0, 1)));
        countMap.put("0次", 0);
        countMap.put("1次", 0);
        countMap.put("2次", 0);
        countMap.put("3次", 0);
        countMap.put("4次", 0);
        countMap.put("5次", 0);
        countMap.put("5次以上", 0);


        Map<String, Integer> sumMap = new TreeMap<>((o1, o2) -> -Integer.parseInt(o2.substring(0, 1)) + Integer.parseInt(o1.substring(0, 1)));
        sumMap.put("0次", 0);
        sumMap.put("1次", 0);
        sumMap.put("2次", 0);
        sumMap.put("3次", 0);
        sumMap.put("4次", 0);
        sumMap.put("5次", 0);
        sumMap.put("5次以上", 0);
        JSONArray ja = sendToKylin(sql, kylin_project3);
        for (int i = 0; i < ja.size(); i++) {
            JSONArray jo = ja.getJSONArray(i);

            Integer c = jo.getInteger(1) == null ? 0 : jo.getInteger(1);
            Integer n = jo.getInteger(2) == null ? 0 : jo.getInteger(2);

            String key = "0";

            if (c == 0) {
                Integer temp = countMap.getOrDefault("0次", 0);
                temp++;
                countMap.put("0次", temp);
            } else if (c == 1) {
                Integer temp = countMap.getOrDefault("1次", 0);
                temp++;
                countMap.put("1次", temp);
            } else if (c == 2) {
                Integer temp = countMap.getOrDefault("2次", 0);
                temp++;
                countMap.put("2次", temp);
            } else if (c == 3) {
                Integer temp = countMap.getOrDefault("3次", 0);
                temp++;
                countMap.put("3次", temp);
            } else if (c == 4) {
                Integer temp = countMap.getOrDefault("4次", 0);
                temp++;
                countMap.put("4次", temp);
            } else if (c == 5) {
                Integer temp = countMap.getOrDefault("5次", 0);
                temp++;
                countMap.put("5次", temp);
            } else if (c >= 6) {
                Integer temp = countMap.getOrDefault("5次以上", 0);
                temp++;
                countMap.put("5次以上", temp);
            }

            if (n == 0) {
                Integer temp = sumMap.getOrDefault("0次", 0);
                temp++;
                sumMap.put("0次", temp);
            } else if (n == 1) {
                Integer temp = sumMap.getOrDefault("1次", 0);
                temp++;
                sumMap.put("1次", temp);
            } else if (n == 2) {
                Integer temp = sumMap.getOrDefault("2次", 0);
                temp++;
                sumMap.put("2次", temp);
            } else if (n == 3) {
                Integer temp = sumMap.getOrDefault("3次", 0);
                temp++;
                sumMap.put("3次", temp);
            } else if (n == 4) {
                Integer temp = sumMap.getOrDefault("4次", 0);
                temp++;
                sumMap.put("4次", temp);
            } else if (n == 5) {
                Integer temp = sumMap.getOrDefault("5次", 0);
                temp++;
                sumMap.put("5次", temp);
            } else if (n == 6) {
                Integer temp = sumMap.getOrDefault("5次以上", 0);
                temp++;
                sumMap.put("5次以上", temp);
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        List<Map<String, Object>> rows2 = new ArrayList<>();
        for (Map.Entry<String, Integer> en : countMap.entrySet()) {

            Map<String, Object> map = new HashMap<>();
            String key = en.getKey();

            Integer value = en.getValue();
            map.put("次数", key);
            map.put("报名人数", value);
            rows.add(map);
        }
        for (Map.Entry<String, Integer> en : sumMap.entrySet()) {

            Map<String, Object> map = new HashMap<>();
            String key = en.getKey();

            Integer value = en.getValue();
            map.put("次数", key);
            map.put("参加人数", value);
            rows2.add(map);
        }

        Collections.sort(rows, (o1, o2) -> o1.get("次数").toString().compareTo(o1.get("次数").toString()));
        Collections.sort(rows2, (o1, o2) -> o1.get("次数").toString().compareTo(o1.get("次数").toString()));

        cMap_child1.put("rows", rows);
        cMap_child2.put("rows", rows2);


        cMap.put("chartData", cMap_child1);
        cMap.put("chartData02", cMap_child2);

        return cMap;
    }


}
