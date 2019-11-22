package com.huatu.tiku.interview.entity.template;


import com.huatu.tiku.interview.entity.result.ResultState;
import lombok.Data;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/12 16:31
 * @Description
 */
@Data
public class TemplateMsgResult extends ResultState {
    /**
     *
     */
    private static final long serialVersionUID = 3198012785950215862L;

    private String msgid; // 消息id(发送模板消息)

}