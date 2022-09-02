package com.java.zhangshiying;

import android.os.Bundle;

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
        myFavoritesAdapter = new FavoritesAdapter(favNewsList, getActivity(), FavoritesFragment.this, myLayoutManager, 0);
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
                    myFavoritesAdapter = new FavoritesAdapter(historyNewsList, getActivity(), FavoritesFragment.this, myLayoutManager, 1);
                    result.setAdapter(myFavoritesAdapter);
                }
                else {
                    mySwitch.setText("To History");
                    mySwitch.setTextColor(getResources().getColor(R.color.dark_grey, null));
                    curPage.setText("Favorites");
                    curPage.setTextColor(getResources().getColor(R.color.teal_700, null));
                    myFavoritesAdapter = new FavoritesAdapter(favNewsList, getActivity(), FavoritesFragment.this, myLayoutManager, 0);
                    result.setAdapter(myFavoritesAdapter);
                }
            }
        });

        return view;
    }
}