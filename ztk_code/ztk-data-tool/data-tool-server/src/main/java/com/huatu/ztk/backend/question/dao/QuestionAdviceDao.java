package com.huatu.ztk.backend.question.dao;

import com.huatu.ztk.backend.question.bean.AdviceBean;
import com.huatu.ztk.backend.question.bean.AdviceStatus;
import com.huatu.ztk.backend.question.bean.QuestionAdvice;
import com.huatu.ztk.commons.AreaConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by ht on 2017/1/4.
 */
@Repository
public class QuestionAdviceDao {

    private static final Logger logger = LoggerFactory.getLogger(QuestionAdviceDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取试题纠错列表
     * @param advice  试题纠错对象
     * @return
     */
    public List<AdviceBean> list(AdviceBean advice,String area){
        String sql=" select c.PUKEY,c.question_id,c.question_area,c.qtype,c.bl_sub_exam,c.error_type,c.error_descrp,c.BB105,c.EB102,c.BB103,c.checker,c.check_content,c.accept_time,u.uname,v.uname as handle " +
                   "  from v_question_correction_log c " +
                   "  LEFT JOIN v_qbank_user u on c.BB105=u.PUKEY " +
                   "  LEFT JOIN v_c_sys_user v on v.pukey=c.acceptor where 1=1 and c.subject="+advice.getSubject();
        //试题模块
        if(advice.getModuleId()>0){
            sql+=" and c.module_id="+advice.getModuleId();
        }
        //地区
        if(!"0".equals(area)){
            sql+=" and c.question_area in ("+area+")";
        }
        //题库类型
        if(advice.getQType()>0){
            if (advice.getQType() == 1) {
                sql+=" and c.qtype in (99,100, 101, 109)";
            } else if (advice.getQType() == 2) {
                sql+=" and c.qtype =105 ";
            }else if (advice.getQType() == 3) {
                sql+=" and c.qtype =106 ";
            }else if (advice.getQType() == 4) {
                sql+=" and c.qtype =107 ";
            }
        }
        if(advice.getStatus()>0){
            sql+=" and checker="+advice.getStatus();
        }
        //错误类型
        if(advice.getErrorType()>0){
            sql+=" and c.error_type="+advice.getErrorType();
        }
        //处理人
        if(StringUtils.isNoneEmpty(advice.getHandler())){
            sql+=" and v.uname like '%"+advice.getHandler()+"%'";
        }
        //当前用户
        if(advice.getIsMine()>0){
            sql+=" and c.acceptor="+advice.getIsMine();
        }
        //排序
        if(advice.getOrderTime()>0){ //0:提交时间，1：处理时间
            sql+=" order by accept_time desc limit 0,100";
        }else{
            sql+=" order by BB103 desc limit 0,100";
        }


        List<AdviceBean> questionAdviceList=  jdbcTemplate.query(sql,new AdviceBeanRowMapper());
        if (CollectionUtils.isEmpty(questionAdviceList)) {
            return  new ArrayList<>();
        }
        return questionAdviceList;
    }

    /**
     * 获取试题纠错信息
     * @param id
     * @return
     */
    public QuestionAdvice findById(int id){
        String sql=" select * from v_question_correction_log where pukey=? ";
        Object[] param = {id};
        final List<QuestionAdvice> advices =jdbcTemplate.query(sql, param, new RowMapper<QuestionAdvice>() {
            @Override
            public QuestionAdvice mapRow(ResultSet rs, int i) throws SQLException {
                final QuestionAdvice questionAdvice = QuestionAdvice.builder()
                        .id(rs.getInt("pukey"))
                        .content(rs.getString("error_descrp"))
                        .qid(rs.getInt("question_id")).build();
                return questionAdvice;
            }
        });
        if (CollectionUtils.isNotEmpty(advices)) {
            return advices.get(0);
        }
        return new QuestionAdvice();

    }

    /**
     * 处理不采纳
     * @param id
     * @param reason  不采纳原因
     */
    public void dealNoAdoption(int id,String reason){
        String sql=" update v_question_correction_log set checker=?,check_content=?,accept_time=? where pukey=? and checker="+AdviceStatus.NODEAL;
        Object[] param = {AdviceStatus.NOADOPTION,reason,new Date().getTime()/1000,id};
        jdbcTemplate.update(sql,param);
        logger.info("dealNoAdoption success,id={}", id);
    }
    /**
     * 处理采纳
     * @param id
     */
    public void dealAdoption(int id){
        String sql=" update v_question_correction_log set checker=?,accept_time=?  where pukey=?";
        Object[] param = {AdviceStatus.ADOPTION,new Date().getTime()/1000,id};
        jdbcTemplate.update(sql,param);
        logger.info("dealAdoption success,id={}", id);
    }

    /**
     * 处理为不使用
     * @param id
     */
    public void dealNoUse(int id){
        String sql=" update v_question_correction_log set checker=?,accept_time=?  where question_id in (?) and checker="+AdviceStatus.NODEAL;
        Object[] param = {AdviceStatus.NOUSE,new Date().getTime()/1000,getQuestionId(id)};
        jdbcTemplate.update(sql,param);
        logger.info("dealNoUse success,id={}", id);
    }

    /**
     * 处理纠错试题的为使用状态
     * @param id
     */
    public void dealUse(int id){
        String sql=" update v_question_correction_log set checker=?,accept_time=?  where question_id in (?) and checker= "+AdviceStatus.NOUSE;
        Object[] param = {AdviceStatus.NODEAL,new Date().getTime()/1000,getQuestionId(id)};
        jdbcTemplate.update(sql,param);
        logger.info("dealUse success,id={}", id);
    }

    /**
     * 删除试题纠错
     * @param id
     */
    public void delete(int id){
        String sql=" delete from v_question_correction_log where pukey=? ";
        Object[] param = {id};
        jdbcTemplate.update(sql,param);
        logger.info("delete success,id={}", id);
    }

    /**
     * 获取试题id
     * @param id
     * @return
     */
    public String getQuestionId(int id){
        logger.info("v_question_correction_log={question_id}",id);
        String sql="select question_id from v_question_correction_log where pukey=?";
        Object[] param = {id};
       List<Integer> qIds= jdbcTemplate.query(sql, param, new RowMapper<Integer>() {
           @Override
           public Integer mapRow(ResultSet resultSet, int i) throws SQLException {
               return resultSet.getInt("question_id");
           }
       });
        if(CollectionUtils.isNotEmpty(qIds)){
           return StringUtils.join(qIds.toArray(),",");
        }
        return "";
    }

    class AdviceBeanRowMapper implements RowMapper<AdviceBean> {
        public AdviceBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            final AdviceBean adviceBean = AdviceBean.builder()
                    .id(rs.getInt("pukey"))
                    .qid(rs.getInt("question_id"))      //试题id
               //     .stem(rs.getString("stem"))         //试题题干
                    .qArea(rs.getInt("question_area"))     //地区
                    .areaName(AreaConstants.getFullAreaNmae(rs.getInt("question_area")))  //地区名称
               //     .mode(rs.getInt("is_ture_answer")) //是否真题
                    .uid(rs.getInt("BB105"))            //反馈人
                    .uname(rs.getString("uname"))       //反馈名称
                    .handler(rs.getString("handle"))   //处理人
                    .catgory(rs.getInt("bl_sub_exam"))  //科目
                    .errorType(rs.getInt("error_type"))  //错误类型
                    .qType(rs.getInt("qtype")) //试题类型
                    .contacts(rs.getString("EB102")) //联系方式
                    .content(rs.getString("error_descrp"))  //纠错内容
                    .createTime(rs.getLong("BB103")*1000)  //创建时间
                    .status(rs.getInt("checker"))   //试题纠错状态
                    .acceptContent(rs.getString("check_content"))  //回复内容
                    .acceptTime(rs.getLong("accept_time")*1000)   //回复时间
                    .build();
            return adviceBean;
        }
    }

}
