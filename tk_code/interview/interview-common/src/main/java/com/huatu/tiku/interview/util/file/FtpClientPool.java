package com.huatu.tiku.interview.util.file;

/**
 * Created by renwenlong on 2016/11/16.
 */

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ftp 客户端连接池
 * Created by shaojieyue
 * Created time 2016-10-21 10:22
 */

@Component
public class FtpClientPool {
    private static final Logger logger = LoggerFactory.getLogger(FtpClientPool.class);
    private static final String FTP_ADDRESS = "192.168.100.16";
    private static final String FTP_USER_NAME = "ftp";
    private static final String FTP_USER_PASS = "vhuatu_2013";
    GenericObjectPool<FTPClient> pool;

    public FtpClientPool() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(3);
        config.setMinIdle(3);
        config.setMaxTotal(5);
        config.setTestWhileIdle(true);
        config.setTimeBetweenEvictionRunsMillis(10 * 1000);//定期检查连接可用性
        config.setMaxWaitMillis(3000);//最大等待3s
        config.setTestOnBorrow(true);//在获取连接之前测试连接可用性
        pool = new GenericObjectPool<FTPClient>(new FtpClientFactory(), config);
    }

    public FTPClient getFTPClient() {
        for (int i = 0; i < 3; i++) {
            try {
                return pool.borrowObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void returnFTPClient(FTPClient ftpClient) {
        pool.returnObject(ftpClient);
    }


    class FtpClientFactory extends BasePooledObjectFactory<FTPClient> {

        /**
         * No-op.
         *
         * @param p ignored
         */
        @Override
        public void passivateObject(PooledObject<FTPClient> p) throws Exception {
            super.passivateObject(p);
        }

        /**
         * Creates an object instance, to be wrapped in a {@link PooledObject}.
         * <p>This method <strong>must</strong> support concurrent, multi-threaded
         * activation.</p>
         *
         * @return an instance to be served by the pool
         * @throws Exception if there is a problem creating a new instance,
         *                   this will be propagated to the code requesting an object.
         */
        @Override
        public FTPClient create() throws Exception {
            long stime = System.currentTimeMillis();
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(FTP_ADDRESS);
            logger.info("ftp connect time={}", System.currentTimeMillis() - stime);

            // After connection attempt, you should check the reply code to verify
            // success.
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                throw new RuntimeException("FTP server refused connection.");
            }

            long stime1 = System.currentTimeMillis();
            ftpClient.login(FTP_USER_NAME, FTP_USER_PASS);
            logger.info("ftp login time={}", System.currentTimeMillis() - stime1);

            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            return ftpClient;
        }

        /**
         * Wrap the provided instance with an implementation of
         * {@link PooledObject}.
         *
         * @param obj the instance to wrap
         * @return The provided instance, wrapped by a {@link PooledObject}
         */
        @Override
        public PooledObject<FTPClient> wrap(FTPClient obj) {
            return new DefaultPooledObject<FTPClient>(obj);
        }

        /**
         * This implementation always returns {@code true}.
         *
         * @param p ignored
         * @return {@code true}
         */
        @Override
        public boolean validateObject(PooledObject<FTPClient> p) {
            final FTPClient ftpClient = p.getObject();
            if (ftpClient == null) {
                return false;
            }
            try {
                //发送ping消息
                return ftpClient.sendNoOp();
            } catch (IOException e) {
                return false;
            }
        }

        /**
         * No-op.
         *
         * @param p ignored
         */
        @Override
        public void destroyObject(PooledObject<FTPClient> p) throws Exception {
            final FTPClient ftpClient = p.getObject();
            if (ftpClient == null) {
                return;
            }
            try {
                ftpClient.logout();
            } catch (Exception e) {
            }
            try {
                ftpClient.disconnect();
            } catch (Exception e) {
            }

        }
    }
}
