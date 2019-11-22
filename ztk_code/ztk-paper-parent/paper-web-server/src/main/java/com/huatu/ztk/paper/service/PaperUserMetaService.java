package com.huatu.ztk.paper.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.PaperUserMeta;
import com.huatu.ztk.paper.dao.PracticeRecordDao;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-04-29 14:56
 */

@Service
public class PaperUserMetaService {
    private static final Logger logger = LoggerFactory.getLogger(PaperUserMetaService.class);

    @Autowired
    private PracticeRecordDao practiceRecordDao;

    /**
     * 获取答卷统计记录id
     *
     * @param uid
     * @param paperId
     * @return
     */
    public String getId(long uid, int paperId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(uid).append("_").append(paperId);
        return stringBuilder.toString();
    }

    /**
     * 根据用户id，批量查询用户作答试卷记录
     *
     * @param uid
     * @param paperIds
     * @return
     */
    public List<PaperUserMeta> findBatch(long uid, List<Integer> paperIds) {
        if (uid < 0L) {
            return Lists.newArrayList();
        }
        List<String> ids = new ArrayList<>(paperIds.size());
        if (CollectionUtils.isEmpty(paperIds)) {
            return Lists.newArrayList();
        }
        for (Integer paperId : paperIds) {
            String is = getId(uid, paperId);
            //  logger.info("huang is:"+is);
            ids.add(is);
        }
        List<PaperUserMeta> paperUserMetas = practiceRecordDao.findByIds(ids);
        if (CollectionUtils.isEmpty(paperUserMetas)) {
            return Lists.newArrayList();
        }
        return paperUserMetas;
    }

    /**
     * 根据用户id，查询单个试卷的答题卡
     *
     * @param uid
     * @param paperId
     * @return
     */
    public PaperUserMeta findById(long uid, int paperId) {
        String id = getId(uid, paperId);
        return practiceRecordDao.findById(id);
    }

    /**
     * 新增未完成的练习记录
     *
     * @param userId     用户id
     * @param paperId    试卷id
     * @param practiceId 练习id
     */
    public void addUndoPractice(long userId, int paperId, long practiceId) {
        String id = getId(userId, paperId);
        PaperUserMeta paperUserMeta = practiceRecordDao.findById(id);
        if (paperUserMeta == null) {//没有则创建新的
            paperUserMeta = PaperUserMeta.builder()
                    .id(id)
                    .uid(userId)
                    .practiceIds(new ArrayList<Long>())
                    .paperId(paperId)
                    .currentPracticeId(practiceId)
                    .build();
            practiceRecordDao.insert(paperUserMeta);
        }

        paperUserMeta = practiceRecordDao.findById(id);
        //设置为完成练习id
        paperUserMeta.setCurrentPracticeId(practiceId);
        //不包含，则添加id
        if (!paperUserMeta.getPracticeIds().contains(practiceId)) {//添加练习id
            paperUserMeta.getPracticeIds().add(practiceId);
        }
        //练习完成次数
        paperUserMeta.setFinishCount(paperUserMeta.getPracticeIds().size());
        practiceRecordDao.save(paperUserMeta);

    }

    /**
     * 添加完成的练习
     *
     * @param userId
     * @param paperId
     * @param practiceId
     */
    public void addFinishPractice(long userId, int paperId, long practiceId) {
        String id = getId(userId, paperId);
        PaperUserMeta paperUserMeta = practiceRecordDao.findById(id);
        //TODO 去掉下边代码
        //检查，如果不包含，则添加进去，这段代码只是为了数据迁移时使用，正常来说不会出现此情况
        if (paperUserMeta == null) {//没有则创建新的
            paperUserMeta = PaperUserMeta.builder()
                    .id(id)
                    .uid(userId)
                    .paperId(paperId)
                    .finishCount(0)
                    .practiceIds(new ArrayList<Long>())
                    .currentPracticeId(-1).build();
            practiceRecordDao.insert(paperUserMeta);
        }
        //检查，如果不包含，则添加进去，这段代码只是为了数据迁移时使用，正常来说不会出现此情况
        if (paperUserMeta.getPracticeIds().indexOf(practiceId) < 0) {
            paperUserMeta.getPracticeIds().add(practiceId);
        }
        //练习完成次数
        paperUserMeta.setFinishCount(paperUserMeta.getPracticeIds().size());
        //设置当前id为-1,表示没有未完成的练习
        paperUserMeta.setCurrentPracticeId(-1);
        practiceRecordDao.save(paperUserMeta);
    }

    /**
     * 根据用户id，paperId,大纲Id查询用户答题卡Id
     *
     * @param uid        用户Id
     * @param paperId    试卷Id
     * @param syllabusId 大纲Id
     * @return
     */
    public PaperUserMeta findByPaperIdAndSyllabusId(long uid, int paperId, long syllabusId) {
        String id = getId(uid, paperId, syllabusId);
        return practiceRecordDao.findById(id);
    }

    private String getId(long uid, int paperId, long syllabusId) {
        String id = getId(uid, paperId);
        if (!Strings.isNullOrEmpty(id)) {
            id = id + "_" + syllabusId;
        }
        return id;
    }

    /**
     * 新增未完成的练习记录
     *
     * @param userId     用户id
     * @param paperId    试卷id
     * @param practiceId 练习id
     */
    public void addPeriodTestPractice(long userId, int paperId, long practiceId, long syllabusId) {
        String id = getId(userId, paperId, syllabusId);
        PaperUserMeta paperUserMeta = practiceRecordDao.findById(id);
        if (paperUserMeta == null) {//没有则创建新的
            paperUserMeta = PaperUserMeta.builder()
                    .id(id)
                    .uid(userId)
                    .practiceIds(new ArrayList<Long>())
                    .paperId(paperId)
                    .currentPracticeId(practiceId)
                    .syllabusId(syllabusId).build();
            practiceRecordDao.insert(paperUserMeta);
        } else {
            //设置为完成练习id
            paperUserMeta.setCurrentPracticeId(practiceId);
            paperUserMeta.setSyllabusId(syllabusId);
        }

        //不包含，则添加id
        if (!paperUserMeta.getPracticeIds().contains(practiceId)) {//添加练习id
            paperUserMeta.getPracticeIds().add(practiceId);
        }
        //练习完成次数
        paperUserMeta.setFinishCount(paperUserMeta.getPracticeIds().size());
        practiceRecordDao.save(paperUserMeta);
    }

    /**
     * 添加完成阶段测试练习
     *
     * @param userId
     * @param paperId
     * @param practiceId
     */
    public void addPeriodTestFinishPractice(long userId, int paperId, long practiceId, long syllabusId) {
        String paperUserMetaId = getId(userId, paperId, syllabusId);
        PaperUserMeta paperUserMeta = practiceRecordDao.findById(paperUserMetaId);
        //TODO 去掉下边代码
        //检查，如果不包含，则添加进去，这段代码只是为了数据迁移时使用，正常来说不会出现此情况
        if (paperUserMeta == null) {//没有则创建新的
            paperUserMeta = PaperUserMeta.builder()
                    .id(paperUserMetaId)
                    .uid(userId)
                    .paperId(paperId)
                    .finishCount(0)
                    .practiceIds(new ArrayList<Long>())
                    .currentPracticeId(-1).build();
            practiceRecordDao.insert(paperUserMeta);
        }
        //检查，如果不包含，则添加进去，这段代码只是为了数据迁移时使用，正常来说不会出现此情况
        if (paperUserMeta.getPracticeIds().indexOf(practiceId) < 0) {
            paperUserMeta.getPracticeIds().add(practiceId);
        }
        //练习完成次数
        paperUserMeta.setFinishCount(paperUserMeta.getPracticeIds().size());
        //设置当前id为-1,表示没有未完成的练习
        paperUserMeta.setCurrentPracticeId(-1);
        practiceRecordDao.save(paperUserMeta);
    }

    /**
     * 删除
     * @param id
     * @return
     */
    public void delete(String id){
        logger.info("删除 paper user meta id:{}", id);
        practiceRecordDao.delete(id);
    }
}
