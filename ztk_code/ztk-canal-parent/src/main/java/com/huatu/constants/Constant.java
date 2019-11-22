package com.huatu.constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 常量
 * Created by ismyway on 16/5/10.
 */
public class Constant {

    private static Properties jdbcProperties = new Properties();
    private static Properties mqProperties = new Properties();

    static{
        try {
            InputStream jdbcInputStream = Constant.class.getClassLoader().getResourceAsStream("properties/jdbc.properties");
            jdbcProperties.load(jdbcInputStream);

            InputStream mqInputStream = Constant.class.getClassLoader().getResourceAsStream("properties/activemq.properties");
            mqProperties.load(mqInputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //canal相关配置属性
    //canal所在服务器的ip
    public static final String SERVER_HOST = jdbcProperties.getProperty("jdbc.server.ip");
    //canal服务器端口
    public static final int SERVER_PORT = Integer.parseInt(jdbcProperties.getProperty("jdbc.server.port"));
    //mysql数据库用户名
    public static final String JDBC_USER = jdbcProperties.getProperty("jdbc.user");
    //mysql数据库连接密码
    public static final String JDBC_PASSWORD = jdbcProperties.getProperty("jdbc.password");
    //canal的destination
    public static final String CNANL_DESTINATON = jdbcProperties.getProperty("canal.destination");


    //mq相关配置属性
    //mq服务器的地址及端口
    public static final String MQ_URL = mqProperties.getProperty("mq.url");
    //mq用户名
    public static final String MQ_USER = mqProperties.getProperty("mq.user");
    //mq密码
    public static final String MQ_PASSWORD = mqProperties.getProperty("mq.password");


    public static void main(String[] args) {
        System.out.println(MQ_PASSWORD);
        System.out.println(MQ_USER);
        System.out.println(MQ_URL);
    }
}
