package com.java.zhangshiying;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.niwattep.materialslidedatepicker.SlideDatePickerDialog;
import com.niwattep.materialslidedatepicker.SlideDatePickerDialogCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SearchFragment extends Fragment {
    Button techBtn, militaryBtn, societyBtn, healthBtn, entertainBtn, cultureBtn, educationBtn, financeBtn, sportsBtn, carBtn;
    Button start_date_btn, end_date_btn;
    TextView display_start_date, display_end_date;
    int techBtnCount = 0;
    int militaryBtnCount = 0;
    int societyBtnCount = 0;
    int healthBtnCount = 0;
    int entertainBtnCount = 0;
    int cultureBtnCount = 0;
    int educationBtnCount = 0;
    int financeBtnCount = 0;
    int sportsBtnCount = 0;
    int carBtnCount = 0;

    MaterialSearchBar searchBar;

    Set<String> categories = new HashSet<String>();
    String today;

    public SearchFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        today = simpleDateFormat.format(date);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        techBtn = (Button) view.findViewById(R.id.tech);    techBtn.setOnClickListener(myBtnOnClickListener);
        militaryBtn = (Button) view.findViewById(R.id.military);    militaryBtn.setOnClickListener(myBtnOnClickListener);
        societyBtn = (Button) view.findViewById(R.id.society);  societyBtn.setOnClickListener(myBtnOnClickListener);
        healthBtn = (Button) view.findViewById(R.id.health);    healthBtn.setOnClickListener(myBtnOnClickListener);
        entertainBtn = (Button) view.findViewById(R.id.entertain);  entertainBtn.setOnClickListener(myBtnOnClickListener);
        cultureBtn = (Button) view.findViewById(R.id.culture);  cultureBtn.setOnClickListener(myBtnOnClickListener);
        educationBtn = (Button) view.findViewById(R.id.education);  entertainBtn.setOnClickListener(myBtnOnClickListener);
        financeBtn = (Button) view.findViewById(R.id.finance);  financeBtn.setOnClickListener(myBtnOnClickListener);
        sportsBtn = (Button) view.findViewById(R.id.sports);    sportsBtn.setOnClickListener(myBtnOnClickListener);
        carBtn = (Button) view.findViewById(R.id.car);  carBtn.setOnClickListener(myBtnOnClickListener);

        start_date_btn = (Button) view.findViewById(R.id.start_date_btn);   start_date_btn.setOnClickListener(myBtnOnClickListener);
        end_date_btn = (Button) view.findViewById(R.id.end_date_btn);   end_date_btn.setOnClickListener(myBtnOnClickListener);

        display_start_date = (TextView) view.findViewById(R.id.display_start_date);
        display_end_date = (TextView) view.findViewById(R.id.display_end_date);

        searchBar = (MaterialSearchBar) view.findViewById(R.id.searchBar);

        return view;
    }

    private final View.OnClickListener myBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.start_date_btn:
                    MainActivity.startDate = true;
                    start_date_btn.setSelected(true);
                    Calendar endDate = Calendar.getInstance();
                    endDate.set(Calendar.YEAR, 2022);
                    SlideDatePickerDialog.Builder builder = new SlideDatePickerDialog.Builder();
                    builder.setEndDate(endDate);
                    builder.setThemeColor(getResources().getColor(R.color.dark_pink, null)).setHeaderDateFormat("yyyy-MM-dd");
                    builder.setLocale(Locale.ENGLISH);
                    SlideDatePickerDialog dialog = builder.build();
                    dialog.show(getActivity().getSupportFragmentManager(), "Dialog");
                    break;

                case R.id.end_date_btn:
                    MainActivity.startDate = false;
                    end_date_btn.setSelected(true);
                    Calendar endDate2 = Calendar.getInstance();
                    endDate2.set(Calendar.YEAR, 2022);
                    SlideDatePickerDialog.Builder builder2 = new SlideDatePickerDialog.Builder();
                    builder2.setEndDate(endDate2);
                    builder2.setThemeColor(getResources().getColor(R.color.dark_pink, null)).setHeaderDateFormat("yyyy-MM-dd");
                    builder2.setLocale(Locale.ENGLISH);
                    SlideDatePickerDialog dialog2 = builder2.build();
                    dialog2.show(getActivity().getSupportFragmentManager(), "Dialog");
                    break;

                case R.id.tech:
                    techBtnCount++;
                    if (techBtnCount % 2 == 1) categories.add("科技");
                    else categories.remove("科技");
                    break;
                case R.id.military:
                    militaryBtnCount++;
                    if (militaryBtnCount % 2 == 1) categories.add("军事");
                    else categories.remove("军事");
                    break;
                case R.id.society:
                    societyBtnCount++;
                    if (societyBtnCount % 2 == 1) categories.add("社会");
                    else categories.remove("社会");
                    break;
                case R.id.health:
                    healthBtnCount++;
                    if (healthBtnCount % 2 == 1) categories.add("健康");
                    else categories.remove("健康");
                    break;
                case R.id.entertain:
                    entertainBtnCount++;
                    if (entertainBtnCount % 2 == 1) categories.add("娱乐");
                    else categories.remove("娱乐");
                    break;
                case R.id.culture:
                    cultureBtnCount++;
                    if (cultureBtnCount % 2 == 1) categories.add("文化");
                    else categories.remove("文化");
                    break;
                case R.id.education:
                    educationBtnCount++;
                    if (educationBtnCount % 2 == 1) categories.add("教育");
                    else categories.remove("教育");
                    break;
                case R.id.finance:
                    financeBtnCount++;
                    if (financeBtnCount % 2 == 1) categories.add("财经");
                    else categories.remove("财经");
                    break;
                case R.id.sports:
                    sportsBtnCount++;
                    if (sportsBtnCount % 2 == 1) categories.add("体育");
                    else categories.remove("体育");
                    break;
                case R.id.car:
                    carBtnCount++;
                    if (carBtnCount % 2 == 1) categories.add("汽车");
                    else categories.remove("汽车");
                    break;
                default:
                    break;
            }
        }
    };
}