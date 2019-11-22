package com.ht.base.start.security.service;

import com.ht.base.start.JbzmNB;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class UserOptionTest extends JbzmNB {

    @Autowired
    private UserOption userOption;

    @Test
    public void getUserByMenus() {
        userOption.getUserByMenus(new Long[]{3L}).forEach(user -> {
            log.info("User name is {}", user.getName());
        });
    }

}