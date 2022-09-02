package com.java.zhangshiying;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.MyViewHolder> {

    ArrayList<News> newsListDisplay;
    Context context;
    Fragment myFragment;
    LinearLayoutManager myLayoutManager;
    View view;
    private ActivityResultLauncher launcher;

    int color; //0=blue, 1=pink


    private class FavoritesHandler extends Handler {
        private final WeakReference<FavoritesFragment> myFragment;
        public FavoritesHandler(FavoritesFragment fragment) {
            myFragment = new WeakReference<FavoritesFragment>(fragment);
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
                        e.printStackTrace();
                        System.out.println("[" + pos + "]: handleMessage error");
                        Log.e("loadImage", "error");
                    }
                    break;
                case 2: //two images
                    int pos_case_2 = msg.getData().getInt("position");
                    LinearLayout images;
                    try {
                        images = (LinearLayout) myLayoutManager.findViewByPosition(pos_case_2).findViewById(R.id.images);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("R.id.images not founded");
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
                            e.printStackTrace();
                            System.out.println("[" + pos_case_2 + "]: handleMessage (image_2) error");
                            Log.e("loadImage2", "error");
                        }
                    }
                    else if (images.getVisibility() != View.VISIBLE) {
                        try {
                            myMap.put(pos_case_2, (Bitmap)msg.obj);
                            mapHelper.put(pos_case_2, msg.getData().getInt("label"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("[" + pos_case_2 + "]: handleMessage (image_1) error");
                            Log.e("loadImage1", "error");
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private final FavoritesAdapter.FavoritesHandler myHandler = new FavoritesAdapter.FavoritesHandler((FavoritesFragment) myFragment);





    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = View.inflate(context, R.layout.news_card_layout, null);
        return new FavoritesAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final int pos = position;
        holder.setIsRecyclable(false);
        News news = newsListDisplay.get(position);
        if (color == 0) {
            holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.light_teal));
            holder.card.setRippleColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_teal)));
            holder.category.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.teal_700)));
        }
        else {
            holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.light_grey));
            holder.card.setRippleColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_grey)));
            holder.category.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dark_grey)));
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

        //display images
        if (news.imageExist) {
            boolean twoImages = false;
            if (news.imageCount >= 2) {
                twoImages = true;
                getBitmapFromURL(news.imageUrls.get(0), news.imageUrls.get(1), pos, twoImages);
            }
            else getBitmapFromURL(news.imageUrls.get(0), "NO OTHER IMAGE!", pos, twoImages);
        }
        else holder.image.setVisibility(View.GONE);
        if (news.videoExist) {
            assert(false);
        }

        view.findViewById(R.id.materialCardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                News news = newsListDisplay.get(pos);
                news.pos = pos;
                System.out.println("[FavoritesAdapter]: news = " + news);
                Gson gson = new Gson();
                String send = gson.toJson(news);
                launcher.launch(send);
            }
        });

    }

    @Override
    public int getItemCount() {
        return newsListDisplay == null ? 0 : newsListDisplay.size();
    }

    public FavoritesAdapter(ArrayList<News> favNewsList, Context activity, Fragment fragment, LinearLayoutManager myLayoutManager, int color, ActivityResultLauncher launcher) {
        if (favNewsList != null ) newsListDisplay = (ArrayList<News>) favNewsList.clone();
        context = activity;
        myFragment = fragment;
        this.myLayoutManager = myLayoutManager;
        this.color = color;
        this.launcher = launcher;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, origin, time;
        ImageView image;
        VideoView video;
        ImageView closeBtn;
        MaterialCardView card;

        public MyViewHolder(View itemView) {
            super(itemView);
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

    public void removeData(int position) {
        newsListDisplay.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
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


}
