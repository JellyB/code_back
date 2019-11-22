package com.huatu.hadoop.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huatu.hadoop.bean.UserBaseDTO;
import com.huatu.hadoop.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class UserService {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");

    private static final String body_temp = "{" +
            "\"sql\":\"%s \"," +
            "\"offset\":0," +
            "\"limit\":50000," +
            "\"acceptPartial\":false," +
            "\"project\":\"%s\"" +
            "}";


    @Autowired
    private MysqlService mysqlService;

    private static String kylin_project = "v_user_answer";
    private static String kylin_project2 = "v_user_video";
    private static String kylin_project3 = "v_user_match";

    public Object getUserBase(String phone) {

        Map<String, Object> baseUser = mysqlService.getBaseUser(phone);
        return baseUser;

    }

    public Object getUserAnswer(String phone) throws ParseException {

        Integer uid = mysqlService.getUidByPhone(phone);
        Map<String, Object> result = new HashMap<>();

        String sql =
                String.format("SELECT  " +
                                "  Min(create_time_stamp) as min_time,  " +
                                "  max(create_time_stamp) as max_time,  " +
                                "  sum(correct) as corres,  " +
                                "  sum(question_num) as queses,  " +
                                "  sum(answer_time) as answers  " +
                                "FROM  " +
                                "  v_user_answer  " +
                                "WHERE  " +
                                "  uid = %s",
                        uid.toString());
        String sql2 =
                String.format("SELECT  " +
                                "  sum(question_num),  " +
                                "  create_time_d  " +
                                "FROM  " +
                                "  v_user_answer  " +
                                "WHERE  " +
                                "  uid = %s  " +
                                "GROUP BY  " +
                                "  create_time_d  " +
                                "ORDER BY  " +
                                "  sum(question_num) DESC",
                        uid.toString());

        log.info("getUserAnswer 1 created sql : {} ", sql);
        log.info("getUserAnswer 2 created sql : {} ", sql2);


        String results = HttpUtil.query(
                String.format(body_temp, sql, kylin_project));

        JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
        for (int i = 0; i < ja.size(); i++) {

            JSONArray jo = ja.getJSONArray(i);

            Long max_time = jo.getLong(1) == null ? 0 : jo.getLong(1);
            Long min_time = jo.getLong(0) == null ? 0 : jo.getLong(0);
            Double corrs = jo.getDouble(2) == null ? 0.0 : jo.getDouble(2);
            Double queses = jo.getDouble(3) == null ? 0.0 : jo.getDouble(3);
            Double times = jo.getDouble(4) == null ? 0.0 : jo.getDouble(4);

            result.put("max_time", max_time == 0 ? "" : sdf.format(new Date(max_time)));
            result.put("min_time", min_time == 0 ? "" : sdf.format(new Date(min_time)));
            result.put("corrs", corrs);
            result.put("queses", queses);
            result.put("times", times);
            result.put("accuracy", queses == 0.0 ? 0 : (corrs / queses));
            result.put("speed", queses == 0.0 ? 0 : (times / queses));
        }

        results = HttpUtil.query(
                String.format(body_temp, sql2, kylin_project));

        ja = JSONObject.parseObject(results).getJSONArray("results");
        for (int i = 0; i < ja.size(); i++) {

            Map<String, Object> inMap = new HashMap<>();
            JSONArray jo = ja.getJSONArray(i);

            Long num = jo.getLong(0) == null ? 0 : jo.getLong(0);
            String the_time = jo.getString(1) == null ? "19700101" : jo.getString(1);


            result.put("num", num);
            result.put("the_time", sdf3.format(sdf2.parse(the_time)));
            break;
        }

        System.out.println(result);

        return result;

    }

    public Object getUserVideo(String phone) throws ParseException {

        String uname = mysqlService.getUnameByPhone(phone);
        Map<String, Object> result = new HashMap<>();

        String sql =
                String.format("SELECT  " +
                                "  sum(playtime) as sum_play_time,  " +
                                "  max(create_time_record_time) as max_record,  " +
                                "  min(create_time_record_time)  as min_record " +
                                "FROM  " +
                                "  v_user_video  " +
                                "WHERE  " +
                                "  uname = '%s'",
                        uname);
        String sql2 =
                String.format("SELECT  " +
                                "  syname  " +
                                "FROM  " +
                                "  v_user_video  " +
                                "WHERE  " +
                                "  uname = '%s'  " +
                                "ORDER BY  " +
                                "  create_time_record_time  " +
                                "LIMIT 1",
                        uname);
        String sql3 =
                String.format("SELECT  " +
                                "  syname  " +
                                "FROM  " +
                                "  v_user_video  " +
                                "WHERE  " +
                                "  uname = '%s'  " +
                                "ORDER BY  " +
                                "  create_time_record_time DESC  " +
                                "LIMIT 1",
                        uname);
        String sql4 =
                String.format("SELECT  " +
                                "  sum(playtime),  " +
                                "  title  " +
                                "FROM  " +
                                "  v_user_video  " +
                                "WHERE  " +
                                "  uname = '%s'  " +
                                "GROUP BY  " +
                                "  title  " +
                                "ORDER BY  " +
                                "  sum(playtime) DESC  " +
                                "LIMIT 1",
                        uname);

        log.info("getUserVideo 1 created sql : {} ", sql);
        log.info("getUserVideo 2 created sql : {} ", sql2);
        log.info("getUserVideo 3 created sql : {} ", sql3);
        log.info("getUserVideo 4 created sql : {} ", sql4);

        String results = HttpUtil.query(
                String.format(body_temp, sql, kylin_project2));

        JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
        for (int i = 0; i < ja.size(); i++) {

            JSONArray jo = ja.getJSONArray(i);

            Long sum_play_time = jo.getLong(0) == null ? 0 : jo.getLong(0);
            Long max_record = jo.getLong(1) == null ? 0 : jo.getLong(1);
            Long min_record = jo.getLong(2) == null ? 0 : jo.getLong(2);

            result.put("sum_play_time", sum_play_time == 0 ? 0 : sum_play_time);
            result.put("max_record", max_record == 0 ? "" : sdf.format(new Date(max_record)));
            result.put("min_record", min_record == 0 ? "" : sdf.format(new Date(min_record)));
        }

        result.put("syname_first", "");
        result.put("syname_end", "");
        result.put("title", "");

        results = HttpUtil.query(
                String.format(body_temp, sql2, kylin_project2));
        ja = JSONObject.parseObject(results).getJSONArray("results");
        for (int i = 0; i < ja.size(); i++) {

            JSONArray jo = ja.getJSONArray(i);

            String syname_first = jo.getString(0) == null ? "" : jo.getString(0);
            result.put("syname_first", syname_first);

        }

        results = HttpUtil.query(
                String.format(body_temp, sql3, kylin_project2));
        ja = JSONObject.parseObject(results).getJSONArray("results");
        for (int i = 0; i < ja.size(); i++) {

            JSONArray jo = ja.getJSONArray(i);

            String syname_first = jo.getString(0) == null ? "" : jo.getString(0);
            result.put("syname_end", syname_first);

        }
        results = HttpUtil.query(
                String.format(body_temp, sql4, kylin_project2));
        ja = JSONObject.parseObject(results).getJSONArray("results");
        for (int i = 0; i < ja.size(); i++) {

            JSONArray jo = ja.getJSONArray(i);

            String title = jo.getString(0) == null ? "" : jo.getString(1);
            result.put("title", title);
        }

        return result;
    }

    public Object getUserMatch(String phone) throws ParseException {

//        Integer uid = 234820193;
        Integer uid = mysqlService.getUidByPhone(phone);

        Map<String, Object> result = new HashMap<>();

        String sql =
                String.format("SELECT " +
                                " min(gmt_create) AS min_gmt_create, " +
                                " min(card_create_time) AS min_card_create_time, " +
                                " sum(is_practice) AS sum_is_practice, " +
                                " count(*) AS _count, " +
                                " max(score) as max_score " +
                                "FROM " +
                                " V_USER_MATCH  " +
                                "WHERE " +
                                " user_id = %s",
                        uid);
        String sql2 =
                String.format("SELECT " +
                                " MIN( " +
                                "  V_USER_MATCH.GMT_CREATE " +
                                " ), " +
                                " PAPER_ACTIVITY.NAME " +
                                "FROM " +
                                " V_USER_MATCH  " +
                                "LEFT JOIN PAPER_ACTIVITY ON V_USER_MATCH .MATCH_ID = PAPER_ACTIVITY.ID " +
                                "WHERE " +
                                " user_id = %s " +
                                "GROUP BY " +
                                " PAPER_ACTIVITY.NAME " +
                                "ORDER BY " +
                                " MIN( " +
                                "  V_USER_MATCH.GMT_CREATE " +
                                " ) " +
                                "LIMIT 1",
                        uid);
        String sql3 =
                String.format("SELECT " +
                                " MIN( " +
                                "  V_USER_MATCH.CARD_CREATE_TIME " +
                                " ), " +
                                " PAPER_ACTIVITY.NAME " +
                                "FROM " +
                                " V_USER_MATCH  " +
                                "LEFT JOIN PAPER_ACTIVITY ON V_USER_MATCH.MATCH_ID = PAPER_ACTIVITY.ID " +
                                "WHERE " +
                                " user_id = %s " +
                                "GROUP BY " +
                                " PAPER_ACTIVITY.NAME " +
                                "ORDER BY " +
                                " MIN( " +
                                "  V_USER_MATCH.CARD_CREATE_TIME " +
                                " ) " +
                                "LIMIT 1",
                        uid);

        log.info("getUserMatch 1 created sql : {} ", sql);
        log.info("getUserMatch 2 created sql : {} ", sql2);
        log.info("getUserMatch 3 created sql : {} ", sql3);

        String results = HttpUtil.query(
                String.format(body_temp, sql, kylin_project3));

        JSONArray ja = JSONObject.parseObject(results).getJSONArray("results");
        for (int i = 0; i < ja.size(); i++) {

            JSONArray jo = ja.getJSONArray(i);

            Long min_gmt_create = jo.getLong(0) == null ? 0 : jo.getLong(0);
            Long min_card_create_time = jo.getLong(1) == null ? 0 : jo.getLong(1);
            Long sum_is_practice = jo.getLong(2) == null ? 0 : jo.getLong(2);

            Long _count = jo.getLong(3) == null ? 0 : jo.getLong(3);
            Double max_score = jo.getDouble(4) == null ? 0 : jo.getDouble(4);

            result.put("min_card_create_time", min_card_create_time == 0 ? "" : sdf.format(new Date(min_card_create_time)));
            result.put("min_gmt_create", min_gmt_create == 0 ? "" : sdf.format(new Date(min_gmt_create)));
            result.put("sum_is_practice", sum_is_practice);
            result.put("_count", _count);
            result.put("max_score", max_score);
        }

        result.put("jiexi_see_time", "");
        result.put("jiexi_see_name", "");
        result.put("jiexi_see_num", "");
        result.put("gmt_name", "");
        result.put("card_name", "");

        results = HttpUtil.query(
                String.format(body_temp, sql2, kylin_project3));
        ja = JSONObject.parseObject(results).getJSONArray("results");
        for (int i = 0; i < ja.size(); i++) {

            JSONArray jo = ja.getJSONArray(i);

            result.put("gmt_name", jo.getString(1) == null ? "" : jo.getString(1));

        }

        results = HttpUtil.query(
                String.format(body_temp, sql3, kylin_project3));
        ja = JSONObject.parseObject(results).getJSONArray("results");
        for (int i = 0; i < ja.size(); i++) {

            JSONArray jo = ja.getJSONArray(i);


            result.put("card_name", jo.getString(1) == null ? "" : jo.getString(1));

        }
        return result;
//        return null;
    }

    public static void main(String[] args) throws ParseException {
        String phone = "18810987791";
        Integer uid = 234820193;



//        System.out.println(result);
    }
}
