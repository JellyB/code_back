package com.huatu.tiku.essay.vo.admin.correct;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-07-10 7:38 PM
 **/

@Getter
@Setter
@NoArgsConstructor
public class PhotoDistinguishVo {

    private String content;
    private long id;
    private int sort;
    private String url;

    @Builder
    public PhotoDistinguishVo(String content, long id, int sort, String url) {
        this.content = content;
        this.id = id;
        this.sort = sort;
        this.url = url;
    }
}
