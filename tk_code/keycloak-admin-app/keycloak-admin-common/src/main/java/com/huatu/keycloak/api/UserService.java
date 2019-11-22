package com.huatu.keycloak.api;

import com.huatu.keycloak.bean.UserInfo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.huatu.keycloak.consts.KeyCloakConsts.KEYCLOAK_ADMIN_SERVICE;

/**
 * @author hanchao
 * @date 2017/10/19 17:34
 */
@FeignClient(KEYCLOAK_ADMIN_SERVICE)
public interface UserService {
    List<UserInfo> search(@RequestParam String search,@RequestParam int firstResult,@RequestParam int onePageSize);
    UserInfo get(@RequestParam String id);
}
