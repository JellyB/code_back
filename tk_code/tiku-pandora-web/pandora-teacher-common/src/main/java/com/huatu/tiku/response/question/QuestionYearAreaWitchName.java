package com.huatu.tiku.response.question;

import lombok.Builder;
import lombok.Data;

/**
 * Created by huangqp on 2018\5\17 0017.
 */
@Data
@Builder
public class QuestionYearAreaWitchName {
    public String area;

    public Integer year;
}
