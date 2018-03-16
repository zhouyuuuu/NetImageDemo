package com.example.administrator.netimageapplication.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.example.administrator.netimageapplication.application.NetImageApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Edited by Administrator on 2018/3/15.
 */

public class DiskUtil {
    /**
     * 从硬盘中获取图片
     */
    public static Bitmap loadBitmap(@NonNull String url) {
        // 文件名为url的hashcode，因为存储时也是用hashcode作为文件名
        String fileName = String.valueOf(url.hashCode());
        String path = NetImageApplication.getApplication().getCacheDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        if (!file.exists()) {
            return null;
        } else {
            return BitmapFactory.decodeFile(path);
        }
    }

    /**
     * 将图片存入硬盘
     */
    public static void saveBitmap(@NonNull Bitmap bitmap, @NonNull String url) {
        // 用hashcode作为文件名可以避免url长度太大造成文件名过长
        String fileName = String.valueOf(url.hashCode());
        // 用默认缓存文件夹作为路径
        File f = new File(NetImageApplication.getApplication().getCacheDir().getAbsolutePath(), fileName);
        if (f.exists()) {
            boolean deleted = f.delete();
            if (!deleted) return;
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
