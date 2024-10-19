package com.example.pork;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.example.pork.databinding.FragmentCameraBinding;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;
import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class CameraFragment extends Fragment {
    private FragmentCameraBinding binding;
    private Dialog loadingDialog;

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    private Uri photoURI;
    private String currentPhotoPath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cameraBtn.setOnClickListener(v -> askCameraPermissions());
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(requireContext(), "Error occurred while creating the file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(requireContext(), "com.example.pork.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            File file = new File(currentPhotoPath);
            if (file.exists()) {
                showLoadingDialog();
                uploadImage(file);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
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
                            NavHostFragment.findNavController(CameraFragment.this)
                                    .navigate(R.id.action_cameraFragment_to_resultFragment, bundle);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
