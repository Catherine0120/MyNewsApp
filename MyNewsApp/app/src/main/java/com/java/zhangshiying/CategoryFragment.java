package com.java.zhangshiying;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

public class CategoryFragment extends Fragment {
    int category_label = 0;
    String category_chinese = "";

    Context mainActivityContext;
    String today = "";

    private SwipeRefreshLayout mySwipeRefreshView;
    private RecyclerView result;
    private View loadPulse;

    final Handler handler = new Handler();

    private CategoryFragmentAdapter myCategoryAdapter;

    public enum State {
        DROP_AND_REFRESH, SCROLL_AND_LOAD
    }

    private class MainHandler extends Handler {
        private final WeakReference<CategoryFragment> myParent;
        public MainHandler(CategoryFragment fragment) {
            myParent = new WeakReference<CategoryFragment>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            String message = msg.obj.toString();
            switch(msg.what) {
                case(1):
                    try {
                        JSONObject obj = new JSONObject(message);
                        JSONArray newsDescriptions = obj.getJSONArray("data");
                        for (int i = 0; i < newsDescriptions.length(); ++i) {
                            JSONObject singleNewsDescription = newsDescriptions.getJSONObject(i);
                            Storage.tmpNewsList.get(category_label).add(new News(singleNewsDescription));
                        }
//                        System.out.println("[CategoryFragment.FirstCreate]: " + Storage.tmpNewsList.get(category_label));
                        LinearLayoutManager myLayoutManager = new LinearLayoutManager(CategoryFragment.this.getContext());
                        result.setLayoutManager(myLayoutManager);
                        myCategoryAdapter = new CategoryFragmentAdapter(mainActivityContext, CategoryFragment.this, myLayoutManager, launcher, category_label);
                        result.setAdapter(myCategoryAdapter);
                        loadPulse.setVisibility(View.INVISIBLE);
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }
                    break;
                case(2):
                    try {
                        JSONObject obj = new JSONObject(message);
                        JSONArray newsDescriptions = obj.getJSONArray("data");
                        ArrayList<News> newsDescriptionList = new ArrayList<>();
                        for (int i = 0; i < newsDescriptions.length(); ++i) {
                            JSONObject singleNewsDescription = newsDescriptions.getJSONObject(i);
                            newsDescriptionList.add(new News(singleNewsDescription));
                        }
                        if (msg.getData().getInt("state") == 0) { //DROP_AND_REFRESH
                            LinearLayoutManager myLayoutManager = new LinearLayoutManager(CategoryFragment.this.getContext());
                            result.setLayoutManager(myLayoutManager);
                            Storage.tmpNewsList.get(category_label).clear();
                            Storage.tmpNewsList.get(category_label).addAll(newsDescriptionList);
                            myCategoryAdapter = new CategoryFragmentAdapter(mainActivityContext, CategoryFragment.this, myLayoutManager, launcher, category_label);
                            result.setAdapter(myCategoryAdapter);
                        }
                        else { //SCROLL_AND_LOAD
                            Storage.tmpNewsList.get(category_label).addAll(newsDescriptionList);
                            assert(myCategoryAdapter != null);
                            myCategoryAdapter.notifyChanged();
                            loadPulse.setVisibility(View.INVISIBLE);
                        }

                    } catch (Exception e) {
//                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private final CategoryFragment.MainHandler myHandler = new CategoryFragment.MainHandler(this);



    public CategoryFragment(Context mainActivityContext) {
        this.mainActivityContext = mainActivityContext;
        initNewsList();
    }

    private void initNewsList() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        today = simpleDateFormat.format(date);

        if (Objects.equals(Storage.findValue(GlobalApplication.getAppContext(), "today"), today)) {
            for (int i = 0; i < 11; ++i) {
                MainActivity.currentPage[i] = Storage.findPageValue(GlobalApplication.getAppContext(), i);
                if (MainActivity.currentPage[i] != 1) MainActivity.firstLoad[i] = false;
            }
        }
        else Storage.write(GlobalApplication.getAppContext(), "today", today);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.category_fragment, container, false);
        result = currentView.findViewById(R.id.rv_cat);
        loadPulse = currentView.findViewById(R.id.spin_kit_cat);

        mySwipeRefreshView = (SwipeRefreshLayout) currentView.findViewById(R.id.refresh_cat);
        mySwipeRefreshView.setColorSchemeColors(ResourcesCompat.getColor(getResources(), R.color.pink, null),
                ResourcesCompat.getColor(getResources(), R.color.dark_pink, null),
                ResourcesCompat.getColor(getResources(), R.color.purple_200, null));
        mySwipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mySwipeRefreshView.setRefreshing(true);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        getNewsList(CategoryFragment.State.DROP_AND_REFRESH);
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

        result.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
//                     && newState == RecyclerView.SCROLL_STATE_DRAGGING
                    Toast.makeText(getContext(), "Discovering more news...", Toast.LENGTH_SHORT).show();
                    loadPulse.setVisibility(View.VISIBLE);
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            getNewsList(CategoryFragment.State.SCROLL_AND_LOAD);
                        }
                    }.start();
                }
            }
        });

        return currentView;
    }

    ActivityResultLauncher<String> launcher = registerForActivityResult(new CategoryFragment.ResultContract(), new ActivityResultCallback<String>() {
        @Override
        public void onActivityResult(String result) {
            String[] message = result.split(",");
            String newsID = message[0];
            int pos = Integer.parseInt(message[1]);
            News news = Storage.findNewsValue(GlobalApplication.getAppContext(), newsID);
            Storage.tmpNewsList.get(category_label).get(pos).images = (ArrayList<String>) news.images.clone();
//            System.out.println("[CategoryFragment] news result received: [pos]=" + pos + ", [news.title]=" + Storage.tmpNewsList.get(category_label).get(pos).title);
            Storage.tmpNewsList.get(category_label).get(pos).readDetail = true;
            if (message.length == 4) {
                Storage.tmpNewsList.get(category_label).get(pos).like = true;
                Storage.tmpNewsList.get(category_label).get(pos).fav = true;
            }
            else if (message.length == 3) {
                if (Objects.equals(message[2], "like")) {
                    Storage.tmpNewsList.get(category_label).get(pos).like = true;
                    Storage.tmpNewsList.get(category_label).get(pos).fav = false;
                }
                if (Objects.equals(message[2], "fav")) {
                    Storage.tmpNewsList.get(category_label).get(pos).like = false;
                    Storage.tmpNewsList.get(category_label).get(pos).fav = true;
                }
            }
            else {
                assert(message.length == 2);
                Storage.tmpNewsList.get(category_label).get(pos).like = false;
                Storage.tmpNewsList.get(category_label).get(pos).fav = false;
            }
            myCategoryAdapter.notifyDataSetChanged();
        }
    });

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



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        loadPulse.bringToFront();
        loadPulse.setVisibility(View.VISIBLE);
        Bundle args = getArguments();
        category_label = args.getInt("category_label");
        switch(category_label) {
            case(0):
                category_chinese = "";
                break;
            case(1):
                category_chinese = "社会";
                break;
            case(2):
                category_chinese = "汽车";
                break;
            case(3):
                category_chinese = "科技";
                break;
            case(4):
                category_chinese = "体育";
                break;
            case(5):
                category_chinese = "财经";
                break;
            case(6):
                category_chinese = "健康";
                break;
            case(7):
                category_chinese = "文化";
                break;
            case(8):
                category_chinese = "娱乐";
                break;
            case(9):
                category_chinese = "军事";
                break;
            case(10):
                category_chinese = "教育";
                break;
            default:
                category_chinese = "";
                break;
        }

        if (Storage.tmpNewsList.get(category_label).size() == 0) {
            getCategoryFragment();
        }
        else {
            LinearLayoutManager myLayoutManager = new LinearLayoutManager(CategoryFragment.this.getContext());
            result.setLayoutManager(myLayoutManager);
            myCategoryAdapter = new CategoryFragmentAdapter(mainActivityContext, CategoryFragment.this, myLayoutManager, launcher, category_label);
            result.setAdapter(myCategoryAdapter);
            loadPulse.setVisibility(View.INVISIBLE);
        }
    }

    private void getCategoryFragment() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String myUrl = "";
                if (MainActivity.firstLoad[category_label]) myUrl = "https://api2.newsminer.net/svc/news/queryNewsList?size=%d&startDate=&endDate=%s&words=&categories=%s";
                else {
                    myUrl = "https://api2.newsminer.net/svc/news/queryNewsList?size=%d&startDate=&endDate=%s&words=&categories=%s&page=%d";
                    MainActivity.currentPage[category_label]++;
                }
                MainActivity.firstLoad[category_label] = false;

                Storage.write(GlobalApplication.getAppContext(), "currentPage", Storage.updateCurrentPage(MainActivity.currentPage));

                myUrl = String.format(myUrl, MainActivity.pageSize, today, category_chinese, MainActivity.currentPage[category_label]);
//                System.out.println(myUrl);
                String s = "";
                try {
                    URL url  = new URL(myUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    InputStream inputStream = conn.getInputStream();
                    s = readFromStream(inputStream);
//                    System.out.println("[Category.crawl]: s = " + s);
                    Message msg = new Message();
                    msg.obj = s;
                    msg.what = 1;
                    myHandler.sendMessage(msg);
                } catch (Exception e) {
                    Looper.prepare();
                    Toast.makeText(getContext(), "Network Failure", Toast.LENGTH_LONG).show();
                    Looper.loop();
//                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getNewsList(CategoryFragment.State state) {
        String myUrl = "https://api2.newsminer.net/svc/news/queryNewsList?size=%d&startDate=&endDate=%s&words=&categories=%s&page=%d";
        myUrl = String.format(myUrl, MainActivity.pageSize, today, category_chinese, ++MainActivity.currentPage[category_label]);

        Storage.write(GlobalApplication.getAppContext(), "currentPage", Storage.updateCurrentPage(MainActivity.currentPage));

//        System.out.println("[CategoryFragment]: URL=" + myUrl);
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
            msg.what = 2;
            msg.obj = s;
            msg.setData(bundle);
            myHandler.sendMessage(msg);
        } catch (Exception e) {
            Looper.prepare();
            Toast.makeText(getContext(), "Network Failure", Toast.LENGTH_LONG).show();
            Looper.loop();
//            e.printStackTrace();
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
//            e.printStackTrace();
        }
        return s;
    }

}
