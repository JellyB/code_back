package com.ht.galaxy.repository;


import com.ht.galaxy.common2.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author gaoyuchao
 * @create 2018-07-27 10:41
 */
@Component
public class RegisterResopitory {

    @Resource(name="statement2")
    private Statement statement;

    private String[] timeUtil(String startTime,String endTime,String mode) throws Exception{
        String[] str = new String[2];
        str[0] = startTime;
        str[1] = endTime;
        if (mode.equals("hour")){
            startTime = startTime + "00";
            endTime = endTime + "24";
            str[0] = startTime;
            str[1] = endTime;
//            System.out.println(startTime + " " + endTime);
        }else if(mode.equals("month")){
            startTime = startTime.substring(0,6);
            endTime = endTime.substring(0,6);
            str[0] = startTime;
            str[1] = endTime;
        }else if(mode.equals("week")){
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date startDate = format.parse(startTime);
            Date endDate = format.parse(endTime);
            Calendar c = Calendar.getInstance();
            Calendar d = Calendar.getInstance();
            c.setTime(startDate);
            d.setTime(endDate);
            //得到当前日期是所在周的第几天
            int n = c.get(Calendar.DAY_OF_WEEK);
            int m = d.get(Calendar.DAY_OF_WEEK);
            //通过减去周偏移量获得本周的第一天
            c.add(Calendar.DAY_OF_MONTH, -(n - 1));
            d.add(Calendar.DAY_OF_MONTH, -(m - 1));
            startTime = format.format(c.getTime());
            endTime = format.format(d.getTime());
            str[0] = startTime;
            str[1] = endTime;
        }
        return str;
    }

    public List select(String startTime,String endTime,String mode) throws Exception{
        String[] str = timeUtil(startTime, endTime, mode);
        startTime = str[0];
        endTime = str[1];
        String sql = "select * from user_count_"  + mode + " where " + mode + " between '" + startTime + "' and '" + endTime + "'";
        ResultSet rs = statement.executeQuery(sql);
        List<UserCount> lists = new ArrayList<>();
        while (rs.next()) {
            UserCount userCount = new UserCount(rs.getInt("count"),rs.getString(mode));
            lists.add(userCount);
        }
        return lists;
    }
    public List select(String startTime,String endTime,String mode,String type) throws Exception{

        String[] str = timeUtil(startTime, endTime, mode);
        startTime = str[0];
        endTime = str[1];
        String sql = "select * from user_count_" + type +"_" + mode + " where " + mode + " between '" + startTime + "' and '" + endTime + "'";
        ResultSet rs = statement.executeQuery(sql);

        List<Object> lists = new ArrayList<>();

        if(type.equals("regfrom")){
            while (rs.next()) {
                UserCountRegfrom userCountRegfrom = new UserCountRegfrom(rs.getString(type),rs.getInt("count"),rs.getString(mode));
                lists.add(userCountRegfrom);
            }

        }else if(type.equals("url")){
            while (rs.next()) {
                UserCountUrl userCountUrl = new UserCountUrl(rs.getString(type),rs.getInt("count"),rs.getString(mode));
                lists.add(userCountUrl);
            }

        }else {
            while (rs.next()) {
                UserCountTerminal userCountTerminal = new UserCountTerminal(rs.getInt("terminal"),rs.getInt("count"),rs.getString(mode));
                lists.add(userCountTerminal);
            }
        }

        return lists;
    }

    public List selectTerminal(String startTime,String endTime,String mode,String terminal) throws Exception{

        String[] str = timeUtil(startTime, endTime, mode);
        startTime = str[0];
        endTime = str[1];
        String sql = "select * from user_count_terminal" +"_" + mode + " where " + mode + " between '" + startTime + "' and '" + endTime + "'" + " and terminal=" + terminal;
        ResultSet rs = statement.executeQuery(sql);

        List<Object> lists = new ArrayList<>();

        while (rs.next()) {
            UserCountTerminal userCountTerminal = new UserCountTerminal(rs.getInt("terminal"),rs.getInt("count"),rs.getString(mode));
            lists.add(userCountTerminal);
        }

        return lists;
    }

    @Resource(name="conn2")
    private Connection conn;

    private String formatTime(String mode){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        String date = null;
        SimpleDateFormat format = null;
        if(mode.equals("day")){
            calendar.add(Calendar.DATE,-1);
            format = new SimpleDateFormat("yyyyMMdd");
            date = format.format(calendar.getTime());
        }else if(mode.equals("month")){
            calendar.add(Calendar.MONTH,-1);
            format = new SimpleDateFormat("yyyyMM");
            date = format.format(calendar.getTime());
        }else {
            int n = calendar.get(Calendar.DAY_OF_WEEK);
            calendar.add(Calendar.DAY_OF_MONTH, -(n - 1));
            format = new SimpleDateFormat("yyyyMMdd");
            date = format.format(calendar.getTime());
        }
        return date;
    }


    @Resource(name="conn4")
    private Connection conn4;
    public Map selectMode(String mode) throws Exception {
        Statement state = conn4.createStatement();

        String date = formatTime(mode);

        String sql = null;
        String sql2 = null;
        HashMap<Integer, Double> map = new HashMap<>();
        if (mode.equals("sum")){
            sql = "select * from sum";
            ResultSet rs = state.executeQuery(sql);

            while (rs.next()){
                map.put(rs.getInt("count"),0.0);
            }

        }else{
            sql = "select  * from " + mode;
            sql2 = "select count from user_count_" + mode + " where " + mode + "='" + date + "'";


            ResultSet rs = state.executeQuery(sql);
            ResultSet rs2 = statement.executeQuery(sql2);

            if (rs.next() && rs2.next()){
                int s1 = rs.getInt("count");
                double s2 = (double)rs2.getInt("count");
                map.put(s1,(s1-s2)/s2);
            } else {
                int s1 = rs.getInt("count");
                map.put(s1,0.0);
            }
        }

        state.close();
        return map;
    }

    public List selectSumReal(String time,String mode) throws Exception {
        Statement statement = conn.createStatement();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date = sdf.parse(time);

        SimpleDateFormat format = null;
        if (mode.equals("day")) {
            format = new SimpleDateFormat("yyyyMMdd");
        }else if (mode.equals("month")) {
            format = new SimpleDateFormat("yyyyMM");
        }else {
            format = new SimpleDateFormat("ww");
        }

        String sql = "select * from 1" + mode + "2sum2realregister" + format.format(date);

        List<Object> lists = new ArrayList<>();

        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()){
            registerSum registerSum = new registerSum(rs.getLong("time"), rs.getInt("sum"));
            lists.add(registerSum);
        }
        statement.close();
        return lists;
    }

}
