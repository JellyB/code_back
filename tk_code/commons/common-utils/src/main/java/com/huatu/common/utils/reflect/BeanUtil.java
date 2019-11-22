package com.huatu.common.utils.reflect;

import com.google.common.base.Preconditions;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 自省代替反射
 *
 * @author hanchao
 * @date 2017/10/7 14:18
 */
public class BeanUtil {
    /**
     * 注意java自省规则，前两个字母大小写判断的问题
     *
     * @param bean
     * @return
     */
    public static HashMap<String, Object> toMap(Object bean) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (null == bean) {
            return map;
        }
        Class<?> clazz = bean.getClass();
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors) {
            String propertyName = descriptor.getName();
            if (!"class".equals(propertyName)) {
                Method method = descriptor.getReadMethod();
                Object result;
                try {
                    result = method.invoke(bean);
                    if (null != result) {
                        map.put(propertyName, result);
                    } else {
                        map.put(propertyName, "");
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }


    /**
     * map 转化为 bean
     *
     * @param clazz
     * @param map
     * @return
     */
    public static <T> T fromMap(Class<T> clazz, Map<String, Object> map) {
        Object object = null;
        try {
            object = clazz.newInstance();
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                String propertyName = descriptor.getName();
                if (map.containsKey(propertyName)) {
                    Object value = map.get(propertyName);
                    Object[] args = new Object[1];
                    args[0] = value;
                    descriptor.getWriteMethod().invoke(object, args);
                }
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return (T) object;
    }


    /**
     * 复制所有source里面不为空的属性到target,注意基础类型的特殊性
     *
     * @param source
     * @param target
     * @param putNotNull 是否覆盖原有属性
     */
    public static void combine(Object source, Object target, boolean putNotNull) {
        if (source == null || target == null) {
            return;
        }
        Preconditions.checkArgument(target.getClass().isAssignableFrom(source.getClass()));
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                Method readMethod = descriptor.getReadMethod();
                Method writeMethod = descriptor.getWriteMethod();
                //只设置为null的
                if (putNotNull || readMethod.invoke(target) == null) {
                    Object value = readMethod.invoke(source);
                    if(value != null){
                        writeMethod.invoke(target,value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }


}
