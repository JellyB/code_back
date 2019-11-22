package com.huatu.tiku.essay.util;

import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @author create by jbzm on 2018年1月4日21:09:58
 */
public class GetAllParameter {
    /**
     * '
     * 利用泛型和反射在结合BeanUtils来完成对两对集合的封装
     *
     * @param list
     * @param lol
     * @param <T>
     * @param <D>
     * @return
     */
    public static <T, D> List<T> test(List<D> list, Class<T> lol) {
        List<T> tList = Lists.newLinkedList();
        for (D d : list) {
            T t = null;
            try {
                //通过反射多次new对象来向list中输入值
                t =lol.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            BeanUtils.copyProperties(d, t);
            tList.add(t);
        }
        return tList;
    }
}
