package com.m3u8test.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 */
public class PasteTool {
    public static String getPasteStr(Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        String content = "";
        if(cm!=null) {
            ClipData data = cm.getPrimaryClip();
            ClipData.Item item = null;
            if (data != null) {
                item = data.getItemAt(0);
            }
            if (item != null) {
                content = item.getText().toString();
            }
        }
        return content;
    }
}
