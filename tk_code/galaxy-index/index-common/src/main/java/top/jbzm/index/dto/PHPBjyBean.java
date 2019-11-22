package top.jbzm.index.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: wangjian
 * @create: 2018-03-28 15:37
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PHPBjyBean {
    private int code;
    private String msg;
    private Object data;
}
