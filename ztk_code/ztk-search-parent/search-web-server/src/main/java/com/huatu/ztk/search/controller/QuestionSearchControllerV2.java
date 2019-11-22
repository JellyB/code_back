package com.huatu.ztk.search.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.search.bean.QuestoinSearchBean;
import com.huatu.ztk.search.bean.SearchResult;
import com.huatu.ztk.search.config.SearchConfig;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;

/**
 * 对分享接口升级(v2)
 *
 * @author jbzm
 * @date 2018下午1:33
 **/
@RestController
@RequestMapping(value = "/v2/search/question", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionSearchControllerV2 {
    private static final Logger logger = LoggerFactory.getLogger(QuestionSearchControllerV2.class);
    private static final Logger reportLogger = LoggerFactory.getLogger("report");

//    @Autowired
//    private QuestionSearchServiceV2 questionSearchServiceV2;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SearchConfig searchConfig;

    /**
     * 查询试题
     *
     * @param q            查询内容
     * @param page         查询页数
     * @param size
     * @param questionType
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object search(@RequestParam String q,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size,
                         @RequestParam(defaultValue = "-1") int point,//知识点
                         @RequestParam(defaultValue = "-1") int year,//试题年份
                         @RequestParam(defaultValue = "-1") int area,//试题区域
                         @RequestParam(defaultValue = "1") int mode,//真题，模拟题
                         @RequestParam(defaultValue = "-1") int subject,//类目
                         @RequestParam(defaultValue = "-1") int questionType,
                         @RequestParam(defaultValue = "-1") int searchType,
                         @RequestHeader(required = false) String token,
                         @RequestHeader(required = false) Integer terminal,
                         HttpServletRequest request) throws BizException {
        logger.info("request params ={}", request.getParameterMap());
        reportLogger.info("13$$" + q + "$$" + System.currentTimeMillis());

        try {
            RestTemplate restTemplate = new RestTemplate();
            String requestParameter = "";
            if (q != null) {
                requestParameter = requestParameter + "keyword=" + q;
            }
            if (size != 20) {
                requestParameter = requestParameter + "&size=" + size;
            }
            if (page != 1) {
                requestParameter = requestParameter + "&page=" + page;
            }
            if (point != -1) {
                requestParameter = requestParameter + "&point=" + point;
            }
            if (year != -1) {
                requestParameter = requestParameter + "&year=" + year;
            }
            if (area != -1) {
                requestParameter = requestParameter + "&area=" + area;
            }
            if (mode != 1) {
                requestParameter = requestParameter + "&mode=" + mode;
            }
            if (subject != -1) {
                requestParameter = requestParameter + "&subject=" + subject;
            }
            if (questionType != -1) {
                requestParameter = requestParameter + "&questionType=" + questionType;
            }
            if (searchType != -1) {
                requestParameter = requestParameter + "&searchType=" + searchType;
            }
            if (token != null) {
                requestParameter = requestParameter + "&token=" + token;
            }

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("token", token);
            requestHeaders.add("terminal", terminal.toString());
            HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
            ResponseEntity<String> response = restTemplate.exchange(searchConfig.getProxy() + "ns/ht/v2/question?" + requestParameter, HttpMethod.GET, requestEntity, String.class);
            String firstMap = response.getBody();
//            String firstMap =
//                    restTemplate.get(searchConfig.getProxy() + "ns/ht/v2/question?" + requestParameter, String.class);
            if (firstMap != null) {
                JSONObject jsonObject = JSONObject.parseObject(firstMap);
                if (jsonObject.get("message") != null) {
                    throw new BizException(ErrorResult.create(Integer.valueOf(String.valueOf(jsonObject.get("code"))), String.valueOf(jsonObject.get("message"))));
                }
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray content = data.getJSONArray("content");
                List<QuestoinSearchBean> questoinSearchBeans = new LinkedList<>();
                for (int i = 0; i < content.size(); i++) {
                    questoinSearchBeans.add(content.getObject(i, QuestoinSearchBean.class));
                }

                SearchResult searchResult = SearchResult.builder().total(Long.valueOf(String.valueOf(data.get("totalElements")))).currentPage(1).results(questoinSearchBeans).build();
                return searchResult;
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw e;
            }

            logger.error("request new search fail");
            e.printStackTrace();
        }
//
//        //获得用户设置的考试科目
//        if (StringUtils.isNoneBlank(token)) {
//            userSessionService.assertSession(token);
//            subject = userSessionService.getSubject(token);
//        }
//
//        //事业单位职测用公务员的题目
//        if (subject == SubjectType.SYDW_XINGCE) {
//            subject = SubjectType.GWY_XINGCE;
//        }

//        final SearchResult searchResult = questionSearchServiceV2.search(q, page, size, point, year, area, mode, questionType, subject, searchType);
//        //去掉复合题
//        final List<QuestoinSearchBean> results = searchResult.getResults().stream()
//                .filter(questoinSearchBean -> questoinSearchBean.getType() != QuestionType.COMPOSITED)
//                .collect(Collectors.toList());
//        searchResult.setResults(results);
//        return searchResult;
        throw new BizException(ErrorResult.create(1000321, "暂无相关数据"));
    }
}
