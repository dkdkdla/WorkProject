package com.example.work;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MenuActivity extends AppCompatActivity {

    private TextView tvWelcome, tvStoreInfo;
    private Button btnChangeStore, btnGoQR, btnGoList, btnGoProfile, btnLogout, btnBoard;

    private String currentUserId, userName, role;
    private String currentStoreId, currentStoreName;
    private String scannedStoreId; // QR 스캔 결과 임시 저장

    private final String qrCheckUrl = AppConfig.API_QR_CHECK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initView();

        Intent intent = getIntent();
        currentUserId   = intent.getStringExtra("userId");
        userName        = intent.getStringExtra("userName");
        role            = intent.getStringExtra("role");
        currentStoreId  = intent.getStringExtra("storeId");
        currentStoreName = intent.getStringExtra("storeName");

        if (currentUserId == null) currentUserId = "unknown";

        updateUI();
        setupListeners();
    }

    private void initView() {
        tvWelcome      = findViewById(R.id.tvWelcome);
        tvStoreInfo    = findViewById(R.id.tvStoreInfo);
        btnChangeStore = findViewById(R.id.btnChangeStore);
        btnGoQR        = findViewById(R.id.btnGoQR);
        btnGoList      = findViewById(R.id.btnGoList);
        btnGoProfile   = findViewById(R.id.btnGoProfile);
        btnLogout      = findViewById(R.id.btnLogout);
        btnBoard       = findViewById(R.id.btnBoard);
    }

    private void updateUI() {
        if (userName != null) tvWelcome.setText(userName + " 님");
        if (currentStoreName != null) {
            tvStoreInfo.setText("현재 매장: " + currentStoreName);
        } else {
            tvStoreInfo.setText("선택된 매장이 없습니다.");
        }
        btnBoard.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        btnChangeStore.setOnClickListener(v -> {
            Intent changeIntent = new Intent(MenuActivity.this, StoreSelectActivity.class);
            changeIntent.putExtra("userId", currentUserId);
            changeIntent.putExtra("userName", userName);
            changeIntent.putExtra("role", role);
            startActivity(changeIntent);
            finish();
        });

        // QR 스캐너 실행
        btnGoQR.setOnClickListener(v -> {
            if (currentStoreId == null || currentStoreId.isEmpty()) {
                Toast.makeText(this, "매장을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setCaptureActivity(CustomScannerActivity.class);
            integrator.setOrientationLocked(false);
            integrator.setPrompt("매장에 비치된 QR 코드를 스캔하세요.");
            integrator.initiateScan();
        });

        btnGoList.setOnClickListener(v -> {
            Intent listIntent = new Intent(MenuActivity.this, ListActivity.class);
            listIntent.putExtra("userId", currentUserId);
            listIntent.putExtra("storeName", currentStoreName);
            listIntent.putExtra("storeId", currentStoreId);
            startActivity(listIntent);
        });

        btnBoard.setOnClickListener(v -> {
            Intent boardIntent = new Intent(MenuActivity.this, BoardListActivity.class);
            boardIntent.putExtra("userId", currentUserId);
            boardIntent.putExtra("storeId", currentStoreId);
            boardIntent.putExtra("role", role);
            startActivity(boardIntent);
        });

        btnGoProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(MenuActivity.this, ProfileActivity.class);
            profileIntent.putExtra("userId", currentUserId);
            profileIntent.putExtra("role", role);
            startActivity(profileIntent);
        });

        btnLogout.setOnClickListener(v -> {
            Intent goMain = new Intent(MenuActivity.this, MainActivity.class);
            goMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goMain);
            Toast.makeText(MenuActivity.this, "정상적으로 로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "스캔이 취소되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                scannedStoreId = result.getContents();
                // 🚨 출근/퇴근 선택 다이얼로그 표시
                showAttendanceDialog(scannedStoreId);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // 출근/퇴근 선택 다이얼로그
    private void showAttendanceDialog(String storeId) {
        new AlertDialog.Builder(this)
                .setTitle("출퇴근 선택")
                .setMessage("스캔한 매장: " + storeId + "\n\n출근 또는 퇴근을 선택해주세요.")
                .setPositiveButton("🟢 출근", (dialog, which) -> {
                    new Thread(() -> requestQrCheck(currentUserId, storeId, "IN")).start();
                })
                .setNegativeButton("🔴 퇴근", (dialog, which) -> {
                    new Thread(() -> requestQrCheck(currentUserId, storeId, "OUT")).start();
                })
                .setNeutralButton("취소", null)
                .setCancelable(false)
                .show();
    }

    // QrCheck 서블릿 POST 요청 (type 파라미터 포함)
    private void requestQrCheck(String userId, String storeId, String type) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(qrCheckUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);

            String postData = "userId=" + userId + "&storeId=" + storeId + "&type=" + type;
            try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json    = new JSONObject(sb.toString());
                String status  = json.optString("status", "fail");
                String message = json.optString("message", "처리 실패");

                runOnUiThread(() -> {
                    // 출근/퇴근에 따라 다른 색상 Toast
                    Toast.makeText(MenuActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}