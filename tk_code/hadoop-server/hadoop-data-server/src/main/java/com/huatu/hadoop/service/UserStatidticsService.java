package com.huatu.hadoop.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huatu.hadoop.controller.UserStatidticsController;
import com.huatu.hadoop.util.CalendarUtil;
import com.huatu.hadoop.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
@Slf4j
public class UserStatidticsService {

    @Autowired
    private MysqlService mysqlService;

    private final static SimpleDateFormat DATE_FORMAT_FROM = new SimpleDateFormat("yyyyMMdd");
    private final static SimpleDateFormat DATE_FORMAT_TO = new SimpleDateFormat("yyyy年MM月dd日");

    private final static SimpleDateFormat WEEK_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 第w周");

    private final static SimpleDateFormat MONTH_FORMAT_FROM = new SimpleDateFormat("yyyyMM");
    private final static SimpleDateFormat MONTH_FORMAT_TO = new SimpleDateFormat("yyyy年MM月");

    private final static SimpleDateFormat HOUR_FORMAR_FROM = new SimpleDateFormat("yyyyMMddHH");
    private final static SimpleDateFormat HOUR_FORMAR_TO = new SimpleDateFormat("yyyy年MM月dd日HH时");


    private static final String body_temp = "{" +
            "\"sql\":\"%s \"," +
            "\"offset\":0," +
            "\"limit\":50000," +
            "\"acceptPartial\":false," +
            "\"project\":\"%s\"" +
            "}";


    public Object getActive(String quert_start_time,
                            String quert_end_time,
                            Integer group_type_int,
                            String group_type,
                            String kylin_project,
                            Integer project,
                            String phone
    ) throws ParseException {

        Integer uid = mysqlService.getUidByPhone(phone);
        String uname = mysqlService.getUnameByPhone(phone);

        long parse_start = DATE_FORMAT_FROM.parse(quert_start_time).getTime();
        long parse_end = DATE_FORMAT_FROM.parse(quert_end_time).getTime();

        List<Map<String, Object>> timesMap = new ArrayList<>();

        Map<String, Object> cMap = new HashMap<>();

        List<String> arr = new ArrayList<>();
        arr.add("日期");
        arr.add("活跃人数");
        cMap.put("columns", arr);

        List<Map<String, Object>> hha = createTimesMap(quert_start_time, quert_end_time, group_type, group_type_int, "活跃人数");

        String sql = "";
        switch (project) {
            case 1:
                sql = String.format("SELECT " +
                                " %s " +
                                " FROM " +
                                " V_USER_ANSWER " +
                                "WHERE " +
                                " V_USER_ANSWER.UID = %s " +
                                "AND V_USER_ANSWER.CREATE_TIME_D >= '%s' " +
                                "AND V_USER_ANSWER.CREATE_TIME_D <= '%s' " +
                                "GROUP BY " +
                                " %s " +
                                "ORDER BY  " +
                                "  %s",
                        " count(DISTINCT V_USER_ANSWER.UID) AS _count," + group_type, uid, quert_start_time, quert_end_time, group_type, group_type);


                break;
            case 2:
                sql = String.format("SELECT " +
                                " %s " +
                                " FROM " +
                                " V_USER_VIDEO  " +
                                "WHERE " +
                                " V_USER_VIDEO.UNAME = '%s' " +
                                "AND V_USER_VIDEO.CREATE_TIME_D >= '%s' " +
                                "AND V_USER_VIDEO.CREATE_TIME_D <= '%s' " +
                                "GROUP BY " +
                                " %s " +
                                "ORDER BY  " +
                                "  %s",
                        " count(DISTINCT V_USER_VIDEO.UNAME) AS _count," + group_type, uname, quert_start_time, quert_end_time, group_type, group_type);
                break;
            default:
                break;
        }


        log.info("getAnswerActive 1 created sql : {} ", sql);
        return baseQuery(cMap, sql, group_type_int, kylin_project, arr, hha);
    }
    //http://123.103.86.52/hadoop/v1/hadoop/user/stat/num?quert_start_time=20190522&quert_end_time=20190522&group_type_s=week&project=2&phone=18810987791

    public Object getNum(String quert_start_time,
                         String quert_end_time,
                         Integer group_type_int,
                         String group_type,
                         String kylin_project,
                         Integer project,
                         String phone
    ) throws ParseException {


        Integer uid = mysqlService.getUidByPhone(phone);
        String uname = mysqlService.getUnameByPhone(phone);

        Map<String, Object> cMap = new HashMap<>();

        List<String> arr = new ArrayList<>();

        List<Map<String, Object>> hha = null;

        String sql = "";
        switch (project) {
            case 1:
                arr.add("日期");
                arr.add("做题数");
                cMap.put("columns", arr);
                sql = String.format("SELECT " +
                                " %s " +
                                " FROM " +
                                " V_USER_ANSWER " +
                                "WHERE " +
                                "  V_USER_ANSWER.UID = %s " +
                                "AND V_USER_ANSWER.CREATE_TIME_D >= '%s' " +
                                "AND V_USER_ANSWER.CREATE_TIME_D <= '%s' " +
                                "GROUP BY " +
                                " %s " +
                                "ORDER BY  " +
                                "  %s ",
                        " sum(V_USER_ANSWER.QUESTION_NUM) AS _sum," + group_type, uid, quert_start_time, quert_end_time, group_type, group_type);
                hha = createTimesMap(quert_start_time, quert_end_time, group_type, group_type_int, "做题数");
                break;
            case 2:
                arr.add("日期");
                arr.add("听课时长");
                cMap.put("columns", arr);
                sql = String.format("SELECT  " +
                                "  %s" +
                                " FROM  " +
                                "  V_USER_VIDEO    " +
                                "WHERE  " +
                                " V_USER_VIDEO.UNAME = '%s' " +
                                "AND V_USER_VIDEO.CREATE_TIME_D  >= '%s'  " +
                                "AND V_USER_VIDEO.CREATE_TIME_D  <= '%s'  " +
                                "GROUP BY  " +
                                "  %s " +
                                "ORDER BY  " +
                                "  %s ",
                        " sum(V_USER_VIDEO.PLAYTIME) AS _count," + group_type, uname, quert_start_time, quert_end_time, group_type, group_type);
                hha = createTimesMap(quert_start_time, quert_end_time, group_type, group_type_int, "听课时长");
                break;
            default:
                break;
        }

        log.info("getNum 1 created sql : {} ", sql);
        return baseQuery(cMap, sql, group_type_int, kylin_project, arr, hha);
    }

    private static Object baseQuery(Map<String, Object> cMap,
                                    String sql,
                                    Integer group_type_int,
                                    String kylin_project,
                                    List<String> arr,
                                    List<Map<String, Object>> hha) throws ParseException {

        String results = HttpUtil.query(
                String.format(body_temp, sql, kylin_project));

        log.info("baseQuery 1 created sql : {} ", sql);

        List<Map<String, Object>> rows = new ArrayList<>();
        JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
        for (int i = 0; i < ja.size(); i++) {
            Map<String, Object> result = new HashMap<>();
            JSONArray jo = ja.getJSONArray(i);

            String _count = jo.getString(0) == null ? "" : jo.getString(0);
            String _time = jo.getString(1) == null ? "" : jo.getString(1);

            switch (group_type_int) {
                case 1:
                    _time = MONTH_FORMAT_TO.format(MONTH_FORMAT_FROM.parse(_time));
                    break;
                case 2:
                    _time = WEEK_FORMAT.format(new Date(Long.parseLong(_time)));
                    break;
                case 3:
                    _time = DATE_FORMAT_TO.format(DATE_FORMAT_FROM.parse(_time));
                    break;
                case 4:
                    _time = HOUR_FORMAR_TO.format(HOUR_FORMAR_FROM.parse(_time));
                    break;
                default:
                    break;
            }

            for (int j = 0; j < hha.size(); j++) {
                Map<String, Object> map = hha.get(j);
                String time = map.get(arr.get(0)).toString();
                if (time.equals(_time)) {
                    map.put(arr.get(1), _count);
                }
            }

//            result.put(arr.get(1), _count);
//            result.put(arr.get(0), _time);
//            rows.add(result);
        }
        cMap.put("rows", hha);
        return cMap;
    }

    private static List<Map<String, Object>> createTimesMap(String quert_start_time,
                                                           String quert_end_time,
                                                           String group_tyoe_s,
                                                           int group_type_int,
                                                           String k2) throws ParseException {
        int[] i = {1};
        int l = 0;
        if (group_type_int == 4) {
            l = ((int) CalendarUtil.countTwoDate(quert_start_time, quert_end_time) + 1) * 24;
        } else if (group_type_int == 3) {
            i[0] = 24;
            l = (int) CalendarUtil.countTwoDate(quert_start_time, quert_end_time) + 1;
        } else if (group_type_int == 2) {
            i[0] = 7 * 24;
            l = (int) CalendarUtil.countTwoDayWeek(quert_start_time, quert_end_time) + 1;
        } else if (group_type_int == 1) {
            i[0] = 32 * 24;
            l = CalendarUtil.getMonthDiff(quert_start_time, quert_end_time) + 1;
        }

        long parse_start = DATE_FORMAT_FROM.parse(quert_start_time).getTime();

        List<Map<String, Object>> timesMap = new ArrayList<>(l);

        for (int j = 0; j < l; j++) {
            Map<String, Object> stringObjectMap = new HashMap<>();
            stringObjectMap.put(k2, 0);

            String format = "";
            long date = parse_start + j * (i[0] * 60 * 60 * 1000L);
            switch (group_type_int) {
                case 1:
                    format = MONTH_FORMAT_TO.format(new Date(date));
                    break;
                case 2:
                    String format1 = DATE_FORMAT_FROM.format(new Date(date));
                    String weekStart = CalendarUtil.getWeekStart(format1);
                    format = WEEK_FORMAT.format(new Date(Long.parseLong(weekStart)));
                    break;
                case 3:
                    format = DATE_FORMAT_TO.format(new Date(date));
                    break;
                case 4:
                    format = HOUR_FORMAR_TO.format(new Date(date));
                    break;
                default:
                    break;
            }
            stringObjectMap.put("日期", format);
            timesMap.add(stringObjectMap);
        }
        return timesMap;
    }

    public static void main(String[] args) throws ParseException {

        String quert_start_time = "20190521";
        String quert_end_time = "20190701";
        String group_tyoe_s = "hour";

        int group_type_int = 4;


        System.out.println(createTimesMap(quert_start_time, quert_end_time, group_tyoe_s, group_type_int, "hah"));

    }


}


