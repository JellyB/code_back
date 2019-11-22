package com.huatu.tiku.request.paper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotNull;

/**
 * Created by huangqp on 2018\7\5 0005.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateActivityPaperReq extends ActivityPaperReq {
    /**
     * 活动卷id
     */
    @NotNull(message = "活动id不能为空")
    private Long id;

    /**
     * 试题卷ID
     */
    private Long paperId;
}

