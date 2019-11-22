package com.huatu.hadoop.service;

@FunctionalInterface
public interface Converter<F, T> {

    T convertInt(F from);
}
