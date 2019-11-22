package com.huatu.hadoop.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import com.huatu.hadoop.util.CalendarUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Service
@Slf4j
public class MysqlService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public List<Map<String, Object>> getUserInfoById(String uids) {

        String sql = "select  PUKEY as id,nick,case avatar when 'http://tiku.huatu.com/cdn/images/vhuatu/avatars/default.png' then 'http://tiku.huatu.com/cdn/pandora/img/e54d7996-6a14-47c0-b198-016e66d3c4f6..png' else avatar END as avatar from v_qbank_user where PUKEY in(" + uids + ")";
//        m.put("avatar","http://tiku.huatu.com/cdn/images/vhuatu/avatars/default.png");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        List<String> no = new ArrayList<>();
        String[] split = uids.split(",");

        for (String s : split) {
            boolean flag = false;
            for (Map<String, Object> u : list) {
                String id = u.get("id").toString();
                if (s.equals(id)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                no.add(s);
            }
        }

        for (String s : no) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", Integer.parseInt(s));
            m.put("nick", s);
            m.put("avatar", "http://tiku.huatu.com/cdn/pandora/img/e54d7996-6a14-47c0-b198-016e66d3c4f6..png");

            list.add(m);
        }
        return list;
    }

    public Map<String, Map<String, Object>> getStudyUserInfoById(String uids) {

        Map<String, Map<String, Object>> umap = new HashMap<>();

        String sql =
                String.format("SELECT " +
                        " a.PUKEY AS id,uname,reg_phone as phone,a.BB103 as reg_time,IFNULL(b.`name`,'全国') as area,IFNULL(c.`name`,'默认') as subject " +
                        "FROM " +
                        " v_qbank_user a  " +
                        "LEFT JOIN v_common_area b  on a.area=b.PUKEY " +
                        "LEFT JOIN v_new_subject c on a.`subject`=c.id " +
                        "WHERE " +
                        " uname IN (%s)", uids);

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);


        for (Map<String, Object> m : list) {

            Map<String, Object> pojo = new HashMap<>();
            String uname = m.get("uname").toString();

            pojo.put("id", m.get("id"));
            pojo.put("phone", m.get("phone"));
            pojo.put("time", m.get("reg_time"));
            pojo.put("area", m.get("area"));
            pojo.put("subject", m.get("subject"));
            pojo.put("uname", uname);

            umap.put(uname, pojo);

        }

        return umap;
    }


    public Map<String, Object> getBaseUser(String phone) {

        Map<String, Object> umap = new HashMap<>();

        String sql =
                String.format("SELECT " +
                        " uname, " +
                        " nick, " +
                        " reg_phone, " +
                        " reg_from, " +
                        " IFNULL(c.`name`, '全国') AS area, " +
                        " IFNULL(b.`name`, '默认') AS 'subject', " +
                        " reg_ip, " +
                        " last_login_time, " +
                        " a.BB103, " +
                        " d.source_type, " +
                        " d.source " +
                        "FROM " +
                        " v_qbank_user a " +
                        "LEFT JOIN v_new_subject b ON a.`subject` = b.id " +
                        "LEFT JOIN v_common_area c ON a.area = c.PUKEY " +
                        "LEFT JOIN app_channel d ON a.FB1Z1 = d.device_token " +
                        "WHERE " +
                        " reg_phone = '%s'", phone);

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);


        for (Map<String, Object> m : list) {

            String uname = m.get("uname").toString();
            String reg_from = m.get("reg_from").toString();
            String reg;
            switch (reg_from) {
                case "1":
                    reg = "ANDROID";
                    break;
                case "2":
                    reg = "IPHONE";
                    break;
                case "3":
                    reg = "PC";
                    break;
                case "4":
                    reg = "ANDROID_IPAD";
                    break;
                case "5":
                    reg = "IPHONE_IPAD";
                    break;
                case "6":
                    reg = "WEI_XIN";
                    break;
                case "7":
                    reg = "MOBILE";
                    break;
                case "8":
                    reg = "BATCH";
                    break;
                case "9":
                    reg = "EDUCATION";
                    break;
                case "21":
                    reg = "WEI_XIN_APPLET";
                    break;
                default:
                    reg = "";
            }

            String source;
            switch (reg_from) {
                case "180001":
                    source = "官网";
                    break;
                case "180002":
                    source = "m站";
                    break;
                case "180003":
                    source = "升级包";
                    break;
                case "180004":
                    source = "百度手助";
                    break;
                case "180005":
                    source = "华为";
                    break;
                case "180006":
                    source = "vivo";
                    break;
                case "180007":
                    source = "oppo";
                    break;
                case "180008":
                    source = "三星";
                    break;
                case "180009":
                    source = "阿里";
                    break;
                case "180010":
                    source = "应用宝";
                    break;
                case "180011":
                    source = "小米";
                    break;
                case "180012":
                    source = "魅族";
                    break;
                case "180013":
                    source = "360";
                    break;
                case "180014":
                    source = "锤子";
                    break;
                case "180015":
                    source = "搜狗手助";
                    break;
                case "180016":
                    source = "金立";
                    break;
                case "180017":
                    source = "联想";
                    break;
                case "180018":
                    source = "木蚂蚁";
                case "180019":
                    source = "其他";
                case "180020":
                    source = "安智";
                case "180050":
                    source = "今日头条";
                case "180051":
                    source = "百度信息流";
                case "180052":
                    source = "应用宝广告";
                case "180053":
                    source = "360sem";
                default:
                    source = "";
            }

            long reg_time = Long.parseLong(m.get("BB103").toString()) * 1000;
            long last_login_time = Long.parseLong(m.get("last_login_time").toString()) * 1000;

            umap.put("uname", uname);
            umap.put("nick", m.get("nick"));
            umap.put("reg_phone", m.get("reg_phone"));
            umap.put("reg_end", reg);
            umap.put("reg_from", source);
            umap.put("area", m.get("area"));
            umap.put("subject", m.get("subject"));
            umap.put("reg_ip", m.get("reg_ip"));
            umap.put("last_login_time", sdf.format(new Date(last_login_time)));
            umap.put("BB103", sdf.format(new Date(reg_time)));
        }

        return umap;
    }

    public Integer getUidByPhone(String phone) {


        String sql =
                String.format("SELECT " +
                        " PUKEY as id " +
                        "FROM " +
                        " v_qbank_user " +
                        "WHERE " +
                        " reg_phone = '%s'", phone);
        log.info("getUidByPhone 1 created sql : {} ", sql);
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
        if (maps != null && maps.size() > 0) {

            Map<String, Object> stringObjectMap = maps.get(0);
            String id = stringObjectMap.getOrDefault("id", 0).toString();
            return Integer.parseInt(id);
        }
        return 0;
    }

    public String getUnameByPhone(String phone) {


        String sql =
                String.format("SELECT " +
                        " uname as uname " +
                        "FROM " +
                        " v_qbank_user " +
                        "WHERE " +
                        " reg_phone = '%s'", phone);
        log.info("getUnameByPhone 1 created sql : {} ", sql);
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
        if (maps != null && maps.size() > 0) {
            Map<String, Object> stringObjectMap = maps.get(0);
            return stringObjectMap.getOrDefault("uname", "").toString();
        }
        return "";
    }

    public static void main(String[] args) throws ParseException {

        SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
        String weekStart = CalendarUtil.getWeekStart(yyyyMMdd.format(new Date(1548518400000L)));
        System.out.println(yyyyMMdd.parse(yyyyMMdd.format(Long.parseLong(CalendarUtil.getWeekStart(yyyyMMdd.format(new Date(System.currentTimeMillis())))))));


    }
}
