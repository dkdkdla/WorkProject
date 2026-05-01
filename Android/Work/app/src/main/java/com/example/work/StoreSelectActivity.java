package com.example.work;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * [코드 설계 설명]
 * 1. JSON 배열 처리: 서버가 [ ] 형태의 배열을 반환하므로 new JSONArray()로 직접 파싱하여 에러를 방지합니다.
 * 2. 데이터 매핑: 서버의 "id", "name" 키값을 안드로이드 리스트뷰의 "display" 텍스트로 변환합니다.
 * 3. 화면 전환: 선택된 매장 정보와 사용자 권한(role)을 바탕으로 Admin 또는 Menu 액티비티로 라우팅합니다.
 */
public class StoreSelectActivity extends AppCompatActivity {

    private ListView lvStoreList;
    private TextView tvSelectTitle;
    private String userId, userName, role;
    private ArrayList<HashMap<String, String>> storeList = new ArrayList<>();

    // AppConfig에 정의된 URL 사용
    private final String storeUrl = AppConfig.API_STORE_LIST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_select);

        lvStoreList = findViewById(R.id.lvStoreList);
        tvSelectTitle = findViewById(R.id.tvSelectTitle);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");
        role = intent.getStringExtra("role");

        if (userName != null) tvSelectTitle.setText(userName + "님의 매장 선택");

        // 매장 목록 로드
        new Thread(this::loadStoreList).start();

        // 아이템 클릭 이벤트
        lvStoreList.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, String> selectedStore = storeList.get(position);

            Intent nextIntent;
            if ("A".equals(role)) {
                nextIntent = new Intent(this, AdminActivity.class);
            } else {
                nextIntent = new Intent(this, MenuActivity.class);
            }

            nextIntent.putExtra("userId", userId);
            nextIntent.putExtra("userName", userName);
            nextIntent.putExtra("role", role);
            nextIntent.putExtra("storeId", selectedStore.get("storeId"));
            nextIntent.putExtra("storeName", selectedStore.get("storeName"));

            startActivity(nextIntent);
            finish();
        });
    }

    private void loadStoreList() {
        try {
            String fullUrl = storeUrl + "?id=" + userId;
            Log.d("STORE_URL", "Request: " + fullUrl);

            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if(conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine()) != null) sb.append(line);

                String result = sb.toString().trim();

                // 🚨 핵심 수정: 서버가 보내는 [ ] 배열을 바로 파싱합니다.
                JSONArray dataArray = new JSONArray(result);
                storeList.clear();

                for(int i=0; i < dataArray.length(); i++) {
                    JSONObject obj = dataArray.getJSONObject(i);
                    HashMap<String, String> map = new HashMap<>();

                    // 서버 응답 키값인 "id"와 "name"으로 추출
                    String sId = obj.getString("id");
                    String sName = obj.getString("name");

                    map.put("storeId", sId);
                    map.put("storeName", sName);
                    map.put("display", sName + " (" + sId + ")");
                    storeList.add(map);
                }

                runOnUiThread(() -> {
                    if (storeList.isEmpty()) {
                        Toast.makeText(this, "가입된 매장이 없습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        SimpleAdapter adapter = new SimpleAdapter(
                                this, storeList, android.R.layout.simple_list_item_1,
                                new String[]{"display"}, new int[]{android.R.id.text1}
                        );
                        lvStoreList.setAdapter(adapter);
                    }
                });
            }
        } catch(Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "데이터 오류: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}