package com.example.work;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * [코드 설계 설명]
 * 1. 중앙 집중식 URL 관리: AppConfig.BASE_URL을 사용하여 서버 주소 변경 시 대응이 쉽도록 설계했습니다.
 * 2. 서블릿 기반 통신: 구형 .jsp 경로를 버리고 /AdminMemberList 및 /AddWork 서블릿을 호출합니다.
 * 3. 비동기 네트워크 처리: 네트워크 작업은 별도 스레드에서, UI 업데이트는 runOnUiThread에서 처리합니다.
 */
public class AdminAddActivity extends AppCompatActivity {

    private Spinner spMember, spType;
    private EditText etDate, etTime;
    private Button btnSave;

    private String storeId;

    // 직원 목록 데이터 저장용
    private ArrayList<String> memberNames = new ArrayList<>();
    private ArrayList<String> memberIds = new ArrayList<>();
    private ArrayAdapter<String> memberAdapter;

    private String[] typeOptions = {"출근", "퇴근"};

    // 🚨 수정 포인트: AppConfig 기반의 서블릿 경로 설정
    private final String listUrl = AppConfig.BASE_URL + "AdminMemberList";
    private final String addUrl = AppConfig.BASE_URL + "AddWork";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add);

        // 이전 액티비티로부터 매장 ID 수신
        storeId = getIntent().getStringExtra("storeId");

        // 뷰 초기화
        spMember = findViewById(R.id.spMember);
        spType = findViewById(R.id.spType);
        etDate = findViewById(R.id.etAddDate);
        etTime = findViewById(R.id.etAddTime);
        btnSave = findViewById(R.id.btnAddSave);

        // 현재 날짜 및 시간 기본값 설정
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        Date now = new Date();
        etDate.setText(sdfDate.format(now));
        etTime.setText(sdfTime.format(now));

        // 어댑터 설정
        memberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, memberNames);
        spMember.setAdapter(memberAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeOptions);
        spType.setAdapter(typeAdapter);

        // 직원 목록 불러오기 (비동기)
        new Thread(this::loadMembers).start();

        // 저장 버튼 이벤트
        btnSave.setOnClickListener(v -> {
            if (memberIds.isEmpty()) {
                Toast.makeText(this, "직원 목록을 불러오는 중이거나 직원이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            int position = spMember.getSelectedItemPosition();
            if (position < 0) return;
            String selectedId = memberIds.get(position);

            String date = etDate.getText().toString();
            String time = etTime.getText().toString();
            String dateTime = date + " " + time; // 서버(AddWork.java)가 기대하는 포맷

            String type = spType.getSelectedItem().toString();

            // 기록 추가 요청 (비동기)
            new Thread(() -> requestAdd(selectedId, dateTime, type)).start();
        });
    }

    /**
     * 서버의 AdminMemberList 서블릿으로부터 직원 목록을 JSON으로 받아옵니다.
     */
    private void loadMembers() {
        try {
            // GET 방식으로 storeId 전달
            URL url = new URL(listUrl + "?storeId=" + storeId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());

                memberNames.clear();
                memberIds.clear();

                if (json.has("members")) {
                    JSONArray array = json.getJSONArray("members");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String id = obj.getString("id");
                        String name = obj.getString("name");

                        memberNames.add(name + " (" + id + ")");
                        memberIds.add(id);
                    }
                    runOnUiThread(() -> memberAdapter.notifyDataSetChanged());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 서버의 AddWork 서블릿으로 근무 기록 추가를 요청합니다.
     */
    private void requestAdd(String id, String dateTime, String type) {
        try {
            // 한글 및 공백 인코딩 필수
            String encodedType = URLEncoder.encode(type, "UTF-8");
            String encodedDateTime = URLEncoder.encode(dateTime, "UTF-8");

            // 🚨 팩트체크: AddWork.java 서블릿의 파라미터(id, storeId, dateTime, type)와 일치시킴
            String page = addUrl + "?id=" + id + "&storeId=" + storeId + "&dateTime=" + encodedDateTime + "&type=" + encodedType;
            URL url = new URL(page);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                String message = json.getString("message");
                String status = json.getString("status");

                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    if ("success".equals(status)) finish(); // 성공 시 이전 화면으로
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
        }
    }
}