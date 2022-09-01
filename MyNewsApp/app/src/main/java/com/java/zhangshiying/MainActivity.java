package com.java.zhangshiying;

import static java.lang.Math.min;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

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

public class MainActivity extends AppCompatActivity {

    private BottomAppBar myBottomAppBar;
    private BottomNavigationView myBottomNavigationView;
    private FloatingActionButton myFloatingActionButton;

    DiscoverFragment discoverFragment;
    private View loadPulse;

    static final int pageSize = 20;

    ArrayList<News> tmpNewsDescriptionList = new ArrayList<>();
    ArrayList<String> urls = new ArrayList<>();
    int tmpCount = 0;

    private class MainHandler extends Handler {
        private final WeakReference<MainActivity> myParent;
        public MainHandler(MainActivity activity) {
            myParent = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case 1: //navigation-switch-to-discoverFragment
                    String message = msg.obj.toString();
                    try {
                        JSONObject obj = new JSONObject(message);
                        JSONArray newsDescriptions = obj.getJSONArray("data");
                        ArrayList<News> newsDescriptionList = new ArrayList<>();
                        for (int i = 0; i < newsDescriptions.length(); ++i) {
                            JSONObject singleNewsDescription = newsDescriptions.getJSONObject(i);
                            newsDescriptionList.add(new News(singleNewsDescription));
                        }
                        System.out.println("[MainActivity.FirstCreate => DiscoverFragment]" + newsDescriptionList);
                        discoverFragment = new DiscoverFragment(newsDescriptionList, MainActivity.this, pageSize, 0, null);
                        loadPulse.setVisibility(View.INVISIBLE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, discoverFragment).commit();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 2: //searchFragment-switch-to-discoverFragment
                    tmpCount++;
                    String message_case_2 = msg.obj.toString();
                    int total = msg.getData().getInt("total");
                    String url = msg.getData().getString("url");
                    urls.add(url);
                    try {
                        JSONObject obj = new JSONObject(message_case_2);
                        JSONArray newsDescriptions = obj.getJSONArray("data");
                        for (int i = 0; i < newsDescriptions.length(); ++i) {
                            JSONObject singleNewsDescription = newsDescriptions.getJSONObject(i);
                            tmpNewsDescriptionList.add(new News(singleNewsDescription));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (total == tmpCount) {
                        if (tmpNewsDescriptionList.size() == 0) Toast.makeText(MainActivity.this, "Sorry, no result matches your search...", Toast.LENGTH_LONG).show();
                        discoverFragment = new DiscoverFragment(tmpNewsDescriptionList, MainActivity.this, min(tmpNewsDescriptionList.size(), 20), 1, urls);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, discoverFragment).commit();
                        tmpNewsDescriptionList.clear();
                        tmpCount = 0;
                        urls.clear();
                    }
                    break;
                default:
                    break;
            }

        }
    }

    private final MainHandler myHandler = new MainHandler(this);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getDiscoverFragment(true);

        loadPulse = findViewById(R.id.spin_kit_main);

        myBottomAppBar = findViewById(R.id.bottom_app_bar);
        setSupportActionBar(myBottomAppBar);

        SearchFragment searchFragment = new SearchFragment(pageSize);
        FavoritesFragment favoritesFragment = new FavoritesFragment();

        myFloatingActionButton = findViewById(R.id.search_action_button);
        myFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, searchFragment).commit();
                myBottomNavigationView.getMenu().getItem(1).setChecked(true); //blank_item

            }
        });

        myBottomNavigationView = findViewById(R.id.bottom_navigation);
        myBottomNavigationView.setSelectedItemId(R.id.menu_discover_news);
        myBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_discover_news:
                        loadPulse.bringToFront();
                        loadPulse.setVisibility(View.VISIBLE);
                        getDiscoverFragment(false);
                        return true;

                    case R.id.menu_favorites:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, favoritesFragment).commit();
                        return true;
                }
                return false;
            }
        });

    }

    private void getDiscoverFragment(boolean firstLoad) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = simpleDateFormat.format(date);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String myUrl = "";
                if (firstLoad) myUrl = "https://api2.newsminer.net/svc/news/queryNewsList?size=%d&startDate=&endDate=%s&words=&categories=";
                else {
                    myUrl = "https://api2.newsminer.net/svc/news/queryNewsList?size=%d&startDate=&endDate=%s&words=&categories=&page=%d";
                    discoverFragment.currentPage++;
                }
                myUrl = String.format(myUrl, pageSize, today, discoverFragment.currentPage);
                System.out.println(myUrl);
                String s = "";
                try {
                    URL url  = new URL(myUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    InputStream inputStream = conn.getInputStream();
                    s = readFromStream(inputStream);
                    System.out.println("[MainActivity.crawl]: s = " + s);
                    Message msg = new Message();
                    msg.obj = s;
                    msg.what = 1;
                    myHandler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getSearchFragment(String myUrl, int total) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(myUrl);
                String s = "";
                try {
                    URL url  = new URL(myUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    InputStream inputStream = conn.getInputStream();
                    s = MainActivity.readFromStream(inputStream);
                    System.out.println("[" + myUrl + "]: " + s);
                    Bundle bundle = new Bundle();
                    bundle.putInt("total", total);
                    bundle.putString("url", myUrl);
                    Message msg = new Message();
                    msg.setData(bundle);
                    msg.obj = s;
                    msg.what = 2;
                    myHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }




    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public static String readFromStream(InputStream inStream) {
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