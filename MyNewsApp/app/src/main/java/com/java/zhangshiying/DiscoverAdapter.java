package com.java.zhangshiying;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.MyViewHolder> {
    Context context;
    ArrayList<News> newsList;
    Fragment fragmentContext;
    View view;
    LinearLayoutManager myLayoutManager;


    private class DiscoverHandler extends Handler {
        private final WeakReference<DiscoverFragment> myFragment;
        public DiscoverHandler(DiscoverFragment fragment) {
            myFragment = new WeakReference<DiscoverFragment>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case 1:
                    int pos = msg.getData().getInt("position");
                    try {
                        ImageView iv = (ImageView) myLayoutManager.findViewByPosition(pos).findViewById(R.id.image);
                        iv.setVisibility(View.VISIBLE);
                        iv.setImageBitmap((Bitmap)msg.obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("loadImage", "error");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private final DiscoverHandler myHandler = new DiscoverHandler((DiscoverFragment) fragmentContext);

    public DiscoverAdapter(ArrayList<News> newsList, Context context, Fragment fragment, LinearLayoutManager myLayoutManager) {
        this.context = context;
        this.newsList = newsList;
        this.fragmentContext = fragment;
        this.myLayoutManager = myLayoutManager;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, origin, time;
        ImageView image;
        VideoView video;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            category = itemView.findViewById(R.id.textCategory);
            origin = itemView.findViewById(R.id.textOrigin);
            time = itemView.findViewById(R.id.textTime);
            image = itemView.findViewById(R.id.image);
            video = itemView.findViewById(R.id.video);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = View.inflate(context, R.layout.news_card_layout, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final int pos = position;
        holder.setIsRecyclable(false);
        News news = newsList.get(position);
        holder.title.setText(news.title);
        holder.category.setText(news.category);
        holder.origin.setText(news.origin);
        holder.time.setText(news.time);
        if (news.imageExist) {
            getBitmapFromURL(news.imageUrls.get(0), pos);
        }
        else holder.image.setVisibility(View.GONE);
        if (news.videoExist) {
            assert(false);
        }

        view.findViewById(R.id.materialCardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //DO SOMETHING...
            }
        });
    }

    private void getBitmapFromURL(String src, int pos) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("src",src);
                    URL url = new URL(src);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    Log.e("Bitmap","returned");

                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", pos);
                    msg.setData(bundle);
                    msg.obj = myBitmap;
                    msg.what = 1;
                    myHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception",e.getMessage());
                }
            }
        }).start();
    }

    private void castVideo (VideoView vv) {
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(vv);
        mediaController.setMediaPlayer(vv);
        vv.setMediaController(mediaController);
        vv.start();
    }

    @Override
    public int getItemCount() {
        return newsList == null ? 0 : newsList.size();
    }
}
