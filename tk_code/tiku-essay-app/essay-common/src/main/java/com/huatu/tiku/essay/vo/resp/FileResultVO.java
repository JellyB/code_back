package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/16.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileResultVO {

    //文件地址
    private String pdfPath;

    //名称
    private String name;

    //地区名称
    private String areaName;

    //是否是批改详情
    private boolean corrected = false;


    //文件大小
    private String fileSize ;



}
