package com.m3u8test.bean;

public class SpBean {
    String url;
    String name;

    public SpBean(String key, String value) {
        this.url=key;
        this.name=value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
