package com.huatu.one.biz.model;

import com.huatu.one.base.model.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 字典
 *
 * @author geek-s
 * @date 2019-09-04
 */
@Getter
@Setter
@ToString
public class Version extends BaseModel {

    /**
     * 审核版本
     */
    private String version;
}
