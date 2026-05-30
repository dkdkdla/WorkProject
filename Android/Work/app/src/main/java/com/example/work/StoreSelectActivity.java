package com.example.work;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
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

public class StoreSelectActivity extends AppCompatActivity {

    private ListView lvStoreList;
    private TextView tvSelectTitle;
    private String userId, userName, role;
    private ArrayList<HashMap<String, String>> storeList = new ArrayList<>();

    private final String storeUrl = AppConfig.API_STORE_LIST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_select);

        lvStoreList   = findViewById(R.id.lvStoreList);
        tvSelectTitle = findViewById(R.id.tvSelectTitle);

        Intent intent = getIntent();
        userId   = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");
        role     = intent.getStringExtra("role");

        if (userName != null) tvSelectTitle.setText(userName + "님의 매장 선택");

        new Thread(this::loadStoreList).start();

        lvStoreList.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, String> selected = storeList.get(position);

            Intent nextIntent;
            if ("A".equals(role)) {
                nextIntent = new Intent(this, AdminActivity.class);
            } else {
                nextIntent = new Intent(this, MenuActivity.class);
            }

            nextIntent.putExtra("userId",    userId);
            nextIntent.putExtra("userName",  userName);
            nextIntent.putExtra("role",      role);
            nextIntent.putExtra("storeId",   selected.get("storeId"));
            nextIntent.putExtra("storeName", selected.get("storeName"));

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
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                String result = sb.toString().trim();
                JSONArray dataArray = new JSONArray(result);
                storeList.clear();

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject obj = dataArray.getJSONObject(i);
                    String sId   = obj.getString("id");
                    String sName = obj.getString("name");

                    HashMap<String, String> map = new HashMap<>();
                    map.put("storeId",   sId);
                    map.put("storeName", sName);
                    map.put("display",   sName + " (" + sId + ")");
                    storeList.add(map);
                }

                runOnUiThread(() -> {
                    if (storeList.isEmpty()) {
                        // 매장이 없거나 모두 PENDING 상태인 경우
                        if ("A".equals(role)) {
                            Toast.makeText(this,
                                    "활성화된 매장이 없습니다.\n매장 생성 신청 후 전체관리자 승인을 기다려주세요.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,
                                    "소속된 매장이 없습니다.\n매장 소속 신청 후 점장 승인을 기다려주세요.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        lvStoreList.setAdapter(new android.widget.BaseAdapter() {
                            @Override public int getCount() { return storeList.size(); }
                            @Override public Object getItem(int pos) { return storeList.get(pos); }
                            @Override public long getItemId(int pos) { return pos; }
                            @Override
                            public android.view.View getView(int pos, android.view.View cv, android.view.ViewGroup p) {
                                if (cv == null) cv = android.view.LayoutInflater.from(StoreSelectActivity.this)
                                        .inflate(R.layout.item_store_select, p, false);
                                java.util.HashMap<String,String> item = storeList.get(pos);
                                android.widget.TextView tvName = cv.findViewById(R.id.tvSelectStoreName);
                                android.widget.TextView tvId   = cv.findViewById(R.id.tvSelectStoreId);
                                if (tvName != null) tvName.setText(item.get("storeName"));
                                if (tvId   != null) tvId.setText(item.get("storeId"));
                                return cv;
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "데이터 오류: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}