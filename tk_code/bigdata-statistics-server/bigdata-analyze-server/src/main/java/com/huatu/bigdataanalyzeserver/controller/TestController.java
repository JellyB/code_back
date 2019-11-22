package com.huatu.bigdataanalyzeserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.huatu.bigdataanalyzecommon.bean.CourseWareDTO;
import com.huatu.bigdataanalyzecommon.bean.TopicRecordEntity;
import com.huatu.bigdataanalyzeserver.service.DataStatisticsSystemService;
import com.huatu.bigdataanalyzeserver.util.RabbitMQUtils;
import com.rabbitmq.client.ConnectionFactory;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.*;

@RestController
public class TestController {

    public final static String QUEUE_NAME = "rabbitMQ.test";
    /**
     * kafka
     */
    @Autowired
    private Producer<String, String> producer;


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DataStatisticsSystemService dataService;

    /**
     * rabbitmq
     */
    @Autowired
    private ConnectionFactory rabbitmqConnection;
    /**
     * 阻塞队列，大小100
     */
    private final BlockingQueue queue = new ArrayBlockingQueue(100);
    //ExecutorService executorService = Executors.newFixedThreadPool(3);
    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();
    //Common Thread Pool
    ThreadPoolExecutor pool = new ThreadPoolExecutor(5,
            200,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024),
            namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    /**
     * 用户点击功能点数据统计
     */
    @PostConstruct
    public void init() {

        pool.execute(() -> {
            while (true) {

                Object obj = null;
                try {
                    obj = queue.take();
                    if (!obj.equals("1")) {

                        producer.send(new KeyedMessage<>("wordcount",
                                obj.toString()));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @GetMapping("/v1/function/analysis")
    public Object userFunAnalysis(@RequestParam(value = "user_id", required = false) Long user_id
            , @RequestParam(value = "fun_id", required = false) String fun_id) {
        System.out.println(user_id + "" + fun_id);
        return 1;
    }

    @PostMapping("/v1/topoc/record/coureWare/correctAnalyze")
    public Object userCorrectAnalyeze(@RequestParam(value = "userId", required = false) Long userId,
                                      @RequestBody List<CourseWareDTO> list,
                                      HttpServletRequest request) throws Exception {

        return dataService.queryAccuracy(userId, list);
    }

    /**
     * 课绑题做题记录接口
     */
    @GetMapping("/v1/topic/record")
    public Object topicRecord(@RequestParam(value = "question_id", required = false) Long questionId,                   //试题Id
                              @RequestParam(value = "user_id", required = false) Long userId,                           //用户id
                              @RequestParam(value = "time", required = false) Long time,                                //做题时长
                              @RequestParam(value = "correct", required = false) Integer correct,                       //是否正确
                              @RequestParam(value = "question_source", required = false) Integer questionSource,        //视频来源（课中题，课后题）
                              @RequestParam(value = "courseWare_id", required = false) Long courseWareId,               //课件id
                              @RequestParam(value = "submit_time", required = false) Long submitTime,                   //提交时间
                              @RequestParam(value = "courseWare_type", required = false) Integer courseWareType,        //提交时间
                              @RequestParam(value = "knowledgePoint", required = false) String knowledgePoint,         //1,2,3 一级知识点，二级知识点，三机知识点
                              //@RequestParam(value = "knowledge_level", required = false) Integer knowledge_level,       //知识点等级
                              @RequestParam(value = "subject_id", required = false) Long subject_id,                 //科目
                              @RequestParam(value = "step", required = false) Long step                              //阶段
    ) {

        try {

            String message = "userId=" + userId +
                    "|questionId=" + questionId +
                    "|correct=" + correct +
                    "|time=" + time +
                    "|knowledgePoint=" + knowledgePoint +
                    "|questionSource=" + questionSource +
                    "|courseWareId=" + courseWareId +
                    "|submitTime=" + submitTime +
                    "|courseWareType=" + courseWareType +
                    "|subject_id=" + subject_id +
                    "|step=" + step;

//            JSONObject.parseObject(message);

            /*pool.execute(() -> {
                try {
                    queue.put(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });*/
            List<TopicRecordEntity> trs = new ArrayList<>();
            TopicRecordEntity tr = new TopicRecordEntity();
            tr.setUserId(userId);
            tr.setCorrect(correct);
            tr.setTime(time);

            tr.setQuestionId(questionId);
            tr.setQuestionSource(questionSource);

            tr.setCourseWareId(courseWareId);
            tr.setCourseWareType(courseWareType);

            tr.setStep(step);
            tr.setSubjectId(subject_id);
            tr.setKnowledgePoint(knowledgePoint);

            tr.setSubmitTime(submitTime);
            tr.setListened(1);

            trs.add(tr);


            pool.execute(() -> {
                try {
                    RabbitMQUtils.sendMessageToRmq(rabbitmqConnection, JSONObject.toJSONString(trs));
                    Thread.sleep((long) (Math.random() * 5000 + 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

//            RabbitMQUtils.sendMessageToRmq(rabbitmqConnection, message);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    @GetMapping("/v1/topic/record/test")
    public Object topicRecord() {

        try {

            //question_id=2&user_id=3&time=21312213213&correct=1&knowledge_point=1&question_source=2&course_ware_id=90&submit_time=128232131238
            Calendar calendar = Calendar.getInstance();

            calendar.set(2018, 7, 26);
            long now = calendar.getTime().getTime();

            for (int i = 1; i <= 10000; i++) {

//                submit_time = yes;
//                long submit_time = random.nextInt(Integer.MAX_VALUE);
                int finalI = i;
//                pool.execute(new Runnable() {
//                    @Override
//                    public void run() {
                long submit_time = 0L;

//                long user_id = 1L;
//                        long user_id = (int) (Math.random() * 100 + 1);
                long user_id = (int) (Math.random() * 1000 + 1);

                long question_id = (int) (Math.random() * 100 + 1);

                StringBuilder knowledge_point = new StringBuilder();
                for (int j = 0; j < 3; j++) {

                    knowledge_point.append(Integer.toString((int) (Math.random() * 100 + 1))).append(",");
                }
                String kp = knowledge_point.toString().substring(0, knowledge_point.toString().length() - 1);

                long time = (int) (Math.random() * 1);
                long course_ware_id = (int) (Math.random() * 23 + 1);
                long subject_id = (int) (Math.random() * 13 + 1);
                long step = (int) (Math.random() * 5 + 1);
//                        long subject_id = (int) (Math.random() * 100 + 1);
//                        long step = (int) (Math.random() * 5 + 1);

                int correct = (int) (Math.random() * 3);
                int courseWareType = (int) (Math.random() * 2 + 1);
                int question_source = (int) (Math.random() * 2 + 1);

                submit_time = now;

                restTemplate.getForObject("http://localhost:11148/analyze/v1/topic/record?" +
                        "question_id=" + question_id + "" +
                        "&user_id=" + user_id + "" +
                        "&time=" + time + "" +
                        "&correct=" + correct + "" +
                        "&knowledgePoint=" + kp + "" +
                        "&question_source=" + question_source + "" +
                        "&courseWare_id=" + course_ware_id + "" +
                        "&submit_time=" + submit_time + "" +
                        "&courseWare_type=" + courseWareType +
                        "&subject_id=" + subject_id +
                        "&step=" + step +
                        "&listened=1", Object.class);
//                    }
//                });
            }


        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    @GetMapping("/v1/testhigh")
    public Object testhigh() {

        try {


            restTemplate.getForObject("http://localhost:11148/analyze/v1/topic/record?" + "question_id=" + 30721 + "" + "&user_id=" + 233 + "" +
                    "&time=" + 10 + "" + "&correct=" + 0 + "" + "&knowledgePoint=" + "435,436,431" + "" + "&question_source=" + 1 + "" +
                    "&courseWare_id=" + 1 + "" + "&submit_time=" + 1531451812458L + "" + "&courseWare_type=" + 1 + "&subject_id=" + 1 + "&step=" + -1 + "&listened=1" +
                    "", Object.class);
            restTemplate.getForObject("http://localhost:11148/analyze/v1/topic/record?" + "question_id=" + 30722 + "" + "&user_id=" + 233 + "" +
                    "&time=" + 10 + "" + "&correct=" + 2 + "" + "&knowledgePoint=" + "435,436,442" + "" + "&question_source=" + 1 + "" +
                    "&courseWare_id=" + 1 + "" + "&submit_time=" + 1531451812458L + "" + "&courseWare_type=" + 1 + "&subject_id=" + 1 + "&step=" + -1 + "&listened=1" +
                    "", Object.class);
            restTemplate.getForObject("http://localhost:11148/analyze/v1/topic/record?" + "question_id=" + 30723 + "" + "&user_id=" + 233 + "" +
                    "&time=" + 10 + "" + "&correct=" + 3 + "" + "&knowledgePoint=" + "435,436,433" + "" + "&question_source=" + 1 + "" +
                    "&courseWare_id=" + 1 + "" + "&submit_time=" + 1531451813084L + "" + "&courseWare_type=" + 1 + "&subject_id=" + 1 + "&step=" + -1 + "&listened=1" +
                    "", Object.class);
            restTemplate.getForObject("http://localhost:11148/analyze/v1/topic/record?" + "question_id=" + 30724 + "" + "&user_id=" + 233 + "" +
                    "&time=" + 10 + "" + "&correct=" + 4 + "" + "&knowledgePoint=" + "435,436,444" + "" + "&question_source=" + 1 + "" +
                    "&courseWare_id=" + 1 + "" + "&submit_time=" + 1531451813084L + "" + "&courseWare_type=" + 1 + "&subject_id=" + 1 + "&step=" + -1 + "&listened=1" +
                    "", Object.class);
            /*restTemplate.getForObject("http://localhost:11148/analyze/v1/topic/record?" + "question_id=" + 30727 + "" + "&user_id=" + 2988 + "" +
                    "&time=" + 10 + "" + "&correct=" + 2 + "" + "&knowledgePoint=" + "435,436,437" + "" + "&question_source=" + 1 + "" +
                    "&courseWare_id=" + 1 + "" + "&submit_time=" + 1531451813814L + "" + "&courseWare_type=" + 1 + "&subject_id=" + 1 + "&step=" + -1 + "&listened=1" +
                    "", Object.class);
            restTemplate.getForObject("http://localhost:11148/analyze/v1/topic/record?" + "question_id=" + 30729 + "" + "&user_id=" + 2988 + "" +
                    "&time=" + 10 + "" + "&correct=" + 1 + "" + "&knowledgePoint=" + "435,436,441" + "" + "&question_source=" + 1 + "" +
                    "&courseWare_id=" + 1 + "" + "&submit_time=" + 1531451813814L + "" + "&courseWare_type=" + 1 + "&subject_id=" + 1 + "&step=" + -1 + "&listened=1" +
                    "", Object.class);
            restTemplate.getForObject("http://localhost:11148/analyze/v1/topic/record?" + "question_id=" + 30727 + "" + "&user_id=" + 2988 + "" +
                    "&time=" + 10 + "" + "&correct=" + 0 + "" + "&knowledgePoint=" + "435,436,437" + "" + "&question_source=" + 1 + "" +
                    "&courseWare_id=" + 1 + "" + "&submit_time=" + 1531451814460L + "" + "&courseWare_type=" + 1 + "&subject_id=" + 1 + "&step=" + -1 + "&listened=1" +
                    "", Object.class);
            restTemplate.getForObject("http://localhost:11148/analyze/v1/topic/record?" + "question_id=" + 30729 + "" + "&user_id=" + 2988 + "" +
                    "&time=" + 10 + "" + "&correct=" + 0 + "" + "&knowledgePoint=" + "435,436,441" + "" + "&question_source=" + 1 + "" +
                    "&courseWare_id=" + 1 + "" + "&submit_time=" + 1531451814460L + "" + "&courseWare_type=" + 1 + "&subject_id=" + 1 + "&step=" + -1 + "&listened=1" +
                    "", Object.class);
            restTemplate.getForObject("http://localhost:11148/analyze/v1/topic/record?" + "question_id=" + 30727 + "" + "&user_id=" + 2988 + "" +
                    "&time=" + 10 + "" + "&correct=" + 1 + "" + "&knowledgePoint=" + "435,436,437" + "" + "&question_source=" + 1 + "" +
                    "&courseWare_id=" + 1 + "" + "&submit_time=" + 1531451815111L + "" + "&courseWare_type=" + 1 + "&subject_id=" + 1 + "&step=" + -1 + "&listened=1" +
                    "", Object.class);
            restTemplate.getForObject("http://localhost:11148/analyze/v1/topic/record?" + "question_id=" + 30729 + "" + "&user_id=" + 2988 + "" +
                    "&time=" + 10 + "" + "&correct=" + 0 + "" + "&knowledgePoint=" + "435,436,441" + "" + "&question_source=" + 1 + "" +
                    "&courseWare_id=" + 1 + "" + "&submit_time=" + 1531451815111L + "" + "&courseWare_type=" + 1 + "&subject_id=" + 3 + "&step=" + -1 + "&listened=1" +
                    "", Object.class);
*/
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }


    @GetMapping("/v1/test")
    public Object testRecord() {

        try {

            String url = "http://testapi.huatu.com/lumenapi/v4/common/user/study_record";

            HttpHeaders headers = new HttpHeaders();
            //  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            //  封装参数，千万不要替换为Map与HashMap，否则参数无法传递
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
            //  也支持中文
            params.add("completeTime", "123456");
            params.add("syllabusId", "123456");
            params.add("userName", "huatu");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(params, headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Object.class);

            //  输出结果
            System.out.println(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    public static void main(String[] args) {

//        String sam = "2$$1$$1$$233981863$$app_ztk1781586666$$rr$$1528885515444$$6.2.0$$2$$0\n";
//        String[] split = sam.split("\\$\\$");
//        for (String s : split) {
//            System.out.println(s);
//        }
//
//        System.out.println(split.length);


        System.out.println(System.currentTimeMillis());
    }

}
