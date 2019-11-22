package top.jbzm.index.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author wangjian
 **/
@Slf4j
public class PHPBaiJiaYunTools {

    //课程目录接口 传入页码参数page
    public  static final String url="http://api.huatu.com/lumenapi/v4/common/statistics/net_classes?pageSize=100&type=other&page=";
    public  static final String liveUrl="http://api.huatu.com/lumenapi/v4/common/statistics/net_classes?pageSize=100&type=live&page=";

    //录播与课程映射
    public static final Map coursePlayMap=new HashMap();
    //直播与课程映射
    public static final Map courseLiveMap=new HashMap();
    //课程集合
    public static final Set<Map> courseSet=new HashSet();
    //子课程与套餐映射
    public static final Map suitSonCourseMap=new HashMap();
    //套餐集合
    public static final Set<Map> suitSet=new HashSet();
    //子课程集合
    public static final Set<Map> sonCourseSet=new HashSet();







    //get连接php
    public static String getConnectPhp(String urlStr, String param){
        OutputStreamWriter out = null;
        BufferedReader br = null;
        if(null!=param){
            urlStr=urlStr+param;
        }
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection )url.openConnection();
            con.setRequestMethod("GET");
            log.info(urlStr);
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

}
