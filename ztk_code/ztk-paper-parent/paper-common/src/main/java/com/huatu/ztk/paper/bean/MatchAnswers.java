package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by linkang on 2017/10/25 下午6:12
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class MatchAnswers implements Serializable{
    private static final long serialVersionUID = 1L;

    private long userId;
    private String uname;
    private long practiceId;
    private List<Answer> answers;
    private int area;
}
