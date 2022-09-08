package com.java.zhangshiying;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DetailNewsActivity extends AppCompatActivity {
    News news;
    ImageView topImageView;
    HorizontalScrollView topImagesScrollView;
    VideoView video;
    LinearLayout myLinearLayout;

    ShineButton likeButton, favButton;

    String pos = "";


    private class DetailHandler extends Handler {
        private final WeakReference<DetailNewsActivity> myActivity;
        public DetailHandler(DetailNewsActivity activity) {
            myActivity = new WeakReference<DetailNewsActivity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case 1:
                    if (!(msg.getData().getBoolean("multiImages"))) {
                        topImageView.setVisibility(View.VISIBLE);
                        try {
                            topImageView.setImageBitmap((Bitmap)((Object[]) msg.obj)[0]);
                            news.images.add(Storage.bitmapToString((Bitmap)((Object[]) msg.obj)[0]));
                            Storage.write(getApplicationContext(), news.newsID, Storage.newsToString(news));
                        } catch (Exception e) {
                            System.out.println("E [DetailNewsActivity.singleImage]: load or save error");
                            e.printStackTrace();
                        }
                        break;
                    }
                    else {
                        try {
                            View view = (View)(((Object[]) msg.obj)[1]);
                            ImageView img = (ImageView) (((Object[]) msg.obj)[2]);
                            img.setImageBitmap((Bitmap)((Object[]) msg.obj)[0]);
                            myLinearLayout.addView(view);
                            news.images.add(Storage.bitmapToString((Bitmap)((Object[]) msg.obj)[0]));
                            Storage.write(getApplicationContext(), news.newsID, Storage.newsToString(news));
                        } catch (Exception e) {
                            System.out.println("E [DetailNewsActivity.multiImages]: load or save error");
                            e.printStackTrace();
                        }
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

        news = Storage.findNewsValue(getApplicationContext(), this.getIntent().getStringExtra("newsID").split(",")[0]);
        System.out.println("[DEBUG] [DetailNewsActivity]: storage.news.images.size() = " + news.images.size());
        pos = this.getIntent().getStringExtra("newsID").split(",")[1];

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
        video = (VideoView) findViewById(R.id.video_detail);

        likeButton = findViewById(R.id.shineBtn_like);
        favButton = findViewById(R.id.shineBtn_favorites);

        if (news.like) likeButton.setChecked(true);
        if (news.fav) favButton.setChecked(true);

        setResult(RESULT_OK, new Intent().putExtra("feedback", getResultMsg()));

        likeButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                news.like = checked;
                setResult(RESULT_OK, new Intent().putExtra("feedback", getResultMsg()));
                System.out.println("[DetailNewsActivity.getResultMsg()]: " + getResultMsg());
                Storage.write(getApplicationContext(), news.newsID, Storage.newsToString(news));
            }
        });

        favButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                if (!news.fav && checked) {
                    System.out.println("[Debug.DetailNewsActivity]: add to fav List");
                    Storage.addFav(getApplicationContext(), news.newsID);
                    ArrayList<String> myList1 = Storage.findListValue(getApplicationContext(),"fav");
                    System.out.println("[Debug.DetailNewsActivity.Storage] after: FavoritesNewsList");
                    for (String str : myList1) System.out.println("      " + str);
                }
                else if (news.fav && !checked) {
                    System.out.println("[Debug.DetailNewsActivity]: remove from fav List");
                    Storage.removeNewsFromFav(getApplicationContext(), news.newsID);
                    ArrayList<String> myList1 = Storage.findListValue(getApplicationContext(),"fav");
                    System.out.println("[Debug.DetailNewsActivity.Storage] after: FavoritesNewsList");
                    for (String str : myList1) System.out.println("      " + str);
                }
                news.fav = checked;
                setResult(RESULT_OK, new Intent().putExtra("feedback", getResultMsg()));
                System.out.println("[DetailNewsActivity.getResultMsg()]: " + getResultMsg());
                Storage.write(getApplicationContext(), news.newsID, Storage.newsToString(news));
            }
        });

        if (news.videoExist) {
            video.setVisibility(View.VISIBLE);
            MediaController mediaController = new MediaController(DetailNewsActivity.this);
            video.setMediaController(mediaController);
            mediaController.setAnchorView(video);
            video.setVideoURI(Uri.parse(news.videoUrls.get(0)));
            video.requestFocus();
            video.start();

            video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d("video", "setOnErrorListener ");
                    return true;
                }
            });

        }

        else if (news.imageExist) {
            if (news.imageCount == 1) {
                topImageView = (ImageView) findViewById(R.id.image_detail);
                topImageView.setVisibility(View.VISIBLE);
                if (!news.readDetail) {
                    news.readDetail = true;
                    System.out.println("[DEBUG] [DetailNewsActivity]: load image from URL");
                    getBitmapFromURL(news.imageUrls.get(0), false, null, null);
                }
                else {
                    try {
                        System.out.println("[DEBUG] [DetailNewsActivity]: load image from local");
                        topImageView.setImageBitmap(Storage.stringToBitmap((Storage.findNewsValue(getApplicationContext(), news.newsID)).images.get(0)));
                    } catch (Exception e) {
                        System.out.println("E [DetailNewsActivity.loadTitleImageFromLocal] : R.id.image not found");
                        e.printStackTrace();
                    }
                }
            }
            else {
                topImagesScrollView = (HorizontalScrollView) findViewById(R.id.horizontal_scroll_view_detail);
                topImagesScrollView.setVisibility(View.VISIBLE);
                myLinearLayout = (LinearLayout) findViewById(R.id.image_horizontal_layout);
                for (int i = 0; i < news.imageCount; ++i) {
                    View view = LayoutInflater.from(this).inflate(R.layout.single_image_layout, myLinearLayout, false);
                    ImageView img = view.findViewById(R.id.single_image);
                    if (!news.readDetail) {
                        if (i == news.imageCount - 1) news.readDetail = true;
                        System.out.println("[DEBUG] [DetailNewsActivity]: load images from URL");
                        getBitmapFromURL(news.imageUrls.get(i), true, view, img);
                    }
                    else {
                        try {
                            System.out.println("[DEBUG] [DetailNewsActivity]: load images from local");
                            img.setImageBitmap(Storage.stringToBitmap((Storage.findNewsValue(getApplicationContext(), news.newsID)).images.get(i)));
                            myLinearLayout.addView(view);
                        } catch (Exception e) {
                            System.out.println("[DEBUG]: can't load images from local");
                            e.printStackTrace();
                        }

                    }
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
        String feedback = news.newsID + "," + pos;
        if (news.like && news.fav) feedback = feedback + ",like" + ",fav";
        else if (news.like) feedback = feedback + ",like";
        else if (news.fav) feedback = feedback + ",fav";
        return feedback;
    }

}
