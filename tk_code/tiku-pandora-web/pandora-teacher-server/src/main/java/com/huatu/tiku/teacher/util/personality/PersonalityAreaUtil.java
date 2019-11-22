package com.huatu.tiku.teacher.util.personality;

import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.entity.common.Area;
import com.huatu.tiku.response.area.AreaTreeResp;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author huangqingpeng
 * @title: PersonalityAreaUtil
 * @description: TODO
 * @date 2019-11-2021:50
 */
public class PersonalityAreaUtil {


    /**
     * 行测全国地区替换为国考
     *
     * @param subject
     * @param areaId
     * @param defaultName
     * @return
     */
    public static String getAreaName(int subject, int areaId, String defaultName) {
        switch (subject) {
            case 1:
                if (areaId == -9 ||
                        Optional.ofNullable(defaultName)
                                .filter(StringUtils::isNotBlank)
                                .filter(i -> i.indexOf("全国") > -1).isPresent()
                ) {
                    return "国家";
                }
        }
        return defaultName;
    }


    public static void filterAndChangeName(List<AreaTreeResp> areaTreeResps, Long subject) {
        if (subject.intValue() != 1) {
            areaTreeResps.removeIf(i -> i.getId().equals(100_000L));
        } else {
            areaTreeResps.stream().filter(i -> i.getId().intValue() == -9)
                    .findAny()
                    .ifPresent(
                            a ->
                                    a.setName("国家")
                    );
        }

    }
}
