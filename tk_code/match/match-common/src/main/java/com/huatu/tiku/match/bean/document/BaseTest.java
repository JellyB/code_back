package com.huatu.tiku.match.bean.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 基础测试 此处使用 huatu_ztk.test
 * Created by lijun on 2018/10/11
 */
@Document(collection = "test")
public class BaseTest {

    @Id
    private String id;

    private String name;
}
