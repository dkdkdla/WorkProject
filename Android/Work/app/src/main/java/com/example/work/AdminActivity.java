package com.example.work;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * [설계 설명]
 * 이 액티비티는 관리자(점장)의 메인 대시보드입니다.
 * 직접적인 API 호출보다는 각 서블릿 기반 API와 통신하는 하위 액티비티들로
 * 사용자 정보(ID, 매장코드 등)를 안전하게 전달하는 역할을 합니다.
 */
public class AdminActivity extends AppCompatActivity {

    private TextView tvTitle;
    private Button btnManageWork, btnLogout, btnChangeStore;
    private Button btnAdminProfile, btnAdminBoard, btnAdminEdit, btnSalary;

    private String userId, userName, role;
    private String myStoreId, myStoreName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // 1. 뷰 초기화
        initView();

        // 2. 인텐트 데이터 수신 (로그인 후 전달된 정보)
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");
        role = intent.getStringExtra("role");
        myStoreId = intent.getStringExtra("storeId");
        myStoreName = intent.getStringExtra("storeName");

        // 3. 상단 타이틀 설정
        if (userName != null) {
            tvTitle.setText(String.format("%s 점장님\n(%s)", userName, (myStoreName != null ? myStoreName : "매장 미선택")));
        }

        // 4. 클릭 리스너 설정
        setupListeners();
    }

    private void initView() {
        tvTitle = findViewById(R.id.tvAdminTitle);
        btnManageWork = findViewById(R.id.btnManageWork);
        btnLogout = findViewById(R.id.btnAdminLogout);
        btnChangeStore = findViewById(R.id.btnAdminChangeStore);
        btnAdminProfile = findViewById(R.id.btnAdminProfile);
        btnAdminEdit = findViewById(R.id.btnAdminEdit);
        btnSalary = findViewById(R.id.btnGoSalary);
        btnAdminBoard = findViewById(R.id.btnAdminBoard);
    }

    private void setupListeners() {
        // 매장 변경 이동
        btnChangeStore.setOnClickListener(v -> {
            Intent changeIntent = new Intent(AdminActivity.this, StoreSelectActivity.class);
            changeIntent.putExtra("userId", userId);
            changeIntent.putExtra("userName", userName);
            changeIntent.putExtra("role", role);
            startActivity(changeIntent);
            finish();
        });

        // 근태 이력 관리 이동 (AdminHistoryActivity)
        btnManageWork.setOnClickListener(v -> {
            if (checkStoreId()) {
                Intent goHistory = new Intent(AdminActivity.this, AdminHistoryActivity.class);
                goHistory.putExtra("storeId", myStoreId);
                startActivity(goHistory);
            }
        });

        // 관리자 프로필 관리 이동 (ProfileActivity)
        btnAdminProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(AdminActivity.this, ProfileActivity.class);
            profileIntent.putExtra("userId", userId);
            profileIntent.putExtra("role", role);
            startActivity(profileIntent);
        });

        // 급여 관리 이동 (AdminSalaryActivity)
        btnSalary.setOnClickListener(v -> {
            if (checkStoreId()) {
                Intent goSalary = new Intent(AdminActivity.this, AdminSalaryActivity.class);
                goSalary.putExtra("storeId", myStoreId);
                startActivity(goSalary);
            }
        });

        // 직원 정보 관리 이동 (AdminMemberActivity)
        btnAdminEdit.setOnClickListener(v -> {
            if (checkStoreId()) {
                Intent memberIntent = new Intent(AdminActivity.this, AdminMemberActivity.class);
                memberIntent.putExtra("storeId", myStoreId);
                startActivity(memberIntent);
            }
        });

        // 소통 게시판 이동 (BoardListActivity)
        btnAdminBoard.setOnClickListener(v -> {
            if (checkStoreId()) {
                Intent boardIntent = new Intent(AdminActivity.this, BoardListActivity.class);
                boardIntent.putExtra("userId", userId);
                boardIntent.putExtra("storeId", myStoreId);
                boardIntent.putExtra("role", role);
                startActivity(boardIntent);
            }
        });

        // 로그아웃 (스택 클리어)
        btnLogout.setOnClickListener(v -> {
            Intent goMain = new Intent(AdminActivity.this, MainActivity.class);
            goMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goMain);
            Toast.makeText(AdminActivity.this, "정상적으로 로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 매장 정보 유무를 체크하는 공통 메서드
     */
    private boolean checkStoreId() {
        if (myStoreId == null || myStoreId.isEmpty()) {
            Toast.makeText(this, "선택된 매장 정보가 없습니다. 매장을 먼저 선택해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}