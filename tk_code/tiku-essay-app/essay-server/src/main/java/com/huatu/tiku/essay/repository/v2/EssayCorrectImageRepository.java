package com.huatu.tiku.essay.repository.v2;

import com.huatu.tiku.essay.entity.correct.CorrectImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface EssayCorrectImageRepository extends JpaRepository<CorrectImage, Long>, JpaSpecificationExecutor<CorrectImage> {

    /**
     * 查询答题卡绑定的图片信息
     * @param questionAnswerId
     * @param status
     * @return
     */
    List<CorrectImage> findByQuestionAnswerIdAndStatusOrderBySort(long questionAnswerId,int status);


    /**
     * 保存图片旋转
     * @param roll
     * @param id
     * @param date
     * @return
     */
    @Transactional
    @Modifying
    @Query("update CorrectImage ci set ci.roll=?1, ci.gmtModify = ?2 where ci.id=?3")
    int modifyImageRollById(int roll, Date date, long id);

    /**
     * 修改图片排序
     * @param sort
     * @param date
     * @param id
     * @return
     */
    @Transactional
    @Modifying
    @Query("update CorrectImage ci set ci.sort=?1, ci.gmtModify = ?2 where ci.id=?3 and ci.questionAnswerId=?4")
    int modifyImageSortById(int sort, Date date, long id, long answerId);


    /**
     * 保存图片旋转
     * @param status
     * @param date
     * @param imageId
     * @param answerId
     * @return
     */
    @Transactional
    @Modifying
    @Query("update CorrectImage ci set ci.status=?1, ci.gmtModify = ?2 where ci.id=?3 and ci.questionAnswerId =?4")
    int deleteImageByLogic(int status, Date date, long imageId, long answerId);

    /**
     * 通过答题卡 id 和 id 获取
     * @param answerId
     * @param id
     * @return
     */
    CorrectImage findOneByQuestionAnswerIdAndId(Long answerId, long id);

    /**
     * 通过答题卡查询答题卡图片
     * @param answerId
     * @return
     */
    List<CorrectImage> findByQuestionAnswerId(Long answerId);
}
