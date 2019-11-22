package com.huatu.tiku.essay.test;

import com.huatu.common.utils.encrypt.EncryptUtil;
import com.huatu.tiku.essay.util.file.Crypt3Des;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Size;
import java.net.URLEncoder;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-05-14 10:36 AM
 **/
@Slf4j
public class Pay {

    public static void main(String[] args) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("gold=300");
        sb.append("&ordernum=49028");
        sb.append("&timestamp=1557803983");
        sb.append("&username=sigejiahao%E5%9B%9B%E4%B8%AA%E5%8A%A0%E5%8F%B7");
        sb.append("!@#$%^&*()qazxswedc");

        System.err.println("StringBuffer ---- " + sb.toString());

        String sign = EncryptUtil.md5(sb.toString());
        System.err.println("sign ---------:" + sign);

        String sign2 = EncryptUtil.md5("gold=300&ordernum=49028&timestamp=1557803983&username=sigejiahao%E5%9B%9B%E4%B8%AA%E5%8A%A0%E5%8F%B7!@#$%^&*()qazxswedc");

        System.err.println("sign2 ---------:" + sign2);

        StringBuffer sbSign = new StringBuffer();
        sbSign.append("username=username=app_ztk271277978四个加号");
        sbSign.append("&ordernum=49021");
        sbSign.append("&gold=300");
        sbSign.append("&timestamp=1557801362");
        sbSign.append("&sign=" + sign);
        log.info("sbSign:{}", sbSign);
        String p = Crypt3Des.encryptMode(sbSign.toString());
        log.info("p----------:{}", p);
        System.err.println("p ---------:" + p);


        String uname = "sigejiahao四个加号";
        String uname2 = "sigejiahao1231234";
        System.err.println(URLEncoder.encode(uname2, "UTF-8"));


    }
}
