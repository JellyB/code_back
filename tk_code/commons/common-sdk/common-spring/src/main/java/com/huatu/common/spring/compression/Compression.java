package com.huatu.common.spring.compression;

/**
 * @author hanchao
 * @date 2018/3/16 19:26
 */
public interface Compression {
    byte[] compress(byte [] bytes);
    byte[] decompress(byte [] bytes);
}
