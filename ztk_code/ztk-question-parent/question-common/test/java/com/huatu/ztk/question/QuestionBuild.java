package com.huatu.ztk.question;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Created by shaojieyue
 * Created time 2016-04-25 13:02
 */
public class QuestionBuild {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        final GenericQuestionProtos.GenericQuestion build = GenericQuestionProtos.GenericQuestion.newBuilder()
                .setId(1)
                .setAnalysis("ddd")
                .setAnswer(23)
                .setArea(12)
                .addChoices("dddd")
                .setDifficult(1)
                .setFrom("dd")
                .setType(1)
                .setStem("ddd")
                .setYear(2012)
                .setStatus(1)
                .setScore(1.01f)
                .build();
        final byte[] bytes = build.toByteArray();
        final GenericQuestionProtos.GenericQuestion question = GenericQuestionProtos.GenericQuestion.parseFrom(bytes);
        System.out.println(GenericQuestionProtos.getDescriptor());
        System.out.println(question);
    }
}
