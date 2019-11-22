package com.huatu.ztk;

import com.google.common.io.Files;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.common.UserErrors;
import com.huatu.ztk.user.utils.FtpClientPool;
import com.huatu.ztk.user.utils.UcenterUtils;
import com.huatu.ztk.user.utils.etag.Etag;
import com.sun.jdi.PathSearchingVirtualMachine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by shaojieyue
 * Created time 2016-10-21 11:47
 */
public class ImageTest {
    public static final String AVATAR_FILE_BASE_BATH = "/var/www/cdn/images/vhuatu/avatars/";
    public static final String AVATAR_BASE_URL = "http://tiku.huatu.com/cdn/images/vhuatu/avatars/";
    private static String makeSavePath(String fileName) {
        return AVATAR_FILE_BASE_BATH + fileName.charAt(0);
    }

    private static String makeUrl(String fileName) {
        return AVATAR_BASE_URL + fileName.charAt(0) + "/" + fileName;
    }
    public static void main(String[] args) throws Exception {
        final String s = StringUtils.rightPad("2.2".replaceAll("\\.", ""),3, '0');
        System.out.println(s);

    }
}
