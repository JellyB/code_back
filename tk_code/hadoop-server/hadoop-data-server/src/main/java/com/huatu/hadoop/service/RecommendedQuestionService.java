package com.huatu.hadoop.service;

import com.huatu.hadoop.bean.RecommendedQuestionDTO;
import com.huatu.hadoop.util.HBaseUtil;
import org.springframework.stereotype.Service;

import java.util.*;

@SuppressWarnings("ALL")
@Service
public class RecommendedQuestionService {

    public RecommendedQuestionDTO getRecommendedQuestion(String userId, String subject, String year, String area, RecommendedQuestionDTO recommendedQuestionDTO) {
        try {
            HBaseUtil hbase = new HBaseUtil();

            Map m = HBaseUtil.get(RecommendedQuestionDTO.ZTK_USER_QUESTION_POINT_NOTGRASP, userId);
            Map points = (Map) m.get("question_point_info");

            int flag = 0;
            int[] waitRecommendArr = new int[7];
            int[] backUpArr = new int[3];

            if (points == null) {
                flag = 1;
                waitRecommendArr = null;
                backUpArr = new int[10];
            }
            Set<Integer> waitRecommendPoint = new TreeSet<>(Comparator.comparingInt(o -> o));
            Set<Integer> backUpPoint = new TreeSet<>(Comparator.comparingInt(o -> o));

            if (flag == 0) {

                Map sortedMap = HBaseUtil.sortByValue(points);
                Set notGraspPoints = sortedMap.keySet();
                Set<String> userNotGraspPoints = new HashSet<>();

                for (Object o : notGraspPoints) {
                    String tmp = o.toString();
                    if (tmp.startsWith(subject + ":")) {
                        userNotGraspPoints.add(tmp);
                    }
                }
                for (Object o : userNotGraspPoints) {
                    String tmp = o.toString();
                    String[] split = tmp.split(":");
                    if (split.length == 4) {

                        waitRecommendPoint.add(Integer.parseInt(split[split.length - 1]));
                    }
                }

                /**
                 * 错题推荐
                 */
                List<String> waitRecommendQuestions = new ArrayList<>();
                HBaseUtil.getQuestionByPoint(Integer.parseInt(subject), Integer.parseInt(year), area, waitRecommendQuestions, waitRecommendPoint);

                int waitRecommendSize = waitRecommendQuestions.size();
                int length1 = waitRecommendArr.length;
                if (waitRecommendSize < length1) {

                    waitRecommendArr = new int[waitRecommendSize];
                    backUpArr = new int[10 - waitRecommendSize];
                }

                if (waitRecommendSize > 0){
                    for (int i = 0; i < length1; i++) {
                        int i1 = Integer.parseInt(waitRecommendQuestions.get((int) (Math.random() * waitRecommendSize)));
                        waitRecommendArr[i] = i1;
                    }
                }
            }
            /**
             *
             */
            // 获取用户u科目下所有知识点
            Map allPoint = null;

            allPoint = HBaseUtil.get(RecommendedQuestionDTO.ZTK_SUBJECT_POINT, subject);

            List<String> pointList = Arrays.asList(((Map) allPoint.get("point_info")).get("points").toString().split(","));

            for (String s : pointList) {
                backUpPoint.add(Integer.parseInt(s));
            }
            //除去需要推荐的知识点(获得未知掌握程度的知识点)
            backUpPoint.removeAll(waitRecommendPoint);

            List<String> backUpRecommendQuestions = new ArrayList<>();
            HBaseUtil.getAllQuestionByPoint(Integer.parseInt(subject), area, backUpRecommendQuestions, backUpPoint);

            int backUpSize = backUpRecommendQuestions.size();

            for (int i = 0; i < backUpArr.length; i++) {
                int i1 = Integer.parseInt(backUpRecommendQuestions.get((int) (Math.random() * backUpSize)));
                backUpArr[i] = i1;
            }


            recommendedQuestionDTO.setWaitRecommendPoint(waitRecommendPoint);
            recommendedQuestionDTO.setWaitRecommendArr(waitRecommendArr);

            recommendedQuestionDTO.setBackUpPoint(backUpPoint);
            recommendedQuestionDTO.setBackUpArr(backUpArr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recommendedQuestionDTO;

    }
}
