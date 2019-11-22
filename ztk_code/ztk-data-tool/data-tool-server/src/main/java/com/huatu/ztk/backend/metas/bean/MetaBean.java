package com.huatu.ztk.backend.metas.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.ws.soap.MTOM;

/**
 * Created by huangqp on 2018\4\27 0027.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaBean {
    private long userId;
    private double score;
    private String location;
    private String nick;
    private String phone;
}
