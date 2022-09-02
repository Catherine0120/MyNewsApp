package com.java.zhangshiying;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment {

    public static ArrayList<News> favNewsList = new ArrayList<>();
    public static ArrayList<News> historyNewsList = new ArrayList<>();

    SwitchMaterial mySwitch;

    private RecyclerView result;
    private FavoritesAdapter myFavoritesAdapter;

    public FavoritesFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        mySwitch = view.findViewById(R.id.switch_fav);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mySwitch.setText("To Favorites");
                    mySwitch.setTextColor(getResources().getColor(R.color.dark_pink, null));
                }
                else {
                    mySwitch.setText("To History");
                    mySwitch.setTextColor(getResources().getColor(R.color.blue, null));
                }
            }
        });

        //after something happens...
        LinearLayoutManager myLayoutManager = new LinearLayoutManager(FavoritesFragment.this.getContext());
        result = view.findViewById(R.id.rv_fav);
        result.setLayoutManager(myLayoutManager);
        myFavoritesAdapter = new FavoritesAdapter(favNewsList, getActivity(), FavoritesFragment.this, myLayoutManager);
        result.setAdapter(myFavoritesAdapter);

        return view;
    }
}