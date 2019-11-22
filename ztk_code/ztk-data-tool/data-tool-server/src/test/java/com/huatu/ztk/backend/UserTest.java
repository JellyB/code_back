package com.huatu.ztk.backend;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.user.dao.UserHuatuDao;
import com.huatu.ztk.user.bean.UserDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\7\4 0004.
 */
public class UserTest extends BaseTestW {
    @Autowired
    UserHuatuDao userHuatuDao;
    private static final int size = 1000;
    @Test
    public void test(){
        File file = new File("C:\\Users\\huangqp\\Desktop\\asdas.txt");
        try {
            String content = FileUtils.readFileToString(file);
            String[] split = content.split(",");
            List<String> list = Lists.newArrayList();
            for (String s : split) {
                list.add(s);
            }
            int total = split.length;
            int index = 0;
            int count = 0;
            List<String> result = Lists.newArrayList();
            StringBuilder sb = new StringBuilder();
            while(true){
                int end = index+size>total?total:index+size;
                if(index+1==end){
                    break;
                }
                List<String> temp = list.subList(index,end);
                if(CollectionUtils.isEmpty(temp)){
                    break;
                }
                List<UserDto> users = userHuatuDao.findByMobiles(temp);
                if(CollectionUtils.isEmpty(users)){
                    continue;
                }
                result.addAll(users.stream().map(i->i.getMobile()).collect(Collectors.toList()));
                System.out.println("进度："+end+"/"+total);
                count += users.size();
                index = end;
            }
            System.out.println("实际手机号："+total+"，注册手机号"+count);
            result.stream().forEach(i->sb.append(i).append(","));
            sb.deleteCharAt(sb.length()-1);
            FileUtils.write(new File("C:\\Users\\huangqp\\Desktop\\asda_back.txt"),sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}

