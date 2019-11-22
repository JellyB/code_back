package com.huatu.naga.test;

import com.huatu.common.test.BaseWebTest;
import com.huatu.naga.dao.es.api.ExceptionDocumentDao;
import com.huatu.naga.dao.es.entity.ExceptionDocument;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author hanchao
 * @date 2018/1/23 16:12
 */
public class TestEs extends BaseWebTest {
    @Autowired
    private ExceptionDocumentDao exceptionDocumentDao;

    @Test
    public void save() throws InterruptedException {
        ExceptionDocument exceptionDocument = new ExceptionDocument();
        exceptionDocument.setApplication("测试应用");
        exceptionDocument.setHost("192.168.100.1111");
        exceptionDocument.setException("exception");
        exceptionDocument.setMessage("message");
        exceptionDocument.setStacktrace("stacktrace");
        exceptionDocument.setTimestamp(new Date());
        exceptionDocumentDao.save(exceptionDocument);

        Thread.sleep(100000L);

        exceptionDocument = new ExceptionDocument();
        exceptionDocument.setApplication("测试应用");
        exceptionDocument.setHost("192.168.100.1111");
        exceptionDocument.setException("exception");
        exceptionDocument.setMessage("message");
        exceptionDocument.setStacktrace("stacktrace");
        exceptionDocument.setTimestamp(new Date());
        exceptionDocumentDao.save(exceptionDocument);
    }
}
