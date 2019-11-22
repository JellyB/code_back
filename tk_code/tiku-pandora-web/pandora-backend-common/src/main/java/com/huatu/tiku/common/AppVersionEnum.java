package com.huatu.tiku.common;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-09-06 下午5:13
 **/
public enum AppVersionEnum {
    ;


    /**
     * 终端类型枚举
     */
    public enum  TerminalTypeEnum {
        ANDROID(1, "Android"),
        IOS(2, "IOS");

        private Integer value;

        private String title;


        TerminalTypeEnum(Integer value, String title) {
            this.value = value;
            this.title = title;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public static String getTitle(Integer value){
            for(TerminalTypeEnum e: TerminalTypeEnum.values()){
                if(e.getValue() == value){
                    return e.getTitle();
                }
            }
            return "";
        }
    }


    /**
     * 灰度发布枚举
     */
    public enum ReleaseTypeEnum{
        ALL(1, "全部用户"),
        WHILT_LIST(2, "白名单"),
        MODE(3, "取模随机");

        private Integer value;

        private String title;

        ReleaseTypeEnum(Integer value, String title) {
            this.value = value;
            this.title = title;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public static String getTitle(Integer value){
            for(ReleaseTypeEnum e: ReleaseTypeEnum.values()){
                if(e.getValue() == value){
                    return e.getTitle();
                }
            }
            return "";
        }
    }

    /**
     * 升级方法枚举
     */
    public enum UpdateTypeEnum {

        TOAST(1, "提示升级"),
        FORCE(2, "强制升级"),
        PATCH(3, "补丁包");

        private Integer value;

        private String title;

        UpdateTypeEnum(Integer value, String title) {
            this.value = value;
            this.title = title;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public static String getTitle(Integer value){
            for(UpdateTypeEnum e: UpdateTypeEnum.values()){
                if(e.getValue() == value){
                    return e.getTitle();
                }
            }
            return "";
        }
    }

    /**
     * 版本管理 app 名称
     */
    public enum AppNameEnum{
        HUATU_ONLINE(1, "华图在线"),
        INTERVIEW_COOL(2, "面酷"),
    	HUATU_TEACHER(3, "华图教师");
        private Integer value;

        private String title;

        AppNameEnum(Integer value, String title) {
            this.value = value;
            this.title = title;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public static String getTitle(Integer value){
            for(AppNameEnum e: AppNameEnum.values()){
                if(e.getValue() == value){
                    return e.getTitle();
                }
            }
            return "";
        }

    }

    public enum UpdateChannelEnum {

        ALL(1, "全部"),
        UPGRADE(2,"升级包"),
        BAIDU(3,"百度助手"),
        HUAWEI(4,"华为"),
        VIVO(5,"vivo"),
        OPPO(6,"oppo"),
        SAMSUNG(7,"三星"),
        UC(8,"阿里"),
        QQ(9,"应用宝"),
        XIAOMI(10,"小米"),
        MEIZU(11,"魅族"),
        SLL(12,"360"),
        SMART(13,"锤子"),
        SG(14,"搜狗手助"),
        QIONEE(15,"金立"),
        LENOVO(16,"联想"),
        MUMAYI(17,"木蚂蚁"),
        WEB(18,"其他"),
        ANZHI(19,"安智");

        private Integer value;

        private String title;

        UpdateChannelEnum(Integer value, String title) {
            this.value = value;
            this.title = title;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public static String getTitle(Integer value){
            for(UpdateChannelEnum e: UpdateChannelEnum.values()){
                if(e.getValue() == value){
                    return e.getTitle();
                }
            }
            return "";
        }
    }

    public enum UpdateMode{

        PERCENT10(1, "10%"),
        PERCENT20(2, "20%"),
        PERCENT25(3, "25%"),
        PERCENT50(4, "50%");

        private Integer value;

        private String title;

        UpdateMode(Integer value, String title) {
            this.value = value;
            this.title = title;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public static String getTitle(Integer value){
            for(UpdateMode e: UpdateMode.values()){
                if(e.getValue() == value){
                    return e.getTitle();
                }
            }
            return "";
        }
    }
}
