package com.huatu.tiku.schedule.biz.vo.Schedule;

import lombok.Data;
import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
public class PageVo implements Serializable {

    private static final long serialVersionUID = 1524680315644972407L;

    private Object content;
    private Boolean first;
    private Boolean last;
    private Integer number;
    private Integer numberOfElements;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

    public PageVo(){
        this.first=true;
        this.last=false;
        this.number=0;
        this.numberOfElements=0;
        this.size=20;
        this.totalElements=0L;
        this.totalPages=0;
    }

}
