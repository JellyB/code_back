package com.huatu.ztk.backend.metas.controller;

<<<<<<< HEAD
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.metas.service.PracticeMetaService;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.user.dao.UserHuatuDao;
import com.huatu.ztk.backend.util.ExcelManageUtil;
import com.huatu.ztk.backend.util.FunFileUtils;
import com.huatu.ztk.backend.util.MailUtil;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.user.bean.UserDto;
=======
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.metas.service.PracticeMetaService;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.Paper;
>>>>>>> master
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

<<<<<<< HEAD
import java.io.IOException;
import java.util.*;
=======
import java.util.List;
import java.util.Map;
>>>>>>> master

/**
 * 查询某张试卷的统计信息
 * Created by lijun on 2018/9/13
 */
@CrossOrigin(origins = "*", maxAge = 36000)
@RestController
@RequestMapping(value = "/practiceInfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PracticeInfoController {

    private final static Logger logger = LoggerFactory.getLogger(PracticeMetaController.class);

    @Autowired
    private PracticeMetaService practiceMetaService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    PaperDao paperDao;

<<<<<<< HEAD
    @Autowired
    private UserHuatuDao userHuatuDao;

=======
>>>>>>> master
    /**
     * 统计一次试卷的信息
     */
    @RequestMapping(value = "{paperId}", method = RequestMethod.GET)
    public Object info(@PathVariable int paperId) {
        Paper paper = paperDao.findById(paperId);
<<<<<<< HEAD
        if (null == paper) {
=======
        if (null == paper){
>>>>>>> master
            return SuccessMessage.create("试卷信息不存在");
        }
        //查询所有的答题卡
        Map mapData = Maps.newHashMap();
        Map map = practiceMetaService.getCountByPosition(paperId);
        mapData.putAll(map);
        Long submitCount = practiceMetaService.getCountSubmit(paperId);
<<<<<<< HEAD
        mapData.put("submitCount", submitCount);
        Map map1 = practiceMetaService.getMaxScoreInfo(paperId);
        mapData.putAll(map1);
        double average = practiceMetaService.getAverage(paperId);
        mapData.put("average", average);
        Line line = practiceMetaService.getLine(paperId);
        mapData.put("line", line);
        List<Map> list = practiceMetaService.getWrongQuestionMeta(paperId);
        mapData.put("wrongList", list);
        practiceMetaService.parseStatements(mapData, paperId);

        return mapData;
    }

    @RequestMapping(value = "userInfo/{paperId}", method = RequestMethod.GET)
    public Object userInfo(@PathVariable int paperId) {
        Set<Long> userInfoSet = practiceMetaService.countEstimatePaper(paperId);
        return writeUserInfo(userInfoSet,paperId);

    }

    @RequestMapping(value = "allUserInfo/{paperId}", method = RequestMethod.GET)
    public Object allUserInfo(@PathVariable int paperId){
        Set<Long> userInfoSet = practiceMetaService.getMatchAllUserInfoByPaperId(paperId);
        return writeUserInfo(userInfoSet,paperId);
    }


    private Object writeUserInfo(Set<Long> userInfoSet,int paperId){
        ArrayList userIdSet = new ArrayList<>(userInfoSet);
        int indexStart = 0;
        int size = 100;
        int length = userIdSet.size();
        List<UserDto> userDtoList = Lists.newArrayList();

        while (indexStart < length) {
            int end = indexStart + size > length ? length : indexStart + size;
            userDtoList.addAll(userHuatuDao.findByIds(userIdSet.subList(indexStart, end)));
            indexStart = end;
        }
        List dataList = Lists.newArrayList();
        for (int index = 0; index < userDtoList.size(); index++) {
            UserDto userDto = userDtoList.get(index);
            dataList.add(Lists.newArrayList(userDto.getName(), userDto.getNick(), userDto.getMobile()));
        }
        String[] title = {"名称", "昵称", "手机号"};
        try {
            ExcelManageUtil.writer(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH, "MatchEnrollInfo_" + paperId, "xls", dataList, title);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map mapData = Maps.newHashMap();
        Paper paper = paperDao.findById(paperId);
        mapData.put("title", "模考答题用户信息-" + paper.getName() + "-" + paper.getId() );
        mapData.put("text", "模考答题用户信息，详情请下载附件中的Excel文本信息。");
        mapData.put("filePath", FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH + "MatchEnrollInfo_" + paperId + ".xls");
        mapData.put("attachName", "MatchEnrollInfo_" + System.currentTimeMillis());

        try {
            MailUtil.sendMail(mapData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return SuccessMessage.create("报名数据统计成功");

    }
=======
        mapData.put("submitCount",submitCount);
        Map map1 = practiceMetaService.getMaxScoreInfo(paperId);
        mapData.putAll(map1);
        double average = practiceMetaService.getAverage(paperId);
        mapData.put("average",average);
        Line line = practiceMetaService.getLine(paperId);
        mapData.put("line",line);
        List<Map> list =  practiceMetaService.getWrongQuestionMeta(paperId);
        mapData.put("wrongList",list);
        practiceMetaService.parseStatements(mapData,paperId);

        return mapData;
    }
>>>>>>> master
}
