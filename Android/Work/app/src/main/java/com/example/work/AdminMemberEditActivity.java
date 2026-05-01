package com.example.work;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * [코드 설계 설명]
 * 1. 중앙 집중식 주소 관리: AppConfig.API_ADMIN_MEMBER_UPDATE를 사용하여 서버 경로를 통제합니다.
 * 2. 표준 통신 규격(POST): 서버 서블릿의 doPost 메서드와 통신하도록 설계하여 데이터 전송의 안정성을 높였습니다.
 * 3. 사용자 피드백: 서버 응답 메시지를 Toast로 출력하고, 성공 시에만 화면을 종료하도록 제어합니다.
 */
public class AdminMemberEditActivity extends AppCompatActivity {

    private EditText etName, etPhone, etWage;
    private Button btnSave, btnCancel;
    private String memId;

    // 🚨 수정 포인트: AppConfig 기반 서블릿 경로 매핑
    private final String updateUrl = AppConfig.API_ADMIN_MEMBER_UPDATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_member_edit);

        // 1. 뷰 초기화
        etName = findViewById(R.id.etEditMemName);
        etPhone = findViewById(R.id.etEditMemPhone);
        etWage = findViewById(R.id.etEditMemWage);
        btnSave = findViewById(R.id.btnMemberSave);
        btnCancel = findViewById(R.id.btnMemberCancel);

        // 2. 전달받은 인텐트 데이터 처리
        Intent intent = getIntent();
        memId = intent.getStringExtra("memId");
        String name = intent.getStringExtra("name");
        String phone = intent.getStringExtra("phone");
        String wage = intent.getStringExtra("wage");

        // 3. 초기 텍스트 설정 (이름과 전화번호는 가독성을 위해 표시)
        if (memId != null) {
            etName.setText(String.format("%s (%s)", (name != null ? name : ""), memId));
            etName.setFocusable(false); // 이름은 수정 불가 처리 (권장)
        }
        if (phone != null) etPhone.setText(phone);
        if (wage != null) etWage.setText(wage);

        // 4. 저장 버튼 이벤트
        btnSave.setOnClickListener(v -> {
            String newWage = etWage.getText().toString().trim();

            if (newWage.isEmpty()) {
                Toast.makeText(this, "시급을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 시급 업데이트 요청 (비동기 처리)
            new Thread(() -> requestUpdateWage(newWage)).start();
        });

        // 5. 취소 버튼
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * 서버의 AdminMemberUpdate 서블릿으로 시급 수정을 요청합니다. (POST 방식)
     */
    private void requestUpdateWage(String wage) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(updateUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);

            // 🚨 팩트체크: AdminMemberUpdate.java 서블릿은 'id'와 'wage' 파라미터를 기대함
            String postData = "id=" + memId + "&wage=" + wage;

            try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                // 서버 결과 분석
                JSONObject json = new JSONObject(sb.toString());
                String status = json.optString("status", "fail");
                String message = json.optString("message", "수정 요청에 실패했습니다.");

                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    // 🚨 성공 시에만 이전 화면(목록)으로 복귀
                    if ("success".equals(status) || message.contains("성공")) {
                        finish();
                    }
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "서버 연결 오류: " + responseCode, Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "통신 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}