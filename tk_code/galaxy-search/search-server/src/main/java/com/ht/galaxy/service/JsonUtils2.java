package com.ht.galaxy.service;


import com.ht.galaxy.common2.UserCountTerminal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class JsonUtils2 {

    public static Map<String,List> getResult(List list,String mode) throws Exception {
        ArrayList<Map<Object, Object>> lists = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();

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
                        hashMap.put(getType(Integer.toString(userCountTerminal.getTerminal())), userCountTerminal.getCount());
                    }
                }
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
            strings.add("安卓Ipad");
            strings.add("安卓");
            strings.add("苹果Ipad");
            strings.add("ios");
            strings.add("微信");
            strings.add("0");
            strings.add("其他");

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

    public static String getType(String s) throws Exception {

            if (s.equals("1")) {
                return "安卓";
            }else if (s.equals("2")){
                return "ios";
            }else if (s.equals("4")){
                return "安卓Ipad";
            }else if (s.equals("5")){
                return "苹果Ipad";
            }else if (s.equals("6")){
                return "微信";
            }else if (s.equals("0")){
                return "0";
            }else{
                return "其他";
            }

    }

}
