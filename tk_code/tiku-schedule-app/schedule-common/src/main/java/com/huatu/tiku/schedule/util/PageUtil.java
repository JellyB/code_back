package com.huatu.tiku.schedule.util;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PageUtil<T> {

	List<T> result;

	int next;

	long total;

	long totalPage;
}
