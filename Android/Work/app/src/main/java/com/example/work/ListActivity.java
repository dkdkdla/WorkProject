package com.example.work;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * [코드 설계 설명]
 * 1. 중앙 집중식 설정: AppConfig.API_MY_ATTENDANCE 상수를 사용하여 서버의 /MyAttendance 서블릿과 통신합니다.
 * 2. 동적 UI 바인딩: 서버에서 내려온 출/퇴근 상태에 따라 텍스트 색상을 다르게 표시하는 ViewBinder를 적용했습니다.
 * 3. 예외 방어 설계: 인텐트로 전달된 데이터가 없을 경우를 대비해 기본값 처리 및 사용자 알림 로직을 강화했습니다.
 */
public class ListActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnStartDate, btnEndDate, btnSearch;
    private TextView tvTotalPay, tvWageInfo, tvListStoreName;

    private String currentUserId, currentStoreName, currentStoreId;
    private String selectedStartDate = "", selectedEndDate = "";
    private ArrayList<HashMap<String, String>> dataList = new ArrayList<>();

    // 🚨 수정 포인트: AppConfig 기반의 서블릿 경로 설정
    private final String serverUrl = AppConfig.API_MY_ATTENDANCE;
    private static final String KEY_TYPE = "type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // 1. 뷰 초기화
        initView();

        // 2. 인텐트 데이터 수신
        Intent intent = getIntent();
        currentUserId = intent.getStringExtra("userId");
        currentStoreName = intent.getStringExtra("storeName");
        currentStoreId = intent.getStringExtra("storeId");

        // 데이터 검증 및 로그 기록
        Log.d("ALBA_DEBUG", "ListActivity - ID: " + currentUserId + ", Store: " + currentStoreId);

        if (currentStoreName != null) {
            tvListStoreName.setText(String.format("%s 근무 기록", currentStoreName));
        } else {
            tvListStoreName.setText("내 근무 기록");
        }

        if (currentUserId == null) {
            Toast.makeText(this, "사용자 정보가 없어 정상적인 조회가 불가능합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. 이벤트 리스너 설정
        setupListeners();

        // 날짜 기본값: 이번 달 1일 ~ 오늘
        Calendar defCal = Calendar.getInstance();
        selectedEndDate   = String.format(Locale.KOREA, "%d-%02d-%02d",
                defCal.get(Calendar.YEAR), defCal.get(Calendar.MONTH)+1, defCal.get(Calendar.DAY_OF_MONTH));
        selectedStartDate = String.format(Locale.KOREA, "%d-%02d-01",
                defCal.get(Calendar.YEAR), defCal.get(Calendar.MONTH)+1);
        btnStartDate.setText(selectedStartDate);
        btnEndDate.setText(selectedEndDate);

        // 4. 초기 데이터 로드 (이번 달)
        new Thread(() -> loadData(currentUserId, selectedStartDate, selectedEndDate)).start();
    }

    private void initView() {
        listView = findViewById(R.id.listView);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnSearch = findViewById(R.id.btnSearch);
        tvTotalPay = findViewById(R.id.tvTotalPay);
        tvWageInfo = findViewById(R.id.tvWageInfo);
        tvListStoreName = findViewById(R.id.tvListStoreName);
    }

    private void setupListeners() {
        btnStartDate.setOnClickListener(v -> showDatePicker(btnStartDate, true));
        btnEndDate.setOnClickListener(v -> showDatePicker(btnEndDate, false));

        btnSearch.setOnClickListener(v -> {
            if (selectedStartDate.isEmpty() || selectedEndDate.isEmpty()) {
                Toast.makeText(this, "조회할 시작일과 종료일을 모두 선택해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "조회 중...", Toast.LENGTH_SHORT).show();
                new Thread(() -> loadData(currentUserId, selectedStartDate, selectedEndDate)).start();
            }
        });
    }

    private void showDatePicker(Button button, boolean isStart) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.KOREA, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            button.setText(dateStr);
            if (isStart) selectedStartDate = dateStr;
            else selectedEndDate = dateStr;
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    /**
     * 서버의 MyAttendance 서블릿으로부터 개인별 근무 기록을 가져옵니다.
     */
    private void loadData(String userId, String sDate, String eDate) {
        HttpURLConnection conn = null;
        try {
            // 🚨 팩트체크: MyAttendance.java 서블릿 파라미터 규격에 맞게 쿼리 생성
            String page = serverUrl + "?id=" + userId + "&storeId=" + currentStoreId;
            if (!sDate.isEmpty() && !eDate.isEmpty()) {
                page += "&startDate=" + sDate + "&endDate=" + eDate;
            }

            URL url = new URL(page);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject root = new JSONObject(sb.toString());

                // 🚨 팩트체크: 서버 응답 JSON의 summary 필드와 list 배열 구조를 분석함
                JSONObject summary = root.optJSONObject("summary");
                long totalPay = (summary != null) ? summary.optLong("totalPay", 0) : 0;
                int hourlyWage = (summary != null) ? summary.optInt("hourlyWage", 0) : 0;

                JSONArray jsonArray = root.getJSONArray("list");
                ArrayList<HashMap<String, String>> tempList = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("date", json.getString("date"));
                    map.put("time", json.getString("time"));
                    map.put(KEY_TYPE, json.getString(KEY_TYPE));
                    tempList.add(map);
                }

                runOnUiThread(() -> {
                    dataList.clear();
                    dataList.addAll(tempList);

                    DecimalFormat df = new DecimalFormat("###,###");
                    if (!sDate.isEmpty() && !eDate.isEmpty()) {
                        tvTotalPay.setText(String.format("예상 급여: %s원", df.format(totalPay)));
                    } else {
                        tvTotalPay.setText("최근 근무 내역");
                    }
                    tvWageInfo.setText(String.format("(시급: %s원)", df.format(hourlyWage)));

                    SimpleAdapter adapter = new SimpleAdapter(
                            this, dataList, R.layout.item_list,
                            new String[]{"date", "time", KEY_TYPE},
                            new int[]{R.id.tvDate, R.id.tvTime, R.id.tvType}
                    );

                    // 텍스트 색상 커스텀 처리
                    adapter.setViewBinder((view, data, textRepresentation) -> {
                        if (view.getId() == R.id.tvType) {
                            TextView tvType = (TextView) view;
                            String type = (String) data;
                            tvType.setText(type);
                            if ("출근".equals(type) || "IN".equals(type)) {
                                tvType.setTextColor(Color.parseColor("#4CAF50"));
                            } else if ("퇴근".equals(type) || "OUT".equals(type)) {
                                tvType.setTextColor(Color.parseColor("#F44336"));
                            } else {
                                tvType.setTextColor(Color.BLACK);
                            }
                            return true;
                        }
                        return false;
                    });
                    listView.setAdapter(adapter);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "기록 로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}