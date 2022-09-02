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
    public static ArrayList<News> historyNewsList = new ArrayList<>();

    SwitchMaterial mySwitch;
    TextView curPage;

    private RecyclerView result;
    private FavoritesAdapter myFavoritesAdapter;

    LinearLayoutManager myLayoutManager;

    public FavoritesFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        myLayoutManager = new LinearLayoutManager(FavoritesFragment.this.getContext());
        result = view.findViewById(R.id.rv_fav);
        result.setLayoutManager(myLayoutManager);

        //by default: fav-list is showed
        myFavoritesAdapter = new FavoritesAdapter(favNewsList, getActivity(), FavoritesFragment.this, myLayoutManager, 0, launcher);
        result.setAdapter(myFavoritesAdapter);

        curPage = view.findViewById(R.id.text_favorites);
        mySwitch = view.findViewById(R.id.switch_fav);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mySwitch.setText("To Favorites");
                    mySwitch.setTextColor(getResources().getColor(R.color.teal_700, null));
                    curPage.setText("History");
                    curPage.setTextColor(getResources().getColor(R.color.dark_grey, null));
                    myFavoritesAdapter = new FavoritesAdapter(historyNewsList, getActivity(), FavoritesFragment.this, myLayoutManager, 1, launcher);
                    result.setAdapter(myFavoritesAdapter);
                }
                else {
                    mySwitch.setText("To History");
                    mySwitch.setTextColor(getResources().getColor(R.color.dark_grey, null));
                    curPage.setText("Favorites");
                    curPage.setTextColor(getResources().getColor(R.color.teal_700, null));
                    myFavoritesAdapter = new FavoritesAdapter(favNewsList, getActivity(), FavoritesFragment.this, myLayoutManager, 0, launcher);
                    result.setAdapter(myFavoritesAdapter);
                }
            }
        });

        return view;
    }


    ActivityResultLauncher<String> launcher = registerForActivityResult(new FavoritesFragment.ResultContract(), new ActivityResultCallback<String>() {
        @Override
        public void onActivityResult(String result) {
            String[] message = result.split(",");
            int pos = Integer.parseInt(message[0]);

            ArrayList<News> newsList;
            if (mySwitch.isChecked()) newsList = historyNewsList;
            else newsList = favNewsList;

            if (message.length == 3) {
                newsList.get(pos).like = true;
                newsList.get(pos).fav = true;
            }
            else if (message.length == 2) {
                if (Objects.equals(message[1], "like")) {
                    newsList.get(pos).like = true;
                    newsList.get(pos).fav = false;
                }
                if (Objects.equals(message[1], "fav")) {
                    newsList.get(pos).like = false;
                    newsList.get(pos).fav = true;
                }
            }
            else {
                assert(message.length == 1);
                newsList.get(pos).like = false;
                newsList.get(pos).fav = false;
            }
        }
    });

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
}