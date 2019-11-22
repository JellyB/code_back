package com.huatu.common.spring.compression;

import org.xerial.snappy.Snappy;

import java.io.IOException;

/**
 * @author hanchao
 * @date 2018/3/16 19:27
 */
public class SnappyCompression implements Compression {
    @Override
    public byte[] compress(byte[] bytes) {
        if(bytes == null){
            return null;
        }
        try {
            return Snappy.compress(bytes);
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if(bytes == null){
            return null;
        }
        try {
            return Snappy.uncompress(bytes);
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
