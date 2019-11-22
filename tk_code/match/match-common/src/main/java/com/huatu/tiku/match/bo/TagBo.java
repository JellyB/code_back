package com.huatu.tiku.match.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-10-25 上午10:30
 **/
@Getter
@Setter
@NoArgsConstructor
public class TagBo{
    /**
     * tag id
     *
     */
    private int id;

    /**
     * tag name
     */
    private String name;

    /**
     * tag code
     */
    @JsonIgnore
    private int code;

    /**
     * tag channel
     * 1 非申论
     * 2 申论
     */
    private int channel;

    /**
     * subject
     */
    private int subject;

    /**
     * 是否生效，true 生效，false 不生效
     */
    private boolean isWork;

    @Builder
    public TagBo(int id, String name, int code, int channel, int subject, boolean isWork) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.channel = channel;
        this.subject = subject;
        this.isWork = isWork;
    }
}