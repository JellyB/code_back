package top.jbzm.index.util;


import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 百家云工具
 */
@Slf4j
public class BaiJiaYunUtil {

    //合作id
    private static final String PARTNER_ID="33243432";
    //私钥
    private static final String PARTNER_KEY="R5Bd3efOItVUzzieFDuo4Mm1/HCDcUpfzi08MaSumWh+xfWdS+qVoki6qK9tX7F2XEIp7mtyW9rTzOyHGhidXQ==";
    private static final String SECRET_KEY="";

    public static SimpleDateFormat ymdHms=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat ymd=new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    //取得两个日期中间所有日期
    public static List<String> getBetweenDates(String beginDate, String endDate) throws ParseException {
        Date begin = ymd.parse(beginDate);
        Date end = ymd.parse(endDate);
        List<String> result = new ArrayList();
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(begin);
        while(begin.getTime()<=end.getTime()){
            result.add(ymd.format(tempStart.getTime()));
            tempStart.add(Calendar.DAY_OF_YEAR, 1);
            begin = tempStart.getTime();
        }
        return result;
    }
    /**
     * 取得昨天日期
     * @return
     */
    public static Date getYesterday(){
        Date date=new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        return date;
    }


    /**
     *指定地址发送请求
     * @param urlStr 请求路径
     * @param map 请求参数
     * @param aotuTimestamp 是否需要时间戳
     * @return url返回值
     */
    public static String postHtpps(String urlStr, Map map,boolean aotuTimestamp) {
        if(null==map){
            map=new HashMap();
        }
        map.put("partner_id",PARTNER_ID);//添加开发者id
        if(aotuTimestamp) {
            map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));//添加时间戳
        }
        String sign=getSign(map);//取加密值
        map.put("sign",sign);
        Set<String > set = map.keySet();
        StringBuffer sb = new StringBuffer();
        for(String key:set){
            sb.append(key).append("=").append(map.get(key)).append("&");
        }
        String param=sb.toString();
        param=param.substring(0,param.length()-1);
        log.info("BaiJiaYun Request url {} ,Param : {}",urlStr,param);
        //System.out.println(param);
        OutputStreamWriter out = null;
        BufferedReader br = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection )url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);//开启输出设置
             out = new OutputStreamWriter(
                    con.getOutputStream(), "utf-8");
            out.write(param);
            out.flush();
            out.close();
            br = new BufferedReader(new InputStreamReader(
                    con.getInputStream(),"UTF-8"));
            StringBuffer lines = new StringBuffer();
            String line = "";
            for (line = br.readLine(); line != null; line = br.readLine()) {
                lines.append(line);
            }
            br.close();
            return lines.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
          if(null!=out){
              try {
                  out.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
          if(null!=br){
              try {
                  br.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
        }
        return null;
    }
    /**
     * 对参数计算md5加密
     * @param map 参数
     * @return 小写md5加密值
     */
    public static String getSign(Map<String, String> map) {
        String[] keys = map.keySet().toArray(new String[0]);
        //key排序
        Arrays.sort(keys);
        StringBuffer sb = new StringBuffer();
        for (String key : keys) {
            String v = map.get(key);
            if (v != null && !v.equals("")) {
                sb.append(key).append("=").append(v).append("&");
            }
        }
        sb.append("partner_key").append("=").append(PARTNER_KEY);
        String str = sb.toString();
        //32位的小写的md5
        String md5 = md5(str).toLowerCase();
        return md5;
    }

    /**
     * 使用md5的算法进行加密
     */
    public static String md5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有md5这个算法！");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);// 16进制数字
        // 如果生成数字未满32位，需要前面补0
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }


}
