package com.example.work;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etId, etPw, etPwConfirm, etName, etPhone, etWage, etStoreId;
    private Button btnBirthPicker, btnStoreSearch, btnRegister;
    private CheckBox chkIsAdmin;
    private TextView tvAdminNotice;

    private String selectedBirth  = "";
    private String selectedStoreId   = "";
    private String selectedStoreName = "";

    // 매장 검색 결과 저장
    private ArrayList<String[]> storeSearchList = new ArrayList<>(); // [0]:id, [1]:name

    private final String url = AppConfig.API_REGISTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();

        // 생년월일 DatePicker
        btnBirthPicker.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        selectedBirth = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
                        btnBirthPicker.setText("생년월일: " + selectedBirth);
                        btnBirthPicker.setTextColor(0xFF212121);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
        });

        // 매장 검색 BottomSheet
        btnStoreSearch.setOnClickListener(v -> showStoreSearchSheet());

        // 관리자 체크박스
        chkIsAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvAdminNotice.setVisibility(View.VISIBLE);
                btnStoreSearch.setVisibility(View.GONE);
                selectedStoreId   = "";
                selectedStoreName = "";
                etWage.setVisibility(View.GONE);
            } else {
                tvAdminNotice.setVisibility(View.GONE);
                btnStoreSearch.setVisibility(View.VISIBLE);
                etWage.setVisibility(View.VISIBLE);
            }
        });

        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void initView() {
        etId           = findViewById(R.id.etRegId);
        etPw           = findViewById(R.id.etRegPw);
        etPwConfirm    = findViewById(R.id.etRegPwConfirm);
        etName         = findViewById(R.id.etRegName);
        etPhone        = findViewById(R.id.etRegPhone);
        etWage         = findViewById(R.id.etWage);
        etStoreId      = findViewById(R.id.etStoreId);
        btnBirthPicker = findViewById(R.id.btnBirthPicker);
        btnStoreSearch = findViewById(R.id.btnStoreSearch);
        chkIsAdmin     = findViewById(R.id.chkIsAdmin);
        tvAdminNotice  = findViewById(R.id.tvAdminNotice);
        btnRegister    = findViewById(R.id.btnRegisterAction);
    }

    // BottomSheetDialog 매장 검색
    private void showStoreSearchSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_store_search, null);
        sheet.setContentView(view);

        EditText etSearch = view.findViewById(R.id.etSheetSearch);
        ListView lvResult = view.findViewById(R.id.lvSheetResult);
        TextView tvEmpty  = view.findViewById(R.id.tvSheetEmpty);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<>());
        lvResult.setAdapter(adapter);

        // 실시간 검색
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.length() >= 1) {
                    new Thread(() -> searchStore(keyword, adapter, lvResult, tvEmpty)).start();
                } else {
                    storeSearchList.clear();
                    runOnUiThread(() -> {
                        adapter.clear();
                        tvEmpty.setVisibility(View.VISIBLE);
                        lvResult.setVisibility(View.GONE);
                    });
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 매장 선택
        lvResult.setOnItemClickListener((parent, v2, position, id) -> {
            if (position < storeSearchList.size()) {
                String[] store = storeSearchList.get(position);
                selectedStoreId   = store[0];
                selectedStoreName = store[1];
                btnStoreSearch.setText("선택된 매장: " + selectedStoreName + " (" + selectedStoreId + ")");
                btnStoreSearch.setTextColor(0xFF1565C0);
                etStoreId.setText(selectedStoreId);
                sheet.dismiss();
            }
        });

        sheet.show();
    }

    // SearchStore 서블릿 호출
    private void searchStore(String keyword, ArrayAdapter<String> adapter,
                             ListView lvResult, TextView tvEmpty) {
        try {
            String apiUrl = AppConfig.BASE_URL + "SearchStore?keyword=" +
                    java.net.URLEncoder.encode(keyword, "UTF-8");
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
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
                        storeSearchList.add(new String[]{
                                obj.getString("id"),
                                obj.getString("name")
                        });
                        displayList.add(obj.getString("name") + " (" + obj.getString("id") + ")");
                    }
                }

                runOnUiThread(() -> {
                    adapter.clear();
                    adapter.addAll(displayList);
                    adapter.notifyDataSetChanged();
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
            runOnUiThread(() -> Toast.makeText(this, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
        }
    }

    private void validateAndRegister() {
        String id      = etId.getText().toString().trim();
        String pw      = etPw.getText().toString().trim();
        String pwCheck = etPwConfirm.getText().toString().trim();
        String name    = etName.getText().toString().trim();
        String phone   = etPhone.getText().toString().trim();
        String wage    = etWage.getText().toString().trim();
        boolean isAdmin = chkIsAdmin.isChecked();

        if (id.isEmpty() || pw.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "필수 정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedBirth.isEmpty()) {
            Toast.makeText(this, "생년월일을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pw.equals(pwCheck)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        registerRequest(id, pw, name, phone, wage, selectedStoreId, isAdmin ? "A" : "S");
    }

    private void registerRequest(String id, String pw, String name, String phone,
                                 String wage, String storeId, String role) {
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        String status   = json.optString("status", "fail");
                        String message  = json.optString("message", "가입 처리에 실패했습니다.");
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        if ("success".equals(status)) finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "데이터 분석 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(RegisterActivity.this, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id",      id);
                params.put("pw",      pw);
                params.put("name",    name);
                params.put("phone",   phone);
                params.put("role",    role);
                params.put("storeId", storeId);
                params.put("wage",    wage);
                params.put("birth",   selectedBirth);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}