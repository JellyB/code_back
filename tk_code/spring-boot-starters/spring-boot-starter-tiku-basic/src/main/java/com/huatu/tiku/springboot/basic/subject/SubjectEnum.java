package com.huatu.tiku.springboot.basic.subject;

/**
 * 一般不建议用
 * @author hanchao
 * @date 2017/10/6 15:40
 */
public enum SubjectEnum {
    GONGWUYUAN(1,"公务员",1000),
    SHIYEDANWEI(3,"事业单位",1001),
    JIAOSHI(200100045,"教师",1003),
    JINRONG(200100002,"金融",1005),
    YILIAO(200100000,"医疗",1004),
    GONGJIANFA(200100047,"公检法",1002),
    QITA(200100046,"其他",1006),
    LINXUAN(41,"遴选",1008),
    JUANZHUAN(42,"军转",1009),
    GUOJIADIANWANG(43,"国家电网",1010);

    private final int code;//最顶级的id
    private final String meaning;//这里定义必须和网校一致，因为网校需要name来获取
    private final int categoryid;//网校的默认categoryid，获取直播录播时候使用
    SubjectEnum(int code,String meaning,int categoryid){
        this.code = code;
        this.meaning = meaning;
        this.categoryid = categoryid;
    }

    public int code() {
        return code;
    }

    public String meaning() {
        return meaning;
    }

    public int categoryid() {
        return categoryid;
    }

    public static SubjectEnum valueOf(int code){
        for (SubjectEnum subjectEnum : SubjectEnum.values()) {
            if(subjectEnum.code() == code){
                return subjectEnum;
            }
        }
        return SubjectEnum.GONGWUYUAN;
    }
}
