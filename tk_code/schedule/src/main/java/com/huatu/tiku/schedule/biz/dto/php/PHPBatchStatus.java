package com.huatu.tiku.schedule.biz.dto.php;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**接收PHP批量修改审核状态
 * @author wangjian
 **/
@Data
public class PHPBatchStatus implements Serializable {

    private static final long serialVersionUID = 3399911386769875396L;

    private List<Long> pids;

    private Integer status;
}
