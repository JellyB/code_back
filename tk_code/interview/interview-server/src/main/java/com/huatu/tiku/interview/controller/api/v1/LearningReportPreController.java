package com.huatu.tiku.interview.controller.api.v1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.interview.constant.BasicParameters;
import com.huatu.tiku.interview.constant.UserStatusConstant;
import com.huatu.tiku.interview.entity.po.User;
import com.huatu.tiku.interview.service.LearningReportService;
import com.huatu.tiku.interview.service.UserService;
import com.huatu.tiku.interview.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * 学习报告授权处理
 * Created by x6 on 2018/1/22.
 */
@Controller
@Slf4j
@RequestMapping("/api/lr/pre")
public class LearningReportPreController {
    @Value("${dailyReportURL}")
    private String dailyReportURL;
    @Value("${phone_check}")
    private String phoneCheckUrl;
    @Value("${domainName}")
    private String domainName;
    @Autowired
    private LearningReportService learningReportService;
    @Autowired
    private UserService userService;

    @LogPrint
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String weixinRedirect(HttpServletRequest request, HttpServletResponse response) {
        log.info("--------------开始oauth跳转------------");

        return "redirect:https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + BasicParameters.appID + "&redirect_uri="+domainName+"/wx/api/lr/pre/oauth?response_type=code&scope=snsapi_base&state=1&connect_redirect=1#wechat_redirect";
    }

    @LogPrint
    @RequestMapping(value = "/oauth", method = RequestMethod.GET)
    public ModelAndView weixinOAuth(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //得到code
        String CODE = request.getParameter("code");
        String APPID = BasicParameters.appID;
        String SECRET = BasicParameters.appsecret;
        //换取access_token 其中包含了openid
        String URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+APPID+"&secret="+SECRET+"&code="+CODE+"&grant_type=authorization_code";
        //URLConnectionHelper是一个模拟发送http请求的类
        HttpGet httpGet = new HttpGet(URL);
        HttpResponse execute = httpClient.execute(httpGet);
        HttpEntity entity = execute.getEntity();
        String jsonStr = EntityUtils.toString(entity);
        JSONObject jsonObject = (JSONObject) JSON.parse(jsonStr);
        String openId = jsonObject.get("openid").toString();
        //有了用户的opendi就可以的到用户的信息了
        //得到用户信息之后返回到一个页面
        ModelAndView view = new ModelAndView();

        //如果用户状态不正确,直接提示
        User user = userService.getUser(openId);
        if(user.getBizStatus() != UserStatusConstant.BizStatus.COMPLETED.getBizSatus()){

            view.setViewName("redirect:"+phoneCheckUrl + "openId=" + openId);

            return view;
        }
        String date = "";
        List<String> dateList = (List<String>)learningReportService.date(openId);
        if(CollectionUtils.isNotEmpty(dateList)){
            date = dateList.get(dateList.size() -1 );
        }
        //点击查看学习效果跳转到最后一天
        view.setViewName("redirect:"+String.format(dailyReportURL,openId,date));
        return view;

    }

}
