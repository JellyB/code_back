package com.huatu.tiku.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 处理不同科目下 试卷的展示字段不同的问题，此处统一做全下配置，需要配合数据库中的一级节点ID使用
 * Created by lijun on 2018/8/6
 */
@AllArgsConstructor
@Getter
public enum PaperPermission {


    //是否是特岗教师
    SPECIAL_Flag(1 << 0, "SPECIAL_FLAG"),
    //学段
    GRADE_FLAG(1 << 2, "GRADE_FLAG");
    private Integer code;
    private String permission;

    public static final HashMap<Long, Integer> PAPER_PERMISSION_CACHE = new HashMap<>();

    private static final List<String> getAllPermissionByCode(long code) {
        return Stream.of(PaperPermission.values())
                .filter(paperPermission -> (paperPermission.getCode() & code) > 0)
                .map(PaperPermission::getPermission)
                .collect(Collectors.toList());
    }

    /**
     * 计算 code 值
     */
    private static final int getTotalCode(PaperPermission... permissions) {
        if (null == permissions || permissions.length == 0) {
            return 0;
        }
        return Arrays.stream(permissions)
                .mapToInt(PaperPermission::getCode)
                .sum();
    }

    /**
     * 初始化功能模块信息
     */
    private static final void initCacheInfo() {
        if (PAPER_PERMISSION_CACHE.size() == 0) {
            synchronized (PAPER_PERMISSION_CACHE) {
                //事业单位
                PAPER_PERMISSION_CACHE.put(3L, getTotalCode());

                //教师资格证-教综
                PAPER_PERMISSION_CACHE.put(1000003L, getTotalCode());
                //综合素质--小学
                PAPER_PERMISSION_CACHE.put(100100587L, getTotalCode(GRADE_FLAG));
                //综合素质--中学
                PAPER_PERMISSION_CACHE.put(100100589L, getTotalCode(GRADE_FLAG));
                //教育知识和能力--小学
                PAPER_PERMISSION_CACHE.put(100100597L, getTotalCode(GRADE_FLAG));
                //教育知识和能力--中学
                PAPER_PERMISSION_CACHE.put(100100598L, getTotalCode(GRADE_FLAG));
                //只有招教有是否是特岗教师的标签，其中教综无学段
                //教师招聘-教综
                PAPER_PERMISSION_CACHE.put(100100262L, getTotalCode(SPECIAL_Flag));
                //教师招聘-语文
                PAPER_PERMISSION_CACHE.put(100100615L, getTotalCode(SPECIAL_Flag, GRADE_FLAG));


            }
        }
    }

    /**
     * 获取某个科目下对应的权限信息
     *
     * @param subjectId 科目ID
     */
    public static final List<String> getAllPermissionBySubjectId(long subjectId) {
        initCacheInfo();
        int permissionCode = PAPER_PERMISSION_CACHE.getOrDefault(subjectId, -1);
        if (permissionCode > 0) {
            return getAllPermissionByCode(permissionCode);
        }
        return Lists.newArrayList();
    }

}
