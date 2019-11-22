package com.huatu.sensors.mq.message;

import java.io.Serializable;

import lombok.Data;

@Data
public class MockStartMessage implements Serializable {
    private static final long serialVersionUID = -4731326195678504565L;

    /**
     * ID
     */
    private int id;

    private String token;

    private int subject;
    
    private int terminal;
}
