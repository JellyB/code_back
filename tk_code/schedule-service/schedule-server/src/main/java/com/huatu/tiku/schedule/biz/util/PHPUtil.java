package com.huatu.tiku.schedule.biz.util;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.dto.php.PHPResultDto;
import com.huatu.tiku.schedule.biz.vo.php.PHPUpdateTeacherVo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wangjian
 **/
@Slf4j
public class PHPUtil {

    //
    private static final String GET_URL = "http://api.huatu.com/lumenapi/v4/common/teacher/list";//正式地址
//    private static final String GET_URL = "http://testapi.huatu.com/lumenapi/v4/common/teacher/list";//测试地址

//    private static final String POST_Status_URL = "http://testapi.huatu.com/lumenapi/v4/common/teacher/status";//测试地址

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(2000, TimeUnit.MILLISECONDS)
            .writeTimeout(5000, TimeUnit.MILLISECONDS).readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectionPool(new ConnectionPool(100, 5 * 60 * 1000, TimeUnit.MILLISECONDS)).followRedirects(true)// 跟踪重定向
            .build();

    /**
     * get方法 拉取php教师
     */
    public static String get() {
        Request request = new Request.Builder().url(GET_URL).get().build();
        String result = null;
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
     *
     * @param teacher vo
     * @return 返回值
     */
    public static Long post(String url,Teacher teacher) {
        String result = null;
        try {
            FormBody formBody = getBody(teacher);
            Request request = new Request.Builder().url(url).post(formBody).build();
            Response execute = okHttpClient.newCall(request).execute();
            result = execute.body().string();
            PHPResultDto dto = JSON.parseObject(result, PHPResultDto.class);
            if(dto.getCode().equals(10000)&&dto.getMsg().equals("success")){  //成功时
                Long pid = dto.getData().getPid();
                if(pid!=null){
                    return pid;
                }
            }
        } catch (Exception e) {
            log.info("id:{},pid:{},name:{},phone:{},examType:{},status:{},subjectId:{}", teacher.getId(), teacher.getPid(),
                    teacher.getName(), teacher.getPhone(), teacher.getTeacherType(), teacher.getStatus(), teacher.getSubjectId());
            log.error(e.getMessage());
        }
        return null;
    }

    //封装参数
    private static FormBody getBody(Teacher bean) {
        PHPUpdateTeacherVo teacher = new PHPUpdateTeacherVo(bean);
        HashMap<String, String> map = Maps.newHashMap();
        if (teacher.getPid() != null) {
            map.put("pid", String.valueOf(teacher.getPid()));
        }
        if (StringUtils.isNotBlank(teacher.getName())) {
            map.put("name", teacher.getName());
        }
        if (teacher.getStatus() != null) {
            map.put("status", String.valueOf(teacher.getStatus()));
        }
        if (StringUtils.isNotBlank(teacher.getPhone())) {
            map.put("phone", teacher.getPhone());
        }
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            String value = map.get(key);
            formBodyBuilder.add(key, value);
        }

        return formBodyBuilder.build();
    }


    /**
     * 批量修改状态
     */
    public static void postStatus(String url, Collection<Long> pids, Integer status) {
        if(null==pids||pids.isEmpty()){
            return;
        }
        Map<String,Object> map = Maps.newHashMap();
        map.put("pids", pids);
        map.put("status", status);
        String string = null;
        try {
            string = new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        try {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    string);
            Request request = new Request.Builder().url(url).post(requestBody).build();
            Response execute = okHttpClient.newCall(request).execute();
            String result=execute.body().string();
            log.info(result);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("pids:{},status:{}", pids.toString(), status.toString());
    }
}
