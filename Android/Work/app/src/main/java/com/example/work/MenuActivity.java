package com.example.work;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * [코드 설계 설명]
 * 1. 허브 역할: 직원용 기능을 한데 모은 메인 화면입니다.
 * 2. QR 통합: ZXing 라이브러리를 통해 스캔한 매장 코드를 서버의 /QrCheck 서블릿으로 전송합니다.
 * 3. 중앙 집중형 URL: AppConfig.API_QR_CHECK를 사용하여 서버 주소를 관리합니다.
 */
public class MenuActivity extends AppCompatActivity {

    private TextView tvWelcome, tvStoreInfo;
    private Button btnChangeStore, btnGoQR, btnGoList, btnGoProfile, btnLogout, btnBoard;

    private String currentUserId, userName, role;
    private String currentStoreId, currentStoreName;

    // 🚨 수정 포인트: AppConfig 기반의 서블릿 경로 설정
    private final String qrCheckUrl = AppConfig.API_QR_CHECK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // 1. 뷰 초기화
        initView();

        // 2. 인텐트 데이터 수신
        Intent intent = getIntent();
        currentUserId = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");
        role = intent.getStringExtra("role");
        currentStoreId = intent.getStringExtra("storeId");
        currentStoreName = intent.getStringExtra("storeName");

        if (currentUserId == null) currentUserId = "unknown";

        // 3. 상단 정보 표시
        updateUI();

        // 4. 클릭 리스너 설정
        setupListeners();
    }

    private void initView() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStoreInfo = findViewById(R.id.tvStoreInfo);
        btnChangeStore = findViewById(R.id.btnChangeStore);
        btnGoQR = findViewById(R.id.btnGoQR);
        btnGoList = findViewById(R.id.btnGoList);
        btnGoProfile = findViewById(R.id.btnGoProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnBoard = findViewById(R.id.btnBoard);
    }

    private void updateUI() {
        if (userName != null) {
            tvWelcome.setText(String.format("%s 님", userName));
        }
        if (currentStoreName != null) {
            tvStoreInfo.setText(String.format("현재 매장: %s", currentStoreName));
        } else {
            tvStoreInfo.setText("선택된 매장이 없습니다.");
        }
        // 게시판은 항상 활성화 (필요 시 권한에 따라 제어 가능)
        btnBoard.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        // 매장 변경 화면 이동
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
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setCaptureActivity(CustomScannerActivity.class);
            integrator.setOrientationLocked(false);
            integrator.setPrompt("매장에 비치된 QR 코드를 스캔하세요.");
            integrator.initiateScan();
        });

        // 근무 기록 조회 이동
        btnGoList.setOnClickListener(v -> {
            Intent listIntent = new Intent(MenuActivity.this, ListActivity.class);
            listIntent.putExtra("userId", currentUserId);
            listIntent.putExtra("storeName", currentStoreName);
            listIntent.putExtra("storeId", currentStoreId);
            startActivity(listIntent);
        });

        // 게시판 이동
        btnBoard.setOnClickListener(v -> {
            Intent boardIntent = new Intent(MenuActivity.this, BoardListActivity.class);
            boardIntent.putExtra("userId", currentUserId);
            boardIntent.putExtra("storeId", currentStoreId);
            boardIntent.putExtra("role", role);
            startActivity(boardIntent);
        });

        // 프로필 수정 이동
        btnGoProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(MenuActivity.this, ProfileActivity.class);
            profileIntent.putExtra("userId", currentUserId);
            profileIntent.putExtra("role", role);
            startActivity(profileIntent);
        });

        // 로그아웃
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
                String scannedStoreId = result.getContents();
                // 스캔 결과 서버 전송 (출근/퇴근 선택 팝업 등을 생략하고 즉시 처리하는 경우)
                new Thread(() -> requestQrCheck(currentUserId, scannedStoreId)).start();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 서버의 QrCheck 서블릿으로 스캔 결과를 전송합니다.
     */
    private void requestQrCheck(String userId, String storeId) {
        try {
            // 🚨 팩트체크: QrCheck.java 서블릿 파라미터(id, storeId)와 일치시킴
            // 서버 로직에 따라 type(IN/OUT) 파라미터가 추가로 필요할 수 있음
            String page = qrCheckUrl + "?id=" + userId + "&storeId=" + storeId;
            URL url = new URL(page);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                String status = json.optString("status", "fail");
                String message = json.optString("message", "인증 실패");

                runOnUiThread(() -> {
                    Toast.makeText(MenuActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
        }
    }
}