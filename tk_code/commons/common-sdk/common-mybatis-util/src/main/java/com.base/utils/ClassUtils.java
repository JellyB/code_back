package com.base.utils;

/**
 * Created by junli on 2017/9/19.
 */
public class ClassUtils {

    /**
     * 判断是否为代理对象
     *
     * @param clazz
     * @return
     */
    public static boolean isProxy(Class<?> clazz) {
        if (clazz != null) {
            for (Class<?> cls : clazz.getInterfaces()) {
                String interfaceName = cls.getName();
                if (interfaceName.equals("net.sf.cglib.proxy.Factory") //cglib
                        || interfaceName.equals("org.springframework.cglib.proxy.Factory")
                        || interfaceName.equals("javassist.util.proxy.ProxyObject") //javassist
                        || interfaceName.equals("org.apache.ibatis.javassist.util.proxy.ProxyObject")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取当前对象的class
     *
     * @param clazz
     * @return
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        return isProxy(clazz) ? clazz.getSuperclass() : clazz;
    }

}
