package com.base.model;

import com.base.annotations.ColumnTransient;

/**
 * 相关预设信息
 * Created by junli on 2017/9/20.
 */
public class StaticInfo {

    /**
     * 拥有此注解 在生成字段的时候 会跳过该字段
     */
    public static final Class[] IGNORED_ANNOTATION = {
            ColumnTransient.class
    };

    /**
     * 所有表的默认别名
     */
    public static final String MAIN_TABLE_ALIAS = "obj";

    /**
     * 查询条件默认别名
     */
    public static final String MAIN_DAO_FILTER_ALIAS = "filter";

    public static final String AS = " AS ";

    /**
     * dao 参数前缀
     */
    public static final String SEARCH_PARAM_PREFIX = "P_";
    public static final String SEARCH_OR_PARAM_PREFIX = "PO_";

    /**
     * 数据状态标志列
     */
    public static final String USING_TYPE = "using_type";

    /**
     * 不需要更改的字段
     */
    public static final String[] NO_UPDATE_FIELD = new String[]{"id", "createTime", "usingType"};

}
