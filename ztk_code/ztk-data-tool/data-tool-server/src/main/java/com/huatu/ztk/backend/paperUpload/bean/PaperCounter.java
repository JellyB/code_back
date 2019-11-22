package com.huatu.ztk.backend.paperUpload.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;

/**
 * Created by lenovo on 2017/6/13.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperCounter {
    private int questionSeq;
    private int questionCount;
    private HashSet<Integer> sucQuestionSeq;
}
