package com.huatu.ztk.pc.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * 申论抽象类
 * Created by shaojieyue
 * Created time 2016-09-26 15:12
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public abstract class ShenlunQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;//试题id
    private String stem;//题干 stem
    private int type;//试题类型 type_id
}
