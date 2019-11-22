package com.huatu.ztk.knowledge.daoPandora.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;

/**
 * @author huangqingpeng
 * @title: QuestionErrorDownTaskProvider
 * @description: TODO
 * @date 2019-09-2915:15
 */
@Slf4j
public class QuestionErrorDownTaskProvider {


    public String findOrderByUserId(long uid, int page, int size) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" order_num,total as payMsg,gmt_create as payTime ")
                .append(" from ")
                .append(" question_error_download_task ");
        sql.append(" where ");
        sql.append(" user_id = ").append(uid);
        sql.append(" AND total > 0  ");
        sql.append(" ORDER BY gmt_create DESC ");
        sql.append(" limit ").append((page - 1) * size).append(",").append(size);
        log.info("findOrderByUserId sql = {}", sql.toString());
        return sql.toString();
    }

    public String countOrderByUserId(@Param("userId") long userId){
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" count(1) ")
                .append(" from ")
                .append(" question_error_download_task ");
        sql.append(" where ");
        sql.append(" user_id = ").append(userId);
        sql.append(" AND total > 0  ");
        log.info("countOrderByUserId sql = {}", sql.toString());
        return sql.toString();
    }


    public String findDownList(long uid, int subject, int page, int size){
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" * ")
                .append(" from ")
                .append(" question_error_download_task ");
        sql.append(" where ");
        sql.append(" user_id = ").append(uid);
        sql.append(" AND subject =").append(subject);
        sql.append(" AND status > 0 ");
        sql.append(" ORDER BY gmt_create DESC ");
        sql.append(" limit ").append((page - 1) * size).append(",").append(size);
        log.info("findDownList sql = {}", sql.toString());
        return sql.toString();
    }

    public String countDownList(long uid, int subject){
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" count(1) as fileCount,sum(file_size) as fileSize ")
                .append(" from ")
                .append(" question_error_download_task ");
        sql.append(" where ");
        sql.append(" user_id = ").append(uid);
        sql.append(" AND subject =").append(subject);
        sql.append(" AND status > 0 ");
        log.info("countDownList sql = {}", sql.toString());
        return sql.toString();
    }
}
