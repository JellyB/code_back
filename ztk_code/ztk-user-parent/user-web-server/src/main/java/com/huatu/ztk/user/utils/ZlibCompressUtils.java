package com.huatu.ztk.user.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;

/**
 * Created by linkang on 10/11/16.
 */
public class ZlibCompressUtils {


    public static String compress(String content) {
        byte[] data = content.getBytes();
        byte[] output = new byte[0];
        Deflater compresser = new Deflater();

        compresser.reset();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        compresser.end();

        byte[] ret = Base64.encodeBase64(output);
        return new String(ret);
    }


    public static String uncompress(String content) {
        byte[] zbytes = Base64.decodeBase64(content.getBytes());
        String unzipped = null;
        try {
            byte[] input = new byte[zbytes.length + 1];
            System.arraycopy(zbytes, 0, input, 0, zbytes.length);
            input[zbytes.length] = 0;
            ByteArrayInputStream bin = new ByteArrayInputStream(input);
            InflaterInputStream in = new InflaterInputStream(bin);
            ByteArrayOutputStream bout = new ByteArrayOutputStream(512);
            int b;
            while ((b = in.read()) != -1) {
                bout.write(b);
            }
            bout.close();
            unzipped = bout.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return unzipped;
    }
}
