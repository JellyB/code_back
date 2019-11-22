package top.jbzm.index.entity.videoEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 课程
 * @author: wangjian
 * @create: 2018-04-03 09:51
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name="course")
public class Course implements Serializable {

    private static final long serialVersionUID = 4691161852440963306L;

    @Id
    private String rid;
    private String IsSuit;//1为套餐课,0为普通课
    private String NetClassCategoryId;//	分类id 小分类 言语理解
    private String Title;
    private String status;	//课程状态,0-未上线,1-上线,2-下线
    private Integer cateId;//分类id 大分类 公务员
}
