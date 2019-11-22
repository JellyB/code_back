package com.huatu.tiku.position.biz.domain;

import com.huatu.tiku.position.base.domain.BaseDomain;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**浏览记录
 * @author wangjian
 **/
@Data
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames={"userId", "positionId"})})
public class BrowseRecord extends BaseDomain{

    private static final long serialVersionUID = 6572483869907133777L;

    private Long userId;//用户id

    private Long positionId;//职位id

    private Boolean accordFlag;//是否符合备注条件

    private Boolean collectionFlag;//是否收藏
}
