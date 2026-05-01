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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * [수정 포인트]
 * 1. 예외 처리 강화: 데이터 파싱 실패 시 정확한 에러 메시지를 화면에 띄웁니다.
 * 2. optString 사용: 서버 응답에 특정 키값이 없어도 앱이 멈추지 않고 빈 문자열로 처리합니다.
 * 3. 디버그 로그 추가: 서버에서 온 실제 JSON 응답을 Logcat에서 확인할 수 있게 했습니다.
 */
public class MainActivity extends AppCompatActivity {

    private EditText etId, etPw;
    private Button btnLogin, btnGoRegister;

    private final String serverUrl = AppConfig.API_LOGIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etId = findViewById(R.id.etId);
        etPw = findViewById(R.id.etPw);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        btnLogin.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            String pw = etPw.getText().toString().trim();

            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(MainActivity.this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> requestLogin(id, pw)).start();
        });

        btnGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
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

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                String resultJson = sb.toString();
                Log.d("LOGIN_DEBUG", "Server Response: " + resultJson); // 서버 응답 로그 확인 필수

                JSONObject json = new JSONObject(resultJson);
                String status = json.optString("status", "fail");
                String message = json.optString("message", "로그인 처리에 실패했습니다.");

                runOnUiThread(() -> {
                    if ("success".equals(status)) {
                        try {
                            // 🚨 팩트체크: 서버의 응답 키값과 대조 필수
                            // 서버에서 mem_role, mem_name 등으로 주는지 확인하세요.
                            String role = json.optString("role", "");
                            String name = json.optString("name", "");
                            String realId = json.optString("id", inputId); // id가 없으면 입력한 id 사용
                            String storeId = json.optString("storeId", "");

                            if(role.isEmpty()) {
                                Toast.makeText(this, "경고: 권한(role) 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                            }

                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(MainActivity.this, StoreSelectActivity.class);
                            intent.putExtra("userId", realId);
                            intent.putExtra("userName", name);
                            intent.putExtra("role", role);
                            intent.putExtra("storeId", storeId);

                            startActivity(intent);
                            finish();

                        } catch (Exception e) {
                            Log.e("LOGIN_ERROR", "Data Parsing Error", e);
                            Toast.makeText(MainActivity.this, "화면 전환 오류: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "서버 연결 실패: " + responseCode, Toast.LENGTH_SHORT).show());
            }

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "통신 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}