package com.example.administrator.netimageapplication.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.administrator.netimageapplication.bean.ImageInfo;
import com.example.administrator.netimageapplication.view.PercentProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/15.
 */

public class NetUtil {
    // 默认请求方法，DEMO中只要用到GET
    private static final java.lang.String HTTP_METHOD_GET = "GET";
    // 请求超时默认时间10秒
    private static final int REQUEST_TIMEOUT = 5000;
    // 干货集中营提供的一百张图片的api
    private static final java.lang.String resource = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/100/1";
    // 缩略图后缀，原图url后面加上该参数则为缩略图url
    private static final java.lang.String THUMBNAIL_PARAM = "?imageView2/0/w/300";
    // 返回的JSON数据中图片数据对应的key
    private static final java.lang.String KEY_RESULT = "results";
    // 图片数据中url对应的key
    private static final java.lang.String KEY_URL = "url";

    /**
     * 加载图片
     */
    public static Bitmap loadBitmap(java.lang.String path, ProgressListener listener, PercentProgressBar percentProgressBar) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 设置网络连接超时
            connection.setConnectTimeout(REQUEST_TIMEOUT);
            // 设置读取数据超时
            connection.setReadTimeout(REQUEST_TIMEOUT);
            // 设置是否从httpUrlConnection读入，GET时设置为true;
            connection.setDoInput(true);
            // 设置是否从httpUrlConnection读入，GET时设置为false;
            connection.setDoOutput(false);
            // 请求方式为GET
            connection.setRequestMethod(HTTP_METHOD_GET);
            // 使用缓存
            connection.setUseCaches(true);
            connection.connect();
            // 如果没连接成功则返回空
            if (connection.getResponseCode() != 200) {
                return null;
            }
            // 获得文件总大小
            int totalLength = connection.getContentLength();
            // 已经下载的大小
            int alreadyLength = 0;
            InputStream is = connection.getInputStream();
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            // 1kb的buffer
            byte[] buff = new byte[1024];
            int len;
            while ((len = is.read(buff)) != -1) {
                arrayOutputStream.write(buff, 0, len);
                alreadyLength += len;
                // 仅在下载原图时回调进度更新监听器
                if (listener != null) {
                    listener.onProgressUpdate((int) (alreadyLength * 1.0f / totalLength * 100),percentProgressBar);
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

    /**
     * 加载图片数据
     */
    public static ArrayList<ArrayList<ImageInfo>> loadImageInfo() throws IOException, JSONException {
        ArrayList<ArrayList<ImageInfo>> imageInfos = new ArrayList<>();
        URL url = new URL(resource);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(REQUEST_TIMEOUT);
        connection.setReadTimeout(REQUEST_TIMEOUT);
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setRequestMethod(HTTP_METHOD_GET);
        connection.setUseCaches(true);
        connection.connect();
        // 如果没连接成功则返回空
        if (connection.getResponseCode() != 200) {
            return null;
        }
        StringBuilder resultBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String tempLine;
        // 一行一行写进builder里
        while ((tempLine = reader.readLine()) != null) {
            resultBuilder.append(tempLine);
        }
        reader.close();
        String result = resultBuilder.toString();
        JSONObject jsonObject = new JSONObject(result);
        JSONArray imageJsonArray = jsonObject.getJSONArray(KEY_RESULT);
        ArrayList<ImageInfo> group = null;
        for (int i = 0; i < imageJsonArray.length(); i++) {
            JSONObject imageJson = imageJsonArray.getJSONObject(i);
            // 拿到原图Url
            String originalImageUrl = imageJson.getString(KEY_URL);
            // 拿到缩略图Url
            String thumbnailUrl = originalImageUrl + THUMBNAIL_PARAM;
            // new一个ImageInfo对象把URL传进去
            ImageInfo imageInfo = new ImageInfo(ImageInfo.ITEM_TYPE_SUB_ITEM, thumbnailUrl, originalImageUrl);
            if (i % 10 == 0) {
                // 按每10张图片分为新的一组，每组的第一个位置添加一张封面，封面为10张图片中的第一张
                group = new ArrayList<>();
                imageInfos.add(group);
                // 先复制第一张图片作为封面添加进group中
                ImageInfo coverImageInfo = new ImageInfo(ImageInfo.ITEM_TYPE_ITEM, thumbnailUrl, originalImageUrl);
                group.add(coverImageInfo);
            }
            if (group != null) {
                // 将图片数据添加到group
                group.add(imageInfo);
            }
        }
        return imageInfos;
    }

    /**
     * 进度监听器
     */
    public interface ProgressListener {
        void onProgressUpdate(int percent, PercentProgressBar percentProgressBar);
    }
}
