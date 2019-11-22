package com.huatu.tiku.essay.repository.v2;

import com.huatu.tiku.essay.entity.correct.CorrectImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 描述：批注图片dao
 *
 * @author biguodong
 * Create time 2019-06-28 2:07 PM
 **/
public interface EssayLabelImageRepository extends JpaRepository<CorrectImage, Long>, JpaSpecificationExecutor<CorrectImage> {


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

    @Transactional
    @Modifying
    @Query("update CorrectImage ci set ci.finalUrl=?1, ci.gmtModify = ?2 where ci.id=?3")
    int updateImageFinalUrl(String finalUrl, Date date, long id);
}
