package com.ht.galaxy.common;

import java.util.List;

/**
 * @author gaoyuchao
 * @create 2018-07-04 17:06
 */
public class Type {

    private String type;
    private String symbol;
    private List<String> values;

    public Type() {
    }

    public Type(String type, String symbol, List<String> values) {
        this.type = type;
        this.symbol = symbol;
        this.values = values;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
