package com.huatu.ztk.backend.metas.bean;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 答题卡
 * Created by shaojieyue on 4/21/16.
 */

@Data
@Document(collection = "ztk_answer_card")
public class AnswerCardSub{
    public long id;
    public Object[] arrays;//是否正确
}
