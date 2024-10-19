package com.example.pork;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pork.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class LlmFragment extends Fragment {

    private EditText editTextInput;
    private Button buttonSend;
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();

    private String apiToken = null;  // save api token

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_llm, container, false);

        editTextInput = view.findViewById(R.id.editTextInput);
        buttonSend = view.findViewById(R.id.buttonSend);
        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);

        messageAdapter = new MessageAdapter(messages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMessages.setAdapter(messageAdapter);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return view;
    }

    private void sendMessage() {
        String text = editTextInput.getText().toString();
        if (!text.isEmpty()) {
            // 添加用户消息到列表
            messages.add(new Message(text, true));
            messageAdapter.notifyItemInserted(messages.size() - 1);
            recyclerViewMessages.scrollToPosition(messages.size() - 1);
            editTextInput.getText().clear();

            // 發送到 LLM API
            if (apiToken == null) {
                sendFirstRequest(text);  // First request, send text only
            } else {
                sendSubsequentRequest(text);  // other request, send text and api_token
            }
        }
    }

    private void sendFirstRequest(String userText) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String requestUrl = "http://140.127.32.30:5000/llm?text=" + userText;
//                String postData = "text=" + userText;

                String response = sendGetRequest(requestUrl);
                // if not send successfully, retry
                while (response == "0"){
                    response = sendGetRequest(requestUrl);
                }

                String finalResponse = response;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalResponse.contains("Error")) {
                            messages.add(new Message(finalResponse, false));
                        } else {
                            // 解析响应获取 output 和 api_token
                            String output = parseOutput(finalResponse);
                            apiToken = parseApiToken(finalResponse);
                            messages.add(new Message(output, false));
                        }
                        messageAdapter.notifyItemInserted(messages.size() - 1);
                        recyclerViewMessages.scrollToPosition(messages.size() - 1);
                    }
                });
            }
        }).start();
    }

    private void sendSubsequentRequest(String userText) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String requestUrl = "http://140.127.32.30:5000/llm?text=" + userText + "&api_token=" + apiToken;
//                String postData = "text=" + userText + "&api_token=" + apiToken;
                Boolean resend = false;
                String response = sendGetRequest(requestUrl);
                if(response == "0"){
                    // except for unexpected end of stream
                    while (response == "0"){
                        requestUrl = "http://140.127.32.30:5000/chat_last_history?api_token=" + apiToken;
                        response = sendGetRequest(requestUrl);
                    }
                    resend = true;
                }
                String finalResponse = response;
                Boolean finalResend = resend;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalResponse.contains("Error")) {
                            messages.add(new Message(finalResponse, false));
                        } else if (finalResend == true) {
                            String output = resendOutput(finalResponse);
                            messages.add(new Message(output, false));
                        } else {
                            String output = parseOutput(finalResponse);
                            messages.add(new Message(output, false));
                        }
                        messageAdapter.notifyItemInserted(messages.size() - 1);
                        recyclerViewMessages.scrollToPosition(messages.size() - 1);
                    }
                });
            }
        }).start();
    }

    public String sendGetRequest(String requestUrl) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(requestUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setConnectTimeout(30000);  // 设置连接超时
            urlConnection.setReadTimeout(30000);     // 设置读取超时

            int responseCode = urlConnection.getResponseCode();
            InputStream inputStream;

            // 检查响应码是否为成功
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
            } else {
                inputStream = urlConnection.getErrorStream();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            }
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return response.toString();
    }

    // 解析 responseData 中的 output
    private String parseOutput(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            JSONObject dataObject = dataArray.getJSONObject(0);
            return dataObject.getString("output");  // 获取 "output"
        } catch (JSONException e) {
            e.printStackTrace();
            return "程式錯誤，請稍後重試，output：" + e.getMessage() + "\n\rInput：" + responseData;
        }
    }

    // 解析 responseData 中的 api_token
    private String parseApiToken(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            JSONObject dataObject = dataArray.getJSONObject(0);
            return dataObject.getString("api_token");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 重新請求 responseData 的解析
    private String resendOutput(String responseData){
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            return jsonObject.getString("data");
        } catch (JSONException e){
            e.printStackTrace();
            return "程式錯誤，請稍後重試，output：" + e.getMessage() + "\n\rInput：" + responseData;
        }
    }
}
