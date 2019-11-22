package com.huatu.ztk.course.service;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.course.common.AuditListType;
import com.huatu.ztk.course.common.NetSchoolConfig;
import com.huatu.ztk.course.common.NetSchoolUrl;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;


/**
 * 用于ios审核
 * course业务层
 */
@Service
public class CourseAuditService {

    private final static int DEFAULT_ORDER = 1000;

    private final static String BASIC_PATH = System.getProperty("server_resources") + "/webapp/WEB-INF/pages/ios_audit/%s.json";

    @Autowired
    private CourseService courseService;

    /**
     * 我的图书或直播列表
     * @param username
     * @param order
     * @param catgory
     * @param listType
     * @return
     * @throws Exception
     */
    public LinkedHashMap getMyList(String username, int order, int catgory, int listType) throws Exception {
        final HashMap<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("username", username);
        paramMap.put("order", order);
        paramMap.put("categoryid", catgory == CatgoryType.GONG_WU_YUAN ?
                NetSchoolConfig.CATEGORY_GWY : NetSchoolConfig.CATEGORY_SHIYE);
        LinkedHashMap data = (LinkedHashMap) courseService.getJson(paramMap, NetSchoolUrl.MY_LIST, false);
        return filterData(data, listType);
    }


    /**
     * 过滤数据
     * @param data
     * @param listType
     * @return
     * @throws Exception
     */
    private LinkedHashMap filterData(LinkedHashMap data, int listType) throws Exception{
        List<Integer> bookIds = getBookIds();
        ArrayList<Map> result = (ArrayList<Map>)data.get("result");

        ArrayList<Map> newResult = new ArrayList<>();
        for (Map item : result) {
            Integer netClassId = Integer.valueOf(item.get("NetClassId").toString());

            if (listType == AuditListType.BOOK && bookIds.contains(netClassId)) {
                item.put("startDate","");
                item.put("endDate","");
                //如果是图书列表,而且id是图书id
                newResult.add(item);
            } else if (listType == AuditListType.LIVE && !bookIds.contains(netClassId)) {
                //如果是直播课列表,而且id不是图书id
                newResult.add(item);
            }
        }
        data.put("result", newResult);

        return data;
    }


    /**
     * 图书id
     * @return
     * @throws Exception
     */
    private List<Integer> getBookIds() throws Exception{
        String filePath = getListFilePath(AuditListType.BOOK);
        String json = FileUtils.readFileToString(new File(filePath));
        Map dataMap = JsonUtil.toMap(json);

        ArrayList<Map> result = (ArrayList<Map>)dataMap.get("result");

        List<Integer> bookIds = new ArrayList<>();
        for (Map map : result) {
            bookIds.add(Integer.valueOf(map.get("rid").toString()));
        }
        return bookIds;
    }

    private String getListFilePath(int listType) {
        return (listType == AuditListType.LIVE) ? String.format(BASIC_PATH, "ios_audit_list")
                :  String.format(BASIC_PATH, "ios_audit_book_list");
    }

    /**
     * 图书/直播列表
     * @param username
     * @param catgory
     * @param listType
     * @return
     * @throws Exception
     */
    public Object getList(String username, int catgory, int listType) throws Exception{
        String filePath = getListFilePath(listType);
        final String auditJson = FileUtils.readFileToString(new File(filePath));
        Map listDataMap = JsonUtil.toMap(auditJson);

        //查询我的直播
        LinkedHashMap myList = getMyList(username, DEFAULT_ORDER, catgory, listType);
        ArrayList<Map> myListResult = (ArrayList<Map>)myList.get("result");
        //已经购买的图书或者课程id
        ArrayList<Integer> buyIds = new ArrayList<>();
        for (Map item : myListResult) {
            Integer netClassId = Integer.valueOf(item.get("NetClassId").toString());
            buyIds.add(netClassId);
        }

        ArrayList<Map> listResult = (ArrayList<Map>)listDataMap.get("result");
        ArrayList<Map> newResult = new ArrayList<>();
        for (Map item : listResult) {
            Integer netClassId = Integer.valueOf(item.get("NetClassId").toString());
            if (buyIds.contains(netClassId)) {
                //状态设置成已经购买的
                item.put("isBuy", 1);
            }
            newResult.add(item);
        }

        listDataMap.put("result", newResult);

        return listDataMap;
    }
}
