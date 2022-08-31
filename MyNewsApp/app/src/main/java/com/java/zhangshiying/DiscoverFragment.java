package com.java.zhangshiying;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DiscoverFragment extends Fragment {
    private ArrayList<News> newsList;
    private Context context;
    private SwipeRefreshLayout mySwipeRefreshView;
    private RecyclerView result;
    private View loadPulse;

    final Handler handler = new Handler();

    private DiscoverAdapter myDiscoverAdapter;

    private int pageSize;
    static public int currentPage = 1;

    public enum State {
        DROP_AND_REFRESH, SCROLL_AND_LOAD
    }

    private class MainHandler extends Handler {
        private final WeakReference<DiscoverFragment> myParent;
        public MainHandler(DiscoverFragment fragment) {
            myParent = new WeakReference<DiscoverFragment>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            String message = msg.obj.toString();
            try {
                JSONObject obj = new JSONObject(message);
                JSONArray newsDescriptions = obj.getJSONArray("data");
                ArrayList<News> newsDescriptionList = new ArrayList<>();
                for (int i = 0; i < newsDescriptions.length(); ++i) {
                    JSONObject singleNewsDescription = newsDescriptions.getJSONObject(i);
                    newsDescriptionList.add(new News(singleNewsDescription));
                }
                newsList = newsDescriptionList;


                if (msg.getData().getInt("state") == 0) {
                    LinearLayoutManager myLayoutManager = new LinearLayoutManager(DiscoverFragment.this.getContext());
                    result.setLayoutManager(myLayoutManager);
                    myDiscoverAdapter = new DiscoverAdapter(newsList, context, DiscoverFragment.this, myLayoutManager);
                    result.setAdapter(myDiscoverAdapter);
                }
                else {
                    assert(myDiscoverAdapter != null);
                    myDiscoverAdapter.addNewsList(newsList);
                    loadPulse.setVisibility(View.INVISIBLE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final DiscoverFragment.MainHandler myHandler = new DiscoverFragment.MainHandler(this);



    public DiscoverFragment() {}

    public DiscoverFragment(ArrayList<News> newsList, MainActivity activity, int pageSize) {
        this.newsList = newsList;
        this.context = activity;
        this.pageSize = pageSize;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_discover, container, false);
        result = currentView.findViewById(R.id.rv);
        loadPulse = currentView.findViewById(R.id.spin_kit);

        mySwipeRefreshView = (SwipeRefreshLayout) currentView.findViewById(R.id.refresh);
        mySwipeRefreshView.setColorSchemeColors(getResources().getColor(R.color.pink, getContext().getTheme()),
                getResources().getColor(R.color.dark_pink, getContext().getTheme()),
                getResources().getColor(R.color.purple_200, getContext().getTheme()));
        mySwipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mySwipeRefreshView.setRefreshing(true);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        getNewsList(DiscoverFragment.State.DROP_AND_REFRESH);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mySwipeRefreshView.setRefreshing(false);
                            }
                        }, 1000);
                    }
                }.start();
            }
        });


        LinearLayoutManager myLayoutManager = new LinearLayoutManager(DiscoverFragment.this.getContext());
        result.setLayoutManager(myLayoutManager);
        myDiscoverAdapter = new DiscoverAdapter(newsList, context, DiscoverFragment.this, myLayoutManager);
        result.setAdapter(myDiscoverAdapter);
        result.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Toast.makeText(context, "Discovering more news...", Toast.LENGTH_SHORT).show();
                    loadPulse.setVisibility(View.VISIBLE);
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            getNewsList(DiscoverFragment.State.SCROLL_AND_LOAD);
                        }
                    }.start();
                }
            }
        });

        return currentView;
    }

    private void getNewsList(State state) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = simpleDateFormat.format(date);

        String myUrl = "https://api2.newsminer.net/svc/news/queryNewsList?size=%d&startDate=&endDate=%s&words=&categories=&page=%d";
        myUrl = String.format(myUrl, pageSize, today, ++currentPage);
        System.out.println(myUrl);
        String s = "";
        try {
            URL url  = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            InputStream inputStream = conn.getInputStream();
            s = readFromStream(inputStream);
            Message msg = new Message();
            Bundle bundle = new Bundle();
            switch(state) {
                case DROP_AND_REFRESH:
                    bundle.putInt("state", 0);
                    break;
                case SCROLL_AND_LOAD:
                    bundle.putInt("state", 1);
                    break;
            }
            msg.obj = s;
            msg.setData(bundle);
            myHandler.sendMessage(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readFromStream(InputStream inStream) {
        String s = "";
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[10240];
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            inStream.close();
            s = outStream.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return s;
    }

}