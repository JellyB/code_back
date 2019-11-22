package com.huatu.tiku.util.question;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.primitives.Ints;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * question相关处理
 *
 * @author zhaoxi
 */
public class QuestionConvert {

    public static Map<Integer, String> point_map = new HashMap<Integer, String>();

    /**
     * 处理答案
     *
     * @param standAnswer
     * @return
     */
    public static int answerParse(String standAnswer) {
        Map<String, String> answerMap = new HashMap<String, String>();
        answerMap.put("A", "1");
        answerMap.put("B", "2");
        answerMap.put("C", "3");
        answerMap.put("D", "4");
        answerMap.put("E", "5");
        answerMap.put("F", "6");
        answerMap.put("G", "7");
        answerMap.put("H", "8");

        char[] chars = standAnswer.toUpperCase().toCharArray();
        Arrays.sort(chars);
        StringBuilder sb = new StringBuilder();
        for (char aChar : chars) {
            final String ss = answerMap.get(aChar + "");
            if (ss != null) {
                sb.append(ss);
            }
        }
        if (sb.length() < 1) {
            sb.append("3");
        }
        return Integer.valueOf(sb.toString());
    }


    /**
     * 获取知识点的对应关系 key：pointId value：parentId
     *
     * @return
     * @throws SQLException
     */
    public static Map<Integer, Integer> getPointMap() throws SQLException {
        String sql = "SELECT PUKEY,name,prev_kp FROM v_knowledge_point  order by prev_kp ASC,PUKEY ASC ";
        final SqlRowSet resultSet = null;
        Map<Integer, Integer> data = new HashMap<Integer, Integer>();
        while (resultSet.next()) {
            int pointId = resultSet.getInt("PUKEY");
            int parent = resultSet.getInt("prev_kp");
            String name = resultSet.getString("name");
            data.put(pointId, parent);
            point_map.put(pointId, name);
        }
        return data;
    }


    /**
     * 获取问题的知识点id
     *
     * @param questionId
     * @return
     */
    public static List<Integer> getKnowledge(int questionId, Map<Integer, Integer> pointMap, String questionPkTable) {
        String sql = "select * from " + questionPkTable + " where question_Id=" + questionId + " and bb102=1";
        List<Integer> ids = new ArrayList<Integer>();
        SqlRowSet resultSet = null;
        while (resultSet.next()) {
            final int pkId = resultSet.getInt("pk_id");
            ids.add(pkId);
        }

        Integer oneLevel = 0;
        Integer twoLevel = 0;
        Integer threeLevel = 0;
        try {//[754, 761, 769])
            //panent-> sub list
            final ArrayListMultimap<Integer, Integer> multimap = ArrayListMultimap.create();
            for (Integer id : ids) {
                Integer parent = pointMap.get(id);
                multimap.put(parent, id);
                while (parent > 0) {
                    int sub = parent;
                    parent = pointMap.get(sub);
                    multimap.put(parent, sub);
                }
            }

            oneLevel = multimap.get(0).get(0);//one level
            twoLevel = multimap.get(oneLevel).get(0);
            threeLevel = multimap.get(twoLevel).get(0);
        } catch (Exception e) {
            oneLevel = 392;
            twoLevel = 398;
            threeLevel = 403;
        }
        return Ints.asList(oneLevel, twoLevel, threeLevel);
    }


    public static String convertQuestionType(String typeName) {
        return "【题型】" + typeName + "\n";
    }

    public static String convertStem(String stem) {
        return "【题干】" + stem + "\n";
    }

    public static String convertAnswer(String answer) {
        return "【答案】" + answer + "\n";
    }

    public static String convertAnalysis(String analysis) {
        return "【解析】" + analysis + "\n";
    }

    public static String convertExtend(String extend) {
        return "【拓展】" + extend + "\n";
    }


    public static String convertTag(List<String> tagNames) {
        if (CollectionUtils.isEmpty(tagNames)) {
            return "【标签】 \n";
        }
        return "【标签】" + String.join(",", tagNames) + " \n";
    }

    public static String convertKnowledge(List<String> knowledgeList) {
        if (CollectionUtils.isEmpty(knowledgeList)) {
            return "【知识点】 \n";
        }
        return "【知识点】" + String.join(",", knowledgeList) + " \n";
    }

    public static String convertDifficult(String difficult) {
        return "【难度】" + difficult + "\n";
    }

    public static String convertChoice(List<String> choices) {
        char ch = 'A';
        StringBuilder stringBuilder = new StringBuilder("");
        for (String choice : choices) {
            stringBuilder.append(ch).append("、")
                    .append(choice.replaceAll("<[^<]+>", ""))
                    .append("\n");
            ch = (char) (ch + 1);
        }
        System.out.println("QuesitonConvert.convertChoice = " + stringBuilder.toString());
        return stringBuilder.toString();
    }

    /**
     * 去除html标签，只保留文本内容
     *
     * @return
     */
    public static String htmlConvertContent(String content) {
        if(StringUtils.isBlank(content)){
            return content;
        }
        content = content.replace("&nbsp"," ");
        StringBuilder sb = new StringBuilder(content);
        Pattern pattern = Pattern.compile("<[^>]+>");
        Matcher matcher = pattern.matcher(sb);
        int index = 0;
        while (matcher.find(index)) {
            String group = matcher.group();
            if (group.indexOf("br") > -1) {
                index = matcher.start();
                sb.replace(matcher.start(),matcher.end(),"\n");
                continue;
            }
            String reg = "\u4e00-\u9fa5";
            boolean matches = group.matches(reg);
            if (!matches) {
                index = matcher.start();
                sb.delete(matcher.start(), matcher.end());
                continue;
            }
            index = matcher.end();
        }
        return sb.toString();
    }

    public static String convertStemBefore(String typeName , String stem) {
        StringBuilder content = new StringBuilder("");
        content.append("（").append(typeName).append("）");
        content.append(stem).append("\n");
        return content.toString();
    }
}
