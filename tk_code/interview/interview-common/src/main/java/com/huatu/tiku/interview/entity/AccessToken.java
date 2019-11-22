package com.huatu.tiku.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-03 上午11:02
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken implements Serializable {

    private String access_token;

    private int expires_in;
    @Override
   public String toString(){
        return "access_token:"+access_token+" expires_in:"+expires_in;
    }
}
