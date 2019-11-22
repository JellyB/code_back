package com.huatu.tiku.request.paper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.entity.teacher.PaperActivity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/8/15
 * @描述
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SelectActivityReq extends PaperActivity {

    /**
     * 实体卷名称
     */
    private String paperEntityName;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 活动类型名称
     */
    private String typeName;

    /**
     * 查看解析名称
     */
    private String looKParseName;

    /**
     * 实体卷分数
     */
    private Double entityScore;

}
