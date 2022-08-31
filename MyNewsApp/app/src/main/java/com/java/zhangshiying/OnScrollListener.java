package com.java.zhangshiying;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OnScrollListener extends RecyclerView.OnScrollListener {
    private LinearLayoutManager myLinearLayoutManager;
    private int currentPage;
    private boolean loading = true;

    public OnScrollListener(LinearLayoutManager myLinearLayoutManager, int currentPage) {
        this.myLinearLayoutManager = myLinearLayoutManager;
        this.currentPage = currentPage;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

    }
}
