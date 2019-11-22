package com.base.mapper;

import com.base.enums.UsingTypeEnum;
import com.base.model.DaoFilter;
import com.base.model.Sort;
import com.base.model.StaticInfo;
import com.base.model.TableInfo;
import com.base.utils.StringUtil;
import com.base.utils.TableInfoUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;

/**
 * Created by junli on 2017/9/8.
 */
public class SqlProvider {

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * 插入一条数据
     *
     * @param object 实体对象
     * @return intu
     */
    public String insert(final Object object) {
        Class<?> clazz = object.getClass();
        TableInfo tableInfo = TableInfoUtil.initTaleInfo(clazz);
        String sql = new SQL() {
            {
                INSERT_INTO(tableInfo.getTableName());
                tableInfo.getFieldInfoList().forEach(tableFieldInfo ->
                        VALUES(
                                tableFieldInfo.getColumn(),
                                SqlBuilder.getFieldAlias(tableFieldInfo.getProperty())
                        )
                );
            }
        }.toString();

        log.info(sql);
        return sql;
    }

    /**
     * 根据条件修改数据
     *
     * @param object    实体对象
     * @param daoFilter 过滤条件
     * @return int
     */
    public String updateByFilter(Object object, DaoFilter daoFilter) {
        Class<?> clazz = object.getClass();
        //表信息
        TableInfo tableInfo = TableInfoUtil.initTaleInfo(clazz);

        //获取所有有值的字段
        SQL sql = new SQL();
        //where 之前所有信息
        sql.UPDATE(SqlBuilder.getTableNameAsAlias(tableInfo.getTableName(), StaticInfo.MAIN_TABLE_ALIAS));
        tableInfo.getFieldInfoList().forEach(tableFieldInfo -> {
            if (!StringUtil.ArraysContains(StaticInfo.NO_UPDATE_FIELD, tableFieldInfo.getProperty())) {
                //过滤 不需要更改的字段
                sql.SET(tableFieldInfo.getColumn() + "=" + SqlBuilder.getFieldAlias(StaticInfo.MAIN_TABLE_ALIAS, tableFieldInfo.getProperty()));
            }
        });
        //构建Where 部分
        buildWhereClause(sql, daoFilter);
        String sqlStr = sql.toString();

        log.debug(sqlStr);
        return sqlStr;
    }

    /**
     * 根据条件 删除数据(逻辑删除)
     *
     * @param daoFilter 过滤条件
     * @return int
     */
    public String removeByFilter(DaoFilter daoFilter, Class persistentClass) {
        //表信息
        String tableName = TableInfoUtil.getTableName(persistentClass);
        SQL sql = new SQL();
        //where 之前所有信息
        sql.UPDATE(SqlBuilder.getTableNameAsAlias(tableName, StaticInfo.MAIN_TABLE_ALIAS));
        sql.SET(StaticInfo.MAIN_TABLE_ALIAS + "." + StaticInfo.USING_TYPE + " = " + UsingTypeEnum.logic_delete.getCode());
        //构建Where 部分
        buildWhereClause(sql, daoFilter);
        String sqlStr = sql.toString();

        log.debug(sqlStr);
        return sqlStr;
    }

    /**
     * 根据条件 删除数据(物理删除)
     *
     * @param daoFilter 过滤条件
     * @return int
     */
    public String removeRealByFilter(DaoFilter daoFilter, Class persistentClass) {
        //表信息
        String tableName = TableInfoUtil.getTableName(persistentClass);
        SQL sql = new SQL();
        //where 之前所有信息
        sql.DELETE_FROM(SqlBuilder.getTableNameAsAlias(tableName, StaticInfo.MAIN_TABLE_ALIAS));
        //构建Where 部分
        buildWhereClause(sql, daoFilter);
        String sqlStr = sql.toString();
        //修正别名问题
        sqlStr = sqlStr.substring(0, 7) + StaticInfo.MAIN_TABLE_ALIAS + sqlStr.substring(6, sqlStr.length());
        log.debug(sqlStr);
        return sqlStr;
    }


    /**
     * 根据条件 查询数据
     *
     * @param daoFilter 过滤条件
     * @return int
     */
    public String flipListByFilter(DaoFilter daoFilter, Class persistentClass) {
        //表信息
        TableInfo tableInfo = TableInfoUtil.initTaleInfo(persistentClass);
        SQL sql = new SQL();

        //where 之前所有信息
        sql.SELECT_DISTINCT(StaticInfo.MAIN_TABLE_ALIAS + "." + "id");
        tableInfo.getFieldInfoList().forEach(tableFieldInfo -> {
            if (!tableFieldInfo.getColumn().equals("id")) {
                sql.SELECT(
                        StaticInfo.MAIN_TABLE_ALIAS + "." + tableFieldInfo.getColumn()
                                + StaticInfo.AS + SqlBuilder.getFieldWithSeparate(tableFieldInfo.getProperty())
                );
            }
        });
        sql.FROM(SqlBuilder.getTableNameAsAlias(tableInfo.getTableName(), StaticInfo.MAIN_TABLE_ALIAS));
        //构建Where 部分
        buildWhereAndSort(sql, daoFilter);
        String sqlStr = sql.toString();
        log.debug(sqlStr);
        return sqlStr;
    }

    /**
     * 根据条件 计算总数
     *
     * @param daoFilter 过滤条件
     * @return 满足条件数量
     */
    public String countByFilter(DaoFilter daoFilter, Class persistentClass) {
        //表信息
        String tableName = TableInfoUtil.getTableName(persistentClass);
        SQL sql = new SQL();
        //where 之前所有信息
        sql.SELECT("DISTINCT(" + StaticInfo.MAIN_TABLE_ALIAS + "." + "id)");
        sql.FROM(SqlBuilder.getTableNameAsAlias(tableName, StaticInfo.MAIN_TABLE_ALIAS));
        //构建Where 部分
        buildWhereClause(sql, daoFilter);
        String sqlStr = sql.toString();
        log.debug(sqlStr);
        return sqlStr;
    }

    /**
     * 构建 where 之后的sql 语句
     *
     * @param sql       SQL 对象
     * @param daoFilter 条件
     */
    public static void buildWhereAndSort(SQL sql, DaoFilter daoFilter) {
        buildWhereClause(sql, daoFilter);
        buildSortClause(sql, daoFilter);
    }

    /**
     * 构建条件
     *
     * @param sql       SQL 对象
     * @param daoFilter 条件
     */
    private static void buildWhereClause(SQL sql, DaoFilter daoFilter) {
        //构建where 部分
        List<String> whereClause = SqlBuilder.getWhereClause(daoFilter, StaticInfo.MAIN_TABLE_ALIAS, StaticInfo.MAIN_DAO_FILTER_ALIAS);
        if (StringUtil.checkObjectNotNull(whereClause) && whereClause.size() > 0) {
            whereClause.forEach(whereClauseStr -> sql.WHERE(whereClauseStr));
        }

        //or 相关
        List<String> whereSearchOrClause = SqlBuilder.getWhereSearchOrClause(daoFilter, StaticInfo.MAIN_TABLE_ALIAS, StaticInfo.MAIN_DAO_FILTER_ALIAS);
        if (StringUtil.checkObjectNotNull(whereSearchOrClause) && whereSearchOrClause.size() > 0) {
            whereSearchOrClause.forEach(whereSearchOrClauseStr -> {
                sql.OR();
                sql.WHERE(whereSearchOrClauseStr);
                sql.WHERE(SqlBuilder.getBasicUsingTypeForSqlProvider());
            });
        }

        //自定义语句
        List<String> selfSql = SqlBuilder.getSelfSql(daoFilter);
        if (StringUtil.checkObjectNotNull(selfSql) && selfSql.size() > 0) {
            sql.AND();
            selfSql.forEach(selfSqlStr -> sql.WHERE(selfSqlStr));
        }
    }

    /**
     * 构建 sort
     *
     * @param sql       sql 对象
     * @param daoFilter 条件
     */
    private static void buildSortClause(SQL sql, DaoFilter daoFilter) {
        List<Sort> sortList = daoFilter.getSortList();
        if (StringUtil.checkObjectNotNull(sortList)) {
            sortList.forEach(sort -> {
                String sortStr = StaticInfo.MAIN_TABLE_ALIAS + "." + sort.getField();
                if (sort.isFlag()) {
                    sortStr += " desc";
                }
                sql.ORDER_BY(sortStr);
            });

        }
    }
}
