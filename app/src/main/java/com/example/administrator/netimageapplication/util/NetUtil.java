package com.example.administrator.netimageapplication.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.administrator.netimageapplication.Bean.ImageInfo;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/15.
 */

public class NetUtil {
    private static final java.lang.String HTTP_METHOD_GET = "GET";
    private static final int REQUEST_TIMEOUT = 50000;
    private static final java.lang.String resource = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/100/1";
    private static final java.lang.String THUMBNAIL_PARAM = "?imageView2/0/w/100";
    private static final java.lang.String KEY_RESULT = "results";
    private static final java.lang.String KEY_URL = "url";

    public static Bitmap loadBitmap(java.lang.String path, ProgressListener listener) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(REQUEST_TIMEOUT);//设置网络连接超时
            connection.setReadTimeout(REQUEST_TIMEOUT);//设置读取数据超时
            connection.setDoInput(true);//设置是否从httpUrlConnection读入，默认情况下是true;
            connection.setDoOutput(false);
            connection.setRequestMethod(HTTP_METHOD_GET);
            connection.setUseCaches(true);
            connection.connect();
            int totalLength = connection.getContentLength();
            int alreadyLength = 0;
            InputStream is = connection.getInputStream();
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len;
            while ((len = is.read(buff)) != -1) {
                arrayOutputStream.write(buff, 0, len);
                alreadyLength += len;
                if (listener != null) {
                    listener.onProgressUpdate((int) (alreadyLength*1.0f / totalLength * 100));
                }
            }
            is.close();
            arrayOutputStream.close();
            byte[] bytes = arrayOutputStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static ArrayList<ArrayList<ImageInfo>> loadImageInfo() {
        ArrayList<ArrayList<ImageInfo>> imageInfos = new ArrayList<>();
        try {
            URL url = new URL(resource);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(REQUEST_TIMEOUT);//设置网络连接超时
            connection.setReadTimeout(REQUEST_TIMEOUT);//设置读取数据超时
            connection.setDoInput(true);//设置是否从httpUrlConnection读入，默认情况下是true;
            connection.setDoOutput(false);
            connection.setRequestMethod(HTTP_METHOD_GET);
            connection.setUseCaches(true);
            connection.connect();
            StringBuilder resultBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            java.lang.String tempLine;
            while ((tempLine = reader.readLine()) != null) {
                resultBuilder.append(tempLine);
            }
            //System.out.println(resultBuffer);
            reader.close();
            java.lang.String result = resultBuilder.toString();
            JSONObject jsonObject = new JSONObject(result);
            JSONArray imageJsonArray = jsonObject.getJSONArray(KEY_RESULT);
            ArrayList<ImageInfo> group = null;
            for (int i=0;i<imageJsonArray.length();i++) {
                JSONObject imageJson = imageJsonArray.getJSONObject(i);
                java.lang.String originalImageUrl = imageJson.getString(KEY_URL);
                java.lang.String thumbnailUrl = originalImageUrl + THUMBNAIL_PARAM;
                ImageInfo imageInfo = new ImageInfo(ImageInfo.ITEM_TYPE_SUB_ITEM,thumbnailUrl,originalImageUrl);
                if (i%10==0){
                    group = new ArrayList<>();
                    imageInfos.add(group);
                    ImageInfo coverImageInfo = new ImageInfo(ImageInfo.ITEM_TYPE_ITEM,thumbnailUrl,originalImageUrl);
                    group.add(coverImageInfo);
                }
                if (group != null) {
                    group.add(imageInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageInfos;
    }

    public interface ProgressListener {
        void onProgressUpdate(int percent);
    }
}
