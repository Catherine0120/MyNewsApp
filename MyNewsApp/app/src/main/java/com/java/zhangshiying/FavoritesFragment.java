package com.java.zhangshiying;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Objects;

public class FavoritesFragment extends Fragment {

    public static ArrayList<News> favNewsList = new ArrayList<>();

    private RecyclerView result;
    public FavoritesAdapter myFavoritesAdapter;
    private LinearLayoutManager myLayoutManager;

    public FavoritesFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        myLayoutManager = new LinearLayoutManager(FavoritesFragment.this.getContext());
        result = view.findViewById(R.id.rv_fav);
        result.setLayoutManager(myLayoutManager);

        ActivityResultLauncher<String> launcher = registerForActivityResult(new FavoritesFragment.ResultContract(), new ActivityResultCallback<String>() {
            @Override
            public void onActivityResult(String result) {
                String[] message = result.split(",");
                int pos = Integer.parseInt(message[0]);
                String conditionChanged = message[message.length - 1];
                if (Objects.equals(conditionChanged, "true")) {
                    myFavoritesAdapter.notifyDataSetChanged();
                }
                else {
                    if (message.length == 4) {
                        favNewsList.get(pos).like = true;
                        favNewsList.get(pos).fav = true;
                    }
                    else if (message.length == 3) {
                        if (Objects.equals(message[1], "like")) {
                            favNewsList.get(pos).like = true;
                            favNewsList.get(pos).fav = false;
                        }
                        if (Objects.equals(message[1], "fav")) {
                            favNewsList.get(pos).like = false;
                            favNewsList.get(pos).fav = true;
                        }
                    }
                    else {
                        assert(message.length == 2);
                        favNewsList.get(pos).like = false;
                        favNewsList.get(pos).fav = false;
                    }
                }
            }
        });

        myFavoritesAdapter = new FavoritesAdapter(getActivity(), FavoritesFragment.this, myLayoutManager, launcher);
        result.setAdapter(myFavoritesAdapter);

        return view;
    }




    class ResultContract extends ActivityResultContract<String, String> {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, String input) {
            Intent intent = new Intent(getContext(), DetailNewsActivity.class);
            intent.putExtra("news", input);
            return intent;
        }

        @Override
        public String parseResult(int resultCode, @Nullable Intent intent) {
            return intent.getStringExtra("feedback");
        }
    }

    public static void removeNewsID(String newsID) {
        favNewsList.removeIf(news -> Objects.equals(news.newsID, newsID));
    }

    public static void newsLikeStateChanged(String newsID, boolean like) {
        for (News news : favNewsList) {
            if (Objects.equals(news.newsID, newsID)) {
                news.like = like;
            }
        }
    }

    public static void setReadDetail(String newsID) {
        for (News news : favNewsList) {
            if (Objects.equals(news.newsID, newsID)) {
                news.readDetail = true;
            }
        }
    }
}