package com.huatu.hadoop.service;

import com.huatu.hadoop.bean.AATopUser;
import com.huatu.hadoop.bean.AbilityAssessment;
import com.huatu.hadoop.bean.AbilityAssessmentWeekReport;
import com.huatu.hadoop.bean.PointDetail;
import com.huatu.hadoop.util.CalendarUtil;
import com.huatu.hadoop.util.HBaseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class AbilityAssessmentService {

    @Autowired
    private MysqlService mysqlService;
    @Autowired
    private HBaseUtil hBaseUtil;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-ww");
    private static SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
    private static SimpleDateFormat MM_dd = new SimpleDateFormat("MM.dd");
    private static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");


    //    private static String hbase_total_station_ability_assessment = "test_total_station_ability_assessment";
//    private static String hbase_week_ability_assessment = "test_week_ability_assessment";
//    private static String hbase_week_top10_ability_assessment = "test_week_top10_ability_assessment";
    private static String hbase_total_station_ability_assessment = "total_station_ability_assessment";
    private static String hbase_week_ability_assessment = "week_ability_assessment";
    private static String hbase_week_top10_ability_assessment = "week_top10_ability_assessment";

    private static String hbase_family = "ability_assessment_info";


    private static Map<Integer, Map<Integer, Integer>> m = new HashMap<>();

    static {

        Map<Integer, Integer> m1 = new HashMap<>();
        m1.put(392, 20);
        m1.put(435, 40);
        m1.put(482, 20);
        m1.put(642, 40);
        m1.put(754, 20);
        m.put(1, m1);

        Map<Integer, Integer> m2 = new HashMap<>();
        m2.put(3125, 40);
        m2.put(3195, 10);
        m2.put(3250, 10);
        m2.put(3280, 10);
        m2.put(3298, 30);
        m2.put(3332, 30);
        m.put(2, m2);


        Map<Integer, Integer> m3 = new HashMap<>();
        m3.put(36667, 20);
        m3.put(36710, 40);
        m3.put(36735, 20);
        m3.put(36748, 40);
        m3.put(36789, 20);
        m3.put(36831, 20);
        m3.put(36846, 20);
        m3.put(46617, 20);
        m.put(3, m3);


        Map<Integer, Integer> m4 = new HashMap<>();
        m4.put(37098, 20);
        m4.put(37099, 40);
        m4.put(37100, 30);
        m4.put(37101, 15);
        m4.put(37175, 10);
        m.put(100100175, m4);

        Map<Integer, Integer> m5 = new HashMap<>();
        m5.put(65217, 30);
        m5.put(65218, 10);
        m5.put(65219, 20);
        m5.put(65220, 10);
        m.put(100100262, m5);
    }

    public List<Map<String, Object>> getUserAccuracy(Long userId, Integer subject) throws Exception {

        try {

            Map<Integer, Integer> imap = m.get(subject);

            Map totalStationReport = HBaseUtil.get("cuoti_report", userId + "-" + subject);

            Map aai = (Map) totalStationReport.get("report_info");
            List<Map<String, Object>> l = new ArrayList<>();
            if (totalStationReport == null || aai == null) {
                return l;
            }
            String spn = Bytes.toString(aai.get("spn").toString().getBytes());
            String sp = Bytes.toString(aai.get("sp").toString().getBytes());

            String[] pointName = spn.split(",");
            String[] pointid = sp.split(",");


            for (int i = 0; i < pointid.length; i++) {
                Map<String, Object> m = new HashMap<>();
                int i1 = Integer.parseInt(pointid[i]);
                m.put("id", i1);

                m.put("name", pointName[i]);
                m.put("error", 0);
                m.put("accuracy", 0.05);
                m.put("num", 0);

                l.add(m);
            }
            int count = 0;
            String grade = aai.get("grade").toString();
            if (grade != null && !grade.equals("")) {
                String[] points = grade.split("_");

                for (String s : points) {

                    String[] sr = s.split(":");
                    int point = Integer.parseInt(sr[0]);
                    int correct = Integer.parseInt(sr[1]);
                    int num = Integer.parseInt(sr[2]);
                    for (int i = 0; i < l.size(); i++) {
                        Map<String, Object> objectMap = l.get(i);
                        if (Integer.parseInt(objectMap.get("id").toString()) == point) {

                            Integer integer = imap.get(point);
                            objectMap.put("accuracy", correct * 1.0 / num);
                            objectMap.put("num", num);
                            objectMap.put("error", num - correct);
                            if (num < integer) {
                                objectMap.put("accuracy", 0.05);
                            }

                            count += num;
                            continue;
                        }
                    }
                }
            }
//            m3.put("num", count);
            return l;
        } catch (Exception e) {
            return null;
        }
    }


    public AbilityAssessment getUserReport(Long userId, Integer subject) throws Exception {

        AbilityAssessment aa = new AbilityAssessment();
        try {
            AbilityAssessmentWeekReport weekReport = new AbilityAssessmentWeekReport();
            aa.setWeekReport(weekReport);

            Map totalStationReport;

            try {
                totalStationReport = hBaseUtil.get2Version(hbase_total_station_ability_assessment, userId + "-" + subject);
            } catch (Exception e) {
                Thread.sleep(3000);
                totalStationReport = hBaseUtil.get2Version(hbase_total_station_ability_assessment, userId + "-" + subject);
            }

            Object aa_info = totalStationReport.get(hbase_family);
            String spn = "";
            String sp = "";
            Map nowUpdate = null;
            Long doExerciseNum = 0L;
            aa.setUserId(userId);
            aa.setSubject(subject);
            if (aa_info != null) {


                nowUpdate = (Map) ((Map) aa_info).get("firstMap");
                Map lastReport = (Map) ((Map) aa_info).get("lastMap");

                if (nowUpdate == null) {
                    nowUpdate = (Map) aa_info;
                }
                if (lastReport == null) {
                    lastReport = nowUpdate;
                }


                Double lastScore = Double.parseDouble(lastReport.get("predictScore").toString());
                Double nowScore = Double.parseDouble(nowUpdate.get("predictScore").toString());

                spn = Bytes.toString(nowUpdate.get("spn").toString().getBytes());
                sp = Bytes.toString(nowUpdate.get("sp").toString().getBytes());


                Integer nowRank = Integer.parseInt(nowUpdate.get("rank5").toString());
                /**
                 * 有效排名
                 */
                Integer nowQRank = Integer.parseInt(nowUpdate.get("rank").toString());
                Integer lastRank = Integer.parseInt(lastReport.get("rank").toString());
                aa.setDif_rank(lastRank - nowRank);

                Integer allUserCount = Integer.parseInt(nowUpdate.get("userCount").toString());
                Integer allQUserCount = Integer.parseInt(nowUpdate.get("qualifiedUserCount").toString());

                if (nowScore < 20.0) {
                    aa.setSurpass_man(allUserCount - nowRank);
                    aa.setHasBeat(0.00);
                } else {
                    aa.setSurpass_man(allQUserCount - nowQRank);
                    Double v = aa.getSurpass_man() * 1.0 / allQUserCount;
                    BigDecimal decimal = new BigDecimal(aa.getSurpass_man() * 1.0).divide(new BigDecimal(allQUserCount * 1.0), 4, RoundingMode.HALF_DOWN).subtract(new BigDecimal(0.0005));
//                    aa.setHasBeat(decimal.doubleValue());
                    aa.setHasBeat(decimal.doubleValue() < 0 ? decimal.doubleValue() * -1 : decimal.doubleValue());

                }

                Double dIfScore = HBaseUtil.getDIfScore(hbase_total_station_ability_assessment, userId + "-" + subject);
                aa.setDif_predictedScore(dIfScore);
                try {
                    if (dIfScore.compareTo(0.0) == 0) {
                        Map on = HBaseUtil.get("user_ability_assessment_on", Long.toString(userId));

                        Object year_on_year = ((Map) on.get("year_on_year_info")).get("year_on_year");
                        aa.setDif_predictedScore(Double.parseDouble(year_on_year == null ? "0.00" : year_on_year.toString()));

                    } else {
                        HBaseUtil.put("user_ability_assessment_on", Long.toString(userId), "yer_info", "year_on_year", dIfScore.toString());
                    }

                } catch (Exception e) {
                    log.info("user_ability_assessment_on {}", userId.toString());
                }

                if (nowScore == 0.00) {
                    aa.setHasBeat(0.00);
                }

                aa.setReportCard(nowUpdate.get("grade").toString());

                aa.setDoExerciseDay(Long.parseLong(nowUpdate.get("exerciseDay").toString()));
                aa.setPredictedScore(nowScore);

                List<PointDetail> pds = new ArrayList<>();
                String[] points = nowUpdate.get("grade").toString().split("_");
                Integer correctSum = 0;
                Integer Sum = 0;
                Integer time = 0;
                for (String s : points) {
                    PointDetail pd = new PointDetail();
                    String[] sr = s.split(":");
                    pd.setPointKey(Integer.parseInt(sr[0]));
                    if (pd.getPointKey() != 0) {

                        String[] strings = AbilityAssessmentWeekReport.map.get(subject);
                        List<String> strings1 = Arrays.asList(sp != null ? sp.split(",") : strings);

                        if (strings1.contains(pd.getPointKey().toString())) {

                            pd.setPointCorrect(Integer.parseInt(sr[1]));
                            pd.setPointNum(Integer.parseInt(sr[2]));
                            pd.setPointTime(Integer.parseInt(sr[3]));
                            pd.setAccuracy(pd.getPointNum() == 0 ? 0.0 : pd.getPointCorrect() * 1.0 / pd.getPointNum());
                            pd.setSpeed(pd.getPointNum() == 0 ? 0.0 : pd.getPointTime() * 1.0 / pd.getPointNum());

                            time += Integer.parseInt(sr[3]);
                            correctSum += Integer.parseInt(sr[1]);
                            Sum += Integer.parseInt(sr[2]);
                            pds.add(pd);
                        }
                    }

                }

                aa.setDoExerciseNum(Long.parseLong(Sum.toString()));
                if (Sum < 1) {
                    aa.setFlag(false);
                    aa.setDoExerciseDay(0L);
                }

                aa.setRank(nowQRank);
                if (nowScore == 0.00) {
                    aa.setRank(0);
                }

                Long doExerciseTime = aa.getDoExerciseTime();
                aa.setDoExerciseSpead(time * 1.0 / Sum);
                aa.setAccuracy(Sum == 0 ? 0.00 : (correctSum * 1.0 / Sum));
                aa.setDoExerciseTime(Long.parseLong(time.toString()));
                doExerciseNum = aa.getDoExerciseNum();

                Map<String, String> sta = aa.getSta();
                sta.put("userCount", nowUpdate.get("qualifiedUserCount") == null ? "" : nowUpdate.get("qualifiedUserCount").toString());
                sta.put("correctNum", nowUpdate.get("correctNum") == null ? "" : nowUpdate.get("correctNum").toString());
                sta.put("exerciseTimeTotal", nowUpdate.get("exerciseTimeTotal") == null ? "" : nowUpdate.get("exerciseTimeTotal").toString());
                sta.put("quesCount", nowUpdate.get("quesCount") == null ? "" : nowUpdate.get("quesCount").toString());
                sta.put("rankByExeDay", nowUpdate.get("rank2") == null ? "" : nowUpdate.get("rank2").toString());
                sta.put("rankByExeTime", nowUpdate.get("rank3") == null ? "" : nowUpdate.get("rank3").toString());
                sta.put("rankByExeNum", nowUpdate.get("rank4") == null ? "" : nowUpdate.get("rank4").toString());

            } else {
                aa.setFlag(false);
            }

            /**
             * 周
             */
            Date weekStartDay = yyyyMMdd.parse(yyyyMMdd.format(Long.parseLong(CalendarUtil.getWeekStart(yyyyMMdd.format(new Date(System.currentTimeMillis()))))));


            String _week = sdf.format(weekStartDay);
            int _year1 = Integer.parseInt(yyyy.format(weekStartDay));
            int _year2 = Integer.parseInt(yyyy.format(weekStartDay));

            int i1 = Integer.parseInt(_week.split("-")[1]);

            int last_w = i1 - 1;
            int llast_w = i1 - 2;

            if (i1 == 1) {
                _year1 = _year1 - 1;
                _year2 = _year2 - 1;

                last_w = 52;
                llast_w = 51;
            } else if (i1 == 2) {
                _year2 = _year2 - 1;
                llast_w = 52;
            }

            String presentDay = _year1 + "-" + last_w;
            String lastDay = _year2 + "-" + llast_w;


            Map nowWeek;
            try {
                nowWeek = HBaseUtil.get2Version(hbase_week_ability_assessment, userId + "-" + subject + "-" + presentDay);
            } catch (Exception e) {
                Thread.sleep(3000);
                nowWeek = HBaseUtil.get2Version(hbase_week_ability_assessment, userId + "-" + subject + "-" + presentDay);
            }


            Map lastWeek;
            try {
                lastWeek = HBaseUtil.get(hbase_week_ability_assessment, userId + "-" + subject + "-" + lastDay);
            } catch (Exception e) {
                Thread.sleep(3000);
                lastWeek = HBaseUtil.get(hbase_week_ability_assessment, userId + "-" + subject + "-" + lastDay);
            }


            long weekStartTime = sdf.parse(presentDay).getTime() + 24 * 60 * 60 * 1000L;
            String startWeek = MM_dd.format(new Date(weekStartTime));
            String endWeek = MM_dd.format(new Date(weekStartTime + 6 * 24 * 60 * 60 * 1000L));
            weekReport.setWeek(startWeek + "~" + endWeek);
//            weekReport.setWeek("12.31" + "~" + "1.6");
            aa.setUpdateTime(new Date(weekStartTime + 7 * 24 * 60 * 60 * 1000L).getTime());

            Object presentWeekInfo = nowWeek.get(hbase_family);


            String[] weekTextArea = new String[]{
                    "", "", "", ""
            };

            String[] pointname = (spn != null && !spn.equals((""))) ? spn.split(",") : AbilityAssessmentWeekReport.map2.get(subject);
            if (presentWeekInfo != null) {

                Map recentWeekInfo = (Map) ((Map) presentWeekInfo).get("firstMap");
                Map oldWeekInfo = (Map) ((Map) presentWeekInfo).get("lastMap");
                if (recentWeekInfo == null) {
                    recentWeekInfo = (Map) presentWeekInfo;
                }
                if (oldWeekInfo == null) {
                    oldWeekInfo = recentWeekInfo;
                }


                /**
                 * 周报--正确率
                 */
                Integer recentWeekCorrect = 0;
                Integer recentWeekSum = 0;
                List<PointDetail> recentPointReport = gradeAnalyze(recentWeekInfo.get("grade").toString().split("_"), recentWeekCorrect, recentWeekSum);
                for (PointDetail pd : recentPointReport) {
                    if (pd.getPointKey() != 0) {
                        recentWeekCorrect += pd.getPointCorrect();
                        recentWeekSum += pd.getPointNum();
                    }
                }

                double weekUserAccuracy = Double.parseDouble(recentWeekInfo.get("week_accuracy").toString());
                weekReport.setAccuracy(recentWeekCorrect * 1.0 / recentWeekSum);

                /**
                 *
                 */
                weekReport.setGrade(recentWeekInfo.get("grade").toString());

                /**
                 * 周用户统计
                 */
                Object weekUCount = recentWeekInfo.get("userCount");
                int weekUserCount = weekUCount == null ? 0 : Integer.parseInt(weekUCount.toString());
                /**
                 * 周做题数量统计
                 */
                //
                Object weekQCount = recentWeekInfo.get("quesCount");
                int weekQuesCount = weekQCount == null ? 0 : Integer.parseInt(weekQCount.toString());
                /**
                 * 周做题时间统计
                 */
                Object weekTime = recentWeekInfo.get("timeTotal");
                long weekTimeSum = weekTime == null ? 0 : Long.parseLong(weekTime.toString());
                /**
                 * 周正确数量统计
                 */
                Object weekCCount = recentWeekInfo.get("correctNum");
                int weekCorrectNum = weekCCount == null ? 0 : Integer.parseInt(weekCCount.toString());

                /**
                 * 均值
                 */
                double ave_doExerciseNum = weekUserCount == 0.00 ? 0.00 : weekQuesCount * 1.0 / weekUserCount;
                double ave_speed = weekQuesCount == 0.00 ? 0.00 : weekTimeSum * 1.0 / weekQuesCount;
                double ave_accuracy = weekQuesCount == 0.00 ? 0.00 : weekCorrectNum * 1.0 / weekQuesCount;
                weekReport.setAve_doExerciseNum(ave_doExerciseNum);
                weekReport.setAve_speed(ave_speed);
                weekReport.setAve_accuracy(ave_accuracy);
                /**
                 * 用户本周练习时长
                 */
                String exerciseTime = recentWeekInfo.get("exerciseTime").toString();
                weekReport.setDoExerciseTime(Long.parseLong(exerciseTime));

                /**
                 * 用户本周做题速度
                 */
                Integer recentWeekTime = 0;
                for (PointDetail pd : recentPointReport) {
                    if (pd.getPointKey() != 0) {

                        recentWeekTime += pd.getPointTime();
                    }
                }


                double weekUserSpeed = (recentWeekTime * 1.0) / recentWeekSum;
//            double weekUserSpeed = Double.parseDouble(recentWeekInfo.get("week_speek").toString());
//            weekReport.setSpeed(weekUserSpeed);
                weekReport.setSpeed(weekUserSpeed);
                /**
                 * 用户科目
                 */
                weekReport.setSubject(1);
                /**
                 * 用户本周排名
                 */
//                int rankByScore = Integer.parseInt(recentWeekInfo.get("rank5").toString());
//                weekReport.setRank(rankByScore);
                Object rankByPredictScore = recentWeekInfo.get("rank5");
                int rankByScore = weekUserCount;
                Object rankBySortScore = recentWeekInfo.get("rank");
                if (rankByPredictScore != null) {

                    rankByScore = Integer.parseInt(rankByPredictScore.toString());
                    weekReport.setRank(rankByScore);
                } else {
                    weekReport.setRank(Integer.parseInt(rankBySortScore.toString()));
                }

                /**
                 * 周报--预测分
                 */
                double sortScore = Double.parseDouble(recentWeekInfo.get("sortScore").toString());
                double predictScore = Double.parseDouble(recentWeekInfo.get("predict_score").toString());
//                if (sortScore == 0.00) {
//                    weekReport.setRank(0);
//                }
                weekReport.setPredictedScore(predictScore);
                /**
                 * 用户做题数量
                 */
//                long weekuesrDoNum = Long.parseLong(recentWeekSum.toString());
//                weekReport.setDoExerciseNum(Long.parseLong(recentWeekInfo.get("exerciseNum").toString()));
                weekReport.setDoExerciseNum(Long.parseLong(recentWeekSum.toString()));
                /**
                 * 用户本周上一次做题数量
                 */
                /**
                 * 统计图
                 */
                weekReport.setPointInfo(AbilityAssessmentWeekReport.point2List(recentPointReport, subject, spn, sp));
                weekReport.setPointIds(weekReport.getPointInfo().get(0));
                weekReport.setExerciseNum(weekReport.getPointInfo().get(1));
                weekReport.setAccuracies(weekReport.getPointInfo().get(2));
                weekReport.setSpeeds(weekReport.getPointInfo().get(3));
                weekReport.setPointName(pointname);
                weekReport.setPointInfo(null);

                createWeekTextArea(weekReport, weekTextArea, recentWeekInfo, weekUserCount, weekUserSpeed, rankByScore);

            } else {

                weekReport.setPointInfo(AbilityAssessmentWeekReport.point2List(null, subject, spn, sp));
                weekReport.setPointIds(weekReport.getPointInfo().get(0));
                weekReport.setExerciseNum(weekReport.getPointInfo().get(1));
                weekReport.setAccuracies(weekReport.getPointInfo().get(2));
                weekReport.setSpeeds(weekReport.getPointInfo().get(3));
                weekReport.setPointName(pointname);
                weekReport.setPointInfo(null);
            }

            Object lastWeekInfoObj = lastWeek.get(hbase_family);
            if (lastWeekInfoObj != null) {

                Map lastWeekInfo = (Map) lastWeekInfoObj;


                String[] weekGrade = lastWeekInfo.get("grade").toString().split("_");

                Long exerciseNum = weekReport.getDoExerciseNum();


                Integer doTime = 0;
                Integer doNum = 0;
                Integer coNum = 0;
                List<PointDetail> week_pds = new ArrayList<>();
                for (String s : weekGrade) {
                    PointDetail pd = new PointDetail();
                    String[] sr = s.split(":");
                    if (Integer.parseInt(sr[0]) != -1) {
                        doNum += Integer.parseInt(sr[2]);
                        doTime += Integer.parseInt(sr[3]);
                        coNum += Integer.parseInt(sr[1]);
                    }
                }

                if (exerciseNum != 0 && doNum != 0) {
                    double tmpSpeed = (doTime * 1.0) / doNum;
                    Double speed = weekReport.getSpeed();
                    weekReport.setDif_speed((tmpSpeed - speed) / tmpSpeed);

                    double predict_score = Double.parseDouble(lastWeekInfo.get("sortScore") == null ? "0.00" : lastWeekInfo.get("sortScore").toString());

//                    int lastWeekRankByScore = Integer.parseInt(lastWeekInfo.get("rank5").toString());
//                    weekReport.setLastWeekRank(lastWeekRankByScore);


                    Object rank5 = lastWeekInfo.get("rank5");
                    int lastWeekRankByScore = Integer.parseInt(rank5 == null ? "0" : rank5.toString());
                    weekReport.setLastWeekRank(lastWeekRankByScore);
//                    if (predict_score == 0.00) {
//                        weekReport.setLastWeekRank(0);
//
//                    }

                    Long lastWeekExerciseNum = Long.parseLong(doNum.toString());
                    weekReport.setDif_doExerciseNum(exerciseNum - lastWeekExerciseNum);

                    Double lastWeekAccuracy = coNum * 1.0 / doNum;
                    weekReport.setDif_accuracy((weekReport.getAccuracy() - lastWeekAccuracy) / lastWeekAccuracy);
                } else {

                    weekReport.setDif_speed(0.00);
                    weekReport.setDif_doExerciseNum(0L);
                    weekReport.setDif_accuracy(0.00);
                    weekReport.setLastWeekRank(0);
                }
            }


            String[] textArea = new String[]{
                    "", "", "", "", "", ""
            };
            if (nowUpdate != null) {

                createTextArea(subject, aa, nowUpdate, doExerciseNum, presentDay, lastDay, textArea);
            }

            aa.setTextArea(textArea);

            aa.setWeekTextArea(weekTextArea);

            StringBuilder svb = new StringBuilder();
            svb.append(userId).append(",");
            try {
                List<AATopUser> top10User = aa.getTop10User();
                for (int i = 1; i < 11; i++) {

                    AATopUser user = new AATopUser();

                    Map nowWeekTop10;
                    try {
                        nowWeekTop10 = HBaseUtil.get(hbase_week_top10_ability_assessment, i + "-" + subject + "-" + presentDay);
                    } catch (Exception e) {
                        Thread.sleep(3000);
                        nowWeekTop10 = HBaseUtil.get(hbase_week_top10_ability_assessment, i + "-" + subject + "-" + presentDay);
                    }


                    Map map = (Map) nowWeekTop10.get(hbase_family);
                    String userId1 = "";
                    if (map != null) {

                        userId1 = (map).get("userId").toString();

                        int r = Integer.parseInt((map).get("rank").toString());
                        int id = Integer.parseInt((map).get("userId").toString());
                        double d = Double.parseDouble((map).get("sortScore").toString());
                        if (d == 0.00) {
                            continue;
                        }
                        svb.append(id).append(",");
                        user.setUserId(id);
                        user.setNowWeekRank(r);
                        user.setPrediceScore(d);
                    }

                    Map topInfo;
                    try {
                        topInfo = HBaseUtil.get(hbase_week_ability_assessment, userId1 + "-" + subject + "-" + presentDay);
                    } catch (Exception e) {
                        Thread.sleep(3000);
                        topInfo = HBaseUtil.get(hbase_week_ability_assessment, userId1 + "-" + subject + "-" + presentDay);
                    }

                    Map map2 = (Map) topInfo.get(hbase_family);
                    if (map2 != null) {
                        /**
                         *周用户统计
                         */
                        Object weekUCount = (map2).get("userCount");
                        int weekUserCount = weekUCount == null ? 0 : Integer.parseInt(weekUCount.toString());
                        /**
                         *周做题数量统计
                         */
                        //
                        Object weekQCount = (map2).get("quesCount");
                        int weekQuesCount = weekQCount == null ? 0 : Integer.parseInt(weekQCount.toString());
                        /**
                         *周做题时间统计
                         */
                        Object weekTime = (map2).get("timeTotal");
                        long weekTimeSum = weekTime == null ? 0 : Long.parseLong(weekTime.toString());
                        /**
                         *周正确数量统计
                         */
                        Object weekCCount = (map2).get("correctNum");
                        int weekCorrectNum = weekCCount == null ? 0 : Integer.parseInt(weekCCount.toString());

                        /**
                         *均值
                         */
                        double ave_doExerciseNum = weekUserCount == 0.00 ? 0.00 : weekQuesCount * 1.0 / weekUserCount;
                        double ave_speed = weekQuesCount == 0.00 ? 0.00 : weekTimeSum * 1.0 / weekQuesCount;
                        double ave_accuracy = weekQuesCount == 0.00 ? 0.00 : weekCorrectNum * 1.0 / weekQuesCount;
                        weekReport.setAve_doExerciseNum(ave_doExerciseNum);
                        weekReport.setAve_speed(ave_speed);
                        weekReport.setAve_accuracy(ave_accuracy);

                    }

                    Map lastWeekTop10;
                    try {
                        lastWeekTop10 = HBaseUtil.get(hbase_week_ability_assessment, userId1 + "-" + subject + "-" + lastDay);
                    } catch (Exception e) {
                        Thread.sleep(3000);
                        lastWeekTop10 = HBaseUtil.get(hbase_week_ability_assessment, userId1 + "-" + subject + "-" + lastDay);
                    }
                    Map map1 = (Map) lastWeekTop10.get(hbase_family);
                    if (map1 != null) {

                        int r2 = Integer.parseInt((map1).get("rank").toString());
                        user.setLastWeekRank(r2);
                    }

                    if (map != null) {
                        top10User.add(user);
                    }
                }
                String ids = svb.substring(0, svb.length() - 1);


                aa.setUserInfo(mysqlService.getUserInfoById(ids));
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
            aa.setWeekReport(new AbilityAssessmentWeekReport());
            System.out.println(e);
//            return reAa;
        }
        return aa;
    }


    private static void createTextArea(int subject, AbilityAssessment aa, Map nowUpdate, Long doExerciseNum, String presentDay, String lastDay, String[] textArea) throws Exception {

        if (doExerciseNum > 9000) {
            textArea[0] = "优秀";
        } else if (doExerciseNum <= 9000 && doExerciseNum > 5000) {
            textArea[0] = "良好";
        } else if (doExerciseNum <= 5000 && doExerciseNum > 800) {
            textArea[0] = "偏低";
        } else {
            textArea[0] = "裸考战将";
        }

        Double accuracy = aa.getAccuracy();
        if (accuracy > 0.75) {
            textArea[1] = "优秀";
        } else if (accuracy <= 0.75 && accuracy > 0.60) {
            textArea[1] = "良好";
        } else if (accuracy <= 0.60 && accuracy > 0.40) {
            textArea[1] = "偏低";
        } else {
            textArea[1] = "认真答题哦";
        }

        Double doExerciseSpead = aa.getDoExerciseSpead();
        if (doExerciseSpead > 90.0) {
            textArea[2] = "蜗牛的速度";
        } else if (doExerciseSpead <= 90 && doExerciseSpead > 53) {
            textArea[2] = "偏低";
        } else if (doExerciseSpead <= 53 && doExerciseSpead > 20) {
            textArea[2] = "良好";
        } else if (doExerciseSpead <= 20 && accuracy < 0.50) {
            textArea[2] = "认真答题哦";
        } else {
            textArea[2] = "秒杀王子";
        }

        Integer rank = aa.getRank();
        int userCount = Integer.parseInt(nowUpdate.get("qualifiedUserCount").toString());

        double rankPercentage = (1.0 - (rank * 1.0 / userCount));
        if (rankPercentage > 0.8 && rank > 0) {
            textArea[3] = "引领风骚";
        } else if (rankPercentage <= 0.8 && rankPercentage > 0.60 && rank > 0) {
            textArea[3] = "中等偏上";
        } else if (rankPercentage <= 0.60 && rankPercentage > 0.40 && rank > 0) {
            textArea[3] = "中等偏下";
        } else {
            textArea[3] = "垫底小霸王";
        }

        Integer doExerciseTimeRank = Integer.parseInt(nowUpdate.get("rank3").toString());
        double doExerciseTimeRankPercentage = (1.0 - (doExerciseTimeRank * 1.0 / userCount));
        if (doExerciseTimeRankPercentage > 0.8) {
            textArea[4] = "刷题王者";
        } else if (doExerciseTimeRankPercentage <= 0.8 && doExerciseTimeRankPercentage > 0.60) {
            textArea[4] = "良好";
        } else if (doExerciseTimeRankPercentage <= 0.60 && doExerciseTimeRankPercentage > 0.40) {
            textArea[4] = "偏低";
        } else {
            textArea[4] = "刷题太少了";
        }

        Integer doExerciseDayRank = Integer.parseInt(nowUpdate.get("exeDayPassMan").toString());
        double doExerciseDayRankPercentage = doExerciseDayRank * 1.0 / userCount;

        Long doExerciseDay = aa.getDoExerciseDay();
        if (doExerciseDay > 120) {
            textArea[5] = "就是这么优秀";
        } else if (doExerciseDay <= 120 && doExerciseDay > 80) {
            textArea[5] = "良好";
        } else if (doExerciseDay <= 80 && doExerciseDay > 40) {
            textArea[5] = "偏低";
        } else {
            textArea[5] = "需要加油哦";
        }
    }

    private static void createWeekTextArea(AbilityAssessmentWeekReport weekReport, String[] weekTextArea, Map recentWeekInfo, int weekUserCount, Double weekUserSpeed, int rankByScore) {

//        int rankByDoNum = Integer.parseInt(recentWeekInfo.get("rank2").toString());
//        double doNumPercentage = (weekUserCount - rankByDoNum) * 1.0 / weekUserCount;
        int rankByDoNum = Integer.parseInt(recentWeekInfo.get("exerciseNum").toString());
        if (rankByDoNum > 1100) {
            weekTextArea[0] = "明天上岸的你一定感谢今天努力的你，成功在向你招手";
        } else if (rankByDoNum <= 1100 && rankByDoNum > 600) {
            weekTextArea[0] = "距离成功一步之遥，不抛弃不放弃，从来都是强者的座右铭";
        } else if (rankByDoNum <= 600 && rankByDoNum > 200) {
            weekTextArea[0] = "贵在坚持，继续努力吧，你需要的是小图的支持和鼓励，加油！";
        } else {
            weekTextArea[0] = "本周题目做的太少了，每天保持大量刷题才能顺利上岸";
        }

//        int rankByAccuracy = Integer.parseInt(recentWeekInfo.get("rank4").toString());
//        double accPercentage = (weekUserCount - rankByAccuracy) * 1.0 / weekUserCount;
        double accPercentage = weekReport.getAccuracy();
        if (accPercentage > 0.75) {
            weekTextArea[1] = " 像你一样优秀的考生不多了，继续保持以碾压之势笑傲江湖";
        } else if (accPercentage <= 0.75 && accPercentage > 0.60) {
            weekTextArea[1] = "上帝从来不会漠视你的努力，继续保持，成功上岸不是梦";
        } else if (accPercentage <= 0.60 && accPercentage > 0.40) {
            weekTextArea[1] = "确认过眼神，你还是需要提高准确率的人";
        } else {
            weekTextArea[1] = "不忍直视的正确率，你是在闹着玩儿么？";
        }

        if (weekUserSpeed > 90.0) {
            weekTextArea[2] = " 蜗牛的速度，谁也拯救不了你～公考战场讲究速战速决";
        } else if (weekUserSpeed <= 90.0 && weekUserSpeed > 53.0) {
            weekTextArea[2] = "虽然做题速度慢，但是我的准确率高。也只能这么安慰你了！";
        } else if (weekUserSpeed <= 53.0 && weekUserSpeed > 20.0) {
            weekTextArea[2] = "这样的速度正是上岸的速度，继续保持。";
        } else if (weekUserSpeed <= 20.0 && weekReport.getAccuracy() < 0.50) {
            weekTextArea[2] = " 蒙的全都对，这只是一个美好的愿望，靠实力才是你的上岸之路";
        } else if (weekUserSpeed <= 20.0 || weekUserSpeed.isInfinite() || weekUserSpeed.isNaN()) {
            weekTextArea[2] = "论速度你就是王者，绝对的秒杀小王子";
        } else {
            weekTextArea[2] = "";
        }

        double scorePercentage = (weekUserCount - rankByScore) * 1.0 / weekUserCount;
        if (scorePercentage > 0.80) {
            weekTextArea[3] = " 此时此刻，唯有一览众山小才能表达你的豪迈情感";
        } else if (scorePercentage <= 0.80 && scorePercentage > 0.60) {
            weekTextArea[3] = " 加油，再苦再累不要放弃，再坚持一下就可以登顶";
        } else if (scorePercentage <= 0.60 && scorePercentage > 0.40) {
            weekTextArea[3] = " 攀登从来都是不容易的，但是我看到了半山腰倔强的你";
        } else {
            weekTextArea[3] = "排名决定高度，永远在金字塔底端，何时才能问鼎顶峰";
        }
    }

    public static List<PointDetail> gradeAnalyze(String[] weekGrade, Integer weekCorrectSum, Integer weekSum) {

        List<PointDetail> week_pds = new ArrayList<>();
        for (String s : weekGrade) {
            PointDetail pd = new PointDetail();
            String[] sr = s.split(":");
            if (Integer.parseInt(sr[0]) != -1) {
                pd.setPointKey(Integer.parseInt(sr[0]));
                pd.setPointCorrect(Integer.parseInt(sr[1]));
                pd.setPointNum(Integer.parseInt(sr[2]));
                pd.setPointTime(Integer.parseInt(sr[3]));
                pd.setAccuracy(pd.getPointNum() == 0 ? 0.0 : pd.getPointCorrect() * 1.0 / pd.getPointNum());
                pd.setSpeed(pd.getPointNum() == 0 ? 0.0 : pd.getPointTime() * 1.0 / pd.getPointNum());

                weekCorrectSum += Integer.parseInt(sr[1]);
                weekSum += Integer.parseInt(sr[2]);
                week_pds.add(pd);
            }
        }
        return week_pds;
    }
}
