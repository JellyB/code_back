package com.huatu.tiku.match.initTest;

import com.huatu.tiku.match.enums.util.EnumCommon;
import com.huatu.tiku.match.enums.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lijun on 2018/10/12
 */
public class EnumTest {

    public static void main(String[] args) {
        int value = EnumUtil.valueOf("5", TestEnum.class);
        System.out.println(value);


        String s = EnumUtil.valueOf(4, TestEnum.class);
        System.out.println(s);

        AtomicInteger i = excptionTest();
        System.out.println(i);
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class Test{
        private int i;
    }
    private static AtomicInteger excptionTest() {
        Test test = new Test();
        test.setI(0);
        AtomicInteger i = new AtomicInteger(0);
        try{
            i.set(Integer.parseInt("122s"));
            return i;
        }catch (Exception e){
            e.printStackTrace();
            i.set(1);
            return i;
        }finally {
            i.set(2);
            Test test1 = new Test(3);
        }
    }

    @AllArgsConstructor
    enum TestEnum implements EnumCommon{
        TEST_ONE(1,"2"),
        TEST_TWO(2,"3")
        ;
        private int code;
        private String value;


        @Override
        public int getKey() {
            return this.code;
        }

        @Override
        public String getValue() {
            return this.value;
        }

        @Override
        public EnumCommon getDefault() {
            return TEST_ONE;
        }

    }
}
