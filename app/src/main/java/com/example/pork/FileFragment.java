package com.example.pork;

import android.app.Activity;
import android.database.Cursor;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

public class FileFragment extends Fragment {
    private static final int REQUEST_IMAGE_PICKER = 1;
    private Dialog loadingDialog;

    private ContentResolver resolver;
    private ImageView imageView2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file, container, false);

        // 初始化 UI 元素
        imageView2 = view.findViewById(R.id.imageView2);
        resolver = requireActivity().getContentResolver();
        view.findViewById(R.id.btn_picker).setOnClickListener(v -> {
            // 打开相册
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_PICKER);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    // 从 URI 中获取输入流，并解码为 Bitmap
                    InputStream inputStream = resolver.openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    // 设置 imageView2 的图片
                    imageView2.setImageBitmap(bitmap);

                    // 将 URI 转换为 File
                    File imageFile = new File(getRealPathFromURI(imageUri));

                    // 立即上传图片
                    uploadImage(imageFile);
                    showLoadingDialog();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String path = null;
        if (uri != null) {
            String[] proj = { android.provider.MediaStore.Images.Media.DATA };
            Cursor cursor = getActivity().getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                path = cursor.getString(column_index);
                cursor.close();
            }
        }
        return path;
    }

    private void uploadImage(File imageFile) {
        new Thread(() -> {
            try {
                String boundary = UUID.randomUUID().toString();
                String twoHyphens = "--";
                String lineEnd = "\r\n";

                URL url = new URL("http://140.127.32.30:5000/api");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + imageFile.getName() + "\"" + lineEnd);
                    dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
                    dos.writeBytes(lineEnd);

                    FileInputStream fis = new FileInputStream(imageFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                    fis.close();

                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    dos.flush();
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    final String response;
                    try (Scanner scanner = new Scanner(conn.getInputStream())) {
                        StringBuilder responseBuilder = new StringBuilder();
                        while (scanner.hasNextLine()) {
                            responseBuilder.append(scanner.nextLine());
                        }
                        response = responseBuilder.toString();
                    }

                    JSONObject jsonResponse = new JSONObject(response);
                    String data = jsonResponse.optString("data");
                    String status = jsonResponse.optString("status");

                    getActivity().runOnUiThread(() -> {
                        hideLoadingDialog();
                        if ("None".equals(data)) {
                            showAlertDialog("錯誤", "該照片沒有偵測到跟豬肉相關的物件");
                        } else if ("400".equals(status)) {
                            showAlertDialog("錯誤", "伺服器未收到檔案，或是檔案不符規定\n請更新或重新上傳");
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString("result", response);
                            NavHostFragment.findNavController(FileFragment.this)
                                    .navigate(R.id.action_FileFragment_to_resultFragment, bundle);
                        }
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        hideLoadingDialog();
                        Toast.makeText(requireContext(), "Failed to Upload Image: " + responseCode, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    hideLoadingDialog();
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showLoadingDialog() {
        loadingDialog = new Dialog(getActivity());
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false); // 禁用返回键
        loadingDialog.setContentView(new ProgressBar(getActivity()));
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
