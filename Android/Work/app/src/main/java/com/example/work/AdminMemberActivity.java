package com.example.work;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * [코드 설계 설명]
 * 1. 데이터 필터링: 원본 리스트(originalList)를 보존하여 검색 시 데이터 손실을 방지합니다.
 * 2. 동적 갱신: 시급 수정(POST) 성공 시 서버 데이터를 다시 로드하여 UI를 동기화합니다.
 */
public class AdminMemberActivity extends AppCompatActivity {

    private ListView listView;
    private SearchView searchView;
    private MemberAdapter adapter;

    private ArrayList<MemberDTO> memberList = new ArrayList<>();
    private ArrayList<MemberDTO> originalList = new ArrayList<>();

    private final String listUrl = AppConfig.API_ADMIN_MEMBER_LIST;
    private final String updateUrl = AppConfig.API_ADMIN_MEMBER_UPDATE;

    private String currentStoreId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_member);

        listView = findViewById(R.id.lvMember);
        searchView = findViewById(R.id.searchView);

        // 🚨 팩트체크: 이전 화면에서 넘겨받은 매장 ID 확인
        currentStoreId = getIntent().getStringExtra("storeId");
        if (currentStoreId == null) currentStoreId = "";

        adapter = new MemberAdapter(this, memberList);
        listView.setAdapter(adapter);

        getData(); // 초기 로드

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        // 클릭 시 시급 수정 팝업
        listView.setOnItemClickListener((parent, view, position, id) -> {
            MemberDTO member = memberList.get(position);
            showWageUpdateDialog(member);
        });
    }

    private void showWageUpdateDialog(MemberDTO member) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(member.getHourlyWage()));

        new AlertDialog.Builder(this)
                .setTitle(member.getName() + "님의 시급 수정")
                .setMessage("변경할 시급을 입력하세요.")
                .setView(input)
                .setPositiveButton("수정", (dialog, which) -> {
                    String newWage = input.getText().toString();
                    if (!newWage.isEmpty()) {
                        new Thread(() -> updateMemberWage(member.getId(), newWage)).start();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void filterList(String text) {
        memberList.clear();
        if (text.isEmpty()) {
            memberList.addAll(originalList);
        } else {
            for (MemberDTO dto : originalList) {
                if (dto.getName().toLowerCase().contains(text.toLowerCase())) {
                    memberList.add(dto);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void getData() {
        new Thread(() -> {
            try {
                // 🚨 팩트체크: 수정된 서버 주소에 매장 ID를 쿼리 스트링으로 전달
                URL url = new URL(listUrl + "?storeId=" + currentStoreId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);

                    String response = sb.toString().trim();
                    Log.d("ADMIN_MEMBER", "Response: " + response);

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("members");

                    ArrayList<MemberDTO> temp = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        temp.add(new MemberDTO(
                                obj.optString("id", ""),
                                obj.optString("name", "이름없음"),
                                obj.optString("phone", "-"),
                                obj.optInt("wage", 0)
                        ));
                    }

                    runOnUiThread(() -> {
                        memberList.clear();
                        originalList.clear();
                        memberList.addAll(temp);
                        originalList.addAll(temp);
                        adapter.notifyDataSetChanged();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "데이터 로드 실패", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateMemberWage(String memId, String newWage) {
        try {
            URL url = new URL(updateUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String postData = "id=" + memId + "&wage=" + newWage;

            try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JSONObject json = new JSONObject(br.readLine());
                String status = json.optString("status", "fail");
                String message = json.optString("message", "수정 성공");

                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    if ("success".equals(status)) {
                        getData(); // 갱신
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}