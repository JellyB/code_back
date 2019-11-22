package com.huatu.tiku.schedule.biz.service.intelligence;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import org.apache.catalina.User;

import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 */
public class TestClass {

    public static void main(String[] args) {
        testClone();



        test1();


    }

    private static void testClone() {
        List<Integer> list = Lists.newArrayList();
        list.add(1);
        list.add(2);
        List<Integer> list1 = list;
        list.remove(1);
        for(Integer num: list1){
            System.out.println(num);
        }

        List<Teacher> teachers = Lists.newArrayList();

        Teacher teacher = new Teacher();
        teacher.setName("laobo");
        Teacher teacher1 = new Teacher();
        teacher1.setName("laobo1");
        Teacher teacher2 = new Teacher();
        teacher2.setName("laobo2");
        teachers.add(teacher);
        teachers.add(teacher1);
        teachers.add(teacher2);

        List<Teacher> teachers1 = Lists.newArrayList();
        teachers1.addAll(teachers);

        teacher.setName("lalala");
        teachers.remove(teacher);
        System.out.println(teachers1.get(0).getName());
    }

    public static void test1(){
        String str = "fjd789klsd908434jk#$$%%^38488545";
        System.out.println("替换之前的字符串：" + str);
        String substr = str.replaceAll("[^0-9]", "");
        System.out.println("替换之后的字符串：" + substr);

    }

}
