package com.base.model;

import com.base.enums.Operate;
import com.base.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by junli on 2017/9/8.
 */
public class DaoFilter extends HashMap {

    public DaoFilter() {

    }

    /**
     * 普通参数
     */
    List<Parameter> parameters = new ArrayList<>();

    /**
     * or 关联词 后接条件
     */
    List<Parameter> searchOrParameters = new ArrayList<>();

    /**
     * 自定义SQL
     */
    List<String> selfSql = new ArrayList<>();

    /**
     * 排序字段
     */
    List<Sort> sortList = new ArrayList<>();

    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<Parameter> getSearchOrParameters() {
        return searchOrParameters;
    }

    public List<String> getSelfSql() {
        return selfSql;
    }

    public List<Sort> getSortList() {
        return sortList;
    }

    /**
     * 添加查询条件
     *
     * @param value   值
     * @param operate 连接符
     * @param field   字段名称
     * @return 构造器
     */
    public DaoFilter addSearch(final Object value, Operate operate, String field) {
        if (!validateSearchValue(value, operate)) {
            return this;
        }
        String paramAlias = StaticInfo.SEARCH_PARAM_PREFIX + parameters.size();
        Parameter parameter = new Parameter(field, value, paramAlias, operate);
        if (Operate.validateOperate(operate)) {
            super.put(parameter.getParamAlias(), parameter.getValue());
        }
        //存储构造条件
        parameters.add(parameter);
        return this;
    }

    /**
     * 添加or 关联查询条件
     *
     * @param value   值
     * @param operate 连接符
     * @param field   字段名称
     * @return 构造器
     */
    public DaoFilter addOrSearch(final Object value, Operate operate, String field) {
        if (!validateSearchValue(value, operate)) {
            return this;
        }
        String paramAlias = StaticInfo.SEARCH_OR_PARAM_PREFIX + searchOrParameters.size();
        Parameter parameter = new Parameter(field, value, paramAlias, operate);
        if (Operate.validateOperate(operate)) {
            super.put(parameter.getParamAlias(), parameter.getValue());
        }
        //存储构造条件
        searchOrParameters.add(parameter);
        return this;
    }

    /**
     * 添加降序字段
     *
     * @param field 字段名称
     * @return 构造器
     */
    public DaoFilter desc(final String field) {
        if (StringUtil.isNotEmpty(field)) {
            Sort sort = new Sort(field, true);
            this.sortList.add(sort);
        }
        return this;
    }

    /**
     * 升序字段
     *
     * @param field 字段名称
     * @return 构造器
     */
    public DaoFilter asc(final String field) {
        if (StringUtil.isNotEmpty(field)) {
            Sort sort = new Sort(field, false);
            this.sortList.add(sort);
        }
        return this;
    }

    /**
     * 拼接自定义sql
     *
     * @param sql
     * @return
     */
    public DaoFilter addSelfSql(String sql) {
        if (StringUtil.isNotEmpty(sql)) {
            selfSql.add(sql);
        }
        return this;
    }

    /**
     * 传入值
     *
     * @param value   字段值
     * @param operate 连接符
     * @return
     */
    private static boolean validateSearchValue(Object value, Operate operate) {
        if (StringUtil.checkObjectNull(value)) {
            if (Operate.validateOperateParamIsNull(operate)) {
                return true;
            }
            return false;
        }
        if (value instanceof String) {
            return !value.equals("null");
        }
        return true;
    }
}
