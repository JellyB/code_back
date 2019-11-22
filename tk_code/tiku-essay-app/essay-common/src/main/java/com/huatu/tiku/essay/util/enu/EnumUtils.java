package com.huatu.tiku.essay.util.enu;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by duanxiangchao on 2019/7/9
 */
public class EnumUtils {

    public EnumUtils() {
    }

    public static <E extends Enum<?>> E getEnum(E[] enums, Object enumVal) {
        Enum[] var2 = enums;
        int var3 = enums.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            E e = (E) var2[var4];
            IEnum en = (IEnum)e;
            if (en.getValue().equals(enumVal)) {
                return e;
            }
        }

        return null;
    }

    public static <E extends Enum<?>> E getEnum(E[] enums, String title) {
        Enum[] var2 = enums;
        int var3 = enums.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            E e = (E) var2[var4];
            IEnum en = (IEnum)e;
            if (en.getValue().equals(title)) {
                return e;
            }
        }

        return null;
    }

    public static <E extends Enum<E>> E valueOf(Class<E> enumType, String name) {
        try {
            return StringUtils.isBlank(name) ? null : Enum.valueOf(enumType, name);
        } catch (RuntimeException var3) {
            return null;
        }
    }

    public static <T> List<T> enumToValList(List<? extends IEnum<T>> enums) {
        List<T> result = Lists.newArrayList();
        Iterator var2 = enums.iterator();

        while(var2.hasNext()) {
            IEnum<T> e = (IEnum)var2.next();
            result.add(e.getValue());
        }

        return result;
    }

    public static final String toJSONString(Object object, SerializerFeature... features) {
        SerializeWriter out = new SerializeWriter();
        Map<String, Object> jsonMap = Maps.newHashMap();
        int var6;
        int var7;
        if (object instanceof Enum) {
            Field[] fields = ((Enum)object).getClass().getDeclaredFields();
            Field[] var5 = fields;
            var6 = fields.length;

            for(var7 = 0; var7 < var6; ++var7) {
                Field f = var5[var7];
                if (!f.getType().isEnum() && !"$VALUES".equals(f.getName())) {
                    f.setAccessible(true);
                    Object val = null;

                    try {
                        val = f.get(object);
                    } catch (IllegalAccessException var14) {
                        var14.printStackTrace();
                    }

                    jsonMap.put(f.getName(), val);
                }
            }
        }

        try {
            JSONSerializer serializer = new JSONSerializer(out);
            SerializerFeature[] var17 = features;
            var6 = features.length;

            for(var7 = 0; var7 < var6; ++var7) {
                SerializerFeature feature = var17[var7];
                serializer.config(feature, true);
            }

            serializer.write(jsonMap);
            String var18 = out.toString();
            return var18;
        } finally {
            out.close();
        }
    }

}
