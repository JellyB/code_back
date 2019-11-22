package com.ht.base.exception;

import java.util.function.Supplier;

/**
 * @author zhengyi
 * @date 2018/10/24 6:14 PM
 * <p>
 * Supply an assert function
 * If false throw RunTime Exception
 **/
public class MyAssert {

    public static void BaseAssert(Supplier<Boolean> supplier, MyException myException) {
        if (!supplier.get()) {
            throw myException;
        }
    }

    public static <T extends RuntimeException> void RunTimeAssert(Supplier<Boolean> supplier, T exception){
        if (!supplier.get()) {
            throw exception;
        }
    }

}