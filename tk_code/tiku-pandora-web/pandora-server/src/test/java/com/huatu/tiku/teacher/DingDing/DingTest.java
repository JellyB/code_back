package com.huatu.tiku.teacher.DingDing;


import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.teacher.util.dingTalkNotice.DingLinkVo;
import com.huatu.tiku.teacher.util.dingTalkNotice.DingTextVo;
import com.huatu.tiku.teacher.util.dingTalkNotice.DingTalkNoticeUtil;
import com.huatu.ztk.commons.JsonUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;


/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/28
 * @描述
 */
public class DingTest extends TikuBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(DingTest.class);

    private static String dingApiUrl = "https://oapi.dingtalk.com/robot/send?access_token=efa2431acd8b8e90c87c9327d2a00f20904b61910559bf82395fbf07baaae29c";

    @Autowired
    private DingTalkNoticeUtil dingTalkNotice;

    @Test
    public void test1() {
        String content = "今天周五了";
        // dingTalkNotice.notice(content);

        // dingTalkNotice.noticeLink(content);

        //combineNotice(content);
    }


    @Test
    public void testTextDing() {
        String content = "今天是个好日子,心想的事儿都能成～";
        Boolean atAll = false;
        String mobiles = "17611401891";
        DingTextVo textVo = DingTextVo.builder().content(content).atAll(atAll).mobiles(mobiles).build();

        logger.info("结果是:{}", JsonUtil.toJson(textVo));
        dingTalkNotice.textNotice(textVo);
    }


    @Test
    public void testLinkDing() {

        String messageUrl = "https://www.dingtalk.com/";
        String picUrl = "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=1802563968,897623865&fm=26&gp=0.jpg";
        String title = "时代的火车向前开";
        String text = "这个即将发布的新版本，创始人陈航（花名“无招”）称它为“红树林“";

        DingLinkVo dingLinkVo = DingLinkVo.builder().messageUrl(messageUrl).picUrl(picUrl).title(title).text(text)
                .build();
        logger.info("testLinkDing 参数是:{}", JsonUtil.toJson(dingLinkVo));
        dingTalkNotice.linkNotice(dingLinkVo);
    }

    /**
     * httpClient方式调用
     */
   /* @Test
    public void testDingWarning() {
        HashMap contentMap = new HashMap();
        contentMap.put("content", "我是钉钉小可爱");

        HashMap param = new HashMap();
        param.put("msgtype", "text");
        param.put("text", contentMap);

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(dingApiUrl);
        httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
        String resultParam = JSONObject.toJSONString(param);
        StringEntity se = new StringEntity(resultParam, "utf-8");
        httpPost.setEntity(se);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            logger.info("返回值是:{}", response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                logger.info("错误信息是:{}", result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    @Test
    public void linkNotice() {

        HashMap message = new HashMap();
        message.put("msgtype", "link");

        HashMap map = new HashMap();
        map.put("messageUrl", "https://www.dingtalk.com/");
        map.put("picUrl", "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=1802563968,897623865&fm=26&gp=0.jpg");
        map.put("title", "时代的火车向前开");
        map.put("text", "这个即将发布的新版本，创始人陈航（花名“无招”）称它为“红树林“");
        message.put("link", map);
        String jsonMessage = JsonUtil.toJson(message);
        logger.info("Ding请求参数是:{}", jsonMessage);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> responseResult = restTemplate.postForEntity(dingApiUrl, message, Map.class);
        if (responseResult.getStatusCode() != org.springframework.http.HttpStatus.OK) {
            logger.info("钉钉发送失败，信息是:{}", responseResult);
        }
    }

}
