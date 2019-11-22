package com.base.mapper;

import com.base.model.DaoFilter;
import com.base.model.RootObject;
import com.base.model.StaticInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by junli on 2017/9/8.
 */
public interface BaseMapper<T extends RootObject> {

    /**
     * 插入一条数据
     *
     * @param t 实体对象
     * @return int
     */
    @InsertProvider(type = SqlProvider.class, method = "insert")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = int.class)
    Integer create(T t);

    /**
     * 根据条件修改数据
     *
     * @param t         实体对象
     * @param daoFilter 过滤条件
     * @return int
     */
    @UpdateProvider(type = SqlProvider.class, method = "updateByFilter")
    Integer updateByFilter(@Param(StaticInfo.MAIN_TABLE_ALIAS) T t, @Param(StaticInfo.MAIN_DAO_FILTER_ALIAS) DaoFilter daoFilter);

    /**
     * 根据条件 删除一条数据(物理删除)
     *
     * @param daoFilter 过滤条件
     * @return int
     */
    @UpdateProvider(type = SqlProvider.class, method = "removeByFilter")
    Integer removeByFilter(@Param(StaticInfo.MAIN_DAO_FILTER_ALIAS) DaoFilter daoFilter, Class persistentClass);

    /**
     * 根据条件 删除一条数据(逻辑删除)
     *
     * @param daoFilter 过滤条件
     * @return int
     */
    @DeleteProvider(type = SqlProvider.class, method = "removeRealByFilter")
    Integer removeRealByFilter(@Param(StaticInfo.MAIN_DAO_FILTER_ALIAS) DaoFilter daoFilter, Class persistentClass);

    /**
     * 根据过滤条件查询所有数据
     *
     * @param daoFilter 过滤集合
     * @return 查询结果集合
     */
    @SelectProvider(type = SqlProvider.class, method = "flipListByFilter")
    List<T> flipList(@Param(StaticInfo.MAIN_DAO_FILTER_ALIAS) DaoFilter daoFilter, Class persistentClass);

    /**
     * 根据条件 计算总数
     * @param daoFilter 过滤条件
     * @return 满足条件数量
     */
    @SelectProvider(type = SqlProvider.class, method = "countByFilter")
    long countByFilter(@Param(StaticInfo.MAIN_DAO_FILTER_ALIAS) DaoFilter daoFilter, Class persistentClass);
}
