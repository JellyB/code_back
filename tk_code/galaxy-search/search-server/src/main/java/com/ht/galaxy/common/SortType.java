package com.ht.galaxy.common;

import java.util.Map;

public class SortType {

    private int sum;
    private int independent;
    private Map<Integer,Double> map;

    public SortType() {
    }

    public SortType(int sum, int independent, Map<Integer, Double> map) {
        this.sum = sum;
        this.independent = independent;
        this.map = map;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getIndependent() {
        return independent;
    }

    public void setIndependent(int independent) {
        this.independent = independent;
    }

    public Map<Integer, Double> getMap() {
        return map;
    }

    public void setMap(Map<Integer, Double> map) {
        this.map = map;
    }
}
