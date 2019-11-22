package interview.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Author: xuhuiqiang
 * Time: 2017-11-02  22:02 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class SynonymStop {
    private long id;
    private Set<String> words;
    private String item;
    private int groupId;
}
