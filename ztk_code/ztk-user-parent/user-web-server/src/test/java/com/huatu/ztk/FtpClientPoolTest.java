package com.huatu.ztk;

import com.huatu.ztk.user.utils.FtpClientPool;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shaojieyue
 * Created time 2016-10-21 11:19
 */
public class FtpClientPoolTest {
    private static final Logger logger = LoggerFactory.getLogger(FtpClientPoolTest.class);
    public static final String AVATAR_FILE_BASE_BATH = "/var/www/cdn/images/vhuatu/avatars/bb.jpg";
    public static void main(String[] args) throws Exception {
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        FtpClientPool pool = new FtpClientPool();
        File file = new File("/home/shaojieyue/Pictures/aa.jpeg");
        FileInputStream fileInputStream = new FileInputStream(file);
        for (int i = 0; i < 100; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    final FTPClient ftpClient = pool.getFTPClient();
                    try {
                        ftpClient.storeFile(AVATAR_FILE_BASE_BATH,fileInputStream);
                        ftpClient.deleteFile(AVATAR_FILE_BASE_BATH);
                        System.out.println(System.currentTimeMillis());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        pool.returnFTPClient(ftpClient);
                    }
                }
            });

        }
    }
}
