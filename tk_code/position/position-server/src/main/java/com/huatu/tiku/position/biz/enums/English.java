//package com.huatu.tiku.position.biz.enums;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import lombok.Getter;
//
///**最高英语水平
// * @author wangjian
// **/
//@Getter
//@JsonFormat(shape = JsonFormat.Shape.OBJECT)
//public enum English {
//
//    ES("英语四级"),
//    EL("英语六级"),
//    ZS("专业四级"),
//    ZB("专业八级"),
//    TF("托福"),
//    YS("雅思"),
//    TY("托业");
//
//    /**
//     * 值
//     */
//    private String value;
//
//    /**
//     * 显示字体
//     */
//    private String text;
//
//    private English(String text) {
//        this.value=name();
//        this.text = text;
//    }
//}