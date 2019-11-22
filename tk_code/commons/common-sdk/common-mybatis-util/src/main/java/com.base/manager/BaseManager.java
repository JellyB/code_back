package com.base.manager;

import com.base.exception.MyBaseException;
import com.base.model.DaoFilter;
import com.base.utils.StringUtil;

import java.io.Serializable;
import java.util.List;

/**
 * Created by junli on 2017/9/20.
 */
public interface BaseManager<T, ID extends Serializable> {

    /**
     * 保存一条数据
     *
     * @param t 实体对象
     * @return int
     */
    Integer save(T t);

    /**
     * 根据条件修改数据
     *
     * @param t         实体对象
     * @param daoFilter 过滤条件
     * @return int
     */
    Integer updateByFilter(T t, DaoFilter daoFilter);

    /**
     * 根据ID 删除一条数据(逻辑删除)
     *
     * @param id 删除ID
     * @return int
     */
    Integer remove(ID id);

    /**
     * 根据条件 删除一条数据(逻辑删除)
     *
     * @param daoFilter 过滤条件
     * @return int
     */
    Integer remove(DaoFilter daoFilter);

    /**
     * 根据ID 删除一条数据(物理删除)
     *
     * @param id 删除ID
     * @return int
     */
    Integer removeReal(ID id);

    /**
     * 根据条件 删除一条数据(物理删除)
     *
     * @param daoFilter 删除ID
     * @return int
     */
    Integer removeReal(DaoFilter daoFilter);

    /**
     * 根据ID 查询一条数据
     *
     * @param id 查询ID
     * @return 实体对象
     */
    T get(ID id);

    /**
     * 根据条件查询一条数据
     *
     * @param daoFilter 过滤条件
     * @return 最近的一条数据
     */
    T get(DaoFilter daoFilter);

    /**
     * 根据过滤条件查询所有数据
     *
     * @param daoFilter 过滤集合
     * @return 查询结果集合
     */
    List<T> flipList(DaoFilter daoFilter);


    /**
     * 验证ID
     */
    default void validateId(ID id) {
        if (StringUtil.checkObjectNull(id)) {
            throw new MyBaseException("Error : ID is null");
        }
    }

    /**
     * 验证实体对象
     */
    default void validatePersistentClass(Class persistentClass) {
        if (StringUtil.checkObjectNull(persistentClass)) {
            throw new MyBaseException("Error : persistentClass is null");
        }
    }

    /**
     * 验证 daoFilter
     */
    default void validateDaoFilter(DaoFilter daoFilter) {
        if (StringUtil.checkObjectNull(daoFilter)) {
            throw new MyBaseException("Error : daoFilter is null");
        }
    }
}
