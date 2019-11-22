package com.huatu.tiku.interview.spring.conf.web;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by x6 on 2018/4/8.
 */
@Data
@Builder
public class AdminInfo implements UserDetails {

    private Long id;
    private Date createTime;
    private Integer status;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> grantedAuthorities;

        
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.of("lol").map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

        
        
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
