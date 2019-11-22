package com.ht.galaxy.repository;


import com.alibaba.fastjson.JSONObject;
import com.ht.galaxy.bean.RedisConf;
import com.ht.galaxy.common.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author gaoyuchao
 * @create 2018-07-03 18:13
 */
@Component
public class HiveRepository {

    @Resource(name="statement")
    private Statement statement;


    public List selectAll(String startTime,String endTime,String mode,String type) throws Exception {

        ArrayList<SortType> lists = new ArrayList<>();
        List sumList = selectMode(startTime, endTime, mode);
        List typeList = selectType(startTime, endTime, mode, type);
        HashMap<Integer, Double> map = new HashMap<>();

        Iterator sumIterator = sumList.iterator();
        Iterator typeIterator = typeList.iterator();

        int sum = 0;
        while (typeIterator.hasNext()){
            if (type.equals("area")){
                UserCountArea userCountArea = (UserCountArea)typeIterator.next();
                sum += userCountArea.getCount();
            }else if (type.equals("subject")){
                UserCountSubject userCountSubject = (UserCountSubject) typeIterator.next();
                sum += userCountSubject.getCount();
            }else if (type.equals("terminal")){
                UserCountTerminal userCountTerminal = (UserCountTerminal)typeIterator.next();
                sum += userCountTerminal.getCount();
            }
        }

        Iterator iterator2 = typeList.iterator();
        while (iterator2.hasNext()){
            if (type.equals("area")){
                UserCountArea userCountArea = (UserCountArea)iterator2.next();
                int count = userCountArea.getCount();
                map.put(count,(double)count/sum);
            }else if (type.equals("subject")){
                UserCountSubject userCountSubject = (UserCountSubject) iterator2.next();
                int count = userCountSubject.getCount();
                map.put(count,(double)count/sum);
            }else if (type.equals("terminal")){
                UserCountTerminal userCountTerminal = (UserCountTerminal)iterator2.next();
                int count = userCountTerminal.getCount();
                map.put(count,(double)count/sum);
            }
        }

        while (sumIterator.hasNext()){
            UserCount userCount = (UserCount)sumIterator.next();
            SortType sortType = new SortType(sum, userCount.getCount(), map);
            lists.add(sortType);
        }
        return lists;
    }

    /**
     * 处理时间参数
     * @param startTime
     * @param endTime
     * @param mode
     * @return
     * @throws Exception
     */
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

    /**
     * 查询按 时/天/周/月 按 终端/考试类型/考试地区 分类的活跃用户数
     * @param startTime
     * @param endTime
     * @param mode
     * @param type
     * @return
     * @throws Exception
     */
    public List selectType(String startTime,String endTime,String mode,String type) throws Exception{

        String[] str = timeUtil(startTime, endTime, mode);
        startTime = str[0];
        endTime = str[1];
        String sql = "select * from user_count_" + type +"_" + mode + " where " + mode + " between '" + startTime + "' and '" + endTime + "'";
        ResultSet rs = statement.executeQuery(sql);

        List<Object> lists = new ArrayList<>();
        if(type.equals("area")){
            while (rs.next()) {
                UserCountArea userCountArea = new UserCountArea(rs.getInt(type),rs.getInt("count"),rs.getString(mode));
                lists.add(userCountArea);
            }
            return lists;
        }else if(type.equals("subject")){
            while (rs.next()) {
                UserCountSubject userCountSubject = new UserCountSubject(rs.getInt(type),rs.getInt("count"),rs.getString(mode));
                lists.add(userCountSubject);
            }
            return lists;
        }else if(type.equals("terminal")){
            while (rs.next()) {
                UserCountTerminal userCountTerminal = new UserCountTerminal(rs.getInt(type),rs.getInt("count"),rs.getString(mode));
                lists.add(userCountTerminal);
            }
            return lists;
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

    /**
     * 查询按 时/天/周/月 的活跃用户总数
     * @param startTime
     * @param endTime
     * @param mode
     * @return
     * @throws Exception
     */
    public List selectMode(String startTime,String endTime,String mode) throws Exception{
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

    /**
     * 查询事件分析
     * @param startTime
     * @param endTime
     * @param mode
     * @param sign
     * @param event
     * @return
     * @throws Exception
     */
    public List selectEvent(String startTime,String endTime,String mode,String sign,Event event) throws Exception{
// 处理时间参数
        String[] str = timeUtil(startTime, endTime, mode);
        startTime = str[0];
        endTime = str[1];
// redis 取值
        JedisSentinelPool jedisPool = RedisConf.getJedisSentinelPool();
        Jedis jedis = jedisPool.getResource();
        Iterator<Type> iteratork = event.getList().iterator();
        StringBuffer k = new StringBuffer();
        k.append(startTime+endTime+mode+sign);
        while(iteratork.hasNext()){
            Type next = iteratork.next();
            k.append(next.getType()+next.getSymbol());
            Iterator<String> iterator1 = next.getValues().iterator();
            while (iterator1.hasNext()){
                String next1 = iterator1.next();
                k.append(next1);
            }
        }
        jedis.set("key",k.toString());

        List<Object> lists = new ArrayList<>();
        if(StringUtils.isNotEmpty(jedis.get(k.toString()))){
//            List<Object> objects = new ArrayList<>();
            String v = jedis.get(k.toString());
            String[] vs = v.split("\n");
            for(String string : vs){
                lists.add(JSONObject.parse(string));
            }
//            System.out.println("redis...");
            jedis.close();
            return lists;
        }

// 拼接sql
        StringBuffer sql = new StringBuffer();
        sql.append("select count(*) as count,"+ mode +" from (select distinct(username),");
        if(mode.equals("hour")){
            sql.append("formattime(loginTime,'yyyyMMddHH') as hour");
        }else if(mode.equals("day")){
            sql.append("formattime(loginTime,'yyyyMMdd') as day");
        }else if(mode.equals("month")){
            sql.append("formattime(loginTime,'yyyyMM') as month");
        }else if(mode.equals("week")){
            sql.append("formattime(getweekbegin(formattime(loginTime,'yyyyMMdd'),'yyyyMMdd'),'yyyyMMdd') as week");
        }
        sql.append(" from user_active where ");

        Iterator<Type> iterator = event.getList().iterator();
        while (iterator.hasNext()){
            Type next = iterator.next();
            sql.append(next.getType() + " "+ next.getSymbol() + " (");
            Iterator<String> iterator1 = next.getValues().iterator();
            while (iterator1.hasNext()){
                String next1 = iterator1.next();
                sql.append(next1);
                if(iterator1.hasNext()){
                    sql.append(",");
                }else {
                    sql.append(")");
                }
            }
            if(iterator.hasNext()){
                sql.append(" " + sign + " ");
            }
        }
        sql.append(") abc where " + mode + " between '" + startTime + "' and '" + endTime + "' group by " + mode);
//        System.out.println(sql);

        jedis.set("mode",mode);
        jedis.set("sql",sql.toString());

// 返回结果
        while(true){
            Thread.sleep(1000);
            if(StringUtils.isNotEmpty(jedis.get(k.toString()))){
                String v = jedis.get(k.toString());
                String[] vs = v.split("\n");
                for(String string : vs){
                    lists.add(JSONObject.parse(string));
                }
//                System.out.println("redis.....");
                jedis.set("mode","123");
                jedis.close();
                return lists;
            }
        }
    }


    @Resource(name="conn")
    private Connection conn;

    /**
     * 查询 当天/当周/当月 的实时活跃用户总数
     * @param mode
     * @return
     * @throws Exception
     */
    public int selectSum(String mode) throws Exception {
        Statement statement = conn.createStatement();

        Date date = new Date();
        SimpleDateFormat format = null;
        if(mode.equals("day")){
            format = new SimpleDateFormat("yyyyMMdd");
        }else if(mode.equals("week")){
            format = new SimpleDateFormat("ww");
        }else {
            format = new SimpleDateFormat("yyyyMM");
        }
        String sql = null;
        if(mode.equals("sum")){
            sql = "select * from sum";
        }else{
            sql = "select  * from 1" + mode + "2sumactive" + format.format(date);
        }
        ResultSet rs = statement.executeQuery(sql);
        int sums = 0;
        while(rs.next()){
            sums = rs.getInt("sum");
        }
        statement.close();
        return sums;
    }

    /**
     * 查询 某天/某周/某月 的实时活跃数据
     * @param time
     * @param mode
     * @return
     * @throws Exception
     */
    public List selectSumReal(String time,String mode) throws Exception {
        Statement statement = conn.createStatement();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date = sdf.parse(time);

        SimpleDateFormat format = null;
        if(mode.equals("day")){
            format = new SimpleDateFormat("yyyyMMdd");
        }else if(mode.equals("month")){
            format = new SimpleDateFormat("yyyyMM");
        }else {
            format = new SimpleDateFormat("ww");
        }

        String sql = "select * from 1" + mode + "2sum2realactive" + format.format(date);

        List<Object> lists = new ArrayList<>();

        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()){
            activeSum activeSum = new activeSum(rs.getLong("time"), rs.getInt("sum"));
            lists.add(activeSum);
        }
        statement.close();
        return lists;
    }

}
