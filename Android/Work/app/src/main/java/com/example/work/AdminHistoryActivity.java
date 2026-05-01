package com.example.work;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * [코드 설계 설명]
 * 1. 데이터 매핑 최적화: 서버에서 내려주는 DB 고유 번호(idx)를 HistoryItem에 포함하여 수정/삭제 시 정확한 대상을 식별합니다.
 * 2. AppConfig 통합: 모든 URL을 AppConfig 상수를 사용하여 관리함으로써 하드코딩된 IP 잔해를 제거했습니다.
 * 3. 생명주기 관리: onResume에서 목록을 갱신하여 수정 화면에서 돌아왔을 때 변경 사항이 즉시 반영되도록 설계했습니다.
 */
public class AdminHistoryActivity extends AppCompatActivity {

    private ListView listView;
    private Spinner spnEmployee;
    private Button btnStartDate, btnEndDate, btnSearch;
    private ArrayList<HistoryItem> items = new ArrayList<>();
    private ArrayList<Employee> employeeList = new ArrayList<>();
    private HistoryAdapter adapter;

    private boolean isInitialLoad = true;
    private String storeId;
    private String selectedMemId = "ALL";
    private String selectedStartDate = "";
    private String selectedEndDate = "";

    // 🚨 수정 포인트: AppConfig 기반 서블릿 경로 설정
    private final String listUrl = AppConfig.BASE_URL + "AdminHistory"; // 기록 조회 서블릿
    private final String employeeUrl = AppConfig.API_ADMIN_MEMBER_LIST; // 직원 목록 서블릿

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_history);

        // 1. 뷰 초기화
        initView();

        // 2. 데이터 수신
        storeId = getIntent().getStringExtra("storeId");

        // 3. 어댑터 연결
        adapter = new HistoryAdapter();
        listView.setAdapter(adapter);

        // 4. 직원 목록 로드 (스피너 구성)
        new Thread(this::loadEmployees).start();

        // 5. 이벤트 리스너 설정
        setupListeners();
    }

    private void initView() {
        listView = findViewById(R.id.lvAdminHistory);
        spnEmployee = findViewById(R.id.spnEmployee);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnSearch = findViewById(R.id.btnSearch);
    }

    private void setupListeners() {
        // 날짜 선택 버튼
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));

        // 검색 버튼
        btnSearch.setOnClickListener(v -> new Thread(this::loadHistory).start());

        // 기록 수동 추가 버튼 (가정)
        Button btnAddRecord = findViewById(R.id.btnAddRecord);
        if (btnAddRecord != null) {
            btnAddRecord.setOnClickListener(v -> {
                Intent intent = new Intent(AdminHistoryActivity.this, AdminAddActivity.class);
                intent.putExtra("storeId", storeId);
                startActivity(intent);
            });
        }

        // 리스트 아이템 클릭 (수정 화면으로 이동)
        listView.setOnItemClickListener((parent, view, position, id) -> {
            HistoryItem item = items.get(position);
            Intent intent = new Intent(AdminHistoryActivity.this, AdminEditActivity.class);

            // 🚨 중요: AdminAttendanceAction 서블릿 처리를 위해 'idx'를 반드시 넘겨야 함
            intent.putExtra("idx", item.idx);
            intent.putExtra("memId", item.memId);
            intent.putExtra("memName", item.name);
            intent.putExtra("type", item.type);
            intent.putExtra("fullTime", item.fullTime);

            try {
                if (item.fullTime.length() >= 10) {
                    intent.putExtra("date", item.fullTime.substring(0, 10));
                    intent.putExtra("time", item.fullTime.substring(11));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 수정 후 돌아왔을 때 목록 최신화
        if (storeId != null && !isInitialLoad) {
            new Thread(this::loadHistory).start();
        }
    }

    private void loadEmployees() {
        try {
            employeeList.clear();
            employeeList.add(new Employee("ALL", "전체 직원"));

            URL url = new URL(employeeUrl + "?storeId=" + storeId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                if (json.has("members")) {
                    JSONArray array = json.getJSONArray("members");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        employeeList.add(new Employee(obj.getString("id"), obj.getString("name")));
                    }
                }

                runOnUiThread(() -> {
                    String[] names = new String[employeeList.size()];
                    for (int i = 0; i < employeeList.size(); i++) names[i] = employeeList.get(i).name;

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
                    spnEmployee.setAdapter(spinnerAdapter);

                    spnEmployee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedMemId = employeeList.get(position).id;
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });

                    if (isInitialLoad) {
                        new Thread(this::loadHistory).start();
                        isInitialLoad = false;
                    }
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.KOREA, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            if (isStartDate) {
                selectedStartDate = date;
                btnStartDate.setText(date);
            } else {
                selectedEndDate = date;
                btnEndDate.setText(date);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void loadHistory() {
        try {
            items.clear();
            String params = "?storeId=" + storeId + "&memId=" + selectedMemId + "&startDate=" + selectedStartDate + "&endDate=" + selectedEndDate;
            URL url = new URL(listUrl + params);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                if (json.has("data")) {
                    JSONArray data = json.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject obj = data.getJSONObject(i);
                        // 🚨 팩트체크: 서버 JSON 응답에 'idx'가 반드시 포함되어 있어야 함
                        items.add(new HistoryItem(
                                obj.optString("idx", "0"),
                                obj.optString("mem_id", ""),
                                obj.getString("name"),
                                obj.getString("type"),
                                obj.getString("time")
                        ));
                    }
                }
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    if (items.isEmpty()) Toast.makeText(this, "조회된 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 데이터 모델 클래스
    static class Employee {
        String id, name;
        Employee(String id, String name) { this.id = id; this.name = name; }
    }

    static class HistoryItem {
        String idx, memId, name, type, fullTime;
        HistoryItem(String idx, String memId, String name, String type, String fullTime) {
            this.idx = idx; this.memId = memId; this.name = name; this.type = type; this.fullTime = fullTime;
        }
    }

    // 내부 어댑터 클래스
    class HistoryAdapter extends BaseAdapter {
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = getLayoutInflater().inflate(R.layout.item_admin_history, parent, false);
            HistoryItem item = items.get(position);

            TextView tvName = convertView.findViewById(R.id.tvAdminItemName);
            TextView tvType = convertView.findViewById(R.id.tvAdminItemType);
            TextView tvTime = convertView.findViewById(R.id.tvAdminItemTime);

            tvName.setText(item.name);
            tvType.setText(item.type);
            tvTime.setText(item.fullTime);

            if ("출근".equals(item.type) || "IN".equals(item.type)) {
                tvType.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                tvType.setTextColor(Color.parseColor("#F44336"));
            }
            return convertView;
        }
    }
}