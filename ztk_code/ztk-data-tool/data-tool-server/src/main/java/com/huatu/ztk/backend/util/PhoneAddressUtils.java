package com.huatu.ztk.backend.util;

import com.google.common.collect.Maps;
import com.huatu.ztk.user.common.RegexConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by linkang on 3/6/17.
 */


@Component
public class PhoneAddressUtils {

    public final static Map<String, Map<String, String>> mobileAddressMap = new HashMap<>();

    static {
        InputStream in = PhoneAddressUtils.class.getClassLoader().getResourceAsStream("mobile.txt");

        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            bufferedReader.lines().forEach(line -> {
                String[] words = line.split(",");

                String code = words[0];
                String[] split = words[1].split(" ");

                String province = split[0];
                String city = split.length == 2  ? split[1] : " ";

                Map tmp = new HashMap();
                tmp.put("province", province);
                tmp.put("city", city);

                mobileAddressMap.put(code, tmp);
            });

        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    public static Map<String,String> getAreaMap(String phone) {
        if (StringUtils.isBlank(phone) || !phone.matches(RegexConfig.MOBILE_PHONE_REGEX)) {
            return Maps.newHashMap();
        }

        String head = phone.substring(0, 7);
        return mobileAddressMap.getOrDefault(head, Maps.newHashMap());
    }

}
