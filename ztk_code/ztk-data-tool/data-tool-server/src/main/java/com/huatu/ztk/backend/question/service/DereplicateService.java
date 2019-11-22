package com.huatu.ztk.backend.question.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.util.HttpTool;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * Created by huangqp on 2017\11\20 0020.
 */
@Service
public class DereplicateService{
    private final static Logger logger = LoggerFactory.getLogger(DereplicateService.class);
    private final static String URL  = "http://123.103.86.62/catools/srv/aat/update/";
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public int dereplicate(Integer[] data) throws IOException {
//        Integer[] ztk = {52859,52538};
        List<Integer> ids = new ArrayList<Integer>(Arrays.asList(data));
        List<Map<String,Object>> questions = getQuestionsByIds(ids);
        logger.info("questions={}",questions);
        List<Integer> status1 = Lists.newArrayList();
        List<Integer> status3 = Lists.newArrayList();
        List<Integer> newIds = Lists.newArrayList();
        for(Map<String,Object> question:questions){
            if("1".equals(String.valueOf(question.get("bb102")))){
                status1.add(Integer.parseInt(String.valueOf(question.get("pukey"))));
            }else if("-3".equals(String.valueOf(question.get("bb102")))){
                status3.add(Integer.parseInt(String.valueOf(question.get("pukey"))));
            }else{
                logger.error("状态有问题，quesiton={}",question);
            }
            if("1".equals(String.valueOf(question.get("status")))){
                newIds.add(Integer.parseInt(String.valueOf(question.get("pukey"))));
            }
        }
        //先判断是否有最新改动的记录，没有的话，直接报错，并结束进程
        if(CollectionUtils.isEmpty(newIds)){
            logger.error("所有相似试题均没有更新过！！！！！！");
            int id = updateOnlyDereplicate(status1,status3);
            return id;
        }
        if(status1.size()==0){
            logger.info("没有状态正常的试题，将一条记录最近表动的试题表位状态正常");
            final int id = newIds.get(0);
            status1.add(id);
            status3.removeIf(i->i==id);
        }else if(status1.size()>1){
            logger.info("状态正常的试题不止一道，留下一道存在newIds中的试题，其他的扔到status3中");
            int id = -1;
            for(int i=0;i<status1.size();i++){
                int tmpId = status1.get(i);
                if(newIds.contains(tmpId)&&id==-1){
                    id = tmpId;
                    continue;
                }
                status3.add(tmpId);
            }
            //如果状态正常的试题，都没有被修改过，那就从newIds中取一个，并从staus3中删除
            if(id==-1){
                id = newIds.get(0);
                final int l = id;
                status3.removeIf(i->i==l);
            }
            status1 = new ArrayList<>();
            status1.add(id);
        }
        //经过上边的逻辑调整，可能试题状态需要重置
        resetStatus(status1,1);
        resetStatus(status3,-3);
        //处理所有真题的关联题目关系
        resetPastpaperQuestion(status3,status1.get(0));
        return status1.get(0);
    }

    private int updateOnlyDereplicate(List<Integer> status1, List<Integer> status3) throws IOException {
        int id = -1;
        if(!CollectionUtils.isEmpty(status1)){
            id = status1.get(0);
            status1.remove(0);
            status3.addAll(status1);
        }
        if(id == -1&&!CollectionUtils.isEmpty(status3)){
            id = status3.get(0);
            status3.remove(0);
        }
        Integer[] ids = {id};
        logger.info("将所有的ids={}变为{}",status3,id);
        resetStatus(Arrays.asList(ids),1);
        resetStatus(status3,-3);
        //处理所有真题的关联题目关系
        resetPastpaperQuestion(status3,id);
        return id;
    }

    /**
     * 查询并处理所有的v_pastpaper_question_r表，同时根据改动的试卷id,重新生成组卷策略
     * @param status3
     * @param id
     */
    private void resetPastpaperQuestion(List<Integer> status3, Integer id) throws IOException {
        List<Integer> pastpaperIds = getPastpaerIdsByQuestion(status3);
        updatePastpaperQuestion(status3,id);
        logger.info("需要更新的试卷id={}",pastpaperIds);
        sendHttpMsg(pastpaperIds);
    }

    public void sendHttpMsg(List<Integer> pastpaperIds) throws IOException {
        for(Integer id:pastpaperIds){
            logger.info("get 请求发送请求，url = {}",URL+id);
            String result=HttpTool.sendGet(URL+id,"");
            logger.info("get 请求发送成功，result = {}",result);
        }
    }

    private void updatePastpaperQuestion(List<Integer> ids, Integer id) {
        if(CollectionUtils.isEmpty(ids)){
            return ;
        }
        String sql = "update v_pastpaper_question_r set question_id =  "+id+"  WHERE question_id  IN (:ids )";
        String sql1 = "update v_pastpaper_question_r_log set question_new_id =  "+id+"  WHERE question_id  IN (:ids )";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        int l = namedJdbcTemplate.update(sql, parameters);
        int k = namedJdbcTemplate.update(sql1, parameters);
        logger.info("sql={}，成功的记录={}",sql,l);
    }

    private List<Integer> getPastpaerIdsByQuestion(List<Integer> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return Lists.newArrayList();
        }
        String sql  = "select pastpaper_id from v_pastpaper_question_r where question_id in (:ids)";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        List<Integer> result = namedJdbcTemplate.queryForList(sql, parameters,Integer.class);
        return result;
    }

    private void resetStatus(List<Integer> ids,int i) {
        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        String sql = "update v_obj_question set bb102 =  "+i+"  WHERE PUKEY  IN (:ids )";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        int l = namedJdbcTemplate.update(sql, parameters);
        logger.info("sql={}，成功记录={}",sql,l);
    }

    private List<Map<String,Object>> getQuestionsByIds(List<Integer> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return Lists.newArrayList();
        }
        String sql = "select pukey,stem,bb102,if(BB106>1506787200,1,-1) AS status from v_obj_question WHERE PUKEY  IN (:ids )";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        List<Map<String,Object>> list = namedJdbcTemplate.queryForList(sql, parameters);
        return list;
    }

}
