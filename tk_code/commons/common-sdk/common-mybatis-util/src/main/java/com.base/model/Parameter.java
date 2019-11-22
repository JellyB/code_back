package com.base.model;

import com.base.enums.Operate;
import com.base.utils.StringEscape;
import com.base.utils.StringUtil;

import java.lang.reflect.Array;
import java.util.*;

/**
 * 条件构造参数
 * Created by junli on 2017/9/20.
 */
public class Parameter {

    private String field;//表字段名称
    private Object value;//值
    private String paramAlias;//参数别名
    private Operate operate;//连接类型

    public Parameter(String field, Object value, String paramAlias, Operate operate) {
        this.field = field;
        this.value = value;
        this.paramAlias = paramAlias;
        this.operate = operate;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = StringUtil.camelToUnderline(field);
    }

    public Object getValue() {
        return getEscapeValue(this.value);
    }

    public Operate getOperate() {
        return operate;
    }

    public String getParamAlias() {
        return paramAlias;
    }

    public void setParamAlias(String paramAlias) {
        this.paramAlias = paramAlias;
    }

    /**
     * 获取转义 后的 value el: value -> 'value'
     *
     * @param object 转义对象
     * @return 转义后的字符串
     */
    private static String getEscapeValue(Object object) {
        if (StringUtil.checkObjectNull(object)) {
            return StringUtil.EMPTY;
        }
        // System.out.println(object.getClass().isArray());

        if (object.getClass().isArray()) {//单独判断数组
            ArrayList list = new ArrayList<>();
            for (int index = 0; index < Array.getLength(object); index++) {
                list.add(Array.get(object, index));
            }
            return getEscapeValue(list);

        } else {

            if (object instanceof String) {
                return StringEscape.escapeString((String) object);
            } else if (object instanceof Integer) {
                String str = String.valueOf(object);
                return StringEscape.escapeString(str);
            } else if (object instanceof List) {
                StringBuffer value = new StringBuffer();
                Iterator objectIterator = ((Collection) object).iterator();
                while (objectIterator.hasNext()) {
                    Object next = objectIterator.next();
                    if (next instanceof String) {
                        value.append("'" + (next) + "',");
                    } else {
                        value.append(next + ",");
                    }
                }
                String valueStr = value.toString();
                if (valueStr.length() > 0) {
                    valueStr = valueStr.substring(0, valueStr.length() - 1);
                }
                return valueStr;
            } else if (object instanceof Boolean) {
                if ((Boolean) object) {
                    return "1";
                } else {
                    return "0";
                }
            } else {
                String str = String.valueOf(object);
                return StringEscape.escapeString(str);
            }
        }
    }
}
