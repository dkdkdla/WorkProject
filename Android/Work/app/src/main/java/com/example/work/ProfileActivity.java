package com.example.work;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * [코드 설계 설명]
 * 1. 중앙 집중식 URL 관리: AppConfig 상수를 사용하여 서버의 서블릿 엔드포인트와 통신한다.
 * 2. 권한별 UI 제어: 사용자의 Role(A: 점주, S: 직원)에 따라 매장 생성 또는 매장 등록 UI를 동적으로 전환한다.
 * 3. 데이터 보안 통신: 개인 정보 수정 시 POST 방식을 사용하여 파라미터 노출을 방지한다.
 */
public class ProfileActivity extends AppCompatActivity {

    private EditText etId, etName, etPhone, etWage;
    private EditText etPw, etPwConfirm;
    private EditText etNewStoreId, etNewStoreName;
    private TextView tvStoreHelp;
    private Button btnJoinStore, btnUpdate;

    private RecyclerView rvStoreList;
    private StoreAdapter adapter;
    private ArrayList<StoreData> storeList;

    private String myId, myRole;

    // 🚨 수정 포인트: AppConfig 기반 서블릿 경로 매핑
    private final String getInfoUrl = AppConfig.API_GET_MEMBER_INFO;
    private final String updateInfoUrl = AppConfig.API_MY_PAGE_UPDATE;
    private final String joinStoreUrl = AppConfig.API_MY_STORE_ADD;
    private final String getStoreListUrl = AppConfig.API_STORE_LIST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. 뷰 초기화
        initView();

        // 2. 데이터 수신 및 초기 설정
        Intent intent = getIntent();
        myId = intent.getStringExtra("userId");
        myRole = intent.getStringExtra("role");
        if (myRole == null) myRole = "S"; // 기본값: 일반 직원

        // 3. 리사이클러뷰 설정
        storeList = new ArrayList<>();
        adapter = new StoreAdapter(this, storeList, myId);
        rvStoreList.setAdapter(adapter);

        // 4. 초기 데이터 로드 (사용자 정보 및 매장 목록)
        if (myId != null) {
            etId.setText(myId);
            etId.setEnabled(false); // ID 수정 불가
            new Thread(() -> loadUserInfo(myId)).start();
            new Thread(() -> loadStoreList(myId)).start();
        }

        // 5. 권한에 따른 UI 분기 처리
        setupRoleUI();

        // 6. 이벤트 리스너 설정
        setupListeners();
    }

    private void initView() {
        etId = findViewById(R.id.etProId);
        etName = findViewById(R.id.etProName);
        etPhone = findViewById(R.id.etProPhone);
        etWage = findViewById(R.id.etProWage);
        etPw = findViewById(R.id.etProPw);
        etPwConfirm = findViewById(R.id.etProPwConfirm);
        etNewStoreId = findViewById(R.id.etNewStoreId);
        etNewStoreName = findViewById(R.id.etNewStoreName);
        tvStoreHelp = findViewById(R.id.tvStoreHelp);
        btnJoinStore = findViewById(R.id.btnJoinStore);
        btnUpdate = findViewById(R.id.btnUpdateAction);
        rvStoreList = findViewById(R.id.rvStoreList);
        rvStoreList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupRoleUI() {
        if ("A".equals(myRole)) {
            tvStoreHelp.setText("새로 생성할 매장의 이름과 ID를 입력하세요.");
            etNewStoreName.setVisibility(View.VISIBLE);
            btnJoinStore.setText("매장 생성");
        } else {
            tvStoreHelp.setText("근무할 매장의 ID를 입력하세요.");
            etNewStoreName.setVisibility(View.GONE);
            btnJoinStore.setText("매장 등록");
        }
    }

    private void setupListeners() {
        // 정보 수정 버튼
        btnUpdate.setOnClickListener(v -> {
            String pw = etPw.getText().toString().trim();
            String pwConfirm = etPwConfirm.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String wage = etWage.getText().toString().trim();

            if (!pw.isEmpty() && !pw.equals(pwConfirm)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> updateUserInfo(myId, pw, name, phone, wage)).start();
        });

        // 매장 추가/생성 버튼
        btnJoinStore.setOnClickListener(v -> {
            String storeId = etNewStoreId.getText().toString().trim();
            if (storeId.isEmpty()) {
                Toast.makeText(this, "매장 ID를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("A".equals(myRole)) {
                String storeName = etNewStoreName.getText().toString().trim();
                if (storeName.isEmpty()) {
                    Toast.makeText(this, "매장 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(() -> requestStoreAction(myId, storeId, storeName)).start();
            } else {
                new Thread(() -> requestStoreAction(myId, storeId, null)).start();
            }
        });
    }

    /**
     * 서버로부터 소속 매장 목록을 가져온다.
     */
    private void loadStoreList(String userId) {
        try {
            URL url = new URL(getStoreListUrl + "?id=" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                ArrayList<StoreData> tempList = new ArrayList<>();
                if (json.has("data")) {
                    JSONArray dataArray = json.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject item = dataArray.getJSONObject(i);
                        tempList.add(new StoreData(
                                item.getString("storeId"),
                                item.getString("storeName"),
                                item.optString("origin", "Server")
                        ));
                    }
                }
                runOnUiThread(() -> {
                    storeList.clear();
                    storeList.addAll(tempList);
                    adapter.notifyDataSetChanged();
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * 매장 등록 또는 생성을 처리한다.
     */
    private void requestStoreAction(String userId, String storeId, String storeName) {
        try {
            // POST 데이터 구성
            String postData = "id=" + userId + "&storeId=" + storeId;
            if (storeName != null) postData += "&storeName=" + URLEncoder.encode(storeName, "UTF-8");

            URL url = new URL(joinStoreUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = br.readLine();
                JSONObject json = new JSONObject(response);
                String message = json.getString("message");
                String status = json.getString("status");

                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    if ("success".equals(status)) {
                        etNewStoreId.setText("");
                        etNewStoreName.setText("");
                        new Thread(() -> loadStoreList(myId)).start();
                    }
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * 서버에서 내 정보(이름, 번호, 시급)를 불러온다.
     */
    private void loadUserInfo(String id) {
        try {
            URL url = new URL(getInfoUrl + "?id=" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JSONObject json = new JSONObject(br.readLine());
                if ("success".equals(json.getString("status"))) {
                    runOnUiThread(() -> {
                        try {
                            etName.setText(json.getString("name"));
                            etPhone.setText(json.getString("phone"));
                            etWage.setText(json.getString("wage"));
                        } catch (Exception e) {}
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * 내 정보를 수정한다 (POST 방식).
     */
    private void updateUserInfo(String id, String pw, String name, String phone, String wage) {
        try {
            URL url = new URL(updateInfoUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String postData = "id=" + id + "&pw=" + pw +
                    "&name=" + URLEncoder.encode(name, "UTF-8") +
                    "&phone=" + phone + "&wage=" + wage;

            try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JSONObject json = new JSONObject(new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine());
                String message = json.getString("message");
                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    if ("success".equals(json.optString("status"))) finish();
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}