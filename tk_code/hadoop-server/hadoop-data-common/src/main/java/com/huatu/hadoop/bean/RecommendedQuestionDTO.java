package com.huatu.hadoop.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Set;

@Builder
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class RecommendedQuestionDTO {

    public static final String ZTK_USER_QUESTION_POINT_NOTGRASP = "ztk_user_question_point_NotGrasp";
    public static final String ZTK_SUBJECT_POINT = "ztk_subject_point";

    private int waitRecommendArrLength;
    private int backUpArrLength;

    private int[] waitRecommendArr;
    private int[] backUpArr;
    private Set<Integer> waitRecommendPoint;
    private Set<Integer> backUpPoint;

    public RecommendedQuestionDTO() {
    }

    public RecommendedQuestionDTO(int waitRecommendArrLength, int backUpArrLength) {

        this.waitRecommendArrLength = waitRecommendArrLength;
        this.backUpArrLength = backUpArrLength;
        this.waitRecommendArr = new int[waitRecommendArrLength];
        this.backUpArr = new int[backUpArrLength];
    }
}
