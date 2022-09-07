package com.java.zhangshiying;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CollectionFragment extends Fragment {
    CollectionFragmentAdapter myCollectionFragmentAdapter;
    ViewPager2 viewPager;
    Context mainActivityContext;

    public CollectionFragment() {}

    CollectionFragment(Context mainActivityContext) {
        this.mainActivityContext = mainActivityContext;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.collection_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        myCollectionFragmentAdapter = new CollectionFragmentAdapter(this, mainActivityContext);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setSaveEnabled(false);
        viewPager.setAdapter(myCollectionFragmentAdapter);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,true, new TabLayoutMediator.TabConfigurationStrategy(){
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch(position) {
                    case(0):
                        tab.setText("HOME");
                        break;
                    case(1):
                        tab.setText("Society");
                        break;
                    case(2):
                        tab.setText("Car");
                        break;
                    case(3):
                        tab.setText("Tech");
                        break;
                    case(4):
                        tab.setText("Sports");
                        break;
                    case(5):
                        tab.setText("Finance");
                        break;
                    case(6):
                        tab.setText("Health");
                        break;
                    case(7):
                        tab.setText("Culture");
                        break;
                    case(8):
                        tab.setText("Entertain");
                        break;
                    case(9):
                        tab.setText("Military");
                        break;
                    case(10):
                        tab.setText("Education");
                        break;
                    default:
                        tab.setText("other");
                        break;
                }
            }
        }).attach();
    }
}
