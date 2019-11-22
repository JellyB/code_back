package com.huatu.ztk.backend.util;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;
/**
 * 邮件发送逻辑
 * Created by huangqp on 2018\6\12 0012.
 */
public class MailUtil {

    private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);


    public static final String mail_host = "smtp.163.com";
    public static final String mail_transport_protocol = "smtp";
    public static final String smtp_port = "465";

    /**
     *
     * @param title
     * @param text
     * @param filePath
     * @param attachName
     * @throws Exception
     */
    public static void sendMail(String title,String text,String filePath,String attachName) throws Exception {
        Map map = Maps.newHashMap();
        map.put("title",title);
        map.put("text",text);
        map.put("filePath",filePath);
        map.put("attachName",attachName);

        sendMail(map);
    }
    /**
     * @param map
     * @throws Exception
     */
    public static void sendMail(Map map) throws Exception {

        logger.info("》》》》》》》》》》》》》》》》邮件发送！！！！！！！！！！！！！！！");

        String user = "18910645425@163.com";
        String passwd= "hqpvs123";
        Properties prop = new Properties();
        prop.setProperty("mail.host", mail_host);
        prop.setProperty("mail.transport.protocol", mail_transport_protocol);
        prop.setProperty("mail.smtp.auth", "true");
        prop.setProperty("mail.smtp.port", smtp_port);
        prop.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.setProperty("mail.smtp.socketFactory.fallback", "false");
        prop.setProperty("mail.smtp.socketFactory.port", smtp_port);
        //使用JavaMail发送邮件的5个步骤
        //1、创建session
        Session session = Session.getInstance(prop);
        //开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(true);
        //2、通过session得到transport对象
        Transport ts = session.getTransport();
        //3、连上邮件服务器
        ts.connect(mail_host,user, passwd);
        //4、创建邮件
        Message message = createAttachMail(session,map);
        //5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();
    }

    /**
     * @Method: createAttachMail
     * @Description: 创建一封带附件的邮件
     * @Anthor:孤傲苍狼
     *
     * @param session
     * @param map
     * @return
     * @throws Exception
     */
    public static MimeMessage createAttachMail(Session session, Map map) throws Exception{
        MimeMessage message = new MimeMessage(session);
        //设置邮件的基本信息
        //发件人
        message.setFrom(new InternetAddress("18910645425@163.com"));

        //收件人 - 何斌
        message.setRecipient(Message.RecipientType.TO, new InternetAddress("1303445656@qq.com"));
        //huangqp
        message.setRecipient(Message.RecipientType.CC, new InternetAddress("18910645425@163.com"));
        //lijun
        message.addRecipients(Message.RecipientType.CC,"156735229@qq.com");

        //邮件标题
        message.setSubject(String.valueOf(map.get("title")));

        //创建邮件正文，为了避免邮件正文中文乱码问题，需要使用charset=UTF-8指明字符编码
        MimeBodyPart text = new MimeBodyPart();
//        text.setContent("使用JavaMail创建的带附件的邮件", "text/html;charset=UTF-8");
        text.setContent(String.valueOf(map.get("text")), "text/html;charset=UTF-8");

        //创建邮件附件
        MimeBodyPart attach = new MimeBodyPart();
//        DataHandler dh = new DataHandler(new FileDataSource("C:\\Users\\huangqp\\Desktop\\20180505周报.txt"));
        DataHandler dh = new DataHandler(new FileDataSource(String.valueOf(map.get("filePath"))));

        attach.setDataHandler(dh);
        attach.setFileName(dh.getName());

        //创建容器描述数据关系
        MimeMultipart mp = new MimeMultipart();
        mp.addBodyPart(text);
        mp.addBodyPart(attach);
        mp.setSubType("mixed");

        message.setContent(mp);
        message.saveChanges();
        //将创建的Email写入到E盘存储
        message.writeTo(new FileOutputStream(FunFileUtils.TMP_MAIL_SOURCE_FILEPATH+String.valueOf(map.get("attachName"))+".eml"));
        //返回生成的邮件
        return message;
    }
}
