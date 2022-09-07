package com.java.zhangshiying;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaPlayer;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.internal.bind.ArrayTypeAdapter;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.MyViewHolder> {

    private Context context;
    private Fragment myFragment;
    private LinearLayoutManager myLayoutManager;
    private View view;
    public ArrayList<String> favNewsList;

    ActivityResultLauncher<String> launcher;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = View.inflate(myFragment.getContext(), R.layout.news_card_layout, null);
        return new FavoritesAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final int pos = position;
        holder.setIsRecyclable(false);
        News news = Storage.findNewsValue(context.getApplicationContext(), favNewsList.get(pos));

        holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.light_pink));
        holder.card.setRippleColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_pink)));
        holder.category.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dark_pink)));

        holder.title.setText(news.title);
        holder.category.setText(news.category);
        holder.origin.setText(news.origin);
        holder.time.setText(news.time);
        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                news.fav = false;
                favNewsList.remove(pos);
                Storage.removeNewsFromFav(context.getApplicationContext(), news.newsID);
                notifyItemRemoved(pos);
                notifyDataSetChanged();
            }
        });

        //display images
        if (news.imageExist) {
            if (Storage.findNewsValue(context.getApplicationContext(), news.newsID).images.size() >= 2) {
                try {
                    ((ImageView) holder.images.findViewById(R.id.image_1)).setImageBitmap(Storage.stringToBitmap(news.images.get(0)));
                    ((ImageView) holder.images.findViewById(R.id.image_2)).setImageBitmap(Storage.stringToBitmap(news.images.get(1)));
                    holder.images.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    System.out.println("[FavoritesAdapter.loadTitleImagesFromLocal] pos=" + pos + ": R.id.images not found");
                        e.printStackTrace();
                }

            } else if (Storage.findNewsValue(context.getApplicationContext(), news.newsID).images.size() == 1) {
                try {
                    holder.image.setImageBitmap(Storage.stringToBitmap(news.images.get(0)));
                    holder.image.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    System.out.println("[FavoritesAdapter.loadTitleImageFromLocal] pos=" + pos + ": R.id.image not found");
                        e.printStackTrace();
                }

            }
        }
        if (news.videoExist) {
            System.out.println("[DiscoverAdapter]: video exists " + news.videoUrls);
            holder.video.setVisibility(View.VISIBLE);
            MediaController mediaController = new MediaController(myFragment.getContext());
            holder.video.setMediaController(mediaController);
            mediaController.setAnchorView(holder.video);
            holder.video.setVideoURI(Uri.parse(news.videoUrls.get(0)));
            holder.video.requestFocus();
            holder.video.start();

            holder.video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d("video", "setOnErrorListener ");
                    return true;
                }
            });
        }

        view.findViewById(R.id.materialCardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("[FavoritesAdapter.onClick]: [pos]=" + pos + ", [news]=" + news.title);
                launcher.launch(news.newsID + "," + -1);
            }
        });

    }

    @Override
    public int getItemCount() {
        return favNewsList == null ? 0 : favNewsList.size();
    }

    public FavoritesAdapter(Context activity, Fragment fragment, LinearLayoutManager myLayoutManager, ActivityResultLauncher<String> launcher) {
        context = activity;
        this.myFragment = fragment;
        this.myLayoutManager = myLayoutManager;
        this.launcher = launcher;
        favNewsList = Storage.findListValue(context.getApplicationContext(), "fav");
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, origin, time;
        ImageView image;
        VideoView video;
        ImageView closeBtn;
        MaterialCardView card;
        LinearLayout images;

        public MyViewHolder(View itemView) {
            super(itemView);
            images = itemView.findViewById(R.id.images);
            card = itemView.findViewById(R.id.materialCardView);
            title = itemView.findViewById(R.id.textTitle);
            category = itemView.findViewById(R.id.textCategory);
            origin = itemView.findViewById(R.id.textOrigin);
            time = itemView.findViewById(R.id.textTime);
            image = itemView.findViewById(R.id.image);
            video = itemView.findViewById(R.id.video);
            closeBtn = itemView.findViewById(R.id.close_btn);
        }
    }


}