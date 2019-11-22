package com.huatu.hadoop.conf.jsch;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

//@Configuration
public class JschConfiguration {

    private static final String HOST = "192.168.100.26";
    private static final String USERNAME = "root";
    private static final String PWSSWORD = "huatu!2016@ztk#1zws%26";
    private static final Integer PORT = 22;

    @Bean
    public ChannelExec createChannelExec() {
        Session session = null;
        ChannelExec openChannel = null;
        Properties config = new Properties();
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(USERNAME, HOST, PORT);

            config.put("StrictHostKeyChecking", "no");//跳过公钥的询问
            session.setConfig(config);
            session.setPassword(PWSSWORD);
            session.connect(5000);//设置连接的超时时间
            openChannel = (ChannelExec) session.openChannel("exec");
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return openChannel;
    }


    @Bean
    public Session createSession() {
        Session session = null;
        ChannelExec openChannel = null;
        Properties config = new Properties();
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(USERNAME, HOST, PORT);

            config.put("StrictHostKeyChecking", "no");//跳过公钥的询问
            session.setConfig(config);
            session.setPassword(PWSSWORD);
            session.connect(5000);//设置连接的超时时间
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return session;
    }
}
