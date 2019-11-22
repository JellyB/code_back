package com.huatu.tiku.entity.download;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @author huangqingpeng
 * @title: QuestionErrorDownloadTask
 * @description: TODO
 * @date 2019-09-2309:49
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question_error_download_task")
public class QuestionErrorDownloadTask extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 名称
     */
    private String name;

    /**
     * 科目ID
     */
    private Integer subject;

    /**
     * 预计下载题量
     */
    private Integer num;

    /**
     * 实际下载题量
     */
    private Integer sum;

    /**
     * 试题ID
     */
    private String questionIds;

    /**
     * 答题卡ID
     */
    private Long answerId;

    /**
     * 下载地址
     */
    private String fileUrl;
    /**
     * 下载文件大小
     */
    private String size;
    /*bizStatus字段——0表示初始化，1表示生成答题卡2表示生成下载地址3表示下载失败*/

    /**
     * 文件字节大小
     */
    private Long fileSize;
    /**
     * 消费金币的订单ID
     */
    private Long orderNum;
    /**
     * 金币总消费值，（不是单条记录的金币消费额，而是以订单为单位的所有一次下载金币消费额）
     */
    private Long total;

    @Builder
    public QuestionErrorDownloadTask(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Long userId, String name, Integer subject, Integer num, Integer sum, String questionIds, Long answerId, String fileUrl, String size, Long fileSize, Long orderNum, Long total) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.userId = userId;
        this.name = name;
        this.subject = subject;
        this.num = num;
        this.sum = sum;
        this.questionIds = questionIds;
        this.answerId = answerId;
        this.fileUrl = fileUrl;
        this.size = size;
        this.fileSize = fileSize;
        this.orderNum = orderNum;
        this.total = total;
    }
}


