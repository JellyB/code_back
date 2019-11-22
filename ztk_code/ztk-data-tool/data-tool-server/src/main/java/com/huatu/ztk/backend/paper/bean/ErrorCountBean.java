package com.huatu.ztk.backend.paper.bean;

import com.huatu.ztk.question.bean.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 错题统计bean
 * Created by linkang on 3/6/17.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ErrorCountBean {
    private Question question; //试题对象
    private int errorCount; //错误计数
    private String moduleName; //模块名称
    private String typeName; //试题类型
}
