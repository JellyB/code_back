package com.huatu.tiku.teacher.service.impl.match;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.match.bean.entity.MatchQuestionMeta;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.teacher.service.MatchService;
import com.huatu.tiku.teacher.service.match.MatchMetaService;
import com.huatu.tiku.teacher.service.match.MatchQuestionMetaService;
import com.huatu.tiku.teacher.service.match.MatchUserMetaService;
import com.huatu.tiku.teacher.util.file.ExcelManageUtil;
import com.huatu.tiku.util.file.FunFileUtils;
import com.huatu.tiku.util.http.ResponseMsg;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.user.bean.UserDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/10/16.
 */
@Service
public class MatchMetaServiceImpl implements MatchMetaService {

    @Autowired
    MatchQuestionMetaService matchQuestionMetaService;

    @Autowired
    MatchUserMetaService matchUserMetaService;

    @Autowired
    MatchService matchService;

    @Autowired
    PracticeMetaService practiceMetaService;

    @Value("${spring.profiles}")
    public String env;


    @Override
    public Object persistence(int matchId) {
        Map mapData = Maps.newHashMap();
        Match match = matchService.findById(matchId);
        if (null == match) {
            mapData.put("id", false);
            return mapData;
        }
        mapData.put("id", true);
        if (match.getEndTime() < System.currentTimeMillis()) {
            mapData.put("endFlag", false);
            return mapData;
        }
        mapData.put("endFlag", true);
        Example example = new Example(MatchQuestionMeta.class);
        example.and().andEqualTo("matchId", matchId);
        List<MatchQuestionMeta> matchQuestionMetas = matchQuestionMetaService.selectByExample(example);
        if (CollectionUtils.isNotEmpty(matchQuestionMetas)) {
            int questionMetaSize = matchQuestionMetaService.persistenceByPaperId(matchId);
            mapData.put("persistenceQuestionMeta", questionMetaSize);
        } else {
            mapData.put("persistenceQuestionMeta", false);
        }
        Example userExample = new Example(MatchUserMeta.class);
        userExample.and().andEqualTo("matchId", matchId);
        List<MatchUserMeta> matchUserMetas = matchUserMetaService.selectByExample(userExample);
        if (CollectionUtils.isNotEmpty(matchUserMetas)) {
            int userMetaSize = matchUserMetaService.persistenceByPaperId(matchId);
            mapData.put("persistenceUserMeta", userMetaSize);
        } else {
            mapData.put("persistenceUserMeta", false);
        }
//        if (match.getEssayPaperId() > 0) {
//            int essaySize= matchEssayUserMetaService.persistenceByPaperId(match.getEssayPaperId());
//            mapData.put("persistenceEssayUserMeta",essaySize);
//        }
        return mapData;
    }

    @Override
    public Object metaEnroll(int paperId) {
        List<MatchUserMeta> matchUserMetas = matchUserMetaService.findByMatchId(paperId);
        int i = 0;
        int size = 100;
        List<LinkedHashMap<String, Object>> data = Lists.newArrayList();
        while (i < matchUserMetas.size()) {
            int end = (i + size) < matchUserMetas.size() ? (i + size) : matchUserMetas.size();
            List<MatchUserMeta> tempList = matchUserMetas.subList(i, end);
            List<UserDto> userDtos = tempList.stream().map(MatchUserMeta::getUserId).map(id -> UserDto.builder().id(id).build()).collect(Collectors.toList());
            assertUserInfo(userDtos, data);
            i = end;
        }
        return writeEnrollInfo2Excel(data, paperId);
    }

    @Override
    public File metaResult(int paperId) {
        Map mapData = Maps.newHashMap();
        Map map = practiceMetaService.getCountByPosition(paperId);
        mapData.putAll(map);
        Long submitCount = practiceMetaService.getCountSubmit(paperId);
        mapData.put("submitCount", submitCount);
        Map map1 = practiceMetaService.getMaxScoreInfo(paperId);
        mapData.putAll(map1);
        double average = practiceMetaService.getAverage(paperId);
        mapData.put("average", average);
        Line line = practiceMetaService.getLine(paperId);
        mapData.put("line", line);
        List<Map> list = practiceMetaService.getWrongQuestionMeta(paperId);
        mapData.put("wrongList", list);
        return practiceMetaService.parseStatements(mapData, paperId);
    }

    @Override
    public Map metaAllTime(int paperId) {
        List<MatchUserMeta> matchUserMetas = matchUserMetaService.findByMatchId(paperId);
        int i = 0;
        int size = 100;
        List<LinkedHashMap<String, Object>> data = Lists.newArrayList();
        while (i < matchUserMetas.size()) {
            int end = (i + size) < matchUserMetas.size() ? (i + size) : matchUserMetas.size();
            List<MatchUserMeta> tempList = matchUserMetas.subList(i, end);
            List<UserDto> userDtos = tempList.stream().map(MatchUserMeta::getUserId).map(id -> UserDto.builder().id(id).build()).collect(Collectors.toList());
            assertUserInfo(userDtos, data);
            i = end;
        }
        return writeAllInfo2Excel(data, matchUserMetas, paperId);
    }

    @Override
    public List<MatchUserMeta> getMetaForEdu(int paperId) {
        Example example = new Example(MatchUserMeta.class);
        example.and().andEqualTo("matchId", paperId).andNotEqualTo("practiceId", -1L);
        List<MatchUserMeta> metas = matchUserMetaService.selectByExample(example);
        if (CollectionUtils.isEmpty(metas)) {
            return Lists.newArrayList();
        }
        return metas;
    }

    public List<HashMap> assemblingEduMeta(List<MatchUserMeta> metas) {
        List<UserDto> collect = metas.parallelStream().map(MatchUserMeta::getUserId)
                .map(i -> UserDto.builder().id(i).build()).collect(Collectors.toList());
        List<LinkedHashMap<String, Object>> data = Lists.newArrayList();
        assertUserInfo(collect, data);
        List<HashMap> result = Lists.newArrayList();
        for (MatchUserMeta meta : metas) {
            HashMap<String, Object> map = Maps.newHashMap();
            map.put("userId", meta.getUserId());
            map.put("score", meta.getScore());
            int rank = -1;
            if (null == meta.getRank() || meta.getRank() <= 0) {
                Example example = new Example(MatchUserMeta.class);
                example.and().andEqualTo("matchId", meta.getMatchId()).andGreaterThan("score", meta.getScore());
                int i = matchUserMetaService.selectCountByExample(example);
                rank = i + 1;
            } else {
                rank = meta.getRank();
            }
            map.put("rank", rank);
            map.put("areaId", meta.getPositionId());
            map.put("areaName", meta.getPositionName());
            Optional<String> first = data.stream()
                    .filter(i -> MapUtils.getInteger(i, "id", -1).equals(meta.getUserId()))
                    .map(i -> MapUtils.getString(i, "mobile",""))
                    .findFirst();
            if (first.isPresent()) {
                map.put("phone", first.get());
            } else {
                map.put("phone", "");
            }
            result.add(map);
        }
        return result;
    }

    private Map writeAllInfo2Excel(List<LinkedHashMap<String, Object>> data, List<MatchUserMeta> matchUserMetas, int paperId) {
        Match match = matchService.findById(paperId);
        long startTime = match.getStartTime() + TimeUnit.MINUTES.toMillis(30);
        long endTime = match.getEndTime() + TimeUnit.MINUTES.toMillis(5);
        String[] title = {"名称", "昵称", "手机号", "报名时间", "考试时间", "交卷时间", "报名地区", "地区ID", "成绩"};
        List dataList = Lists.newArrayList();
        for (LinkedHashMap<String, Object> datum : data) {
            ArrayList<String> temp = Lists.newArrayList(MapUtils.getString(datum, "name", ""),
                    MapUtils.getString(datum, "nick", ""),
                    MapUtils.getString(datum, "mobile", ""));
            Integer userId = MapUtils.getInteger(datum, "id", -1);
            Optional<MatchUserMeta> first = matchUserMetas.stream().filter(i -> i.getUserId().equals(userId)).findFirst();
            if (first.isPresent()) {
                MatchUserMeta matchUserMeta = first.get();
                temp.add(null == matchUserMeta.getEnrollTime() ? "" : (matchUserMeta.getEnrollTime().getTime() > startTime ? new Date(startTime).toLocaleString() : matchUserMeta.getEnrollTime().toLocaleString()));
                temp.add(null == matchUserMeta.getCardCreateTime() ? "" : (matchUserMeta.getCardCreateTime().getTime() > startTime ? new Date(startTime).toLocaleString() : matchUserMeta.getCardCreateTime().toLocaleString()));
                temp.add(null == matchUserMeta.getSubmitTime() ? "" : (matchUserMeta.getSubmitTime().getTime() > endTime ? new Date(endTime).toLocaleString() : matchUserMeta.getSubmitTime().toLocaleString()));
                temp.add(matchUserMeta.getPositionName());
                temp.add(matchUserMeta.getPositionId() + "");
                temp.add(null == matchUserMeta.getScore() ? "0" : matchUserMeta.getScore().toString());
            } else {
                temp.add("");
                temp.add("");
                temp.add("");
                temp.add("");
                temp.add("");
                temp.add("");
            }
            dataList.add(temp);
        }
        Map apply = createExcel.apply(title, dataList);
        apply.put("name",match.getName());
        return apply;
    }

    /**
     * @param data
     * @param paperId
     * @return
     */
    private Object writeEnrollInfo2Excel(List<LinkedHashMap<String, Object>> data, int paperId) {
        String[] title = {"名称", "昵称", "手机号"};
        List dataList = Lists.newArrayList();
        for (LinkedHashMap<String, Object> datum : data) {
            ArrayList<String> temp = Lists.newArrayList(MapUtils.getString(datum, "name", ""),
                    MapUtils.getString(datum, "nick", ""),
                    MapUtils.getString(datum, "mobile", ""));
            dataList.add(temp);
        }
        return createExcel.apply(title, dataList);
    }

    BiFunction<String[], List, Map> createExcel = ((title, dataList) -> {
        String name = UUID.randomUUID().toString();
        try {
            ExcelManageUtil.writer(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH, name, "xls", dataList, title);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map mapData = Maps.newHashMap();
        mapData.put("title", name);
        mapData.put("text", name + "_" + new Date());
        mapData.put("filePath", FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH + name + ".xls");
        mapData.put("attachName", name);
        return mapData;
    });

    private void assertUserInfo(List<UserDto> userDtos, List<LinkedHashMap<String, Object>> data) {
        String url = "";
        if (!"test".equalsIgnoreCase(env)) {
            url = "https://ns.huatu.com/u/essay/statistics/user";
        } else {
            url = "http://192.168.100.22:11453/u/essay/statistics/user";
//            url = "https://ns.huatu.com/u/essay/statistics/user";
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseMsg<List<LinkedHashMap<String, Object>>> userDtoList = restTemplate.postForObject(url, userDtos, ResponseMsg.class);
        data.addAll(userDtoList.getData());
    }

    public static <T, D> List<T> test(List<D> list, Class<T> lol) {
        List<T> tList = Lists.newLinkedList();
        for (D d : list) {
            T t = null;
            try {
                //通过反射多次new对象来向list中输入值
                t = lol.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            BeanUtils.copyProperties(d, t);
            tList.add(t);
        }
        return tList;
    }
}
