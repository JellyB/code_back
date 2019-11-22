package com.huatu.tiku.schedule.biz.domain;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


/**
 * @author wangjian
 **/
@Getter
@Setter
@Entity
public class Module extends BaseDomain {

    private static final long serialVersionUID = 5730966574014090038L;

    /**
     * 模块
     */
	private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subjectId", insertable = false, updatable = false)
    private Subject subject;

    private Long subjectId;

}
