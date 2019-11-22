package com.huatu.tiku.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lijun on 2018/5/31
 */
public enum AdvertEnum {
    ;

    /**
     * APP 类型
     */
    @AllArgsConstructor
    @Getter
    public enum AppType {
        ALL(0, "华图在线、砖题库共用"),
        ZTK(1, "砖题库"),
        HTZX(2, "华图在线");
        private int code;
        private String name;
    }

    /**
     * 广告类型
     */
    @AllArgsConstructor
    @Getter
    public enum Type {
        SYLBT(1, "首页轮播图"),
        QDYTP(2, "启动页图片"),
        SYTCT(3, "首页弹出图");
        /*SYGG(5, "首页公告");*/
        private int code;
        private String name;

        //是否需要地址信息
        public static Boolean needPosition(int code) {
            // type = 4 才需要，新版本中无type=4 情况
            return false;
        }

        //是否需要图片高宽限定（暂时只有弹出图需要）
        public static Boolean needHW(int code) {
            return code == SYTCT.getCode();
        }

        //是否判断广告时间重叠问题
        public static Boolean needJudgmentTime(int code) {
            return false;
            //return code == SYTCT.getCode();
        }

        public static List<HashMap<String, Object>> toList() {
            List<HashMap<String, Object>> collect = Stream.of(AdvertEnum.Type.values())
                    .map(type -> {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("name", type.getName());
                        hashMap.put("code", type.getCode());
                        return hashMap;
                    })
                    .collect(Collectors.toList());
            return collect;
        }

        public static String getValueByCode(int code) {
            Optional<Type> any = Stream.of(Type.values()).filter(type -> type.getCode() == code).findAny();
            if (any.isPresent()) {
                return any.get().getName();
            }
            return "";
        }
    }

    /**
     * 页面类型
     */
    @AllArgsConstructor
    @Getter
    public enum Target {
        H5HDYM("ztk://h5/active", "H5活动页面"),
        //H5MKYM("ztk://h5/simulate", "H5模考页面"),
        KCYM("ztk://course/detail", "课程页面"),
        //JJCYM("ztk://arena/home", "竞技场页面"),
        ZTYM("ztk://pastPaper/home", "真题列表"),
        GFLB("ztk://estimatePaper/home", "估分列表"),
        MKLB("ztk://simulatePaper/home", "模考列表"),
        ZTZT("ztk://pastPaper", "真题做题页面"),
        MKDS("ztk://match/detail", "模考大赛首页"),
        SLSY("ztk://essay/home", "申论首页"),
        SLTT("ztk://essay/paper", "申论套题"),//{"areaId":123}
        SLYL("ztk://essay/argument", "申论议论文"),
        SLMK("ztk://match/essay", "申论模考大赛首页"),
        ZBLB("ztk://live/home", "课程列表"),
        //LBLB("ztk://recording/home", "录播列表"),
        KCHJ("ztk://course/collection", "课程合集页面"),
        MSKC("ztk://course/seckill", "秒杀课程"),
        SMK("ztk://small/estimate", "小模考"),
        JPWK("ztk://exquisite/small/course", "精品微课"),
        BKJH("ztk://exam/articles", "备考精华"),
        GFZT("ztk://estimatePaper","估分做题页面"),
        XFZX("ztk://noticeCenter","消息中心"),
        ZTSF("ztk://pastPaper/province","真题试卷列表"),
        JPWKLB("ztk://exquisite/small/course","精品微课列表"),
        ;


        private String url;
        private String name;

        /**
         * 根据科目获取页面类型
         *
         * @param code 科目code
         * @return
         */
        public static List<HashMap<String, Object>> getTargetByCategory(int code) {
            if (!Category.isIllegal(code)) {
                //code 不合法
                return new ArrayList<>();
            }
            List<Target> collect = Stream.of(Target.values()).collect(Collectors.toList());
//            if (code != Category.GWY.getCode()) {
//                //没有竞技赛场的节点
//                collect = collect.stream().filter(target -> !target.getUrl().equals(JJCYM.getUrl()))
//                        .collect(Collectors.toList());
//            }
            Category category = Category.create(code);
            if (!category.isMatchFlag()) {
                //没有模考大赛的节点
                collect = collect.stream().filter(target -> !target.getUrl().equals(MKDS.getUrl()))
                        .collect(Collectors.toList());
            }
            //公务下才存在申论节点
            if (code != Category.GWY.getCode()) {
                //没有申论首页的节点
                collect = collect.stream().filter(target -> (!target.getUrl().equals(SLSY.getUrl())
                        && !target.getUrl().equals(SLTT.getUrl())
                        && !target.getUrl().equals(SLYL.getUrl())
                        && !target.getUrl().equals(SLMK.getUrl())
                ))

                        .collect(Collectors.toList());
                //公务员下才有小模考,别的科目移除小模考
                collect.removeIf(target -> target.getUrl().equals(SMK.getUrl()));
            }

            List<HashMap<String, Object>> mapList = collect.stream().map(target -> {
                HashMap<String, Object> map = new HashMap<>();
                map.put("url", target.getUrl());
                map.put("name", target.getName());
                return map;
            }).collect(Collectors.toList());
            return mapList;
        }

        public static String getValueByCode(String code) {
            Optional<Target> any = Stream.of(Target.values()).filter(target -> target.getUrl().equals(code)).findAny();
            if (any.isPresent()) {
                return any.get().getName();
            }
            return "";
        }
    }

    /**
     * m 站 页面类型
     */
    @AllArgsConstructor
    @Getter
    public enum MTarget {
        MKCYM("ztk://course/detail", "课程页面"),
        MZBLB("ztk://live/home", "课程列表"),
        MKCHJ("ztk://course/collection", "课程合集列表"),
        MZXLB("ztk://news/list", "资讯列表"),
        MZXXQY("ztk://news/detail", "资讯详情页"),
        MBKJH("ztk://exam/articles", "备考精华"),
        MBKJHXQY("ztk://exam/articles/detail", "备考精华详情页"),
        MH5HDYM("ztk://h5/active", "H5活动页面"),
        MMKDS("ztk://match/detail", "模考大赛首页"),
        MMSKC("ztk://course/seckill", "秒杀页面");

        private String url;
        private String name;

        public static List<HashMap<String, Object>> getTargetByCategory() {
            List<MTarget> collect = Stream.of(MTarget.values()).collect(Collectors.toList());
            List<HashMap<String, Object>> mapList = collect.stream().map(target -> {
                HashMap<String, Object> map = new HashMap<>();
                map.put("url", target.getUrl());
                map.put("name", target.getName());
                return map;
            }).collect(Collectors.toList());
            return mapList;
        }
    }

    /**
     * 科目
     */
    @AllArgsConstructor
    @Getter
    public enum Category {

        GWY(1, "公务员", true),
        SYDW(3, "事业单位", true),
        JS(200100045, "教师招聘", true),
        JSZGZXX(200100048, "教师资格证-小学", true),
        JSZGZZX(200100053, "教师资格证-中学", true),
        ZJ(200100047, "招警", false),
        LX(41, "遴选", false),
        JZ(42, "军转", false),
        GJDW(43, "国家电网", false),
        YL(200100000, "医疗", false),
        JR(200100002, "金融", false),
        SZYF(200100064, "三支一扶", false),
        JDWZ(200100060, "军队文职", false),
        KY(100100633, "考研", false),
        CK(200100058, "财会", false),
        JZHU(200100059, "建筑", false),
        QT(200100046, "其他", false);

        private int code;
        private String name;
        private boolean matchFlag;

        /**
         * 判断是否合法
         *
         * @param code
         * @return 合法 返回 true
         */
        public static boolean isIllegal(int code) {
            return Stream.of(Category.values()).anyMatch(category -> category.getCode() == code);
        }

        public static List<HashMap<String, Object>> toList() {
            List<HashMap<String, Object>> mapList = Stream.of(Category.values())
                    .map(category -> {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("code", category.getCode());
                        map.put("name", category.getName());
                        return map;
                    })
                    .collect(Collectors.toList());
            return mapList;
        }

        public static String getValueByCode(int code) {
            Optional<Category> any = Stream.of(Category.values()).filter(category -> category.getCode() == code).findAny();
            if (any.isPresent()) {
                return any.get().getName();
            }
            return "";
        }

        public static Category create(int code) {
            Optional<Category> any = Stream.of(Category.values()).filter(category -> category.getCode() == code).findAny();
            if (any.isPresent()) {
                return any.get();
            }
            return Category.QT;
        }
    }

    /**
     * 状态
     */
    @AllArgsConstructor
    @Getter
    public enum Status {
        ENABLED(1),
        CLOSED(0);
        private int code;

        public static boolean isEnable(int code) {
            return code == ENABLED.getCode();
        }

    }

    /**
     * 显示平台
     */
    @AllArgsConstructor
    @Getter
    public enum PlatForm{
        APP(1, "APP"),
        M(2, "M站"),
        APP_M(100, "APP 和 M 站");

        private int code;
        private String text;
        public static PlatForm convert (String platForm){
            if(null == platForm || "".equals(platForm)){
                return APP;
            }
            if(platForm.contains(",")){
                return APP_M;
            }else if(APP.getCode() == Integer.parseInt(platForm)){
                return APP;
            }else if(M.getCode() == Integer.parseInt(platForm)){
                return M;
            }else{
                return null;
            }
        }
    }
}
