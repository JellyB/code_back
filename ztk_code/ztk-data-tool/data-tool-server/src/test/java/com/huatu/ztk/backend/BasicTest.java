package com.huatu.ztk.backend;

import com.huatu.ztk.commons.JsonUtil;
import org.junit.Test;

import java.util.List;

/**
 * Created by huangqp on 2018\3\14 0014.
 */
public class BasicTest {
    @Test
    public void test(){
        String object = "[{\"_id\":100100196,\"num_tutorial\":22.0},{\"_id\":100100186,\"num_tutorial\":147.0},{\"_id\":1006,\"num_tutorial\":1.0},{\"_id\":100100191,\"num_tutorial\":123.0},{\"_id\":100100189,\"num_tutorial\":337.0},{\"_id\":100100187,\"num_tutorial\":123.0},{\"_id\":100100262,\"num_tutorial\":80.0},{\"_id\":100100193,\"num_tutorial\":255.0},{\"_id\":24,\"num_tutorial\":1047.0},{\"_id\":1005,\"num_tutorial\":1064.0},{\"_id\":1000,\"num_tutorial\":5895.0},{\"_id\":100100167,\"num_tutorial\":110.0},{\"_id\":100100263,\"num_tutorial\":1556.0},{\"_id\":100100190,\"num_tutorial\":261.0},{\"_id\":1009,\"num_tutorial\":604.0},{\"_id\":1017,\"num_tutorial\":1682.0},{\"_id\":1003,\"num_tutorial\":515.0},{\"_id\":100100221,\"num_tutorial\":1.0},{\"_id\":2,\"num_tutorial\":33613.0},{\"_id\":-1,\"num_tutorial\":255.0},{\"_id\":1,\"num_tutorial\":137287.0},{\"_id\":100100188,\"num_tutorial\":125.0},{\"_id\":1019,\"num_tutorial\":103.0},{\"_id\":1018,\"num_tutorial\":2240.0},{\"_id\":100100177,\"num_tutorial\":2142.0},{\"_id\":100100126,\"num_tutorial\":11226.0},{\"_id\":3,\"num_tutorial\":4776.0},{\"_id\":100100176,\"num_tutorial\":3324.0},{\"_id\":100100192,\"num_tutorial\":257.0},{\"_id\":100100145,\"num_tutorial\":391.0},{\"_id\":1020,\"num_tutorial\":268.0},{\"_id\":14,\"num_tutorial\":14.0},{\"_id\":100100175,\"num_tutorial\":1254.0},{\"_id\":100100259,\"num_tutorial\":18.0}]";
        System.out.println(JsonUtil.toJson(JsonUtil.toObject(object,List.class)));
    }
}
