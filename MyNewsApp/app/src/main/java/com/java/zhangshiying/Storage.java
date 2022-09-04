package com.java.zhangshiying;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Storage {
    private static final String SP_NAME = "storage";

    /*
    ==[key, value]==
    ["currentDiscoverPage", String currentPage]
    ["today" String today]
    ["fav", String[] newsIDs], join by ','
    ["his", String[] newsIDs], join by ','
    [String newsID, News news], parsed by gson
    */

    public static void write(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
        System.out.println("[Storage]: commit " + key + ", " + value);
    }

    public static String newsToString(News news) {
        Gson gson = new Gson();
        return gson.toJson(news);
    }

    //"fav"
    public static void addFav(Context context, String newsID) {
        String str = findValue(context, "fav");
        str = str + newsID + ",";
        write(context, "fav", str);
    }

    public static void removeNewsFromFav(Context context, String newsID) {
        ArrayList<String> favNewsList = findListValue(context, "fav");
        favNewsList.removeIf(_newsID -> Objects.equals(_newsID, newsID));
        write(context, "fav", String.join(",", favNewsList));
    }

    //"his"
    public static void addHis(Context context, String newsID) {
        String str = findValue(context, "his");
        str = str + newsID + ",";
        write(context, "his", str);
    }

    public static String findValue(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    //return favNewsIDList or hisNewsIDList, [key]="fav"/"his"
    public static ArrayList<String> findListValue(Context context, String key) {
        String msg = findValue(context, key);
        String[] myArray = msg.split(",");
        return new ArrayList<>(Arrays.asList(myArray));
    }

    //return News, [key]=newsID
    public static News findNewsValue(Context context, String newsID) {
        String msg = findValue(context, newsID);
        return new GsonBuilder().create().fromJson(msg, News.class);
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
}
