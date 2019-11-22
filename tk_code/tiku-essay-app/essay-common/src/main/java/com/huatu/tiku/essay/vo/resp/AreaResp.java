package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/17
 */
@Data
public class AreaResp implements Serializable {

    private Long areaId;

    private String areaName;

    private List<AreaResp> areas;

}
