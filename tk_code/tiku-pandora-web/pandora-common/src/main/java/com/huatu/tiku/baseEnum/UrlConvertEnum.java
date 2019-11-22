package com.huatu.tiku.baseEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by huangqingpeng on 2018/9/27.
 */
@AllArgsConstructor
@Getter
public enum  UrlConvertEnum {
    KONGGE(" ","%20",1,"空格"),
    SHUANGYIHANG("\"","%22",0,"双引号"),
    JIANGHAO("#","%23",0,"井号"),
    BENFENHAO("%","%25",0,"百分号"),
    AND("&","%26",0,"交集"),
    ZUOKUOKAOE("(","%28",1,"左括号（英）"),
    ZUOKUOHAOZ("（","%28",1,"左括号（中）"),    //中文括号以英文做处理，避免前端输入失误问题
    YOUKUOHAOZ("）","%29",1,"右括号（中）"), //中文括号以英文做处理，避免前端输入失误问题
    YOUKUOHAOE(")","%29",1,"右括号（英）"),
    JIAHAO("+","%2B",0,"加号"),
    DOUHAO(",","%2C",0,"逗号"),
    XIEXIAN("/","%2F",0,"斜线"),
    MAOHAO(":","%3A",0,"冒号"),
    FENHAO(";","%3B",0,"分号"),
    XIAOYU("<","%3C",0,"小于号"),
    DENGYU("=","%3D",0,"等号"),
    DAYU(">","%3E",0,"大于号"),
    WENHAO("?","%3F",0,"问号"),
    EMAIL("@","%40",0,"mail"),
    ZHUANYIFU("\\","%5C",0,"转义字符"),
    FENGEXIAN("|","%7C",0,"空格"),
    ;

    private String preKey;
    private String value;
    private int flag;
    private String preName;


}
