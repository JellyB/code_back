package com.huatu.tiku.springboot.basic.subject;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.utils.recursion.RecursionUtils;
import com.huatu.tiku.springboot.basic.support.ConfigSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.huatu.common.consts.ApolloConfigConsts.NAMESPACE_TIKU_BASIC;

/**
 * 支持在线更新的科目服务（不要修改已经定义过的id,可能引起数据问题）
 * @author hanchao
 * @date 2017/10/6 10:35
 */
@Slf4j
public class SubjectService implements ConfigSubscriber {
    private static List<Subject> subjectList = Lists.newArrayList();
    //根级节点（业务上，根节点才是subject，上层可能是定义节点或考试类型,只是再表现形式上统一称为）
    private static Map<Integer,Subject> subjectMapping = Maps.newHashMap();

    private static volatile String subjectConfigSign = "";
    /**
     * 使用配置
     * @param config
     */
    public SubjectService(String config) throws IOException {
        if(StringUtils.isEmpty(config)){
            throw new IllegalArgumentException("basic info cant be empty");
        }
        load(config);
    }

    /**
     * 订阅配置改变的事件
     * @param configChange
     */
    @Override
    public void update(ConfigChange configChange) {
        log.debug(">>> subject changed,oldValue:{} -> newValue:{}",configChange.getOldValue(),configChange.getNewValue());
        String sign = DigestUtils.md5Hex(configChange.getNewValue());
        if(Objects.equals(sign, subjectConfigSign)){
            log.debug(">>> subjects not really changed!!!");
            return;
        }
        try {
            load(configChange.getNewValue());
        } catch (IOException e) {
            log.error("",e);
        }
    }

    @Override
    public String key() {
        return "tiku.basic.subjects";
    }

    @Override
    public String namespace() {
        return NAMESPACE_TIKU_BASIC;
    }


    private void load(String config) throws IOException {
        log.debug(">>> load subjects from config...");
        //
        ObjectMapper objectMapper = new ObjectMapper();
        List<Subject> subjectListTemp = Lists.newArrayList();
        Map<Integer,Subject> subjectMappingTemp = Maps.newHashMap();

        List<Subject> tops = objectMapper.readValue(config,new TypeReference<List<Subject>>(){});


        for (Subject subject : tops) {
            //递归组装
            RecursionUtils.collect(subject,(t) -> {
                subjectListTemp.add(t);
                List<Subject> childrens = t.getChildrens();
                if(CollectionUtils.isEmpty(childrens)){
                    subjectMappingTemp.put(t.getId(),t);
                }else{
                    for (Subject children : childrens) {
                        children.setParent(t);
                    }
                }
                return childrens;
            });
        }

        //subjectMappingTemp = subjectListTemp.stream().collect(Collectors.toMap(Subject::getId, Function.identity()));

        //直接修改引用地址，避免线程安全问题
        synchronized (this){
            subjectList = subjectListTemp;
            subjectMapping = subjectMappingTemp;
            subjectConfigSign = DigestUtils.md5Hex(config);
        }
    }



    /**
     * 返回subject顶级节点，不存在返回-1
     * @param subjectId
     * @return
     */
    public int top(int subjectId){
        Subject subject = subjectMapping.get(subjectId);
        if(subject == null){
            return -1;
        }
        Subject top = RecursionUtils.deal(subject, (s) -> {
            s.getId();
            return s.getParent();
        });
        return top.getId();
    }


    /**
     * 返回科目信息
     */
    public Subject get(int subjectId){
        return subjectMapping.get(subjectId);
    }


    /**
     * 获取所有的科目
     * @return
     */
    public List<Subject> total(){
        //返回副本
        return Lists.newArrayList(subjectList);
    }

}
