package com.ewlab.a_cube.accessibilityservice.model;public class ActionButton extends Action {    private String deviceId = "", keyId = "";    public ActionButton(String name, String deviceId, String keyId) {        this.name = name;        this.deviceId = deviceId;        this.keyId = keyId;    }    public String getDeviceId() {        return deviceId;    }    public String getKeyId() {        return keyId;    }}