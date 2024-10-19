package com.example.pork;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

public class RegisterFragment extends Fragment {
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextBirthday;
    private EditText editTextPhoneNumber;
    private EditText editTextEmail;
    private Button buttonRegister;
    // radio group
    private RadioGroup radioGroupGender;
    private RadioButton radioButtonMale, radioButtonFemale, radioButtonOther;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        editTextUsername = view.findViewById(R.id.accudent);
        editTextPassword = view.findViewById(R.id.password);
        editTextConfirmPassword = view.findViewById(R.id.comfirm_password);
        editTextBirthday = view.findViewById(R.id.birthdayDate);
        editTextPhoneNumber = view.findViewById(R.id.numberPhone);
        editTextEmail = view.findViewById(R.id.EmailAddress);
        buttonRegister = view.findViewById(R.id.registerbutton);
        radioGroupGender = view.findViewById(R.id.radioGroup);

        radioButtonMale = view.findViewById(R.id.maleButton);
        radioButtonFemale = view.findViewById(R.id.femaleButton);
        radioButtonOther = view.findViewById(R.id.otherButton);
        
        // set button click listener
        
        
        return view;
    }
    
    private void handleRegister(){
        // disable user input and button
        DisableInput();
        // get user input
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String comfirmpassword = editTextConfirmPassword.getText().toString().trim();
        String sex;
        if (radioButtonMale.isChecked()) {
            sex = "male";
        } else if (radioButtonFemale.isChecked()) {
            sex = "female";
        } else if (radioButtonOther.isChecked()) {
            sex = "other";
        }else {
            sex = "";
            Toast.makeText(getActivity(), "請選擇性別", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        }
        String birthday = editTextBirthday.getText().toString().trim();
        String phonenumber = editTextPhoneNumber.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        
        // check username is not empty
        if (username.isEmpty()) {
            Toast.makeText(getActivity(), "請輸入帳號", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        }
        // check password is legal
        if (password.isEmpty()) {
            Toast.makeText(getActivity(), "請輸入密碼", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        } else if (password.length() < 8) {
            Toast.makeText(getActivity(), "密碼長度至少為8", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        }else if (comfirmpassword.isEmpty()) {
            Toast.makeText(getActivity(), "請再次輸入密碼", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        } else if (password.equals(comfirmpassword)) {
            Toast.makeText(getActivity(), "密碼不一致", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        }

        // check birthday is legal
        if (birthday.isEmpty()) {
            Toast.makeText(getActivity(), "請輸入生日", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        }
        if (!birthday.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(getActivity(), "生日格式錯誤", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        }
        // check phonenumber is legal
        if (phonenumber.isEmpty()) {
            Toast.makeText(getActivity(), "請輸入電話", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        }
        if (!phonenumber.matches("\\d{10}")) {
            Toast.makeText(getActivity(), "電話格式錯誤", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        }
        // check email is legal
        if (email.isEmpty()) {
            Toast.makeText(getActivity(), "請輸入Email", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        }
        if (!email.contains("@")) {
            Toast.makeText(getActivity(), "Email格式錯誤", Toast.LENGTH_SHORT).show();
            EnableInput();
            return;
        }

        // if check all pass, send request to server
        // TODO: send request to server function
        new Thread(() -> sendRegisterRequest(username, password, sex, birthday, phonenumber, email)).start();

    }

    private void sendRegisterRequest(String username, String password,
                                     String sex, String birthday,
                                     String phonenumber, String email){
        HttpURLConnection urlConnection = null;

        try{
            // reading url from shared preferences, if not found, use default url
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String ip = sharedPreferences.getString("baseUrl", "140.127.32.30");
            String baseUrl = "http://" + ip + "/api/register";
            String urlString = baseUrl + "?username=" + URLEncoder.encode(username, "UTF-8") +
                    "&password=" + URLEncoder.encode(password, "UTF-8") +
                    "&sex" + URLEncoder.encode(sex, "UTF-8") +
                    "&birthday" + URLEncoder.encode(birthday, "UTF-8") +
                    "&phonenumber" + URLEncoder.encode(phonenumber, "UTF-8") +
                    "&email" + URLEncoder.encode(email, "UTF-8");
            URL url = new URL(urlString);

            // open link
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

            // check response code
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
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

                if(!"success".equals(status)){
                    ErrorCodeShower(status);
                    EnableInput();
                }else{
                    Toast.makeText(getActivity(), "註冊成功，請使用所設定的帳號密碼登入", Toast.LENGTH_LONG).show();
                    getActivity().runOnUiThread(this::navigateToFragmentFirst);
                }
            }

        }catch (IOException | JSONException e){
            e.printStackTrace();
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getActivity(), "註冊失敗，請稍後再試", Toast.LENGTH_SHORT).show()
            );
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void ErrorCodeShower(String code) {
        switch (code) {
            case "database_error":
                Toast.makeText(getActivity(), "伺服器錯誤，請稍後在試", Toast.LENGTH_SHORT).show();
                break;
            case "provide_error":
                Toast.makeText(getActivity(), "參數錯誤", Toast.LENGTH_SHORT).show();
                break;
            case "user_already_exist":
                Toast.makeText(getActivity(), "帳號已存在", Toast.LENGTH_SHORT).show();
                break;
            case "email_already_exist":
                Toast.makeText(getActivity(), "Email已存在", Toast.LENGTH_SHORT).show();
                break;
            case "phone_number_already_exist":
                Toast.makeText(getActivity(), "電話已存在", Toast.LENGTH_SHORT).show();
                break;
            case "phone_number_format_error":
                Toast.makeText(getActivity(), "電話格式錯誤", Toast.LENGTH_SHORT).show();
                break;
            case "email_format_error":
                Toast.makeText(getActivity(), "Email格式錯誤", Toast.LENGTH_SHORT).show();
                break;
            case "password_length_error":
                Toast.makeText(getActivity(), "密碼長度錯誤", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(getActivity(), "總之就是錯誤了", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void navigateToFragmentFirst() {
        // 導航到 FragmentFirst
        FirstFragment fragmentFirst = new FirstFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragmentFirst);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void DisableInput(){
        editTextUsername.setEnabled(false);
        editTextPassword.setEnabled(false);
        editTextConfirmPassword.setEnabled(false);
        editTextBirthday.setEnabled(false);
        editTextPhoneNumber.setEnabled(false);
        editTextEmail.setEnabled(false);
        buttonRegister.setEnabled(false);
        radioGroupGender.setEnabled(false);
        radioButtonMale.setEnabled(false);
        radioButtonFemale.setEnabled(false);
        radioButtonOther.setEnabled(false);
    }

    private void EnableInput(){
        editTextUsername.setEnabled(true);
        editTextPassword.setEnabled(true);
        editTextConfirmPassword.setEnabled(true);
        editTextBirthday.setEnabled(true);
        editTextPhoneNumber.setEnabled(true);
        editTextEmail.setEnabled(true);
        buttonRegister.setEnabled(true);
        radioGroupGender.setEnabled(true);
        radioButtonMale.setEnabled(true);
        radioButtonFemale.setEnabled(true);
        radioButtonOther.setEnabled(true);
    }
}
