package top.jbzm.index.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 百家云返回对象
 * @author: wangjian
 * @create: 2018-03-23 11:11
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaiJiaYunResult {

    private String code;
    private Map<String,Object> data;
    private String msg;
    private String ts;

}
