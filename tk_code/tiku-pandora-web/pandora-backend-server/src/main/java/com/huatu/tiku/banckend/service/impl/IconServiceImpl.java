package com.huatu.tiku.banckend.service.impl;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.consts.ApolloConfigConsts;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.banckend.dao.manual.IconMapper;
import com.huatu.tiku.banckend.service.IconService;
import com.huatu.tiku.dto.request.IconDto;
import com.huatu.tiku.entity.Icon;
import com.huatu.tiku.springboot.basic.support.ConfigSubscriber;
import com.huatu.tiku.teacher.enums.BizStatusEnum;
import com.huatu.tiku.teacher.enums.StatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-29 2:14 PM
 **/

@Slf4j
public class IconServiceImpl implements IconService, ConfigSubscriber{

    @Autowired
    private IconMapper iconMapper;

    private static volatile String configSign = "";
    private static volatile List<Icon> defaultIcons = null;

    private static final int DEFAULT_COUNT = 4;

    public IconServiceImpl(String config) throws IOException {
        if(Strings.isNullOrEmpty(config)){
            throw new IllegalArgumentException("icon 默认配置不能为空!!");
        }
        load(config);
    }

    @Override
    public boolean notifyOnReady() {
        return false;
    }

    @Override
    public String key() {
        return "default.icons";
    }

    @Override
    public String namespace() {
        return ApolloConfigConsts.NAMESPACE_DEFAULT;
    }

    @Override
    public void update(ConfigChange configChange) {
        log.info("pandora 默认 icon 配置被更新: oldValue:{} -> newValue:{}", configChange.getOldValue(), configChange.getNewValue());
        String sing = DigestUtils.md5Hex(configChange.getNewValue());
        if(Objects.equals(sing, configSign)){
            log.error("icon not really changed!!");
            return;
        }
        try{
            load(configChange.getNewValue());
        }catch (IOException e){
            log.error("", e);
        }
    }

    private void load(String sign) throws IOException{
        log.debug("load icon setting from config...");
        ObjectMapper objectMapper = new ObjectMapper();
        List<Icon> iconList = objectMapper.readValue(sign, new TypeReference<List<Icon>>(){});
        synchronized (this){
            configSign = DigestUtils.md5Hex(sign);
            defaultIcons = iconList;
        }
    }



    @Override
    public int add(int subject, List<IconDto> icons) {
        if(CollectionUtils.isEmpty(icons)){
            throw new BizException(ErrorResult.create(100010, "数据不能为空"));
        }
        boolean exist = icons.stream().anyMatch(item-> null != item.getId() && item.getId() > 0);
        if(exist){
            throw new BizException(ErrorResult.create(100010, "数据异常"));
        }
        int count = 0;
        for(IconDto iconDto : icons){
            Icon icon = new Icon();
            icon.setSubject(subject);
            icon.setName(StringUtils.isEmpty(iconDto.getName()) ? iconDto.getType().getName() : iconDto.getName());
            icon.setType(iconDto.getType().getType());
            icon.setSort(iconDto.getSort());
            icon.setUrl(StringUtils.isEmpty(iconDto.getUrl()) ? iconDto.getType().getUrl() : iconDto.getUrl());
            icon.setInfo(iconDto.getInfo());
            icon.setGmtCreate(new Timestamp(System.currentTimeMillis()));
            icon.setGmtModify(new Timestamp(System.currentTimeMillis()));
            icon.setStatus(StatusEnum.NORMAL.getValue());
            icon.setBizStatus(BizStatusEnum.NO_PUBLISH.getValue());
            iconMapper.insert(icon);
            count ++;
        }
        return count;
    }


    @Override
    public int update(int subject, IconDto iconDto) throws BizException {
        if(iconDto.getId() == null || 0 == iconDto.getId().intValue()){
            throw new BizException(ErrorResult.create(100013, "id不能为空"));
        }
        Icon icon = iconMapper.selectByPrimaryKey(iconDto.getId());
        if(null == icon){
            throw new BizException(ErrorResult.create(100014, "数据不存在"));
        }
        if(!iconDto.getSort().equals(icon.getSort())){
            icon.setSort(iconDto.getSort());
            icon.setGmtModify(new Timestamp(System.currentTimeMillis()));
        }
        if(!iconDto.getUrl().equals(icon.getUrl())){
            icon.setUrl(iconDto.getUrl());
            icon.setGmtModify(new Timestamp(System.currentTimeMillis()));
        }
        if(StringUtils.isNotEmpty(iconDto.getInfo())){
            icon.setInfo(iconDto.getInfo());
            icon.setGmtModify(new Timestamp(System.currentTimeMillis()));
        }
        if(null != iconDto.getBizStatus() && !iconDto.getBizStatus().equals(icon.getBizStatus())){
            icon.setBizStatus(iconDto.getBizStatus());
            icon.setGmtModify(new Timestamp(System.currentTimeMillis()));
        }
        if(StringUtils.isNotEmpty(iconDto.getName()) && !iconDto.getName().equals(icon.getName())){
            icon.setName(iconDto.getName());
            icon.setGmtModify(new Timestamp(System.currentTimeMillis()));
        }
        return iconMapper.updateByPrimaryKeySelective(icon);
    }

    @Override
    public List<IconDto> list(int subject) {
        if(null == defaultIcons){
            throw new BizException(ErrorResult.create(100010, "请先配置默认配置"));
        }
        List<IconDto> result = Lists.newArrayList();
        Example example = new Example(Icon.class);
        example.and().andEqualTo("subject", subject)
                .andEqualTo("status", StatusEnum.NORMAL.getValue());

        example.orderBy("sort").asc();
        List<Icon> list = iconMapper.selectByExample(example);
        //如果当前科目为空 新增数据保存到数据库中并返回
        if(CollectionUtils.isEmpty(list)){
            defaultIcons.stream().forEach(item -> {
                Icon icon = new Icon(subject, item.getType(), item.getName(), item.getUrl(), item.getSort());
                icon.setBizStatus(BizStatusEnum.PUBLISH.getValue());
                icon.setStatus(StatusEnum.NORMAL.getValue());
                icon.setGmtCreate(new Timestamp(System.currentTimeMillis()));
                icon.setGmtModify(new Timestamp(System.currentTimeMillis()));
                iconMapper.insert(icon);
                list.add(icon);
            });
        }
        list.forEach(icon ->{
            IconDto iconDto = new IconDto(icon.getId(), icon.getType(), icon.getName(), icon.getUrl(), icon.getInfo(), icon.getSort(), icon.getBizStatus() == null ? BizStatusEnum.NO_PUBLISH.getValue() : icon.getBizStatus());
            result.add(iconDto);
        });
        return result;
    }

    @Override
    public int turn(Long id, int bizStatus) throws BizException {
        Icon icon = iconMapper.selectByPrimaryKey(id);
        Integer subject = icon.getSubject();
        if(BizStatusEnum.NO_PUBLISH.getValue() == bizStatus){
            Example example = new Example(Icon.class);
            example.and().andEqualTo("subject", subject)
                    .andEqualTo("status", StatusEnum.NORMAL.getValue())
                    .andEqualTo("bizStatus", BizStatusEnum.PUBLISH.getValue());

            int count = iconMapper.selectCountByExample(example);
            if(count == DEFAULT_COUNT){
                throw new BizException(ErrorResult.create(100010, "默认开启不能少于4个"));
            }
        }

        if(null == icon){
            throw new BizException(ErrorResult.create(1000103, "数据不存在"));
        }
        Icon record = new Icon();
        record.setId(id);
        record.setBizStatus(bizStatus);
        record.setGmtModify(new Timestamp(System.currentTimeMillis()));
        return iconMapper.updateByPrimaryKeySelective(record);
    }
}
