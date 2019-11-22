package com.huatu.ztk.commons;


/**
 * Created by shaojieyue
 * Created time 2016-07-26 11:09
 */
public class JsonUtilTest {
    public static void main(String[] args) {
        String ss = "<p>当前，各国政治、经济和社会<span style=\"text-decoration:underline;\">不知道</span>不同，历史文化<span style=\"text-decoration:underline;\">     </span>不同，发展<span style=\"text-decoration:underline;\">     </span>也不尽一致。<br />填入划横线处最恰当的一项是（）。</p>";
        System.out.println(ss.replaceAll(" "," ").replaceAll("<span style=\"text-decoration:underline;\">[\\s]+</span>","_____"));
    }


}
