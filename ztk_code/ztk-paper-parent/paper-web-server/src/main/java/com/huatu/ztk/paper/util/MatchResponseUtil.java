package com.huatu.ztk.paper.util;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.common.PaperErrorInfo;
import com.huatu.ztk.paper.common.ResponseMsg;
import org.apache.commons.collections.MapUtils;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 处理模考大赛外部接口返回数据结果
 * Created by huangqingpeng on 2019/3/12.
 */
public class MatchResponseUtil {

    /**
     * 新模考大赛首页模考信息查询
     *
     * @return
     */
    public static List<LinkedHashMap> getMatches(String token, int subject) throws BizException {
        ResponseEntity<ResponseMsg> matches = RestTemplateUtil.matches(token, subject);
        ResponseMsg body = matches.getBody();
        if (null == body) {
            return Lists.newArrayList();
        }
        Object data = body.getData();
        if (null == data) {
            return Lists.newArrayList();
        }
        if (data instanceof LinkedHashMap) {
            List list = (List) ((LinkedHashMap) data).get("list");
            return list;
        }
        return Lists.newArrayList();
    }


    /**
     * 创建答题卡或者继续答题
     *
     * @param token
     * @param paper
     * @param terminal
     * @return
     */
    public static Object createAnswerCard(String token, Paper paper, int terminal) {
        ResponseEntity<ResponseMsg> responseEntity = RestTemplateUtil.createAnswerCard(token, paper.getId(), terminal);
        ResponseMsg body = responseEntity.getBody();
        if (null == body) {
            return null;
        }
        transAnswerCard(body);
        Object data = body.getData();
        if(data instanceof LinkedHashMap){
            ((LinkedHashMap) data).put("paper",paper);
            ((LinkedHashMap) data).put("type", AnswerCardType.MATCH);
            ((LinkedHashMap) data).put("status", AnswerCardStatus.UNDONE);
        }
        return data;
    }

    /**
     * 查看报告
     *
     * @param token
     * @param paperId
     * @return
     */
    public static Object getReport(String token, int paperId) {
        ResponseEntity<ResponseMsg> responseEntity = RestTemplateUtil.getReport(token, paperId);
        ResponseMsg body = responseEntity.getBody();
        if (null == body) {
            return null;
        }
        transAnswerCard(body);
        return body.getData();
    }

    private static void transAnswerCard(ResponseMsg body) {
        handleException(body);
        LinkedHashMap data = (LinkedHashMap) body.getData();
        Object answers = MapUtils.getObject(data, "answers");
        if (null == answers) {
            return;
        }
        List answerList = (List) answers;
        for (int i = 0; i < answerList.size(); i++) {
            Object o = answerList.get(i);
            if(o == null || "null".equalsIgnoreCase(String.valueOf(o))){
                answerList.set(i,"0");
            }
        }
        data.put("answers", answerList);
    }

    private static void handleException(ResponseMsg body) {
        int code = body.getCode();
        if (code != 1000000) {
            PaperErrorInfo.AnswerCard answerCard = PaperErrorInfo.AnswerCard.create(code);
            answerCard.exception();
        }
    }
}
