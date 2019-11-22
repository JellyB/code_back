package com.base.manager.Impl;

import com.base.enums.Operate;
import com.base.enums.UsingTypeEnum;
import com.base.exception.MyBaseException;
import com.base.manager.BaseManager;
import com.base.mapper.BaseMapper;
import com.base.model.DaoFilter;
import com.base.model.RootObject;
import com.base.model.StaticInfo;
import com.base.utils.StringUtil;

import java.io.Serializable;
import java.util.List;

/**
 * Created by junli on 2017/9/20.
 */
public class BaseManagerImpl<T extends RootObject, ID extends Serializable> implements BaseManager<T, ID> {

    private Class persistentClass;
    private BaseMapper<T> baseMapper;

    public BaseManagerImpl(BaseMapper<T> baseMapper, Class persistentClass) {
        this.baseMapper = baseMapper;
        this.persistentClass = persistentClass;
    }

    @Override
    public Integer save(T t) {
        //修改时间
        t.setModifyTime();
        if (StringUtil.checkObjectNull(t.getId())) {
            return baseMapper.create(t);
        } else {
            DaoFilter daoFilter = new DaoFilter();
            daoFilter.addSearch(t.getId(), Operate.EQUAL, "id");
            return updateByFilter(t, daoFilter);
        }
    }

    @Override
    public Integer updateByFilter(T t, DaoFilter daoFilter) {
        validateDaoFilter(daoFilter);

        t.setModifyTime();
        daoFilter.addSearch(UsingTypeEnum.in_using.getCode(), Operate.EQUAL, StaticInfo.USING_TYPE);
        return baseMapper.updateByFilter(t, daoFilter);
    }

    @Override
    public Integer remove(ID id) {
        validatePersistentClass(this.persistentClass);
        validateId(id);

        DaoFilter daoFilter = new DaoFilter();
        daoFilter.addSearch(id, Operate.EQUAL, "id");
        return baseMapper.removeByFilter(daoFilter, this.persistentClass);
    }

    @Override
    public Integer remove(DaoFilter daoFilter) {
        validatePersistentClass(this.persistentClass);
        validateDaoFilter(daoFilter);

        daoFilter.addSearch(UsingTypeEnum.in_using.getCode(), Operate.EQUAL, StaticInfo.USING_TYPE);
        return baseMapper.removeByFilter(daoFilter, this.persistentClass);
    }

    @Override
    public Integer removeReal(ID id) {
        validateId(id);

        DaoFilter daoFilter = new DaoFilter();
        daoFilter.addSearch(id, Operate.EQUAL, "id");
        return removeReal(daoFilter);
    }

    @Override
    public Integer removeReal(DaoFilter daoFilter) {
        validatePersistentClass(persistentClass);
        validateDaoFilter(daoFilter);

        return baseMapper.removeRealByFilter(daoFilter, this.persistentClass);
    }

    @Override
    public T get(ID id) {
        validateId(id);

        DaoFilter daoFilter = new DaoFilter();
        daoFilter.addSearch(id, Operate.EQUAL, "id");

        return get(daoFilter);
    }

    @Override
    public T get(DaoFilter daoFilter) {
        validateDaoFilter(daoFilter);

        List<T> ObjectListByFilter = flipList(daoFilter);
        if (!StringUtil.checkObjectNull(ObjectListByFilter) && ObjectListByFilter.size() > 0) {
            return ObjectListByFilter.get(0);
        }
        return null;
    }

    @Override
    public List<T> flipList(DaoFilter daoFilter) {
        validateDaoFilter(daoFilter);

        daoFilter.addSearch(UsingTypeEnum.in_using.getCode(), Operate.EQUAL, StaticInfo.USING_TYPE);
        return baseMapper.flipList(daoFilter, this.persistentClass);
    }

}
