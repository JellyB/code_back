package com.huatu.ztk.backend.paperUpload.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lenovo on 2017/5/17.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperUploadError {
    private String errorMsg;
    private String errorType;
    private String location;
    private String floor;
    private Object errorFlag;
}
