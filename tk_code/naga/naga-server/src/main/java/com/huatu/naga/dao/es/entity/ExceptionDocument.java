package com.huatu.naga.dao.es.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;

/**
 * @author hanchao
 * @date 2018/1/23 13:12
 */
@Document(indexName = "exception-log.#{T(com.huatu.naga.util.MonthlyUtil).current()}", type = "web-exception-log")
@Mapping
@Data
public class ExceptionDocument {
    @Id
    private String id;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String application;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String host;
    private String exception;
    private String message;
    private String stacktrace;
    @Field(type = FieldType.Date, format = DateFormat.none)
    private Date timestamp;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String url;
    private String urlParameters;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String method;
    private String body;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String requestHeaders;
}
