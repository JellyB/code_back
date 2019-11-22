package interview.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: xuhuiqiang
 * Time: 2018-07-05  11:18 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class HownetPrimitive {
    private long id;
    private String englishItem;//英文
    private String chineseItem;//中文
    private long parentId;
}
