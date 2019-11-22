package com.ht.galaxy.service;

import com.alibaba.fastjson.JSONObject;
import com.ht.galaxy.common.UserCountArea;
import com.ht.galaxy.common.UserCountSubject;
import com.ht.galaxy.common.UserCountTerminal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class JsonUtils {

    public static void main(String[] args) throws Exception {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("123","gao");
        hashMap.put("456","yu");
        hashMap.put("789","chao");
        System.out.println(JSONObject.toJSONString(hashMap));
        HashMap<String, String> hashMap2 = new HashMap<>();
        hashMap2.put("123","li");
        hashMap2.put("456","yu");
        hashMap2.put("789","gang");
        System.out.println(JSONObject.toJSONString(hashMap2));
        ArrayList<Object> list = new ArrayList<>();
        list.add(hashMap);
        list.add(hashMap2);
        System.out.println(JSONObject.toJSONString(list));
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
        System.out.println(format.format(date));

    }

    public static Map<String,List> getResult(List list,String type,String mode) throws Exception {
        ArrayList<Map<Object, Object>> lists = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();
        if (type.equals("area")) {
            for (Object o : list) {
                UserCountArea userCountArea = (UserCountArea) o;
                map.put(userCountArea.getTime(),"");
            }
            for (String s : map.keySet()) {
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put("日期",getTime(s,mode));
                for (Object o : list) {
                    UserCountArea userCountArea = (UserCountArea) o;
                    if (userCountArea.getTime().equals(s)) {
                        hashMap.put(getType(Integer.toString(userCountArea.getArea()),type),userCountArea.getCount());
                    }
                }
                lists.add(hashMap);
            }
        }else if (type.equals("subject")) {
            for (Object o : list) {
                UserCountSubject userCountSubject = (UserCountSubject) o;
                map.put(userCountSubject.getTime(), "");
            }
            for (String s : map.keySet()) {
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put("日期", getTime(s,mode));
                for (Object o : list) {
                    UserCountSubject userCountSubject = (UserCountSubject) o;
                    if (userCountSubject.getTime().equals(s)) {
                        hashMap.put(getType(Integer.toString(userCountSubject.getSubject()),type), userCountSubject.getCount());
                    }
                }
                lists.add(hashMap);
            }
        }else if (type.equals("terminal")) {
            for (Object o : list) {
                UserCountTerminal userCountTerminal = (UserCountTerminal) o;
                map.put(userCountTerminal.getTime(), "");
            }
            for (String s : map.keySet()) {
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put("日期", getTime(s,mode));
                for (Object o : list) {
                    UserCountTerminal userCountTerminal = (UserCountTerminal) o;
                    if (userCountTerminal.getTime().equals(s)) {
                        hashMap.put(getType(Integer.toString(userCountTerminal.getTerminal()),type), userCountTerminal.getCount());
                    }
                }
                lists.add(hashMap);
            }
        }
        lists.sort((o1, o2) -> {
            String s1 = o1.get("日期").toString();
            String s2 = o2.get("日期").toString();
           return s1.compareTo(s2);
        });

        HashMap<String, List> resMap = new HashMap<>();

        ArrayList<String> strings = new ArrayList<>();
        if (type.equals("terminal")){
            strings.add("日期");
            strings.add("安卓Ipad");
            strings.add("安卓");
            strings.add("苹果Ipad");
            strings.add("ios");
            strings.add("微信");
            strings.add("其他");
        }else if (type.equals("area")){
            strings.add("日期");
            strings.add("全国");
            strings.add("未知");
            strings.add("北京");
            strings.add("天津");
            strings.add("河北");
            strings.add("山西");
            strings.add("内蒙古");
            strings.add("辽宁");
            strings.add("吉林");
            strings.add("黑龙江");
            strings.add("江苏");
            strings.add("浙江");
            strings.add("安徽");
            strings.add("福建");
            strings.add("江西");
            strings.add("山东");
            strings.add("河南");
            strings.add("湖北");
            strings.add("湖南");
            strings.add("广东");
            strings.add("广西");
            strings.add("海南");
            strings.add("重庆");
            strings.add("四川");
            strings.add("贵州");
            strings.add("云南");
            strings.add("西藏");
            strings.add("陕西");
            strings.add("甘肃");
            strings.add("青海");
            strings.add("宁夏");
            strings.add("新疆");
            strings.add("其他");
        }else {
            strings.add("日期");
            strings.add("未知");
            strings.add("公务员");
            strings.add("公基");
            strings.add("事业单位");
            strings.add("遴选");
            strings.add("军转");
            strings.add("400");
            strings.add("医疗");
            strings.add("金融");
            strings.add("其他");
            strings.add("公安专业科目");
            strings.add("教育综合知识");
            strings.add("电工类专业本科");
            strings.add("少余");
        }

        resMap.put("columns",strings);
        resMap.put("rows",lists);

        return resMap;

    }

    public static Map<String,List> getTerminal(List list,String mode) throws Exception {

        ArrayList<Map<Object, Object>> lists = new ArrayList<>();
        for (Object o : list) {
            UserCountTerminal userCountTerminal = (UserCountTerminal) o;
            HashMap<Object, Object> hashMap = new HashMap<>();
            hashMap.put("日期",getTime(userCountTerminal.getTime(),mode));
            hashMap.put("人数",userCountTerminal.getCount());
            hashMap.put("终端",userCountTerminal.getTerminal());
            lists.add(hashMap);
        }

        lists.sort((o1, o2) -> {
            String s1 = o1.get("日期").toString();
            String s2 = o2.get("日期").toString();
            return s1.compareTo(s2);
        });

        HashMap<String, List> resMap = new HashMap<>();

        ArrayList<String> strings = new ArrayList<>();
        strings.add("日期");
        strings.add("人数");

        resMap.put("columns",strings);
        resMap.put("rows",lists);

        return resMap;
    }

    public static Map<String,List> getEvent(List list,String mode) throws Exception {

        ArrayList<Map<Object, Object>> lists = new ArrayList<>();
        for (Object o : list) {
            JSONObject jsonObject = JSONObject.parseObject(o.toString());
            HashMap<Object, Object> hashMap = new HashMap<>();
            hashMap.put("日期",getTime(jsonObject.getString("time"),mode));
            hashMap.put("人数",jsonObject.getString("count"));
            lists.add(hashMap);
        }

        lists.sort((o1, o2) -> {
            String s1 = o1.get("日期").toString();
            String s2 = o2.get("日期").toString();
            return s1.compareTo(s2);
        });

        HashMap<String, List> resMap = new HashMap<>();

        ArrayList<String> strings = new ArrayList<>();
        strings.add("日期");
        strings.add("人数");

        resMap.put("columns",strings);
        resMap.put("rows",lists);

        return resMap;
    }

    public static String getTime(String s,String mode) throws ParseException {
        if (mode.equals("day")) {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date date = format.parse(s);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy年MM月dd日");
            return format1.format(date);
        }else if (mode.equals("month")){
            SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
            Date date = format.parse(s);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy年MM月");
            return format1.format(date);
        } else if (mode.equals("hour")) {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
            Date date = format.parse(s);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy年MM月dd日HH时");
            return format1.format(date);
        }else {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date date = format.parse(s);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy年MM月dd日");
            return format1.format(date);
        }
    }

    public static String getType(String s,String type) throws Exception {

        if (type.equals("area")) {
            switch (s) {
                case "-9": return "全国";
                case "-1": return "未知";
                case "1": return "北京";
                case "21": return "天津";
                case "41": return "河北";
                case "225": return "山西";
                case "356": return "内蒙古";
                case "471": return "辽宁";
                case "586": return "吉林";
                case "656": return "黑龙江";
                case "802": return "上海";
                case "823": return "江苏";
                case "943": return "浙江";
                case "1045": return "安徽";
                case "1168": return "福建";
                case "1263": return "江西";
                case "1374": return "山东";
                case "1532": return "河南";
                case "1709": return "湖北";
                case "1826": return "湖南";
                case "1963": return "广东";
                case "2106": return "广西";
                case "2230": return "海南";
                case "2257": return "重庆";
                case "2299": return "四川";
                case "2502": return "贵州";
                case "2600": return "云南";
                case "2746": return "西藏";
                case "2827": return "陕西";
                case "2945": return "甘肃";
                case "3046": return "青海";
                case "3098": return "宁夏";
                case "3125": return "新疆";
                default: return "其他";
            }
        }else if (type.equals("subject")) {
            switch (s) {
                case "-1": return "未知";
                case "1" : return "公务员";
                case "2" : return "公基";
                case "3" : return "事业单位";
                case "41" : return "遴选";
                case "42" : return "军转";
                case "400" : return "400";
                case "410" : return "医疗";
                case "420" : return "金融";
                case "430" : return "其他";
                case "100100175" : return "公安专业科目";
                case "100100262" : return "教育综合知识";
                case "100100263" : return "电工类专业本科";
                default: return "少余";
            }
        }else {
            if (s.equals("1")) {
                return "安卓";
            }else if (s.equals("2")){
                return "ios";
            }else if (s.equals("4")){
                return "安卓Ipad";
            }else if (s.equals("5")){
                return "苹果Ipad";
            }else if (s.equals("34")){
                return "微信";
            }else {
                return "其他";
            }
        }
    }

}
