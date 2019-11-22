package com.base.utils;

import com.base.annotations.IsNotUnderlineToCamel;
import com.base.annotations.TableName;
import com.base.functionInterface.TryFunction;
import com.base.functionInterface.TryPredicate;
import com.base.model.StaticInfo;
import com.base.model.TableFieldInfo;
import com.base.model.TableInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by junli on 2017/9/19.
 * update on 2018-04 此处参考 java bean 自省机制,添加缓存信息
 */
public final class TableInfoUtil {

    //对象转换信息缓存
    private static final WeakHashMap<Class<?>, TableInfo> tableInfoCache = new WeakHashMap<>();


    /**
     * 初始化表
     *
     * @param clazz 实体反射类
     * @return 表信息
     */
    public final static TableInfo initTaleInfo(Class<?> clazz) {
        TableInfo tableInfo;
        synchronized (tableInfoCache) {
            tableInfo = tableInfoCache.get(clazz);
        }
        if (tableInfo == null) {
            tableInfo = new TableInfo();
            tableInfo.setTableName(getTableName(clazz));
            tableInfo.setFieldInfoList(getTableFieldInfo(clazz));
            synchronized (tableInfoCache) {
                tableInfoCache.put(clazz, tableInfo);
            }
        }
        return tableInfo;
    }


    /**
     * 初始化 对象对应的表信息
     *
     * @param object 需要初始化对象
     * @return 表信息(只包含 非空字段)
     * @throws Exception
     */
    public final static TableInfo initTableInNotNull(Object object) throws Exception {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName(getTableName(object));
        tableInfo.setFieldInfoList(getTableNotNullFieldInfo(object));
        return tableInfo;
    }


    /**
     * 获取表名称
     *
     * @param clazz 表对象类
     * @return 表名称
     */
    public final static String getTableName(Class<?> clazz) {
        TableName table = clazz.getAnnotation(TableName.class);
        if (table != null && StringUtil.isNotEmpty(table.name())) {//扫描到注解情况
            return table.name();
        }
        //缺少注解
        String tableName = clazz.getSimpleName();
        return StringUtil.camelToUnderline(tableName);
    }

    /**
     * 获取表名称
     *
     * @param object 实体类
     * @return 表名称
     */
    public final static String getTableName(Object object) {
        if (StringUtil.checkObjectNull(object)) {
            return StringUtil.EMPTY;
        }
        Class<?> clazz = object.getClass();
        return getTableName(clazz);
    }

    /**
     * 获取所有列名
     *
     * @param clazz 表对象类
     * @return 字段
     */
    private final static List<TableFieldInfo> getTableFieldInfo(Class<?> clazz) {
        boolean includeSuperClass = clazz.getClassLoader() != null;
        final Method[] methods = includeSuperClass ? clazz.getMethods() : clazz.getDeclaredMethods();
        List<TableFieldInfo> collect = Stream.of(methods)
                .filter(TableInfoUtil::notIgnoreMethod)
                .map(method -> {
                    String fieldByMethod = getFieldByMethod(method);
                    return StringUtil.isNotEmpty(fieldByMethod)
                            ? new TableFieldInfo(fieldByMethod, method.getAnnotation(IsNotUnderlineToCamel.class))
                            : null;
                })
                .filter(StringUtil::checkObjectNotNull)
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * 获取所有非空字段的列
     *
     * @param object 初始化对象
     * @return 字段集合
     * @throws Exception 反射调用错误
     */
    private final static List<TableFieldInfo> getTableNotNullFieldInfo(Object object) throws Exception {
        if (StringUtil.checkObjectNull(object)) {
            return null;
        }
        Class<?> clazz = object.getClass();
        boolean includeSuperClass = clazz.getClassLoader() != null;
        final Method[] methods = includeSuperClass ? clazz.getMethods() : clazz.getDeclaredMethods();
        List<TableFieldInfo> collect = Stream.of(methods)
                .filter(TryPredicate.of(method -> notIgnoreMethod(method)))
                .map(TryFunction.of(
                        method -> {
                            String fieldByMethod = getNotNullFieldForObject(method, object);
                            return StringUtil.isNotEmpty(fieldByMethod)
                                    ? new TableFieldInfo(fieldByMethod, method.getAnnotation(IsNotUnderlineToCamel.class))
                                    : null;
                        }
                ))
                .filter(StringUtil::checkObjectNotNull)
                .collect(Collectors.toList());
        return collect;
    }


    /**
     * 通过方法(get)获取 字段
     *
     * @return 返回驼峰式命令字段
     */
    private final static String getFieldByMethod(Method method) {
        String name = method.getName();
        String field = StringUtil.EMPTY;
        if (name.startsWith("get")) {
            if ("getClass".equals(name) || "getDeclaringClass".equals(name)) {
                field = "";
            } else {
                field = name.substring(3);
            }
        } else if (name.startsWith("is")) {
            field = name.substring(2);
        } else {
            return field;
        }
        if (StringUtil.isNotEmpty(field) // get/is 方法
                && StringUtil.isCapitalFirst(field)// 大写字母
                && method.getTypeParameters().length == 0// 无参
                ) {
            field = StringUtil.firstToLowerCase(field);
        }
        return field;
    }

    /**
     * 获取一个对象中的(get)方法对应的非空字段
     *
     * @param object 目标对象
     * @return 字段集合
     */
    private final static String getNotNullFieldForObject(Method method, Object object) throws Exception {
        String fieldByMethod = getFieldByMethod(method);
        if (StringUtil.isNotEmpty(fieldByMethod)) {
            Object value = method.invoke(object);
            if (StringUtil.checkObjectNotNull(value)) {
                return fieldByMethod;
            }
        }
        return fieldByMethod;
    }

    /**
     * 验证字段是否合法
     *
     * @param method
     * @return 非法 返回 true
     */
    private final static boolean ignoreMethod(Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return true;
        }
        boolean hasAnnotation = Stream.of(StaticInfo.IGNORED_ANNOTATION)
                .anyMatch(clazz -> {
                    Annotation annotation = method.getAnnotation(clazz);
                    return StringUtil.checkObjectNotNull(annotation);
                });
        return hasAnnotation;
    }

    private final static boolean notIgnoreMethod(Method method){
        return !ignoreMethod(method);
    }
}
