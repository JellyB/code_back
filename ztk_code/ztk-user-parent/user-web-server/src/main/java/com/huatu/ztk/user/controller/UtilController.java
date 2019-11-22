package com.huatu.ztk.user.controller;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.SuccessResponse;
import com.huatu.ztk.user.bean.UcenterBind;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dao.UserDao;
import com.huatu.ztk.user.service.ThirdTaskComponent;
import com.huatu.ztk.user.service.UcenterService;
import com.huatu.ztk.user.service.UserService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by linkang on 8/8/16.
 */
@RestController
public class UtilController {
    private final static Logger logger = LoggerFactory.getLogger(UtilController.class);
    @Resource(name = "coreRedisTemplate")
    private ValueOperations valueOperations;
    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private UcenterService ucenterService;
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserService userService;

    @Autowired
    ThirdTaskComponent thirdTaskComponent;

    /**
     * 删除轮播图缓存
     */
    @RequestMapping(value = "d1")
    public void delete(@RequestParam int category, HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        List<String> l = Arrays.asList(new String[]{"user_banner_11_1_2", "user_banner_11_2_1", "user_banner_11_1_1", "user_banner_11_2_2", "user_banner_01_1_2", "user_banner_01_2_1", "user_banner_01_1_1", "user_banner_01_2_2"});
        //banner
        List<String> params = Lists.newArrayList();
        Integer[] versions = {-1, 1, 0};
        Integer[] appTypes = {0, 1, 2};
        for (Integer version : versions) {
            for (Integer appType : appTypes) {
                String key = new StringBuilder("user_banner_" + version).append(1).append("_").append(category).append("_").append(appType).toString();
                params.add(key);
                String key1 = new StringBuilder("user_advert_").append(2).append("_").append(version).append("_").append(appType).append("_").append(category).append("_").toString();
                String key2 = new StringBuilder("user_advert_").append(3).append("_").append(version).append("_").append(appType).append("_").append(category).append("_").toString();
                String key3 = new StringBuilder("user_advert_").append(5).append("_").append(version).append("_").append(appType).append("_").append(category).append("_").toString();
                params.add(key1);
                params.add(key2);
                params.add(key3);

            }
        }
        //app启动页图片
        logger.info("keys ={}", params);
        valueOperations.getOperations().delete(params);
//        valueOperations.getOperations().delete(l);
        return;
    }


    /**
     * 对比，不同的数据存入redis中
     *
     * @param userDtoList
     */
    public void saveDifferentAccount(List<UserDto> userDtoList, File filename, File notExistsFilename) {
        for (UserDto userDto : userDtoList) {
            if (userDto != null) {
                try {
                    if (userDto.getMobile() != null) {
                        // 手机号查询绑定信息
                        final UcenterBind ucenterBind = ucenterService.findBind(userDto.getMobile());
                        if (ucenterBind != null) {
                            String username = ucenterBind.getUsername();
                            if (!userDto.getName().equals(username)) {//是否一致
                                String id = Joiner.on("$$").join(userDto.getId(), userDto.getName(), ucenterBind.getId(), ucenterBind.getUserid(), username);
                                try {
                                    FileUtils.writeStringToFile(filename, id + "\r\n", true);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        } else {//绑定表中不存在
                            FileUtils.writeStringToFile(notExistsFilename, userDto.getId() + "\r\n", true);
                        }
                    }
                } catch (Exception e) {
                    logger.error("deal data 数据处理出现异常,id:{},msg:", userDto.getId(), e.getMessage());
                }


            }

        }
    }


    /**
     * 砖题库用户表和中心库用户表比较，手机号一致用户名不一致记录下来
     * <p>
     * 分页获取重复数据
     */
    @RequestMapping("deal")
    public Object dealWithAccount(HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        if (true) {
            return null;
        }
        new Thread() {
            @Override
            public void run() {
                int offset = 2000;
                int start = 0;
                int max = (int) userDao.countUsers();
                File filename = new File("/data/logs/user-web-server/diff_user.log");
                File notExistsFilename = new File("/data/logs/user-web-server/not_exists_user.log");
                while (start + offset < max) {
                    List<UserDto> userDtoList = userDao.findUsers(start, offset);
                    saveDifferentAccount(userDtoList, filename, notExistsFilename);
                    start = start + offset;
                }
                if (start < max) { //最后一波数据
                    List<UserDto> userDtoList = userDao.findUsers(start, max - start);
                    saveDifferentAccount(userDtoList, filename, notExistsFilename);
                }
            }
        }.start();

        return new SuccessResponse();
    }


    @RequestMapping("file")
    public Object getDifferentAccountFromFile(HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        if (true) {
            return null;
        }
        List<String> result = null;
        try {
            result = Files.readLines(new File("/data/logs/user-web-server/diff_user.log"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setData(result);
        return successResponse;
    }


    @RequestMapping(value = "/util/countNum")
    public Object countNum(
            @RequestParam(value = "regFrom", required = false, defaultValue = "") String regFrom,
            @RequestParam(value = "beginTime") long beginTime,
            @RequestParam(value = "endTime") long endTime,
            HttpServletRequest httpServletRequest
    ) {
//        String remoteAddr = httpServletRequest.getRemoteAddr();
//        String url = httpServletRequest.getRequestURL().toString();
//        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
//        if(true){
//            return null;
//        }
        if (beginTime < 0 || endTime < 0) {
            return new ArrayList<>();
        }
        return userDao.countNum(regFrom, beginTime, endTime);
    }

    @RequestMapping(value = "/util/register")
    public Object batchRegister(@RequestHeader String phone,
                                @RequestHeader String preName){
        List<UserDto> userDtoList = IntStream.rangeClosed(1, 100).boxed().map(i -> {
            String n = String.valueOf(i);
            String p = String.valueOf(i * 3);
            StringBuilder name = new StringBuilder(preName).append("0000", 0, 3 - n.length()).append(n);
            StringBuilder passwd = new StringBuilder(preName).append("0000", 0, 3 - p.length()).append(p);
            return UserDto.builder().password(passwd.toString())
                    .name(name.toString())
                    .mobile((Long.parseLong(phone) + i) + "")
                    .build();
        }).collect(Collectors.toList());
        Map<Boolean, List<Object>> booleanListMap = userService.registerForPHP(userDtoList, "127.0.0.1", "");
        System.out.println("JsonUtil.toJson(booleanListMap) = " + JsonUtil.toJson(booleanListMap));
        return booleanListMap;
    }

    @RequestMapping(value = "/util/sync")
    public Object syncUserData(@RequestParam String ids) {
        ArrayList<Object> list = Lists.newArrayList();
        Optional.of(ids)
                .map(userDao::findByIds)
                .ifPresent(
                        i -> i.forEach(userDto -> {
                            thirdTaskComponent.syncUserData(userDto.getEmail(), userDto.getMobile(), userDto.getName());
                            list.add(userDto.getName());
                        })
                );
        return list;
    }
}
