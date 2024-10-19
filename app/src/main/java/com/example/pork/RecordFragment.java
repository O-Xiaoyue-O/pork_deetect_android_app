package com.example.pork;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import com.example.pork.databinding.FragmentRecordBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import androidx.core.content.res.ResourcesCompat;
import android.graphics.Typeface;
import android.graphics.Color;
import android.widget.Button;
import android.widget.TextView;

public class RecordFragment extends Fragment {
    private FragmentRecordBinding binding;
    private HistoryDatabaseHelper dbHelper;
    private ExpandableAdapter adapter;
    private List<ExpandableItem> itemList;
    private static final String BASE_URL = "http://140.127.32.30/results/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecordBinding.inflate(inflater, container, false);
        dbHelper = new HistoryDatabaseHelper(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemList = new ArrayList<>();
        adapter = new ExpandableAdapter(itemList);

        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 将 RecyclerView 添加到 ScrollView 中的 LinearLayout
        binding.historyContainer.addView(recyclerView);

        loadHistory();

        binding.infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog();
            }
        });
    }

    private void loadHistory() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                HistoryDatabaseHelper.TABLE_NAME,
                null, // 返回所有列
                null,
                null,
                null,
                null,
                HistoryDatabaseHelper.COLUMN_DATE + " DESC" // 按日期排序
        );

        while (cursor.moveToNext()) {
            String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_IMAGE_URL));
            String value = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_VALUE));
            String val1 = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_VAL1));
            int val2 = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_VAL2));
            String average = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_AVERAGE));
            String recipeTitle = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_RECIPE_TITLE));
            String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_INGREDIENTS));
            String recipeContent = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_RECIPE_CONTENT));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(HistoryDatabaseHelper.COLUMN_DATE));

            String title = "";
            String content = "油花數值: " + value + "\n肉色數值: " + val1 + "\n等級: " + val2 + "\n油花" + average;
            itemList.add(new ExpandableItem(title, content, imageUrl, recipeTitle, ingredients, recipeContent, date));
        }

        cursor.close();
        adapter.notifyDataSetChanged();
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
