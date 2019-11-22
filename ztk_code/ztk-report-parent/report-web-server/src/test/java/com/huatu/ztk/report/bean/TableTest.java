package com.huatu.ztk.report.bean;

import com.google.common.collect.HashBasedTable;
import com.huatu.ztk.commons.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shaojieyue
 * Created time 2016-06-20 16:37
 */
public class TableTest {
    private static final Logger logger = LoggerFactory.getLogger(TableTest.class);

    public static void main(String[] args) {
        final HashBasedTable<String, String, Integer> basedTable = HashBasedTable.create();
        basedTable.put("ziji","1201",1);
        basedTable.put("ziji","1202",1);
        basedTable.put("ziji","1203",1);
        basedTable.put("ziji","1204",1);
        basedTable.put("ziji","1205",1);
        final String json = JsonUtil.toJson(basedTable);
        System.out.println(json);
    }
}
