package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**工作经验
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum  Exp {

    BX("无限制"),
    ZERO("无工作经验"),
    ONE("一年"),
    TWO("二年"),
    THREE("三年"),
    FOUR("四年"),
    FIVE("五年以上");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    private Exp(String text) {
        this.value=name();
        this.text = text;
    }

    public static Exp findByName(String name){
        for(Exp exp: Exp.values()){
            if(exp.text.equals(name)){
                return exp;
            }
        }
        return null;
    }
}