package com.java.zhangshiying;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.MyViewHolder> {
    Context mainActivityContext;
    Fragment fragmentContext;
    LinearLayoutManager myLayoutManager;
    private ActivityResultLauncher launcher;

    ArrayList<News> newsList = null;
    View view;

    private class DiscoverHandler extends Handler {
        private final WeakReference<DiscoverFragment> myFragment;
        public DiscoverHandler(DiscoverFragment fragment) {
            myFragment = new WeakReference<DiscoverFragment>(fragment);
        }

        HashMap<Integer, Bitmap> myMap = new HashMap<>(); //<position, image>
        HashMap<Integer, Integer> mapHelper = new HashMap<>(); //<position, label>

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case 1: //one image only
                    int pos = msg.getData().getInt("position");
                    try {
                        ImageView iv = (ImageView) myLayoutManager.findViewByPosition(pos).findViewById(R.id.image);
                        iv.setVisibility(View.VISIBLE);
                        iv.setImageBitmap((Bitmap)msg.obj);
                    } catch (Exception e) {
//                        e.printStackTrace();
                        System.out.println("E [DiscoverAdapter.handleTitleImage] pos=" + pos + ": R.id.image not found");
                        Log.e("loadImage", "error");
                    }
                    break;
                case 2: //two images
                    int pos_case_2 = msg.getData().getInt("position");
                    LinearLayout images;
                    try {
                        images = (LinearLayout) myLayoutManager.findViewByPosition(pos_case_2).findViewById(R.id.images);
                    } catch (Exception e) {
//                        e.printStackTrace();
                        System.out.println("E [DiscoverAdapter.handleTitleImages] pos=" + pos_case_2 + ": R.id.images not found");
                        break;
                    }
                    if (myMap.containsKey(pos_case_2)
                            && images.getVisibility() != View.VISIBLE
                            && msg.getData().getInt("label") != mapHelper.get(pos_case_2)) {
                        try {
                            images.setVisibility(View.VISIBLE);
                            ImageView iv_1 = (ImageView) myLayoutManager.findViewByPosition(pos_case_2).findViewById(R.id.image_1);
                            iv_1.setImageBitmap(myMap.get(pos_case_2));
                            ImageView iv_2 = (ImageView) myLayoutManager.findViewByPosition(pos_case_2).findViewById(R.id.image_2);
                            iv_2.setImageBitmap((Bitmap)msg.obj);
                            myMap.remove(pos_case_2);
                        } catch (Exception e) {
//                            e.printStackTrace();
                            System.out.println("E [DiscoverAdapter.handleTitleImages] pos=" + pos_case_2 + ": image_2 error");
                            Log.e("loadImage2", "error");
                        }
                    }
                    else if (images.getVisibility() != View.VISIBLE) {
                        try {
                            myMap.put(pos_case_2, (Bitmap)msg.obj);
                            mapHelper.put(pos_case_2, msg.getData().getInt("label"));
                        } catch (Exception e) {
//                            e.printStackTrace();
                            System.out.println("E [DiscoverAdapter.handleTitleImages] pos=" + pos_case_2 + ": image_1 error");
                            Log.e("loadImage1", "error");
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private final DiscoverHandler myHandler = new DiscoverHandler((DiscoverFragment) fragmentContext);



    public DiscoverAdapter(ArrayList<News> newsList, Context context, Fragment fragment, LinearLayoutManager myLayoutManager, ActivityResultLauncher launcher) {
        this.mainActivityContext = context;
        this.newsList = newsList;
        this.fragmentContext = fragment;
        this.myLayoutManager = myLayoutManager;
        this.launcher = launcher;
        System.out.println("[DiscoverAdapter.Constructor]: newsList = " + newsList);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, origin, time;
        ImageView image;
        VideoView video;
        ImageView closeBtn;
        MaterialCardView card;
        LinearLayout images;

        public MyViewHolder(View itemView) {
            super(itemView);
            images = itemView.findViewById(R.id.images);
            card = itemView.findViewById(R.id.materialCardView);
            title = itemView.findViewById(R.id.textTitle);
            category = itemView.findViewById(R.id.textCategory);
            origin = itemView.findViewById(R.id.textOrigin);
            time = itemView.findViewById(R.id.textTime);
            image = itemView.findViewById(R.id.image);
            video = itemView.findViewById(R.id.video);
            closeBtn = itemView.findViewById(R.id.close_btn);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = View.inflate(mainActivityContext, R.layout.news_card_layout, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final int pos = position;
        holder.setIsRecyclable(false);
        News news = newsList.get(position);
        if (news.read) {
            holder.card.setStrokeColor(ContextCompat.getColor(mainActivityContext, R.color.light_grey));
            holder.card.setRippleColor(ColorStateList.valueOf(ContextCompat.getColor(mainActivityContext, R.color.light_grey)));
            holder.category.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(mainActivityContext, R.color.dark_grey)));
        }
        holder.title.setText(news.title);
        holder.category.setText(news.category);
        holder.origin.setText(news.origin);
        holder.time.setText(news.time);
        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeData(pos);
            }
        });

        if (news.imageExist) {
            if (news.imageCount >= 2) {
                boolean cannotLoadFromLocal = false;
                if (news.read) {
                    try {
                        ((ImageView) holder.images.findViewById(R.id.image_1)).setImageBitmap(getBitmapFromHis(news.newsID, 0));
                        ((ImageView) holder.images.findViewById(R.id.image_2)).setImageBitmap(getBitmapFromHis(news.newsID, 1));
                        holder.images.setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        cannotLoadFromLocal = true;
                        System.out.println("E [DiscoverAdapter.loadTitleImagesFromLocal] pos=" + pos + ": R.id.images not found");
//                        e.printStackTrace();
                    }
                }
                if (cannotLoadFromLocal || !news.read) {
                    getBitmapFromURL(news.imageUrls.get(0), news.imageUrls.get(1), pos, true);
                }
            }
            else {
                boolean cannotLoadFromLocal = false;
                if (news.read) {
                    try {
                        ImageView image = (ImageView) myLayoutManager.findViewByPosition(pos).findViewById(R.id.image);
                        image.setImageBitmap(getBitmapFromHis(news.newsID, 0));
                        holder.image.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        cannotLoadFromLocal = true;
                        System.out.println("E [DiscoverAdapter.loadTitleImageFromLocal] pos=" + pos + ": R.id.image not found");
//                        e.printStackTrace();
                    }
                }
                if (cannotLoadFromLocal || !news.read)  getBitmapFromURL(news.imageUrls.get(0), "NO OTHER IMAGE!", pos, false);
            }
        }
        if (news.videoExist) {
            assert(false);
        }

        view.findViewById(R.id.materialCardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                News news = newsList.get(pos);
                news.pos = pos;
                if (!news.read) {
                    news.read = true;
                    HistoryFragment.historyNewsList.add(news);
                    Storage.write(mainActivityContext.getApplicationContext(), "historyNewsList", Storage.joinNewsList(HistoryFragment.historyNewsList));
                }
                System.out.println("[DiscoverAdapter.onClick]: [pos] = " + pos + ", [news] = " + news.title + " @ " + news);
                Gson gson = new Gson();
                String send = gson.toJson(news);
                launcher.launch(send);
            }
        });
    }




    private void getBitmapFromURL(String src, String src2, int pos, boolean twoImages) {
        if (!twoImages) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(src);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
//                        System.out.println("[" + pos + "]: SUCCESS src = " + src);
//                        Log.e("Bitmap","returned");

                        Message msg = new Message();

                        Bundle bundle = new Bundle();
                        bundle.putInt("position", pos);
                        msg.setData(bundle);
                        msg.obj = myBitmap;
                        msg.what = 1;
                        myHandler.sendMessage(msg);

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("[" + pos + "]: ERROR src = " + src);
                        Log.e("Exception",e.getMessage());
                    }
                }
            }).start();
        }

        else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(src);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
//                        System.out.println("[" + pos + "]: SUCCESS src1 = " + src);
//                        Log.e("Bitmap1","returned");

                        Message msg = new Message();

                        Bundle bundle = new Bundle();
                        bundle.putInt("position", pos);
                        bundle.putInt("label", 1);
                        msg.setData(bundle);
                        msg.obj = myBitmap;
                        msg.what = 2;
                        myHandler.sendMessage(msg);

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("[" + pos + "]: ERROR src1 = " + src);
                        Log.e("Exception (image_1)",e.getMessage());
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(src2);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
//                        System.out.println("[" + pos + "]: SUCCESS src2 = " + src2);
//                        Log.e("Bitmap2","returned");

                        Message msg = new Message();

                        Bundle bundle = new Bundle();
                        bundle.putInt("position", pos);
                        bundle.putInt("label", 2);
                        msg.setData(bundle);
                        msg.obj = myBitmap;
                        msg.what = 2;
                        myHandler.sendMessage(msg);

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("[" + pos + "]: ERROR src2 = " + src2);
                        Log.e("Exception (image_2)",e.getMessage());
                    }
                }
            }).start();

        }
    }

    private void castVideo (VideoView vv) {
        MediaController mediaController = new MediaController(mainActivityContext);
        mediaController.setAnchorView(vv);
        mediaController.setMediaPlayer(vv);
        vv.setMediaController(mediaController);
        vv.start();
    }

    @Override
    public int getItemCount() {
        return newsList == null ? 0 : newsList.size();
    }

    public void addNewsList(ArrayList<News> newsList) {
        this.newsList.addAll(newsList);
        notifyDataSetChanged();
    }

    public void removeData(int position) {
        newsList.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    private Bitmap getBitmapFromHis(String newsID, int num) {
        for (News news : HistoryFragment.historyNewsList) {
            if (Objects.equals(news.newsID, newsID)) {
                if (news.images.size() > num)
                    return news.images.get(num);
            }
        }
        System.out.println("[DiscoverAdapter.getBitmapFromHis]: cannot find title image(s), newsID=" + newsID + ", num=" + num);
        return null;
    }
}
