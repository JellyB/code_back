package com.huatu.ztk.chart.util;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.chart.LineSeries;

import java.util.*;

/**
 * 图表工具包
 * Created by shaojieyue
 * Created time 2016-06-21 09:28
 */
public class ChartUtils {

    /**
     * 将table转换为符合highchart 曲线结构的数据
     * @param table
     * @return
     */
    public static final Line table2LineSeries(TreeBasedTable<String,String,? extends Number> table){
        final Set<String> columnKeySet = table.columnKeySet();
        final SortedSet<String> rowKeySet = table.rowKeySet();
        List<LineSeries> seriesList = new ArrayList<LineSeries>(rowKeySet.size());
        for (String lineSeriesName : rowKeySet) {
            List data = new ArrayList(columnKeySet.size());
            for (String column : columnKeySet) {
                Number number = table.get(lineSeriesName, column);
                if (number == null) {//为空则进行初始化
                    number = Double.valueOf(0);
                }
                data.add(number);
            }
            final LineSeries lineSeries = LineSeries.builder()
                    .name(lineSeriesName)
                    .data(data)
                    .build();
            seriesList.add(lineSeries);
        }
        final Line line = Line.builder()
                .categories(Lists.newArrayList(columnKeySet))
                .series(seriesList)
                .build();

        return line;
    }
}
