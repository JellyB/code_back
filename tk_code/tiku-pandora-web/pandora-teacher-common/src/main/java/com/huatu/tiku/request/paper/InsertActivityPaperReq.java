package com.huatu.tiku.request.paper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by huangqp on 2018\7\5 0005.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertActivityPaperReq extends ActivityPaperReq {
    /**
     * 申论考试开始时间(数据迁移时会用到，一般为空)
     */
    private Timestamp essayStartTime;
    /**
     * 申论开始结束时间（数据迁移时会用到，一般为空）
     */
    private Timestamp essayEndTime;

    private Long paperId;
}

