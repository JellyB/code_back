package com.huatu.tiku.match.report;

import com.alibaba.fastjson.JSONObject;
import com.huatu.common.test.BaseWebTest;
import com.huatu.ztk.paper.bean.StandardCard;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-09 上午9:48
 **/
@Slf4j
public class ReportServiceTest extends BaseWebTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void cache(){
        String value = "{\n" +
                "    \"meta\" : 2,\n" +
                "    \"answers\" : [\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"3\",\n" +
                "      \"3\",\n" +
                "      \"3\",\n" +
                "      \"3\",\n" +
                "      \"3\",\n" +
                "      \"1\",\n" +
                "      \"3\",\n" +
                "      \"2\",\n" +
                "      \"2\",\n" +
                "      \"1\",\n" +
                "      \"3\",\n" +
                "      \"3\",\n" +
                "      \"3\",\n" +
                "      \"2\",\n" +
                "      \"1\",\n" +
                "      \"4\",\n" +
                "      \"3\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"3\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"2\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"2\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"2\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"2\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"2\",\n" +
                "      \"2\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\",\n" +
                "      \"1\"\n" +
                "    ],\n" +
                "    \"difficulty\" : 6,\n" +
                "    \"id\" : 72687019647224104,\n" +
                "    \"remainingTime\" : 7053,\n" +
                "    \"lastIndex\" : 124,\n" +
                "    \"addGroupUrl\" : null,\n" +
                "    \"expendTime\" : 147,\n" +
                "    \"subject\" : 1,\n" +
                "    \"points\" : [\n" +
                "      {\n" +
                "        \"id\" : 642,\n" +
                "        \"speed\" : 1,\n" +
                "        \"qnum\" : 35,\n" +
                "        \"unum\" : 0,\n" +
                "        \"rnum\" : 6,\n" +
                "        \"level\" : 0,\n" +
                "        \"unfinishedPracticeId\" : 0,\n" +
                "        \"children\" : [\n" +
                "          {\n" +
                "            \"id\" : 643,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 5,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 1,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 644,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"数量类\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 667,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"样式类\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 675,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"name\" : \"功能类\",\n" +
                "                \"wnum\" : 0\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 681,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 2,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"立体类\",\n" +
                "                \"wnum\" : 2\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 5,\n" +
                "            \"accuracy\" : 20,\n" +
                "            \"name\" : \"图形推理\",\n" +
                "            \"wnum\" : 4\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\" : 688,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 10,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 2,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 689,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"语法关系\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 693,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 2,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"外延关系\",\n" +
                "                \"wnum\" : 2\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 698,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 5,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 2,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 5,\n" +
                "                \"accuracy\" : 40,\n" +
                "                \"name\" : \"内涵关系\",\n" +
                "                \"wnum\" : 3\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 702,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 2,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"语义关系\",\n" +
                "                \"wnum\" : 2\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 10,\n" +
                "            \"accuracy\" : 20,\n" +
                "            \"name\" : \"类比推理\",\n" +
                "            \"wnum\" : 8\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\" : 707,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 10,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 1,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 1025,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 5,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 5,\n" +
                "                \"accuracy\" : 20,\n" +
                "                \"name\" : \"单定义\",\n" +
                "                \"wnum\" : 4\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 1026,\n" +
                "                \"speed\" : 2,\n" +
                "                \"qnum\" : 5,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 10,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"多定义\",\n" +
                "                \"wnum\" : 5\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 15,\n" +
                "            \"accuracy\" : 10,\n" +
                "            \"name\" : \"定义判断\",\n" +
                "            \"wnum\" : 9\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\" : 715,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 10,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 2,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 716,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"翻译推理\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 722,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"真假推理\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 727,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"name\" : \"分析推理\",\n" +
                "                \"wnum\" : 0\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 731,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"归纳推理\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 741,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 4,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 4,\n" +
                "                \"accuracy\" : 25,\n" +
                "                \"name\" : \"加强论证\",\n" +
                "                \"wnum\" : 3\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 744,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 2,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"削弱论证\",\n" +
                "                \"wnum\" : 2\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 10,\n" +
                "            \"accuracy\" : 20,\n" +
                "            \"name\" : \"逻辑判断\",\n" +
                "            \"wnum\" : 8\n" +
                "          }\n" +
                "        ],\n" +
                "        \"times\" : 40,\n" +
                "        \"accuracy\" : 17.100000000000001,\n" +
                "        \"name\" : \"判断推理\",\n" +
                "        \"wnum\" : 29\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\" : 392,\n" +
                "        \"speed\" : 1,\n" +
                "        \"qnum\" : 20,\n" +
                "        \"unum\" : 0,\n" +
                "        \"rnum\" : 4,\n" +
                "        \"level\" : 0,\n" +
                "        \"unfinishedPracticeId\" : 0,\n" +
                "        \"children\" : [\n" +
                "          {\n" +
                "            \"id\" : 393,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 3,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 1,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 394,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 3,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 4,\n" +
                "                \"accuracy\" : 33.299999999999997,\n" +
                "                \"name\" : \"时政\",\n" +
                "                \"wnum\" : 2\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 4,\n" +
                "            \"accuracy\" : 33.299999999999997,\n" +
                "            \"name\" : \"政治\",\n" +
                "            \"wnum\" : 2\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\" : 404,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 4,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 1,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 405,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"宪法\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 406,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"行政法\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 1001,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 2,\n" +
                "                \"accuracy\" : 50,\n" +
                "                \"name\" : \"法律其它\",\n" +
                "                \"wnum\" : 1\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 4,\n" +
                "            \"accuracy\" : 25,\n" +
                "            \"name\" : \"法律\",\n" +
                "            \"wnum\" : 3\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\" : 412,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 3,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 1,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 413,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 3,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"中国史\",\n" +
                "                \"wnum\" : 2\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 415,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"name\" : \"世界史\",\n" +
                "                \"wnum\" : 0\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 4,\n" +
                "            \"accuracy\" : 33.299999999999997,\n" +
                "            \"name\" : \"历史\",\n" +
                "            \"wnum\" : 2\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\" : 416,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 3,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 0,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 421,\n" +
                "                \"speed\" : 2,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 2,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"传统民俗\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 423,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 2,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"人文其它\",\n" +
                "                \"wnum\" : 2\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 4,\n" +
                "            \"accuracy\" : 0,\n" +
                "            \"name\" : \"人文\",\n" +
                "            \"wnum\" : 3\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\" : 424,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 7,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 1,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 425,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"科技成就\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 427,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"物理\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 428,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"name\" : \"化学\",\n" +
                "                \"wnum\" : 0\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 429,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"生物医学\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 430,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 3,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 3,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"地理国情\",\n" +
                "                \"wnum\" : 3\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 7,\n" +
                "            \"accuracy\" : 14.300000000000001,\n" +
                "            \"name\" : \"科技\",\n" +
                "            \"wnum\" : 6\n" +
                "          }\n" +
                "        ],\n" +
                "        \"times\" : 23,\n" +
                "        \"accuracy\" : 20,\n" +
                "        \"name\" : \"常识判断\",\n" +
                "        \"wnum\" : 16\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\" : 435,\n" +
                "        \"speed\" : 1,\n" +
                "        \"qnum\" : 40,\n" +
                "        \"unum\" : 0,\n" +
                "        \"rnum\" : 10,\n" +
                "        \"level\" : 0,\n" +
                "        \"unfinishedPracticeId\" : 0,\n" +
                "        \"children\" : [\n" +
                "          {\n" +
                "            \"id\" : 436,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 14,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 1,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 437,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 4,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 5,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"主旨概括\",\n" +
                "                \"wnum\" : 4\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 438,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"标题选择\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 439,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 4,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 5,\n" +
                "                \"accuracy\" : 25,\n" +
                "                \"name\" : \"意图判断\",\n" +
                "                \"wnum\" : 3\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 440,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"态度理解\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 441,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 3,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 4,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"细节理解\",\n" +
                "                \"wnum\" : 3\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 445,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"代词指代\",\n" +
                "                \"wnum\" : 1\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 17,\n" +
                "            \"accuracy\" : 7.0999999999999996,\n" +
                "            \"name\" : \"片段阅读\",\n" +
                "            \"wnum\" : 13\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\" : 446,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 20,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 8,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 447,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 10,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 3,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 10,\n" +
                "                \"accuracy\" : 30,\n" +
                "                \"name\" : \"实词辨析\",\n" +
                "                \"wnum\" : 7\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 449,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 7,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 3,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 7,\n" +
                "                \"accuracy\" : 42.899999999999999,\n" +
                "                \"name\" : \"成语辨析\",\n" +
                "                \"wnum\" : 4\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 451,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 3,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 2,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 3,\n" +
                "                \"accuracy\" : 66.700000000000003,\n" +
                "                \"name\" : \"实词与成语综合考查\",\n" +
                "                \"wnum\" : 1\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 20,\n" +
                "            \"accuracy\" : 40,\n" +
                "            \"name\" : \"逻辑填空\",\n" +
                "            \"wnum\" : 12\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\" : 453,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 6,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 1,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 454,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 2,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"语句填空\",\n" +
                "                \"wnum\" : 2\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 455,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 2,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"下文推断\",\n" +
                "                \"wnum\" : 2\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 456,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 3,\n" +
                "                \"accuracy\" : 50,\n" +
                "                \"name\" : \"语句排序\",\n" +
                "                \"wnum\" : 1\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 7,\n" +
                "            \"accuracy\" : 16.699999999999999,\n" +
                "            \"name\" : \"语句表达\",\n" +
                "            \"wnum\" : 5\n" +
                "          }\n" +
                "        ],\n" +
                "        \"times\" : 44,\n" +
                "        \"accuracy\" : 25,\n" +
                "        \"name\" : \"言语理解与表达\",\n" +
                "        \"wnum\" : 30\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\" : 482,\n" +
                "        \"speed\" : 1,\n" +
                "        \"qnum\" : 10,\n" +
                "        \"unum\" : 0,\n" +
                "        \"rnum\" : 3,\n" +
                "        \"level\" : 0,\n" +
                "        \"unfinishedPracticeId\" : 0,\n" +
                "        \"children\" : [\n" +
                "          {\n" +
                "            \"id\" : 524,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 10,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 3,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 1033,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"时间类问题\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 525,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"name\" : \"基础计算\",\n" +
                "                \"wnum\" : 0\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 555,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"方程与不等式\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 568,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"name\" : \"溶液问题\",\n" +
                "                \"wnum\" : 0\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 572,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"工程问题\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 583,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 2,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"几何问题\",\n" +
                "                \"wnum\" : 2\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 602,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"排列组合问题\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 630,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"经济利润问题\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 636,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"name\" : \"行程问题\",\n" +
                "                \"wnum\" : 0\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 10,\n" +
                "            \"accuracy\" : 30,\n" +
                "            \"name\" : \"数学运算\",\n" +
                "            \"wnum\" : 7\n" +
                "          }\n" +
                "        ],\n" +
                "        \"times\" : 10,\n" +
                "        \"accuracy\" : 30,\n" +
                "        \"name\" : \"数量关系\",\n" +
                "        \"wnum\" : 7\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\" : 754,\n" +
                "        \"speed\" : 1,\n" +
                "        \"qnum\" : 20,\n" +
                "        \"unum\" : 0,\n" +
                "        \"rnum\" : 3,\n" +
                "        \"level\" : 0,\n" +
                "        \"unfinishedPracticeId\" : 0,\n" +
                "        \"children\" : [\n" +
                "          {\n" +
                "            \"id\" : 774,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 2,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 0,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 775,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"增长率比较\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 779,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"平均数比较\",\n" +
                "                \"wnum\" : 1\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 2,\n" +
                "            \"accuracy\" : 0,\n" +
                "            \"name\" : \"比较类\",\n" +
                "            \"wnum\" : 2\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\" : 781,\n" +
                "            \"speed\" : 3,\n" +
                "            \"qnum\" : 4,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 0,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 782,\n" +
                "                \"speed\" : 3,\n" +
                "                \"qnum\" : 4,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 13,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"综合分析\",\n" +
                "                \"wnum\" : 4\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 13,\n" +
                "            \"accuracy\" : 0,\n" +
                "            \"name\" : \"综合分析类\",\n" +
                "            \"wnum\" : 4\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\" : 761,\n" +
                "            \"speed\" : 1,\n" +
                "            \"qnum\" : 14,\n" +
                "            \"unum\" : 0,\n" +
                "            \"rnum\" : 3,\n" +
                "            \"level\" : 1,\n" +
                "            \"unfinishedPracticeId\" : 0,\n" +
                "            \"children\" : [\n" +
                "              {\n" +
                "                \"id\" : 768,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"增长量计算\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 769,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 4,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 5,\n" +
                "                \"accuracy\" : 25,\n" +
                "                \"name\" : \"比重计算\",\n" +
                "                \"wnum\" : 3\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 770,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 2,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"平均数计算\",\n" +
                "                \"wnum\" : 2\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 772,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"倍数计算\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 773,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 0,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 0,\n" +
                "                \"name\" : \"其他类型计算\",\n" +
                "                \"wnum\" : 1\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 764,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 4,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 4,\n" +
                "                \"accuracy\" : 25,\n" +
                "                \"name\" : \"增长率计算\",\n" +
                "                \"wnum\" : 3\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\" : 765,\n" +
                "                \"speed\" : 1,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"unum\" : 0,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"level\" : 2,\n" +
                "                \"unfinishedPracticeId\" : 0,\n" +
                "                \"children\" : [\n" +
                "\n" +
                "                ],\n" +
                "                \"times\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"name\" : \"间隔增长率计算\",\n" +
                "                \"wnum\" : 0\n" +
                "              }\n" +
                "            ],\n" +
                "            \"times\" : 15,\n" +
                "            \"accuracy\" : 21.399999999999999,\n" +
                "            \"name\" : \"计算类\",\n" +
                "            \"wnum\" : 11\n" +
                "          }\n" +
                "        ],\n" +
                "        \"times\" : 30,\n" +
                "        \"accuracy\" : 15,\n" +
                "        \"name\" : \"资料分析\",\n" +
                "        \"wnum\" : 17\n" +
                "      }\n" +
                "    ],\n" +
                "    \"wcount\" : 99,\n" +
                "    \"hasGift\" : 0,\n" +
                "    \"score\" : 20,\n" +
                "    \"moduleCreateTime\" : {\n" +
                "      \"3\" : -1,\n" +
                "      \"1\" : -1,\n" +
                "      \"4\" : -1,\n" +
                "      \"2\" : -1,\n" +
                "      \"5\" : -1\n" +
                "    },\n" +
                "    \"type\" : 12,\n" +
                "    \"currentTime\" : null,\n" +
                "    \"corrects\" : [\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      1,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      2,\n" +
                "      2\n" +
                "    ],\n" +
                "    \"times\" : [\n" +
                "      2,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      2,\n" +
                "      1,\n" +
                "      1,\n" +
                "      2,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      2,\n" +
                "      1,\n" +
                "      2,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      2,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      2,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      6,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      2,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      1,\n" +
                "      10\n" +
                "    ],\n" +
                "    \"doubts\" : [\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0,\n" +
                "      0\n" +
                "    ],\n" +
                "    \"matchMeta\" : {\n" +
                "      \"schoolName\" : null,\n" +
                "      \"positionName\" : \"天津\",\n" +
                "      \"scoreLine\" : {\n" +
                "        \"series\" : [\n" +
                "          {\n" +
                "            \"name\" : \"12-8\",\n" +
                "            \"data\" : [\n" +
                "              38,\n" +
                "              28\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\" : \"12-15\",\n" +
                "            \"data\" : [\n" +
                "              54,\n" +
                "              20\n" +
                "            ]\n" +
                "          }\n" +
                "        ],\n" +
                "        \"categories\" : [\n" +
                "          \"全站平均得分\",\n" +
                "          \"模考得分\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"positionId\" : 21,\n" +
                "      \"positionAverage\" : 55.399999999999999,\n" +
                "      \"positionBeatRate\" : 10,\n" +
                "      \"schoolRank\" : null,\n" +
                "      \"positionCount\" : 59,\n" +
                "      \"positionMax\" : 85,\n" +
                "      \"schoolCount\" : null,\n" +
                "      \"positionRank\" : 53\n" +
                "    },\n" +
                "    \"name\" : \"2019省考万人模考行测-第六季\",\n" +
                "    \"hasGetBigGift\" : 0,\n" +
                "    \"status\" : 3,\n" +
                "    \"giftHtmlUrl\" : null,\n" +
                "    \"cardUserMeta\" : {\n" +
                "      \"max\" : 88,\n" +
                "      \"average\" : 54.700000000000003,\n" +
                "      \"rank\" : 2864,\n" +
                "      \"total\" : 3159,\n" +
                "      \"beatRate\" : 9\n" +
                "    },\n" +
                "    \"idStr\" : null,\n" +
                "    \"iconUrl\" : null,\n" +
                "    \"rightImgUrl\" : null,\n" +
                "    \"createTime\" : 1544835743161,\n" +
                "    \"rcount\" : 26,\n" +
                "    \"moduleStatus\" : {\n" +
                "      \"3\" : 0,\n" +
                "      \"1\" : 0,\n" +
                "      \"4\" : 0,\n" +
                "      \"2\" : 0,\n" +
                "      \"5\" : 0\n" +
                "    },\n" +
                "    \"catgory\" : 1,\n" +
                "    \"paper\" : {\n" +
                "      \"time\" : 7200,\n" +
                "      \"status\" : 2,\n" +
                "      \"url\" : null,\n" +
                "      \"passScore\" : 60,\n" +
                "      \"offlineTime\" : 1544850000000,\n" +
                "      \"difficulty\" : 6,\n" +
                "      \"score\" : 100,\n" +
                "      \"bigQuestions\" : [\n" +
                "        40028909,\n" +
                "        40028910,\n" +
                "        40028911,\n" +
                "        40028912,\n" +
                "        40028913,\n" +
                "        40028914,\n" +
                "        40028915,\n" +
                "        40028916,\n" +
                "        40028917,\n" +
                "        40028918,\n" +
                "        40028919,\n" +
                "        40028920,\n" +
                "        40028921,\n" +
                "        40028922,\n" +
                "        40028923,\n" +
                "        40028924,\n" +
                "        40028925,\n" +
                "        40028926,\n" +
                "        40028927,\n" +
                "        40028928,\n" +
                "        40028869,\n" +
                "        40028870,\n" +
                "        40028871,\n" +
                "        40028872,\n" +
                "        40028873,\n" +
                "        40028874,\n" +
                "        40028875,\n" +
                "        40028876,\n" +
                "        40028877,\n" +
                "        40028878,\n" +
                "        40028879,\n" +
                "        40028880,\n" +
                "        40028881,\n" +
                "        40028882,\n" +
                "        40028883,\n" +
                "        40028884,\n" +
                "        40028885,\n" +
                "        40028886,\n" +
                "        40028887,\n" +
                "        40028888,\n" +
                "        40028889,\n" +
                "        40028890,\n" +
                "        40028891,\n" +
                "        40028892,\n" +
                "        40028893,\n" +
                "        40028894,\n" +
                "        40028895,\n" +
                "        40028896,\n" +
                "        40028897,\n" +
                "        40028898,\n" +
                "        40028899,\n" +
                "        40028900,\n" +
                "        40028901,\n" +
                "        40028902,\n" +
                "        40028903,\n" +
                "        40028904,\n" +
                "        40028905,\n" +
                "        40028906,\n" +
                "        40028907,\n" +
                "        40028908,\n" +
                "        40028835,\n" +
                "        40028836,\n" +
                "        40028837,\n" +
                "        40028838,\n" +
                "        40028839,\n" +
                "        40028840,\n" +
                "        40028841,\n" +
                "        40028842,\n" +
                "        40028843,\n" +
                "        40028844,\n" +
                "        40028946,\n" +
                "        40028947,\n" +
                "        40028948,\n" +
                "        40028951,\n" +
                "        40028954,\n" +
                "        40028955,\n" +
                "        40028956,\n" +
                "        40028957,\n" +
                "        40028959,\n" +
                "        40028961,\n" +
                "        40028962,\n" +
                "        40028964,\n" +
                "        40028966,\n" +
                "        40028968,\n" +
                "        40028969,\n" +
                "        40028971,\n" +
                "        40028972,\n" +
                "        40028973,\n" +
                "        40028974,\n" +
                "        40028975,\n" +
                "        40028976,\n" +
                "        40028977,\n" +
                "        40028978,\n" +
                "        40028979,\n" +
                "        40028980,\n" +
                "        40028981,\n" +
                "        40028982,\n" +
                "        40028984,\n" +
                "        40028985,\n" +
                "        40028986,\n" +
                "        40028987,\n" +
                "        40028988,\n" +
                "        40028989,\n" +
                "        40028991,\n" +
                "        40028992,\n" +
                "        40028845,\n" +
                "        40028851,\n" +
                "        40028857,\n" +
                "        40028863\n" +
                "      ],\n" +
                "      \"onlineTime\" : 1544239800000,\n" +
                "      \"endTime\" : 1544842800000,\n" +
                "      \"catgory\" : 1,\n" +
                "      \"name\" : \"2019省考万人模考行测-第六季\",\n" +
                "      \"year\" : 2019,\n" +
                "      \"type\" : 9,\n" +
                "      \"modules\" : [\n" +
                "        {\n" +
                "          \"name\" : \"第一部分 常识判断\",\n" +
                "          \"category\" : 1,\n" +
                "          \"qcount\" : 20\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\" : \"第二部分 言语理解与表达\",\n" +
                "          \"category\" : 2,\n" +
                "          \"qcount\" : 40\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\" : \"第三部分 数量关系\",\n" +
                "          \"category\" : 3,\n" +
                "          \"qcount\" : 10\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\" : \"第四部分 判断理解\",\n" +
                "          \"category\" : 4,\n" +
                "          \"qcount\" : 35\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\" : \"第五部分 资料分析\",\n" +
                "          \"category\" : 5,\n" +
                "          \"qcount\" : 20\n" +
                "        }\n" +
                "      ],\n" +
                "      \"questions\" : [\n" +
                "        40028909,\n" +
                "        40028910,\n" +
                "        40028911,\n" +
                "        40028912,\n" +
                "        40028913,\n" +
                "        40028914,\n" +
                "        40028915,\n" +
                "        40028916,\n" +
                "        40028917,\n" +
                "        40028918,\n" +
                "        40028919,\n" +
                "        40028920,\n" +
                "        40028921,\n" +
                "        40028922,\n" +
                "        40028923,\n" +
                "        40028924,\n" +
                "        40028925,\n" +
                "        40028926,\n" +
                "        40028927,\n" +
                "        40028928,\n" +
                "        40028869,\n" +
                "        40028870,\n" +
                "        40028871,\n" +
                "        40028872,\n" +
                "        40028873,\n" +
                "        40028874,\n" +
                "        40028875,\n" +
                "        40028876,\n" +
                "        40028877,\n" +
                "        40028878,\n" +
                "        40028879,\n" +
                "        40028880,\n" +
                "        40028881,\n" +
                "        40028882,\n" +
                "        40028883,\n" +
                "        40028884,\n" +
                "        40028885,\n" +
                "        40028886,\n" +
                "        40028887,\n" +
                "        40028888,\n" +
                "        40028889,\n" +
                "        40028890,\n" +
                "        40028891,\n" +
                "        40028892,\n" +
                "        40028893,\n" +
                "        40028894,\n" +
                "        40028895,\n" +
                "        40028896,\n" +
                "        40028897,\n" +
                "        40028898,\n" +
                "        40028899,\n" +
                "        40028900,\n" +
                "        40028901,\n" +
                "        40028902,\n" +
                "        40028903,\n" +
                "        40028904,\n" +
                "        40028905,\n" +
                "        40028906,\n" +
                "        40028907,\n" +
                "        40028908,\n" +
                "        40028835,\n" +
                "        40028836,\n" +
                "        40028837,\n" +
                "        40028838,\n" +
                "        40028839,\n" +
                "        40028840,\n" +
                "        40028841,\n" +
                "        40028842,\n" +
                "        40028843,\n" +
                "        40028844,\n" +
                "        40028946,\n" +
                "        40028947,\n" +
                "        40028948,\n" +
                "        40028951,\n" +
                "        40028954,\n" +
                "        40028955,\n" +
                "        40028956,\n" +
                "        40028957,\n" +
                "        40028959,\n" +
                "        40028961,\n" +
                "        40028962,\n" +
                "        40028964,\n" +
                "        40028966,\n" +
                "        40028968,\n" +
                "        40028969,\n" +
                "        40028971,\n" +
                "        40028972,\n" +
                "        40028973,\n" +
                "        40028974,\n" +
                "        40028975,\n" +
                "        40028976,\n" +
                "        40028977,\n" +
                "        40028978,\n" +
                "        40028979,\n" +
                "        40028980,\n" +
                "        40028981,\n" +
                "        40028982,\n" +
                "        40028984,\n" +
                "        40028985,\n" +
                "        40028986,\n" +
                "        40028987,\n" +
                "        40028988,\n" +
                "        40028989,\n" +
                "        40028991,\n" +
                "        40028992,\n" +
                "        40028846,\n" +
                "        40028847,\n" +
                "        40028848,\n" +
                "        40028849,\n" +
                "        40028850,\n" +
                "        40028852,\n" +
                "        40028853,\n" +
                "        40028854,\n" +
                "        40028855,\n" +
                "        40028856,\n" +
                "        40028858,\n" +
                "        40028859,\n" +
                "        40028860,\n" +
                "        40028861,\n" +
                "        40028862,\n" +
                "        40028864,\n" +
                "        40028865,\n" +
                "        40028866,\n" +
                "        40028867,\n" +
                "        40028868\n" +
                "      ],\n" +
                "      \"id\" : 4001081,\n" +
                "      \"hideFlag\" : 0,\n" +
                "      \"iconUrl\" : null,\n" +
                "      \"paperMeta\" : null,\n" +
                "      \"descrp\" : \"考试说明\\n1. 开考前5分钟可提前进入考场查看题目，开考30分钟后则无法报名和进入考试 。\\n2. 开始答题后不可暂停计时，如需完全退出可直接提交试卷;考试结束自动交卷。\\n3. 分享“报名成功”截图至微博并@华图在线官微，获得更多惊喜\",\n" +
                "      \"area\" : -9,\n" +
                "      \"qcount\" : 125,\n" +
                "      \"lookParseTime\" : 2,\n" +
                "      \"startTime\" : 1544835600000\n" +
                "    },\n" +
                "    \"cardCreateTime\" : 1544835365004,\n" +
                "    \"giftImgUrl\" : null,\n" +
                "    \"courseId\" : null,\n" +
                "    \"ucount\" : 0,\n" +
                "    \"userId\" : 234934290,\n" +
                "    \"speed\" : 1\n" +
                "  }";
        StandardCard answerCard = JSONObject.parseObject(value, StandardCard.class);
        log.info(JSONObject.toJSONString(answerCard));
        ValueOperations<String,String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set("temp_practice_cache_key", JSONObject.toJSONString(answerCard), 10, TimeUnit.DAYS);
    }

    @Test
    public void answerCardContent(){
        ValueOperations<String,String> valueOperations = stringRedisTemplate.opsForValue();
        String value = valueOperations.get("temp_practice_cache_key");
        StandardCard answerCard = JSONObject.parseObject(value, StandardCard.class);
        log.info(JSONObject.toJSONString(answerCard));
    }


}
