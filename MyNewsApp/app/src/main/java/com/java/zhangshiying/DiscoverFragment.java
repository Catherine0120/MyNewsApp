package com.java.zhangshiying;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.Objects;

public class DiscoverFragment extends Fragment {

    int condition = 0; //状态参数为“0”表示“discover”， 状态参数为“1”表示“search”
    private ArrayList<String> urlsFromSearch;
    private ArrayList<News> tmpNewsList = new ArrayList<>();
    int tmpCount = 0;

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
                newsList.addAll(newsDescriptionList);

                if (msg.getData().getInt("state") == 0) { //DROP_AND_REFRESH
                    if (condition == 0) { //discover
                        LinearLayoutManager myLayoutManager = new LinearLayoutManager(DiscoverFragment.this.getContext());
                        result.setLayoutManager(myLayoutManager);
                        myDiscoverAdapter = new DiscoverAdapter(newsDescriptionList, context, DiscoverFragment.this, myLayoutManager, launcher);
                        result.setAdapter(myDiscoverAdapter);
                    }
                    else { //search
                        tmpCount++;
                        System.out.println("[tmpCount] = " + tmpCount);
                        tmpNewsList.addAll(newsDescriptionList);
                        System.out.println("[tmpNewsList] = " + tmpNewsList);
                        if (tmpCount == urlsFromSearch.size()) {
                            System.out.println("IT'S TIME!");
                            LinearLayoutManager myLayoutManager = new LinearLayoutManager(DiscoverFragment.this.getContext());
                            result.setLayoutManager(myLayoutManager);
                            myDiscoverAdapter = new DiscoverAdapter(tmpNewsList, context, DiscoverFragment.this, myLayoutManager, launcher);
                            result.setAdapter(myDiscoverAdapter);
                            tmpCount = 0;
                            tmpNewsList.clear();
                        }
                    }
                }
                else { //SCROLL_AND_LOAD
                    assert(myDiscoverAdapter != null);
                    myDiscoverAdapter.addNewsList(newsDescriptionList);
                    loadPulse.setVisibility(View.INVISIBLE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final DiscoverFragment.MainHandler myHandler = new DiscoverFragment.MainHandler(this);



    public DiscoverFragment() {}

    public DiscoverFragment(ArrayList<News> newsList, MainActivity activity, int pageSize, int condition, ArrayList<String> urls) {
        this.newsList = (ArrayList<News>) newsList.clone();
        if (urls != null) this.urlsFromSearch = (ArrayList<String>) urls.clone();
        this.context = activity;
        this.pageSize = pageSize;
        this.condition = condition;
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
        System.out.println("[DiscoverFragment.onCreateView => DiscoverAdapter]: newsList=" + newsList);
        myDiscoverAdapter = new DiscoverAdapter(newsList, context, DiscoverFragment.this, myLayoutManager, launcher);
        result.setAdapter(myDiscoverAdapter);
        result.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
//                     && newState == RecyclerView.SCROLL_STATE_DRAGGING
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


    ActivityResultLauncher<String> launcher = registerForActivityResult(new ResultContract(), new ActivityResultCallback<String>() {
        @Override
        public void onActivityResult(String result) {
            String[] message = result.split(",");
            int pos = Integer.parseInt(message[0]);
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

    private void getNewsList(State state) {
        if (condition == 0) {
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

        else if (condition == 1) {
            String s = "";
            String tmpUrl = "";
            for (int i = 0; i < urlsFromSearch.size(); ++i) {
                String str = urlsFromSearch.get(i);
                if (getQueryString(str, "page") == null) {
                    tmpUrl = str + "&page=2";
                    urlsFromSearch.set(i, str + "&page=2");
                }
                else {
                    int currentPage = Integer.parseInt(getQueryString(str, "page"));
                    tmpUrl = replace(str, "page", Integer.toString(++currentPage));
                    urlsFromSearch.set(i, tmpUrl);
                }
                System.out.println("[SearchFragment]" + tmpUrl);
                try {
                    URL url  = new URL(tmpUrl);
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

    public static String getQueryString (String url, String tag) {
        try {
            Uri uri = Uri.parse(url);
            return uri.getQueryParameter(tag);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String replace (String url, String key, String value) {
        if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(key)) {
            url = url.replaceAll("(" + key + "=[^&]*)", key + "=" + value);
        }
        return url;
    }
}