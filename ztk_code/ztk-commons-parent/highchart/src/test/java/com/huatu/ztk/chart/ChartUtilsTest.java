package com.huatu.ztk.chart;

import com.google.common.collect.TreeBasedTable;
import com.huatu.ztk.chart.util.ChartUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-06-21 10:05
 */
public class ChartUtilsTest {

    @Test
    public void table2LineSeriesTest(){
        final TreeBasedTable<String, String, Number> basedTable = TreeBasedTable.create();
        String[] columns = {
                "2012",
                "2013",
                "2014",
                "2015"
        };
        String[] rows = {
                "自己",
                "平均"
        };
        for (String row : rows) {
            int i = 0;
            for (String column : columns) {
                basedTable.put(row, column, i++);
            }
        }
        final Line line = ChartUtils.table2LineSeries(basedTable);
        for (int i = 0; i < line.getCategories().size(); i++) {
            Assert.assertEquals(line.getCategories().get(i),columns[i]);
        }
        for (LineSeries lineSeries : line.getSeries()) {
            final List<? extends Number> data = lineSeries.getData();
            int i = 0;
            for (Number number : data) {
                Assert.assertEquals(number.longValue(),i );
            }
        }
    }
}
