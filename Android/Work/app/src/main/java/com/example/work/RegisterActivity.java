package com.example.work;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * [코드 설계 설명]
 * 1. 동적 UI 바인딩: CheckBox 선택 상태에 따라 관리자 전용 필드(매장명, 인증코드)와 직원용 필드(시급)의 가시성을 제어한다.
 * 2. 중앙 집중식 설정: AppConfig.API_REGISTER 상수를 사용하여 서버의 /Register 서블릿과 통신한다.
 * 3. Volley 기반 통신: POST 방식을 사용하여 가입 정보를 본문(Body)에 담아 안전하게 전송한다.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etId, etPw, etPwConfirm, etName, etPhone, etWage, etStoreId, etStoreName, etAdminCode;
    private CheckBox chkIsAdmin;
    private Button btnRegister;

    // 🚨 수정 포인트: AppConfig 기반의 서블릿 경로 설정
    private final String url = AppConfig.API_REGISTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. 뷰 초기화
        initView();

        // 2. 관리자 체크박스 이벤트 (역할에 따른 입력 필드 제어)
        chkIsAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 점주 모드: 매장 이름과 관리자 코드 입력창 활성화, 시급 입력창 비활성화
                etStoreName.setVisibility(View.VISIBLE);
                etAdminCode.setVisibility(View.VISIBLE);
                etStoreId.setHint("생성할 매장 ID *");
                etWage.setVisibility(View.GONE);
            } else {
                // 직원 모드: 매장 이름과 관리자 코드 입력창 비활성화, 시급 입력창 활성화
                etStoreName.setVisibility(View.GONE);
                etAdminCode.setVisibility(View.GONE);
                etStoreId.setHint("매장 ID (선택)");
                etWage.setVisibility(View.VISIBLE);
            }
        });

        // 3. 회원가입 버튼 이벤트
        btnRegister.setOnClickListener(v -> {
            validateAndRegister();
        });
    }

    private void initView() {
        etId = findViewById(R.id.etRegId);
        etPw = findViewById(R.id.etRegPw);
        etPwConfirm = findViewById(R.id.etRegPwConfirm);
        etName = findViewById(R.id.etRegName);
        etPhone = findViewById(R.id.etRegPhone);
        etWage = findViewById(R.id.etWage);
        etStoreId = findViewById(R.id.etStoreId);
        etStoreName = findViewById(R.id.etStoreName);
        etAdminCode = findViewById(R.id.etAdminCode);
        chkIsAdmin = findViewById(R.id.chkIsAdmin);
        btnRegister = findViewById(R.id.btnRegisterAction);
    }

    /**
     * 입력값의 유효성을 검사하고 서버에 가입을 요청한다.
     */
    private void validateAndRegister() {
        String id = etId.getText().toString().trim();
        String pw = etPw.getText().toString().trim();
        String pwConfirm = etPwConfirm.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String wage = etWage.getText().toString().trim();
        String storeId = etStoreId.getText().toString().trim();
        String storeName = etStoreName.getText().toString().trim();
        String adminCode = etAdminCode.getText().toString().trim();
        boolean isAdmin = chkIsAdmin.isChecked();

        // 필수 항목 검사
        if (id.isEmpty() || pw.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "필수 정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 비밀번호 일치 검사
        if (!pw.equals(pwConfirm)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 관리자용 추가 항목 검사
        if (isAdmin && (storeId.isEmpty() || storeName.isEmpty() || adminCode.isEmpty())) {
            Toast.makeText(this, "관리자는 매장 정보와 인증 코드가 필수입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 서버 가입 요청 실행
        registerRequest(id, pw, name, phone, wage, storeId, storeName, adminCode, isAdmin ? "A" : "S");
    }

    /**
     * Volley를 사용하여 서버의 Register 서블릿으로 가입 데이터를 전송한다.
     */
    private void registerRequest(String id, String pw, String name, String phone, String wage,
                                 String storeId, String storeName, String adminCode, String role) {

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        String status = json.optString("status", "fail");
                        String message = json.optString("message", "가입 처리에 실패했습니다.");

                        if ("success".equals(status)) {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            finish(); // 가입 성공 시 화면 종료
                        } else {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "데이터 분석 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(RegisterActivity.this, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                // 🚨 팩트체크: Register.java 서블릿에서 받는 파라미터명과 100% 일치시킨다.
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("pw", pw);
                params.put("name", name);
                params.put("phone", phone);
                params.put("role", role);
                params.put("storeId", storeId);
                params.put("wage", wage);
                params.put("adminCode", adminCode);
                params.put("storeName", storeName);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}