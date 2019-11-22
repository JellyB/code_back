package com.huatu.tiku.course.service.exam;

import com.huatu.tiku.course.bean.NetSchoolResponse;

import java.util.HashMap;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/2/19
 * @描述 备考精华相关
 */

public interface ExamService {

    Object getArticleList(int type, int page, int pageSize, int category);

    Object detail(int id);

    List<HashMap<String, Object>> typeList();

    NetSchoolResponse like(HashMap map);


}
