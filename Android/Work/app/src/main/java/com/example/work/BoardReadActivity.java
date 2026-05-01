package com.example.work;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BoardReadActivity extends AppCompatActivity {

    TextView tvTitle, tvWriter, tvDate, tvContent;
    TextView tvFileName;
    LinearLayout layoutFile;
    ImageView ivImage;
    Button btnBack;

    String boardSeq;

    String viewUrl = "http://10.0.2.2:8080/Work/api_board_view.jsp";
    String fileBaseUrl = "http://10.0.2.2:8080/Work/upload/";

    final int REQUEST_CODE_STORAGE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_read);

        tvTitle = findViewById(R.id.tvTitle);
        tvWriter = findViewById(R.id.tvWriter);
        tvDate = findViewById(R.id.tvDate);
        tvContent = findViewById(R.id.tvContent);
        tvFileName = findViewById(R.id.tvFileName);
        layoutFile = findViewById(R.id.layoutFile);
        ivImage = findViewById(R.id.ivImage);
        btnBack = findViewById(R.id.btnBack);

        Intent intent = getIntent();
        boardSeq = intent.getStringExtra("board_seq");

        if (boardSeq == null || boardSeq.isEmpty()) {
            Toast.makeText(this, "오류: 글 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> finish());

        new Thread(this::loadBoardDetail).start();
    }

    void loadBoardDetail() {
        try {
            String page = viewUrl + "?id=" + boardSeq;
            URL url = new URL(page);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                String status = json.optString("status");

                if ("success".equals(status)) {
                    String title = json.optString("title", "");
                    String writer = json.optString("writer", "");
                    String date = json.optString("date", "");
                    String content = json.optString("content", "");
                    String imgData = json.optString("imgData", "");
                    String filename = json.optString("filename", "");

                    runOnUiThread(() -> {
                        tvTitle.setText(title);
                        tvWriter.setText("작성자: " + writer);
                        tvDate.setText(date);
                        tvContent.setText(content);

                        if (!imgData.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(imgData, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ivImage.setImageBitmap(decodedByte);
                                ivImage.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ivImage.setVisibility(View.GONE);
                            }
                        } else {
                            ivImage.setVisibility(View.GONE);
                        }

                        if (!filename.isEmpty() && !filename.equals("null")) {
                            tvFileName.setText(filename);
                            layoutFile.setVisibility(View.VISIBLE);

                            layoutFile.setOnClickListener(v -> checkPermissionAndDownload(filename));
                        } else {
                            layoutFile.setVisibility(View.GONE);
                        }
                    });
                } else {
                    String msg = json.optString("message", "데이터 로드 실패");
                    runOnUiThread(() -> Toast.makeText(BoardReadActivity.this, msg, Toast.LENGTH_SHORT).show());
                }
            } else {
                runOnUiThread(() -> Toast.makeText(BoardReadActivity.this, "연결 실패: " + responseCode, Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(BoardReadActivity.this, "오류: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    void checkPermissionAndDownload(String filename) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
                return;
            }
        }
        downloadFile(filename);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "권한이 허용되었습니다. 파일을 다시 눌러주세요.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "권한이 거부되어 다운로드할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void downloadFile(String fileName) {
        try {
            String downloadUrl = fileBaseUrl + fileName;

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setTitle(fileName);
            request.setDescription("파일 다운로드 중...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(this, "다운로드를 시작합니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "다운로드 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}