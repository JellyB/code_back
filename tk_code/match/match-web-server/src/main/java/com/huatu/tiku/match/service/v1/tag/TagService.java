package com.huatu.tiku.match.service.v1.tag;

import com.huatu.tiku.match.common.Tag;
import com.huatu.ztk.commons.exception.BizException;

import java.util.List;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-10-24 上午10:07
 **/
public interface TagService {

    /**
     * 模考获取tags
     * @param subject
     * @return
     * @throws BizException
     */
    List<Tag> getTags(int subject)throws BizException;

    /**
     * 获取含有模考大赛的相关的所有科目
     * @param subject
     * @return
     */
    Object getSubjectList(int subject);

    /**
     * 获得支持模考大赛的所有考试类型信息
     * @return
     * @param terminal
     */
    Object getMatchCategory(int terminal);

}
