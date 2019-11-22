package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/13 13:55
 * 套题练习
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="t_paper_practice")
public class PaperPractice extends BaseEntity  implements Serializable {
    //---------答题日期---------
    private String answerDate;
    // -----姓名---------
    private String name;
    //---------openId---------
    private String openId;
//    //----------练习内容(1自我认知  2工作实务   3策划组织  4综合分析   5材料题与特殊题型   6套题演练)-------------
//    private Integer practiceContent;



    //------------举止仪态-------------
    private Double behavior;
    //------------语言表达-------------
    private Double languageExpression;
    //------------是否精准扣题-------------
    private Double focusTopic;
    //------------是否条理清晰-------------
    private Double isOrganized;
    //------------是否言之有物-------------
    private Double haveSubstance;



    //------------其他评价-------------
    private String elseRemark;
}
