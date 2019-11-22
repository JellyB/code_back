package com.huatu.naga.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huatu.naga.dao.es.api.ExceptionDocumentDao;
import com.huatu.naga.dao.es.entity.ExceptionDocument;
import com.huatu.tiku.common.bean.report.ExceptionReportMessage;
import com.huatu.tiku.common.bean.report.ReportMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author hanchao
 * @date 2018/3/16 10:06
 */
@Component
public class ExceptionReportMessageHandler implements MessageHandler {
    @Autowired
    private ExceptionDocumentDao exceptionDocumentDao;

    @Override
    public boolean supports(ReportMessage message) {
        return message instanceof ExceptionReportMessage;
    }

    @Override
    public void handleMessage(ReportMessage message) {
        ExceptionReportMessage exceptionReportMessage = (ExceptionReportMessage) message;
        ExceptionDocument exceptionDocument = new ExceptionDocument();
        BeanUtils.copyProperties(exceptionReportMessage,exceptionDocument);
        exceptionDocument.setRequestHeaders(JSON.toJSONString(exceptionReportMessage.getRequestHeaders(), SerializerFeature.PrettyFormat));
        exceptionDocument.setTimestamp(new Date(exceptionReportMessage.getTimestamp()));
        exceptionDocumentDao.save(exceptionDocument);
    }
}
