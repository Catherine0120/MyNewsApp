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
    public RecyclerView result;
    public FavoritesAdapter myFavoritesAdapter;
    public LinearLayoutManager myLayoutManager;

    public FavoritesFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        myLayoutManager = new LinearLayoutManager(FavoritesFragment.this.getContext(), LinearLayoutManager.VERTICAL, true);
        myLayoutManager.setStackFromEnd(true);
        result = view.findViewById(R.id.rv_fav);
        result.setLayoutManager(myLayoutManager);

        ActivityResultLauncher<String> launcher = registerForActivityResult(new FavoritesFragment.ResultContract(), new ActivityResultCallback<String>() {
            @Override
            public void onActivityResult(String result) {
                myFavoritesAdapter.favNewsList = Storage.findListValue(getActivity().getApplicationContext(), "fav");
                myFavoritesAdapter.notifyDataSetChanged();
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
            intent.putExtra("newsID", input);
            return intent;
        }

        @Override
        public String parseResult(int resultCode, @Nullable Intent intent) {
            return intent.getStringExtra("feedback");
        }
    }

}