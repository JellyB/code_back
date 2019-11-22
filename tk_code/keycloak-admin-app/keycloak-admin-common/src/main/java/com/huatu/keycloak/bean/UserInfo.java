package com.huatu.keycloak.bean;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/10/18 17:40
 */
@Data
@Builder
public class UserInfo {
    private String id;
    private String username;
    private String name;
    private String email;
    private Boolean enabled;
    private Map<String,List<String>> clientRoles;
}
