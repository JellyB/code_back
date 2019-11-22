package com.huatu.tiku.position.biz.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangjian
 **/
@Data
public class PositionInfoDto implements Serializable{

    private static final long serialVersionUID = -5009972880275348232L;

    private Double start;

    private List<String> label;

}
