package com.java.zhangshiying;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.GsonBuilder;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailNewsActivity extends AppCompatActivity {
    News news;
    ImageView topImageView;

    private class DetailHandler extends Handler {
        private final WeakReference<DetailNewsActivity> myAcitivity;
        public DetailHandler(DetailNewsActivity activity) {
            myAcitivity = new WeakReference<DetailNewsActivity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case 1:
                    topImageView.setVisibility(View.VISIBLE);
                    try {
                        topImageView.setImageBitmap((Bitmap)msg.obj);
                    } catch (Exception e) { e.printStackTrace();}
                    break;
                default:
                    break;
            }
        }
    }

    private DetailHandler myHandler = new DetailHandler(this);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_news);

        news = new GsonBuilder().create().fromJson(this.getIntent().getStringExtra("news"), News.class);

        TextView titleDetail = (TextView) findViewById(R.id.title_detail);
        titleDetail.setText(news.title);
        TextView categoryDetail = (TextView) findViewById(R.id.category_detail);
        categoryDetail.setText(news.category);
        TextView originDetail = (TextView) findViewById(R.id.origin_detail);
        originDetail.setText(news.origin);
        TextView timeDetail = (TextView) findViewById(R.id.time_detail);
        timeDetail.setText(news.time);
        TextView contentDetail = (TextView) findViewById(R.id.content_detail);
        contentDetail.setText(news.content);

        if (news.imageExist) {
            topImageView = (ImageView) findViewById(R.id.image_detail);
            topImageView.setVisibility(View.VISIBLE);
            System.out.println(topImageView.getHeight());
            getBitmapFromURL(news.imageUrls.get(0));
        }
    }

    private void getBitmapFromURL(String src) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(src);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
//                    System.out.println("[" + pos + "]: SUCCESS src = " + src);
//                    Log.e("Bitmap","returned");

                    Message msg = new Message();

                    msg.obj = myBitmap;
                    msg.what = 1;
                    myHandler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("[Detailed Page] ERROR src = " + src);
                    Log.e("Exception",e.getMessage());
                }
            }
        }).start();
    }
}
