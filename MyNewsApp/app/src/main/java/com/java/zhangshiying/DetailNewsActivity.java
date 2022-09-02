package com.java.zhangshiying;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.GsonBuilder;
import com.sackcentury.shinebuttonlib.ShineButton;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailNewsActivity extends AppCompatActivity {

    News news;
    ImageView topImageView;
    HorizontalScrollView topImagesScrollView;
    LinearLayout myLinearLayout;

    ShineButton likeButton, favButton;

    int fromPos;
    String resultMsg = "";


    private class DetailHandler extends Handler {
        private final WeakReference<DetailNewsActivity> myAcitivity;
        public DetailHandler(DetailNewsActivity activity) {
            myAcitivity = new WeakReference<DetailNewsActivity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case 1:
                    if (!(msg.getData().getBoolean("multiImages"))) {
                        topImageView.setVisibility(View.VISIBLE);
                        try {
                            topImageView.setImageBitmap((Bitmap)((Object[]) msg.obj)[0]);
                        } catch (Exception e) { e.printStackTrace();}
                        break;
                    }
                    else {
                        try {
                            View view = (View)(((Object[]) msg.obj)[1]);
                            ImageView img = (ImageView) (((Object[]) msg.obj)[2]);
                            img.setImageBitmap((Bitmap)((Object[]) msg.obj)[0]);
                            myLinearLayout.addView(view);
                        } catch (Exception e) { e.printStackTrace();}
                        break;
                    }
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
        System.out.println("[DetailNewsActivity]: news = " + news);
//        if (!news.read) FavoritesFragment.historyNewsList.add(news);

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

        likeButton = findViewById(R.id.shineBtn_like);
        favButton = findViewById(R.id.shineBtn_favorites);

        if (news.like) likeButton.setChecked(true);
        if (news.fav) favButton.setChecked(true);
        System.out.println("[like/fav]: " + news.like + news.fav);

        fromPos = news.pos;
        assert (fromPos != -1);

        setResult(RESULT_OK, new Intent().putExtra("feedback", getResultMsg()));

        likeButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                FavoritesFragment.historyNewsList.add(news);
                news.like = checked;
                setResult(RESULT_OK, new Intent().putExtra("feedback", getResultMsg()));
            }
        });

        favButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                FavoritesFragment.favNewsList.add(news);
                news.fav = checked;
                resultMsg += fromPos + ",fav,";
                setResult(RESULT_OK, new Intent().putExtra("feedback", getResultMsg()));
            }
        });

        if (news.imageExist) {
            if (news.imageCount == 1) {
                topImageView = (ImageView) findViewById(R.id.image_detail);
                topImageView.setVisibility(View.VISIBLE);
                getBitmapFromURL(news.imageUrls.get(0), false, null, null);
            }
            else {
                topImagesScrollView = (HorizontalScrollView) findViewById(R.id.horizontal_scroll_view_detail);
                topImagesScrollView.setVisibility(View.VISIBLE);
                myLinearLayout = (LinearLayout) findViewById(R.id.image_horizontal_layout);
                for (int i = 0; i < news.imageCount; ++i) {
                    View view = LayoutInflater.from(this).inflate(R.layout.single_image_layout, myLinearLayout, false);
                    ImageView img = view.findViewById(R.id.single_image);
                    getBitmapFromURL(news.imageUrls.get(i), true, view, img);
                }
            }
        }
    }

    private void getBitmapFromURL(String src, boolean multiImages, View view, ImageView img) {
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
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("multiImages", multiImages);
                    msg.setData(bundle);
                    msg.obj = new Object[] {myBitmap, view, img};
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

    private String getResultMsg() {
        if (news.like && news.fav) return fromPos + ",like" + ",fav";
        else if (news.like) return fromPos + ",like";
        else if (news.fav) return fromPos + ",fav";
        else return Integer.toString(fromPos);
    }
}
