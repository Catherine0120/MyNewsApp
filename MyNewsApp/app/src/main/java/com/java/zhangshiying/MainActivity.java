package com.java.zhangshiying;

import static java.lang.Math.min;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.niwattep.materialslidedatepicker.SlideDatePickerDialogCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity  implements SlideDatePickerDialogCallback  {

    private BottomAppBar myBottomAppBar;
    private BottomNavigationView myBottomNavigationView;
    public FloatingActionButton myFloatingActionButton;

    public static boolean startDate = true;
    public static boolean searchButton = false;

    public static int[] currentPage = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    public static boolean[] firstLoad= {true, true, true, true, true, true, true, true, true, true, true};

    public DiscoverFragment discoverFragment;
    public SearchFragment searchFragment;
    public FavoritesFragment favoritesFragment;
    public HistoryFragment historyFragment;
    public FragmentBlank blankFragment;
    public FragmentBlank2 blankFragment2;
    public CollectionFragment collectionFragment;
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
                case 2: //searchFragment-switch-to-discoverFragment
                    tmpCount++;
                    String message = msg.obj.toString();
                    int total = msg.getData().getInt("total");
                    String url = msg.getData().getString("url");
                    urls.add(url);
                    try {
                        JSONObject obj = new JSONObject(message);
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
                        discoverFragment = new DiscoverFragment(tmpNewsDescriptionList, MainActivity.this, urls);
                        loadPulse.setVisibility(View.INVISIBLE);
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
    public void onPositiveClick(int date, int month, int year, Calendar calendar) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (startDate) searchFragment.display_start_date.setText(format.format(calendar.getTime()));
        else searchFragment.display_end_date.setText(format.format(calendar.getTime()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        collectionFragment = new CollectionFragment(MainActivity.this);
        searchFragment = new SearchFragment();
        favoritesFragment = new FavoritesFragment();
        historyFragment = new HistoryFragment();
        blankFragment = new FragmentBlank();
        blankFragment2 = new FragmentBlank2();

        loadPulse = findViewById(R.id.spin_kit_main);

        myBottomAppBar = findViewById(R.id.bottom_app_bar);
        setSupportActionBar(myBottomAppBar);

        myFloatingActionButton = findViewById(R.id.search_action_button);
        myFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!searchButton) {
                    searchButton = !searchButton;
                    getSupportFragmentManager().beginTransaction().replace(R.id.search_fragment, searchFragment).commit();
                    myBottomNavigationView.getMenu().getItem(3).setChecked(true); //blank_item
                }
                else {
                    searchButton = !searchButton;
                    getSupportFragmentManager().beginTransaction().replace(R.id.search_fragment, blankFragment).commit();
                    myBottomNavigationView.getMenu().getItem(0).setChecked(true); //blank_item

                    TextView tv1 = searchFragment.display_start_date;
                    TextView tv2 = searchFragment.display_end_date;
                    String start_date = "startDate=" + tv1.getText();
                    String end_date = "endDate=" + tv2.getText();

                    String myTmpUrl = "https://api2.newsminer.net/svc/news/queryNewsList?size=%d";
                    if (searchFragment.categories.size() != 0) myTmpUrl = String.format(myTmpUrl, pageSize * 2 / searchFragment.categories.size());
                    else myTmpUrl = String.format(myTmpUrl, pageSize);
                    myTmpUrl = myTmpUrl + "&" + start_date + "&" + end_date + "&words=" + searchFragment.searchBar.getText();
                    for (String category : searchFragment.categories) {
                        String tmpUrl = myTmpUrl + "&categories=" + category;
                        loadPulse.setVisibility(View.VISIBLE);
                        getSearchFragment(tmpUrl, searchFragment.categories.size());
                    }
                    if (searchFragment.categories.size() == 0) {
                        String tmpUrl = myTmpUrl + "&categories=";
                        loadPulse.setVisibility(View.VISIBLE);
                        getSearchFragment(tmpUrl, 1);
                    }
                }
            }
        });

        myBottomNavigationView = findViewById(R.id.bottom_navigation);
        myBottomNavigationView.setSelectedItemId(R.id.menu_discover_news);
        myBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_discover_news:
                        getSupportFragmentManager().beginTransaction().replace(R.id.search_fragment, blankFragment).commit();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, collectionFragment).commit();
                        return true;

                    case R.id.menu_favorites:
                        getSupportFragmentManager().beginTransaction().replace(R.id.search_fragment, blankFragment).commit();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, favoritesFragment).commit();
                        return true;

                    case R.id.menu_history:
                        getSupportFragmentManager().beginTransaction().replace(R.id.search_fragment, blankFragment).commit();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, historyFragment).commit();
                        return true;
                }
                return false;
            }
        });

    }

    public void getSearchFragment(String myUrl, int total) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String s = "";
                try {
                    URL url  = new URL(myUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    InputStream inputStream = conn.getInputStream();
                    s = MainActivity.readFromStream(inputStream);
                    System.out.println("[MainActivity.getSearchFragment]: [" + myUrl + "] " + s);
                    Bundle bundle = new Bundle();
                    bundle.putInt("total", total);
                    bundle.putString("url", myUrl);
                    Message msg = new Message();
                    msg.setData(bundle);
                    msg.obj = s;
                    msg.what = 2;
                    myHandler.sendMessage(msg);
                } catch (Exception e) {
                    Looper.prepare();
                    loadPulse.setVisibility(View.INVISIBLE);
                    discoverFragment = new DiscoverFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, discoverFragment).commit();
                    Toast.makeText(MainActivity.this, "Network Failure", Toast.LENGTH_SHORT).show();
                    Looper.loop();
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