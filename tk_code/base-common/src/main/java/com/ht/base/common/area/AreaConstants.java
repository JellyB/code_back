package com.ht.base.common.area;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.io.*;
import java.util.*;

/**
 * 区域常量
 * Created by shaojieyue
 * Created time 2016-07-02 14:41
 */
public class AreaConstants {

    /**全国id**/
    public static final int QUAN_GUO_ID = -9;

    private static Map<Integer,Area> AREAS;
    /**
     * 题库下地区分两级
     */
    public static Map<Integer, Area> FIRST_AREA_MAP;
    public static Map<Integer,Area> SEC_AREA_MAP;
    /**
     *
     */
    private static List<Area> FIRSET_AREAS;
    private static List<Area> SECOND_AREAS;

    //region 初始化
    static {
        InputStream input = AreaConstants.class.getClassLoader().getResourceAsStream("area.txt");
        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            //
            ImmutableMap.Builder<Integer,Area> areasBuilder = ImmutableMap.builder();

            Map<Integer,Integer> areaMapping = getNetschoolMapping();

            bufferedReader.lines().forEach(line ->{
                final String[] split = line.split(",");
                int id = Integer.valueOf(split[0]);
                String name = split[1];
                int parent = Integer.valueOf(split[2]);
                int gid = Integer.valueOf(split[3]);
                int cid = Optional.ofNullable(areaMapping.get(id)).orElse(0);
                Area area = new Area(id,name,parent,gid,cid);
                areasBuilder.put(area.getId(),area);
            });
            // 所有地区集合
            AREAS = areasBuilder.build();

            ImmutableMap.Builder<Integer,Area> firstAreaBuilder = ImmutableMap.builder();
            ImmutableMap.Builder<Integer,Area> secondAreaBuilder = ImmutableMap.builder();
            AREAS.forEach((k,v) -> {
                if(v.getParentId() == 0){//一级节点
                    firstAreaBuilder.put(k,v);
                }else {
                    secondAreaBuilder.put(k,v);
                    Area parent = getArea(v.getParentId());
                    if(parent != null){
                        parent.getChildren().add(v);
                    }
                }
            });

            FIRST_AREA_MAP = firstAreaBuilder.build();
            SEC_AREA_MAP = secondAreaBuilder.build();

            //调整area的children不可修改
            FIRST_AREA_MAP.forEach((k,v) -> {
                if(!v.getChildren().isEmpty()){
                    ImmutableList<Area> children = ImmutableList.copyOf(v.getChildren());
                    v.setChildren(children);
                }
            });

            FIRSET_AREAS = ImmutableList.copyOf(FIRST_AREA_MAP.values());
            SECOND_AREAS = ImmutableList.copyOf(SEC_AREA_MAP.values());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //endregion


    /**
     * 课程相关，省份映射
     * @return
     */
    private static Map<Integer,Integer> getNetschoolMapping(){
        Map<Integer,Integer> mapping = Maps.newHashMap();
        InputStream input = AreaConstants.class.getClassLoader().getResourceAsStream("area.mapping");
        Properties properties = new Properties();
        try {
            properties.load(input);
            properties.forEach((k,v) -> {
               mapping.put(Integer.parseInt(String.valueOf(k)),Integer.parseInt(String.valueOf(v)));
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        properties = null;
        return mapping;
    }

    /**
     * 根据areaId获取课程使用的provinceId
     * @param areaId
     * @return
     */
    public static int getNetSchoolProvinceId(int areaId){
        Area area = getArea(areaId);
        if(area.getCid() == 0){
            area = getArea(area.getParentId());
        }
        return Optional.ofNullable(area).map( (a) -> a.getCid()).orElse(0);
    }


    /**
     * 根据区域查询区域
     * @param areaId
     * @return
     */
    public static final Area getArea(int areaId){
        return AREAS.get(areaId);
    }

    /**
     * 根据级别查询区域列表
     * @param depth
     * @return
     */
    public static final List<Area> getAreas(int depth){
        if (depth == 1) {
            return FIRSET_AREAS;
        }else if (depth == 2) {
            return SECOND_AREAS;
        }
        return new ArrayList<>();
    }

    /**
     * 根据区域id查询区域全名
     * @param areaId
     * @return
     */
    public static final String getFullAreaNmae(int areaId){
        Area area = getArea(areaId);
        if(area.getParentId() == 0){
            return area.getName();
        }
        Area parent = getArea(area.getParentId());
        if(parent != null){
            return parent.getName()+area.getName();
        }
        return "未知区域";
    }


    /**
     * 根据areaId查询其parentId
     * @param areaId
     * @return
     */
    public static final int getParentId(int areaId){
        return Optional.<Area>ofNullable(getArea(areaId)).map(x -> x.getParentId()).orElse(-1);
    }

}
