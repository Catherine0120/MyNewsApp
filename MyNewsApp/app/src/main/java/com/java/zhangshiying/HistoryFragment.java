package com.java.zhangshiying;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

public class HistoryFragment extends Fragment {
    public RecyclerView result;
    public HistoryAdapter myHistoryAdapter;
    private LinearLayoutManager myLayoutManager;

    public HistoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        myLayoutManager = new LinearLayoutManager(HistoryFragment.this.getContext());
        myLayoutManager.setReverseLayout(true);
        myLayoutManager.setStackFromEnd(true);
        result = view.findViewById(R.id.rv_his);
        result.setLayoutManager(myLayoutManager);

        ActivityResultLauncher<String> launcher = registerForActivityResult(new HistoryFragment.ResultContract(), new ActivityResultCallback<String>() {
            @Override
            public void onActivityResult(String result) {
                myHistoryAdapter.notifyDataSetChanged();
            }
        });

        myHistoryAdapter = new HistoryAdapter(getActivity(), HistoryFragment.this, myLayoutManager, launcher);
        result.setAdapter(myHistoryAdapter);
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