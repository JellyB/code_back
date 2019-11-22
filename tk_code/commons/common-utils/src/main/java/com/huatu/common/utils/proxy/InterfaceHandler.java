package com.huatu.common.utils.proxy;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/12 22:13
 */
public interface InterfaceHandler {
    Class<?> generateClass(Class<?> inter,Map<Class,String> resultMapping) throws Exception;
}
