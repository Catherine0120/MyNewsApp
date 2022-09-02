package com.java.zhangshiying;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class Storage {
    private static final String SP_NAME = "storage";

    public static boolean write(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static boolean remove(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        return editor.commit();
    }

    public static boolean clear(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        return editor.commit();
    }

    public static boolean contains(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return prefs.contains(key);
    }

    public static String findValue(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public static String[] getValues(String value) {
        return value.split("&&&");
    }

    public static String joinNewsList(ArrayList<News> newsList) {
        ArrayList<String> newsStrings = new ArrayList<>();
        Gson gson = new Gson();
        for (News news : newsList) {
            newsStrings.add(gson.toJson(news));
        }
        return String.join("&&&", newsStrings);
    }

    public static ArrayList<News> parseNewsList(String msg) {
        ArrayList<News> historyNewsList = new ArrayList<>();
        String[] newsList = getValues(msg);
        for (String news_str : newsList) {
            News news = new GsonBuilder().create().fromJson(news_str, News.class);
            historyNewsList.add(news);
        }
        return historyNewsList;
    }

    /*
    [key, value]
    [currentDiscoverPage, currentPage &&& today]
    [historyNewsList, parseNewsList(FavoritesFragment.historyNewsList)]
    [favoritesNewsList, parseNewsList(FavoritesFragment.favNewsList)]
     */
}
