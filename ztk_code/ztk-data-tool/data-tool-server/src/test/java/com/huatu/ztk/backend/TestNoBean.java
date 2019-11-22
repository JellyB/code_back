package com.huatu.ztk.backend;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.util.ExcelManageUtil;
import com.huatu.ztk.backend.util.FunFileUtils;
import com.huatu.ztk.backend.util.RestTemplateUtil;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * Created by huangqp on 2018\7\5 0005.
 */
public class TestNoBean {
    @Test
    public void tests(){
        String[] title = {"手机号","时间"};
        List dataList = Lists.newArrayList();
        for(int i=0;i<10;i++){
            dataList.add(Lists.newArrayList(i,i));
        }

        try {
            ExcelManageUtil.writer(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH,"MatchEnrollInfo_all","xls",dataList,title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test(){
//        Long temp = DateUtil.parseYYYYMMDDDate("2018/07/01").getTime() ;
//        System.out.println(temp);
        Random rand = new Random();
        int i = 0;
//        int i = rand.nextInt(0);
        int l = rand.nextInt(1);
        System.out.println(i+"_"+l);
        for(i=0;i<10;i++){
            System.out.println(rand.nextInt(1));
        }
    }
    @Test
    public void test1(){
        try {
            List<List> list = ExcelManageUtil.readExcel("C:\\Users\\x6\\Desktop\\1.xls");
            List<Integer> singleList = Lists.newArrayList();
            Function<Object,Integer> intParse = (obj->{
                if(Objects.nonNull(obj)|| NumberUtils.isNumber(String.valueOf(obj))){
                    Float i = Float.parseFloat(String.valueOf(obj));
                    return i.intValue();
                }
                return 0;
            });
            for (List rowList : list) {
                int mainId = 0;
                for (Object cell : rowList) {
                    Integer cellIntValue = intParse.apply(cell);
                    if(cellIntValue.intValue()!=0){
                        singleList.add(cellIntValue);
                        if(mainId == 0){
                            mainId = cellIntValue.intValue();
                        }else{
                            replaceQuestion(mainId,cellIntValue.intValue());
                        }

                    }


                }
            }

//            Set<Integer> collect = singleList.stream().collect(Collectors.toSet());
//            if(singleList.size()!=collect.size()){
//                System.out.println("有重复Id");
//                Map<Integer, List<Integer>> collect1 = singleList.stream().collect(Collectors.groupingBy(i -> i.intValue()));
//                List<Integer> collect2 = collect1.entrySet().stream().filter(integerListEntry -> integerListEntry.getValue().size() > 1).map(i -> i.getKey()).collect(Collectors.toList());
//                System.out.println("重复Id="+collect2);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void replaceQuestion(int mainId, int i) {
        System.out.println("mainId = [" + mainId + "], i = [" + i + "]");
        RestTemplateUtil.postDuplicateClear(mainId,i,"ns.huatu.com");
    }

    @Test
    public void test12()  {
        boolean b = RestTemplateUtil.postDuplicateClear(261890, 65945, "ns.huatu.com");
        System.out.println("b = " + b);

    }

    @Test
    public void testCount() throws IOException {
        List<List> list = Lists.newArrayList();
        String[] rows = new String[]{"第一题","第二题","第三题","第四题","第五题","第六题","第七题","第八题","第九题","第十题","创建时间"};
        String result = "{   \"resutls\": [\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811191413\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 3\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 4\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 3\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        265895,\n" +
                "                        266796,\n" +
                "                        268248,\n" +
                "                        267239,\n" +
                "                        265850,\n" +
                "                        264622,\n" +
                "                        265867,\n" +
                "                        266696,\n" +
                "                        267745,\n" +
                "                        268254\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 600,\n" +
                "                \"id\": 2080965430519791616,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 40,\n" +
                "                \"difficulty\": 6,\n" +
                "                \"name\": \"智能刷题--201811191413\",\n" +
                "                \"rcount\": 4,\n" +
                "                \"wcount\": 6,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 388,\n" +
                "                \"speed\": 38,\n" +
                "                \"createTime\": 1542608397397,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"4\",\n" +
                "                    \"2\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    71,\n" +
                "                    91,\n" +
                "                    67,\n" +
                "                    76,\n" +
                "                    13,\n" +
                "                    27,\n" +
                "                    39\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811191404\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 5.6,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 2\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 1\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 7\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        266084,\n" +
                "                        266875,\n" +
                "                        266763,\n" +
                "                        267764,\n" +
                "                        267078,\n" +
                "                        260999,\n" +
                "                        266616,\n" +
                "                        263516,\n" +
                "                        267077,\n" +
                "                        267710\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 560,\n" +
                "                \"id\": 2080960966379438080,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 30,\n" +
                "                \"difficulty\": 5.6,\n" +
                "                \"name\": \"智能刷题--201811191404\",\n" +
                "                \"rcount\": 3,\n" +
                "                \"wcount\": 7,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 437,\n" +
                "                \"speed\": 43,\n" +
                "                \"createTime\": 1542607916867,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"2\",\n" +
                "                    \"24\",\n" +
                "                    \"4\",\n" +
                "                    \"1234\",\n" +
                "                    \"2\",\n" +
                "                    \"4\",\n" +
                "                    \"2\",\n" +
                "                    \"4\",\n" +
                "                    \"3\",\n" +
                "                    \"1\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    1,\n" +
                "                    41,\n" +
                "                    165,\n" +
                "                    41,\n" +
                "                    27,\n" +
                "                    23,\n" +
                "                    37,\n" +
                "                    36,\n" +
                "                    29,\n" +
                "                    37\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811191404\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 5.8,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 3\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 3\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 4\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        266875,\n" +
                "                        266096,\n" +
                "                        264415,\n" +
                "                        266765,\n" +
                "                        264698,\n" +
                "                        266576,\n" +
                "                        263470,\n" +
                "                        267710,\n" +
                "                        267764,\n" +
                "                        267077\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 580,\n" +
                "                \"id\": 2080960677417058304,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 0,\n" +
                "                \"difficulty\": 5.8,\n" +
                "                \"name\": \"智能刷题--201811191404\",\n" +
                "                \"rcount\": 0,\n" +
                "                \"wcount\": 0,\n" +
                "                \"ucount\": 10,\n" +
                "                \"status\": 2,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 8,\n" +
                "                \"speed\": 8,\n" +
                "                \"createTime\": 1542607463365,\n" +
                "                \"lastIndex\": 0,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"0\",\n" +
                "                    \"0\",\n" +
                "                    \"0\",\n" +
                "                    \"0\",\n" +
                "                    \"0\",\n" +
                "                    \"0\",\n" +
                "                    \"0\",\n" +
                "                    \"0\",\n" +
                "                    \"0\",\n" +
                "                    \"0\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    8,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811181612\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 2\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 2\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 6\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        266871,\n" +
                "                        264587,\n" +
                "                        266554,\n" +
                "                        266700,\n" +
                "                        264837,\n" +
                "                        259624,\n" +
                "                        266876,\n" +
                "                        264895,\n" +
                "                        260122,\n" +
                "                        267717\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 600,\n" +
                "                \"id\": 2080300380595421184,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 60,\n" +
                "                \"difficulty\": 6,\n" +
                "                \"name\": \"智能刷题--201811181612\",\n" +
                "                \"rcount\": 6,\n" +
                "                \"wcount\": 4,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 410,\n" +
                "                \"speed\": 41,\n" +
                "                \"createTime\": 1542529164307,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"24\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"4\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"2\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    80,\n" +
                "                    1,\n" +
                "                    54,\n" +
                "                    48,\n" +
                "                    130,\n" +
                "                    8,\n" +
                "                    36,\n" +
                "                    32,\n" +
                "                    9,\n" +
                "                    12\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811181026\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6.2,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 3\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 2\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 5\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        267322,\n" +
                "                        264183,\n" +
                "                        264421,\n" +
                "                        266694,\n" +
                "                        261072,\n" +
                "                        264405,\n" +
                "                        266393,\n" +
                "                        267805,\n" +
                "                        266877,\n" +
                "                        266881\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 620,\n" +
                "                \"id\": 2080126433597849600,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 40,\n" +
                "                \"difficulty\": 6.2,\n" +
                "                \"name\": \"智能刷题--201811181026\",\n" +
                "                \"rcount\": 4,\n" +
                "                \"wcount\": 6,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 271,\n" +
                "                \"speed\": 27,\n" +
                "                \"createTime\": 1542508295732,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"1\",\n" +
                "                    \"2\",\n" +
                "                    \"1\",\n" +
                "                    \"134\",\n" +
                "                    \"3\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    61,\n" +
                "                    53,\n" +
                "                    44,\n" +
                "                    59,\n" +
                "                    7,\n" +
                "                    16,\n" +
                "                    28\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811181019\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 8\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 1\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 1\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        261018,\n" +
                "                        261145,\n" +
                "                        265983,\n" +
                "                        266083,\n" +
                "                        260096,\n" +
                "                        268260,\n" +
                "                        260071,\n" +
                "                        265904,\n" +
                "                        264760,\n" +
                "                        259607\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 600,\n" +
                "                \"id\": 2080122710742007808,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 30,\n" +
                "                \"difficulty\": 6,\n" +
                "                \"name\": \"智能刷题--201811181019\",\n" +
                "                \"rcount\": 3,\n" +
                "                \"wcount\": 7,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 171,\n" +
                "                \"speed\": 17,\n" +
                "                \"createTime\": 1542507894249,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"4\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    79,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    38,\n" +
                "                    47\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811180734\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 5.8,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 6\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 2\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 2\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        264572,\n" +
                "                        264414,\n" +
                "                        265969,\n" +
                "                        267200,\n" +
                "                        266135,\n" +
                "                        266285,\n" +
                "                        266653,\n" +
                "                        264775,\n" +
                "                        268240,\n" +
                "                        264517\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 580,\n" +
                "                \"id\": 2080039970730409984,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 50,\n" +
                "                \"difficulty\": 5.8,\n" +
                "                \"name\": \"智能刷题--201811180734\",\n" +
                "                \"rcount\": 5,\n" +
                "                \"wcount\": 5,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 492,\n" +
                "                \"speed\": 49,\n" +
                "                \"createTime\": 1542498180501,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"4\",\n" +
                "                    \"4\",\n" +
                "                    \"4\",\n" +
                "                    \"1\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    47,\n" +
                "                    74,\n" +
                "                    48,\n" +
                "                    130,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    55,\n" +
                "                    92,\n" +
                "                    25,\n" +
                "                    18\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811180721\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6.2,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 2\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 3\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 5\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        271987,\n" +
                "                        268243,\n" +
                "                        272061,\n" +
                "                        272049,\n" +
                "                        267336,\n" +
                "                        260122,\n" +
                "                        223992,\n" +
                "                        267008,\n" +
                "                        218555,\n" +
                "                        264632\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 620,\n" +
                "                \"id\": 2080033357839728640,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 50,\n" +
                "                \"difficulty\": 6.2,\n" +
                "                \"name\": \"智能刷题--201811180721\",\n" +
                "                \"rcount\": 5,\n" +
                "                \"wcount\": 5,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 321,\n" +
                "                \"speed\": 32,\n" +
                "                \"createTime\": 1542497221057,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"3\",\n" +
                "                    \"1\",\n" +
                "                    \"1\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    1,\n" +
                "                    5,\n" +
                "                    49,\n" +
                "                    88,\n" +
                "                    31,\n" +
                "                    19,\n" +
                "                    59,\n" +
                "                    12,\n" +
                "                    38,\n" +
                "                    19\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811180704\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 5.2,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 2\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 1\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 7\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        266866,\n" +
                "                        266282,\n" +
                "                        266722,\n" +
                "                        227001,\n" +
                "                        240958,\n" +
                "                        106018,\n" +
                "                        236151,\n" +
                "                        242054,\n" +
                "                        235713,\n" +
                "                        252869\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 520,\n" +
                "                \"id\": 2080024945617797120,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 50,\n" +
                "                \"difficulty\": 5.2,\n" +
                "                \"name\": \"智能刷题--201811180704\",\n" +
                "                \"rcount\": 5,\n" +
                "                \"wcount\": 5,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 377,\n" +
                "                \"speed\": 37,\n" +
                "                \"createTime\": 1542496274074,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"13\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"134\",\n" +
                "                    \"4\",\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"4\",\n" +
                "                    \"2\",\n" +
                "                    \"2\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    141,\n" +
                "                    1,\n" +
                "                    42,\n" +
                "                    7,\n" +
                "                    32,\n" +
                "                    55,\n" +
                "                    13,\n" +
                "                    17,\n" +
                "                    35,\n" +
                "                    34\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811180658\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 5.8,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 3\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 1\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 6\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        96257,\n" +
                "                        266805,\n" +
                "                        268251,\n" +
                "                        267237,\n" +
                "                        266708,\n" +
                "                        264852,\n" +
                "                        267714,\n" +
                "                        264835,\n" +
                "                        267070,\n" +
                "                        261011\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 580,\n" +
                "                \"id\": 2080021622865002496,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 40,\n" +
                "                \"difficulty\": 5.8,\n" +
                "                \"name\": \"智能刷题--201811180658\",\n" +
                "                \"rcount\": 4,\n" +
                "                \"wcount\": 6,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 334,\n" +
                "                \"speed\": 33,\n" +
                "                \"createTime\": 1542495835663,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"1\",\n" +
                "                    \"4\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"3\",\n" +
                "                    \"2\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    70,\n" +
                "                    2,\n" +
                "                    3,\n" +
                "                    43,\n" +
                "                    22,\n" +
                "                    62,\n" +
                "                    34,\n" +
                "                    46,\n" +
                "                    44,\n" +
                "                    8\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811171743\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6.4,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 2\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 2\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 6\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        266809,\n" +
                "                        271988,\n" +
                "                        272037,\n" +
                "                        259663,\n" +
                "                        267721,\n" +
                "                        267058,\n" +
                "                        267716,\n" +
                "                        261012,\n" +
                "                        271978,\n" +
                "                        223966\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 640,\n" +
                "                \"id\": 2079621403551727616,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 50,\n" +
                "                \"difficulty\": 6.4,\n" +
                "                \"name\": \"智能刷题--201811171743\",\n" +
                "                \"rcount\": 5,\n" +
                "                \"wcount\": 5,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 537,\n" +
                "                \"speed\": 53,\n" +
                "                \"createTime\": 1542448328174,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"1\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"2\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    83,\n" +
                "                    160,\n" +
                "                    33,\n" +
                "                    14,\n" +
                "                    17,\n" +
                "                    64,\n" +
                "                    16,\n" +
                "                    148\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811171000\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 5.8,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 1\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 3\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 6\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        266234,\n" +
                "                        266587,\n" +
                "                        267117,\n" +
                "                        259640,\n" +
                "                        264649,\n" +
                "                        264844,\n" +
                "                        271984,\n" +
                "                        267744,\n" +
                "                        264644,\n" +
                "                        261002\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 580,\n" +
                "                \"id\": 2079388533260288000,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 70,\n" +
                "                \"difficulty\": 5.8,\n" +
                "                \"name\": \"智能刷题--201811171000\",\n" +
                "                \"rcount\": 7,\n" +
                "                \"wcount\": 3,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 375,\n" +
                "                \"speed\": 37,\n" +
                "                \"createTime\": 1542420446625,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"1\",\n" +
                "                    \"4\",\n" +
                "                    \"4\",\n" +
                "                    \"1\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"2\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    1,\n" +
                "                    47,\n" +
                "                    180,\n" +
                "                    31,\n" +
                "                    15,\n" +
                "                    14,\n" +
                "                    7,\n" +
                "                    23,\n" +
                "                    20,\n" +
                "                    37\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811170957\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 5.2,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 4\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 1\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 5\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        228877,\n" +
                "                        262051,\n" +
                "                        223805,\n" +
                "                        262085,\n" +
                "                        265846,\n" +
                "                        266665,\n" +
                "                        264059,\n" +
                "                        265029,\n" +
                "                        267038,\n" +
                "                        259621\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 520,\n" +
                "                \"id\": 2079386950623559680,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 70,\n" +
                "                \"difficulty\": 5.2,\n" +
                "                \"name\": \"智能刷题--201811170957\",\n" +
                "                \"rcount\": 7,\n" +
                "                \"wcount\": 3,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 136,\n" +
                "                \"speed\": 13,\n" +
                "                \"createTime\": 1542419978359,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"1\",\n" +
                "                    \"3\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    28,\n" +
                "                    18,\n" +
                "                    18,\n" +
                "                    18,\n" +
                "                    20,\n" +
                "                    28\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811170832\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 5\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 3\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 2\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        267120,\n" +
                "                        266508,\n" +
                "                        261123,\n" +
                "                        264861,\n" +
                "                        264862,\n" +
                "                        264711,\n" +
                "                        266683,\n" +
                "                        264072,\n" +
                "                        263534,\n" +
                "                        264851\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 600,\n" +
                "                \"id\": 2079344014506590208,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 40,\n" +
                "                \"difficulty\": 6,\n" +
                "                \"name\": \"智能刷题--201811170832\",\n" +
                "                \"rcount\": 4,\n" +
                "                \"wcount\": 6,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 322,\n" +
                "                \"speed\": 32,\n" +
                "                \"createTime\": 1542415043992,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"2\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    120,\n" +
                "                    37,\n" +
                "                    83,\n" +
                "                    15,\n" +
                "                    62\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811170700\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 3\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 3\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 4\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        261063,\n" +
                "                        261146,\n" +
                "                        267098,\n" +
                "                        266733,\n" +
                "                        266919,\n" +
                "                        264689,\n" +
                "                        267028,\n" +
                "                        271967,\n" +
                "                        266427,\n" +
                "                        267784\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 600,\n" +
                "                \"id\": 2079297930740629504,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 50,\n" +
                "                \"difficulty\": 6,\n" +
                "                \"name\": \"智能刷题--201811170700\",\n" +
                "                \"rcount\": 5,\n" +
                "                \"wcount\": 5,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 341,\n" +
                "                \"speed\": 34,\n" +
                "                \"createTime\": 1542409570895,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"1\",\n" +
                "                    \"1\",\n" +
                "                    \"1\",\n" +
                "                    \"2\",\n" +
                "                    \"4\",\n" +
                "                    \"2\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    89,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    40,\n" +
                "                    32,\n" +
                "                    96,\n" +
                "                    9,\n" +
                "                    28,\n" +
                "                    30,\n" +
                "                    15\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811170651\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 4\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 1\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 5\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        266911,\n" +
                "                        266137,\n" +
                "                        266092,\n" +
                "                        264151,\n" +
                "                        268262,\n" +
                "                        266879,\n" +
                "                        263475,\n" +
                "                        267024,\n" +
                "                        266398,\n" +
                "                        266563\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 600,\n" +
                "                \"id\": 2079293248127369216,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 40,\n" +
                "                \"difficulty\": 6,\n" +
                "                \"name\": \"智能刷题--201811170651\",\n" +
                "                \"rcount\": 4,\n" +
                "                \"wcount\": 6,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 261,\n" +
                "                \"speed\": 26,\n" +
                "                \"createTime\": 1542408933067,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"5\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"1\",\n" +
                "                    \"1\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"3\",\n" +
                "                    \"1\",\n" +
                "                    \"1\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    27,\n" +
                "                    115,\n" +
                "                    34,\n" +
                "                    14,\n" +
                "                    44,\n" +
                "                    8,\n" +
                "                    16\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811170645\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 5\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 2\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 3\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        265901,\n" +
                "                        266397,\n" +
                "                        266262,\n" +
                "                        264587,\n" +
                "                        266090,\n" +
                "                        268274,\n" +
                "                        266598,\n" +
                "                        267818,\n" +
                "                        264847,\n" +
                "                        266517\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 600,\n" +
                "                \"id\": 2079290258595250176,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 40,\n" +
                "                \"difficulty\": 6,\n" +
                "                \"name\": \"智能刷题--201811170645\",\n" +
                "                \"rcount\": 4,\n" +
                "                \"wcount\": 6,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 215,\n" +
                "                \"speed\": 21,\n" +
                "                \"createTime\": 1542408529291,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"1\",\n" +
                "                    \"4\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    3,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    39,\n" +
                "                    87,\n" +
                "                    11,\n" +
                "                    51,\n" +
                "                    19\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811170635\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 2\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 3\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 5\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        264623,\n" +
                "                        268228,\n" +
                "                        266700,\n" +
                "                        259719,\n" +
                "                        264086,\n" +
                "                        268292,\n" +
                "                        260120,\n" +
                "                        259764,\n" +
                "                        264845,\n" +
                "                        266567\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 600,\n" +
                "                \"id\": 2079285333903540224,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 60,\n" +
                "                \"difficulty\": 6,\n" +
                "                \"name\": \"智能刷题--201811170635\",\n" +
                "                \"rcount\": 6,\n" +
                "                \"wcount\": 4,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 496,\n" +
                "                \"speed\": 49,\n" +
                "                \"createTime\": 1542408224097,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"4\",\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"2\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    2,\n" +
                "                    95,\n" +
                "                    43,\n" +
                "                    143,\n" +
                "                    52,\n" +
                "                    19,\n" +
                "                    61,\n" +
                "                    26,\n" +
                "                    19,\n" +
                "                    36\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811170629\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 5.6,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 7\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 1\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 2\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        228854,\n" +
                "                        262817,\n" +
                "                        218861,\n" +
                "                        228064,\n" +
                "                        218613,\n" +
                "                        265184,\n" +
                "                        115851,\n" +
                "                        259670,\n" +
                "                        267801,\n" +
                "                        266651\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 560,\n" +
                "                \"id\": 2079282439531790336,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 40,\n" +
                "                \"difficulty\": 5.6,\n" +
                "                \"name\": \"智能刷题--201811170629\",\n" +
                "                \"rcount\": 4,\n" +
                "                \"wcount\": 6,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 233,\n" +
                "                \"speed\": 23,\n" +
                "                \"createTime\": 1542407616155,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    1\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"3\",\n" +
                "                    \"1\",\n" +
                "                    \"2\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    1,\n" +
                "                    85,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    11,\n" +
                "                    2,\n" +
                "                    79,\n" +
                "                    18,\n" +
                "                    34\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            },\n" +
                "            {\n" +
                "                \"paper\": {\n" +
                "                    \"name\": \"智能刷题--201811162158\",\n" +
                "                    \"qcount\": 10,\n" +
                "                    \"difficulty\": 6,\n" +
                "                    \"catgory\": 1,\n" +
                "                    \"subject\": 1,\n" +
                "                    \"modules\": [\n" +
                "                        {\n" +
                "                            \"category\": 482,\n" +
                "                            \"name\": \"数量关系\",\n" +
                "                            \"qcount\": 4\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 435,\n" +
                "                            \"name\": \"言语理解与表达\",\n" +
                "                            \"qcount\": 5\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": 392,\n" +
                "                            \"name\": \"常识判断\",\n" +
                "                            \"qcount\": 1\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"questions\": [\n" +
                "                        261108,\n" +
                "                        260053,\n" +
                "                        267607,\n" +
                "                        266827,\n" +
                "                        259664,\n" +
                "                        261046,\n" +
                "                        266385,\n" +
                "                        266968,\n" +
                "                        266368,\n" +
                "                        267706\n" +
                "                    ]\n" +
                "                },\n" +
                "                \"recommendedTime\": 600,\n" +
                "                \"id\": 2079024908771262464,\n" +
                "                \"userId\": 235474593,\n" +
                "                \"subject\": 1,\n" +
                "                \"catgory\": 1,\n" +
                "                \"score\": 30,\n" +
                "                \"difficulty\": 6,\n" +
                "                \"name\": \"智能刷题--201811162158\",\n" +
                "                \"rcount\": 3,\n" +
                "                \"wcount\": 7,\n" +
                "                \"ucount\": 0,\n" +
                "                \"status\": 3,\n" +
                "                \"type\": 1,\n" +
                "                \"terminal\": 1,\n" +
                "                \"expendTime\": 271,\n" +
                "                \"speed\": 27,\n" +
                "                \"createTime\": 1542376952818,\n" +
                "                \"lastIndex\": 9,\n" +
                "                \"remainingTime\": -1,\n" +
                "                \"corrects\": [\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    2,\n" +
                "                    2,\n" +
                "                    2\n" +
                "                ],\n" +
                "                \"answers\": [\n" +
                "                    \"2\",\n" +
                "                    \"2\",\n" +
                "                    \"3\",\n" +
                "                    \"2\",\n" +
                "                    \"1\",\n" +
                "                    \"1\",\n" +
                "                    \"4\",\n" +
                "                    \"4\",\n" +
                "                    \"4\",\n" +
                "                    \"3\"\n" +
                "                ],\n" +
                "                \"times\": [\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    1,\n" +
                "                    30,\n" +
                "                    46,\n" +
                "                    66,\n" +
                "                    42,\n" +
                "                    62,\n" +
                "                    21\n" +
                "                ],\n" +
                "                \"points\": null,\n" +
                "                \"doubts\": [\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0,\n" +
                "                    0\n" +
                "                ],\n" +
                "                \"moduleStatus\": null,\n" +
                "                \"iconUrl\": null,\n" +
                "                \"hasGift\": 0\n" +
                "            }\n" +
                "        ]}";
        Map map = JsonUtil.toMap(result);
        Object result1 = MapUtils.getObject(map, "resutls");
        System.out.println("result1 = " + result1);
        List resultList = (List) result1;
        for (Object o : resultList) {

        }
//        ExcelManageUtil.writer("C:\\Users\\x6\\Desktop\\pandora\\","做题时间统计","xls",list,rows);

    }

    private void insertList(String string,List result) {
        List<LinkedHashMap> list = JsonUtil.toObject(string, List.class);
        List times = Lists.newArrayList();
        System.out.println("list.size() = " + list.size());
        int time = 0;
        for (LinkedHashMap linkedHashMap : list) {
            Integer time1 = MapUtils.getInteger(linkedHashMap, "time");
            times.add(time1.toString());
            time  += time1;
        }
        printTime(times);
        result.add(times);
        System.out.println("time = " + time);
    }

    private void printTime(List times) {
        List list1 = times.subList(0, 20);
        int total = 0;
        int i = 0;
        for (Object o : list1) {
            i += Integer.parseInt(String.valueOf(o));
        }
        System.out.println("list1 = " + i);
        total += i;
        List list2 = times.subList(20, 60);
        i = 0;
        for (Object o : list2) {
            i += Integer.parseInt(String.valueOf(o));
        }
        System.out.println("list2 = " + i);
        total += i;
        List list3 = times.subList(60, 75);
        i = 0;
        for (Object o : list3) {
            i += Integer.parseInt(String.valueOf(o));
        }
        System.out.println("list3 = " + i);
        total += i;
        List list4 = times.subList(75, 115);
        i = 0;
        for (Object o : list4) {
            i += Integer.parseInt(String.valueOf(o));
        }
        System.out.println("list4 = " + i);
        total += i;
        List list5 = times.subList(115, 135);
        i = 0;
        for (Object o : list5) {
            i += Integer.parseInt(String.valueOf(o));
        }
        System.out.println("list5 = " + i);
        total += i;

        System.out.println("total = " + total);
    }


}

