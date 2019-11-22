package top.jbzm.index.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 主要负责存放通用的sql
 *
 * @author jbzm
 * @date 2018下午8:39
 **/
@Component
@Slf4j
public class JdbcUtils {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcUtils(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertData(String table, List<Map<String, Object>> data) {
        String sql;
        sql = String.format("insert into %s ", table);
        StringBuffer tableResult = new StringBuffer();
        StringBuffer valueResult = new StringBuffer();
        for (Map<String, Object> aData : data) {
            Set<Map.Entry<String, Object>> entries = aData.entrySet();
            for (Map.Entry<String, Object> entrie : entries) {
                tableResult.append(entrie.getKey()).append(",");
                valueResult.append("'").append(entrie.getValue()).append("'").append(",");
            }
        }
        tableResult.deleteCharAt(tableResult.length()-1);
        valueResult.deleteCharAt(valueResult.length()-1);
//        tableResult = stringList.stream().map(x -> x.split(":")[0] + ",").collect(Collectors.joining()).substring(0, substring.length() - 1);
//        valueResult = stringList.stream().map(x -> x.split(":")[1] + ",").collect(Collectors.joining()).substring(0, substring1.length() - 1);
        sql = String.format("%s(%s) values (%s)", sql, tableResult, valueResult);
        jdbcTemplate.update(sql);
    }

    public static String chagen(String lol, String loa) {
        return lol + loa;
    }
}

