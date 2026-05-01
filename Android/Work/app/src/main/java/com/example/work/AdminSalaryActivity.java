package com.example.work;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
 * 1. 중앙 집중식 설정: AppConfig.API_GET_SALARY_INFO를 사용하여 서블릿 경로(/GetSalaryInfo)를 호출합니다.
 * 2. 동적 데이터 바인딩: 서버에서 계산되어 내려오는 '시간'과 '급여' 문자열을 리스트뷰에 실시간으로 반영합니다.
 * 3. 예외 처리: 데이터가 없는 경우나 서버 통신 실패 시 사용자에게 알림(Toast)을 제공하여 앱의 안정성을 높였습니다.
 */
public class AdminSalaryActivity extends AppCompatActivity {

    private EditText etYear, etMonth;
    private Button btnSearch;
    private ListView listView;
    private ArrayList<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private String storeId;

    // 🚨 수정 포인트: AppConfig 기반 서블릿 경로 매핑
    private final String urlAddr = AppConfig.API_GET_SALARY_INFO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_salary);

        // 1. 뷰 초기화
        initView();

        // 2. 인텐트 데이터 수신
        storeId = getIntent().getStringExtra("storeId");
        if (storeId == null) storeId = "";

        // 3. 기본 날짜 설정 (현재 연도/월)
        Calendar cal = Calendar.getInstance();
        etYear.setText(String.valueOf(cal.get(Calendar.YEAR)));
        etMonth.setText(String.valueOf(cal.get(Calendar.MONTH) + 1));

        // 4. 어댑터 설정
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        // 5. 검색 버튼 이벤트
        btnSearch.setOnClickListener(v -> {
            String y = etYear.getText().toString().trim();
            String m = etMonth.getText().toString().trim();

            if (y.isEmpty() || m.isEmpty()) {
                Toast.makeText(this, "연도와 월을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 월 입력 유효성 검사 (1~12)
            int monthInt = Integer.parseInt(m);
            if (monthInt < 1 || monthInt > 12) {
                Toast.makeText(this, "월은 1에서 12 사이로 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 서블릿으로부터 급여 데이터 로드 (비동기)
            new Thread(() -> loadSalaryData(y, m)).start();
        });
    }

    private void initView() {
        etYear = findViewById(R.id.etSalaryYear);
        etMonth = findViewById(R.id.etSalaryMonth);
        btnSearch = findViewById(R.id.btnSalarySearch);
        listView = findViewById(R.id.lvSalary);
    }

    /**
     * 서버의 GetSalaryInfo 서블릿으로 급여 정보를 요청합니다.
     */
    private void loadSalaryData(String year, String month) {
        try {
            // 🚨 팩트체크: GetSalaryInfo.java 서블릿은 storeId, year, month 파라미터를 기대함
            String query = String.format(Locale.KOREA, "?storeId=%s&year=%s&month=%s", storeId, year, month);
            URL url = new URL(urlAddr + query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                String status = json.optString("status", "fail");

                runOnUiThread(() -> {
                    dataList.clear();
                    if ("success".equals(status)) {
                        JSONArray ja = json.optJSONArray("data");

                        if (ja == null || ja.length() == 0) {
                            Toast.makeText(this, "해당 기간의 급여 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject obj = ja.optJSONObject(i);
                                String name = obj.optString("name", "이름없음");
                                String time = obj.optString("time", "0시간");
                                String pay = obj.optString("pay", "0원");

                                String text = String.format("%s\n근무시간: %s | 예상 급여: %s", name, time, pay);
                                dataList.add(text);
                            }
                        }
                    } else {
                        String msg = json.optString("message", "정보 조회에 실패했습니다.");
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                    adapter.notifyDataSetChanged();
                });
            } else {
                int code = conn.getResponseCode();
                runOnUiThread(() -> Toast.makeText(this, "서버 연결 실패: " + code, Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
        }
    }
}