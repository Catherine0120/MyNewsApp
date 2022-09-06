package com.java.zhangshiying;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class CollectionFragmentAdapter extends FragmentStateAdapter {
    Context mainActivityContext;

    public CollectionFragmentAdapter(@NonNull Fragment fragment, Context mainActivityContext) {
        super(fragment);
        this.mainActivityContext = mainActivityContext;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = new CategoryFragment(mainActivityContext);
        Bundle args = new Bundle();
        args.putInt("category_label", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 11;
    }
}
