package com.huatu.hadoop.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConditionDTO {

    private String type;
    private Integer[] values;
    private String symbol;


    public static Integer[] getSecId(Integer fid) {
        String s = "{\"data\":[{\"id\":1,\"name\":\"公务员\",\"childrens\":[{\"id\":1,\"name\":\"行测\",\"childrens\":[],\"tiku\":false},{\"id\":14,\"name\":\"申论\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":3,\"name\":\"事业单位\",\"childrens\":[{\"id\":2,\"name\":\"公基\",\"childrens\":[],\"tiku\":false},{\"id\":24,\"name\":\"综应\",\"childrens\":[],\"tiku\":false},{\"id\":200100054,\"name\":\"职测-联考A类\",\"childrens\":[],\"tiku\":false},{\"id\":200100055,\"name\":\"职测-联考B类\",\"childrens\":[],\"tiku\":false},{\"id\":200100056,\"name\":\"职测-联考C类\",\"childrens\":[],\"tiku\":false},{\"id\":200100057,\"name\":\"职测-非联考\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":200100045,\"name\":\"教师招聘\",\"childrens\":[{\"id\":100100262,\"name\":\"教育综合知识\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":200100048,\"name\":\"教师资格证-小学\",\"childrens\":[{\"id\":200100049,\"name\":\"综素\",\"childrens\":[],\"tiku\":false},{\"id\":200100051,\"name\":\"教知\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":200100053,\"name\":\"教师资格证-中学\",\"childrens\":[{\"id\":200100050,\"name\":\"综素\",\"childrens\":[],\"tiku\":false},{\"id\":200100052,\"name\":\"教知\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":200100047,\"name\":\"招警考试\",\"childrens\":[{\"id\":100100175,\"name\":\"公安专业科目\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":41,\"name\":\"公遴选\",\"childrens\":[{\"id\":100100267,\"name\":\"公遴选\",\"childrens\":[],\"tiku\":false}],\"tiku\":false},{\"id\":42,\"name\":\"军转\",\"childrens\":[{\"id\":100100268,\"name\":\"军转\",\"childrens\":[],\"tiku\":false}],\"tiku\":false},{\"id\":200100060,\"name\":\"军队文职\",\"childrens\":[{\"id\":200100063,\"name\":\"军队文职\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":200100064,\"name\":\"三支一扶\",\"childrens\":[{\"id\":200100065,\"name\":\"三支一扶\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":43,\"name\":\"国家电网\",\"childrens\":[{\"id\":100100263,\"name\":\"电工类专业本科\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":200100000,\"name\":\"医疗\",\"childrens\":[{\"id\":410,\"name\":\"医疗\",\"childrens\":[],\"tiku\":false}],\"tiku\":false},{\"id\":200100002,\"name\":\"金融\",\"childrens\":[{\"id\":420,\"name\":\"金融\",\"childrens\":[],\"tiku\":false}],\"tiku\":false},{\"id\":100100633,\"name\":\"考研\",\"childrens\":[{\"id\":100100634,\"name\":\"考研\",\"childrens\":[],\"tiku\":false}],\"tiku\":false},{\"id\":200100046,\"name\":\"其他\",\"childrens\":[{\"id\":430,\"name\":\"其他\",\"childrens\":[],\"tiku\":false}],\"tiku\":false}],\"code\":1000000}";

        JSONObject j = JSON.parseObject(s);
        JSONArray data = j.getJSONArray("data");
        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {

            JSONObject jo = data.getJSONObject(i);
            Integer id = jo.getInteger("id");
            if (id.intValue() == fid.intValue()) {
                JSONArray childrens = jo.getJSONArray("childrens");
                for (int k = 0; k < childrens.size(); k++) {
                    JSONObject jo_1 = childrens.getJSONObject(k);
                    Integer id1 = jo_1.getInteger("id");
                    l.add(id1);
                }
                break;
            }

        }
        Integer[] c = new Integer[1];
        return l.toArray(c);
    }

    public static void main(String[] args) {
        Integer[] arr = {1, 3};
        List<Integer> list = new ArrayList<>();
        for (Integer i : arr) {
            Integer[] irr = ConditionDTO.getSecId(i);
            list.addAll(Arrays.asList(irr));
        }
        System.out.println(list);
    }


}
