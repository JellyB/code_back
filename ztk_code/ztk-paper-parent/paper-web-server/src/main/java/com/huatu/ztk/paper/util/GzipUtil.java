/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.huatu.ztk.paper.util;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {  
  public static String compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return Base64.encodeBase64String(out.toByteArray());
    }
    
    public static String uncompress(String str) throws IOException {
        if (str == null) {
            return null;
        }
        if (Base64.isBase64(str)) {
            byte[] decodedStr = Base64.decodeBase64(str);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(decodedStr);
            GZIPInputStream gunzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = gunzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString("UTF-8");
        } else {
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        final String uncompress = GzipUtil.uncompress("H4sIAAAAAAAAAF2QMQ6DMAxF7+KZSo7j2AlbKR26dWgPgFRUMcBAR8Tda1BCpW5PL87Xtxd4fvr51kINjigQSoAK7t3cjVAvII69t7fGZFB00fiysxM0bo1VFDn7hJ6c8XmfYcLCyVEsOaIBC5tPJVNUJGcGJR8Ki0b85bvSJyYOePQRProF8VufjL6gbZdRba2MIon2f2sFTf8epscw9tsxWDSxR2Wbuk6vP+3JtF2u+BMTrF/Kq49ITAEAAA==");
        System.out.println(uncompress);
    }
}  