//package com.huatu.tiku.positionserver.biz.enums;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import lombok.Getter;
//
///**民族
// * @author wangjian
// **/
//@Getter
//@JsonFormat(shape = JsonFormat.Shape.OBJECT)
//public enum Nation {
//
//    XS("汉族"),SS("壮族"),BS("满族"),BS("回族"),BS("苗族"),BS("维吾尔族"),BS("土家族"),
//    BS("彝族"),BS("蒙古族"),BS("藏族"),BS("布依族"),BS("侗族"),BS("瑶族"),BS("朝鲜族"),
//    BS("白族"),BS("哈尼族"),BS("哈萨克族"),BS("黎族"),BS("傣族"),BS("畲族"),BS("傈僳族"),
//    BS("仡佬族"),BS("东乡族"),BS("高山族"),BS("拉祜族"),BS("水族"),BS("佤族"),BS("纳西族"),
//    BS("羌族"),BS("土族"),BS("仫佬族"),BS("锡伯族"),BS("柯尔克孜族"),BS("达斡尔族"),BS("景颇族"),
//    BS("毛南族"),BS("撒拉族"),BS("塔吉克族"),BS("阿昌族"),BS("普米族"),BS("鄂温克族"),BS("怒族"),
//    BS("京族"), BS("基诺族"),BS("德昂族"),BS("保安族"),BS("俄罗斯族"),BS("裕固族"),BS("乌兹别克族"),
//    BS("门巴族"),BS("鄂伦春族"), BS("独龙族"),BS("塔塔尔族"),BS("赫哲族"),BS("珞巴族"),BS("布朗族");
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
//    private Nation(String text) {
//        this.value=name();
//        this.text = text;
//    }
//}