package com.java.zhangshiying;


import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private Context context;
    private Fragment myFragment;
    private LinearLayoutManager myLayoutManager;
    private View view;
    private ArrayList<String> historyNewsList;

    ActivityResultLauncher<String> launcher;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = View.inflate(myFragment.getContext(), R.layout.news_card_layout, null);
        return new HistoryAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final int pos = position;
        holder.setIsRecyclable(false);
        News news = Storage.findNewsValue(context.getApplicationContext(), historyNewsList.get(pos));

        holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.light_grey));
        holder.card.setRippleColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_grey)));
        holder.category.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dark_grey)));

        holder.title.setText(news.title);
        holder.category.setText(news.category);
        holder.origin.setText(news.origin);
        holder.time.setText(news.time);
        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyNewsList.remove(pos);
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
                    System.out.println("[HistoryAdapter.loadTitleImagesFromLocal] pos=" + pos + ": R.id.images not found");
                        e.printStackTrace();
                }

            } else if (Storage.findNewsValue(context.getApplicationContext(), news.newsID).images.size() == 1) {
                try {
                    holder.image.setImageBitmap(Storage.stringToBitmap(news.images.get(0)));
                    holder.image.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    System.out.println("[HistoryAdapter.loadTitleImageFromLocal] pos=" + pos + ": R.id.image not found");
                        e.printStackTrace();
                }

            }
        }

        view.findViewById(R.id.materialCardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("[HistoryAdapter.onClick]: [pos]=" + pos + ", [news]=" + news.title);
                launcher.launch(news.newsID + "," + -1);
            }
        });

    }

    @Override
    public int getItemCount() {
        return historyNewsList == null ? 0 : historyNewsList.size();
    }

    public HistoryAdapter(Context activity, Fragment fragment, LinearLayoutManager myLayoutManager, ActivityResultLauncher<String> launcher) {
        historyNewsList = Storage.findListValue(activity.getApplicationContext(), "his");
        context = activity;
        myFragment = fragment;
        this.myLayoutManager = myLayoutManager;
        this.launcher = launcher;
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

