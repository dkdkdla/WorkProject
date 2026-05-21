package com.example.work;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

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

public class ProfileActivity extends AppCompatActivity {

    private EditText etId, etName, etPhone, etWage;
    private EditText etPw, etPwConfirm;
    private EditText etNewStoreId, etNewStoreName;
    private TextView tvStoreHelp, tvSelectedStore, tvPendingNotice;
    private Button btnJoinStore, btnCreateStore, btnStoreSearch, btnUpdate;

    private RecyclerView rvStoreList;
    private StoreAdapter adapter;
    private ArrayList<StoreData> storeList;

    private String myId, myRole;
    private String selectedStoreId   = "";
    private String selectedStoreName = "";

    private ArrayList<String[]> storeSearchList = new ArrayList<>();

    private final String getInfoUrl    = AppConfig.API_GET_MEMBER_INFO;
    private final String updateInfoUrl = AppConfig.API_MY_PAGE_UPDATE;
    private final String joinStoreUrl  = AppConfig.API_MY_STORE_ADD;
    private final String storeManageUrl = AppConfig.BASE_URL + "StoreManage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initView();

        myId   = getIntent().getStringExtra("userId");
        myRole = getIntent().getStringExtra("role");
        if (myRole == null) myRole = "S";

        storeList = new ArrayList<>();
        adapter   = new StoreAdapter(this, storeList, myId);
        rvStoreList.setAdapter(adapter);
        rvStoreList.setLayoutManager(new LinearLayoutManager(this));

        if (myId != null) {
            etId.setText(myId);
            etId.setEnabled(false);
            new Thread(() -> loadUserInfo(myId)).start();
            new Thread(() -> loadStoreList(myId)).start();
        }

        setupRoleUI();
        setupListeners();
    }

    private void initView() {
        etId           = findViewById(R.id.etProId);
        etName         = findViewById(R.id.etProName);
        etPhone        = findViewById(R.id.etProPhone);
        etWage         = findViewById(R.id.etProWage);
        etPw           = findViewById(R.id.etProPw);
        etPwConfirm    = findViewById(R.id.etProPwConfirm);
        etNewStoreId   = findViewById(R.id.etNewStoreId);
        etNewStoreName = findViewById(R.id.etNewStoreName);
        tvStoreHelp    = findViewById(R.id.tvStoreHelp);
        tvSelectedStore = findViewById(R.id.tvSelectedStore);
        tvPendingNotice = findViewById(R.id.tvPendingNotice);
        btnJoinStore   = findViewById(R.id.btnJoinStore);
        btnCreateStore = findViewById(R.id.btnCreateStore);
        btnStoreSearch = findViewById(R.id.btnStoreSearch);
        btnUpdate      = findViewById(R.id.btnUpdateAction);
        rvStoreList    = findViewById(R.id.rvStoreList);
    }

    private void setupRoleUI() {
        if ("A".equals(myRole)) {
            // 점장: 매장 생성 신청 UI
            tvStoreHelp.setText("새 매장 생성 신청 (전체관리자 승인 후 사용 가능)");
            etNewStoreName.setVisibility(View.VISIBLE);
            etNewStoreId.setVisibility(View.VISIBLE);
            btnCreateStore.setVisibility(View.VISIBLE);
            btnStoreSearch.setVisibility(View.GONE);
            btnJoinStore.setVisibility(View.GONE);
        } else {
            // 직원: 매장 검색 소속 신청 UI
            tvStoreHelp.setText("소속 매장 신청 (점장 승인 후 활성화)");
            etNewStoreName.setVisibility(View.GONE);
            etNewStoreId.setVisibility(View.GONE);
            btnCreateStore.setVisibility(View.GONE);
            btnStoreSearch.setVisibility(View.VISIBLE);
            btnJoinStore.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        // 정보 수정
        btnUpdate.setOnClickListener(v -> {
            String pw       = etPw.getText().toString().trim();
            String pwConfirm = etPwConfirm.getText().toString().trim();
            String name     = etName.getText().toString().trim();
            String phone    = etPhone.getText().toString().trim();
            String wage     = etWage.getText().toString().trim();

            if (!pw.isEmpty() && !pw.equals(pwConfirm)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> updateUserInfo(myId, pw, name, phone, wage)).start();
        });

        // 직원: 매장 검색 BottomSheet
        btnStoreSearch.setOnClickListener(v -> showStoreSearchSheet());

        // 직원: 소속 신청
        btnJoinStore.setOnClickListener(v -> {
            if (selectedStoreId.isEmpty()) {
                Toast.makeText(this, "매장을 검색하여 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> requestJoinStore(myId, selectedStoreId)).start();
        });

        // 점장: 매장 생성 신청
        btnCreateStore.setOnClickListener(v -> {
            String storeId   = etNewStoreId.getText().toString().trim();
            String storeName = etNewStoreName.getText().toString().trim();
            if (storeId.isEmpty() || storeName.isEmpty()) {
                Toast.makeText(this, "매장 코드와 이름을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> requestCreateStore(storeId, storeName)).start();
        });
    }

    // BottomSheetDialog 매장 검색 (직원용)
    private void showStoreSearchSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_store_search, null);
        sheet.setContentView(view);

        EditText etSearch = view.findViewById(R.id.etSheetSearch);
        ListView lvResult = view.findViewById(R.id.lvSheetResult);
        TextView tvEmpty  = view.findViewById(R.id.tvSheetEmpty);

        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<>());
        lvResult.setAdapter(listAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.length() >= 1) {
                    new Thread(() -> searchStore(keyword, listAdapter, lvResult, tvEmpty)).start();
                } else {
                    storeSearchList.clear();
                    runOnUiThread(() -> {
                        listAdapter.clear();
                        tvEmpty.setVisibility(View.VISIBLE);
                        lvResult.setVisibility(View.GONE);
                    });
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        lvResult.setOnItemClickListener((parent, v2, position, id) -> {
            if (position < storeSearchList.size()) {
                String[] store   = storeSearchList.get(position);
                selectedStoreId   = store[0];
                selectedStoreName = store[1];
                btnStoreSearch.setText("선택: " + selectedStoreName + " (" + selectedStoreId + ")");
                tvSelectedStore.setText("✓ 선택된 매장: " + selectedStoreName);
                tvSelectedStore.setVisibility(View.VISIBLE);
                sheet.dismiss();
            }
        });

        sheet.show();
    }

    // 매장 검색 (SearchStore 서블릿 호출)
    private void searchStore(String keyword, ArrayAdapter<String> listAdapter,
                             ListView lvResult, TextView tvEmpty) {
        try {
            String apiUrl = AppConfig.BASE_URL + "SearchStore?keyword=" +
                    URLEncoder.encode(keyword, "UTF-8");
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                storeSearchList.clear();
                ArrayList<String> displayList = new ArrayList<>();

                if ("success".equals(json.optString("status")) && json.has("list")) {
                    JSONArray arr = json.getJSONArray("list");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        storeSearchList.add(new String[]{obj.getString("id"), obj.getString("name")});
                        displayList.add(obj.getString("name") + " (" + obj.getString("id") + ")");
                    }
                }

                runOnUiThread(() -> {
                    listAdapter.clear();
                    listAdapter.addAll(displayList);
                    listAdapter.notifyDataSetChanged();
                    if (displayList.isEmpty()) {
                        tvEmpty.setText("검색 결과가 없습니다.");
                        tvEmpty.setVisibility(View.VISIBLE);
                        lvResult.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        lvResult.setVisibility(View.VISIBLE);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 직원: 매장 소속 신청
    private void requestJoinStore(String userId, String storeId) {
        try {
            String postData = "userId=" + userId + "&add_store_id=" + storeId;
            URL url = new URL(joinStoreUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);

            try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JSONObject json = new JSONObject(br.readLine());
                String status  = json.optString("status", "fail");
                String message = json.optString("message", "처리 실패");

                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    if ("success".equals(status)) {
                        // PENDING 안내 표시
                        tvPendingNotice.setVisibility(View.VISIBLE);
                        selectedStoreId   = "";
                        selectedStoreName = "";
                        btnStoreSearch.setText("소속 매장 검색");
                        tvSelectedStore.setVisibility(View.GONE);
                        new Thread(() -> loadStoreList(myId)).start();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
        }
    }

    // 점장: 매장 생성 신청
    private void requestCreateStore(String storeId, String storeName) {
        try {
            String postData = "storeId=" + storeId +
                    "&storeName=" + URLEncoder.encode(storeName, "UTF-8") +
                    "&userId=" + myId;
            URL url = new URL(storeManageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);

            try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JSONObject json = new JSONObject(br.readLine());
                String status  = json.optString("status", "fail");
                String message = json.optString("message", "처리 실패");

                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    if ("success".equals(status)) {
                        etNewStoreId.setText("");
                        etNewStoreName.setText("");
                        new Thread(() -> loadStoreList(myId)).start();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadStoreList(String userId) {
        try {
            URL url = new URL(AppConfig.API_STORE_LIST + "?id=" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONArray dataArray = new JSONArray(sb.toString().trim());
                ArrayList<StoreData> tempList = new ArrayList<>();

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject obj = dataArray.getJSONObject(i);
                    tempList.add(new StoreData(
                            obj.getString("id"),
                            obj.getString("name"),
                            "Server"
                    ));
                }

                runOnUiThread(() -> {
                    storeList.clear();
                    storeList.addAll(tempList);
                    adapter.notifyDataSetChanged();
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadUserInfo(String id) {
        try {
            URL url = new URL(getInfoUrl + "?id=" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JSONObject json = new JSONObject(br.readLine());
                if ("success".equals(json.optString("status"))) {
                    runOnUiThread(() -> {
                        try {
                            etName.setText(json.getString("name"));
                            etPhone.setText(json.getString("phone"));
                            etWage.setText(json.getString("wage"));
                        } catch (Exception e) { e.printStackTrace(); }
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateUserInfo(String id, String pw, String name, String phone, String wage) {
        try {
            URL url = new URL(updateInfoUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);

            String postData = "id=" + id + "&pw=" + pw +
                    "&name=" + URLEncoder.encode(name, "UTF-8") +
                    "&phone=" + phone + "&wage=" + wage;

            try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JSONObject json = new JSONObject(br.readLine());
                String message = json.optString("message", "처리 실패");
                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    if ("success".equals(json.optString("status"))) finish();
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}