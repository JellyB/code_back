package com.huatu.tiku.provider;

import com.google.common.collect.Lists;
import com.huatu.common.bean.BaseEntity;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.entity.EntityTable;
import tk.mybatis.mapper.mapperhelper.EntityHelper;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/7/5
 */
public class InsertCompeteProvider<T extends BaseEntity> {


    /**
     * 此处需确认 list 非空
     *
     * @param list
     * @return
     */
    public String insertAll(List<T> list) {
        Class<? extends BaseEntity> entityClass = list.get(0).getClass();
        //此处可能 出现表名 不存在
        Table annotation = entityClass.getAnnotation(Table.class);
        //获取列信息
        EntityTable entityTable = EntityHelper.getEntityTable(entityClass);

        StringBuilder sql = new StringBuilder();
        //获取全部列
        sql.append(SqlHelper.insertIntoTable(entityClass, annotation.name()));
        sql.append(insertColumn(entityTable));
        sql.append(" VALUES ");

        //生成values 后的数据模板
        ArrayList<String> allInsertData = Lists.newArrayList();
        for (int index = 0; index < list.size(); index++) {
            allInsertData.add(insertValue(entityTable, index));
        }
        sql.append(allInsertData.stream().collect(Collectors.joining(",")));
        //System.out.println("sql  == " + sql.toString());
        return sql.toString();
    }

    /**
     * 获取所有需要插入的列
     */
    private static String insertColumn(EntityTable entityTable) {
        ArrayList<String> list = Lists.newArrayList();

        for (EntityColumn column : entityTable.getEntityClassColumns()) {
            if (!column.isInsertable()) {
                continue;
            }
            list.add(column.getColumn());
        }
        String column = list.stream().collect(Collectors.joining(","));
        return "(" + column + ")";
    }

    /**
     * 获取插入数据模板
     */
    private static String insertValue(EntityTable entityTable, int index) {
        ArrayList<String> list = Lists.newArrayList();
        //获取全部列
        //当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : entityTable.getEntityClassColumns()) {
            if (!column.isInsertable()) {
                continue;
            }
            list.add("#{arg0[" + index + "]." + column.getEntityField().getName() + "}");
        }
        String data = list.stream().collect(Collectors.joining(","));
        return "(" + data + ")";
    }
}
