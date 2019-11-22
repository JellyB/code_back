package com.huatu.ztk.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.course.bean.NetSchoolResponse;
import com.huatu.ztk.course.utils.Crypt3Des;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by linkang on 11/30/16.
 */
public class EncryptTest {

    @Test
    public void testEncrypt() {
//        String param = "com="+type+"&num="+no;
        //加密
        String plainText = "com=yuantong&num=809274414413";
        System.out.println(Crypt3Des.encryptMode(plainText));

        //解密
        String cipherText = "0E47280C909DB5FB97BF80811B21C739";
        System.out.println(Crypt3Des.decryptMode(cipherText));
    }


    @Test
    public void jsonTest() throws Exception{
        String json = "{\"code\":0,\"msg\":\"\\u6682\\u65e0\\u4fe1\\u606f\",\"data\":[]}";
        ObjectMapper objectMapper = new ObjectMapper();

        NetSchoolResponse response = JsonUtil.toObject(json, NetSchoolResponse.class);
        System.out.println(response.getCode());
        System.out.println(response.getMsg());
        System.out.println(response.getData());
    }

    @Test
    public void readFile() throws Exception{
        String json = FileUtils.readFileToString(new File("abc.json"));

        Map dataMap = JsonUtil.toMap(json);

        ArrayList<Map> result = (ArrayList<Map>)dataMap.get("result");

        ArrayList<Map> newResult = new ArrayList<>();
        for (Map map1 : result) {
            if (map1.get("NetClassId").equals("56078")) {
                newResult.add(map1);
            }
        }
        dataMap.put("result", newResult);

        System.out.println(dataMap);
    }


    @Test
    public void readFile2() throws Exception{
        String json = FileUtils.readFileToString(new File("ios_audit_book_list.json"));

        Map dataMap = JsonUtil.toMap(json);

        ArrayList<Map> result = (ArrayList<Map>)dataMap.get("result");

        List<Integer> bookIds = new ArrayList<>();
        for (Map map : result) {
            bookIds.add(Integer.valueOf(map.get("rid").toString()));
        }
        System.out.println(bookIds);
    }
}
