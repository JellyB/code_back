package com.huatu.ztk.pc.service;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

/**
 * Created by huangqp on 2018\6\3 0003.
 */
public class Test1 {
    @Test
    public void test(){
        List<Integer> list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17);
        System.out.println(list.subList(0,10));
        System.out.println(list.subList(10,list.size()));
    }
}

