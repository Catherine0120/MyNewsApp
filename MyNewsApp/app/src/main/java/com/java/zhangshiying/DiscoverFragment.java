package com.java.zhangshiying;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class DiscoverFragment extends Fragment {
    private ArrayList<News> newsList;
    private Context context;

    public DiscoverFragment() {}

    public DiscoverFragment(ArrayList<News> newsList, MainActivity activity) {
        this.newsList = newsList;
        this.context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View currentPage = inflater.inflate(R.layout.fragment_discover, container, false);
        RecyclerView result = currentPage.findViewById(R.id.rv);

        LinearLayoutManager myLayoutManager = new LinearLayoutManager(DiscoverFragment.this.getContext());
        result.setLayoutManager(myLayoutManager);
        result.setAdapter(new DiscoverAdapter(newsList, context, DiscoverFragment.this, myLayoutManager));

        return currentPage;
    }

}