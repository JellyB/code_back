package com.huatu.splider.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FbTeacherPaperService {

    @Autowired
    private RestTemplate restTemplate;

    public void getList(){
        //登录
//        String loginUrl = "http://fenbi.com/android/users/loginV2?phone=18701244063&password=OCMknXYi%2BvU8A%2Fl2oItGzCzuCSnOPOidorU6jq%2FkiLhKVgIgt61uOg8KecOZmgRhzfIu%2BF2%2Fi%2BC1bsCplGuZjqofdnY%2FA5brY1zQa4uMfTGXanNtmhjYe4e2n8EljTa5psvd84p%2FW5slQjTIrTRlNkSBFKfPNRKcVWqLtAvS98U%3D";
        String loginUrl = "http://fenbi.com/android/users/loginV2?phone=18701244063&password=OCMknXYi+vU8A/l2oItGzCzuCSnOPOidorU6jq/kiLhKVgIgt61uOg8KecOZmgRhzfIu+F2/i+C1bsCplGuZjqofdnY/A5brY1zQa4uMfTGXanNtmhjYe4e2n8EljTa5psvd84p/W5slQjTIrTRlNkSBFKfPNRKcVWqLtAvS98U=";
        ResponseEntity<Map> mapResponseEntity = restTemplate.postForEntity(loginUrl, null, Map.class);
        Map body = mapResponseEntity.getBody();

        String url = "http://fenbi.com/android/{courseType}/exercises/331560723?platform=android22&version=6.5.2&vendor=Sj360&app=gwy&deviceId=Hz/LT+W1bduuhMOPYjIFmA==&av=8&kav=3";
        String responseStr = restTemplate.getForObject(url, String.class, "jszgzhy");

        JSONObject data = JSONObject.parseObject(responseStr);
        JSONArray courses = data.getJSONArray("datas");
        System.out.println(courses);

    }

}
