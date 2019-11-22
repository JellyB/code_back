package com.huatu.hadoop.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.CollectionUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HBaseUtil {




    //    @Value("${hbase.zookeeper.quorum}")
//    private  String quorum;
    static Configuration conf;
    static Connection connection;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

    static {
        conf = new Configuration();
        conf.set("hbase.zookeeper.quorum", "192.168.100.68,192.168.100.70,192.168.100.72");
        conf.set("zookeeper.znode.parent", "/hbase");
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        try {
            connection = ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        System.out.println(get("user_time_shaft", "app_ztk558151151"));

    }

    /**
     * 根据知识点获取题
     */
    public static void getQuestionByPoint(int subject, int year, String area, List<String> list, Set<Integer> pointSet) {
        for (Object i : pointSet) {

            Map question = getQuestionPriorityYear(subject, year, area, i.toString());
            Object question_info = question.get("question_info");
            if (question_info != null) {

                String questions = ((Map) question_info).get("questions").toString();
                list.addAll(Arrays.asList(questions.split(",")));
            }
        }
    }

    public static void getAllQuestionByPoint(int subject, String area, List<String> list, Set<Integer> pointSet) {
        for (Object i : pointSet) {

            Map question = getAllQuestion(subject, area, i.toString());
            Object question_info = question.get("question_info");
            if (question_info != null) {

                String questions = ((Map) question_info).get("questions").toString();
                list.addAll(Arrays.asList(questions.split(",")));
            }
        }
    }

    public static Map getQuestionPriorityYear(int subject, int year, String area, String recommendPoint) {
        Map m2 = null;
        try {
            m2 = HBaseUtil.get("ztk_point_question", "" + subject + ":" + year + ":" + area + ":" + recommendPoint + "");
            if (m2.get("question_info") == null && year > 1999) {
                m2 = getQuestionPriorityYear(subject, year - 1, area, recommendPoint);
            } else if (m2.get("question_info") == null && year <= 1999) {
                m2 = HBaseUtil.get("ztk_point_question", "" + subject + ":" + year + ":" + area + ":" + recommendPoint + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (m2 != null) {

            return m2;
        } else {
            return null;
        }
    }

    public static Map getAllQuestion(int subject, String area, String point) {
        Map m2 = null;
        try {
            m2 = HBaseUtil.get("ztk_point_question", "" + subject + ":" + area + ":" + point + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (m2 != null) {

            return m2;
        } else {
            return null;
        }
    }

    /**
     * 插入数据
     */
    public static boolean put(String tablename, String row, String columnFamily,
                              String qualifier, String data) throws Exception {
        Table table = connection.getTable(TableName.valueOf(tablename));
        Put put = new Put(Bytes.toBytes(row));
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(qualifier),
                Bytes.toBytes(data));

        table.put(put);
        return true;
    }


    /**
     * 插入数据
     */
    public static boolean putMulti(String tablename, String row, String columnFamily,
                                   Map<String, Object> map) throws Exception {
        Table table = connection.getTable(TableName.valueOf(tablename));
        Put put = new Put(Bytes.toBytes(row));
        for (String key : map.keySet()) {
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(key),
                    Bytes.toBytes(map.get(key).toString()));
        }
        table.put(put);
        return true;
    }

    /**
     * 插入数据，指定时间
     */
    public static boolean putMultiOnTime(String tablename, String row, String columnFamily,
                                         Map<String, Object> map) throws Exception {

        Table table = connection.getTable(TableName.valueOf(tablename));
        Put put = new Put(Bytes.toBytes(row));
        put.setTimestamp(Long.parseLong(map.get("timestamp").toString()));

        for (String key : map.keySet()) {
            if (!key.equals("timestamp")) {
                put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(key),
                        Bytes.toBytes(map.get(key).toString()));
            }
        }
        table.put(put);
        return true;
    }

    //把result转换成map，方便返回json数据
    private static Map<String, Object> resultToMap(Result result, Integer versionNum) {

        Map<String, Object> resMap = new HashMap<String, Object>();
        List<Cell> listCell = result.listCells();
        Map<String, Object> tempMap = new HashMap<String, Object>();


        String rowname = "";
        List<String> familynamelist = new ArrayList<String>();
        if (!CollectionUtils.isEmpty(listCell)) {
            for (Cell cell : listCell) {
                byte[] rowArray = cell.getRowArray();
                byte[] familyArray = cell.getFamilyArray();
                byte[] qualifierArray = cell.getQualifierArray();
                byte[] valueArray = cell.getValueArray();
                int rowoffset = cell.getRowOffset();
                int familyoffset = cell.getFamilyOffset();
                int qualifieroffset = cell.getQualifierOffset();
                int valueoffset = cell.getValueOffset();
                int rowlength = cell.getRowLength();
                int familylength = cell.getFamilyLength();
                int qualifierlength = cell.getQualifierLength();
                int valuelength = cell.getValueLength();

                byte[] temprowarray = new byte[rowlength];
                System.arraycopy(rowArray, rowoffset, temprowarray, 0, rowlength);
                String temprow = Bytes.toString(temprowarray);

                byte[] tempqulifierarray = new byte[qualifierlength];
                System.arraycopy(qualifierArray, qualifieroffset, tempqulifierarray, 0, qualifierlength);
                String tempqulifier = Bytes.toString(tempqulifierarray);

                byte[] tempfamilyarray = new byte[familylength];
                System.arraycopy(familyArray, familyoffset, tempfamilyarray, 0, familylength);
                String tempfamily = Bytes.toString(tempfamilyarray);

                byte[] tempvaluearray = new byte[valuelength];
                System.arraycopy(valueArray, valueoffset, tempvaluearray, 0, valuelength);
                String tempvalue = Bytes.toString(tempvaluearray);


                tempMap.put(tempfamily + ":" + tempqulifier + ":" + cell.getTimestamp(), tempvalue);
                if (versionNum > 1) {

                    tempMap.put(tempfamily + ":" + "timestamp" + ":" + cell.getTimestamp(), cell.getTimestamp());
                }


                rowname = temprow;
                String familyname = tempfamily;
                if (familynamelist.indexOf(familyname) < 0) {
                    familynamelist.add(familyname);
                }
            }
        }

        resMap.put("rowname", rowname);
        for (String familyname : familynamelist) {

            HashMap<Long, Object> TimestampMap = new HashMap<Long, Object>();

            HashMap<String, Object> tempFilterMap = new HashMap<String, Object>();

            for (String key : tempMap.keySet()) {
                String[] keyArray = key.split(":");

                String timeStamp = keyArray[keyArray.length - 1];

                HashMap<String, Object> o = (HashMap<String, Object>) TimestampMap.get(Long.parseLong(timeStamp));

                if (o != null) {
                    if (keyArray[0].equals(familyname)) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < keyArray.length - 1; i++) {
                            sb.append(keyArray[i]).append(":");
                        }
                        o.put(sb.deleteCharAt(sb.length() - 1).toString(), tempMap.get(key));
                    }
                    TimestampMap.put(Long.parseLong(timeStamp), o);
                } else {
                    if (keyArray[0].equals(familyname)) {
                        HashMap<String, Object> t2 = new HashMap();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < keyArray.length - 1; i++) {
                            sb.append(keyArray[i]).append(":");
                        }
                        t2.put(sb.deleteCharAt(sb.length() - 1).toString(), tempMap.get(key));

                        TimestampMap.put(Long.parseLong(timeStamp), t2);
                    }
                }
            }

            resMap.put(familyname, sortMapByKey(TimestampMap));
        }

        return resMap;
    }

    public static Map<String, Object> sortMapByKey(Map<Long, Object> map) {

        Map<String, Object> rmap = new HashMap<>();

        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<Long, Object> sortMap = new TreeMap<Long, Object>(
                new Comparator<Long>() {
                    @Override
                    public int compare(Long o1, Long o2) {
                        int i = Long.compare(o2, o1);
                        return i;
                    }
                });

        sortMap.putAll(map);
        TreeMap<Long, Object> tmap = (TreeMap<Long, Object>) sortMap;


        Long firstKey = tmap.firstKey();
        Map firstMap = (Map) sortMap.get(firstKey);

        if (sortMap.size() > 1) {

            Long lastKey = tmap.lastKey();
            Map lastMap = (Map) sortMap.get(lastKey);

            rmap.put("firstMap", firstMap);
            rmap.put("lastMap", lastMap);
        } else {

            rmap.putAll(firstMap);
        }
        return rmap;
    }


    public static Map sortByValue(Map map) {

        List list = new LinkedList(map.entrySet());

        Collections.sort(list, (Comparator) (o1, o2) -> ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue()));
        Map result = new LinkedHashMap();

        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }


    public static Map get(String tablename, String row) throws Exception {
        Table table = connection.getTable(TableName.valueOf(tablename));
        Get get = new Get(Bytes.toBytes(row));
        Result result = table.get(get);

        return resultToMap(result, 1);
    }

    public static Double getDIfScore(String tablename, String row) throws Exception {
        Table table = connection.getTable(TableName.valueOf(tablename));
        Get get = new Get(Bytes.toBytes(row));
        get.readVersions(10);
        Result result = table.get(get);

        TreeMap<Long, Double> tm = new TreeMap<>(new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return o2.compareTo(o1);
            }
        });

        List<Cell> cells = result.listCells();
        if (!CollectionUtils.isEmpty(cells)) {
            for (Cell cell : cells) {

                long timestamp = cell.getTimestamp();
                byte[] familyArray = cell.getFamilyArray();
                byte[] qualifierArray = cell.getQualifierArray();
                byte[] valueArray = cell.getValueArray();

                int familyoffset = cell.getFamilyOffset();
                int qualifieroffset = cell.getQualifierOffset();
                int valueoffset = cell.getValueOffset();

                int familylength = cell.getFamilyLength();
                int qualifierlength = cell.getQualifierLength();
                int valuelength = cell.getValueLength();


                byte[] tempqulifierarray = new byte[qualifierlength];
                System.arraycopy(qualifierArray, qualifieroffset, tempqulifierarray, 0, qualifierlength);
                String tempqulifier = Bytes.toString(tempqulifierarray);

                byte[] tempfamilyarray = new byte[familylength];
                System.arraycopy(familyArray, familyoffset, tempfamilyarray, 0, familylength);
                String tempfamily = Bytes.toString(tempfamilyarray);

                byte[] tempvaluearray = new byte[valuelength];
                System.arraycopy(valueArray, valueoffset, tempvaluearray, 0, valuelength);
                String tempvalue = Bytes.toString(tempvaluearray);

                if (tempfamily.equals("ability_assessment_info") && tempqulifier.equals("predictScore")) {

                    tm.put(timestamp, Double.parseDouble(tempvalue));

//                    System.out.println(tempvalue+"_"+timestamp);
                }
            }
        }
        Double nowScorce = tm.get(tm.firstKey());


        Collection<Double> values = tm.values();
        for (Double ds : values) {
            if (nowScorce.compareTo(ds) != 0) {
                nowScorce = (nowScorce - ds) / ds;


                break;
            }
        }

        return nowScorce >= 1 ? 0 : nowScorce < -1 ? 0 : nowScorce;
    }

    public static Map get2Version(String tablename, String row) throws Exception {
        Table table = connection.getTable(TableName.valueOf(tablename));
        Get get = new Get(Bytes.toBytes(row));
        get.setMaxVersions(2);
        Result result = table.get(get);

        return resultToMap(result, 2);
    }

    //查看全表
    public static String scan(String tablename) throws Exception {
        Table table = connection.getTable(TableName.valueOf(tablename));
        Scan s = new Scan();
        ResultScanner rs = table.getScanner(s);

        List<Map<String, Object>> resList = new ArrayList<Map<String, Object>>();
        for (Result r : rs) {
            Map<String, Object> tempmap = resultToMap(r, 1);
            resList.add(tempmap);
        }
        return resList.toString();
    }


    //新建表(应该校验是否已存在同名表)
    public static boolean create(String tableName, String columnFamily)
            throws Exception {
        HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();

        String[] columnFamilyArray = columnFamily.split(",");
        HColumnDescriptor[] hColumnDescriptor = new HColumnDescriptor[columnFamilyArray.length];
        for (int i = 0; i < hColumnDescriptor.length; i++) {
            hColumnDescriptor[i] = new HColumnDescriptor(columnFamilyArray[i]);
        }
        HTableDescriptor familyDesc = new HTableDescriptor(TableName.valueOf(tableName));
        for (HColumnDescriptor columnDescriptor : hColumnDescriptor) {
            familyDesc.addFamily(columnDescriptor);
        }
        HTableDescriptor tableDesc = new HTableDescriptor(TableName.valueOf(tableName), familyDesc);

        admin.createTable(tableDesc);
        System.out.println(tableName + " create successfully!");
        return true;

    }

}
