package com.huatu.tiku.interview.entity;

import com.huatu.tiku.interview.entity.template.TemplateData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-03 下午7:58
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WeChatTemplate {
    private String touser;

    private String template_id;

    private String url;

    private Map<String, TemplateData> data;
}
