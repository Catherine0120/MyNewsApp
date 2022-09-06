package com.java.zhangshiying;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class News {
    String newsID = "";

    public String title, category, origin, time;
    public String content;
    public List<String> imageUrls,videoUrls;
    boolean imageExist = false;
    boolean videoExist = false;
    int imageCount = 0;

    ArrayList<String> images = new ArrayList<>();

    boolean fav = false;
    boolean like = false;
    boolean read = false;
    boolean readDetail = false;

    News(JSONObject news) {
        try {
            this.newsID = news.getString("newsID");
            if (this.newsID.equals("")) {
                this.newsID = getRandomString(44);
            }
            News news_tmp = newsInHistory(newsID);
            if (news_tmp != null) {
                this.fav = news_tmp.fav;
                this.like = news_tmp.like;
                this.read = true;
                this.readDetail = true;
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

    private News newsInHistory(String newsID) {
        return Storage.findNewsValue(GlobalApplication.getAppContext(), newsID);
    }

    private List<String> getVideo(JSONObject news) {
        List<String> urls = new ArrayList<>();
        try {
            String videoUrls = news.getString("video");
            if (videoUrls.length() == 0) return urls;
            videoUrls = videoUrls.substring(1, videoUrls.length() - 1);
            if (videoUrls.length() == 0) return urls;
            else {
                videoUrls = videoUrls.replaceAll("\\] \\[", ", ");
                List<String> urlList = Arrays.asList(videoUrls.split(", "));
                for (String str : urlList) { if (str.length() != 0) urls.add(str);}
                urls = deduplicate(urls);
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
                imageUrls = imageUrls.replaceAll("\\] \\[", ", ");
                List<String> urlList = Arrays.asList(imageUrls.split(", "));
                for (String str : urlList) { if (!Objects.equals(str, "")) urls.add(str);}
                urls = deduplicate(urls);
                imageCount = urls.size();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (urls.size() != 0) imageExist = true;
        return urls;
    }

    private List<String> deduplicate(List<String> urlList) {
        Set set = new HashSet();
        List<String> listNew = new ArrayList<String>();
        set.addAll(urlList);
        listNew.addAll(set);
        return listNew;
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
