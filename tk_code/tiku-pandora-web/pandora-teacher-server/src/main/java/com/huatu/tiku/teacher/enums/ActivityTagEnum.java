package com.huatu.tiku.teacher.enums;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @创建时间 2018/8/13
 * @描述 因为现在库中未存储每个科目对应的标签，并且涉及到年份变化，因此现在是写枚举处理
 */
public enum ActivityTagEnum {
    ;

    @Slf4j
    @AllArgsConstructor
    public enum Subject

    {
        /**
         * 纠正：此处的key(1,14,3,200100045 存放的是考试类别;比如招警类别,下面又有行测，公安专业，申论等科目，
         * 此处存放的是招警类别的ID)
         */
        //公务员对应的标签
        CivilServantSubject(1, Lists.newArrayList(
                TagEnum.XINGCESHENGKAO2020,
                TagEnum.XINGCEGUOKAO2020,
                TagEnum.XINGCEGUOKAO2019,
                TagEnum.XINGCESHENGKAO2019,
                TagEnum.XINGCEGUOKAO2018,
                TagEnum.XINGCESHENGKAO2018)),
        //公务员申论对应的标签
        ShenlunSubject(14, Lists.newArrayList(
                TagEnum.SHENLUN2020,
                TagEnum.SHENLUN2018,
                TagEnum.SHENLUN2019
        )),
        //事业单位的标签
        PublicInstitutionSubject(2, Lists.newArrayList(
                TagEnum.SYDWGONGJI2019,
                TagEnum.SYDWGONGJI2018)),
        //教师招聘-教育综合知识
        TEACHER_ZP_KYZHZS(100100262, Lists.newArrayList(
                TagEnum.JIAOZHAO2019,
                TagEnum.TEGANG2019
        )),
        //招警机考
        POLICE_SUBJECT(100100173, Lists.newArrayList(
                TagEnum.ZHAOJINGJIKAO2020,
                TagEnum.ZHAOJINGJIKAO2018
        )),
        //公安专业科目
        GAZY_SUBJECT(100100175, Lists.newArrayList(
                TagEnum.QUANZHENMOKAO
        )),
        //教师资格证（小学）-综素
        TEACHER_ZGZ_XX_ZHSZ(200100049, Lists.newArrayList(TagEnum.TEACHER_ZGZ_XX_ZHSZ)),
        //教师资格证（小学）-教育知识与能力
        TEACHER_ZGZ_XX_JZ(200100051, Lists.newArrayList(TagEnum.TEACHER_ZGZ_XX_JZ)),
        //教师资格证（中学）-综素
        TEACHER_ZGZ_ZX_ZHSZ(200100050, Lists.newArrayList(TagEnum.TEACHER_ZGZ_ZX_ZHSZ)),
        //教师资格证（中学）-教育知识与能力
        TEACHER_ZGZ_ZX_JZ(200100052, Lists.newArrayList(TagEnum.TEACHER_ZGZ_ZX_JZ)),
        //事业单位职测ABC非联考
        SYDW_ZC_A(200100054, Lists.newArrayList(TagEnum.SYDW_ZC_A)),
        SYDW_ZC_B(200100055, Lists.newArrayList(TagEnum.SYDW_ZC_B)),
        SYDW_ZC_C(200100056, Lists.newArrayList(TagEnum.SYDW_ZC_C)),
        SYDW_ZC_FLK(200100057, Lists.newArrayList(TagEnum.SYDW_ZC_FLK)),
        //军队文职
        JDWZ(200100063, Lists.newArrayList(TagEnum.JDWZ)),
        //金融
        JIN_RONG(420, Lists.newArrayList(TagEnum.JINRONG2019)),;

        private Integer key;
        private List<TagEnum> object;


        public Integer getKey() {
            return this.key;
        }

        public List<TagEnum> getValues() {
            return this.object;
        }

        /**
         * 通过科目和标签code,确定标签枚举(数据迁移到pandora时使用)
         *
         * @param subjectId
         * @param code      纠正： 此方法只能够查询科目类别,不能用于查询具体的科目。比如：只能查询公务员,事业单位等
         * @return
         */
        public static TagEnum getTag(int subjectId, int code) throws BizException {
            for (Subject subject : Subject.values()) {
                if (subject.getKey() == subjectId) {
                    for (TagEnum tagEnum : subject.getValues()) {
                        if (tagEnum.getCode() == code) {
                            return tagEnum;
                        }
                    }
                    break;
                }
            }
            log.error("科目{}下，没有标签ID{}", subjectId, code);
            throw new BizException(ErrorResult.create(10101211, "科目" + subjectId + "下，没有标签ID" + code));
        }
    }

    @Slf4j
    @AllArgsConstructor
    @Getter
    public enum TagEnum {
        /**
         * 2018年标签
         */
        //行测
        XINGCEGUOKAO2018(1, "2018年国考", 3, false),
        XINGCESHENGKAO2018(2, "2018年省考", 4, false),
        //公基
        SYDWGONGJI2018(3, "2018年公基", 1, true),
        //招教
        SHENGTONGKAO2018(4, "教师资格证", 1, false),
        JIAOZHAO2019(5, "2019教招教综", 2, true),
        TEGANG2019(6, "2019特岗教综", 3, true),
        SYDWLIANKAOD2018(7, "事业单位D类", 4, false),
        //去掉其他
        //OTHER2018(8, "其他", 5),
        //招警
        ZHAOJINGJIKAO2018(9, "2018年招警机考", 1, true),
        /**
         * 申论标签
         */
        SHENLUN2018(13, "2018申论模考", 3, false),
        /**
         * 2019年标签
         */
        XINGCEGUOKAO2019(10, "2019国考行测", 1, true),
        XINGCESHENGKAO2019(11, "2019省考行测", 2, true),

        SHENLUN2019(14, "2019申论模考", 3, true),

        SYDWGONGJI2019(12, "2019年公基", 12, true),

        TEACHER_ZGZ_XX_ZHSZ(15, "2019综素-小学", 15, true),

        TEACHER_ZGZ_XX_JZ(16, "2019教知-小学", 16, true),

        TEACHER_ZGZ_ZX_ZHSZ(17, "2019综素-中学", 17, true),

        TEACHER_ZGZ_ZX_JZ(18, "2019教知-中学", 18, true),
        //事业单位职测,
        SYDW_ZC_A(19, "2019年职测A", 19, true),
        SYDW_ZC_B(20, "2019年职测B", 20, true),
        SYDW_ZC_C(21, "2019年职测C", 21, true),
        SYDW_ZC_FLK(22, "2019年职测非联考", 22, true),
        //军队文职
        JDWZ(23, "2019年军队文职", 23, true),
        /**
         * 2020年行测标签
         */
        XINGCEGUOKAO2020(24, "2020国考行测", 24, true),
        XINGCESHENGKAO2020(25, "2020省考行测", 25, true),
        /**
         * 申论2020标签
         */
        SHENLUN2020(26, "2020申论模考", 26, true),
        /**
         * 公安主页科目模考标签
         */
        QUANZHENMOKAO(27, "全真模考", 27, true),
        ZHAOJINGJIKAO2020(28, "2020年招警机考", 28, true),
        /**
         * 金融
         */
        JINRONG2019(30, "2019金融", 30, true),;

        private Integer tagId;
        private String tagName;
        private Integer code;
        private boolean work;

        public boolean equals(TagEnum tagEnum) {
            return this.tagId == tagEnum.getTagId();
        }

        public static TagEnum create(int tagId) throws BizException {
            TagEnum[] values = TagEnum.values();
            for (TagEnum value : values) {
                if (value.getTagId() == tagId) {
                    return value;
                }
            }
            log.error("无效的标签ID={}", tagId);
            throw new BizException(ErrorResult.create(1000099, "无效的标签ID" + tagId));
        }


    }

}
