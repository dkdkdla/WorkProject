package com.example.work;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private EditText etId, etPw;
    private Button btnLogin, btnGoRegister;

    private final String serverUrl = AppConfig.API_LOGIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 세션 쿠키 자동 유지 (로그인 후 JSESSIONID 저장)
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        etId          = findViewById(R.id.etId);
        etPw          = findViewById(R.id.etPw);
        btnLogin      = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        btnLogin.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            String pw = etPw.getText().toString().trim();

            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> requestLogin(id, pw)).start();
        });

        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void requestLogin(String inputId, String inputPw) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(serverUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);

            String postData = "id=" + inputId + "&pw=" + inputPw;
            try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                String resultJson = sb.toString();
                Log.d("LOGIN_DEBUG", "Response: " + resultJson);

                JSONObject json    = new JSONObject(resultJson);
                String status  = json.optString("status", "fail");
                String message = json.optString("message", "로그인 처리에 실패했습니다.");

                runOnUiThread(() -> {
                    if ("success".equals(status)) {
                        try {
                            String role    = json.optString("role", "").trim();
                            String name    = json.optString("name", "");
                            String realId  = json.optString("id", inputId);
                            String storeId = json.optString("storeId", "");

                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                            // 🚨 SA(전체관리자)는 앱 지원 없음 안내
                            if ("SA".equals(role)) {
                                Toast.makeText(this,
                                        "전체관리자 계정은 웹에서만 이용 가능합니다.",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            // 매장 선택 화면으로 이동
                            Intent intent = new Intent(this, StoreSelectActivity.class);
                            intent.putExtra("userId",   realId);
                            intent.putExtra("userName", name);
                            intent.putExtra("role",     role);
                            intent.putExtra("storeId",  storeId);
                            startActivity(intent);
                            finish();

                        } catch (Exception e) {
                            Log.e("LOGIN_ERROR", "Parsing Error", e);
                            Toast.makeText(this, "화면 전환 오류: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // PENDING, REJECTED 등 서버 메시지 그대로 표시
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "통신 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}