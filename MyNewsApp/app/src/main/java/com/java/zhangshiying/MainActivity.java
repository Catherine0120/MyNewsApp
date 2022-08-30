package com.java.zhangshiying;

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

    int currentPage = 1;
    static final int pageSize = 40;

    private class MainHandler extends Handler {
        private final WeakReference<MainActivity> myParent;
        public MainHandler(MainActivity activity) {
            myParent = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            String message = msg.obj.toString();
            try {
                JSONObject obj = new JSONObject(message);
//                int pagesize = Integer.parseInt(obj.getString("pageSize"));
//                int total = obj.getInt("total");
                JSONArray newsDescriptions = obj.getJSONArray("data");
                ArrayList<News> newsDescriptionList = new ArrayList<>();
                for (int i = 0; i < newsDescriptions.length(); ++i) {
                    JSONObject singleNewsDescription = newsDescriptions.getJSONObject(i);
                    newsDescriptionList.add(new News(singleNewsDescription));
                }
                discoverFragment = new DiscoverFragment(newsDescriptionList, MainActivity.this, pageSize);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, discoverFragment).commit();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final MainHandler myHandler = new MainHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getDiscoverFragment();

        myBottomAppBar = findViewById(R.id.bottom_app_bar);
        setSupportActionBar(myBottomAppBar);

        SearchFragment searchFragment = new SearchFragment();
        FavoritesFragment favoritesFragment = new FavoritesFragment();

        myFloatingActionButton = findViewById(R.id.search_action_button);
        myFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, searchFragment).commit();
            }
        });

        myBottomNavigationView = findViewById(R.id.bottom_navigation);
        myBottomNavigationView.setSelectedItemId(R.id.menu_discover_news);
        myBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_discover_news:
                        getDiscoverFragment();
                        return true;

                    case R.id.menu_favorites:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, favoritesFragment).commit();
                        return true;
                }
                return false;
            }
        });

    }

    private void getDiscoverFragment() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = simpleDateFormat.format(date);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String myUrl = "https://api2.newsminer.net/svc/news/queryNewsList?size=%d&startDate=&endDate=%s&words=&categories=";
                myUrl = String.format(myUrl, pageSize, today);
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
                    msg.obj = s;
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