package com.example.pork;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.ProgressDialog;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class ReportActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_report); // 使用之前的 fragment_report.xml 佈局

        // 初始化 UI 元素
        EditText editTextFeedback = findViewById(R.id.edit_feedback);
        Button buttonSubmit = findViewById(R.id.button_submit);

        // 設置送出按鈕的點擊事件
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feedback = editTextFeedback.getText().toString().trim();
                if (TextUtils.isEmpty(feedback)) {
                    Toast.makeText(ReportActivity.this, "請輸入您的意見回饋", Toast.LENGTH_SHORT).show();
                } else {
                    // 顯示載入對話框
                    showLoadingDialog();

                    // 模擬隨機的載入時間 (1 到 5 秒)
                    int randomDelay = 500 + random.nextInt(2000);

                    // 使用 Handler 延遲一段時間來模擬傳送過程
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 隱藏載入對話框
                            hideLoadingDialog();

                            // 顯示傳送成功的訊息
                            Toast.makeText(ReportActivity.this, "感謝您的意見回饋！", Toast.LENGTH_SHORT).show();

                            // 清空文字框
                            editTextFeedback.setText("");

                            // 返回到上一個頁面
                            finish();
                        }
                    }, randomDelay); // 延遲的時間
                }
            }
        });
    }
    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(ReportActivity.this);
        progressDialog.setMessage("傳送中...");
        progressDialog.setCancelable(false); // 不允許取消
        progressDialog.show();
    }

    // 隱藏載入對話框
    private void hideLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
