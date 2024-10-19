package com.example.pork;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.pork.databinding.FragmentResultBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultFragment extends Fragment {
    private FragmentResultBinding binding;
    private ExpandableAdapter adapter;
    private List<ExpandableItem> itemList;
    private static final String BASE_URL = "http://140.127.32.30/results/";
    private HistoryDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentResultBinding.inflate(inflater, container, false);
        dbHelper = new HistoryDatabaseHelper(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initData();

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpandableAdapter(itemList);
        recyclerView.setAdapter(adapter);

        // 設置按鈕的點擊事件
        binding.infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog();
            }
        });
    }

    private void initData() {
        itemList = new ArrayList<>();

        if (getArguments() != null) {
            String result = getArguments().getString("result");
            try {
                JSONObject json_response = new JSONObject(result);
                JSONArray dataArray = json_response.getJSONArray("data");
                String urlPath = json_response.getString("url");
                updateResultsCount(dataArray.length());
                parseDataAndSaveResults(dataArray, urlPath);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateResultsCount(int count) {
        TextView resultsCountTextView = binding.resultsCountTextView;
        resultsCountTextView.setText("共有 " + count + " 筆資料");
    }

    private void parseDataAndSaveResults(JSONArray dataArray, String urlPath) throws JSONException {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        for (int i = 0; i < dataArray.length(); i++) {
            JSONArray innerArray = dataArray.getJSONArray(i);

            int id = innerArray.getInt(0);
            String value1 = String.format("%.2f", innerArray.getDouble(1));
            String value2 = String.format("%.2f", innerArray.getDouble(2));
            int value3 = innerArray.getInt(3);
            String average = innerArray.getString(4);
            String recipeTitle = innerArray.getString(5);
            String ingredients = innerArray.getString(6);
            String recipeContent = innerArray.getString(7);

            // 拼接图片URL
            String imageUrl = BASE_URL + urlPath + "/_cutting" + (i + 1) + ".png";
//            value1 = "1.03"; // 臨時
//            value2 = "34.23"; // 臨時

            // 添加到显示列表
            String title = "第 " + (i + 1) + " 塊";
            String content = "油花數值： " + value1 + "\n肉色數值： " + value2 + "\n等級： " + value3 + "\n油花" + average;
            itemList.add(new ExpandableItem(title, content, imageUrl, recipeTitle, ingredients, recipeContent, currentDate));

            // 保存数据到 SQLite 数据库
            ContentValues values = new ContentValues();
            values.put(HistoryDatabaseHelper.COLUMN_IMAGE_URL, imageUrl);
            values.put(HistoryDatabaseHelper.COLUMN_VALUE, value1);
            values.put(HistoryDatabaseHelper.COLUMN_VAL1, value2);
            values.put(HistoryDatabaseHelper.COLUMN_VAL2, value3);
            values.put(HistoryDatabaseHelper.COLUMN_AVERAGE, average);
            values.put(HistoryDatabaseHelper.COLUMN_RECIPE_TITLE, recipeTitle);
            values.put(HistoryDatabaseHelper.COLUMN_INGREDIENTS, ingredients);
            values.put(HistoryDatabaseHelper.COLUMN_RECIPE_CONTENT, recipeContent);
            values.put(HistoryDatabaseHelper.COLUMN_DATE, currentDate);

            db.insert(HistoryDatabaseHelper.TABLE_NAME, null, values);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dbHelper.close();
        binding = null;
    }

    private void showInfoDialog() {
        // 創建一個對話框使用的自定義佈局
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_info, null);

        // 設置對話框中的圖片和文本
        ImageView infoImageView = dialogView.findViewById(R.id.infoImageView);
        // 你可以在這裡使用 Glide 或 Picasso 載入圖片，如果圖片不是資源
        // Glide.with(this).load(imageUrl).into(infoImageView);

        TextView infoTextView = dialogView.findViewById(R.id.infoTextView);
        String context = "油花分數：當油花分數越高時，代表油脂越多，建議依據油質分數去調整烹飪方式。\n\n" +
                "肉色分數：代表肉的色澤分數，通常豬肉在37~43為正常，數值越低時，吃起來會越水；數值越高時，吃起來會越乾硬。\n\n" +
                "等級：等級是由油花與肉色跟均勻與否，這三個指標下去判斷的方式。";
        infoTextView.setText(context);
        TextView titleView = new TextView(requireContext());
        titleView.setText("分數是什麼？");
        titleView.setTextSize(18); // 設定字體大小
        titleView.setTextColor(Color.BLACK); // 設定字體顏色
        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.df_liyuan_std_w8);
        titleView.setTypeface(typeface, Typeface.BOLD); // 設置為粗體
        titleView.setPadding(40, 40, 25, 10);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCustomTitle(titleView) // 使用自定義標題
                .setView(dialogView)
                .setPositiveButton("確定", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            // 設置確定按鈕顏色
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setBackgroundColor(Color.parseColor("#FFCC81")); // 按鈕顏色
                positiveButton.setTextColor(Color.WHITE);
                positiveButton.setTypeface(null, Typeface.BOLD); // 按鈕文字顏色
            }

        });
        dialog.show();
    }
}
