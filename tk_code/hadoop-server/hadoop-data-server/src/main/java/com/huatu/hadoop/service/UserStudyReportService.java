package com.huatu.hadoop.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.huatu.hadoop.util.CalendarUtil;
import com.huatu.hadoop.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserStudyReportService {

    private static String kylin_project = "user_study";

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyw");

    private static SimpleDateFormat f1 = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat $f1 = new SimpleDateFormat("yyyy年MM月dd日");

    //    private static SimpleDateFormat f2 = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat $f2 = new SimpleDateFormat("yyyy年MM月dd日 第w周");

    private static SimpleDateFormat f3 = new SimpleDateFormat("yyyyMM");
    private static SimpleDateFormat $f3 = new SimpleDateFormat("yyyy年MM月");

    private static SimpleDateFormat f4 = new SimpleDateFormat("yyyyMMdd:HH");
    private static SimpleDateFormat $f4 = new SimpleDateFormat("yyyy年MM月dd日HH时");

    static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();
    static ThreadPoolExecutor pool = new ThreadPoolExecutor(5,
            200,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024),
            namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    @Autowired
    private RestTemplate restTemplate;

    @Resource(name = "getPrestoStmt")
    private Statement prestoStmt;
    @Resource(name = "getPrestoStmt2")
    private Statement prestoStmt2;
    @Resource(name = "getPrestoStmt3")
    private Statement prestoStmt3;

    @Autowired
    private MysqlService mysqlService;

    public Object studyActiveUser(
            String startRecordTime,
            String endRecordTime,
            String groupType,
            String terminal
    ) {
        PrestoQuery pq = new PrestoQuery();

        ConcurrentHashMap<String, Object> cMap = new ConcurrentHashMap<>();

        String[] arr = {"日期", "课程学习用户数"};
        cMap.put("columns", arr);

        BlockingQueue<Integer> queue1 = new ArrayBlockingQueue<>(1);
        BlockingQueue<Integer> queue2 = new ArrayBlockingQueue<>(1);

        try {

            String startWeek = CalendarUtil.getWeekStart(startRecordTime);
            String endWeek = CalendarUtil.getWeekend(endRecordTime);

            pool.execute(() -> {

                ConcurrentLinkedDeque<Object> rows = new ConcurrentLinkedDeque<>();
                try {

                    String sql =
                            String.format(" select count(distinct uname),%s from videoplay2 where  %s  terminal = %s group by %s order by %s",
                                    groupType,
                                    pq.timeSelecto3r(startWeek, endWeek, startRecordTime, endRecordTime),
                                    terminal,
                                    groupType,
                                    groupType);

                    log.info("studyActiveUser 1 created sql : {} ", sql);

                    String body = "{" +
                            "\"sql\":\"" + sql + "\"," +
                            "\"offset\":0," +
                            "\"limit\":50000," +
                            "\"acceptPartial\":false," +
                            "\"project\":\"" + kylin_project + "\"" +
                            "}";
                    String results = HttpUtil.query(
                            body);
                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
                    for (int i = 0; i < ja.size(); i++) {

                        Map<String, Object> inMap = new HashMap<>();
                        JSONArray jsonArray = ja.getJSONArray(i);

                        inMap.put("课程学习用户数", jsonArray.getInteger(0));

                        String string = jsonArray.getString(1);
                        try {
                            if (!jsonArray.getString(2).isEmpty()) {
                                string = string + ":" + jsonArray.getString(2);
                            }
                        } catch (Exception e) {
                            int k = 0;
                        }

                        try {

                            if (groupType.equals("playday,playhour")) {
                                inMap.put("日期", $f4.format(f4.parse(string)));
                            } else if (groupType.equals("playday")) {
                                inMap.put("日期", $f1.format(f1.parse(string)));
                            } else if (groupType.equals("playweek")) {
                                inMap.put("日期", $f2.format(new Date(Long.parseLong(string))));
                            } else if (groupType.equals("playmonth")) {
                                inMap.put("日期", $f3.format(f3.parse(string)));
                            }

                        } catch (Exception e) {
                            continue;
                        }
                        rows.add(inMap);
                    }
                    cMap.put("rows", rows);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    queue1.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            pool.execute(() -> {
                try {

                    String sql2 =
                            String.format(" select count(distinct uname) from videoplay2 where %s terminal = %s",
                                    pq.timeSelecto3r(startWeek, endWeek, startRecordTime, endRecordTime),
                                    terminal);
                    log.info("studyActiveUser 2 created sql : {} ", sql2);

                    String body = "{" +
                            "\"sql\":\"" + sql2 + "\"," +
                            "\"offset\":0," +
                            "\"limit\":50000," +
                            "\"acceptPartial\":false," +
                            "\"project\":\"" + kylin_project + "\"" +
                            "}";
                    String results = HttpUtil.query(
                            body);
                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");

                    for (int i = 0; i < ja.size(); i++) {
                        cMap.put("total", ja.getJSONArray(i).getString(0));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    queue2.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            });

            queue1.take();
            queue2.take();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return cMap;
    }


    public Object studyReportLength(
            String startRecordTime,
            String endRecordTime,
            String groupType,
            String terminal
    ) {


//        BlockingQueue<Integer> queue1 = new ArrayBlockingQueue<Integer>(1);
//        BlockingQueue<Integer> queue2 = new ArrayBlockingQueue<Integer>(1);

        Map<String, Map<String, Object>> cache = new HashMap<>();

        ConcurrentHashMap<String, Object> m = new ConcurrentHashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        List<Map<String, Object>> rows2 = new ArrayList<>();

        Map<String, Long> result = new HashMap<>();

        String[] arr = {"日期", "0-15m",
                "15-30m",
                "30-60m",
                "60-90m",
                "90-120m",
                "120-150m",
                "150-180m",
                "180m以上",
        };
        m.put("columns", arr);

        try {
            String startWeek = CalendarUtil.getWeekStart(startRecordTime);
            String endWeek = CalendarUtil.getWeekend(endRecordTime);

            try {

                String sql2 =
                        String.format(" select uname,sum(playtime),%s from videoplay2 where  playday >= '%s' and playday<='%s'  and terminal = %s group by uname, %s",
                                groupType,
                                startRecordTime,
                                endRecordTime,
                                terminal,
                                groupType);

                log.info("studyReportLength 1 created sql : {} ", sql2);

                String body = "{" +
                        "\"sql\":\"" + sql2 + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";
                String results = HttpUtil.query(
                        body);

                JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");

                String key = "0-15m";
                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);


                    String string = jsonArray.getString(2);
                    try {
                        if (!jsonArray.getString(3).isEmpty()) {
                            string = string + ":" + jsonArray.getString(3);
                        }
                    } catch (Exception e) {
                        int k = 0;
                    }


                    String s = "";
                    if (groupType.equals("playday,playhour")) {
                        s = $f4.format(f4.parse(string));
                    } else if (groupType.equals("playday")) {
                        s = $f1.format(f1.parse(string));
                    } else if (groupType.equals("playweek")) {
                        s = $f2.format(new Date(Long.parseLong(string)));
                    } else if (groupType.equals("playmonth")) {
                        s = $f3.format(f3.parse(string));
                    }
                    Long sUser = result.getOrDefault(s + "-user", 0L);
                    Long sTime = result.getOrDefault(s + "-time", 0L);


                    Integer t = jsonArray.getInteger(1);

                    int userSumLength = t / 60;

                    if (userSumLength > 15 && userSumLength <= 30) {
                        key = "15-30m";

                    } else if (userSumLength > 0 && userSumLength <= 15) {
                        key = "0-15m";
                    } else if (userSumLength > 30 && userSumLength <= 60) {
                        key = "30-60m";
                    } else if (userSumLength > 60 && userSumLength <= 90) {
                        key = "60-90m";
                    } else if (userSumLength > 90 && userSumLength <= 120) {
                        key = "90-120m";
                    } else if (userSumLength > 120 && userSumLength <= 150) {
                        key = "120-150m";
                    } else if (userSumLength > 150 && userSumLength <= 180) {
                        key = "150-180m";
                    } else if (userSumLength > 180) {
                        key = "180m以上";
                    }

                    sUser++;
                    sTime += t;

                    long count = result.getOrDefault(s + "=" + key, 0L);
                    count++;
                    result.put(s + "=" + key, count);

                    result.put(s + "-user", sUser);
                    result.put(s + "-time", sTime);
                }

                for (Map.Entry<String, Long> en : result.entrySet()) {

                    String key1 = en.getKey();

                    if (key1.contains("=")) {
                        String[] s = key1.split("=");
                        try {
                            long i = en.getValue();

                            Map<String, Object> inMap = new HashMap<>();
                            inMap.put("日期", s[0]);
                            inMap.put(s[1], i);
                            rows.add(inMap);
                        } catch (Exception e) {
                            result.put(key1, en.getValue());
                        }
                    } else {
                        String[] s = key1.split("-");
                        Map<String, Object> map = cache.getOrDefault(s[0], new HashMap<>());

                        if (s[1].equals("user")) {
                            map.put("学习人数", en.getValue());
                        } else {
                            map.put("学习时长", en.getValue());
                        }
                        cache.put(s[0], map);
//                            rows2.add(map);

                    }

                }

                Collections.sort(rows, (o1, o2) -> {

                    String dt1 = o1.get("日期").toString();
                    String dt2 = o2.get("日期").toString();
                    return dt1.compareTo(dt2);
                });


            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                String sql2 =
                        String.format(" select count(distinct  uname),sum(playtime) from videoplay2 where  playday >= '%s' and playday<='%s' and terminal = %s",
                                startRecordTime,
                                endRecordTime,
                                terminal);
                log.info("studyReportLength 2 created sql : {} ", sql2);
                String body = "{" +
                        "\"sql\":\"" + sql2 + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                String results = HttpUtil.query(
                        body);

                JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
                ConcurrentHashMap<String, Object> inMap = new ConcurrentHashMap<>();
                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);

                    inMap.put("total", jsonArray.getInteger(1));
                    inMap.put("ave", jsonArray.getInteger(1) / jsonArray.getInteger(0));
                }

                m.put("datagrid", inMap);
            } catch (Exception e) {
                e.printStackTrace();
            }

//            queue1.take();
//            queue2.take();

            String uniqueTime = "";
            List<Integer> li = new ArrayList<>();
            List<String> li2 = new ArrayList<>();

            int i = 0;
            for (Map<String, Object> l : rows) {

                String time = l.get("日期").toString();
                if (i == 0) {
                    li2.add(time);
                    uniqueTime = time;
                    li.add(0);
                }
                if (!time.equals(uniqueTime)) {
                    li.add(i - 1);
                    li.add(i);
                    li2.add(time);
                    uniqueTime = time;
                }
                if (i == rows.size() - 1) {
                    li.add(i);
                }
                i++;
            }

            List<Map<String, Object>> r = new ArrayList<>();
            for (int j = 0; j < li.size(); j = j + 2) {

                Map<String, Object> tmp = new HashMap<>();
                String date = li2.get(j / 2);

                tmp.put("日期", date);

                List<Map<String, Object>> maps = rows.subList(li.get(j), li.get(j + 1) + 1);
                for (Map<String, Object> a : maps) {
                    Set<String> keySet = a.keySet();

                    String s = keySet.stream().filter(s1 -> !s1.equals("日期")).collect(Collectors.toList()).get(0);

                    Object o = a.get(s);
                    tmp.put(s, o);
                }
                Map<String, Object> map = cache.get(date);
                long tim = Long.parseLong(map.getOrDefault("学习时长", "0").toString());
                int stu = Integer.parseInt(map.getOrDefault("学习人数", "0").toString());
                tmp.put("学习时长", tim);
                tmp.put("平均学习时长", (double) tim / stu);
                r.add(tmp);
            }


            m.put("rows", r);
//            m.put("rows2", cache);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return m;
    }

    public Object fixData() {

        Map<String, Double> result = new HashMap<>();
        try {
            BlockingQueue<Integer> queue1 = new ArrayBlockingQueue<Integer>(1);
            BlockingQueue<Integer> queue2 = new ArrayBlockingQueue<Integer>(1);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");


            String yesteday = sdf.format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L));
            String huanbi = sdf.format(new Date(System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L + 1L)));
            String tongbi = sdf.format(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L));

            String endWeek = CalendarUtil.getWeekend(yesteday);
            String startWeek = CalendarUtil.getWeekStart(tongbi);


            int[] activeData = new int[3];

            pool.execute(() -> {
                try {
                    String active_sql =
                            String.format("select count(distinct uname) as ucount,playday from videoplay2 where  playday in('%s','%s','%s') group by playday order by playday desc",
                                    yesteday,
                                    huanbi,
                                    tongbi);

                    String body = "{" +
                            "\"sql\":\"" + active_sql + "\"," +
                            "\"offset\":0," +
                            "\"limit\":50000," +
                            "\"acceptPartial\":false," +
                            "\"project\":\"" + kylin_project + "\"" +
                            "}";
                    log.info("fixData created 1 sql {} ", active_sql);
                    String results = HttpUtil.query(
                            body);

                    JSONArray active = JSONObject.parseObject(results).getJSONArray("results");
                    int j = 0;
                    for (int i = 0; i < active.size(); i++) {
                        JSONArray jsonArray = active.getJSONArray(i);
                        activeData[j] = jsonArray.getInteger(0);
                        j++;
                    }

                    Double v = (activeData[0] - activeData[1]) * 1.0 / activeData[1];
                    Double v1 = (activeData[0] - activeData[2]) * 1.0 / activeData[2];
                    result.put("activeData_huanbi", v.isNaN() ? 0.00 : v);
                    result.put("activeData_tongbi", v1.isNaN() ? 0.00 : v1);

                    result.put("activeData", activeData[0] * 1.0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    queue1.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            int[] lengthData = new int[3];
            pool.execute(() -> {

                try {
                    String length_sql =
                            String.format("select sum(playtime) as sumlength,playday from videoplay2 where  playday in('%s','%s','%s') group by playday order by playday desc",
                                    yesteday,
                                    huanbi,
                                    tongbi);

                    log.info("fixData created 2 sql {} ", length_sql);
                    String body = "{" +
                            "\"sql\":\"" + length_sql + "\"," +
                            "\"offset\":0," +
                            "\"limit\":50000," +
                            "\"acceptPartial\":false," +
                            "\"project\":\"" + kylin_project + "\"" +
                            "}";
                    String results = HttpUtil.query(
                            body);

                    JSONArray active = JSONObject.parseObject(results).getJSONArray("results");
                    int j = 0;
                    for (int i = 0; i < active.size(); i++) {
                        JSONArray jsonArray = active.getJSONArray(i);
                        lengthData[j] = jsonArray.getInteger(0);
                        j++;
                    }

                    Double v = (lengthData[0] - lengthData[1]) * 1.0 / lengthData[1];
                    Double v1 = (lengthData[0] - lengthData[2]) * 1.0 / lengthData[2];
                    result.put("lengthData_huanbi", v.isNaN() ? 0.00 : v);
                    result.put("lengthData_tongbi", v1.isNaN() ? 0.00 : v1);
                    result.put("lengthData", lengthData[0] * 1.0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    queue2.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            queue1.take();
            queue2.take();

            Double ave_length = lengthData[0] * 1.0 / activeData[0];
            double ave_length_huanbi = lengthData[1] * 1.0 / activeData[1];
            double ave_length_tongbi = lengthData[2] * 1.0 / activeData[2];

            Double v = (ave_length - ave_length_huanbi) / ave_length_huanbi;
            Double v1 = (ave_length - ave_length_tongbi) / ave_length_tongbi;
            result.put("ave_length", ave_length.isNaN() ? 0.00 : ave_length);
            result.put("ave_length_huanbi", v.isNaN() ? 0.00 : v);
            result.put("ave_length_tongbi", v1.isNaN() ? 0.00 : v1);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }

    public Object netStudy(
            String startPlayDaY,
            String endPlayDaY,
            String groupType,
            String id,
            String idtype
    ) {
        PrestoQuery pq = new PrestoQuery();

        BlockingQueue<Integer> queue1 = new ArrayBlockingQueue<>(1);
        BlockingQueue<Integer> queue2 = new ArrayBlockingQueue<>(1);

        List<String> dateList = new ArrayList<>();

        ConcurrentHashMap<String, Object> m = new ConcurrentHashMap<>();
        try {

            String startWeek = CalendarUtil.getWeekStart(startPlayDaY);
            String endWeek = CalendarUtil.getWeekend(endPlayDaY);

            pool.execute(() -> {

                try {
                    String weekStart = CalendarUtil.getWeekStart(startPlayDaY);

                    int dSIze = dateList.size();
                    String sql =
                            String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser,%s from videoplay2 where  %s group by %s order by %s",
                                    groupType,
                                    pq.timeSelector2(startWeek, endWeek, startPlayDaY, endPlayDaY, idtype, id),
                                    groupType,
                                    groupType);

                    log.info("netStudy created 1 sql : {} ", sql);

                    String body = "{" +
                            "\"sql\":\"" + sql + "\"," +
                            "\"offset\":0," +
                            "\"limit\":50000," +
                            "\"acceptPartial\":false," +
                            "\"project\":\"" + "user_study" + "\"" +
                            "}";
                    String results = HttpUtil.query(
                            body);

                    Map<String, Object> cMap = new HashMap<>();

                    List<Map<String, Object>> rows = new ArrayList<>();
                    String[] arr = {"日期", "学习人数", "学习时长", "平均时长"};
                    m.put("columns", arr);

                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
                    for (int i = 0; i < ja.size(); i++) {

                        Map<String, Object> inMap = new HashMap<>();
                        JSONArray jsonArray = ja.getJSONArray(i);

                        inMap.put("学习时长", jsonArray.getInteger(0));
                        inMap.put("学习人数", jsonArray.getInteger(1));
                        inMap.put("平均时长", jsonArray.getInteger(0) / jsonArray.getInteger(1));


                        String string = jsonArray.getString(2);
                        try {
                            if (!jsonArray.getString(3).isEmpty()) {
                                string = string + ":" + jsonArray.getString(3);
                            }
                        } catch (Exception e) {
                            int k = 0;
                        }

                        if (groupType.equals("playday,playhour")) {
                            inMap.put("日期", $f4.format(f4.parse(string)));
                        } else if (groupType.equals("playday")) {
                            inMap.put("日期", $f1.format(f1.parse(string)));
                        } else if (groupType.equals("playweek")) {
                            inMap.put("日期", $f2.format(new Date(Long.parseLong(string))));
                        } else if (groupType.equals("playmonth")) {
                            inMap.put("日期", $f3.format(f3.parse(string)));
                        }

                        rows.add(inMap);
                    }

//                    cMap.put("rows", rows);
                    m.put("rows", rows);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    queue1.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            pool.execute(() -> {

                try {
                    String sql =
                            String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser from videoplay2 where %s",
                                    pq.timeSelector2(startWeek, endWeek, startPlayDaY, endPlayDaY, idtype, id));

                    log.info("netStudy created 2 sql : {} ", sql);

                    String body = "{" +
                            "\"sql\":\"" + sql + "\"," +
                            "\"offset\":0," +
                            "\"limit\":50000," +
                            "\"acceptPartial\":false," +
                            "\"project\":\"" + "user_study" + "\"" +
                            "}";

                    String results = HttpUtil.query(
                            body);
                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");

                    ConcurrentHashMap<String, Object> cMap = new ConcurrentHashMap<>();

                    for (int i = 0; i < ja.size(); i++) {

                        JSONArray jsonArray = ja.getJSONArray(i);

                        cMap.put("playTime", jsonArray.getLong(0));
                        cMap.put("activeUser", jsonArray.getLong(1));
                        cMap.put("ave", jsonArray.getLong(0) / jsonArray.getLong(1));

                    }
                    m.put("netStudydatagrid", cMap);


                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    queue2.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            });

            queue1.take();
            queue2.take();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return m;
    }

    public Object netStudydatagrid(
            String startPlayDaY,
            String endPlayDay,
            String groupType,
            String id,
            String idtype,
            int limitSize,
            long offset,
            int isend
    ) {
        PrestoQuery pq = new PrestoQuery();


        ConcurrentHashMap<String, Object> m = new ConcurrentHashMap<>();
        String[] arr = {"日期", "课程ID",
                "课程标题",
                "学习人数",
                "学习时长",
                "平均学习时长",
                "操作"
        };
        m.put("columns", arr);
        try {

            String weekStart = CalendarUtil.getWeekStart(startPlayDaY);
            //yyyyMMdd
            long startTimestamp = f1.parse(startPlayDaY).getTime();
            long endTimestamp = f1.parse(endPlayDay).getTime();
            long d = 0L;

            String startWeek = weekStart;
            String endWeek = CalendarUtil.getWeekend(endPlayDay);

            String t = "";

            Map<String, Object> inMap = new HashMap<>();
            Map<String, Object> inMap2 = new HashMap<>();


            String n = "title";
            if (idtype.equals("netclassid")) {
                n = "title";
            } else if (idtype.equals("classid")) {
                n = "classname";
            }

            try {
                String sql =
                        String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser,%s from videoplay2 where %s  group by %s ",
                                idtype + "," + n,
                                pq.timeSelector2(startWeek, endWeek, startPlayDaY, endPlayDay, idtype, id),
                                idtype + "," + n);

                log.info("netStudydatagrid created 合计 sql : {} ", sql);

                String body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                String results = HttpUtil.query(
                        body);
                JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);

                    String title = jsonArray.getString(3);

                    inMap.put("length", jsonArray.getLong(0));
                    inMap.put("num", jsonArray.getLong(1));
                    inMap.put("ave", jsonArray.getLong(0) / jsonArray.getLong(1));

                    inMap.put("id", jsonArray.getString(2));
                    inMap.put("title", title);
                    inMap.put("time", "合计");
                    inMap.put("see", "查看详情");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (idtype.equals("netclassid")) {
                    n = "title";
                } else if (idtype.equals("classid")) {
                    n = "classname";
                }
                String sql =
                        String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser,%s from videoplay2 where %s  group by %s ",
                                idtype + "," + n,
                                pq.timeSelector2(startWeek, endWeek, "19700101", endPlayDay, idtype, id),
                                idtype + "," + n);
                log.info("netStudydatagrid created 截止当前 sql : {} ", sql);

                String body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                String results = HttpUtil.query(
                        body);
                JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);

                    String title = jsonArray.getString(3);

                    inMap2.put("length", jsonArray.getLong(0));
                    inMap2.put("num", jsonArray.getLong(1));
                    inMap2.put("ave", jsonArray.getLong(0) / jsonArray.getLong(1));

                    t = title;
                    inMap2.put("id", jsonArray.getString(2));
                    inMap2.put("title", title);
                    inMap2.put("time", "截止当前");
                    inMap2.put("see", "查看详情");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            List<Map<String, Object>> l2 = new ArrayList<>();
            l2.add(inMap);
            l2.add(inMap2);


            try {

                String gt = groupType;
                if (idtype.equals("netclassid")) {
                    n = "title";
                } else if (idtype.equals("classid")) {

                    n = "classname";
                }

                String sql =
                        String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser,%s from videoplay2 where %s group by %s order by %s",
                                idtype + "," + n + "," + gt,
                                pq.timeSelector2(startWeek, endWeek, startPlayDaY, endPlayDay, idtype, id),
                                groupType + "," + idtype + "," + n,groupType);

                log.info("netStudydatagrid created 1 sql : {} ", sql);

                String body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                String results = HttpUtil.query(
                        body);

                List<Map<String, Object>> rows = new ArrayList<>();

                JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");

                for (int i = 0; i < ja.size(); i++) {

                    Map<String, Object> im = new HashMap<>();
                    JSONArray jsonArray = ja.getJSONArray(i);

                    String title = jsonArray.getString(3);

                    im.put("length", jsonArray.getInteger(0));
                    im.put("num", jsonArray.getInteger(1));
                    im.put("ave", jsonArray.getInteger(0) / jsonArray.getInteger(1));
                    im.put("id", jsonArray.getString(2));
                    im.put("title", title);
                    im.put("see", "查看详情");

                    String string = jsonArray.getString(4);
                    try {
                        if (!jsonArray.getString(5).isEmpty()) {
                            string = string + ":" + jsonArray.getString(5);
                        }
                    } catch (Exception e) {
                        int k = 0;
                    }

                    if (groupType.equals("playday,playhour")) {
                        im.put("time", $f4.format(f4.parse(string)));
                    } else if (groupType.equals("playday")) {
                        im.put("time", $f1.format(f1.parse(string)));
                    } else if (groupType.equals("playweek")) {
                        im.put("time", $f2.format(new Date(Long.parseLong(string))));
                    } else if (groupType.equals("playmonth")) {
                        im.put("time", $f3.format(f3.parse(string)));

                    }
                    rows.add(im);
                }

                m.put("count", rows.size());
                int i = (int) (offset + limitSize);

                List<Map<String, Object>> r = rows.subList((int) offset, i > rows.size() ? rows.size() : i);
                m.put("rows", r);

            } catch (Exception e) {
                e.printStackTrace();
            }


            m.put("row2", l2);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return m;
    }


    public Object datagridBySyllabus(
            String start,
            String end,
            String groupType,
            String courseid,
            Integer parentId,
            int limitSize,
            long offset
    ) {
        PrestoQuery pq = new PrestoQuery();

        List<String> dateList = new ArrayList<>();

        final String[] n = {""};

        ConcurrentHashMap<String, Object> m = new ConcurrentHashMap<>();
        String[] arr = {"日期", "课时ID",
                "课时名称",
                "学习人数",
                "学习时长",
                "平均时长",
                "操作"
        };
        String[] arr1 = {"日期",
                "学习人数",
                "学习时长",
                "平均时长"
        };
        m.put("columns", arr);

        m.put("columns2", arr1);
        try {

            String[] t = {""};
            BlockingQueue<Integer> hejieQueue = new ArrayBlockingQueue<>(1);
            BlockingQueue<Integer> jiezhiQueue = new ArrayBlockingQueue<>(1);
            BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(1);
            BlockingQueue<Integer> queue2 = new ArrayBlockingQueue<>(1);
            BlockingQueue<Integer> totalQueue = new ArrayBlockingQueue<>(1);

            String startWeek = CalendarUtil.getWeekStart(start);
            String endWeek = CalendarUtil.getWeekend(end);

            Map<String, Object> heJiMap = new HashMap<>();
            Map<String, Object> jieZhiMap = new HashMap<>();

            pool.execute(() -> {

                try {
                    String sql =
                            String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser,courseid,syname from videoplay2 where %s courseid = %s group by courseid,syname ",
                                    pq.timeSelectorp(startWeek, endWeek, start, end, parentId),
                                    courseid);

                    log.info("datagridBySyllabus created 合计 sql : {} ", sql);

                    String body = String.format("{\"sql\":\"%s\",\"offset\":0,\"limit\":50000,\"acceptPartial\":false,\"project\":\"user_study\"}", sql);

                    String results = HttpUtil.query(
                            body);
                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");

                    for (int i = 0; i < ja.size(); i++) {

                        JSONArray jsonArray = ja.getJSONArray(i);

                        String title = jsonArray.getString(3);
                        if (title == null) {
                            title = "";
                        }
                        heJiMap.put("length", jsonArray.getLong(0));
                        heJiMap.put("num", jsonArray.getLong(1));
                        heJiMap.put("ave", jsonArray.getLong(0) / jsonArray.getLong(1));
                        heJiMap.put("title", title);
                        heJiMap.put("time", "合计");


                        heJiMap.put("id", jsonArray.getString(2));
                        heJiMap.put("see", "查看详情");
                    }

                    if (ja.size() == 0) {

                        heJiMap.put("length", 0);
                        heJiMap.put("num", 0);
                        heJiMap.put("ave", 0);
                        heJiMap.put("title", n[0]);
                        heJiMap.put("time", "合计");
                        heJiMap.put("id", courseid);
                        heJiMap.put("see", "查看详情");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    hejieQueue.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            pool.execute(() -> {

                try {


                    String sql = String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser,courseid,syname from videoplay2 where %s courseid = %s and playday<= '%s' group by courseid,syname",
                            pq.timeSelectorp(startWeek, endWeek, "19700101", end, parentId),
                            courseid,
                            end);

                    log.info("datagridBySyllabus created 截止当前 sql : {} ", sql);

                    String body = String.format("{\"sql\":\"%s\",\"offset\":0,\"limit\":50000,\"acceptPartial\":false,\"project\":\"user_study\"}", sql);

                    String results = HttpUtil.query(
                            body);

                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
                    for (int i = 0; i < ja.size(); i++) {

                        JSONArray jsonArray = ja.getJSONArray(i);

                        String title = jsonArray.getString(3);
                        if (title == null) {
                            title = "";
                        }

                        t[0] = title;
                        jieZhiMap.put("length", jsonArray.getLong(0));
                        jieZhiMap.put("num", jsonArray.getLong(1));
                        jieZhiMap.put("ave", jsonArray.getLong(0) / jsonArray.getLong(1));
                        jieZhiMap.put("title", title);
                        jieZhiMap.put("time", "截止当前");

                        jieZhiMap.put("id", jsonArray.getString(2));
                        jieZhiMap.put("see", "查看详情");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    jiezhiQueue.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            pool.execute(() -> {

                try {
                    String sql =
                            String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser,courseid,syname,%s from videoplay2 where %s courseid = %s group by %s ,courseid,syname order  by %s limit %s OFFSET %s",
                                    groupType,
                                    pq.timeSelectorp(startWeek, endWeek, start, end, parentId),
                                    courseid,
                                    groupType,
                                    groupType,
                                    limitSize,
                                    offset);

                    log.info("datagridBySyllabus created 表格 sql : {} ", sql);

                    String body = String.format("{\"sql\":\"%s\",\"offset\":0,\"limit\":50000,\"acceptPartial\":false,\"project\":\"user_study\"}", sql);


                    String results = HttpUtil.query(
                            body);

                    List<Map<String, Object>> rows = new ArrayList<>();

                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");

                    for (int i = 0; i < ja.size(); i++) {

                        JSONArray jsonArray = ja.getJSONArray(i);

                        Map<String, Object> inMap = new HashMap<>();

                        String title = jsonArray.getString(3);
                        if (title == null) {
                            title = "";
                        }

                        inMap.put("length", jsonArray.getInteger(0));
                        inMap.put("num", jsonArray.getInteger(1));
                        inMap.put("ave", jsonArray.getInteger(0) / jsonArray.getInteger(1));

                        inMap.put("id", jsonArray.getString(2));
                        inMap.put("title", title);
                        inMap.put("see", "查看详情");


                        String string = jsonArray.getString(4);
                        try {
                            if (!jsonArray.getString(5).isEmpty()) {
                                string = string + ":" + jsonArray.getString(5);
                            }
                        } catch (Exception e) {
                            int k = 0;
                        }

                        if (groupType.equals("playday,playhour")) {
                            inMap.put("time", $f4.format(f4.parse(string)));
                        } else if (groupType.equals("playday")) {
                            inMap.put("time", $f1.format(f1.parse(string)));
                        } else if (groupType.equals("playweek")) {
                            inMap.put("time", $f2.format(new Date(Long.parseLong(string))));
                        } else if (groupType.equals("playmonth")) {
                            inMap.put("time", $f3.format(f3.parse(string)));
                        }
                        rows.add(inMap);
                    }
                    int size = rows.size();
                    int i = (int) (offset + limitSize);

                    List<Map<String, Object>> r = rows.subList((int) offset, i > size ? size : i);
                    m.put("rows3", r);
                    m.put("count", size);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    queue.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            pool.execute(() -> {

                try {
                    String sql =
                            String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser,courseid,syname,%s from videoplay2 where %s courseid = %s group by %s ,courseid,syname order  by %s ",
                                    groupType,
                                    pq.timeSelectorp(startWeek, endWeek, start, end, parentId),
                                    courseid,
                                    groupType,
                                    groupType);

                    log.info("datagridBySyllabus created 统计图 sql : {} ", sql);

                    String body = String.format("{\"sql\":\"%s\",\"offset\":0,\"limit\":50000,\"acceptPartial\":false,\"project\":\"user_study\"}", sql);


                    String results = HttpUtil.query(
                            body);

                    List<Map<String, Object>> rows = new ArrayList<>();

                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");

                    for (int i = 0; i < ja.size(); i++) {

                        JSONArray jsonArray = ja.getJSONArray(i);

                        Map<String, Object> inMap = new HashMap<>();

                        String title = jsonArray.getString(3);
                        if (title == null) {
                            title = "";
                        }

                        inMap.put("学习时长", jsonArray.getInteger(0));
                        inMap.put("学习人数", jsonArray.getInteger(1));
                        inMap.put("平均学习时长", jsonArray.getInteger(0) / jsonArray.getInteger(1));


                        String string = jsonArray.getString(4);
                        try {
                            if (!jsonArray.getString(5).isEmpty()) {
                                string = string + ":" + jsonArray.getString(5);
                            }
                        } catch (Exception e) {
                            int k = 0;
                        }

                        if (groupType.equals("playday,playhour")) {
                            inMap.put("日期", $f4.format(f4.parse(string)));
                        } else if (groupType.equals("playday")) {
                            inMap.put("日期", $f1.format(f1.parse(string)));
                        } else if (groupType.equals("playweek")) {
                            inMap.put("日期", $f2.format(new Date(Long.parseLong(string))));
                        } else if (groupType.equals("playmonth")) {
                            inMap.put("日期", $f3.format(f3.parse(string)));
                        }
                        rows.add(inMap);
                    }

                    m.put("rows", rows);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    queue2.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });


            pool.execute(() -> {

                try {
                    if (parentId != null) {
                        String sql =
                                String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser from videoplay2 where %s courseid = %s ",
                                        pq.timeSelectorp(startWeek, endWeek, start, end, parentId),
                                        courseid);

                        log.info("datagridBySyllabus created total sql : {} ", sql);

                        String body = String.format("{\"sql\":\"%s\",\"offset\":0,\"limit\":50000,\"acceptPartial\":false,\"project\":\"user_study\"}", sql);

                        String results = HttpUtil.query(
                                body);
                        JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
                        for (int i = 0; i < ja.size(); i++) {

                            JSONArray jsonArray = ja.getJSONArray(i);
                            int playtime = jsonArray.getInteger(0);
                            int activeuser = jsonArray.getInteger(1);
                            Double ave = playtime * 1.0 / activeuser;

                            Map<String, Object> inMap = new HashMap<>();

                            inMap.put("累计学习人数", activeuser);
                            inMap.put("累计学习时长", playtime);
                            inMap.put("平均时长", ave.isNaN() ? "0.00" : ave);

                            m.put("total", inMap);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    totalQueue.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });


            hejieQueue.take();
            jiezhiQueue.take();
            queue.take();
            queue2.take();
            totalQueue.take();

            List<Map<String, Object>> l2 = new ArrayList<>();
            if (heJiMap != null && heJiMap.size() > 0) {
                l2.add(heJiMap);
            }
            if (jieZhiMap != null && jieZhiMap.size() > 0) {
                l2.add(jieZhiMap);
            }


            m.put("row2", l2);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return m;
    }

    public Object findByNetclassId(
            String startRecordTime,
            String endRecordTime,
            String id,
            int isEN,
            int limitSize,
            long offset,
            String querytype,
            String idtype
    ) {

        ConcurrentHashMap<String, Object> m = new ConcurrentHashMap<>();
        String[] arr = {"日期", "课时ID",
                "课时名称",
                "学习人数",
                "学习时长",
                "平均学习时长",
                "查看详情"
        };

        String[] arr1 = {"课时名称",
                "学习人数",
                "学习时长",
                "平均学习时长"
        };

        m.put("columns", arr1);
        if (isEN == 1) {
            m.put("columns", arr);
        }

        try {
            PrestoQuery pq = new PrestoQuery();

            BlockingQueue<Integer> queue1 = new ArrayBlockingQueue<Integer>(1);
            BlockingQueue<Integer> queue2 = new ArrayBlockingQueue<Integer>(1);
            BlockingQueue<Integer> queue3 = new ArrayBlockingQueue<Integer>(1);
            BlockingQueue<Integer> queue4 = new ArrayBlockingQueue<Integer>(1);

            String startWeek = CalendarUtil.getWeekStart(startRecordTime);
            String endWeek = CalendarUtil.getWeekend(endRecordTime);

            pool.execute(() -> {

                try {

                    String sql =
                            String.format("select count(*) from (" +
                                            "select sum(playtime) as playtime,count(distinct uname) as activeuser,courseid,syname from videoplay2 where %s group by courseid,syname " +
                                            ") ",
                                    pq.timeSelector2(startWeek, endWeek, startRecordTime, endRecordTime, idtype, id),
                                    id);

                    log.info("findByNetclassId created count sql : {} ", sql);
                    String body = String.format("{\"sql\":\"%s\",\"offset\":0,\"limit\":50000,\"acceptPartial\":false,\"project\":\"user_study\"}", sql);

                    String results = HttpUtil.query(
                            body);

                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
                    for (int i = 0; i < ja.size(); i++) {
                        JSONArray jsonArray = ja.getJSONArray(i);
                        int count = jsonArray.getInteger(0);
                        m.put("count", count);
                    }

                    sql =
                            String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser,courseid,syname from videoplay2 where %s group by courseid,syname order by courseid limit %s OFFSET %s",
                                    pq.timeSelector2(startWeek, endWeek, startRecordTime, endRecordTime, idtype, id),
                                    limitSize,
                                    offset);


                    log.info("findByNetclassId created 1 sql : {} ", sql);

                    body = String.format("{\"sql\":\"%s\",\"offset\":0,\"limit\":50000,\"acceptPartial\":false,\"project\":\"user_study\"}", sql);

                    results = HttpUtil.query(
                            body);

                    ConcurrentHashMap<String, Object> cMap = new ConcurrentHashMap<>();
                    ConcurrentLinkedDeque<Object> rows = new ConcurrentLinkedDeque<>();

                    ja = JSONObject.parseObject(results).getJSONArray("results");
                    for (int i = 0; i < ja.size(); i++) {

                        JSONArray jsonArray = ja.getJSONArray(i);

                        Map<String, Object> inMap = new HashMap<>();

                        String title = jsonArray.getString(3);
                        if (title == null) {
                            title = "";
                        }

                        String keyname = isEN == 1 ? "title" : "课时名称";
                        String keynum = isEN == 1 ? "num" : "学习人数";
                        String keylength = isEN == 1 ? "length" : "学习时长";
                        String keyave = isEN == 1 ? "ave" : "平均学习时长";


                        String keyid = isEN == 1 ? "id" : "课时ID";
                        String keyaction = isEN == 1 ? "see" : "查看详情";
                        String keytime = isEN == 1 ? "time" : "日期";


                        inMap.put(keyname, title);
                        inMap.put(keylength, jsonArray.getInteger(0));
                        inMap.put(keynum, jsonArray.getInteger(1));
                        inMap.put(keyave, jsonArray.getInteger(0) / jsonArray.getInteger(1));

                        if (isEN == 1) {

                            inMap.put(keyid, jsonArray.getString(2));
                            inMap.put(keyaction, "查看详情");
                            if (querytype.equals("playmonth")) {
                                inMap.put(keytime, $f3.format(f1.parse(startRecordTime)));
                            } else if (querytype.equals("playweek")) {
                                inMap.put(keytime, $f2.format(f1.parse(startRecordTime)));
                            } else {
                                inMap.put(keytime, $f1.format(f1.parse(startRecordTime)));
                            }
                        }
                        rows.add(inMap);

                    }
                    m.put("rows", rows);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    queue1.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            pool.execute(() -> {

                try {

                    String sql =
                            String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser from videoplay2 where %s  ",
                                    pq.timeSelector2(startWeek, endWeek, startRecordTime, endRecordTime, idtype, id),
                                    id);

                    log.info("findByNetclassId created total sql : {} ", sql);

                    String body = "{" +
                            "\"sql\":\"" + sql + "\"," +
                            "\"offset\":0," +
                            "\"limit\":50000," +
                            "\"acceptPartial\":false," +
                            "\"project\":\"" + "user_study" + "\"" +
                            "}";

                    String results = HttpUtil.query(
                            body);

                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
                    for (int i = 0; i < ja.size(); i++) {

                        Map<String, Object> total = new HashMap<>();

                        JSONArray jsonArray = ja.getJSONArray(i);
                        long time = jsonArray.getLong(0);
                        int uname = jsonArray.getInteger(1);
                        double ave = time * 1.0 / uname;

                        total.put("time", time);
                        total.put("uname", uname);
                        total.put("ave", ave);

                        m.put("total", total);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    queue2.put(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            Map<String, Object> inMap = new HashMap<>();
            pool.execute(() -> {

                try {
                    String t = "title";

                    if (idtype.equals("classid")) {
                        t = "classname";
                    }

                    String sql =
                            String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser,%s from videoplay2 where %s  group by %s",
                                    t,
                                    pq.timeSelector2(startWeek, endWeek, startRecordTime, endRecordTime, idtype, id),
                                    t);

                    log.info("findByNetclassId created 合计 sql : {} ", sql);

                    String body = "{" +
                            "\"sql\":\"" + sql + "\"," +
                            "\"offset\":0," +
                            "\"limit\":50000," +
                            "\"acceptPartial\":false," +
                            "\"project\":\"" + "user_study" + "\"" +
                            "}";

                    String results = HttpUtil.query(
                            body);


                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
                    for (int i = 0; i < ja.size(); i++) {

                        JSONArray jsonArray = ja.getJSONArray(i);

                        inMap.put("time", "合计");
                        inMap.put("id", id);
                        inMap.put("num", jsonArray.getLong(1));
                        inMap.put("ave", jsonArray.getLong(0) / jsonArray.getLong(1));
                        inMap.put("title", jsonArray.getString(2));
                        inMap.put("length", jsonArray.getLong(0));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    queue3.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            Map<String, Object> inMap2 = new HashMap<>();
            pool.execute(() -> {

                try {

                    String t = "title";

                    if (idtype.equals("classid")) {
                        t = "classname";
                    }
                    String sql =
                            String.format("select sum(playtime) as playtime,count(distinct uname) as activeuser,%s from videoplay2 where %s  group by %s",
                                    t,
                                    pq.timeSelector2(startWeek, endWeek, "19700101", endRecordTime, idtype, id),
                                    t);

                    log.info("findByNetclassId created 截止当前 sql : {} ", sql);

                    String body = "{" +
                            "\"sql\":\"" + sql + "\"," +
                            "\"offset\":0," +
                            "\"limit\":50000," +
                            "\"acceptPartial\":false," +
                            "\"project\":\"" + "user_study" + "\"" +
                            "}";

                    String results = HttpUtil.query(
                            body);


                    JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
                    for (int i = 0; i < ja.size(); i++) {

                        JSONArray jsonArray = ja.getJSONArray(i);

                        inMap2.put("time", "截止当前");
                        inMap2.put("id", id);
                        inMap2.put("num", jsonArray.getLong(1));
                        inMap2.put("ave", jsonArray.getLong(0) / jsonArray.getLong(1));
                        inMap2.put("title", jsonArray.getString(2));
                        inMap2.put("length", jsonArray.getLong(0));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    queue4.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            queue1.take();
            queue2.take();
            queue3.take();
            queue4.take();

            List<Map<String, Object>> l2 = new ArrayList<>();
            l2.add(inMap);
            l2.add(inMap2);

            m.put("row2", l2);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return m;
    }


    public Object userTable(
            String startRecordTime,
            String endRecordTime,
            Integer parentId,
            String netclassid,
            String courseid,
            String classid,
            int limitSize,
            long offset,
            String filterCondition
    ) {
        String key = "";
        String value = "";

        if (!filterCondition.isEmpty() && !filterCondition.equals("")) {

            String[] kv = filterCondition.split("=");
            key = kv[0];
            value = kv[1];

        }

        Map<String, Object> re = new HashMap<>();
        String[] arr = {"用户ID",
                "用户名",
                "该课程/课时累计学习时长",
                "累计学习时长",
                "学习时长",
                "手机号",
                "注册日期",
                "考试类型",
                "考试地区"
        };
        re.put("columns", arr);
        int gcount = 0;

        List<Map<String, Object>> result = new ArrayList<>();

        try {

            String startWeek = CalendarUtil.getWeekStart(startRecordTime);
            String endWeek = CalendarUtil.getWeekend(endRecordTime);
            PrestoQuery pq = new PrestoQuery();


            if (netclassid != null && !netclassid.isEmpty()) {


                String sql =
                        String.format("select uname,sum(playtime) as playtime from videoplay2 where %s netclassid = %s group by uname ",
                                pq.timeSelecto3r(startWeek, endWeek, startRecordTime, endRecordTime),
                                netclassid);

                log.info("userTable created 1 sql : {} ", sql);

                String body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                String results = HttpUtil.query(
                        body);

                JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");


                StringBuilder sb = new StringBuilder();
                Map<String, Map<String, Object>> map = new HashMap<>();

                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);

                    Map<String, Object> pojo = new HashMap<>();

                    String usname = jsonArray.getString(0);
                    pojo.put("uname", usname);
                    pojo.put("length", jsonArray.getInteger(1));

                    map.put(usname, pojo);
                    sb.append("'" + usname + "'").append(",");

                }
                String uids = sb.toString().substring(0, sb.length() - 1);


                sql = String.format("select uname,sum(playtime) as playtime from videoplay2 where  netclassid = %s group by uname",
                        netclassid);
                log.info("userTable created 2 sql : {} ", sql);

                body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                results = HttpUtil.query(
                        body);

                ja = JSONObject.parseObject(results).getJSONArray("results");
                Map<String, Map<String, Object>> netLength = new HashMap<>();
                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);
                    Map<String, Object> pojo = new HashMap<>();

                    String usname = jsonArray.getString(0);
                    pojo.put("uname", usname);
                    pojo.put("lengthn", jsonArray.getInteger(1));

                    netLength.put(usname, pojo);

                    System.out.println(jsonArray.toJSONString());
                }


                sql = String.format("select uname,sum(playtime) as playtime from videoplay2 where uname in (%s) group by uname",
                        uids);
                log.info("userTable created 3 sql : {} ", sql);

                body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                results = HttpUtil.query(
                        body);

                ja = JSONObject.parseObject(results).getJSONArray("results");
                Map<String, Map<String, Object>> totalLength = new HashMap<>();
                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);

                    Map<String, Object> pojo = new HashMap<>();

                    String usname = jsonArray.getString(0);
                    pojo.put("uname", usname);
                    pojo.put("lengtht", jsonArray.getInteger(1));

                    totalLength.put(usname, pojo);
                }

                Map<String, Map<String, Object>> studyUserInfoById = mysqlService.getStudyUserInfoById(uids);

                for (String uname : map.keySet()) {

                    Map<String, Object> map1 = map.get(uname);
                    Map<String, Object> map2 = studyUserInfoById.get(uname);
                    Map<String, Object> map3 = totalLength.get(uname);
                    Map<String, Object> map4 = netLength.get(uname);
                    map1.putAll(map2);
                    map1.putAll(map3);
                    map1.putAll(map4);
                    result.add(map1);
                }

            } else if (courseid != null && !courseid.isEmpty()) {


                String sql =
                        String.format("select uname,sum(playtime) as playtime from videoplay2 where %s courseid = %s group by uname",
                                pq.timeSelecto3r(startWeek, endWeek, startRecordTime, endRecordTime),
                                courseid);
                if (parentId != null){
                    sql =
                            String.format("select uname,sum(playtime) as playtime from videoplay2 where %s courseid = %s and netclassid = %s group by uname",
                                    pq.timeSelecto3r(startWeek, endWeek, startRecordTime, endRecordTime),
                                    courseid,
                                    parentId);
                }

                log.info("userTable created 1 sql : {} ", sql);

                StringBuilder sb = new StringBuilder();

                String body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                String results = HttpUtil.query(
                        body);

                JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");

                Map<String, Map<String, Object>> map = new HashMap<>();

                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);

                    Map<String, Object> pojo = new HashMap<>();

                    String usname = jsonArray.getString(0);
                    pojo.put("uname", usname);
                    pojo.put("length", jsonArray.getInteger(1));

                    map.put(usname, pojo);
                    sb.append("'" + usname + "'").append(",");
                }
                String uids = sb.toString().substring(0, sb.length() - 1);


                sql = String.format("select uname,sum(playtime) as playtime from videoplay2 where  courseid = %s group by uname",
                        courseid);

                log.info("userTable created 2 sql : {} ", sql);

                body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                results = HttpUtil.query(
                        body);

                ja = JSONObject.parseObject(results).getJSONArray("results");

                Map<String, Map<String, Object>> netLength = new HashMap<>();
                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);

                    Map<String, Object> pojo = new HashMap<>();

                    String usname = jsonArray.getString(0);
                    pojo.put("uname", usname);
                    pojo.put("lengthn", jsonArray.getInteger(1));

                    netLength.put(usname, pojo);
                }

                sql = String.format("select  uname,sum(playtime) as playtime from videoplay2 where uname in (%s) group by uname",
                        uids);

                log.info("userTable created 3 sql : {} ", sql);

                Map<String, Map<String, Object>> totalLength = new HashMap<>();

                body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                results = HttpUtil.query(
                        body);

                ja = JSONObject.parseObject(results).getJSONArray("results");
                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);

                    Map<String, Object> pojo = new HashMap<>();

                    String usname = jsonArray.getString(0);
                    pojo.put("uname", usname);
                    pojo.put("lengtht", jsonArray.getInteger(1));

                    totalLength.put(usname, pojo);
                }


                Map<String, Map<String, Object>> studyUserInfoById = mysqlService.getStudyUserInfoById(uids);

                for (String uname : map.keySet()) {

                    Map<String, Object> map1 = map.get(uname);
                    Map<String, Object> map2 = studyUserInfoById.get(uname);
                    Map<String, Object> map3 = totalLength.get(uname);
                    Map<String, Object> map4 = netLength.get(uname);
                    map1.putAll(map2);
                    map1.putAll(map3);
                    map1.putAll(map4);
                    result.add(map1);
                }
            } else if (classid != null && !classid.isEmpty()) {

                String sql =
                        String.format("select uname,sum(playtime) as playtime from videoplay2 where %s classid = %s group by uname",
                                pq.timeSelecto3r(startWeek, endWeek, startRecordTime, endRecordTime),
                                classid);

                log.info("userTable created 1 sql : {} ", sql);

                StringBuilder sb = new StringBuilder();

                String body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                String results = HttpUtil.query(
                        body);

                JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");

                Map<String, Map<String, Object>> map = new HashMap<>();

                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);

                    Map<String, Object> pojo = new HashMap<>();

                    String usname = jsonArray.getString(0);
                    pojo.put("uname", usname);
                    pojo.put("length", jsonArray.getInteger(1));

                    map.put(usname, pojo);
                    sb.append("'" + usname + "'").append(",");
                }
                String uids = sb.toString().substring(0, sb.length() - 1);


                sql = String.format("select uname,sum(playtime) as playtime from videoplay2 where  classid = %s group by uname",
                        classid);

                log.info("userTable created 2 sql : {} ", sql);

                body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                results = HttpUtil.query(
                        body);

                ja = JSONObject.parseObject(results).getJSONArray("results");

                Map<String, Map<String, Object>> netLength = new HashMap<>();
                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);

                    Map<String, Object> pojo = new HashMap<>();

                    String usname = jsonArray.getString(0);
                    pojo.put("uname", usname);
                    pojo.put("lengthn", jsonArray.getInteger(1));

                    netLength.put(usname, pojo);
                }

                sql = String.format("select  uname,sum(playtime) as playtime from videoplay2 where uname in (%s) group by uname",
                        uids);

                log.info("userTable created 3 sql : {} ", sql);

                Map<String, Map<String, Object>> totalLength = new HashMap<>();

                body = "{" +
                        "\"sql\":\"" + sql + "\"," +
                        "\"offset\":0," +
                        "\"limit\":50000," +
                        "\"acceptPartial\":false," +
                        "\"project\":\"" + "user_study" + "\"" +
                        "}";

                results = HttpUtil.query(
                        body);

                ja = JSONObject.parseObject(results).getJSONArray("results");
                for (int i = 0; i < ja.size(); i++) {

                    JSONArray jsonArray = ja.getJSONArray(i);

                    Map<String, Object> pojo = new HashMap<>();

                    String usname = jsonArray.getString(0);
                    pojo.put("uname", usname);
                    pojo.put("lengtht", jsonArray.getInteger(1));

                    totalLength.put(usname, pojo);
                }


                Map<String, Map<String, Object>> studyUserInfoById = mysqlService.getStudyUserInfoById(uids);

                for (String uname : map.keySet()) {

                    Map<String, Object> map1 = map.get(uname);
                    Map<String, Object> map2 = studyUserInfoById.get(uname);
                    Map<String, Object> map3 = totalLength.get(uname);
                    Map<String, Object> map4 = netLength.get(uname);
                    map1.putAll(map2);
                    map1.putAll(map3);
                    map1.putAll(map4);
                    result.add(map1);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!filterCondition.isEmpty() && !filterCondition.equals("")) {

            String[] kv = filterCondition.split("=");
            String k = kv[0];
            String v = kv[1];

            result = result.stream().filter(map -> map.get(k).toString().equals(v)).collect(Collectors.toList());
        }

        Collections.sort(result, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return (int) (Long.parseLong(o2.get("time").toString()) - Long.parseLong(o1.get("time").toString()));
            }
        });
        gcount = result.size();
        re.put("count", gcount);
        List<Map<String, Object>> result2 = new ArrayList<>();
        long l = (offset + limitSize) > gcount ? gcount : (offset + limitSize);
        for (long i = offset; i < l; i++) {
            result2.add(result.get(Integer.parseInt(Long.toString(i))));
        }

        re.put("rows", result2);
        return re;
    }


    /**
     * 1，安卓，2，ios，3，PC，4，安卓ipad，5，苹果ipad，6，微信
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {


        Class.forName("com.facebook.presto.jdbc.PrestoDriver");
        Connection connection = DriverManager.getConnection(
                "jdbc:presto://huatu68:9999/hive/default", "hive", "");

        Statement statement = connection.createStatement();

        String sql =
                String.format("select rid from scheduledetail where netclassid = %s",
                        2461);

        log.info("getDaoke created 1 sql : {} ", sql);

        ResultSet reset = statement.executeQuery(sql);
//
//        while (reset.next()) {
//
//            System.out.println(reset.getInt(1));
//        }

        List<Map<String, String>> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        while (reset.next()) {

            Map<String, String> m = new HashMap<>();

            String s = Integer.toString(reset.getInt(1));
            m.put("label", s);
            m.put("value", s);

            sb.append(s).append(",");
            result.add(m);

        }

        Map<String, String> m = new HashMap<>();

        m.put("label", "所有");
        m.put("value", sb.toString().substring(0, sb.length() - 1));
        result.add(m);

        System.out.println(JSONObject.toJSONString(result));

    }

    public Object getDaoke(String netid, String cid, String rid, int isDatagrid, String startRecordTime, String endRecordTime, String groupType) {


        Map<String, Object> result = new HashMap<>();

        String[] col = {"日期",
                "购买人数",
                "观看直播人数",
                "到课率"};
        String[] col1 = {"日期",
                "购买人数",
                "观看直播人数",
                "到课率",
                "课程id", "课程标题"};

        result.put("columns", col);
        if (isDatagrid == 1) {
            result.put("columns", col1);
        }

        String startEndTime = startRecordTime + " 00:00:00";
        String endEndTime = endRecordTime + " 23:59:59";

        SimpleDateFormat sdgf = new SimpleDateFormat("yyyy-MM-dd");

        List<Map<String, Object>> rows = new ArrayList<>();

        try {

            if (cid != null) {
                return cidAttendance(cid, isDatagrid, startEndTime, endEndTime, groupType);
            }

            String sql =
                    String.format("select b.classid,a.class_attendance,a.peak_user,a.sales_volume,a.endtime,c.title " +
                                    "from scheduledetail a " +
                                    "left join netclasses_zengsong b on a.netclassid = b.zengclassid " +
                                    "left join Netclasses c on b.classid = c.rid " +
                                    "where b.classid = %s and IsSuit = 1 and a.rid in ( %s ) and endtime >='%s' and endtime <='%s'",
                            netid,
                            rid,
                            startEndTime,
                            endEndTime);

            log.info("getDaoke created 1 sql : {} ", sql);

            ResultSet reset = prestoStmt.executeQuery(sql);

            while (reset.next()) {

                Map<String, Object> pojo = new HashMap<>();

                int netclssid = reset.getInt(1);
                pojo.put("netclssid", netclssid);
                pojo.put("class_attendance", reset.getDouble(2));
                pojo.put("peak_user", reset.getInt(3));
                pojo.put("sales_volume", reset.getInt(4));
                String endtime = reset.getString(5);
                String title = reset.getString(6);

                endtime = endtime.split(" ")[0];

                Date parse = sdgf.parse(endtime);
                String format = $f1.format(parse);

                if (groupType.equals("week")) {

                    String weekStart = CalendarUtil.getWeekStart(endtime.replaceAll("-", ""));
                    format = $f1.format(new Date(Long.parseLong(weekStart)));
                } else if (groupType.equals("month")) {

                    format = $f3.format(parse);
                }
                pojo.put("endtime", format);
                pojo.put("title", title);
                rows.add(pojo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Map<String, Object>> mapMap = new HashMap<>();

        rows.forEach(r -> {
            Converter<String, Integer> converter = Integer::parseInt;

            int netclssid = converter.convertInt(r.get("netclssid").toString());
            String endtime = r.get("endtime").toString();
            String key = endtime + "_" + netclssid;

            int peak_user = converter.convertInt(r.get("peak_user").toString());
            int sales_volume = converter.convertInt(r.get("sales_volume").toString());
            String title = r.get("title").toString();
            Map<String, Object> map = mapMap.getOrDefault(key, new HashMap<String, Object>());

            int peakUser = peak_user + converter.convertInt(map.getOrDefault("peak_user", 0).toString());
            int salesVolume = sales_volume + converter.convertInt(map.getOrDefault("sales_volume", 0).toString());

            String $aclassAttendanceve = isDatagrid == 1 ? "aclass_attendanceve" : "到课率";
            String $peakUser = isDatagrid == 1 ? "peak_user" : "观看直播人数";
            String $salesVolume = isDatagrid == 1 ? "sales_volume" : "购买人数";
            String $time = isDatagrid == 1 ? "time" : "日期";
            String $id = isDatagrid == 1 ? "id" : "课程id";
            String $title = isDatagrid == 1 ? "title" : "课程标题";

            if (isDatagrid == 1) {
                map.put($id, netclssid);
                map.put($title, title);
            }

            map.put($peakUser, peakUser);
            map.put($salesVolume, salesVolume);
            map.put($time, endtime);
            map.put($aclassAttendanceve, peakUser * 1.0 / salesVolume);
            mapMap.put(key, map);
        });
        result.put("rows", mapMap.values());
//        System.out.println(JSONObject.toJSON(mapMap.values()));
        return result;
    }


    public Object ridlist(String idtype, String id) throws SQLException {

        String sql = "";
        ResultSet reset = null;
        if (idtype.equals("netid")) {

            StringBuilder sb = new StringBuilder();
            sql = String.format("select zengclassid from netclasses_zengsong where classid = %s",
                    id);
            ResultSet reset2 = prestoStmt.executeQuery(sql);
            while (reset2.next()) {

                String s = Integer.toString(reset2.getInt(1));
                sb.append(s).append(",");
            }
            sql = String.format("select rid from scheduledetail where netclassid in ( %s )",
                    sb.toString().substring(0, sb.length() - 1));
            reset = prestoStmt.executeQuery(sql);
        } else {

            sql = String.format("select rid from scheduledetail where netclassid = %s",
                    id);
            reset = prestoStmt.executeQuery(sql);

        }

        List<Map<String, String>> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        while (reset.next()) {

            Map<String, String> m = new HashMap<>();

            String s = Integer.toString(reset.getInt(1));
            m.put("label", s);
            m.put("value", s);

            sb.append(s).append(",");
            result.add(m);

        }

        try {
            Map<String, String> m = new HashMap<>();

            m.put("label", "所有");
            m.put("value", sb.toString().substring(0, sb.length() - 1));
            result.add(m);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public Object cidAttendance(String cid, int isDatagrid, String startEndTime, String endEndTime, String groupType) {

        Map<String, Object> result = new HashMap<>();
        try {

            SimpleDateFormat sdgf = new SimpleDateFormat("yyyy-MM-dd");
            String url = "http://testapi.huatu.com/lumenapi/v5/c/class/class_rate?classId={}&date={}";

            long s = sdgf.parse(startEndTime).getTime();
            long e = sdgf.parse(endEndTime).getTime();

            for (long i = s; i <= e; i = i + 24 * 60 * 60 * 100L) {

                String format = sdgf.format(new Date(i));

                Map<String, Object> uriVariables = new HashMap<String, Object>();
                uriVariables.put("classId", cid);
                uriVariables.put("date", format);

                Object r = restTemplate.getForObject("http://testapi.huatu.com/lumenapi/v5/c/class/class_rate?classId=" + cid + "&date=" + format + "",
                        Object.class);
                System.out.println(r);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    private void playBack(Long syllabusId, String username) {

        //用户服务上报学习记录接口
        String url = "http://123.103.86.52:10917/c/v1/report/playBack";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
        params.add("syllabusId", Long.toString(syllabusId));
        params.add("userName", username);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(params, headers);

        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Object.class);

        //  输出结果
        System.out.println(response.getBody());
    }
}

class PrestoQuery {


    String timeSelector(String s1, String s2, String s3, String s4) {

        return " playday >='" + s3 + "' " +
                "and playday<='" + s4 + "' and";
    }

    String timeSelector2(String s1, String s2, String s3, String s4, String s5, String s6) {

        return " playday >='" + s3 + "' " +
                "and playday<='" + s4 + "' and " +
                s5 + " = " + s6 + " ";
    }


    String timeSelecto3r(String s1, String s2, String s3, String s4) {

        return " playweek >= " + s1 +
                " and playweek <= " + s2 +
                " and playday >='" + s3 + "' " +
                " and playday<='" + s4 + "' and ";
    }

    public String timeSelectorp(String startWeek, String endWeek, String startPlayDaY, String endPlayDay, Integer parentId) {

        if (parentId == null) {
            return " playday >='" + startPlayDaY + "' " +
                    "and playday<='" + endPlayDay + "' and";
        } else {
            return " playday >='" + startPlayDaY + "' " +
                    "and playday<='" + endPlayDay + "' and " +
                    "netclassid = " + parentId + " and  ";
        }

    }
}