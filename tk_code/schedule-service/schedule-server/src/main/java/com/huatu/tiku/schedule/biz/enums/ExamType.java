package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

/**
 * 考试类型
 * 
 * @author Geek-S
 *
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExamType {

	GWY(1, "公务员", true),
	SYDW(8, "事业单位", true),
	JZG(9, "军转干", true),
	CG(10, "村官", false),
	JSZP(12, "教师招聘", true),
	JSZGZ(19, "教师资格证", true),
	LX(20, "遴选", true),
	SZYF(26, "三支一扶", true),
	YLWS(31, "医疗卫生", true),
	YLZGZ(33, "医疗资格证", true),
	GJDW(30, "国家电网", true),
	MS(700, "面试", true),

	SG(3, "社工", false),
	ZJ(4, "招警", false),
	FJ(5,"法检",false),
	JCZFJG(6,"基层政法机关",false),
	XTS(7, "选调生", false),
	SZGWY(11,"乡镇公务员",false),
	BGY(13,"报关员",false),
	BJY(14,"报检员",false),
	SF(15,"司法",false),
	KY(16,"考研",false),
	KJ(17,"会计",false),
	LZS(18,"路转税",false),
	TGJS(21, "特岗教师", false),
	NSH(22, "农商行/农信社", false),
	YZSZJY(23,"优职素质教育",false),
	ZYK(24,"专业课",false),
	YHZP(25, "银行招聘", false),
	TY(27,"通用",false),
	JDWZ(28,"军队文职",true),
	GABF(29,"公安边防",false),
	JZS(32,"建造师",false),
	XLZXS(34,"心理咨询师",false),
	RLZYS(35,"人力资源师",false),
	GQ(36,"国企",false),
	ZFGJ(37, "政法干警", false),
	JRZGL(38, "金融资格类", false),
	GX(39, "公选", false),
	BDQT(40, "部队其他", false),
	QT(41, "其他", false),
	ALL(1000,"全部",false);


	/**
	 * 唯一标识
	 */
	private Integer id;

	/**
	 * 值
	 */
	private String value;

	/**
	 * 是否可用
	 */
	private Boolean status;

	/**
	 * 显示字体
	 */
	private String text;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            唯一标识
	 * @param text
	 *            显示文字
	 * @param status
	 *            状态
	 */
	private ExamType(Integer id, String text, Boolean status) {
		this.id = id;
		this.value = name();
		this.text = text;
		this.status = status;
	}

	/**
	 * 根据ID查询枚举
	 * 
	 * @param id
	 *            ID
	 * @return 考试类型
	 */
	public static ExamType findById(Integer id) {
		ExamType examType = null;

		for (ExamType temp : ExamType.values()) {
			if (temp.getId().equals(id)) {
				examType = temp;
				break;
			}
		}

		return examType;
	}
}
