package com.huatu.ztk.knowledge.service;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by linkang on 11/15/16.
 */
public class CountTest{


    @Test
    public void ddddd() {
        try {
            File file1 = new File("point_log.txt");
            String[] pointIds = FileUtils.readFileToString(file1).split("\\n");

            Map<String,Integer> map1 = new HashedMap();

            for (String pointId : pointIds) {
                if (map1.containsKey(pointId)) {
                    //计数+1
                    map1.put(pointId, map1.get(pointId) + 1);
                } else {
                    map1.put(pointId, 1);
                }
            }

            System.out.println(map1);



            List<QuestionCountObj> objList = new ArrayList<>();

            for (String qid : map1.keySet()) {
                QuestionCountObj obj = new QuestionCountObj(qid, map1.get(qid));
                objList.add(obj);
            }

            objList.sort(new Comparator<QuestionCountObj>(

            ) {
                @Override
                public int compare(QuestionCountObj o1, QuestionCountObj o2) {
                    return o2.getCount() - o1.getCount();
                }
            });


            int sum = 0;
            for (QuestionCountObj questionCountObj : objList) {

                System.out.println(questionCountObj.getQid() + ":" + questionCountObj.getCount());
                sum += questionCountObj.getCount();
            }

            System.out.println("sum=" + sum);
            System.out.println(objList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void ddddd2() {
        try {
            File file1 = new File("question_log.txt");
            String[] qids = FileUtils.readFileToString(file1).split("\\n");

            Map<String,Integer> map1 = new HashedMap();

            for (String qid : qids) {
                if (map1.containsKey(qid)) {
                    //计数+1
                    map1.put(qid, map1.get(qid) + 1);
                } else {
                    map1.put(qid, 1);
                }
            }

            List<QuestionCountObj> objList = new ArrayList<>();

            for (String qid : map1.keySet()) {
                QuestionCountObj obj = new QuestionCountObj(qid, map1.get(qid));
                objList.add(obj);
            }

            objList.sort(new Comparator<QuestionCountObj>(

            ) {
                @Override
                public int compare(QuestionCountObj o1, QuestionCountObj o2) {
                    return o2.getCount() - o1.getCount();
                }
            });


            int sum = 0;
            for (QuestionCountObj questionCountObj : objList) {

                System.out.println(questionCountObj.getQid() + ":" + questionCountObj.getCount());
                sum += questionCountObj.getCount();
            }

            System.out.println("sum=" + sum);
            System.out.println(objList.size());


            for (int i = 0; i < 30; i++) {
                QuestionCountObj questionCountObj = objList.get(i);
                System.out.print(questionCountObj.getQid() );
                if (i <29) {
                    System.out.print(",");
                }
            }


            System.out.println("map=" + map1);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class QuestionCountObj {
    String qid;
    int count;

    public QuestionCountObj(String qid, int count) {
        this.qid = qid;
        this.count = count;
    }

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
