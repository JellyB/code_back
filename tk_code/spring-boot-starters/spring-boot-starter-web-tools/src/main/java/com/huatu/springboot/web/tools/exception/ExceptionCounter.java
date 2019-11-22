package com.huatu.springboot.web.tools.exception;

import java.util.Collection;

/**
 * @author hanchao
 * @date 2018/1/16 9:40
 */
public interface ExceptionCounter {
    void add(Exception e);
    int count();
    int seconds();
    Collection<Exception> samples();
}
