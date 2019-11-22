package com.huatu.hadoop.util;

public class KafkaValueParse {

    public static String parseValueToStr(String value, String fieldName) {

        String result = "";
        try {
            if (value != null && !value.equals("")) {
                String[] fields = value.split("|");

                for (String fie : fields) {

                    if (fie.startsWith(fieldName)) {
                        String[] strings = fie.split("=");

                        result = strings[1];
                        break;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }
}
