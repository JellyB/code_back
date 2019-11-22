package com.huatu.ztk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.sql.*;

/**
 * Created by shaojieyue
 * Created time 2016-11-13 13:50
 */
public class LoadRobot {
    private static final Logger logger = LoggerFactory.getLogger(LoadRobot.class);

    public static void main(String[] args) throws Exception {
        Jedis jedis = new Jedis("192.168.100.110",6380);
        Connection con = null; //定义一个MYSQL链接对象
        Class.forName("com.mysql.jdbc.Driver").newInstance(); //MYSQL驱动
        con = DriverManager.getConnection("jdbc:mysql://192.168.100.18/vhuatu", "vhuatu", "vhuatu_2013"); //链接本地MYSQL
        Statement stmt; //创建声明
        stmt = con.createStatement();
        String querySql = "SELECT PUKEY FROM v_qbank_user WHERE  FB1Z3='true'";
        //新增一条数据
        final ResultSet resultSet = stmt.executeQuery(querySql);
        while (resultSet.next()){
            System.out.println(resultSet.getLong("pukey"));
            jedis.sadd("robots",resultSet.getLong("pukey")+"");
        }

        stmt.close();
    }
}
