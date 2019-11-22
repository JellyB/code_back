package com.ht.galaxy.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * @author gaoyuchao
 * @create 2018-07-31 16:31
 */
@Configuration
public class Hive {

    @Bean
    public Statement statement() throws Exception{
        Class.forName("org.apache.hive.jdbc.HiveDriver");
//        Connection conn = DriverManager.getConnection("jdbc:hive2://192.168.11.102:10000/default","admin","");
        Connection conn = DriverManager.getConnection("jdbc:hive2://192.168.100.26:10000/active","root","");
        Statement stmt = conn.createStatement();
        return stmt;
    }

    @Bean
    public Connection conn() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.100.21:3306/bigdata03", "root", "unimob@12254ns");
        return conn;
    }

    @Bean
    public Statement statement2() throws Exception{
        Class.forName("org.apache.hive.jdbc.HiveDriver");
//        Connection conn = DriverManager.getConnection("jdbc:hive2://192.168.11.102:10000/default","admin","");
        Connection conn = DriverManager.getConnection("jdbc:hive2://192.168.100.26:10000/register","root","");
        Statement stmt = conn.createStatement();
        return stmt;
    }
    @Bean
    public Connection conn2() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.100.21:3306/bigdata02", "root", "unimob@12254ns");
        return conn;
    }

    @Bean
    public Connection conn4() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn4 = DriverManager.getConnection("jdbc:mysql://192.168.100.21:3306/bigdata06", "root", "unimob@12254ns");
        return conn4;
    }

    @Bean
    public Connection conn3() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.100.21:3306/bigdata01", "root", "unimob@12254ns");
        return conn;
    }

}
