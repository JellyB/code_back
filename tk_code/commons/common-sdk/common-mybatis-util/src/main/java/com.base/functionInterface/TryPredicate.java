package com.base.functionInterface;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Created by junli on 2018/1/4.
 */
public class TryPredicate {


    public static <T> Predicate<T> of(UncheckedPredicateFunction<T> predicateFunction) {
        Objects.requireNonNull(predicateFunction);
        return t -> {
            try {
                return predicateFunction.test(t);
            } catch (Exception exception) {
                throw new RuntimeException(exception.getMessage());
            }
        };
    }

    public static <T> Predicate<T> of(UncheckedPredicateFunction<T> predicateFunction, Boolean defaultValue) {
        Objects.requireNonNull(predicateFunction);
        return t -> {
            try {
                return predicateFunction.test(t);
            } catch (Exception exception) {
                return defaultValue;
            }
        };
    }

    @FunctionalInterface
    public interface UncheckedPredicateFunction<T> {
        Boolean test(T t) throws Exception;
    }

}
