package com.huatu.hadoop.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

@Builder
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@JsonIgnoreProperties({"pds"})
public class AbilityAssessmentWeekReport {
    @JsonIgnore
    @JSONField(serialize = false)
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

    private static Map<Integer, String[]> map3 = new HashMap<>();
    public static Map<Integer, String[]> map = new HashMap<>();
    public static Map<Integer, String[]> map2 = new HashMap<>();

    static {

        map.put(1, new String[]{"642", "754", "392", "435", "482"});
        map.put(2, new String[]{"3125", "3195", "3250", "3280", "3298", "3332"});
//        map.put(200100055, new String[]{"36667", "36710", "36735", "36748", "36789", "36846"});
        map.put(100100175, new String[]{"37098", "37099", "37100", "37101", "37175"});

        map.put(200100055, new String[]{"65836", "65877", "65903", "65904", "65905"});
        map.put(200100054, new String[]{"65836", "65877", "65903", "65904", "65905"});
        map.put(200100057, new String[]{"65836", "65877", "65903", "65904", "65905"});

        map.put(200100056, new String[]{"65836", "65877", "65903", "65904", "65905", "66089", "65902"});


        map2.put(1, new String[]{"判断推理", "数量关系", "常识判断", "言语理解与表达", "资料分析"});
        map2.put(2, new String[]{"政治", "经济", "管理", "公文", "人文科技", "法律"});
        map2.put(100100175, new String[]{"职业素质", "基础知识", "基本能力", "业务能力", "警务技能"});

        map2.put(200100055, new String[]{"常识判断", "言语理解与表达", "数量关系", "判断推理", "资料分析"});
        map2.put(200100054, new String[]{"常识判断", "言语理解与表达", "数量关系", "判断推理", "资料分析"});
        map2.put(200100057, new String[]{"常识判断", "言语理解与表达", "数量关系", "判断推理", "资料分析"});
        map2.put(200100056, new String[]{"常识判断", "言语理解与表达", "数量关系", "判断推理", "资料分析", "实验设计", "策略制定"});
    }

    public AbilityAssessmentWeekReport() {
        map3.put(1, new String[]{"642", "754", "392", "435", "482"});
        map3.put(2, new String[]{"3125", "3195", "3250", "3280", "3298", "3332"});
        map3.put(200100055, new String[]{"36667", "36710", "36735", "36748", "36789", "36846", "36831", "46617"});
        map3.put(100100175, new String[]{"37100", "37098", "37099", "37175", "37101"});

        map3.put(200100054, new String[]{"65836", "65877", "65903", "65904", "65905"});
        map3.put(200100057, new String[]{"65836", "65877", "65903", "65904", "65905"});

        map3.put(200100056, new String[]{"65836", "65877", "65903", "65904", "65905", "66089", "65902"});
    }

    /**
     * 科目
     */
    private Integer subject;
    @JsonIgnore
    @JSONField(serialize = false)
    private List<PointDetail> pds = new ArrayList<>();
    /**
     * 周
     */
    private String week;
    /**
     * 成绩单
     */
    private String grade;
    /**
     * 预测分
     */
    private Double predictedScore = 0.0;
    /**
     * 做题数量
     */
    private Long doExerciseNum = 0L;
    /**
     * 做题时长
     */
    private Long doExerciseTime = 0L;
    /**
     * 对比上周
     */
    private Long dif_doExerciseNum = 0L;
    /**
     * 平均做题
     */
    private Double ave_doExerciseNum = 0.00;

    /**
     * 排名
     */
    private Integer rank = 0;
    /**
     * 上周排名
     */
    private Integer lastWeekRank = 0;

    /**
     * 正确率
     */
    private Double accuracy = 0.0;
    /**
     * 平均正确率
     */
    private Double ave_accuracy = 0.0;
    /**
     * 对比上周正确率
     */
    private Double dif_accuracy = 0.0;
    /**
     * 做题速度
     */
    private Double speed = 0.0;
    /**
     * 平均做题速度
     */
    private Double ave_speed = 0.0;
    /**
     * 对比上周做题速度
     */
    private Double dif_speed = 0.0;

    private String[] pointName;
    private LinkedList<String> pointIds;
    private LinkedList<String> exerciseNum;
    private LinkedList<String> accuracies;
    private LinkedList<String> speeds;

    private LinkedList<LinkedList<String>> pointInfo;

    public static void setMap(Map<Integer, String[]> map) {
        AbilityAssessmentWeekReport.map = map;
    }

    public void setSubject(Integer subject) {
        this.subject = subject;
    }

    public void setPds(List<PointDetail> pds) {
        this.pds = pds;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setPredictedScore(Double predictedScore) {
        if (predictedScore.isNaN() || predictedScore.isInfinite()) {
            this.predictedScore = 0.00;
        } else {
            this.predictedScore = predictedScore;
        }
    }

    public void setDoExerciseNum(Long doExerciseNum) {
        this.doExerciseNum = doExerciseNum;
    }

    public void setDoExerciseTime(Long doExerciseTime) {
        this.doExerciseTime = doExerciseTime;
    }

    public void setDif_doExerciseNum(Long dif_doExerciseNum) {
        this.dif_doExerciseNum = dif_doExerciseNum;
    }

    public void setAve_doExerciseNum(Double ave_doExerciseNum) {
        if (ave_doExerciseNum.isNaN() || ave_doExerciseNum.isInfinite()) {
            this.ave_doExerciseNum = 0.00;
        } else {
            this.ave_doExerciseNum = ave_doExerciseNum;
        }
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public void setLastWeekRank(Integer lastWeekRank) {
        this.lastWeekRank = lastWeekRank;
    }

    public void setAccuracy(Double accuracy) {
        if (accuracy.isNaN() || accuracy.isInfinite()) {
            this.accuracy = 0.00;
        } else {
            this.accuracy = accuracy;
        }
    }

    public void setAve_accuracy(Double ave_accuracy) {
        if (ave_accuracy.isNaN() || ave_accuracy.isInfinite()) {
            this.ave_accuracy = 0.00;
        } else {
            this.ave_accuracy = ave_accuracy;
        }
    }

    public void setDif_accuracy(Double dif_accuracy) {
        if (dif_accuracy.isNaN() || dif_accuracy.isInfinite()) {
            this.dif_accuracy = 0.00;
        } else {
            this.dif_accuracy = dif_accuracy;
        }
    }

    public void setSpeed(Double speed) {

        if (speed.isNaN() || speed.isInfinite()) {
            this.speed = 0.00;
        } else {
            this.speed = speed;
        }
    }

    public void setAve_speed(Double ave_speed) {
        if (ave_speed.isNaN() || ave_speed.isInfinite()) {
            this.ave_speed = 0.00;
        } else {
            this.ave_speed = ave_speed;
        }
    }

    public void setDif_speed(Double dif_speed) {
        if (dif_speed.isNaN() || dif_speed.isInfinite()) {
            this.dif_speed = 0.00;
        } else {
            this.dif_speed = dif_speed;
        }
    }

    public void setPointName(String[] pointName) {
        this.pointName = pointName;
    }

    public void setPointIds(LinkedList<String> pointIds) {
        this.pointIds = pointIds;
    }

    public void setExerciseNum(LinkedList<String> exerciseNum) {
        this.exerciseNum = exerciseNum;
    }

    public void setAccuracies(LinkedList<String> accuracies) {
        this.accuracies = accuracies;
    }

    public void setSpeeds(LinkedList<String> speeds) {
        this.speeds = speeds;
    }

    public void setPointInfo(LinkedList<LinkedList<String>> pointInfo) {
        this.pointInfo = pointInfo;
    }

    public static LinkedList<LinkedList<String>> point2List(List<PointDetail> pds, Integer subject, String spn, String sp) {

        if (spn != null && !spn.equals("")) {

            map2.put(subject, spn.split(","));
        }

        List<String> allPoints = new ArrayList<>();
        String[] sps = sp.split(",");
        if (sp != null && !sp.equals("")) {

            map.put(subject, sps);
            allPoints = Arrays.asList(sps);
        } else {
            try {
                allPoints = Arrays.asList(map.get(subject));
            } catch (Exception e) {
                allPoints = Arrays.asList(new String[]{"642", "754", "392", "435", "482"});
            }
        }


        List<PointDetail> new_pds = new ArrayList<>();

        if (pds != null) {

            for (PointDetail pd : pds) {
                String s = pd.getPointKey().toString();
                if (allPoints.contains(s) || s.equals("0")) {
                    new_pds.add(pd);
                }
            }
        }

        LinkedList<LinkedList<String>> l = new LinkedList<>();
        LinkedList<String> point = new LinkedList<>();
        LinkedList<String> num = new LinkedList<>();
        LinkedList<String> accuracy = new LinkedList<>();
        LinkedList<String> speed = new LinkedList<>();

        Map<String, Map<String, Object>> m = new HashMap<>();


        if (new_pds != null && new_pds.size() > 0) {

            for (PointDetail pd : new_pds) {

                if (pd.getPointKey() != -1) {

                    Map<String, Object> m2 = new HashMap<>();
                    m2.put("key", pd.getPointKey().toString());
                    m2.put("num", pd.getPointNum().toString());
                    m2.put("acc", pd.getAccuracy().toString());
                    m2.put("sped", pd.getSpeed().toString());

                    m.put(pd.getPointKey().toString(), m2);

                }
            }


            for (String p : allPoints) {
                Set<String> set = m.keySet();

                if (set.contains(p)) {

                    point.addLast(m.get(p).get("key").toString());
                    num.addLast(m.get(p).get("num").toString());
                    accuracy.addLast(m.get(p).get("acc").toString());
                    speed.addLast(m.get(p).get("sped").toString());
                } else {
                    point.addLast(p);
                    num.addLast("0");
                    accuracy.addLast("0.05");
                    speed.addLast("0");
                }

            }

            l.addLast(point);
            l.addLast(num);
            l.addLast(accuracy);
            l.addLast(speed);

            return l;
        } else {
            for (String p : allPoints) {

                point.addLast(p);
                num.addLast("0");
                accuracy.addLast("0.05");
                speed.addLast("0");

            }

            l.addLast(point);
            l.addLast(num);
            l.addLast(accuracy);
            l.addLast(speed);
            return l;
        }
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
