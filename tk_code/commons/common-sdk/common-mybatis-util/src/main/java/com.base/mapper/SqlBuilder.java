package com.base.mapper;

import com.base.enums.UsingTypeEnum;
import com.base.model.DaoFilter;
import com.base.model.Parameter;
import com.base.model.StaticInfo;
import com.base.model.TableInfo;
import com.base.utils.StringUtil;
import com.base.utils.TableInfoUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 构建sql
 * Created by junli on 2017/9/20.
 */
public class SqlBuilder {

    /**
     * 获取别名
     *
     * @param prefix 前缀字段
     * @param field  传入字符串
     * @return 别名
     */
    public static String getFieldAlias(String prefix, String field) {
        return new StringBuilder().append("#{")
                .append(prefix)
                .append(".")
                .append(field)
                .append("}").toString();
    }

    /**
     * 获取别名
     *
     * @param field 传入字符串
     * @return 别名
     */
    public static String getFieldAlias(String field) {
        return "#{" + field + "}";
    }

    /**
     * 获取 查询列别名的 包围符 el: name as 'name'
     *
     * @param field 字段
     * @return
     */
    public static String getFieldWithSeparate(String field) {
        return "'" + field + "' ";
    }

    /**
     * 构建别名
     *
     * @param tableName 表名
     * @param alias     别名
     * @return 构建后的别名
     */
    public static String getTableNameAsAlias(String tableName, String alias) {
        return tableName + StaticInfo.AS + alias;
    }

    /*------------------------------构建select 相关信息------------------------------------------*/

    /**
     * 获取表查询字段
     *
     * @param tableInfo  表信息
     * @param tableAlias 表别名
     * @return 拼接后的字符串
     */
    public static String getTableFiledWithTableAlias(TableInfo tableInfo, final String tableAlias) {
        String collect = tableInfo.getFieldInfoList().parallelStream()
                .map(
                        tableFieldInfo -> {
                            StringBuilder columnBuilder = new StringBuilder(256);
                            columnBuilder
                                    .append(tableAlias).append(".")
                                    .append(tableFieldInfo.getColumn())
                                    .append(StaticInfo.AS)
                                    .append(getFieldWithSeparate(tableFieldInfo.getProperty()));
                            return columnBuilder.toString();
                        }
                )
                .collect(Collectors.joining(","));
        return collect;
    }

    /**
     * 获取表查询字段
     *
     * @param clazz      类
     * @param tableAlias 别名
     * @return 查询信息
     */
    public static String getTableFiledWithTableAlias(Class clazz, String tableAlias) {
        TableInfo tableInfo = TableInfoUtil.initTaleInfo(clazz);
        return getTableFiledWithTableAlias(tableInfo, tableAlias);
    }

    /**
     * 获取表查询字段,使用默认 别名'obj'
     *
     * @param clazz 类
     * @return 拼接后的字符串
     */
    public static String getTableFiledWithTableAlias(Class clazz) {
        return getTableFiledWithTableAlias(clazz, StaticInfo.MAIN_TABLE_ALIAS);
    }

    /*------------------------------构建where 相关信息------------------------------------------*/

    /**
     * 构建where 语句 获取 and 相关语句
     *
     * @param daoFilter  daoFilter
     * @param tableAlias 表别名
     * @param daoAlias   DaoFilter 别名
     * @return 构建好的where 条件
     */
    public static List<String> getWhereClause(DaoFilter daoFilter, String tableAlias, String daoAlias) {
        List<Parameter> parameters = daoFilter.getParameters();
        return getClauseStatic(parameters, tableAlias, daoAlias);
    }

    /**
     * 构建where 语句 获取 or 相关语句
     *
     * @param daoFilter  daoFilter
     * @param tableAlias 表别名
     * @param daoAlias   DaoFilter 别名
     * @return 构建好的where 条件
     */
    public static List<String> getWhereSearchOrClause(DaoFilter daoFilter, String tableAlias, String daoAlias) {
        List<Parameter> searchOrParameters = daoFilter.getSearchOrParameters();
        return getClauseStatic(searchOrParameters, tableAlias, daoAlias);
    }

    /**
     * 获取自定义sql
     *
     * @param daoFilter daoFilter
     * @return 自定义sql
     */
    public static List<String> getSelfSql(DaoFilter daoFilter) {
        return daoFilter.getSelfSql();
    }

    /**
     * 组装查询条件
     *
     * @param parameters 条件集合
     * @param tableAlias 表别名
     * @param daoAlias   DaoFilter 别名
     * @return 构建好的where 条件
     */
    public static List<String> getClauseStatic(List<Parameter> parameters, final String tableAlias, final String daoAlias) {
        if (StringUtil.checkObjectNull(parameters) || parameters.size() == 0) {
            return null;
        }
        List<String> whereClauseList = parameters.parallelStream()
                .map(parameter -> buildOperateClause(parameter, tableAlias, daoAlias))
                .collect(Collectors.toList());
        return whereClauseList;
    }

    /**
     * 根据连接字 构建单个关联信息
     *
     * @param parameter
     */
    private static final String buildOperateClause(Parameter parameter, String tableAlias, String daoAlias) {
        StringBuffer whereClauseSb = new StringBuffer(256);
        whereClauseSb.append(tableAlias + "." + parameter.getField()).append(parameter.getOperate().getSql());
        switch (parameter.getOperate()) {
            case LIKE:
                whereClauseSb.append("concat('%',")
                        .append(getFieldAlias(daoAlias, parameter.getParamAlias()))
                        .append(",'%')");
                break;
            case LEFTLIKE:
                whereClauseSb.append("concat('%',").append(getFieldAlias(daoAlias, parameter.getParamAlias()));
                break;
            case RIGHTLIKE:
                whereClauseSb.append("concat(").append(getFieldAlias(daoAlias, parameter.getParamAlias())).append(",'%')");
                break;
            case IN:
            case NOTIN:
                Object value = parameter.getValue();
                whereClauseSb.append("(").append(StringUtil.checkObjectNotNull(value) ? value : "''").append(")");
                break;
            case EQUAL:
            case NOTEQUAL:
            case ISNULL:
            case NOTNULL:
            case LESS:
            case LESSEQUAL:
            case GREAT:
            case GREATEQUAL:
            default:
                whereClauseSb.append(getFieldAlias(daoAlias, parameter.getParamAlias()));

        }
        return whereClauseSb.toString();

    }

    /*------------------------------构建where 相关信息------------------------------------------*/
    public static final String getBasicUsingTypeForSqlProvider() {
        return StaticInfo.MAIN_TABLE_ALIAS + "." + StaticInfo.USING_TYPE + " = " + UsingTypeEnum.in_using.getCode();
    }
}
