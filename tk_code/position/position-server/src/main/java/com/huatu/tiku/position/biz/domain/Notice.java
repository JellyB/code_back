package com.huatu.tiku.position.biz.domain;

import com.huatu.tiku.position.base.domain.BaseDomain;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**公告
 * @author wangjian
 **/
@Data
@Entity
public class Notice extends BaseDomain{
    private static final long serialVersionUID = 5852662763766735181L;

    private String name;

    @Temporal(TemporalType.DATE)
    private Date beginDate;//报名开始日期

    @Temporal(TemporalType.DATE)
    private Date endDate;//报名结束日期

    private String url;//公告网址

//    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL)
//    private List<Position> positions;
}
