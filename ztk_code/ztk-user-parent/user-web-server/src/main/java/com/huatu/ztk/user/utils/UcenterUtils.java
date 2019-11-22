package com.huatu.ztk.user.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by linkang on 7/11/16.
 */
public class UcenterUtils {
    private static final Logger logger = LoggerFactory.getLogger(UcenterUtils.class);


    /**
     * 生成随机盐
     * @param length
     * @return
     */
    public static String salt_get(int length) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(length);
        for(int i=0; i<length;i++){
            sb.append(r.nextInt(9));
        }
        return sb.toString();
    }

    /**
     * 密码加密
     * @param password
     * @param salt
     * @param type
     * @return
     */
    public static String password_encypt(String password,String salt,Integer type){
        if(null==type){
            type=1;
        }
        if(null==salt){
            salt="";
        }
        switch (type) {
            case 1:
                password= md5(password+salt);
                break;
            case 2:
                password= md5(md5(password)+salt);
                break;
            case 3:
                password= md5(password).substring(8,24);
                break;
            default:
                break;
        }
        return password;
    }

    /**
     *md5加密
     * @param str
     * @return
     */
    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());

            byte[] b = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                int v = b[i];
                v = v < 0 ? 0x100 + v : v;
                String cc = Integer.toHexString(v);
                if (cc.length() == 1)
                    sb.append('0');
                sb.append(cc);
            }

            return sb.toString();
        } catch (Exception e) {
            logger.error("md5 encypt fail",e);
        }
        return "";
    }

    /**
     * GetGUID
     * @return
     */
    public static synchronized String GetGUID() {
        try {
            SecureRandom seeder;
            String midValue;
            String midValueUnformated;
            StringBuffer stringbuffer = new StringBuffer();
            StringBuffer stringbuffer1 = new StringBuffer();
            seeder = new SecureRandom();
            // 根据网卡地址计算一个随机数
            int i = 0;
            int j = 24;
            try {
                InetAddress inetaddress = InetAddress.getLocalHost();
                byte abyte0[] = inetaddress.getAddress();
                for (int k = 0; j >= 0; k++) {
                    int l = abyte0[k] & 0xff;
                    i += l << j;
                    j -= 8;
                }
            } catch (Exception exception) {
                i = seeder.nextInt();
            }
            String s = ToHex(i, 8);
            String s1 = ToHex(Integer.toString(i).hashCode(), 8);
            // 第一二部分是该数据的Hex码
            // 第三四部分是该数据的hash的Hex代码
            stringbuffer1.append(s.substring(0, 4));
            stringbuffer1.append(s.substring(4));
            stringbuffer1.append(s1.substring(0, 4));
            stringbuffer1.append(s1.substring(4));
            stringbuffer.append("-");
            stringbuffer.append(s.substring(0, 4));
            stringbuffer.append("-");
            stringbuffer.append(s.substring(4));
            stringbuffer.append("-");
            stringbuffer.append(s1.substring(0, 4));
            stringbuffer.append("-");
            stringbuffer.append(s1.substring(4)); // 最后一部分和时间数值合并

            midValue = stringbuffer.toString();// 带格式的
            long l = System.currentTimeMillis();
            int i1 = (int) l & 0xffffffff;
            int j1 = seeder.nextInt();
            midValue = "{" + ToHex(i1, 8) + midValue + ToHex(j1, 8) + "}";

            midValueUnformated = stringbuffer1.toString();// 不带格式的
            int i2 = (int) l & 0xffffffff;
            int j2 = seeder.nextInt();
            midValueUnformated = ToHex(i2, 8) + midValueUnformated
                    + ToHex(j2, 8);
            // midValue就是完全符合微软格式的GUID字符串
            // 本来应该直接返回midValue,为了节省存储空间，用HashCode代替，hashCode数据类型为Int,有正负20亿空间，足够了。
			/*
			 * java数据类型： int 4 字节 -2,147,483,648到2,147,483,647 long 8 字节
			 * -9,223,372,036,854,775,808到9,223,372,036, 854,775,807
			 * 映射到int,有40亿的数字空间，因此也是足够了。
			 */
            int GUIDHashCode = midValue.hashCode();
            if (GUIDHashCode < 0) {
                GUIDHashCode = -GUIDHashCode;
            } // 折叠在20亿空间内，虽然进一步增加了重码的可能性，但20亿空间还是足够了
            return Integer.toString(GUIDHashCode);
        } catch (Exception exception) { // 如果发生错误，用时间戳代替，26位长度，能保证一定的唯一性
            Calendar NewDate = Calendar.getInstance();
            int Year, Month, Day, Week, Hours, Minutes, Seconds;
            long Time;
            Year = NewDate.get(Calendar.YEAR) + 1900;
            String GUID = String.valueOf(Year);
            Month = NewDate.get(Calendar.MONTH) + 1;
            if (Month < 10) {
                GUID = GUID + "0" + String.valueOf(Month);
            } else {
                GUID = GUID + String.valueOf(Month);
            }
            Day = NewDate.get(Calendar.DAY_OF_MONTH);
            if (Day < 10) {
                GUID = GUID + "0" + String.valueOf(Day);
            } else {
                GUID = GUID + String.valueOf(Day);
            }
            Week = NewDate.get(Calendar.DAY_OF_WEEK);
            GUID = GUID + String.valueOf(Week);
            Hours = NewDate.get(Calendar.HOUR);
            if (Hours < 10) {
                GUID = GUID + "0" + String.valueOf(Hours);
            } else {
                GUID = GUID + String.valueOf(Hours);
            }
            Minutes = NewDate.get(Calendar.MINUTE);
            if (Minutes < 10) {
                GUID = GUID + "0" + String.valueOf(Minutes);
            } else {
                GUID = GUID + String.valueOf(Minutes);
            }
            Seconds = NewDate.get(Calendar.SECOND);
            if (Seconds < 10) {
                GUID = GUID + "0" + String.valueOf(Seconds);
            } else {
                GUID = GUID + String.valueOf(Seconds);
            }
            Time = NewDate.getTimeInMillis();
            if (Time < 10) {
                GUID = GUID + "0" + String.valueOf(Time);
            } else {
                GUID = GUID + String.valueOf(Time);
            }
            // 因为java的执行速度很快，所以延迟一下，然后再继续
            long l = System.currentTimeMillis();
            while (System.currentTimeMillis() == l) {
                l = System.currentTimeMillis();
            }
            return GUID;
        }
    }

    /**
     *  将任何整数转换为16进制的HEX格式字符串,j为要求的长度，不够补0
     * @param i
     * @param j
     * @return
     */
    public static String ToHex(int i, int j) {
        String s = Integer.toHexString(i);
        StringBuffer stringbuffer = new StringBuffer();
        if (s.length() < j) {
            for (int k = 0; k < (j - s.length()); k++)
                stringbuffer.append("0");
            return stringbuffer.toString() + s;
        } else {
            return s.substring(0, j);
        }
    }

    public static String getUsername() {
        String username = "app_ztk" + GetGUID();
        username = username.replace("4", "8");
        return username;
    }
}
