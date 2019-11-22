package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/9/17
 * @描述
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class FeedBackContentDto {

    private int feedBackId;

    private List<String> replyContent;


}
