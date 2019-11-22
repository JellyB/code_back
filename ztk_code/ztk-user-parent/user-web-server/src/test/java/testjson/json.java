package testjson;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huatu.ztk.user.bean.UserDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @Author jbzm
 * @Date Create on 2018/1/7 13:42
 */
public class json {

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void test() {
        List<UserDto> userDtoList = Lists.newLinkedList();
        for (int i = 0; i < 10; i++) {
            UserDto userDto = new UserDto();
            userDto.setId(233906452 + i);
            userDtoList.add(userDto);
        }
        String str= JSON.toJSONString(userDtoList);
        String url="http://123.103.86.52/u/essay/statistics/user";
        Object obj=userDtoList;
        Object userDtoList1= restTemplate.postForObject(url, userDtoList, Object.class);
    }
}
