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
    public static Bitmap loadBitmap(@NonNull String url){
        String fileName = String.valueOf(url.hashCode());
        String path = NetImageApplication.getApplication().getCacheDir().getAbsolutePath()+"/"+fileName;
        LogUtil.e("disk",path);
        File file = new File(path);
        if (!file.exists()){
            return null;
        }else {
            return BitmapFactory.decodeFile(path);
        }
    }

    public static void saveBitmap(@NonNull Bitmap bitmap,@NonNull String url){
        String fileName = String.valueOf(url.hashCode());
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
        }finally {
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
