package com.live2d.live2dsimple;

public class LSImpleModel {
    private String modelName;
    private float center_x;
    private float y;
    private String userId;
    private RealFaceBean faceBean;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public float getCenter_x() {
        return center_x;
    }

    public void setCenter_x(float center_x) {
        this.center_x = center_x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public RealFaceBean getFaceBean() {
        return faceBean;
    }

    public void setFaceBean(RealFaceBean faceBean) {
        this.faceBean = faceBean;
    }
}
