package com.huatu.ztk.knowledge;

import com.huatu.common.utils.encrypt.EncryptUtil;
import com.huatu.ztk.knowledge.util.Crypt3Des;
import com.huatu.ztk.paper.common.ResponseMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author huangqingpeng
 * @title: QuestionTest
 * @description: TODO
 * @date 2019-09-3011:29
 */
public class QuestionTest {

    private static final Logger logger = LoggerFactory.getLogger(QuestionTest.class);
    public static final String SECRET = "!@#$%^&*()qazxswedc";
    public static void main(String[] args) {
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss");
        Random r = new Random();
        String format = date.format(new Date());
        long orderNum = Long.parseLong(format) * 10000 + r.nextInt(1000);
        payByCoin(201909301145470007L,"app_ztk663759187",1);

    }
    public static int payByCoin(long orderNum, String userName, int total) {
        RestTemplate restTemplate = new RestTemplate();

        //进入金币支付环节
        String url = "http://tk.htexam.com/v3/goldenPay.php";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String encodeUserName = "";
        try {
            encodeUserName = URLEncoder.encode(userName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encodeUserName = userName;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("gold=" + total);
        sb.append("&ordernum=" + orderNum);
        sb.append("&timestamp=" + timestamp);
        sb.append("&useType=" + 2);     //错题下载金币消费类型
        sb.append("&username=" + encodeUserName);
        sb.append(SECRET);
        logger.info(sb.toString());
        String sign = EncryptUtil.md5(sb.toString());

        StringBuffer sbSign = new StringBuffer();
        sbSign.append("useType=" + 2);
        sbSign.append("&username=" + encodeUserName);
        sbSign.append("&ordernum=" + orderNum);
        sbSign.append("&gold=" + total);
        sbSign.append("&timestamp=" + timestamp);
        sbSign.append("&sign=" + sign);
        logger.info("sbSign:{}", sbSign);
        String p = Crypt3Des.encryptMode(sbSign.toString());

        /**
         * 1 :支付成功
         * -4 ：账户金币余额不足
         */
        logger.info("金币支付：发送get请求，url = {}", url + "?p=" + p);
        ResponseEntity<ResponseMsg> forEntity = restTemplate.getForEntity(url + "?p=" + p, ResponseMsg.class);
        ResponseMsg body = forEntity.getBody();
        if (null != body) {
            logger.info("get 请求发送成功");
            System.out.println("body = " + body);
            return body.getCode();
        } else {
            return -1;
        }

    }
}
