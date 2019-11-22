package com.huatu.common.utils.cache;

import java.io.Serializable;

/**
 * 防止缓存击穿
 * @author hanchao
 * @date 2017/9/13 10:40
 */
public class NullHolder implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final NullHolder DEFAULT = new NullHolder();

    private byte placeHolder = 0;
    private NullHolder(){

    }

    public byte getPlaceHolder() {
        return placeHolder;
    }

}
