package com.ht.galaxy.common;

/**
 * @author gaoyuchao
 * @create 2018-08-06 9:43
 */
public class ClassPv {

    private String classId;
    private int pv1;
    private int uv1;
    private int pv2;
    private int uv2;
    private int pv3;
    private int uv3;
    private double pv_cvr12;
    private double uv_cvr12;
    private double pv_cvr23;
    private double uv_cvr23;

    public ClassPv() {
    }

    public ClassPv(String classId, int pv1, int uv1, int pv2, int uv2, int pv3, int uv3, double pv_cvr12, double uv_cvr12, double pv_cvr23, double uv_cvr23) {
        this.classId = classId;
        this.pv1 = pv1;
        this.uv1 = uv1;
        this.pv2 = pv2;
        this.uv2 = uv2;
        this.pv3 = pv3;
        this.uv3 = uv3;
        this.pv_cvr12 = pv_cvr12;
        this.uv_cvr12 = uv_cvr12;
        this.pv_cvr23 = pv_cvr23;
        this.uv_cvr23 = uv_cvr23;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public int getPv1() {
        return pv1;
    }

    public void setPv1(int pv1) {
        this.pv1 = pv1;
    }

    public int getUv1() {
        return uv1;
    }

    public void setUv1(int uv1) {
        this.uv1 = uv1;
    }

    public int getPv2() {
        return pv2;
    }

    public void setPv2(int pv2) {
        this.pv2 = pv2;
    }

    public int getUv2() {
        return uv2;
    }

    public void setUv2(int uv2) {
        this.uv2 = uv2;
    }

    public int getPv3() {
        return pv3;
    }

    public void setPv3(int pv3) {
        this.pv3 = pv3;
    }

    public int getUv3() {
        return uv3;
    }

    public void setUv3(int uv3) {
        this.uv3 = uv3;
    }

    public double getPv_cvr12() {
        return pv_cvr12;
    }

    public void setPv_cvr12(double pv_cvr12) {
        this.pv_cvr12 = pv_cvr12;
    }

    public double getUv_cvr12() {
        return uv_cvr12;
    }

    public void setUv_cvr12(double uv_cvr12) {
        this.uv_cvr12 = uv_cvr12;
    }

    public double getPv_cvr23() {
        return pv_cvr23;
    }

    public void setPv_cvr23(double pv_cvr23) {
        this.pv_cvr23 = pv_cvr23;
    }

    public double getUv_cvr23() {
        return uv_cvr23;
    }

    public void setUv_cvr23(double uv_cvr23) {
        this.uv_cvr23 = uv_cvr23;
    }
}
