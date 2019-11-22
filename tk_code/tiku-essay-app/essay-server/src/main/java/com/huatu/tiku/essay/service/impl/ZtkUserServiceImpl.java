package com.huatu.tiku.essay.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.huatu.tiku.essay.service.ZtkUserService;
import com.huatu.tiku.essay.util.ResponseMsg;
import com.huatu.tiku.essay.vo.user.ZtkUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ZtkUserServiceImpl implements ZtkUserService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${userService.baseUrl}")
    private String userServiceBaseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ZtkUserVO getById(Integer id) {
        List<ZtkUserVO> ztkUserVOS = getByIds(Lists.newArrayList(id));

        return CollectionUtils.isEmpty(ztkUserVOS) ? null : ztkUserVOS.get(0);
    }

    @Override
    public List<ZtkUserVO> getByIds(List<Integer> ids) {
        String responseContent = restTemplate.postForObject(userServiceBaseUrl + "/v1/users/batchUserInfo", ids, String.class);

        try {
            ResponseMsg<List<ZtkUserVO>> responseMsg = objectMapper.readValue(responseContent, new TypeReference<ResponseMsg<List<ZtkUserVO>>>() {
            });

            return responseMsg.getData();
        } catch (IOException e) {
            log.error("ZtkUserService.getByIds", e);
            return null;
        }
    }

    @Override
    public ZtkUserVO getByUsernameOrderMobile(String params) {
        List<ZtkUserVO> ztkUserVOS = getByUsernameOrderMobiles(Lists.newArrayList(params));

        return CollectionUtils.isEmpty(ztkUserVOS) ? null : ztkUserVOS.get(0);
    }

    @Override
    public List<ZtkUserVO> getByUsernameOrderMobiles(List<String> params) {
        String responseContent = restTemplate.postForObject(userServiceBaseUrl + "/v1/users/batchUserInfoByUsernameOrMobile", params, String.class);

        try {
            ResponseMsg<List<ZtkUserVO>> responseMsg = objectMapper.readValue(responseContent, new TypeReference<ResponseMsg<List<ZtkUserVO>>>() {
            });

            return responseMsg.getData();
        } catch (IOException e) {
            log.error("ZtkUserService.getByUsernameOrderMobiles", e);
            return null;
        }
    }
}
