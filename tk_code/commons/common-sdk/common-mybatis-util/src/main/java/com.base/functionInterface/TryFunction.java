package com.base.functionInterface;

import com.base.utils.StringUtil;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by junli on 2018/1/4.
 */
public class TryFunction {


    public static <T, R> Function<T, R> of(UncheckedFunction<T, R> mapper) {
        return of(mapper, null);
    }

    public static <T, R> Function<T, R> of(UncheckedFunction<T, R> mapper, R defaultValue) {
        Objects.requireNonNull(mapper);
        return t -> {
            try {
                return mapper.apply(t);
            } catch (Exception ex) {
                if (StringUtil.checkObjectNull(defaultValue)) {
                    throw new RuntimeException(ex.getMessage());
                } else {
                    System.err.println("ex = " + ex.getMessage());
                    return defaultValue;
                }
            }
        };
    }

    @FunctionalInterface
    public interface UncheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

}
