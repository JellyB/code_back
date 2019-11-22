package com.huatu.tiku.constants;

import java.util.function.Function;

public class QuestionWeightConstant {


    public static final Function<Integer, Double> YEAR_PERCENT = (year -> new Double(year) / 2019);

    public static final Function<Integer, Double> WRONG_PERCENT = (errorPercent -> new Double(errorPercent) / 100);

    public static final Function<Integer, Double> MODE_PERCENT = (mode -> mode == 1 ? 0.11d : 0.05d);

    public static final Function<Integer, Double> DO_NUM_PERCENT = (num -> {
        if (num < 100) {
            return 0d;
        } else if (num > 100 && num < 1000) {
            return 0.5d;
        } else if (num > 1000 && num > 10000) {
            return 1.0d;
        } else if (num > 10000) {
            return 1.3d;
        }
        return 2d;
    });

    public static final Function<Integer, Boolean> YEAR_BOOLEAN = (year -> year >= 2017);
    public static final Function<Integer, Boolean> WRONG_BOOLEAN = (errorPercent -> errorPercent >= 60);
    public static final Function<Integer, Boolean> MODE_BOOLEAN = (year -> true);
    public static final Function<Integer, Boolean> DO_NUM_BOOLEAN = (num -> num >= 1000);

}
