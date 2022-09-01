package com.java.zhangshiying;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
                    mySwitch.setTextColor(getResources().getColor(R.color.blue, null));
                    System.out.println(historyNewsList.size());
                }
                else {
                    mySwitch.setText("To History");
                    mySwitch.setTextColor(getResources().getColor(R.color.dark_pink, null));
                    System.out.println(favNewsList.size());
                }
            }
        });

        return view;
    }
}