package com.example.pork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginFragment extends Fragment {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private TextView textViewRegister;
    private Button buttonLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // 取得輸入框和按鈕的引用
        editTextUsername = view.findViewById(R.id.accudent);
        editTextPassword = view.findViewById(R.id.password);
        buttonLogin = view.findViewById(R.id.loginbutton);
        textViewRegister = view.findViewById(R.id.registerText);

        // 設置登入按鈕的點擊事件
        buttonLogin.setOnClickListener(v -> handleLogin());

        // 設置註冊按鈕的點擊事件
        textViewRegister.setOnClickListener(v -> handleRegister());

        // 檢查是否已有有效的 Token
        checkIfAlreadyLoggedIn();

        return view;
    }

    private void checkIfAlreadyLoggedIn() {
        // 從 SharedPreferences 中取得已儲存的 token
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);

        if (token != null) {
            // 有 token，檢查是否有效
            new Thread(() -> validateToken(token)).start();
        }
    }

    private void toRegisterPage(){
        RegisterFragment fragmentRegister = new RegisterFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragmentRegister);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void validateToken(String token) {
        HttpURLConnection urlConnection = null;

        try {
            // reading url from shared preferences, if not found, use default url
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String ip = sharedPreferences.getString("baseUrl", "140.127.32.30");
            String baseUrl = "http://" + ip + "/api/validateToken";
            String urlString = baseUrl + "?token=" + URLEncoder.encode(token, "UTF-8");
            URL url = new URL(urlString);

            // 打開連接
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000); // 連接超時 5 秒
            urlConnection.setReadTimeout(5000);    // 讀取超時 5 秒

            // 檢查回應碼
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 讀取回應
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder responseBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                String responseBody = responseBuilder.toString();
                JSONObject jsonObject = new JSONObject(responseBody);
                String status = jsonObject.getString("status");

                // 判斷 token 是否有效
                if ("token_found".equals(status)) {
                    // Token 有效，跳轉到 fragment_first
                    getActivity().runOnUiThread(this::navigateToFragmentFirst);
                }

            } else {
                // 無法連接伺服器或回應代碼錯誤
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "伺服器錯誤，請稍後再試", Toast.LENGTH_SHORT).show()
                );
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getActivity(), "網絡錯誤或無效的回應", Toast.LENGTH_SHORT).show()
            );

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect(); // 斷開連接
            }
        }
    }

    private void handleRegister(){
        new Thread(() -> toRegisterPage()).start();
    }

    private void handleLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "請輸入帳號和密碼", Toast.LENGTH_SHORT).show();
            return;
        }

        // 發送 GET 請求進行登入
        new Thread(() -> sendLoginRequest(username, password)).start();
    }

    private void sendLoginRequest(String username, String password) {
        HttpURLConnection urlConnection = null;

        try {
            // 構建完整的 URL，並對參數進行編碼
            String baseUrl = "http://your-api-url.com/login";
            String urlString = baseUrl + "?username=" + URLEncoder.encode(username, "UTF-8") +
                    "&password=" + URLEncoder.encode(password, "UTF-8");
            URL url = new URL(urlString);

            // 打開連接
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000); // 連接超時 5 秒
            urlConnection.setReadTimeout(5000);    // 讀取超時 5 秒

            // 檢查回應碼
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 讀取回應
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder responseBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                String responseBody = responseBuilder.toString();
                parseAndHandleResponse(responseBody);

            } else {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "伺服器錯誤，請稍後再試", Toast.LENGTH_SHORT).show()
                );
            }

        } catch (IOException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getActivity(), "登入失敗，請檢查網絡", Toast.LENGTH_SHORT).show()
            );

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect(); // 斷開連接
            }
        }
    }

    private void parseAndHandleResponse(String responseBody) {
        try {
            // 解析回傳的 JSON 資料
            JSONObject jsonObject = new JSONObject(responseBody);
            String status = jsonObject.getString("status");

            if ("success".equals(status)) {
                String token = jsonObject.getString("data");

                // 儲存 token 至 SharedPreferences
                saveTokenToDevice(token);

                // 成功訊息
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "登入成功", Toast.LENGTH_SHORT).show()
                );

                // 跳轉至主頁面
                getActivity().runOnUiThread(this::navigateToFragmentFirst);

            } else {
                String message = jsonObject.getString("message");
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "登入失敗：" + message, Toast.LENGTH_SHORT).show()
                );
            }

        } catch (JSONException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getActivity(), "回應解析錯誤", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void saveTokenToDevice(String token) {
        // 使用 SharedPreferences 存儲 token
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("authToken", token);
        editor.apply();
    }

    private void navigateToFragmentFirst() {
        // 導航到 FragmentFirst
        FirstFragment fragmentFirst = new FirstFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragmentFirst);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
