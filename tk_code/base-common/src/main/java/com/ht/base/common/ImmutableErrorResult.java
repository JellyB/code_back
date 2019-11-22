package com.ht.base.common;


/**
 * @author hanchao
 * @date 2017/8/24 9:44
 */
public class ImmutableErrorResult extends ErrorResult {

    public ImmutableErrorResult(){

    }
    public ImmutableErrorResult(int code,String message){
        super(code, message);
    }

    @Override
    @Deprecated
    public void setMessage(String message) {

    }
    @Override
    @Deprecated
    public void setData(Object data){

    }
}
