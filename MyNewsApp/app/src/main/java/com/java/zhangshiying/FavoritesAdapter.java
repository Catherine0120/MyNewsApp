package com.java.zhangshiying;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.MyViewHolder> {

    private Context context;
    private Fragment myFragment;
    private LinearLayoutManager myLayoutManager;
    private View view;
    private ActivityResultLauncher launcher;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = View.inflate(context, R.layout.news_card_layout, null);
        return new FavoritesAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final int pos = position;
        holder.setIsRecyclable(false);
        News news = FavoritesFragment.favNewsList.get(position);

        holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.light_teal));
        holder.card.setRippleColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_teal)));
        holder.category.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.teal_700)));

        holder.title.setText(news.title);
        holder.category.setText(news.category);
        holder.origin.setText(news.origin);
        holder.time.setText(news.time);
        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeData(pos);
            }
        });

        //display images
        if (news.imageExist) {
            if (news.imageCount >= 2) {
                ((ImageView) holder.images.findViewById(R.id.image_1)).setImageBitmap(getBitmapFromHis(news.newsID, 0));
                ((ImageView) holder.images.findViewById(R.id.image_2)).setImageBitmap(getBitmapFromHis(news.newsID, 1));
                holder.images.setVisibility(View.VISIBLE);
            } else {
                holder.image.setImageBitmap(getBitmapFromHis(news.newsID, 0));
                holder.image.setVisibility(View.VISIBLE);
            }
        }
        if (news.videoExist) {
            assert (false);
        }

        view.findViewById(R.id.materialCardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                News news = FavoritesFragment.favNewsList.get(pos);
                news.pos = pos;
                System.out.println("[FavoritesAdapter.onClick]: [pos]=" + pos + ", [news]=" + news.title);
                Gson gson = new Gson();
                String send = gson.toJson(news);
                launcher.launch(send);
            }
        });

    }

    private Bitmap getBitmapFromHis(String newsID, int num) {
        for (News news : HistoryFragment.historyNewsList) {
            if (Objects.equals(news.newsID, newsID)) {
                if (news.images.size() > num)
                    return news.images.get(num);
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return FavoritesFragment.favNewsList == null ? 0 : FavoritesFragment.favNewsList.size();
    }

    public FavoritesAdapter(Context activity, Fragment fragment, LinearLayoutManager myLayoutManager, ActivityResultLauncher launcher) {
        context = activity;
        this.myFragment = fragment;
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

    public void removeData(int position) {
        FavoritesFragment.favNewsList.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

}