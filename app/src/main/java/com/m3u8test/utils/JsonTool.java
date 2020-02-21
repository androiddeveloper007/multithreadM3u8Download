package com.m3u8test.utils;

import com.m3u8test.bean.SpBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonTool {
    public static String SpBean2Json(List<SpBean> items) throws JSONException {
        if (items == null) return "";
        JSONArray array = new JSONArray();
        JSONObject jsonObject = null;
        SpBean info = null;
        for (int i = 0; i < items.size(); i++) {
            info = items.get(i);
            jsonObject = new JSONObject();
            jsonObject.put("url", info.getUrl());
            jsonObject.put("name", info.getName());
            array.put(jsonObject);
        }
        return array.toString();
    }

    public static List<SpBean> Json2SpBean(String data) throws JSONException {
        List<SpBean> items = new ArrayList<>();
        if (data.equals("")) return items;

        JSONArray array = new JSONArray(data);
        JSONObject object = null;
        SpBean item = null;
        for (int i = 0; i < array.length(); i++) {
            object = array.getJSONObject(i);
            String url = object.getString("url");
            String name = object.getString("name");
            item = new SpBean(url, name);
            items.add(item);
        }
        return items;
    }
}
