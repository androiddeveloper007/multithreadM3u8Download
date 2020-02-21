package com.m3u8test.utils;

import android.text.TextUtils;

import java.io.File;

import static com.m3u8test.utils.AES128Utils.parseByte2HexStr;
import static com.m3u8test.utils.AES128Utils.parseHexStr2Byte;

/**
 * 创建日期：2017/11/27
 * 描    述: M3U8加密助手类
 */
public class M3U8EncryptHelper {
    public static void encryptFile(String key, String fileName) throws Exception {
        if (TextUtils.isEmpty(key)) return;
        byte[] bytes = AES128Utils.getAESEncode(key,MUtils.readFile(fileName));
        MUtils.saveFile(bytes, fileName);
    }

    public static void decryptFile(String key, String fileName) throws Exception {
        if (TextUtils.isEmpty(key)) return;
        byte[] bytes = AES128Utils.getAESDecode(key,MUtils.readFile(fileName));
        MUtils.saveFile(bytes, fileName);
    }

    public static String encryptFileName(String key, String str) throws Exception {
        if (TextUtils.isEmpty(key)) return str;
        str = parseByte2HexStr(AES128Utils.getAESEncode(key,str));
        return str;
    }

    public static String decryptFileName(String key, String str) throws Exception {
        if (TextUtils.isEmpty(key)) return str;
        str = new String(AES128Utils.getAESDecode(key,parseHexStr2Byte(str)));
        return str;
    }

    public static void encryptTsFilesName(String key, String dirPath) throws Exception {
        if (TextUtils.isEmpty(key)) return;
        File dirFile = new File(dirPath);
        if (dirFile.exists() && dirFile.isDirectory()){
            File[] files = dirFile.listFiles();
            for (int i = 0; i < files.length; i++) {// 遍历目录下所有的文件
                if (files[i].getName().contains("m3u8"))continue;
                File renameFile = new File(dirPath, encryptFileName(key, files[i].getName()));
                files[i].renameTo(renameFile);
            }
        }
    }

    public static void decryptTsFilesName(String key, String dirPath) throws Exception {
        if (TextUtils.isEmpty(key)) return ;
        File dirFile = new File(dirPath);
        if (dirFile.exists() && dirFile.isDirectory()){
            File[] files = dirFile.listFiles();
            for (int i = 0; i < files.length; i++) {// 遍历目录下所有的文件
                if (files[i].getName().contains("m3u8"))continue;
                File renameFile = new File(dirPath,decryptFileName(key, files[i].getName()));
                files[i].renameTo(renameFile);
            }
        }
    }
}
