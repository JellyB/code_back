package com.huatu.tiku.service.impl;

import com.google.common.base.Preconditions;
import com.ht.base.start.security.module.base.UserDetails;
import com.ht.base.user.module.security.UserInfo;
import com.huatu.common.ErrorResult;
import com.huatu.common.bean.BaseEntity;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.baseEnum.BaseStatusEnum;
import com.huatu.tiku.mapper.BaseCompeteMapper;
import com.huatu.tiku.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/5/28
 */
@Slf4j
public class BaseServiceImpl<T extends BaseEntity> implements BaseService<T> {

    @Autowired
    private Mapper<T> baseMapper;

    @Autowired
    private BaseCompeteMapper<T> baseCompeteMapper;

    private static int ERROR_RESULT_CODE = 5000000;
    private static String RESULT_MESSAGE = "操作失败，异常数据ID=%s";

    private Class<T> clazz;

    public BaseServiceImpl(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Transactional
    @Override
    public Integer save(T t) {
        if (null == t) {
            return 0;
        }
        if (null == t.getId()) {
            buildInsertInfo(t);
            return baseMapper.insertSelective(t);
        } else if (t.getId() > 0) {
            WeekendSqls<T> sql = WeekendSqls.custom();
            sql.andEqualTo(T::getId, t.getId());
            Example example = Example.builder(this.clazz)
                    .andWhere(sql)
                    .build();
            buildUpdateInfo(t);
            return updateByExampleSelective(t, example);
        }
        log.info("操作失败,异常数据信息 = {}", t);

        throwBizException(t.getId());
        return 0;
    }

    @Transactional
    @Override
    public T selectByPrimaryKey(Object key) {
        checkIllegalId(key);
        Object o = baseMapper.selectByPrimaryKey(key);
        if (null != o) {
            T result = (T) o;
            if (!BaseStatusEnum.isNormal(result.getStatus())) {
                return null;
            }
            return result;
        }
        return null;
    }

    @Transactional
    @Override
    public T selectOne(T t) {
        t.setStatus(BaseStatusEnum.NORMAL.getCode());
        return baseMapper.selectOne(t);
    }

    @Transactional
    @Override
    public int selectCount(T t) {
        t.setStatus(BaseStatusEnum.NORMAL.getCode());
        return baseMapper.selectCount(t);
    }

    @Transactional
    @Override
    public List<T> selectByExample(Example example) {
        fixExample(example);
        return baseMapper.selectByExample(example);
    }

    @Transactional
    @Override
    public int selectCountByExample(Example example) {
        fixExample(example);
        return baseMapper.selectCountByExample(example);
    }

    @Transactional
    @Override
    public List<T> selectAll() {
        List<T> list = baseMapper.selectAll();
        List<T> collect = list.parallelStream()
                .filter(data -> BaseStatusEnum.isNormal(data.getStatus()))
                .collect(Collectors.toList());
        return collect;
    }


    @Transactional
    @Override
    public int deleteByPrimaryKey(T t) {
        checkIllegalId(t.getId());
        return deleteByPrimaryKey(t.getId());
    }


    @Transactional
    @Override
    public int deleteByPrimaryKey(Long id) {
        if (null == id || 0 >= id) {
            return 0;
        }
        WeekendSqls<T> sql = WeekendSqls.custom();
        sql.andEqualTo(T::getId, id);
        Example example = Example.builder(clazz)
                .where(sql)
                .build();
        return deleteByExample(example);
    }


    @Transactional
    @Override
    public int deleteByExample(Example example) {
        try {
            T deleteEntity = clazz.newInstance();
            fixExample(example);
            deleteEntity.setStatus(BaseStatusEnum.DELETED.getCode());
            buildUpdateInfo(deleteEntity);
            return baseMapper.updateByExampleSelective(deleteEntity, example);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }


    @Transactional
    @Override
    public int deleteByPrimaryKeys(Collection primaryKeys) {
        if (primaryKeys == null || primaryKeys.size() == 0) {
            log.info("批量删除的id 集合长度为0");
            return 0;
        }
        WeekendSqls<T> sql = WeekendSqls.custom();
        sql.andIn(T::getId, primaryKeys);

        Example example = Example.builder(this.clazz)
                .where(sql)
                .build();
        fixExample(example);
        return deleteByExample(example);
    }

    @Transactional
    @Override
    public int insert(T t) {
        buildInsertInfo(t);
        return baseMapper.insertSelective(t);
    }

    @Transactional
    @Override
    public int insertAll(List<T> list) {
        if (null == list || list.size() == 0) {
            return 0;
        }
        List<T> collect = list.stream()
                .map(data -> {
                    buildInsertInfo(data);
                    return data;
                })
                .collect(Collectors.toList());
        return baseCompeteMapper.insertAll(collect);
    }


    @Transactional
    public int updateByPrimaryKey(T t) {
        if (null == t) {
            return 0;
        }
        if (t.getId() > 0) {
            WeekendSqls<T> sql = WeekendSqls.custom();
            sql.andEqualTo(T::getId, t.getId());
            Example example = Example.builder(this.clazz)
                    .andWhere(sql)
                    .build();
            buildUpdateInfo(t);
            return updateByExample(t, example);
        }
        log.info("操作失败,异常数据信息 = {}", t);
        throwBizException(t.getId());
        return 0;
    }

    @Transactional
    @Override
    public int updateByExample(T t, Example example) {
        fixExample(example);
        buildUpdateInfo(t);
        return baseMapper.updateByExample(t, example);
    }

    @Transactional
    @Override
    public int updateByExampleSelective(T t, Example example) {
        fixExample(example);
        buildUpdateInfo(t);
        return baseMapper.updateByExampleSelective(t, example);
    }

    public static void throwBizException(String message) {
        throw new BizException(ErrorResult.create(ERROR_RESULT_CODE, message));
    }

    public static void throwBizException(Long id) {
        throw new BizException(ErrorResult.create(ERROR_RESULT_CODE, String.format(RESULT_MESSAGE, id)));
    }

    /**
     * 判断ID 是否非法
     *
     * @return 非法 直接抛出异常信息
     */
    private static void checkIllegalId(Object id) {
        Preconditions.checkArgument(null != id && id instanceof Long && (Long) id > 0, RESULT_MESSAGE, id);
    }

    private static void fixExample(Example example) {
        example.and().andEqualTo("status", BaseStatusEnum.NORMAL.getCode());
        example.setOrderByClause(StringUtils.isBlank(example.getOrderByClause()) ?
                "id DESC" : example.getOrderByClause() + ",id DESC");
    }

    private static Timestamp timestampInit() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 获取用户信息
     */
    private static UserInfo getUserInfo() {
        if (null == SecurityContextHolder.getContext() || null == SecurityContextHolder.getContext().getAuthentication()) {
            return null;
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUserInfo();
        }
        if ("anonymousUser".equals(principal)) {
            return null;
        }
        return null;
    }

    /**
     * 新增 时 数据补充
     */
    public static <T extends BaseEntity> void buildInsertInfo(T t) {
        t.setGmtCreate(timestampInit());
        t.setStatus(BaseStatusEnum.NORMAL.getCode());
        if (null == t.getBizStatus()) {
            t.setBizStatus(BaseStatusEnum.NORMAL.getCode());
        }
        UserInfo userInfo = getUserInfo();
        if (null != userInfo) {
            t.setCreatorId(userInfo.getId());
        }
    }

    /**
     * 修改时候 数据补充
     */
    public static <T extends BaseEntity> void buildUpdateInfo(T t) {
        t.setGmtModify(timestampInit());
        UserInfo userInfo = getUserInfo();
        if (null != userInfo) {
            t.setModifierId(getUserInfo().getId());
        }
    }
}
