package com.huatu.tiku.match.util;

import com.google.common.collect.Lists;
import com.google.common.collect.TreeBasedTable;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.chart.LineSeries;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 数据类型转化工具
 * Created by huangqingpeng on 2018/12/23.
 */
public class ConvertUtil {

    /**
     * 成绩曲线图转换
     *
     * @param table
     * @return
     */
    public static final Line table2LineSeries(TreeBasedTable<Long, String, ? extends Number> table) {
        final Set<String> columnKeySet = table.columnKeySet();
        final Set<Long> rowKeySet = table.rowKeySet();
        List<LineSeries> seriesList = new ArrayList<>(rowKeySet.size());
        for (Long dateStamp : rowKeySet) {
            List data = new ArrayList(columnKeySet.size());
            List<String> strData = new ArrayList<>(columnKeySet.size());
            for (String column : columnKeySet) {
                Number number = table.get(dateStamp, column);
                if (number == null) {//为空则进行初始化
                    number = Double.valueOf(0);
                }
                data.add(number);
                strData.add(String.valueOf(number));
            }

            final LineSeries lineSeries = LineSeries.builder()
                    .name(DateFormatUtils.format(dateStamp, "M-d"))
                    .data(data)
                    .strData(strData)
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
