package com.huatu.tiku.position.biz.domain;

import com.huatu.tiku.position.base.domain.BaseDomain;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;

/**点击推荐报告记录
 * @author wangjian
 **/
@Data
@Entity
public class RecommendReccord extends BaseDomain {

    private static final long serialVersionUID = 6501648638931092531L;

    @Column(unique = true)
    private Long userId;//用户id

    private Integer count;

}
