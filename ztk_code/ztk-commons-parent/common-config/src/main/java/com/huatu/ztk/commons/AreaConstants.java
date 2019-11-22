package com.huatu.ztk.commons;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 区域常量
 * Created by shaojieyue
 * Created time 2016-07-02 14:41
 */
public class AreaConstants {

    /**全国id**/
    public static final int QUAN_GUO_ID = -9;
    public static final List<Area> DEPTH_ONE_ARENA = new LinkedList<>();
    public static final List<Area> DEPTH_TWO_ARENA = new LinkedList<>();
    public static final List<Integer> AREA_ORDER = Lists.newArrayList();
    static {
        Integer[] sort = {1,1374,3239,3238,3237,823,3234,3233,3232,1963,1964,1988,3235,3236,943,3231,3230,1532,586,3240,3242,2600,225,471,356,1045,3226,3224,1168,41,2502,2299,656,3227,3228,3229,1709,2827,1263,2106,2945,1826,2257,21,2230,3098,3046,3125};
        for (Integer integer : sort) {
            AREA_ORDER.add(integer);
        }
        InputStream input = AreaConstants.class.getClassLoader().getResourceAsStream("area.txt");
        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            bufferedReader.lines().forEach(line ->{
                final String[] split = line.split(",");
                int id = Integer.valueOf(split[0]);
                String name = split[1];
                int parent = Integer.valueOf(split[2]);
                if (parent == 0) {
                    Area depthOneArea = new Area(id,name,0);
                    //一级区域
                    DEPTH_ONE_ARENA.add(depthOneArea);
                    Area depthTwoArea = new Area(id,name,0);
                    depthTwoArea.setChildren(Lists.newLinkedList());
                    DEPTH_TWO_ARENA.add(depthTwoArea);
                }else {
                    for (Area parentArea : DEPTH_TWO_ARENA) {
                        if (parentArea.getId() == parent) {
                            Area area = new Area(id,name,parent);
                            //设置子集
                            parentArea.getChildren().add(area);
                            break;
                        }
                    }
                }
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据级别查询区域列表
     * @param depth
     * @return
     */
    public static final List<Area> getAreas(int depth){
        if (depth == 1) {
            return DEPTH_ONE_ARENA;
        }else if (depth == 2) {
            return DEPTH_TWO_ARENA;
        }
        return new ArrayList<>();
    }

    /**
     * 根据区域id查询区域全名
     * @param areaId
     * @return
     */
    public static final String getFullAreaNmae(int areaId){

        for (Area area : DEPTH_ONE_ARENA) {//先从顶级节点查询
            if (area.getId() == areaId) {
                return area.getName();
            }
        }

        for (Area area : DEPTH_TWO_ARENA) {
            final List<Area> children = area.getChildren();
            for (Area child : children) {
                if (child.getId() == areaId) {
                    return area.getName()+child.getName();
                }
            }
        }

        return "未知区域";
    }

    /**
     * 根据区域查询区域
     * @param areaId
     * @return
     */
    public static final Area getArea(int areaId){
        for (Area area : DEPTH_TWO_ARENA) {
            if (area.getId() == areaId) {
                return area;
            }

            final List<Area> children = area.getChildren();
            for (Area child : children) {
                if (child.getId() == areaId) {
                    return child;
                }
            }
        }

        return null;
    }

    /**
     * 根据areaId查询其parentId
     * @param areaId
     * @return
     */
    public static final int getParentId(int areaId){
        final Area area = getArea(areaId);
        if (area != null) {
            return area.getParentId();
        }
        return -1;
    }

}
