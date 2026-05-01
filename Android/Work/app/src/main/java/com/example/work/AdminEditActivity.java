package com.example.work;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * [코드 설계 설명]
 * 1. 서블릿 연동: AdminAttendanceAction 서블릿의 /AdminAttendanceAction 경로를 사용합니다.
 * 2. 식별자 최적화: 기존의 복잡한 조건 대신 DB의 PK인 'idx'를 사용하여 기록을 정확히 식별합니다.
 * 3. POST 통신: 보안 및 데이터 규격에 맞춰 서버 서블릿의 doPost 메서드와 통신하도록 설계했습니다.
 */
public class AdminEditActivity extends AppCompatActivity {

    private EditText etType, etTime;
    private TextView tvTitle;
    private Button btnSave, btnDelete;

    private String memId, date, idx;

    // 🚨 수정 포인트: AppConfig와 서블릿 경로 매핑
    private final String updateUrl = AppConfig.BASE_URL + "AdminAttendanceAction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit);

        tvTitle = findViewById(R.id.tvEditTitle);
        etType = findViewById(R.id.etEditType);
        etTime = findViewById(R.id.etEditTime);
        btnSave = findViewById(R.id.btnEditSave);
        btnDelete = findViewById(R.id.btnEditDelete);

        // 인텐트 데이터 수신
        Intent intent = getIntent();
        idx = intent.getStringExtra("idx"); // 🚨 서버 처리를 위해 반드시 필요함
        memId = intent.getStringExtra("memId");
        String name = intent.getStringExtra("memName");
        String type = intent.getStringExtra("type");
        String time = intent.getStringExtra("time");
        date = intent.getStringExtra("date");

        if (name != null) {
            tvTitle.setText(String.format("%s님의 기록 수정", name));
        }
        etType.setText(type);
        etTime.setText(time);

        // 수정 저장 버튼
        btnSave.setOnClickListener(v -> {
            String newType = etType.getText().toString().trim();
            String newTime = etTime.getText().toString().trim();

            if(newType.isEmpty() || newTime.isEmpty()) {
                Toast.makeText(this, "수정할 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // "update" 모드로 요청
            new Thread(() -> requestAction("update", newType, newTime)).start();
        });

        // 삭제 버튼
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("기록 삭제")
                    .setMessage("정말 이 근태 기록을 삭제하시겠습니까?\n(삭제 후에는 복구가 불가능합니다.)")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        // "delete" 모드로 요청
                        new Thread(() -> requestAction("delete", "", "")).start();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });
    }

    /**
     * 서버의 AdminAttendanceAction 서블릿으로 POST 요청을 보냅니다.
     * @param mode "update" 또는 "delete"
     */
    private void requestAction(String mode, String newType, String newTime) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(updateUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);

            // POST 파라미터 구성 (서버의 AdminAttendanceAction.java 파라미터와 일치)
            // 🚨 팩트체크: 서버는 idx, mode, type, date, time 파라미터를 기대함
            String postData = "mode=" + URLEncoder.encode(mode, "UTF-8")
                    + "&idx=" + URLEncoder.encode(idx, "UTF-8")
                    + "&type=" + URLEncoder.encode(newType, "UTF-8")
                    + "&date=" + URLEncoder.encode(date, "UTF-8")
                    + "&time=" + URLEncoder.encode(newTime, "UTF-8");

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

                // 서버 응답 분석
                JSONObject json = new JSONObject(sb.toString());
                String status = json.optString("status", "fail");
                String message = json.optString("message", "처리에 실패했습니다.");

                runOnUiThread(() -> {
                    Toast.makeText(AdminEditActivity.this, message, Toast.LENGTH_SHORT).show();
                    if ("success".equals(status)) finish(); // 성공 시 닫기
                });
            } else {
                runOnUiThread(() -> Toast.makeText(AdminEditActivity.this, "서버 연결 실패: " + responseCode, Toast.LENGTH_SHORT).show());
            }

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(AdminEditActivity.this, "통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}