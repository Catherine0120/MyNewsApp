package com.java.zhangshiying;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class News {
    String newsID = "";

    public String title, category, origin, time;
    public List<String> imageUrls,videoUrls;
    boolean imageExist = false;
    boolean videoExist = false;
    int imageCount = 0;

    ArrayList<Bitmap> images = new ArrayList<>();

    boolean fav = false;
    boolean like = false;
    boolean read = false;

    int pos = -1;

    public String content;

    News(JSONObject news) {
        try {
            this.newsID = news.getString("newsID");
            if (this.newsID.equals("")) {
                this.newsID = getRandomString(44);
                System.out.println("[News()]: randomly create a newsID");
            }
            this.title = news.getString("title");
            this.category = news.getString("category");
            this.origin = news.getString("publisher");
            this.time = news.getString("publishTime");
            this.content = news.getString("content");
            this.imageUrls = getImage(news);
            this.videoUrls = getVideo(news);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<String> getVideo(JSONObject news) {
        List<String> urls = new ArrayList<>();
        try {
            String videoUrls = news.getString("video");
            if (videoUrls.length() == 0) return urls;
            videoUrls = videoUrls.substring(1, videoUrls.length() - 1);
            if (videoUrls.length() == 0) return urls;
            else {
                List<String> urlList = Arrays.asList(videoUrls.split(", "));
                for (String str : urlList) { if (str.length() != 0) urls.add(str);}
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (urls.size() != 0) videoExist = true;
        return urls;
    }

    private List<String> getImage(JSONObject news) {
        List<String> urls = new ArrayList<>();
        try {
            String imageUrls = news.getString("image");
            if (imageUrls.length() == 0) return urls;
            imageUrls = imageUrls.substring(1, imageUrls.length() - 1);
            if (imageUrls.length() == 0) return urls;
            else {
                List<String> urlList = Arrays.asList(imageUrls.split(", "));
                for (String str : urlList) { if (!Objects.equals(str, "")) urls.add(str);}
                imageCount = urls.size();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (urls.size() != 0) imageExist = true;
        return urls;
    }

    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
