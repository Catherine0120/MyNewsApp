package com.java.zhangshiying;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.MyViewHolder> {

    ArrayList<News> newsListDisplay;
    Context context;
    Fragment myFragment;
    LinearLayoutManager myLayoutManager;
    View view;


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
        News news = newsListDisplay.get(position);
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
    }

    @Override
    public int getItemCount() {
        return newsListDisplay == null ? 0 : newsListDisplay.size();
    }

    public FavoritesAdapter(ArrayList<News> favNewsList, Context activity, Fragment fragment, LinearLayoutManager myLayoutManager) {
        newsListDisplay = favNewsList;
        context = activity;
        myFragment = fragment;
        this.myLayoutManager = myLayoutManager;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, origin, time;
        ImageView image;
        VideoView video;
        ImageView closeBtn;

        public MyViewHolder(View itemView) {
            super(itemView);
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
        newsListDisplay.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }
}
