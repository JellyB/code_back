package com.huatu.tiku.schedule.biz.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.vo.php.PHPUpdateTeacherVo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wangjian
 **/
@Slf4j
public class PHPUtil {

    //
    //private static final String MD_API_URL = "http://api.huatu.com/lumenapi/v4/common/teacher/list";//正式地址
    private static final String GET_URL = "http://testapi.huatu.com/lumenapi/v4/common/teacher/list";//测试地址

    private static final String POST_URL = "http://testapi.huatu.com/lumenapi/v4/common/teacher/sync";//测试地址
    //private static final String POST_URL = "http://api.huatu.com/lumenapi/v4/common/teacher/sync";//正式地址

    private static final String POST_Status_URL = "http://testapi.huatu.com/lumenapi/v4/common/teacher/status";//测试地址

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(2000, TimeUnit.MILLISECONDS)
            .writeTimeout(5000, TimeUnit.MILLISECONDS).readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectionPool(new ConnectionPool(100, 5 * 60 * 1000, TimeUnit.MILLISECONDS)).followRedirects(true)// 跟踪重定向
            .build();

    /**
     * get方法
     * @return 返回值
     */
    public static String get() {
        Request request = new Request.Builder().url(GET_URL).get().build();
        String result=null;
        try {
            Response execute = okHttpClient.newCall(request).execute();
            result = execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * post方法
     * @param teacher vo
     * @return 返回值
     */
  public static String post(Teacher teacher) {
      String result=null;
        try {
            FormBody formBody = getBody(teacher);
            Request request = new Request.Builder().url(POST_URL).post(formBody).build();
            Response execute = okHttpClient.newCall(request).execute();
            result = execute.body().string();
        } catch (Exception e) {
            log.info("id:{},pid:{},name:{},phone:{},examType:{},status:{},subjectId:{}",teacher.getId(),teacher.getPid(),
                    teacher.getName(),teacher.getPhone(),teacher.getTeacherType(),teacher.getStatus(),teacher.getSubjectId());
            log.error(e.getMessage());
        }
      return result;
    }


    //封装参数
    private static FormBody getBody(Teacher bean){
        PHPUpdateTeacherVo teacher=new PHPUpdateTeacherVo(bean);
        HashMap<String,String> map = new HashMap();
        if(teacher.getPid()!=null){
            map.put("pid",String.valueOf(teacher.getPid()));
        }
        if(teacher.getName()!=null){
            map.put("name",teacher.getName());
        }
        if(teacher.getExamType()!=null){
            map.put("examType",String.valueOf(teacher.getExamType()));
        }
        if(teacher.getStatus()!=null){
            map.put("status",String.valueOf(teacher.getStatus()));
        }
        if(teacher.getSubjectId()!=null){
            map.put("subjectId",String.valueOf(teacher.getSubjectId()));
        }
        if(teacher.getPhone()!=null){
            map.put("phone",teacher.getPhone());
        }
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        Set<String> keySet = map.keySet();
        for(String key:keySet) {
            String value = map.get(key);
            formBodyBuilder.add(key,value);
        }

        return formBodyBuilder.build();
    }


    public static String postStatus(List<Long> pids,Integer status) {
        Map map=new HashMap();
        map.put("pids",pids);
        map.put("status",status);
        String string = null;
        try {
            string = new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String result=null;
        try {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    string);
            Request request = new Request.Builder().url(POST_Status_URL).post(requestBody).build();
            Response execute = okHttpClient.newCall(request).execute();
            result = execute.body().string();
        } catch (Exception e) {
            log.info("pids:{},status:{}",pids.toString(),status.toString());
            log.error(e.getMessage());
        }
        return result;
    }
}
