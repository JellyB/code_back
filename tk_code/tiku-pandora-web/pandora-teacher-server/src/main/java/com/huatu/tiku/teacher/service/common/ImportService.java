package com.huatu.tiku.teacher.service.common;

import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * 数据迁移相关
 *
 * @author zhaoxi
 */
public interface ImportService extends BaseService<BaseQuestion>, ImportPaperService {

    /**
     * 试题 - mysql->mongo
     *
     * @param questionId
     */
    void importQuestion(long questionId);

    /**
     * 试题信息同步到mongo
     *
     * @param questionId
     */
    void sendQuestion2Mongo(int questionId);

    /**
     * 试题信息同步到mongo
     *
     * @param questionIdList
     */
    void sendQuestion2Mongo(List<Integer> questionIdList);

    /**
     * 试题同步到ES
     *
     * @param questionId
     */
    void sendQuestion2SearchForDuplicate(Long questionId);


    /**
     * 以试卷为单位，mysql同步试题到mongo中
     * isUpdateBizStatus 表识是否修改起状态
     */
    void sendQuestion2MongoByPaper(Boolean isUpdateBizStatus, Long paperId,  PaperInfoEnum.TypeInfo type);


    /**
     * 以知识点为单位，mysql同步试题到mongo中

     */
    void sendQuestion2MongoByKnowledge();


    /**
     * 同步科目下的所有试题题源
     * @param subject
     */
    void importQuestionSource(int subject);

}
