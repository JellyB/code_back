package com.huatu.hadoop.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseWareDTO {

    private int questionSource;  //试题来源		int		课后题：1，课中题：2
    private long coursewareId;      // 	课件id	        long			//数量关系
    private int coursewareType;      //课件类型   	int		1直播 2录播 3回放

    private String accuracy;  //correct=|error=
}
