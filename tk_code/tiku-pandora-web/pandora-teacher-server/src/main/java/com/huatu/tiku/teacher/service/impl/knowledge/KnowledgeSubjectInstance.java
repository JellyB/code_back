package com.huatu.tiku.teacher.service.impl.knowledge;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 事业单位 科目信息转换类
 * Created by lijun on 2019/2/14
 */
@Component
public final class KnowledgeSubjectInstance {

    @Value("${spring.profiles}")
    private String env;

    /**
     * 获取默认的可选取数组
     */
    public List<Long> getBaseSubjectList() {
        //本地、测试
        if (env.equals("dev") || env.equals("test")) {
            return Lists.newArrayList(3L);
        }
        //线上
        if (env.equals("product")) {
            return Lists.newArrayList(4L);
        }
        return Lists.newArrayList();
    }

    /**
     * 把子类转换成科目
     */
    public Long transChildrenSubjectToBase(Long childrenSubject) {
        //本地、测试
        if (env.equals("dev") || env.equals("test")) {
            if (childrenSubject.equals(200100054L) || childrenSubject.equals(200100055L) || childrenSubject.equals(200100056L)
                    || childrenSubject.equals(200100057L)) {
                return 3L;
            }

        }
        //线上
        if (env.equals("product")) {
            if (childrenSubject.equals(200100054L) || childrenSubject.equals(200100055L) || childrenSubject.equals(200100056L)
                    || childrenSubject.equals(200100057L)) {
                return 4L;
            }
        }
        return childrenSubject;
    }

    @PostConstruct
    private void init() {
        Instance.INSTANCE = this;
        Instance.INSTANCE.env = env;
    }

    public static final KnowledgeSubjectInstance getInstance() {
        return Instance.INSTANCE;
    }

    private static class Instance {
        private static KnowledgeSubjectInstance INSTANCE = new KnowledgeSubjectInstance();
    }
}
