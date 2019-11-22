package com.huatu.splider.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author hanchao
 * @date 2018/2/23 14:28
 */
public class FBQuestions {
    public static OkHttpClient client = new OkHttpClient.Builder().connectTimeout(2000, TimeUnit.MILLISECONDS)
            .writeTimeout(2000, TimeUnit.MILLISECONDS)
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .connectionPool(new ConnectionPool(100, 600000, TimeUnit.MILLISECONDS))
            .followRedirects(true)//不跟踪重定向
            .retryOnConnectionFailure(true)
            .build();

    public static final int SIZE = 500;//一次抓取数量
    private static final String FILE_PREFIX = "D:/fb/questions/";

    public static LinkedHashMap<String,String> subjects = new LinkedHashMap<>();

    static {
//        try {
//            FileUtils.write(new File(FILE_SAVE_NORMAL), "\r\n", Charset.forName("UTF-8"),false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        subjects.put("xingce","公务员行测");
//        subjects.put("nxs","农信社");
//        subjects.put("sk1","司考-卷一");
//        subjects.put("sk2","司考-卷二");
//        subjects.put("sk3","司考-卷三");
//        subjects.put("kjcjsw","初级会计（2018）-初级会计实务");
//        subjects.put("kjcjjj","初级会计（2018）-经济法基础");
//        subjects.put("kjzjsw","中级会计职称（2018)-中级会计实务");
//        subjects.put("kjzjjjf","中级会计职称（2018)-中级经济法");
//        subjects.put("kjzjcwgl","中级会计职称（2018)-财务管理");
//        subjects.put("kjzkkj","注会（2018)-会计");
//        subjects.put("kjzksj","注会（2018)-审计");
//        subjects.put("kjzkcwcb","注会（2018)-财务成本管理");
//        subjects.put("kjzkjjf","注会（2018)-经济法");
//        subjects.put("kjzksf","注会（2018)-税法");
//        subjects.put("kjzkzlfx","注会（2018)-战略与风险管理");
//        subjects.put("kyyy1","考研-英语一");
//        subjects.put("kyyy2","考研-英语二");
//        subjects.put("kyzz","考研-政治");
//        subjects.put("kysx1","考研-数学一");
//        subjects.put("kysx2","考研-数学二");
//        subjects.put("kysx3","考研-数学三");
//        subjects.put("kyfs","考研-法律硕士");
//        subjects.put("ejjzs0","建造师-建设工程法规及相关知识");
//        subjects.put("ejjzs1","建造师-建设工程施工管理");
//        subjects.put("ejjzs2","建造师-建筑工程管理与实务");
//        subjects.put("ejjzs3","建造师-机电工程管理与实务");
//        subjects.put("ylzp","医疗招聘");
//        subjects.put("sydw","事业单位-公基");
//        subjects.put("syzc","事业单-职测");
//        subjects.put("jszgzhy","教师资格证-幼儿园-综合素质-幼儿园");
//        subjects.put("jszgbj","教师资格证-幼儿园-保教知识与能力");
//        subjects.put("jszgzhx","教师资格证-小学-综合素质-小学");
//        subjects.put("jszgjx","教师资格证-小学-教育教学知识与能力");
//        subjects.put("jszgzhz","教师资格证-中学-综合素质-中学");
//        subjects.put("jszgjy","教师资格证-中学-教育知识与能力");
//        subjects.put("jszgywz","教师资格证-中学-语文");
//        subjects.put("jszgsxz","教师资格证-中学-数学");
//        subjects.put("jszgyinyuz","教师资格证-中学-英语");
//        subjects.put("jszglsz","教师资格证-中学-历史");
//        subjects.put("jszgdlz","教师资格证-中学-地理");//2086502
//        subjects.put("jszgzzz","教师资格证-中学-政治");//2085344
//        subjects.put("jszgmsz","教师资格证-中学-美术");//2087648
        subjects.put("jsjyzhzz","教师招聘-教育综合知识");//1774490,2207646
        subjects.put("jszpgj","教师招聘-公共基础知识");//1690776,1771616
        subjects.put("jsyy","教师招聘-语文");//1801500开始有数据，2002000
        subjects.put("jssx","教师招聘-数学");
        subjects.put("jswy","教师招聘-英语");

    }

    private static ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(10,100,5, TimeUnit.MINUTES,new LinkedBlockingQueue(100000), Executors.defaultThreadFactory(),new BlockingPolicy());
    public static void main(String[] args) throws IOException {
        for ( int i = 0; i < 10000; i++) {
//            taskExecutor.submit(()->{
                try {
                    final int offset = 0;
//                    Set<String> keys = subjects.keySet();
//                    for(String key:keys){
                        QuestionTask xingce = new QuestionTask(offset+500*i, "jswy");
                        xingce.run();
//                    }
                    System.out.print(i+":");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//            });
        }
        System.out.println("123123");

    }

    /**
     * 拒绝策略，保证阻塞顺序放入,而不是直接丢掉任务或者异常
     */
    public static class BlockingPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected synchronized static void save(String subject,int offset, Collection<Question> questions){
        String subjectName = subjects.get(subject);
        System.out.println("write data,subject: "+subjectName+",from "+offset+",size "+SIZE+",length "+questions.size());
        for (Question question : questions) {
            try {
                FileUtils.write(new File(FILE_PREFIX+"/"+subjectName), JSON.toJSONString(question)+"\r\n\r\n", Charset.forName("UTF-8"),true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected static class QuestionTask implements Runnable {
        private int offset;

        private String subject;

        public QuestionTask(int offset,String subject){
            this.offset = offset;
            this.subject = subject;
        }

        @Override
        public void run() {
            int [] ids = new int[SIZE];
            for(int i = offset;i<offset+SIZE;i++){
                ids[i-offset] = i;
            }

            try {
                Request request = new Request.Builder()
                        .url("http://fenbi.com/android/"+subject+"/questions?ids="+ URLEncoder.encode(StringUtils.join(ids,','))+"&platform=android24&version=6.4.1.6&vendor=fenbi&app=gwy&av=7&kav=3")
                        .addHeader("Cookie","sess=cVPTGwAu+53ROPLouJuBs3fcI+xLpCYACc3ZKMxNpC9F6yO43X+TBHF5yxKWyp2non9jw32kriNNzHNKeGYU7We3oXL0zS7YWGcOzz3pFT0=; persistent=94rBZkR36CV9cTDbqTngfYNi/L+1AQSe9DwlDVDkTOPa6qOw4j8rNSJB3ZXm5FlGUaUBQSLmJrE4fz9mRAOlLA==; userid=52907612; sid=1988460873662549596")
//                      .addHeader("Cookie","sess=WKbYsqCqoGiJNlz2rp6AyD5S1wimKIHN06uG4/CKVF936Sgg/q7vFPYUNi70pGKv0N+JSjCA+S5G1OyM11/dSQ==; persistent=WKbYsqCqoGiJNlz2rp6AyD5S1wimKIHN06uG4/CKVF936Sgg/q7vFPYUNi70pGKv0N+JSjCA+S5G1OyM11/dSQ==; userid=54004536; sid=6459198498243137753")
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String body = response.body().string();
                List<Question> data = JSON.parseObject(body, new TypeReference<List<Question>>(){});
                Map<Integer, Question> questions = data.stream().filter(Objects::nonNull).collect(Collectors.toMap(Question::getId, x->{
                    String content = x.getContent();
                    if(content.contains("img")){
                        StringBuilder builder = new StringBuilder(content);
                        Pattern pattern = Pattern.compile("\\[img[^\\]]*\\]([^\\[]+)\\[/img\\]");
                        Matcher matcher = pattern.matcher(builder);
                        int i = 0;
                        while (matcher.find(i)) {
                            String image = matcher.group(1);
                            builder.replace(matcher.start(),matcher.end(),"http://fb.fbstatic.cn/api/"+subject+"/images/"+image);
                            i = matcher.start();
                        }
                        x.setContent(builder.toString());
                    }
                    return x;
                }));


                List<Integer> questionIds = Lists.newArrayList(questions.keySet());
                if(questionIds.isEmpty()){
                    System.out.println("from "+offset+" is empty...");
                    return;
                }
                String idsParam = StringUtils.join(questionIds,",");

                request = new Request.Builder()
                        .url("http://fenbi.com/android/"+subject+"/solution/keypoints?ids="+ idsParam+"&platform=android24&version=6.4.1.6&vendor=fenbi&app=gwy&av=7&kav=3")
                        .addHeader("Cookie","sess=cVPTGwAu+53ROPLouJuBs3fcI+xLpCYACc3ZKMxNpC9F6yO43X+TBHF5yxKWyp2non9jw32kriNNzHNKeGYU7We3oXL0zS7YWGcOzz3pFT0=; persistent=94rBZkR36CV9cTDbqTngfYNi/L+1AQSe9DwlDVDkTOPa6qOw4j8rNSJB3ZXm5FlGUaUBQSLmJrE4fz9mRAOlLA==; userid=52907612; sid=1988460873662549596")
//                        .addHeader("Cookie","sess=WKbYsqCqoGiJNlz2rp6AyD5S1wimKIHN06uG4/CKVF936Sgg/q7vFPYUNi70pGKv0N+JSjCA+S5G1OyM11/dSQ==; persistent=WKbYsqCqoGiJNlz2rp6AyD5S1wimKIHN06uG4/CKVF936Sgg/q7vFPYUNi70pGKv0N+JSjCA+S5G1OyM11/dSQ==; userid=54004536; sid=6459198498243137753")
                        .get()
                        .build();
                response = client.newCall(request).execute();
                body = response.body().string();
                List points = JSON.parseObject(body,List.class);
                for (int i = 0; i < questionIds.size(); i++) {
                    Integer id = questionIds.get(i);
                    Question question = questions.get(id);
                    question.setPoint(points.get(i));
                }

                request = new Request.Builder()
                        .url("http://fenbi.com/android/"+subject+"/pure/solutions?ids="+ idsParam+"&platform=android24&version=6.4.1.6&vendor=fenbi&app=gwy&av=7&kav=3")
                        .addHeader("Cookie","sess=cVPTGwAu+53ROPLouJuBs3fcI+xLpCYACc3ZKMxNpC9F6yO43X+TBHF5yxKWyp2non9jw32kriNNzHNKeGYU7We3oXL0zS7YWGcOzz3pFT0=; persistent=94rBZkR36CV9cTDbqTngfYNi/L+1AQSe9DwlDVDkTOPa6qOw4j8rNSJB3ZXm5FlGUaUBQSLmJrE4fz9mRAOlLA==; userid=52907612; sid=1988460873662549596")
//                        .addHeader("Cookie","sess=WKbYsqCqoGiJNlz2rp6AyD5S1wimKIHN06uG4/CKVF936Sgg/q7vFPYUNi70pGKv0N+JSjCA+S5G1OyM11/dSQ==; persistent=WKbYsqCqoGiJNlz2rp6AyD5S1wimKIHN06uG4/CKVF936Sgg/q7vFPYUNi70pGKv0N+JSjCA+S5G1OyM11/dSQ==; userid=54004536; sid=6459198498243137753")
                        .get()
                        .build();
                response = client.newCall(request).execute();
                body = response.body().string();
                JSONArray solutions = JSON.parseArray(body);
                for (int i = 0; i < solutions.size(); i++) {
                    JSONObject solution = solutions.getJSONObject(i);
                    Integer id = solution.getInteger("id");
                    Question question = questions.get(id);
                    question.setFlags(solution.get("flags"));
                    question.setTags(solution.get("tags"));
                    question.setSolutionAccessories(solution.get("solutionAccessories"));
                    question.setSolution(solution.getString("solution"));
                    question.setSource(solution.getString("source"));
                }

                request = new Request.Builder()
                        .url("http://fenbi.com/android/"+subject+"/question/meta?ids="+ idsParam+"&platform=android24&version=6.4.1.6&vendor=fenbi&app=gwy&av=7&kav=3")
                        .addHeader("Cookie","sess=cVPTGwAu+53ROPLouJuBs3fcI+xLpCYACc3ZKMxNpC9F6yO43X+TBHF5yxKWyp2non9jw32kriNNzHNKeGYU7We3oXL0zS7YWGcOzz3pFT0=; persistent=94rBZkR36CV9cTDbqTngfYNi/L+1AQSe9DwlDVDkTOPa6qOw4j8rNSJB3ZXm5FlGUaUBQSLmJrE4fz9mRAOlLA==; userid=52907612; sid=1988460873662549596")
//                        .addHeader("Cookie","sess=WKbYsqCqoGiJNlz2rp6AyD5S1wimKIHN06uG4/CKVF936Sgg/q7vFPYUNi70pGKv0N+JSjCA+S5G1OyM11/dSQ==; persistent=WKbYsqCqoGiJNlz2rp6AyD5S1wimKIHN06uG4/CKVF936Sgg/q7vFPYUNi70pGKv0N+JSjCA+S5G1OyM11/dSQ==; userid=54004536; sid=6459198498243137753")
                        .get()
                        .build();
                response = client.newCall(request).execute();
                body = response.body().string();
                JSONArray metas = JSON.parseArray(body);
                for (int i = 0; i < metas.size(); i++) {
                    JSONObject meta = metas.getJSONObject(i);
                    Integer id = meta.getInteger("id");
                    Question question = questions.get(id);
                    question.setAnswerCount(meta.getInteger("answerCount"));
                    question.setWrongCount(meta.getInteger("wrongCount"));
                    question.setTotalCount(meta.getInteger("totalCount"));
                    question.setMostWrongAnswer(meta.get("mostWrongAnswer"));
                    question.setCorrectRatio(meta.getString("correctRatio"));
                }


                save(subject,offset,questions.values());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
